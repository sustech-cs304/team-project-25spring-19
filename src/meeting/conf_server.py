# server.py
import asyncio
import json
import config
import socket
from datetime import datetime

from encryption import FeistelCipher, load_key, generate_key  # 添加加密模块

# 加载密钥并初始化加密器
try:
    key = load_key()
except FileNotFoundError:
    key = generate_key()
cipher = FeistelCipher(key)


class MediaDatagramProtocol(asyncio.DatagramProtocol):
    def __init__(self, server):
        self.server = server

    def datagram_received(self, data, addr):
        """收到数据时触发"""
        asyncio.create_task(self.server.handle_media_data(data, addr))

    def error_received(self, exc):
        print(f"Error receiving datagram: {exc}")


class ConferenceServer:
    def __init__(
        self,
        next_port,
        conference_id=None,
        conference_name=None,
        creator_id=None,
        on_cancel_callback=None,
    ):
        self.MEDIA_PORT = (config.UDP_PORT,)
        self.next_port = next_port
        self.conference_id = conference_id or f"{next_port}"
        self.conference_name = conference_name
        self.creator_id = creator_id
        self.message_writer = {}  # 保存客户端的消息写入器
        self.data_writer = {}  # 保存客户端的数据写入器
        self.client_addr = {}  # 保存客户端的地址
        self.client_count = 0  # 记录当前会议的客户端数量
        self.on_cancel_callback = on_cancel_callback  # 回调函数
        self.udp_socket = None
        self.loop = asyncio.get_event_loop()  # 获取事件循环
        self.p2p = False

    def _create_udp_socket(self):
        """创建 UDP 套接字"""
        udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        udp_socket.bind(("0.0.0.0", self.next_port))  # 在同一端口上绑定 UDP
        print(f"UDP Socket bound to port {self.next_port}")
        return udp_socket

    async def handle_client(self, reader, writer):
        """处理客户端的请求"""
        addr = writer.get_extra_info("peername")
        print(f"Client connected from {addr}")
        user_id = None
        try:
            while True:
                data = await reader.read(config.CHUNK)
                if not data:
                    break
                print(f"conferance received data: {data}")  # 调试输出

                try:
                    request = json.loads(data.decode())
                    action = request.get("action")
                    data = request.get("data", {})
                    user_id = data["user_id"]

                    if action == "join_conference":
                        # 处理客户端加入会议
                        self.client_count += 1
                        if user_id in self.message_writer:
                            response = {
                                "status": "failed",
                                "message": f"User {user_id} already joined",
                            }
                        else:
                            self.message_writer[user_id] = writer
                            self.client_addr[user_id] = addr
                            print(
                                f"User {user_id} joined conference {self.conference_id}"
                            )
                            response = {
                                "status": "success",
                                "data": {
                                    "conference_name": self.conference_name,
                                    "conference_id": self.conference_id,
                                    "creator_id": self.creator_id,
                                },
                            }
                            writer.write(json.dumps(response).encode())
                            await writer.drain()
                            size = len(self.client_addr)
                            if size == 2 and not self.p2p:
                                self.p2p = True
                                await self.switch(self.p2p)
                            elif size != 2 and self.p2p:
                                self.p2p = False
                                await self.switch(self.p2p)
                            continue

                    elif action == "data transfer":
                        # 建立数据传输连接
                        if user_id in self.data_writer:
                            response = {
                                "status": "failed",
                                "message": f"User {user_id} already connected",
                            }
                        else:
                            self.data_writer[user_id] = writer
                            print(
                                f"User {user_id} can transfer data with conference {self.conference_id}"
                            )
                            response = {
                                "status": "success",
                                "data": {
                                    "conference_name": self.conference_name,
                                    "conference_id": self.conference_id,
                                    "creator_id": self.creator_id,
                                },
                            }
                    elif action == "send_text":
                        user_id = data["user_id"]
                        encrypted_message = data.get("message", "")
                        if not encrypted_message:
                            response = {
                                "status": "failed",
                                "message": "Message content cannot be empty",
                            }
                        else:
                            # 解密收到的消息
                            try:
                                message = cipher.decrypt(encrypted_message)
                            except Exception as e:
                                response = {
                                    "status": "failed",
                                    "message": f"Decryption failed: {e}",
                                }
                                writer.write(json.dumps(response).encode())
                                await writer.drain()
                                continue

                            timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                            broadcast_message = {
                                "action": "receive_text",
                                "data": {
                                    "user_id": user_id,
                                    "message": cipher.encrypt(message),  # 加密消息
                                    "timestamp": timestamp,
                                },
                            }
                            await self.broadcast(
                                broadcast_message, exclude_user=user_id, ty="message"
                            )
                            print(f"Broadcasted text message from {user_id}")
                            response = {
                                "status": "success",
                                "message": "Text message sent successfully",
                            }

                    elif action == "get_member_list":
                        # 获取会议成员列表
                        member_list = [
                            {"user_id": uid, "user_addr": self.client_addr[uid]}
                            for uid in self.message_writer.keys()
                        ]
                        response = {
                            "status": "success",
                            "data": {"member_list": member_list},
                        }

                    elif action == "quit_conference":
                        # 处理客户端退出会议
                        if not user_id:
                            response = {
                                "status": "failed",
                                "message": "User not joined yet",
                            }
                        else:
                            response = {
                                "status": "success",
                                "message": f"User {user_id} quitting conference",
                            }
                            print(
                                f"User {user_id} is quitting conference {self.conference_id}"
                            )
                            await self.remove_client(user_id, response)

                    elif action == "cancel_conference":
                        # 处理会议取消请求
                        if not user_id:
                            response = {
                                "status": "failed",
                                "message": "User not joined yet",
                            }
                        elif user_id != self.creator_id:
                            response = {
                                "status": "failed",
                                "message": "Only the creator can cancel the conference",
                            }
                        else:
                            response = {
                                "status": "success",
                                "message": "Conference canceled by creator",
                            }
                            print(
                                f"Conference {self.conference_id} is being canceled by creator {user_id}"
                            )
                            await self.cancel_conference()

                    else:
                        response = {
                            "status": "failed",
                            "message": f"Unknown action: {action}",
                        }

                except json.JSONDecodeError:
                    response = {"status": "failed", "message": "Invalid JSON format"}
                except Exception as e:
                    response = {
                        "status": "failed",
                        "message": f"Error processing request: {e}",
                    }

                # 发送响应给客户端
                writer.write(json.dumps(response).encode())
                await writer.drain()

        except ConnectionResetError:
            print(f"ConnectionResetError: Client {addr} disconnected abruptly")
        except Exception as e:
            print(f"Error handling client {addr}: {e}")

    async def switch(self, p2p):
        response = {
            "action": "switch",
            "data": {"p2p": p2p, "member_list": self.client_addr},
        }
        await self.broadcast(response)

    async def close_media(self, user_id):
        response = {
            "action": "close_media",
            "data": {"user_id": user_id},
        }
        await self.broadcast(response)

    async def handle_media_data(self, data, addr):
        """专门处理从 UDP 接收到的媒体数据"""
        try:
            message = json.loads(data.decode())
            user_id = message.get("data").get("user_id")
            action = message.get("action")  # 使用 action 判断数据类型

            # 根据 action 判断媒体类型并处理
            if action == "send_audio":
                media_content = message.get("data").get("audio_data")
                media_type = "audio"
            elif action == "send_video":
                media_content = message.get("data").get("video_data")
                media_type = "video"
            elif action == "send_screen":
                media_content = message.get("data").get("screen_data")
                media_type = "screen"
            else:
                media_content = None
                media_type = None
                print(f"Unknown action: {action}")

            # 如果是有效的媒体数据，广播给所有其他客户端
            if user_id and media_content:
                print(f"Received {media_type} from {user_id}, broadcasting to others.")
                await self.broadcast_media(user_id, media_type, media_content, addr)
            else:
                print(f"Invalid or incomplete media data from {addr}")
        except Exception as e:
            print(f"Error processing UDP data: {e}")

    async def broadcast_media(self, user_id, media_type, media_content, from_addr):
        """广播媒体数据给所有客户端"""
        broadcast_message = {
            "status": "success",
            "user_id": user_id,
            "type": media_type,
            media_type: media_content,
        }

        # 广播消息到所有客户端
        broadcast_data = json.dumps(broadcast_message).encode()
        # ip使用client_addr中的ip，port使用固定的端口
        for addr in self.client_addr.values():
            # if addr[0] != from_addr[0]:
            try:
                self.udp_socket.sendto(broadcast_data, (addr[0], self.MEDIA_PORT[0]))
                print(f"Broadcasted {media_type} to {(addr[0], self.MEDIA_PORT[0])}")
            except Exception as e:
                print(f"Error broadcasting UDP to {addr}: {e}")

    async def broadcast(self, message, exclude_user=None, ty="message"):
        """广播消息给所有客户端，除指定用户"""
        if ty == "message":
            id_writer = self.message_writer
        elif ty == "data":
            id_writer = self.data_writer
            print(self.data_writer)
        broadcast_data = json.dumps(message).encode()
        for uid, writer in id_writer.items():
            if uid != exclude_user:
                try:
                    writer.write(broadcast_data)
                    await writer.drain()
                except Exception as e:
                    print(f"Error broadcasting to {uid}: {e}")

    async def remove_client(self, user_id, response=None):
        """移除客户端连接"""
        writer = self.message_writer.get(user_id)
        if writer:
            try:
                del self.client_addr[user_id]
                if len(self.client_addr) != 2 and self.p2p:
                    self.p2p = False
                    await self.switch(self.p2p)
                else:
                    if len(self.client_addr) == 2 and not self.p2p:
                        self.p2p = True
                        await self.switch(self.p2p)
                self.client_count -= 1
                print(f"User {user_id} left conference {self.conference_id}")
                if response:
                    writer.write(json.dumps(response).encode())
                    await writer.drain()
                writer.close()
                del self.message_writer[user_id]
            except ConnectionResetError as e:
                print(
                    f"[Warning] ConnectionResetError occurred while removing {user_id}: {e}"
                )
            except Exception as e:
                print(f"[Error] Unexpected error during removing {user_id}: {e}")

            # 检查是否需要取消会议
            if self.client_count == 0:
                await self.cancel_conference()

    async def cancel_conference(self):
        """取消会议，关闭所有连接"""
        if self.conference_id:
            cancellation_message = {
                "status": "success",
                "message": "conference_canceled",
            }
            await self.broadcast(cancellation_message)
            # 通知 MainServer 移除会议
            if self.on_cancel_callback:
                print(f"Notifying MainServer to remove conference {self.conference_id}")
                self.on_cancel_callback(self.conference_id)

    async def start(self):
        """启动会议服务器"""
        print(f"Starting ConferenceServer at port {self.next_port}...")
        server = await asyncio.start_server(
            self.handle_client, "0.0.0.0", self.next_port
        )
        print(f"Started Conference Server at {self.next_port}")

        self.udp_socket, _ = await self.loop.create_datagram_endpoint(
            lambda: MediaDatagramProtocol(self), local_addr=("0.0.0.0", self.next_port)
        )
        print(f"Started listening for UDP media on port {self.MEDIA_PORT}")

        async with server:
            await server.serve_forever()

    async def start_udp_listener(self):
        """异步启动 UDP 数据监听"""
        print(f"Starting UDP listener on port {self.MEDIA_PORT}...")
        # 使用 asyncio 启动独立任务来处理 UDP 消息接收
        self.loop.create_task(self.handle_media_via_udp())
        print(f"UDP listener started on port {self.MEDIA_PORT}")


