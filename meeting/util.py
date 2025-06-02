# util.py
"""
Simple util implementation for video conference
Including data capture, image compression and image overlap
Note that you can use your own implementation as well :)
"""

from io import BytesIO
import pyaudio
import cv2
import pyautogui
import numpy as np
from PIL import Image, ImageGrab
from config import *
import base64
import threading
import time
import mss
import asyncio
import queue

# print warning if no available camera
cap = cv2.VideoCapture(0)
if cap.isOpened():
    can_capture_camera = True
    cap.set(cv2.CAP_PROP_FRAME_WIDTH, camera_width)
    cap.set(cv2.CAP_PROP_FRAME_HEIGHT, camera_height)
else:
    can_capture_camera = False

my_screen_size = pyautogui.size()


def resize_image_to_fit_screen(image, my_screen_size):
    screen_width, screen_height = my_screen_size

    original_width, original_height = image.size

    aspect_ratio = original_width / original_height

    if screen_width / screen_height > aspect_ratio:
        # resize according to height
        new_height = screen_height
        new_width = int(new_height * aspect_ratio)
    else:
        # resize according to width
        new_width = screen_width
        new_height = int(new_width / aspect_ratio)

    # resize the image
    resized_image = image.resize((new_width, new_height), Image.LANCZOS)

    return resized_image


def overlay_camera_images(screen_image, camera_images):
    """
    screen_image: PIL.Image
    camera_images: list[PIL.Image]
    """
    if screen_image is None and camera_images is None:
        print("[Warn]: cannot display when screen and camera are both None")
        return None
    if screen_image is not None:
        screen_image = resize_image_to_fit_screen(screen_image, my_screen_size)

    if camera_images is not None:
        # make sure same camera images
        if not all(img.size == camera_images[0].size for img in camera_images):
            raise ValueError("All camera images must have the same size")

        screen_width, screen_height = (
            my_screen_size if screen_image is None else screen_image.size
        )
        camera_width, camera_height = camera_images[0].size

        # calculate num_cameras_per_row
        num_cameras_per_row = screen_width // camera_width

        # adjust camera_imgs
        if len(camera_images) > num_cameras_per_row:
            adjusted_camera_width = screen_width // len(camera_images)
            adjusted_camera_height = (
                                             adjusted_camera_width * camera_height
                                     ) // camera_width
            camera_images = [
                img.resize(
                    (adjusted_camera_width, adjusted_camera_height), Image.LANCZOS
                )
                for img in camera_images
            ]
            camera_width, camera_height = adjusted_camera_width, adjusted_camera_height
            num_cameras_per_row = len(camera_images)

        # if no screen_img, create a container
        if screen_image is None:
            display_image = Image.fromarray(
                np.zeros((camera_width, my_screen_size[1], 3), dtype=np.uint8)
            )
        else:
            display_image = screen_image
        # cover screen_img using camera_images
        for i, camera_image in enumerate(camera_images):
            row = i // num_cameras_per_row
            col = i % num_cameras_per_row
            x = col * camera_width
            y = row * camera_height
            display_image.paste(camera_image, (x, y))

        return display_image
    else:
        return screen_image


def capture_screen():
    # capture screen with the resolution of display
    # img = pyautogui.screenshot()
    img = ImageGrab.grab()
    return img


def capture_camera():
    # capture frame of camera
    ret, frame = cap.read()
    if not ret:
        raise Exception("Fail to capture frame from camera")
    return Image.fromarray(frame)


def compress_image(image, format="JPEG", quality=85):
    """
    compress image and output Bytes

    :param image: PIL.Image, input image
    :param format: str, output format ('JPEG', 'PNG', 'WEBP', ...)
    :param quality: int, compress quality (0-100), 85 default
    :return: bytes, compressed image data
    """
    img_byte_arr = BytesIO()
    image.save(img_byte_arr, format=format, quality=quality)
    img_byte_arr = img_byte_arr.getvalue()

    return img_byte_arr


