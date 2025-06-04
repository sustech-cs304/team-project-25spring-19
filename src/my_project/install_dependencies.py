#!/usr/bin/env python3
"""
Web IDE PDF版本 - 依赖安装脚本
自动安装项目所需的所有依赖包
"""

import subprocess
import sys
import os
from pathlib import Path

def run_command(command, description):
    """运行命令并处理错误"""
    print(f"\n🔄 {description}...")
    try:
        result = subprocess.run(command, shell=True, check=True, capture_output=True, text=True)
        print(f"✅ {description} 成功完成")
        return True
    except subprocess.CalledProcessError as e:
        print(f"❌ {description} 失败:")
        print(f"错误代码: {e.returncode}")
        print(f"错误输出: {e.stderr}")
        return False

def check_python_version():
    """检查Python版本"""
    version = sys.version_info
    if version.major < 3 or (version.major == 3 and version.minor < 8):
        print("❌ 错误: 需要Python 3.8或更高版本")
        print(f"当前版本: {version.major}.{version.minor}.{version.micro}")
        return False
    print(f"✅ Python版本检查通过: {version.major}.{version.minor}.{version.micro}")
    return True

def install_dependencies():
    """安装项目依赖"""
    current_dir = Path(__file__).parent
    core_requirements = current_dir / "requirements-core.txt"
    full_requirements = current_dir / "requirements.txt"
    
    print("🚀 Web IDE PDF版本 - 依赖安装程序")
    print("=" * 50)
    
    # 检查Python版本
    if not check_python_version():
        return False
    
    # 升级pip
    if not run_command(f"{sys.executable} -m pip install --upgrade pip", "升级pip"):
        print("⚠️  pip升级失败，但将继续安装依赖...")
    
    # 选择安装选项
    print("\n📦 请选择安装选项:")
    print("1. 安装核心依赖 (推荐，只安装运行必需的包)")
    print("2. 安装完整依赖 (包含开发工具)")
    print("3. 退出")
    
    while True:
        choice = input("\n请输入选择 (1-3): ").strip()
        
        if choice == "1":
            # 安装核心依赖
            if core_requirements.exists():
                if run_command(f"{sys.executable} -m pip install -r {core_requirements}", 
                             "安装核心依赖包"):
                    print("\n🎉 核心依赖安装完成！")
                    return True
            else:
                print(f"❌ 找不到核心依赖文件: {core_requirements}")
                return False
                
        elif choice == "2":
            # 安装完整依赖
            if full_requirements.exists():
                if run_command(f"{sys.executable} -m pip install -r {full_requirements}", 
                             "安装完整依赖包"):
                    print("\n🎉 完整依赖安装完成！")
                    return True
            else:
                print(f"❌ 找不到完整依赖文件: {full_requirements}")
                return False
                
        elif choice == "3":
            print("👋 安装已取消")
            return False
            
        else:
            print("❌ 无效选择，请输入1、2或3")

def main():
    """主函数"""
    try:
        if install_dependencies():
            print("\n" + "=" * 50)
            print("🎯 下一步操作:")
            print("1. 运行应用: ./dist/WebIDE-PDF")
            print("2. 或使用源码: uvicorn app.main:app --reload")
            print("3. 访问: http://127.0.0.1:8000")
            print("4. API文档: http://127.0.0.1:8000/docs")
            print("\n📄 现在支持PDF文件智能处理功能！")
        else:
            print("\n❌ 依赖安装失败，请检查错误信息")
            sys.exit(1)
            
    except KeyboardInterrupt:
        print("\n\n👋 安装被用户中断")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ 安装过程中出现意外错误: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main() 