from sqlalchemy import Column, Integer, String, ForeignKey, DateTime, Text
from sqlalchemy.orm import relationship
from datetime import datetime
from .database import Base
from sqlalchemy import Column, Integer, Text

class AIRequestRecord(Base):
    __tablename__ = "ai_request_records"
    id = Column(Integer, primary_key=True, index=True)
    question = Column(Text, nullable=False)   # 提问内容
    answer = Column(Text, nullable=False)     # AI 助手的回答

class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, nullable=False, index=True)
    hashed_password = Column(String, nullable=False)
    role = Column(String, nullable=False)  # "teacher" 或 "student"

class PPT(Base):
    __tablename__ = "ppts"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, nullable=False)          # PPT 文件名称
    file_path = Column(String, nullable=False)     # PPT文件保存路径
    uploaded_by = Column(Integer, ForeignKey("users.id"), nullable=False)  # 上传者用户ID（老师）
    # 可选：建立与用户的关系（老师），便于查询
    uploader = relationship("User")

class CodeBlock(Base):
    __tablename__ = "codeblocks"
    id = Column(Integer, primary_key=True, index=True)
    ppt_id = Column(Integer, ForeignKey("ppts.id"), nullable=False)   # 所属PPT ID
    page_number = Column(Integer, nullable=False)   # 所在PPT页码
    language = Column(String, nullable=False)       # 代码语言: "python", "javascript", "java", "cpp"
    content = Column(Text, nullable=True)           # 代码内容（可为空，表示无初始代码）
    # 代码块与 PPT 的关系
    ppt = relationship("PPT")

class RunRecord(Base):
    __tablename__ = "run_records"
    id = Column(Integer, primary_key=True, index=True)
    codeblock_id = Column(Integer, ForeignKey("codeblocks.id"), nullable=False)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)   # 提交运行的用户ID
    code_content = Column(Text, nullable=False)       # 运行时的代码内容
    stdout = Column(Text, nullable=True)              # 标准输出结果
    stderr = Column(Text, nullable=True)              # 标准错误输出
    run_time = Column(DateTime, default=datetime.utcnow)  # 提交运行的时间

    # 建立关系，方便以后查询（可选）
    codeblock = relationship("CodeBlock")
    user = relationship("User")