class AudioStreamUtil:
    def __init__(self, channels, rate, chunk_size):
        self.channels = channels
        self.rate = rate
        self.chunk_size = chunk_size
        self.audio = pyaudio.PyAudio()
        self.input_stream = None
        self.output_stream = None
        self.is_streaming = False

    def start_audio_capture(self):
        """
        启动音频捕获
        """
        if self.input_stream is None:
            self.input_stream = self.audio.open(
                format=pyaudio.paInt16,
                channels=self.channels,
                rate=self.rate,
                input=True,
                frames_per_buffer=self.chunk_size,
            )
        return self.input_stream

    def stop_audio_capture(self):
        """
        停止音频捕获
        """
        if self.input_stream:
            self.input_stream.stop_stream()
            self.input_stream.close()
            self.input_stream = None

    def capture_audio_stream(self, send_callback):
        """
        持续捕获音频流并通过回调发送
        """
        self.is_streaming = True
        self.start_audio_capture()

        def capture_loop():
            while self.is_streaming:
                data = self.input_stream.read(self.chunk_size)
                # 将 audio_data 转换为 Base64 编码的字符串
                encoded_data = base64.b64encode(data).decode("utf-8")
                send_callback(encoded_data)
                # time.sleep(self.chunk_size / self.rate)

        threading.Thread(target=capture_loop, daemon=True).start()

    def stop_audio_stream(self):
        """
        停止音频流捕获
        """
        self.is_streaming = False
        self.stop_audio_capture()


class CameraStreamUtil:
    def __init__(self, camera_index=0, frame_width=640, frame_height=480, fps=30):
        self.camera_index = camera_index
        self.frame_width = frame_width
        self.frame_height = frame_height
        self.fps = fps
        self.capture = cv2.VideoCapture(self.camera_index)
        self.is_streaming = False

    def start_video_capture(self):
        """
        检查摄像头是否正常工作
        """
        if not self.capture.isOpened():
            raise Exception(f"Camera at index {self.camera_index} failed to open.")
        self.capture.set(cv2.CAP_PROP_FRAME_WIDTH, self.frame_width)
        self.capture.set(cv2.CAP_PROP_FRAME_HEIGHT, self.frame_height)
        return self.capture

    def stop_video_capture(self):
        """
        停止视频捕获
        """
        if self.capture.isOpened():
            self.capture.release()
        self.capture = cv2.VideoCapture(self.camera_index)

    def capture_video_stream(self, send_callback, compress_quality=30):
        """
        持续捕获视频流并通过回调发送
        """
        self.is_streaming = True
        self.start_video_capture()

        def capture_loop():
            while self.is_streaming:
                ret, frame = self.capture.read()
                if not ret:
                    print("[Warn] Failed to read frame from camera.")
                    continue
                # 转换为 PIL 图像
                frame_image = Image.fromarray(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))
                # 压缩图像
                compressed_frame = self.compress_image(
                    frame_image, quality=compress_quality
                )
                # 发送图像数据
                encoded_frame = base64.b64encode(compressed_frame).decode("utf-8")
                send_callback(encoded_frame)
                time.sleep(1 / self.fps)

        threading.Thread(target=capture_loop, daemon=True).start()

    def stop_video_stream(self):
        """
        停止视频流捕获
        """
        self.is_streaming = False
        self.stop_video_capture()

    @staticmethod
    def compress_image(image, format="JPEG", quality=85):
        """
        压缩图像
        """
        img_byte_arr = BytesIO()
        image.save(img_byte_arr, format=format, quality=quality)
        img_byte_arr = img_byte_arr.getvalue()
        return img_byte_arr


