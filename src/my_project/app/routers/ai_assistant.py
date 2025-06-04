# routers/ai_assistant.py
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File, Form
from sqlalchemy.orm import Session
import os
from openai import OpenAI
from pydantic import BaseModel
from typing import Optional, List
import io
import re
import uuid
import PyPDF2
from .. import models
from ..auth import get_db

router = APIRouter(prefix="/ai", tags=["AI Assistant"])

# 存储生成的判断题及其答案和解释
generated_questions = {}


# 初始化AI客户端
def get_ai_client():
    deepseek_api_key = os.getenv("DEEPSEEK_API_KEY", "sk-c641fc0377ea4b51a7c94cd5f6803b0c")
    if not deepseek_api_key:
        raise HTTPException(status_code=500, detail="服务器未配置 AI 模型 API 密钥")

    return OpenAI(
        api_key=deepseek_api_key,
        base_url="https://api.deepseek.com"
    )


# 提取PDF文字的函数
def extract_pdf_text(file: UploadFile):
    try:
        pdf_reader = PyPDF2.PdfReader(io.BytesIO(file.file.read()))
        full_text = []
        for page in pdf_reader.pages:
            text = page.extract_text()
            if text:
                full_text.append(text)
        return "\n".join(full_text)
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"PDF文件解析失败: {str(e)}")


def convert_mermaid_to_text(mermaid_text):
    """将mermaid格式的思维导图转换为纯文本格式"""
    # 移除mermaid和mindmap/flowchart标记
    lines = mermaid_text.strip().split('\n')
    content_lines = []
    started = False
    
    for line in lines:
        if line.strip().startswith('```'):
            continue
        if line.strip() in ['mermaid', 'mindmap', 'flowchart']:
            continue
        if not started and line.strip():
            started = True
        if started:
            content_lines.append(line)
            
    # 处理内容行
    result_lines = []
    indent_level = {}
    current_level = 0
    
    for line in content_lines:
        # 计算缩进级别
        indent = len(line) - len(line.lstrip())
        content = line.strip()
        
        # 跳过空行
        if not content:
            continue
            
        # 处理根节点
        if "root" in content or "((" in content:
            match = re.search(r'\(\((.*?)\)\)', content)
            if match:
                node_text = match.group(1)
            else:
                node_text = content.replace('root', '').strip()
            result_lines.append(node_text)
            indent_level[indent] = 0
            current_level = 0
            continue
            
        # 其他节点的缩进级别
        if indent in indent_level:
            current_level = indent_level[indent]
        else:
            current_level += 1
            indent_level[indent] = current_level
            
        # 移除markdown样式的符号
        content = re.sub(r'\[.*?\]', '', content)  # 移除[...]
        content = re.sub(r'\(.*?\)', '', content)  # 移除(...)
        content = re.sub(r'\*\*(.*?)\*\*', r'\1', content)  # 移除**粗体**
        content = re.sub(r'\*(.*?)\*', r'\1', content)  # 移除*斜体*
        content = content.strip()
        
        # 添加适当的缩进和符号
        prefix = '  ' * current_level
        if current_level == 1:
            prefix += '├─ '
        else:
            prefix += '│  ' * (current_level - 1) + '├─ '
            
        result_lines.append(prefix + content)
        
    return '\n'.join(result_lines)


class TextQuestionRequest(BaseModel):
    question: str


class PDFQuestionRequest(BaseModel):
    question: str
    pdf_file: UploadFile


class MermaidToTextRequest(BaseModel):
    mermaid_content: str


class TrueFalseQuestion(BaseModel):
    question_id: str
    question: str
    answer: bool
    explanation: str

class TrueFalseResponse(BaseModel):
    question_id: str
    question: str

class TrueFalseAnswerResponse(BaseModel):
    question_id: str
    answer: bool

class TrueFalseExplanationResponse(BaseModel):
    question_id: str
    explanation: str

