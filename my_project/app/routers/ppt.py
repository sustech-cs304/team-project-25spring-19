from fastapi import APIRouter, File, UploadFile, Depends, HTTPException
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session
import os
from typing import List
from .. import models, schemas
from ..auth import get_current_user, get_current_teacher
from ..database import SessionLocal

router = APIRouter(prefix="/ppts", tags=["ppts"])

# 确保上传目录存在
UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@router.post("/upload", response_model=schemas.PPTOut)
async def upload_ppt(file: UploadFile = File(...),
                     current_user: models.User = Depends(get_current_teacher),
                     db: Session = Depends(SessionLocal)):
    """老师上传 PPT(PDF) 文件"""
    # 仅教师可上传 (通过依赖 get_current_teacher 保证)
    # 简单校验文件类型
    filename = file.filename
    if not filename.lower().endswith(".pdf"):
        raise HTTPException(status_code=400, detail="只支持 PDF 文件上传")
    # 读取文件内容
    content = await file.read()
    # 创建 PPT 数据库记录
    ppt = models.PPT(name=filename, file_path="", uploaded_by=current_user.id)
    db.add(ppt)
    db.flush()  # 先写入以获得ppt.id
    # 构造文件保存路径（包含ppt.id确保唯一）
    ppt_path = os.path.join(UPLOAD_DIR, f"ppt_{ppt.id}.pdf")
    # 保存文件到路径
    with open(ppt_path, "wb") as f:
        f.write(content)
    # 更新数据库记录的文件路径
    ppt.file_path = ppt_path
    db.commit()
    db.refresh(ppt)
    return ppt

@router.get("/", response_model=List[schemas.PPTOut])
def list_ppts(current_user: models.User = Depends(get_current_user),
              db: Session = Depends(SessionLocal)):
    """获取PPT列表：老师只能看自己上传的PPT，学生可以看到所有PPT列表"""
    if current_user.role == "teacher":
        ppts = db.query(models.PPT).filter(models.PPT.uploaded_by == current_user.id).all()
    else:  # student
        ppts = db.query(models.PPT).all()
    return ppts

@router.get("/{ppt_id}/content")
def get_ppt_content(ppt_id: int,
                    current_user: models.User = Depends(get_current_user),
                    db: Session = Depends(SessionLocal)):
    """获取指定 PPT 的PDF内容"""
    ppt = db.query(models.PPT).filter(models.PPT.id == ppt_id).first()
    if not ppt:
        raise HTTPException(status_code=404, detail="PPT不存在")
    # 权限：若需要可在此限制学生只能访问特定PPT，这里简化为所有登录用户均可访问
    if not os.path.isfile(ppt.file_path):
        raise HTTPException(status_code=500, detail="PPT文件丢失")
    # 返回PDF文件响应
    return FileResponse(ppt.file_path, media_type="application/pdf")