class MainServer:
    def __init__(self, server_ip, main_port):
        self.server_ip = server_ip
        self.main_port = main_port
        self.server = None
        self.conference_servers = {}
        self.next_port = 8889  # 从 8889 开始分配端口

    async def request_handler(self, reader, writer):
        try:
            data = await reader.read(config.CHUNK)
            print(f"Received data: {data}")  # 调试输出
            if data:
                request = json.loads(data.decode())
                action = request.get("action")
                print(f"Action: {action}")

                if action == "create_conference":
                    conference_name = request["data"]["conference_name"]
                    conference_id = conference_name
                    conference_name = request["data"]["conference_name"]
                    assigned_port = self.next_port
                    creator_id = request["data"]["user_id"]

                    # 回调函数
                    def on_conference_canceled(conf_id):
                        if conf_id in self.conference_servers:
                            del self.conference_servers[conf_id]
                            print(f"Conference {conf_id} removed from MainServer.")

                    conference_server = ConferenceServer(
                        assigned_port,
                        conference_id,
                        conference_name,
                        creator_id,
                        on_cancel_callback=on_conference_canceled,
                    )
                    self.next_port += 1
                    asyncio.create_task(conference_server.start())
                    self.conference_servers[conference_server.conference_id] = (
                        conference_server
                    )

                    response = {
                        "status": "success",
                        "data": {
                            "conference_id": conference_server.conference_id,
                            "conference_port": assigned_port,
                            "conference_ip": self.server_ip,
                        },
                    }

                elif action == "get_conference_list":
                    conference_list = [
                        {
                            "conference_id": conf.conference_id,
                            "conference_name": conf.conference_name,
                        }
                        for conf in self.conference_servers.values()
                    ]
                    response = {
                        "status": "success",
                        "data": {"conference_list": conference_list},
                    }

                elif action == "join_conference":
                    conference_id = request["data"]["conference_id"]
                    if conference_id in self.conference_servers:
                        conference_server = self.conference_servers[conference_id]
                        response = {
                            "status": "success",
                            "data": {
                                "conference_port": conference_server.next_port,
                                "conference_ip": self.server_ip,
                            },
                        }
                    else:
                        response = {
                            "status": "failed",
                            "message": "Conference not found",
                        }
                else:
                    response = {
                        "status": "failed",
                        "message": f"Unknown action: {action}",
                    }
                writer.write(json.dumps(response).encode())
                await writer.drain()

        except Exception as e:
            print(f"[Error] Error in request handler: {e}")
            response = {"status": "failed", "message": f"Internal server error: {e}"}
            writer.write(json.dumps(response).encode())
            await writer.drain()

    async def start(self):
        """启动主服务器"""
        server = await asyncio.start_server(
            self.request_handler, self.server_ip, self.main_port
        )
        print(f"Started Main Server at {self.server_ip}:{self.main_port}")
        async with server:
            await server.serve_forever()


if __name__ == "__main__":
    server = MainServer(config.SERVER_IP, config.MAIN_SERVER_PORT)
    asyncio.run(server.start())