class TrueFalseQuestionList(BaseModel):
    questions: List[TrueFalseResponse]


@router.post("/ask")
async def ask_text_question(
        request: TextQuestionRequest,
        db: Session = Depends(get_db)
):
    """处理纯文本提问"""
    question_text = request.question.strip()
    if not question_text:
        raise HTTPException(status_code=400, detail="问题内容不能为空")

    try:
        client = get_ai_client()

        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=[
                {"role": "system", "content": "你是一个乐于助人的 AI 助手。"},
                {"role": "user", "content": question_text}
            ],
            stream=False
        )

        answer_text = response.choices[0].message.content


        record = models.AIRequestRecord(question=question_text, answer=answer_text)
        db.add(record)
        db.commit()

        return {"answer": answer_text}

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"服务处理失败: {str(e)}")


@router.post("/ask_with_pdf")
async def ask_with_pdf(
        question: str = Form(...),
        pdf_file: UploadFile = File(...),
        db: Session = Depends(get_db)
):
    """处理带PDF文件上传的提问"""
    # 验证文件类型
    if not pdf_file.filename.lower().endswith(".pdf"):
        raise HTTPException(status_code=400, detail="仅支持PDF格式文件")

    # 提取文字内容
    try:
        extracted_text = extract_pdf_text(pdf_file)
    except HTTPException as he:
        raise he

    # 合并问题和PDF内容
    final_prompt = f"{question.strip()}\n\n相关PDF内容：\n{extracted_text[:3000]}"

    try:
        client = get_ai_client()

        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=[
                {"role": "system", "content": "你是一个擅长分析PDF内容的AI助手。"},
                {"role": "user", "content": final_prompt}
            ],
            stream=False
        )

        answer_text = response.choices[0].message.content

        # 保存记录（仅保存用户原始问题）
        record = models.AIRequestRecord(question=question, answer=answer_text)
        db.add(record)
        db.commit()

        return {
            "answer": answer_text,
            "pdf_summary": f"已分析{len(extracted_text)}字符的PDF内容"
        }

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"服务处理失败: {str(e)}")


@router.post("/generate_mindmap")
async def generate_mindmap_from_pdf(
        pdf_file: UploadFile = File(...),
        map_type: str = Form("mindmap"),  # 默认为思维导图，可选"mindmap"或"flowchart"
        db: Session = Depends(get_db)
):
    """根据PDF文件生成思维导图或流程图"""
    # 验证文件类型
    if not pdf_file.filename.lower().endswith(".pdf"):
        raise HTTPException(status_code=400, detail="仅支持PDF格式文件")
    
    # 验证图表类型
    if map_type not in ["mindmap", "flowchart"]:
        raise HTTPException(status_code=400, detail="图表类型仅支持'mindmap'(思维导图)或'flowchart'(流程图)")
    
    # 提取文字内容
    try:
        extracted_text = extract_pdf_text(pdf_file)
    except HTTPException as he:
        raise he
    
    # 根据类型设置不同的提示词
    map_type_zh = "思维导图" if map_type == "mindmap" else "流程图"
    
    # 构建提示词
    prompt = f"""
    请基于以下PDF内容，生成一个详细的{map_type_zh}。
    
    {map_type_zh}应该:
    1. 使用纯文本格式（不要使用markdown或mermaid语法）
    2. 使用缩进、空格、ASCII字符（如│, ├, ─, └等）或简单的符号来表示层级关系
    3. 捕捉PDF的核心主题和重要概念
    4. 展示概念之间的逻辑关系
    5. 层次分明，结构清晰，直接可读
    
    PDF内容:
    {extracted_text[:4000]}
    """
    
    try:
        client = get_ai_client()
        
        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=[
                {"role": "system", "content": f"你是一个擅长将PDF内容转换为{map_type_zh}的AI助手。请使用纯文本格式创建{map_type_zh}，不要使用markdown或mermaid语法。"},
                {"role": "user", "content": prompt}
            ],
            stream=False
        )
        
        mindmap_text = response.choices[0].message.content
        
        # 保存记录
        question = f"为PDF生成{map_type_zh}"
        record = models.AIRequestRecord(question=question, answer=mindmap_text)
        db.add(record)
        db.commit()
        
        return {
            "diagram": mindmap_text,
            "diagram_type": map_type_zh,
            "pdf_summary": f"已分析{len(extracted_text)}字符的PDF内容生成{map_type_zh}"
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"服务处理失败: {str(e)}")


