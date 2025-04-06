# 使用 Ubuntu 作为基础镜像
FROM ubuntu:20.04

# 防止交互式安装
ENV DEBIAN_FRONTEND=noninteractive

# 更新APT源并安装所需语言环境和工具
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3 python3-pip python3-dev \
    nodejs npm \
    openjdk-11-jdk-headless \
    gcc g++ make \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# 为容器内执行代码创建工作目录
WORKDIR /code

# 默认命令（空，实际运行时会被指定）
CMD ["bash"]