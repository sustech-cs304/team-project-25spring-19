from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from .. import models, schemas
from ..auth import get_current_user, get_current_teacher
from ..database import SessionLocal
from typing import List
from ..utils import code_runner  # 引入代码执行工具模块

router = APIRouter(prefix="/codeblocks", tags=["codeblocks"])

@router.post("/", response_model=schemas.CodeBlockOut)
def create_codeblock(data: schemas.CodeBlockCreate,
                     current_user: models.User = Depends(get_current_teacher),
                     db: Session = Depends(SessionLocal)):
    """创建一个新的代码块（仅限老师）"""
    # 检查对应的 PPT 是否存在且属于当前教师
    ppt = db.query(models.PPT).filter(models.PPT.id == data.ppt_id).first()
    if not ppt:
        raise HTTPException(status_code=404, detail="指定的PPT不存在")
    if ppt.uploaded_by != current_user.id:
        # 老师不允许在别人的PPT中添加代码块
        raise HTTPException(status_code=403, detail="无权在该PPT添加代码块")
    # 创建代码块
    codeblock = models.CodeBlock(ppt_id=data.ppt_id, page_number=data.page_number,
                                 language=data.language, content=data.content or "")
    db.add(codeblock)
    db.commit()
    db.refresh(codeblock)
    return codeblock

@router.get("/", response_model=List[schemas.CodeBlockOut])
def list_codeblocks(ppt_id: int,
                    current_user: models.User = Depends(get_current_user),
                    db: Session = Depends(SessionLocal)):
    """获取某个PPT下的所有代码块列表"""
    # 验证PPT存在
    ppt = db.query(models.PPT).filter(models.PPT.id == ppt_id).first()
    if not ppt:
        raise HTTPException(status_code=404, detail="PPT不存在")
    # 如果是老师，只允许访问自己PPT的代码块
    if current_user.role == "teacher" and ppt.uploaded_by != current_user.id:
        raise HTTPException(status_code=403, detail="无权访问该PPT的代码块")
    # 查询该PPT的所有代码块
    blocks = db.query(models.CodeBlock).filter(models.CodeBlock.ppt_id == ppt_id).all()
    return blocks

@router.get("/{block_id}", response_model=schemas.CodeBlockOut)
def get_codeblock(block_id: int,
                  current_user: models.User = Depends(get_current_user),
                  db: Session = Depends(SessionLocal)):
    """获取指定代码块详情（包括代码内容）"""
    codeblock = db.query(models.CodeBlock).filter(models.CodeBlock.id == block_id).first()
    if not codeblock:
        raise HTTPException(status_code=404, detail="代码块不存在")
    # 权限检查：老师不能获取不属于自己的PPT的代码块；学生均可获取（假设公开）
    ppt = db.query(models.PPT).filter(models.PPT.id == codeblock.ppt_id).first()
    if current_user.role == "teacher" and ppt.uploaded_by != current_user.id:
        raise HTTPException(status_code=403, detail="无权查看该代码块")
    return codeblock

@router.post("/{block_id}/run", response_model=schemas.RunCodeResult)
def run_code(block_id: int, req: schemas.RunCodeRequest,
             current_user: models.User = Depends(get_current_user),
             db: Session = Depends(SessionLocal)):
    """运行指定代码块中的代码，返回运行结果"""
    codeblock = db.query(models.CodeBlock).filter(models.CodeBlock.id == block_id).first()
    if not codeblock:
        raise HTTPException(status_code=404, detail="代码块不存在")
    # 对于老师，确保其拥有该PPT，否则无权运行；学生可以运行任意获取到的代码块
    ppt = db.query(models.PPT).filter(models.PPT.id == codeblock.ppt_id).first()
    if current_user.role == "teacher" and ppt.uploaded_by != current_user.id:
        raise HTTPException(status_code=403, detail="无权运行该代码块中的代码")
    # 获取要执行的代码和语言
    code_text = req.code
    language = codeblock.language
    # 调用工具函数，通过Docker运行代码
    try:
        stdout, stderr = code_runner.run_code_in_docker(code_text, language)
    except TimeoutError:
        stdout, stderr = "", "执行超时，代码运行被终止。"
    except Exception as e:
        stdout, stderr = "", f"运行错误: {str(e)}"
    # 保存运行记录到数据库
    run_rec = models.RunRecord(codeblock_id=block_id, user_id=current_user.id,
                               code_content=code_text, stdout=stdout, stderr=stderr)
    # 更新代码块当前内容为执行后的内容（保存最新状态）
    codeblock.content = code_text
    db.add(run_rec)
    db.commit()
    # 返回运行结果
    return {"stdout": stdout, "stderr": stderr}