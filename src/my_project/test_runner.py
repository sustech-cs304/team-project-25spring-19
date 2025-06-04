#!/usr/bin/env python3
"""
Web IDE PDF项目 - 自动化测试运行器
运行完整的测试套件并生成覆盖率报告
"""

import subprocess
import sys
import os
from pathlib import Path
import webbrowser

def run_command(command, description):
    """运行命令并处理错误"""
    print(f"\n🔄 {description}...")
    try:
        result = subprocess.run(
            command, 
            shell=True, 
            check=True, 
            capture_output=True, 
            text=True
        )
        print(f"✅ {description} 成功完成")
        return True, result.stdout
    except subprocess.CalledProcessError as e:
        print(f"❌ {description} 失败:")
        print(f"错误代码: {e.returncode}")
        print(f"错误输出: {e.stderr}")
        return False, e.stderr

def install_test_dependencies():
    """安装测试依赖"""
    test_requirements = [
        "pytest>=7.4.3",
        "pytest-asyncio>=0.21.1", 
        "pytest-cov>=4.1.0",
        "httpx>=0.25.0",
        "coverage>=7.0.0"
    ]
    
    for package in test_requirements:
        success, output = run_command(
            f"{sys.executable} -m pip install {package}",
            f"安装 {package}"
        )
        if not success:
            print(f"⚠️  安装 {package} 失败，但继续...")

def run_tests():
    """运行测试套件"""
    current_dir = Path(__file__).parent
    
    print("🚀 Web IDE PDF项目 - 自动化测试")
    print("=" * 60)
    
    # 安装测试依赖
    install_test_dependencies()
    
    # 运行不同类型的测试
    test_commands = [
        {
            "command": "python -m pytest tests/ -v --tb=short",
            "description": "运行所有测试"
        },
        {
            "command": "python -m pytest tests/ --cov=app --cov-report=html --cov-report=term-missing",
            "description": "运行测试并生成覆盖率报告"
        },
        {
            "command": "python -m pytest tests/test_main.py -v",
            "description": "运行主应用测试"
        },
        {
            "command": "python -m pytest tests/test_ai_assistant.py -v",
            "description": "运行AI助手测试"
        },
        {
            "command": "python -m pytest tests/test_models.py -v",
            "description": "运行数据库模型测试"
        },
        {
            "command": "python -m pytest tests/test_integration.py -v",
            "description": "运行集成测试"
        }
    ]
    
    results = []
    
    for test_cmd in test_commands:
        success, output = run_command(
            test_cmd["command"], 
            test_cmd["description"]
        )
        results.append({
            "description": test_cmd["description"],
            "success": success,
            "output": output
        })
        
        if success:
            print(f"📊 输出摘要:\n{output[-500:]}...")  # 显示最后500个字符
    
    # 生成测试报告
    generate_test_report(results)
    
    return results

def generate_test_report(results):
    """生成测试报告"""
    print("\n" + "=" * 60)
    print("📊 测试结果汇总")
    print("=" * 60)
    
    total_tests = len(results)
    passed_tests = sum(1 for r in results if r["success"])
    failed_tests = total_tests - passed_tests
    
    print(f"总测试套件: {total_tests}")
    print(f"✅ 通过: {passed_tests}")
    print(f"❌ 失败: {failed_tests}")
    print(f"📈 成功率: {(passed_tests/total_tests)*100:.1f}%")
    
    print("\n📋 详细结果:")
    for i, result in enumerate(results, 1):
        status = "✅" if result["success"] else "❌"
        print(f"{i}. {status} {result['description']}")
    
    # 检查覆盖率报告
    coverage_html = Path("htmlcov/index.html")
    if coverage_html.exists():
        print(f"\n📄 覆盖率报告已生成: {coverage_html.absolute()}")
        print("💡 提示: 可以在浏览器中打开查看详细覆盖率")
        
        # 询问是否打开报告
        try:
            open_report = input("\n是否在浏览器中打开覆盖率报告? (y/n): ").lower().strip()
            if open_report in ['y', 'yes', '是']:
                webbrowser.open(f"file://{coverage_html.absolute()}")
        except KeyboardInterrupt:
            print("\n跳过打开报告")

def run_specific_test_category():
    """运行特定类别的测试"""
    print("\n🎯 选择测试类别:")
    print("1. 单元测试")
    print("2. 集成测试") 
    print("3. API测试")
    print("4. 性能测试")
    print("5. 所有测试")
    
    try:
        choice = input("\n请输入选择 (1-5): ").strip()
        
        test_commands = {
            "1": "python -m pytest tests/test_main.py tests/test_models.py -v",
            "2": "python -m pytest tests/test_integration.py -v",
            "3": "python -m pytest tests/test_ai_assistant.py -v",
            "4": "python -m pytest tests/test_integration.py::TestPerformance -v",
            "5": "python -m pytest tests/ -v --cov=app --cov-report=html"
        }
        
        if choice in test_commands:
            success, output = run_command(
                test_commands[choice],
                f"运行选择的测试类别 ({choice})"
            )
            if success:
                print("✅ 测试完成")
            else:
                print("❌ 测试失败")
        else:
            print("❌ 无效选择")
            
    except KeyboardInterrupt:
        print("\n测试被中断")

def main():
    """主函数"""
    try:
        print("🔬 Web IDE PDF项目 - 测试管理器")
        print("=" * 50)
        print("1. 运行完整测试套件")
        print("2. 运行特定测试类别")
        print("3. 只生成覆盖率报告")
        print("4. 退出")
        
        choice = input("\n请输入选择 (1-4): ").strip()
        
        if choice == "1":
            results = run_tests()
            return all(r["success"] for r in results)
        elif choice == "2":
            run_specific_test_category()
            return True
        elif choice == "3":
            success, output = run_command(
                "python -m pytest tests/ --cov=app --cov-report=html --cov-report=term-missing",
                "生成覆盖率报告"
            )
            return success
        elif choice == "4":
            print("👋 退出测试")
            return True
        else:
            print("❌ 无效选择")
            return False
            
    except KeyboardInterrupt:
        print("\n\n👋 测试被用户中断")
        return False
    except Exception as e:
        print(f"\n❌ 测试过程中出现错误: {e}")
        return False

if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1) 