@router.post("/convert_mermaid_to_text")
async def convert_mermaid_diagram_to_text(
    request: MermaidToTextRequest,
    db: Session = Depends(get_db)
):
    """将mermaid格式的思维导图或流程图转换为纯文本格式"""
    if not request.mermaid_content.strip():
        raise HTTPException(status_code=400, detail="内容不能为空")
    
    try:
        text_diagram = convert_mermaid_to_text(request.mermaid_content)
        
        # 记录请求
        question = "转换mermaid图表为纯文本格式"
        record = models.AIRequestRecord(
            question=question, 
            answer=f"已转换为纯文本格式：\n{text_diagram[:100]}..."
        )
        db.add(record)
        db.commit()
        
        return {"text_diagram": text_diagram}
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"转换失败: {str(e)}")


@router.post("/generate_truefalse", response_model=TrueFalseResponse)
async def generate_truefalse_question(
    pdf_file: UploadFile = File(...),
    db: Session = Depends(get_db)
):
    """基于PDF内容生成判断题"""
    # 验证文件类型
    if not pdf_file.filename.lower().endswith(".pdf"):
        raise HTTPException(status_code=400, detail="仅支持PDF格式文件")
    
    # 提取文字内容
    try:
        extracted_text = extract_pdf_text(pdf_file)
    except HTTPException as he:
        raise he
    
    # 构建提示词
    prompt = f"""
    请基于以下PDF内容，生成一个判断题（真/假题）。
    
    你的回答必须严格按照以下格式：
    
    题目：<判断题内容>
    
    答案：<true或false>
    
    解释：<详细解释为什么这个答案是正确的>
    
    确保题目清晰明确，不存在歧义，并与PDF内容直接相关。
    解释部分必须详细说明判断依据。
    
    PDF内容:
    {extracted_text[:4000]}
    """
    
    try:
        client = get_ai_client()
        
        response = client.chat.completions.create(
            model="deepseek-chat",
            messages=[
                {"role": "system", "content": "你是一个擅长出题的AI助手，可以基于PDF内容生成高质量的判断题。你的回答必须包含题目、答案和详细解释三个部分。"},
                {"role": "user", "content": prompt}
            ],
            stream=False
        )
        
        generated_content = response.choices[0].message.content
        
        # 使用更可靠的方式解析生成的内容
        question_text = ""
        answer_text = ""
        explanation_text = ""
        
        # 使用正则表达式提取内容
        question_match = re.search(r'题目[:：](.*?)(?=答案[:：]|$)', generated_content, re.DOTALL)
        answer_match = re.search(r'答案[:：](.*?)(?=解释[:：]|$)', generated_content, re.DOTALL)
        explanation_match = re.search(r'解释[:：](.*?)$', generated_content, re.DOTALL)
        
        if question_match:
            question_text = question_match.group(1).strip()
        if answer_match:
            answer_text = answer_match.group(1).strip()
        if explanation_match:
            explanation_text = explanation_match.group(1).strip()
        
        # 如果正则表达式失败，回退到原始解析方法
        if not question_text or not answer_text:
            lines = generated_content.strip().split('\n')
            current_section = "question"
            
            for line in lines:
                line = line.strip()
                if not line:
                    continue
                    
                if "题目" in line or "判断题" in line:
                    current_section = "question"
                    line = re.sub(r'^题目[:：]', '', line).strip()
                    if line:
                        question_text += line + " "
                    continue
                elif "答案" in line:
                    current_section = "answer"
                    line = re.sub(r'^答案[:：]', '', line).strip()
                    if line:
                        answer_text += line + " "
                    continue
                elif "解释" in line or "说明" in line or "分析" in line:
                    current_section = "explanation"
                    line = re.sub(r'^解释[:：]|^说明[:：]|^分析[:：]', '', line).strip()
                    if line:
                        explanation_text += line + " "
                    continue
                    
                if current_section == "question":
                    question_text += line + " "
                elif current_section == "answer":
                    answer_text += line + " "
                elif current_section == "explanation":
                    explanation_text += line + " "
        
        # 清理和确定答案
        question_text = question_text.strip()
        answer_text = answer_text.strip().lower()
        explanation_text = explanation_text.strip()
        
        # 确定答案
        is_true = False
        if "true" in answer_text or "正确" in answer_text or "是" in answer_text or "对" in answer_text:
            is_true = True
        
        # 生成唯一ID
        question_id = str(uuid.uuid4())
        
        # 存储问题
        question_data = TrueFalseQuestion(
            question_id=question_id,
            question=question_text,
            answer=is_true,
            explanation=explanation_text
        )
        
        generated_questions[question_id] = question_data
        
        # 记录请求
        record = models.AIRequestRecord(
            question=f"生成判断题基于PDF内容",
            answer=f"已生成判断题: {question_text[:100]}..."
        )
        db.add(record)
        db.commit()
        
        return {
            "question_id": question_id,
            "question": question_text
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"生成判断题失败: {str(e)}")


