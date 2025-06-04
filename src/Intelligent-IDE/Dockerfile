FROM dockerproxy.com/library/node:18
ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      curl ca-certificates && \
    apt-get clean && rm -rf /var/lib/apt/lists/*
# 创建非 root 用户和目录
RUN useradd -ms /bin/bash appuser && \
    mkdir -p /home/appuser && \
    chown -R appuser:appuser /home/appuser
# 切换到 appuser
USER appuser
WORKDIR /home/appuser
# 复制 package.json 和 package-lock.json
COPY --chown=appuser:appuser package*.json ./
# 安装依赖
RUN npm install
# 复制项目文件
COPY --chown=appuser:appuser . .
CMD ["npm", "run", "dev"]