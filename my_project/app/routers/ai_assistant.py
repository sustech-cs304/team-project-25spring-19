# routers/ai_assistant.py
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
import os
from openai import OpenAI
from pydantic import BaseModel
from .. import models
from ..auth import get_db

router = APIRouter(prefix="/ai", tags=["AI Assistant"])


# 创建全局客户端实例
def get_ai_client():
    deepseek_api_key = os.getenv("DEEPSEEK_API_KEY", "sk-c641fc0377ea4b51a7c94cd5f6803b0c")
    if not deepseek_api_key:
        raise HTTPException(status_code=500, detail="服务器未配置 AI 模型 API 密钥")

    return OpenAI(
        api_key=deepseek_api_key,
        base_url="https://api.deepseek.com"
    )


class QuestionRequest(BaseModel):
    question: str


@router.post("/ask")
async def ask_ai(request: QuestionRequest, db: Session = Depends(get_db)):
    question_text = request.question.strip()
    if not question_text:
        raise HTTPException(status_code=400, detail="问题内容不能为空")

    try:
        client = get_ai_client()  # 使用统一方法获取客户端

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
        try:
            db.commit()
        except Exception as db_error:
            db.rollback()
            raise HTTPException(status_code=500, detail=f"数据库保存失败: {str(db_error)}")

        return {"answer": answer_text}

    except HTTPException as he:
        raise he
    except Exception as e:
        raise HTTPException(status_code=502, detail=f"调用 AI 模型服务失败: {str(e)}")