#!/usr/bin/env python3
"""
Web IDE PDF版本 - 自动化打包脚本
将后端代码打包为可执行文件
"""

import subprocess
import sys
import os
import shutil
from pathlib import Path

def run_command(command, description, cwd=None):
    """运行命令并处理错误"""
    print(f"\n🔄 {description}...")
    try:
        result = subprocess.run(
            command, 
            shell=True, 
            check=True, 
            capture_output=True, 
            text=True,
            cwd=cwd
        )
        print(f"✅ {description} 成功完成")
        if result.stdout:
            print(f"输出: {result.stdout[:200]}...")
        return True
    except subprocess.CalledProcessError as e:
        print(f"❌ {description} 失败:")
        print(f"错误代码: {e.returncode}")
        print(f"错误输出: {e.stderr}")
        return False

def check_requirements():
    """检查必要的文件和依赖"""
    print("🔍 检查项目文件...")
    
    current_dir = Path(__file__).parent
    required_files = [
        "main_launcher.py",
        "app/main.py",
        "app/routers/ai_assistant.py",
        "app.db"
    ]
    
    missing_files = []
    for file_path in required_files:
        if not (current_dir / file_path).exists():
            missing_files.append(file_path)
    
    if missing_files:
        print(f"❌ 缺少必要文件: {', '.join(missing_files)}")
        return False
    
    print("✅ 项目文件检查通过")
    return True

def install_pyinstaller():
    """安装PyInstaller"""
    print("📦 检查PyInstaller...")
    try:
        import PyInstaller
        print("✅ PyInstaller已安装")
        return True
    except ImportError:
        print("⚠️  PyInstaller未安装，正在安装...")
        return run_command(
            f"{sys.executable} -m pip install pyinstaller>=6.1.0",
            "安装PyInstaller"
        )

def clean_build_files():
    """清理之前的构建文件"""
    print("🧹 清理构建文件...")
    
    current_dir = Path(__file__).parent
    dirs_to_clean = ["build", "dist", "__pycache__"]
    files_to_clean = ["*.spec"]
    
    for dir_name in dirs_to_clean:
        dir_path = current_dir / dir_name
        if dir_path.exists():
            shutil.rmtree(dir_path)
            print(f"   删除目录: {dir_name}")
    
    # 清理spec文件
    for spec_file in current_dir.glob("*.spec"):
        spec_file.unlink()
        print(f"   删除文件: {spec_file.name}")
    
    print("✅ 构建文件清理完成")

def build_executable():
    """构建可执行文件"""
    current_dir = Path(__file__).parent
    
    # PyInstaller命令参数
    cmd_parts = [
        "pyinstaller",
        "--onefile",                    # 打包成单个文件
        "--name=WebIDE-PDF",           # 可执行文件名称
        "--add-data=app:app",          # 包含app目录
        "--add-data=app.db:.",         # 包含数据库文件
        "--console",                   # 显示控制台输出
        "--clean",                     # 清理缓存
        "--noconfirm",                 # 不确认覆盖
        "main_launcher.py"             # 主入口文件
    ]
    
    command = " ".join(cmd_parts)
    
    return run_command(command, "使用PyInstaller打包应用", cwd=current_dir)

def verify_executable():
    """验证可执行文件"""
    current_dir = Path(__file__).parent
    executable_path = current_dir / "dist" / "WebIDE-PDF"
    
    if not executable_path.exists():
        print("❌ 可执行文件未生成")
        return False
    
    # 检查文件大小
    file_size = executable_path.stat().st_size
    file_size_mb = file_size / (1024 * 1024)
    
    print(f"✅ 可执行文件生成成功")
    print(f"   文件路径: {executable_path}")
    print(f"   文件大小: {file_size_mb:.1f} MB")
    
    # 设置执行权限
    os.chmod(executable_path, 0o755)
    print("✅ 执行权限设置完成")
    
    return True

def create_launcher_script():
    """创建启动脚本"""
    current_dir = Path(__file__).parent
    
    # 创建shell启动脚本
    launcher_content = '''#!/bin/bash
# Web IDE PDF版本启动脚本

echo "🚀 启动 Web IDE (PDF版本)..."
echo "=================================="

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# 运行可执行文件
"$SCRIPT_DIR/dist/WebIDE-PDF"
'''
    
    launcher_path = current_dir / "start_webide.sh"
    with open(launcher_path, 'w', encoding='utf-8') as f:
        f.write(launcher_content)
    
    os.chmod(launcher_path, 0o755)
    print(f"✅ 启动脚本已创建: {launcher_path}")

def main():
    """主函数"""
    print("🚀 Web IDE PDF版本 - 自动化打包程序")
    print("=" * 60)
    
    try:
        # 步骤1: 检查项目文件
        if not check_requirements():
            return False
        
        # 步骤2: 安装PyInstaller
        if not install_pyinstaller():
            return False
        
        # 步骤3: 清理构建文件
        clean_build_files()
        
        # 步骤4: 构建可执行文件
        if not build_executable():
            return False
        
        # 步骤5: 验证可执行文件
        if not verify_executable():
            return False
        
        # 步骤6: 创建启动脚本
        create_launcher_script()
        
        # 成功完成
        print("\n" + "=" * 60)
        print("🎉 打包完成！")
        print("\n📋 生成的文件:")
        print("   • dist/WebIDE-PDF - 主可执行文件")
        print("   • start_webide.sh - 启动脚本")
        
        print("\n🚀 使用方法:")
        print("   方法1: ./dist/WebIDE-PDF")
        print("   方法2: ./start_webide.sh")
        print("   方法3: 双击 dist/WebIDE-PDF")
        
        print("\n🌐 访问地址:")
        print("   • 主服务: http://127.0.0.1:8000")
        print("   • API文档: http://127.0.0.1:8000/docs")
        
        print("\n📄 现在支持PDF文件智能处理功能！")
        
        return True
        
    except KeyboardInterrupt:
        print("\n\n👋 打包被用户中断")
        return False
    except Exception as e:
        print(f"\n❌ 打包过程中出现错误: {e}")
        return False

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1) 