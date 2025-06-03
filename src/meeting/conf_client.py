# client.py
import socket
import json
from util import *
import config
import threading
from datetime import datetime
import time

CHUNK_SIZE = config.CHUNK

from encryption import FeistelCipher, load_key  # 添加这一行

# 加载密钥并初始化加密器
try:
    key = load_key()  # 从文件加载密钥
except FileNotFoundError:
    print("[Error] 密钥文件不存在，请确保服务器已生成密钥并共享给客户端。")
    exit(1)
cipher = FeistelCipher(key)


class ConferenceClient:
    def __init__(
        self,
        text_callback=None,
    ):
        """
        初始化 ConferenceClient
        :param text_callback: 接收到文本消息时调用的回调函数，格式为 callback(sender, message, timestamp)
        """
        self.server_addr = (config.SERVER_IP, config.MAIN_SERVER_PORT)  # 主服务器地址
        self.conference_addr = None  # 会议服务器地址
        self.on_meeting = False  # 是否正在会议中
        self.user_id = None  # 用户ID
        self.conference_id = None  # 当前会议ID
        self.members = []  # 会议成员列表
        self.P2P = False

        self.udp_port = config.UDP_PORT
        self.udp_socket = self._create_udp_socket()

        self.audio_util = AudioStreamUtil(
            channels=config.CHANNELS, rate=config.RATE, chunk_size=1024
        )
        self.is_audio_streaming = False
        self.audio_stream_player = AudioStreamPlayer(
            channels=config.CHANNELS, rate=config.RATE, chunk_size=1024
        )

        self.video_util = CameraStreamUtil()
        self.is_video_streaming = False

        self.screen_util = ScreenStreamUtil()
        self.is_screen_streaming = False

        self.video_stream_player = VideoStreamPlayer()

    def _create_udp_socket(self):
        """
        创建UDP套接字
        """
        udp_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        print(self.conference_addr)
        udp_socket.setblocking(False)
        return udp_socket

    def send_udp_request(self, request, addr):
        """
        发送UDP请求
        """
        if self.P2P:
            for addr in self.members.values():
                try:
                    addr = (addr[0], self.udp_port)
                    message = request
                    action = message.get("action")
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
                    new_request = {
                        "status": "success",
                        "user_id": self.user_id,
                        "type": media_type,
                        media_type: media_content,
                    }
                    self.udp_socket.sendto(json.dumps(new_request).encode(), addr)
                except Exception as e:
                    print(f"[Error] Failed to send UDP request: {e}")
        else:
            try:
                self.udp_socket.sendto(json.dumps(request).encode(), addr)
            except Exception as e:
                print(f"[Error] Failed to send UDP request: {e}")

    def send_request(self, request, addr):
        """
        发送请求到服务器并接收响应
        """
        try:
            with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                s.connect(addr)
                s.sendall(json.dumps(request).encode())
                response = s.recv(config.CHUNK)
                response = json.loads(response.decode())
                return response
        except Exception as e:
            print(f"[Error] Failed to send request: {e}")
            pass

    def start_listener(self):
        """
        启动两个监听线程，持续接收来自服务器的消息和数据
        """

        def listen_for_messages():
            try:
                with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                    print(f"Messages listener connecting to {address}...")
                    s.connect(address)
                    request = {
                        "action": "join_conference",
                        "data": {
                            "user_id": self.user_id,
                        },
                    }
                    s.sendall(json.dumps(request).encode())
                    response = s.recv(config.CHUNK)
                    response = json.loads(response.decode())
                    if response.get("status") == "failed":
                        print(
                            f"[Error] Failed to join conference: {response.get('message')}"
                        )
                        self.on_meeting = False
                        return
                    while self.on_meeting:
                        response = s.recv(config.CHUNK)
                        if not response:
                            break
                        response = json.loads(response.decode())

                        # 处理会议结束通知
                        if response.get("message") == "conference_canceled":
                            print("[Info] 会议已结束，您已被退出会议。")
                            self.conference_id = None
                            self.conference_port = None
                            self.on_meeting = False
                            self.server_addr = (
                                config.SERVER_IP,
                                config.MAIN_SERVER_PORT,
                            )
                            break

                        # 处理接收的文本消息
                        elif response.get("action") == "receive_text":
                            sender = response["data"]["user_id"]
                            encrypted_message = response["data"][
                                "message"
                            ]  # 接收加密消息
                            timestamp = response["data"]["timestamp"]
                            try:
                                decrypted_message = cipher.decrypt(
                                    encrypted_message
                                )  # 解密消息
                            except Exception as e:
                                print(f"[Error] Failed to decrypt message: {e}")
                                continue
                            if self.text_callback:
                                self.text_callback(sender, decrypted_message, timestamp)
                            else:
                                print(f"[{timestamp}] {sender}: {decrypted_message}")

                        # 更新成员名单
                        elif response.get("action") == "update_member_list":
                            member_list = response["data"]["member_list"]
                            print(
                                "会议成员列表:",
                                member_list,
                                "p2p:",
                                response["data"]["p2p"],
                            )
                            self.members = member_list
                            if response["data"]["p2p"]:
                                self.P2P = True
                            else:
                                self.P2P = False

            except Exception as e:

                print(f"[Error] Failed to receive message: {e}")
                time.sleep(1)  # 等待后重试

        def listen_for_data():
            try:
                # with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
                #     print(f"Data listener connecting to {address}...")
                #     s.connect(address)
                #     request = {
                #         "action": "data transfer",
                #         "data": {
                #             "user_id": self.user_id,
                #         },
                #     }
                #     s.sendall(json.dumps(request).encode())
                #     response = s.recv(config.CHUNK)
                #     response = json.loads(response.decode())
                #     if response.get("status") == "failed":
                #         print(
                #             f"[Error] Failed to join conference: {response.get('message')}"
                #         )
                #         self.on_meeting = False
                #         return

                with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
                    print(f"Data listener using UDP to {address}...")
                    s.bind(("0.0.0.0", self.udp_port))
                    s.setblocking(False)

                    while self.on_meeting:
                        # 接收数据
                        try:
                            response, _ = s.recvfrom(config.CHUNK)
                        except Exception as e:
                            continue
                        response = json.loads(response.decode())

                        # 处理不同类型的数据
                        if response.get("status") == "failed":
                            print(
                                f"[Error] Failed to continue meeting: {response.get('message')}"
                            )
                            self.on_meeting = False
                            return

                        user_id = response.get("user_id")

                        # 处理数据传输
                        if response.get("type") == "audio":
                            self.play_received_audio(response.get("audio"), user_id)
                        elif response.get("type") == "video":
                            self.play_received_video(response.get("video"), user_id)
                        elif response.get("type") == "screen":
                            self.play_received_screen(response.get("screen"), user_id)

            except Exception as e:

                print(f"[Error] Failed to receive message: {e}")
                time.sleep(1)

        address = self.conference_addr
        # 启动监听线程
        self.listener_thread = threading.Thread(target=listen_for_messages, daemon=True)
        self.listener_thread.start()
        # 启动数据接收线程
        self.data_thread = threading.Thread(target=listen_for_data, daemon=True)
        self.data_thread.start()

    def create_conference(self, user_id, conference_name):
        """
        创建会议
        """
        if user_id is None:
            self.user_id = input("请输入您的用户ID: ")
            conference_name = input("请输入会议名称: ")
        else:
            self.user_id = user_id

        request = {
            "action": "create_conference",
            "data": {"user_id": self.user_id, "conference_name": conference_name},
        }

        response = self.send_request(request, self.server_addr)
        if response.get("status") == "success":
            self.conference_id = response["data"]["conference_id"]
            self.conference_addr = (
                response["data"]["conference_ip"],
                response["data"]["conference_port"],
            )
            self.on_meeting = True
            print(f"Conference created successfully! ID: {self.conference_id}")
            self.start_listener()
            return self.on_meeting, self.conference_id
        else:
            print(f"[Error] Failed to create conference: {response.get('message')}")
            return False, response.get("message", "Unknown error")

    def ask_for_conference_list(self):
        """
        请求会议列表
        """
        request = {"action": "get_conference_list", "data": {}}
        response = self.send_request(request, self.server_addr)
        if response.get("status") == "success":
            conference_list = response["data"]["conference_list"]
            print("会议列表:")
            for conf in conference_list:
                print(f"ID: {conf['conference_id']}, Name: {conf['conference_name']}")
            return conference_list
        else:
            print(f"[Error] 获取会议列表失败: {response.get('message')}")
            return []

    def ask_for_member_list(self):
        """
        请求会议成员列表
        """
        if not self.on_meeting or not self.conference_id:
            print("[Info] 您当前没有参与任何会议。")
            return

        request = {
            "action": "get_member_list",
            "data": {"conference_id": self.conference_id, "user_id": self.user_id},
        }

        response = self.send_request(request, self.conference_addr)
        if response.get("status") == "success":
            member_list = response["data"]["member_list"]
            print("会议成员列表:")
            for member in member_list:
                print(member)
            return member_list
        else:
            print(f"[Error] 获取会议成员列表失败: {response.get('message')}")
            return []

    def join_conference(self, conference_id, user_id=None):
        """
        加入会议

        :param conference_id: 会议ID
        :param user_id: 用户ID（可选）
        :return: 成功与否
        """
        if user_id is None:
            self.user_id = input("请输入您的用户ID: ")
        else:
            self.user_id = user_id

        request = {
            "action": "join_conference",
            "data": {"user_id": self.user_id, "conference_id": conference_id},
        }

        response = self.send_request(request, self.server_addr)
        if response.get("status") == "success":
            self.conference_id = conference_id
            self.conference_addr = (
                response["data"]["conference_ip"],
                response["data"]["conference_port"],
            )
            self.on_meeting = True
            self.start_listener()
            print(f"Joined conference successfully! ID: {self.conference_id}")
            return True
        else:
            print(f"[Error] Failed to join conference: {response.get('message')}")
            return False

        threading.Thread(target=join_thread, daemon=True).start()

    def quit_conference(self):
        """
        退出当前会议
        """
        if not self.on_meeting or not self.conference_id:
            print("[Info] 您当前没有参与任何会议。")
            return

        request = {
            "action": "quit_conference",
            "data": {"conference_id": self.conference_id, "user_id": self.user_id},
        }

        response = self.send_request(request, self.conference_addr)
        if response.get("status") == "success":
            print(f"成功退出会议: {self.conference_id}")
            self.conference_id = None
            self.on_meeting = False
            self.conference_addr = None
            return True
        else:
            print(f"[Error] 退出会议失败: {response.get('message')}")
            return False

    def cancel_conference(self):
        """
        取消当前会议（需要是会议创建者）
        """
        if not self.on_meeting or not self.conference_id:
            print("[Info] 您当前没有参与任何会议。")
            return

        request = {
            "action": "cancel_conference",
            "data": {"conference_id": self.conference_id, "user_id": self.user_id},
        }

        self.send_request(request, self.conference_addr)
        self.conference_id = None
        self.on_meeting = False
        self.conference_addr = None
        self.server_addr = (config.SERVER_IP, config.MAIN_SERVER_PORT)
        return True

    def start_audio_stream(self):
        """
        启动音频流的捕获与发送
        """
        if self.is_audio_streaming:
            print("[Info] 麦克风已经开启。")
            return

        def send_audio(encoded_audio_data):
            self.send_audio_data(encoded_audio_data)

        self.is_audio_streaming = True
        self.audio_util.capture_audio_stream(send_audio)
        print("[Info] 麦克风已开启，音频流正在捕获并发送。")

    def stop_audio_stream(self):
        """
        停止音频流的捕获与发送
        """
        if not self.is_audio_streaming:
            print("[Info] 麦克风尚未开启。")
            return

        self.audio_util.stop_audio_stream()
        self.is_audio_streaming = False
        print("[Info] 麦克风已关闭，音频流已停止。")

    def send_audio_data(self, audio_data):
        """
        发送音频数据到会议服务器
        """

        request = {
            # TODO: 协议格式待定，这里只是测试用一下
            "action": "send_audio",
            "data": {
                "conference_id": self.conference_id,
                "user_id": self.user_id,
                "audio_data": audio_data,
            },
        }

        # TODO: 完善发送音频数据的逻辑(config.CHUNK大小、解析成功响应等)
        try:
            self.send_udp_request(request, self.conference_addr)

        except Exception as e:
            print(f"[Error] Failed to send audio data: {e}")
            return

    def play_received_audio(self, audio_data, user_id):
        """
        播放接收到的音频数据
        """
        self.audio_stream_player.start_audio_playback(user_id)
        self.audio_stream_player.add_audio_frame(user_id, audio_data)

    def start_video_stream(self):
        """
        启动视频流捕获与发送
        """
        if self.is_video_streaming:
            print("[Info] 摄像头已经开启。")
            return

        def send_video(encoded_video_data):
            self.send_video_data(encoded_video_data)

        self.is_video_streaming = True
        self.video_util.capture_video_stream(send_video)
        print("[Info] 摄像头已开启，视频流正在捕获并发送。")

    def stop_video_stream(self):
        """
        停止视频流捕获与发送
        """
        if not self.is_video_streaming:
            print("[Info] 摄像头尚未开启。")
            return

        self.video_util.stop_video_stream()
        self.is_video_streaming = False
        print("[Info] 摄像头已关闭，视频流已停止。")

    def play_received_video(self, encoded_video_data, user_id):
        """
        播放接收到的视频流
        """
        self.video_stream_player.play_stream(user_id)
        self.video_stream_player.add_frame(user_id, encoded_video_data)

    def send_video_data(self, video_data):
        """
        发送视频数据到会议服务器
        """
        request = {
            # TODO: 协议格式待定，这里只是测试用一下
            "action": "send_video",
            "data": {
                "conference_id": self.conference_id,
                "user_id": self.user_id,
                "video_data": video_data,
            },
        }

        # TODO: 完善发送视频数据的逻辑(config.CHUNK大小、解析成功响应等)
        try:
            self.send_udp_request(request, self.conference_addr)

        except Exception as e:
            print(f"[Error] Failed to send video data: {e}")
            return

    def start_screen_stream(self):
        """
        启动屏幕流捕获与发送
        """
        if self.is_screen_streaming:
            print("[Info] 屏幕共享已经开启。")
            return

        def send_screen(encoded_screen_data):
            self.send_screen_data(encoded_screen_data)

        self.is_screen_streaming = True
        self.screen_util.capture_screen_stream(send_screen)
        print("[Info] 屏幕共享已开启，屏幕流正在捕获并发送。")

    def stop_screen_stream(self):
        """
        停止屏幕流捕获与发送
        """
        if not self.is_screen_streaming:
            print("[Info] 屏幕共享尚未开启。")
            return

        self.screen_util.stop_screen_stream()
        self.is_screen_streaming = False
        print("[Info] 屏幕共享已关闭，屏幕流已停止。")

    def play_received_screen(self, encoded_screen_data, user_id):
        """
        播放接收到的屏幕流
        """
        self.video_stream_player.play_stream(user_id)
        self.video_stream_player.add_frame(user_id, encoded_screen_data)

    def send_screen_data(self, screen_data):
        """
        发送屏幕数据到会议服务器
        """
        request = {
            "action": "send_screen",
            "data": {
                "conference_id": self.conference_id,
                "user_id": self.user_id,
                "screen_data": screen_data,
            },
        }

        try:
            self.send_udp_request(request, self.conference_addr)

        except Exception as e:
            print(f"[Error] Failed to send screen data: {e}")
            return

    def send_text_message(self, message):
        """
        发送文本消息到会议服务器

        :param message: 要发送的消息内容
        """
        if not self.on_meeting or not self.conference_addr:
            print("[Error] 您当前没有参与任何会议。")
            return

        encrypted_message = cipher.encrypt(message)  # 加密消息

        request = {
            "action": "send_text",
            "data": {
                "conference_id": self.conference_id,
                "user_id": self.user_id,
                "message": encrypted_message,  # 发送加密后的消息
            },
        }

        try:
            response = self.send_request(request, self.conference_addr)
            if response.get("status") == "success":
                # 获取当前时间作为时间戳
                current_time = datetime.now().strftime("%H:%M:%S")
                print(f"[{current_time}] 您: {message}")
            else:
                print(f"[Error] 发送文本消息失败: {response.get('message')}")
        except Exception as e:
            print(f"[Error] 发送文本消息时发生错误: {e}")

    def start(self):
        """
        根据命令行输入执行相应的功能
        """
        while True:
            status = f"OnMeeting-{self.conference_id}" if self.on_meeting else "Free"
            cmd_input = (
                input(f'({status}) 请输入操作（输入 "?" 查看帮助）: ').strip().lower()
            )
            fields = cmd_input.split(maxsplit=1)
            recognized = True
            if self.on_meeting:
                if len(fields) == 1:
                    if fields[0] in ("?", "？"):
                        print(config.HELP)
                    elif fields[0] == "quit":
                        self.quit_conference()
                    elif fields[0] == "cancel":
                        self.cancel_conference()
                    elif fields[0] == "list_member":
                        self.ask_for_member_list()
                    elif fields[0] == "start_audio":
                        self.start_audio_stream()
                    elif fields[0] == "stop_audio":
                        self.stop_audio_stream()
                    elif fields[0] == "start_video":
                        self.start_video_stream()
                    elif fields[0] == "stop_video":
                        self.stop_video_stream()
                    elif fields[0] == "start_screen":
                        self.start_screen_stream()
                    elif fields[0] == "stop_screen":
                        self.stop_screen_stream()
                    elif fields[0] == "send_text":
                        message = input("请输入要发送的消息: ").strip()
                        if message:
                            self.send_text_message(message)
                        else:
                            print("[Error] 消息内容不能为空。")
                    else:
                        recognized = False
                elif len(fields) == 2:
                    if fields[0] == "send_text":
                        message = fields[1]
                        if message:
                            self.send_text_message(message)
                        else:
                            print("[Error] 消息内容不能为空。")
                    else:
                        recognized = False
                else:
                    recognized = False

                if not recognized:
                    print(f"[Warn]: 未识别的命令: {cmd_input}")
            else:
                if len(fields) == 1:
                    if fields[0] in ("?", "？"):
                        print(config.HELP)
                    elif fields[0] == "create":
                        self.create_conference(None, None)
                    elif fields[0] == "list_conference":
                        self.ask_for_conference_list()
                    else:
                        recognized = False
                elif len(fields) == 2:
                    if fields[0] == "join":
                        input_conf_id = fields[1]
                        if input_conf_id.isdigit():
                            self.join_conference(input_conf_id)
                        else:
                            print("[Warn]: 会议ID必须是数字形式")
                else:
                    recognized = False

                if not recognized:
                    print(f"[Warn]: 未识别的命令: {cmd_input}")


if __name__ == "__main__":
    client = ConferenceClient()
    client.start()
