# app/main.py
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

# 导入所有路由
from .routers import ai_assistant, ppt, codeblock, collab
from .auth import router as auth_router

# 导入模型和数据库模块
from . import models, database

# 创建 FastAPI 应用实例
app = FastAPI(
    title="Web IDE Backend",
    description="基于 FastAPI 的网页 IDE 后端系统"
)

# 配置 CORS（开发阶段允许所有源，生产环境请修改）
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 初始化数据库：创建所有表（包括新添加的 AIRequestRecord）
models.Base.metadata.create_all(bind=database.engine)

# 挂载各个子路由
app.include_router(auth_router)
app.include_router(ppt.router, prefix="/ppts")
app.include_router(codeblock.router, prefix="/codeblocks")
app.include_router(collab.router)  # WebSocket 协作编辑接口
app.include_router(ai_assistant.router, prefix="/ai")
#app.include_router(presentation.router, prefix="/presentations")


# 根路径简单返回服务状态
@app.get("/")
def read_root():
    return {"message": "Web IDE Backend is running"}

# 启动命令示例（在命令行执行）：
#   uvicorn app.main:app --reload
