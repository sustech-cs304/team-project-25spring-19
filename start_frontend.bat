@echo off
echo 正在启动前端服务...

REM 启动meeting/app.py
start cmd /k "cd meeting && python app.py"

cd Intelligent-IDE
npm run dev

echo 前端服务已启动。 