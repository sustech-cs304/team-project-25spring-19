import base64
import pyaudio

# 测试音频捕获和播放功能
# 直接运行即可，会捕获5秒音频并保存到文件，然后播放该文件中的音频数据
# 如果捕获有问题，把70行encode_audio_file("captured_audio_data.txt")读取的文件路径改为"test_base64_audio.txt",并注释掉38行test_audio_capture()函数仅测试播放功能

def test_audio_capture():
    p = pyaudio.PyAudio()
    stream = p.open(
        format=pyaudio.paInt16,
        channels=1,
        rate=44100,
        input=True,
        frames_per_buffer=1024,
    )

    print("开始捕获音频...")

    frames = []
    for _ in range(0, int(44100 / 1024 * 5)):  # 捕获5秒音频
        data = stream.read(1024)
        frames.append(data)

    print("捕获完毕，正在保存文件...")

    stream.stop_stream()
    stream.close()
    p.terminate()

    # 将捕获到的音频数据先base64编码，然后保存到文件
    encoded_audio_data = base64.b64encode(b"".join(frames))
    with open("captured_audio_data.txt", "wb") as f:
        f.write(encoded_audio_data)
    print("音频文件已保存。")


test_audio_capture()


def test_play_received_audio(encoded_audio_data):
    # 将 Base64 编码的音频数据解码
    audio_data = base64.b64decode(encoded_audio_data)

    # 使用 pyaudio 播放音频
    p = pyaudio.PyAudio()
    stream = p.open(
        format=pyaudio.paInt16,
        channels=1,
        rate=44100,
        output=True,
        frames_per_buffer=1024,
    )

    stream.write(audio_data)
    stream.stop_stream()
    stream.close()

    print("音频播放完毕。")


# 假设这是接收到的音频数据的 Base64 编码
def encode_audio_file(file_path):
    with open(file_path, "rb") as f:
        audio_data = f.read()
    return audio_data


# 使用一个音频文件进行编码测试
encoded_audio_data = encode_audio_file("captured_audio_data.txt")
test_play_received_audio(encoded_audio_data)
