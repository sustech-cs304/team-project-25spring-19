# app/main.py
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# 导入路由模块
from .routers import ppt, codeblock, collab
from .auth import router as auth_router
from . import models, database

# 创建 FastAPI 应用实例，并定义全局变量 app
app = FastAPI(
    title="Web IDE Backend",
    description="基于 FastAPI 的网页 IDE 后端系统"
)

# 配置 CORS（开发时允许所有源，生产时建议指定允许的域名）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 初始化数据库，创建所有表（如果不存在）
models.Base.metadata.create_all(bind=database.engine)

# 挂载各个子路由
app.include_router(auth_router)
app.include_router(ppt.router, prefix="/ppts")
app.include_router(codeblock.router, prefix="/codeblocks")
app.include_router(collab.router)  # WebSocket 协作编辑接口

# 根路径简单返回状态信息
@app.get("/")
def read_root():
    return {"message": "Web IDE Backend is running"}
        #   cd my_project/
        #   uvicorn app.main:app --reload