class ScreenStreamUtil:
    def __init__(self, fps=60):
        """
        初始化屏幕流设置
        """
        self.fps = fps
        self.screen_size = pyautogui.size()  # 获取屏幕分辨率
        self._lock = threading.Lock()
        self.stop_event = threading.Event()
        self.capture_thread = None

    def capture_screen_stream(self, send_callback, compress_quality=10):
        """
        持续捕获屏幕流并通过回调发送
        """
        self.stop_event.clear()  # 重置停止事件

        def capture_loop():
            while not self.stop_event.is_set():
                try:
                    # 捕获屏幕图像
                    screen_image = ImageGrab.grab()
                    # 调整图像分辨率
                    target_width, target_height = (
                        self.screen_size[0] // 2,
                        self.screen_size[1] // 2,
                    )

                    screen_image = self.resize_image(
                        screen_image, target_width, target_height
                    )

                    # 减少颜色深度
                    screen_image = self.reduce_color_depth(
                        screen_image, palette_size=64
                    )
                    # 压缩图像
                    compressed_image = self.compress_image(
                        screen_image, quality=compress_quality
                    )
                    # 编码为 Base64 并发送
                    encoded_image = base64.b64encode(compressed_image).decode("utf-8")
                    send_callback(encoded_image)

                    # TODO：这里休不休眠都没用，上面的处理时间已经远超过了1/fps，所以后续再优化，这里先把功能实现
                    time.sleep(1 / self.fps)
                except Exception as e:
                    print(f"[Error] Failed to capture screen frame: {e}")

        self.capture_thread = threading.Thread(target=capture_loop, daemon=True)
        self.capture_thread.start()

    def stop_screen_stream(self):
        """
        停止屏幕流捕获
        """
        with self._lock:
            self.stop_event.set()  # 通知线程停止运行

        if self.capture_thread:
            self.capture_thread.join()
            self.capture_thread = None

        cv2.destroyAllWindows()

    @staticmethod
    def reduce_color_depth(image, palette_size=64):
        """
        减少图像颜色深度
        """
        return image.convert("P", palette=Image.ADAPTIVE, colors=palette_size)

    @staticmethod
    def compress_image(image, format="WEBP", quality=85):
        """
        压缩图像
        """
        img_byte_arr = BytesIO()
        image.save(img_byte_arr, format=format, quality=quality)
        img_byte_arr = img_byte_arr.getvalue()
        return img_byte_arr

    @staticmethod
    def resize_image(image, target_width, target_height):
        """
        调整图像分辨率
        """
        resized_image = image.resize(
            (target_width, target_height), Image.Resampling.LANCZOS
        )
        return resized_image

    @staticmethod
    def resize_and_compress_image(image, target_width, target_height, quality=85):
        """
        调整图像分辨率并压缩
        """
        image_np = np.array(image)
        # 调整分辨率
        image_np = cv2.cvtColor(image_np, cv2.COLOR_BGRA2BGR)
        image_np = cv2.resize(image_np, (target_width, target_height))

        # 压缩图像
        encode_param = [cv2.IMWRITE_JPEG_QUALITY, quality]
        result, enc_img = cv2.imencode(".png", image_np, encode_param)
        if result:
            return enc_img.tobytes()
        else:
            raise ValueError("Image compression failed")


class AudioStreamPlayer:
    def __init__(self, channels, rate, chunk_size):
        self.channels = channels
        self.rate = rate
        self.chunk_size = chunk_size
        self.audio = pyaudio.PyAudio()
        self.streams = {}  # 存储每个播放流的信息，格式：{stream_id: {'thread': Thread, 'stop_event': Event, 'frame_queue': Queue}}
        self.lock = threading.Lock()

    def start_audio_playback(self, stream_id):
        """
        开始播放音频流

        :param stream_id: 标识流的唯一ID
        """
        if stream_id in self.streams:
            return  # 如果音频流已经在播放，返回

        stop_event = threading.Event()
        frame_queue = queue.Queue(maxsize=10)  # 队列存储音频数据

        def play_loop():
            # 打开音频输出流
            output_stream = self.audio.open(
                format=pyaudio.paInt16,
                channels=self.channels,
                rate=self.rate,
                output=True,
                frames_per_buffer=self.chunk_size,
            )

            while not stop_event.is_set():
                try:
                    encoded_data = frame_queue.get(timeout=1)  # 等待并从队列中获取音频数据
                    if stop_event.is_set():
                        break
                    decoded_data = base64.b64decode(encoded_data)  # 解码音频数据
                    output_stream.write(decoded_data)  # 播放音频
                except queue.Empty:
                    continue
                except Exception as e:
                    print(f"[Error] Failed to play stream {stream_id}: {e}")
                    break

            # 停止并关闭输出流
            output_stream.stop_stream()
            output_stream.close()
            print(f"[Info] Stream {stream_id} playback stopped.")

        # 启动播放线程
        play_thread = threading.Thread(target=play_loop, daemon=True)
        with self.lock:
            self.streams[stream_id] = {
                "thread": play_thread,
                "stop_event": stop_event,
                "frame_queue": frame_queue,
            }
        play_thread.start()
        print(f"[Info] Stream {stream_id} started.")

    def add_audio_frame(self, stream_id, encoded_audio_data):
        """
        添加音频数据到指定音频流的播放队列。

        :param stream_id: 标识流的唯一ID
        :param encoded_audio_data: Base64 编码的音频数据
        """
        with self.lock:
            stream_info = self.streams.get(stream_id)
            if not stream_info:
                print(f"[Warn] Stream {stream_id} is not playing.")
                return
            try:
                stream_info["frame_queue"].put_nowait(encoded_audio_data)  # 将帧数据放入队列
            except queue.Full:
                print(f"[Warn] Frame queue for stream {stream_id} is full.")

    def stop_audio_stream(self, stream_id):
        """
        停止播放指定的音频流。

        :param stream_id: 标识流的唯一ID
        """
        with self.lock:
            stream_info = self.streams.pop(stream_id, None)
            if stream_info:
                # 设置停止事件，确保线程能够检测到停止标记
                stream_info["stop_event"].set()

        if stream_info:
            # 等待线程退出
            stream_info["thread"].join(timeout=5)  # 设置超时时间，防止无限阻塞
            print(f"[Info] Stream {stream_id} stopped.")
        else:
            print(f"[Warn] Stream {stream_id} is not playing.")

    def stop_all_streams(self):
        """
        停止所有正在播放的音频流。
        """
        with self.lock:
            stream_ids = list(self.streams.keys())
        for stream_id in stream_ids:
            self.stop_audio_stream(stream_id)

    def get_stream_ids(self):
        """
        返回当前所有正在播放的 stream_id 的集合。

        :return: set 包含所有 stream_id。
        """
        with self.lock:
            return set(self.streams.keys())


