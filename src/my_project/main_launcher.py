#!/usr/bin/env python3
"""
Web IDE 启动器 (支持PDF处理)
这个脚本用于启动 FastAPI 应用服务器
"""
import uvicorn
import os
import sys
from pathlib import Path

def main():
    """启动 FastAPI 应用"""
    # 获取当前脚本所在目录
    current_dir = Path(__file__).parent.absolute()
    
    # 将项目目录添加到 Python 路径
    if str(current_dir) not in sys.path:
        sys.path.insert(0, str(current_dir))
    
    # 切换到项目目录
    os.chdir(current_dir)
    
    print("正在启动 Web IDE 后端服务...")
    print("现已支持PDF文件处理功能！")
    print("服务地址: http://127.0.0.1:8000")
    print("API 文档: http://127.0.0.1:8000/docs")
    print("按 Ctrl+C 停止服务")
    
    try:
        # 启动 uvicorn 服务器
        uvicorn.run(
            "app.main:app",
            host="127.0.0.1",
            port=8000,
            reload=False,  # 可执行文件中不使用reload
            access_log=True
        )
    except KeyboardInterrupt:
        print("\n正在停止服务...")
    except Exception as e:
        print(f"启动失败: {e}")
        return 1
    
    return 0

if __name__ == "__main__":
    sys.exit(main()) 