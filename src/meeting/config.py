HELP = (
    "create         : create an conference\n"
    "join [conf_id ]: join a conference with conference ID\n"
    "quit           : quit an on-going conference\n"
    "cancel         : cancel your on-going conference (only the manager)\n\n"
    "list_conference: list all on-going conferences\n"
    "list_members   : list all members in a conference\n"
)

SERVER_IP = "10.13.80.177"
MAIN_SERVER_PORT = 8888
TIMEOUT_SERVER = 5
# DGRAM_SIZE = 1500  # UDP
LOG_INTERVAL = 2
UDP_PORT = 15565

CHUNK = (
    1024 * 256
)  # 视频流(尤其是屏幕视频流)太大了，后续改了传输方式之后再改回正常的, 这里先改大一点，具体后面分块发送还是别的再讨论
CHANNELS = 1  # Channels for audio capture
RATE = 44100  # Sampling rate for audio capture

camera_width, camera_height = 480, 480  # resolution for camera capture
