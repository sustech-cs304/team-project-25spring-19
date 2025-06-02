@echo off
echo 正在启动后端服务...

REM 启动SpringBoot服务
start cmd /k "cd SpringBoot && mvn spring-boot:run"

REM 启动meeting/conf_server.py
start cmd /k "cd meeting && python conf_server.py"

REM 激活虚拟环境并启动FastAPI后端
start cmd /k "cd my_project && venv\Scripts\activate && uvicorn app.main:app --reload"

echo 后端服务启动指令已发送。请查看新打开的终端窗口以确认服务状态。 