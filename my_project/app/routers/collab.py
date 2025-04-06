from fastapi import APIRouter, WebSocket, WebSocketDisconnect, Depends
from typing import Dict, List
from ..auth import SECRET_KEY, ALGORITHM
import jwt
from ..database import SessionLocal
from .. import models

router = APIRouter(tags=["collaboration"])

# 管理每个代码块的活动连接
class ConnectionManager:
    def __init__(self):
        # 字典：codeblock_id -> 该代码块的所有WebSocket连接列表
        self.active_connections: Dict[int, List[WebSocket]] = {}

    async def connect(self, codeblock_id: int, websocket: WebSocket):
        # 新连接加入
        await websocket.accept()
        if codeblock_id not in self.active_connections:
            self.active_connections[codeblock_id] = []
        self.active_connections[codeblock_id].append(websocket)

    def disconnect(self, codeblock_id: int, websocket: WebSocket):
        # 移除断开的连接
        if codeblock_id in self.active_connections:
            self.active_connections[codeblock_id].remove(websocket)
            if not self.active_connections[codeblock_id]:
                # 若该代码块已无连接，清除该项
                del self.active_connections[codeblock_id]

    async def broadcast(self, codeblock_id: int, message: str, sender: WebSocket):
        """将消息广播给同一代码块的其他所有连接"""
        if codeblock_id in self.active_connections:
            for connection in self.active_connections[codeblock_id]:
                if connection != sender:
                    await connection.send_text(message)

manager = ConnectionManager()

@router.websocket("/ws/codeblocks/{block_id}")
async def websocket_codeblock(websocket: WebSocket, block_id: int, token: str = None):
    """
    WebSocket端点：用于代码块协同编辑
    前端应使用: ws://{server}/ws/codeblocks/{block_id}?token={JWT} 进行连接。
    """
    # 读取URL查询参数中的token，用于认证
    token = websocket.query_params.get("token")
    if token is None:
        # 若无token则拒绝连接
        await websocket.close(code=1008)  # Policy Violation
        return
    # 验证token
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        user_id: str = payload.get("sub")
        if user_id is None:
            await websocket.close(code=1008)
            return
    except Exception:
        await websocket.close(code=1008)
        return

    codeblock_id = block_id
    # 检查代码块是否存在
    db = SessionLocal()
    codeblock = db.query(models.CodeBlock).filter(models.CodeBlock.id == codeblock_id).first()
    if not codeblock:
        await websocket.close(code=1008)
        db.close()
        return
    # 获取当前代码初始内容
    current_code = codeblock.content
    db.close()
    # 接受连接
    await manager.connect(codeblock_id, websocket)
    # 发送当前代码内容给新连接用户，以同步初始状态
    if current_code:
        await websocket.send_text(current_code)
    try:
        # 监听来自此连接的消息（新的代码内容）
        while True:
            data = await websocket.receive_text()
            # 更新数据库中代码块内容（保持持久化最新状态）
            db_sess = SessionLocal()
            code_obj = db_sess.query(models.CodeBlock).filter(models.CodeBlock.id == codeblock_id).first()
            if code_obj:
                code_obj.content = data
                db_sess.commit()
            db_sess.close()
            # 广播给其他连接此代码块的用户
            await manager.broadcast(codeblock_id, data, sender=websocket)
    except WebSocketDisconnect:
        # 客户端断开连接
        manager.disconnect(codeblock_id, websocket)