#!/bin/bash
# Web IDE PDF版本启动脚本

echo "🚀 启动 Web IDE (PDF版本)..."
echo "=================================="

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# 运行可执行文件
"$SCRIPT_DIR/dist/WebIDE-PDF"