class VideoStreamPlayer:
    def __init__(self):
        self.streams = (
            {}
        )  # 存储每个播放流的信息，格式：{stream_id: {'thread': Thread, 'stop_event': Event, 'frame_queue': Queue}}
        self.lock = threading.Lock()

    def play_stream(self, stream_id):
        """
        开始播放一个视频流。

        :param stream_id: 标识流的唯一ID。
        """
        if stream_id in self.streams:
            # print(f"[Warn] Stream {stream_id} is already playing.")
            return

        stop_event = threading.Event()
        frame_queue = queue.Queue(maxsize=10)  # 队列存储帧数据

        def play_loop():
            while not stop_event.is_set():
                try:
                    encoded_data = frame_queue.get(timeout=1)
                    if stop_event.is_set():
                        break
                    decoded_data = base64.b64decode(encoded_data)
                    image = Image.open(BytesIO(decoded_data))
                    frame = cv2.cvtColor(np.array(image), cv2.COLOR_RGB2BGR)
                    cv2.imshow(f"{stream_id}", frame)

                    if cv2.waitKey(1) & 0xFF == ord("q"):
                        print(f"[Info] Stream {stream_id} closed by user.")
                        break
                except queue.Empty:
                    continue
                except Exception as e:
                    continue
                    # print(f"[Error] Failed to play frame for stream {stream_id}: {e}")
                    # break

            # 直接退出循环，不再调用 stop_stream
            cv2.destroyWindow(f"{stream_id}")
            print(f"[Info] Exiting play_loop for stream {stream_id}.")

        # 启动播放线程
        play_thread = threading.Thread(target=play_loop, daemon=True)
        with self.lock:
            self.streams[stream_id] = {
                "thread": play_thread,
                "stop_event": stop_event,
                "frame_queue": frame_queue,
            }
        play_thread.start()
        print(f"[Info] Stream {stream_id} started.")

    def add_frame(self, stream_id, encoded_frame):
        """
        添加一帧数据到指定视频流。

        :param stream_id: 标识流的唯一ID。
        :param encoded_frame: Base64 编码的帧数据。
        """
        with self.lock:
            stream_info = self.streams.get(stream_id)
            if not stream_info:
                print(f"[Warn] Stream {stream_id} is not playing.")
                return
            try:
                stream_info["frame_queue"].put_nowait(encoded_frame)
            except Exception as e:
                print(f"[Warn] Frame queue for stream {stream_id} is full: {e}")

    def stop_stream(self, stream_id):
        with self.lock:
            stream_info = self.streams.pop(stream_id, None)
            if stream_info:
                # 先设置停止事件，确保线程能够检测到
                stream_info["stop_event"].set()

        if stream_info:
            # 等待线程退出
            stream_info["thread"].join(timeout=5)  # 设置超时时间，防止无限阻塞

            # 检查窗口是否存在后再销毁
            window_name = f"{stream_id}"
            if cv2.getWindowProperty(window_name, cv2.WND_PROP_VISIBLE) >= 1:
                cv2.destroyWindow(window_name)
            else:
                print(f"[Warn] Window '{window_name}' does not exist.")
            print(f"[Info] Stream {stream_id} stopped.")
        else:
            print(f"[Warn] Stream {stream_id} is not playing.")

    def stop_all_streams(self):
        """
        停止所有正在播放的视频流。
        """
        with self.lock:
            stream_ids = list(self.streams.keys())
        for stream_id in stream_ids:
            self.stop_stream(stream_id)

    def get_stream_ids(self):
        """
        返回当前所有正在播放的 stream_id 的集合。

        :return: set 包含所有 stream_id。
        """
        with self.lock:
            return set(self.streams.keys())