@router.get("/truefalse/{question_id}/answer", response_model=TrueFalseAnswerResponse)
async def get_truefalse_answer(
    question_id: str,
    db: Session = Depends(get_db)
):
    """获取判断题的答案"""
    if question_id not in generated_questions:
        raise HTTPException(status_code=404, detail="找不到该判断题")
    
    question_data = generated_questions[question_id]
    
    return {
        "question_id": question_id,
        "answer": question_data.answer
    }


@router.get("/truefalse/{question_id}/explanation", response_model=TrueFalseExplanationResponse)
async def get_truefalse_explanation(
    question_id: str,
    db: Session = Depends(get_db)
):
    """获取判断题的解释"""
    if question_id not in generated_questions:
        raise HTTPException(status_code=404, detail="找不到该判断题")
    
    question_data = generated_questions[question_id]
    
    # 如果解释为空，重新调用AI生成解释
    if not question_data.explanation or question_data.explanation.strip() == "":
        try:
            client = get_ai_client()
            
            prompt = f"""
            请为以下判断题提供详细的解释：
            
            题目：{question_data.question}
            
            答案：{"正确" if question_data.answer else "错误"}
            
            请提供详细的解释，说明为什么这个答案是正确的，并可以引用相关知识点。
            """
            
            response = client.chat.completions.create(
                model="deepseek-chat",
                messages=[
                    {"role": "system", "content": "你是一个擅长解释问题的AI助手，可以为判断题提供清晰详细的解释。"},
                    {"role": "user", "content": prompt}
                ],
                stream=False
            )
            
            explanation = response.choices[0].message.content.strip()
            
            # 更新存储的解释
            question_data.explanation = explanation
            generated_questions[question_id] = question_data
            
        except Exception as e:
            # 如果重新生成失败，记录错误但继续返回原始结果
            print(f"重新生成解释失败: {str(e)}")
    
    return {
        "question_id": question_id,
        "explanation": question_data.explanation
    }

@router.get("/truefalse/list", response_model=TrueFalseQuestionList)
async def list_truefalse_questions():
    """获取所有已生成的判断题列表"""
    question_list = []
    
    for question_id, question_data in generated_questions.items():
        question_list.append({
            "question_id": question_id,
            "question": question_data.question
        })
    
    return {"questions": question_list}
