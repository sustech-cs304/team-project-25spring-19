import subprocess
import tempfile
import os

# 预先定义不同语言对应的Docker镜像和执行命令
# 如果采用单一镜像 code_executor，这里统一用该镜像，通过bash调用对应命令
IMAGE_NAME = "code_executor"  # 假设已经构建此镜像

def run_code_in_docker(code: str, language: str, timeout_sec: int = 5):
    """
    在Docker容器中运行给定代码，并返回 (stdout, stderr)。
    超时时间默认为5秒。
    """
    # 根据语言决定文件扩展名和执行命令
    if language.lower() == "python":
        file_ext = ".py"
        run_cmd = "python3 /code/code.py"
    elif language.lower() == "javascript":
        file_ext = ".js"
        run_cmd = "node /code/code.js"
    elif language.lower() == "java":
        file_ext = ".java"
        # 对于Java，假设代码中定义了一个 Main 类
        run_cmd = "sh -c 'javac /code/Main.java && java -cp /code Main'"
    elif language.lower() in ["c++", "cpp", "c"]:
        file_ext = ".cpp"
        run_cmd = "sh -c 'g++ /code/code.cpp -o /code/code.out && /code/code.out'"
    else:
        # 不支持的语言
        raise Exception(f"不支持的语言类型: {language}")
    # 创建临时文件存储代码
    with tempfile.NamedTemporaryFile(delete=False, suffix=file_ext) as tmp:
        tmp.write(code.encode('utf-8'))
        tmp.flush()
        host_code_path = tmp.name  # 主机上的临时代码文件路径
    # 准备Docker运行命令
    # --rm: 运行后自动删除容器; -v: 挂载代码文件进容器; --network none: 禁用网络
    docker_cmd = [
        "docker", "run", "--rm", "--network", "none",
        "-v", f"{host_code_path}:/code/{'Main.java' if language.lower()=='java' else 'code'+file_ext}",
        IMAGE_NAME
    ]
    # 将执行命令添加到参数
    docker_cmd += run_cmd.split()
    try:
        result = subprocess.run(docker_cmd, capture_output=True, text=True, timeout=timeout_sec)
    except subprocess.TimeoutExpired:
        # 超时：杀掉容器进程（这里直接抛出，由上层捕获处理）
        raise TimeoutError("代码运行超时")
    finally:
        # 清理临时文件
        try:
            os.remove(host_code_path)
        except:
            pass
    # result.stdout/result.stderr 捕获容器中程序的输出
    stdout = result.stdout
    stderr = result.stderr
    return stdout, stderr