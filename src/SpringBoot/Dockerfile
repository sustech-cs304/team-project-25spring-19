FROM dockerproxy.com/library/ubuntu:22.04

# 安装 JDK、GCC、Python
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    openjdk-17-jdk-headless gcc g++ make \
    python3 python3-pip python3-venv && \
    ln -s /usr/bin/python3 /usr/bin/python && \
    apt-get clean && rm -rf /var/lib/apt/lists/*



# 拷贝 Java 项目 jar
ARG JAR_FILE=target/cs304Project-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /app.jar

# 暴露端口
EXPOSE 8080

# 启动 Spring Boot 项目
ENTRYPOINT ["java", "-jar", "/app.jar"]
