from pydantic import BaseModel
from typing import Optional, List
from datetime import datetime

# 用户注册/登录数据模型
class UserCreate(BaseModel):
    username: str
    password: str
    role: str  # "teacher" or "student"

class UserLogin(BaseModel):
    username: str
    password: str

# 用户响应模型（不包含密码）
class UserOut(BaseModel):
    id: int
    username: str
    role: str
    class Config:
        orm_mode = True  # 支持ORM对象直接转化


class PPTContentOut(BaseModel):
    content: str

# PPT 上传和获取模型
class PPTOut(BaseModel):
    id: int
    name: str
    uploaded_by: int
    class Config:
        orm_mode = True

# 代码块创建和返回模型
class CodeBlockCreate(BaseModel):
    ppt_id: int
    page_number: int
    language: str
    content: Optional[str] = ""  # 初始代码内容可选

class CodeBlockOut(BaseModel):
    id: int
    ppt_id: int
    page_number: int
    language: str
    content: str
    class Config:
        orm_mode = True

# 运行代码请求/响应模型
class RunCodeRequest(BaseModel):
    code: str  # 要运行的代码内容

class RunCodeResult(BaseModel):
    stdout: Optional[str] = None
    stderr: Optional[str] = None


class PPTProgressBase(BaseModel):
    user_id: int
    ppt_id: int

class PPTProgressCreate(PPTProgressBase):
    is_viewed: bool

class PPTProgressOut(PPTProgressBase):
    is_viewed: bool

    class Config:
        orm_mode = True
