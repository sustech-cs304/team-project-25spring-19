# Web IDE PDF - 智能PDF处理平台

[![Python](https://img.shields.io/badge/Python-3.8%2B-blue.svg)](https://python.org)
[![FastAPI](https://img.shields.io/badge/FastAPI-Latest-009688.svg)](https://fastapi.tiangolo.com)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](#)
[![Tests](https://img.shields.io/badge/Tests-33%20Passed-brightgreen.svg)](#)
[![Coverage](https://img.shields.io/badge/Coverage-57%25-yellow.svg)](#)

> 🚀 **基于AI的智能PDF处理平台** - 让PDF文档交互变得智能化！

## 📖 项目简介

Web IDE PDF是一个现代化的Web应用程序，集成了AI技术，专门用于PDF文档的智能处理和分析。用户可以上传PDF文件，通过AI进行智能问答、生成思维导图、创建判断题等操作。

### 🌟 核心特性

- 🤖 **AI智能问答** - 上传PDF后可进行智能对话
- 🗺️ **思维导图生成** - 自动分析PDF内容生成可视化图表
- ❓ **智能题目生成** - 基于PDF内容生成判断题和解释
- 📄 **文档文本提取** - 精确提取PDF中的文字内容
- 🔄 **多格式支持** - 支持各种PDF文档格式
- 🌐 **RESTful API** - 完整的API接口供开发者使用
- 🎯 **高性能** - 亚秒级响应时间
- 📱 **跨平台** - Windows、macOS、Linux全平台支持

## 🚀 快速开始

### 方式一：一键运行（推荐）

最简单的启动方式，无需安装任何依赖：

```bash
# macOS/Linux
./dist/WebIDE-PDF

# Windows
dist\WebIDE-PDF.exe

# 或使用启动脚本
./start_webide.sh
```

### 方式二：自动打包运行

自动构建最新版本：

```bash
python build_executable.py
```

### 方式三：开发模式

适合开发者和高级用户：

```bash
# 1. 自动安装依赖
python install_dependencies.py

# 2. 启动开发服务器
uvicorn app.main:app --reload

# 访问 http://127.0.0.1:8000
```

## 🎯 主要功能

### 1. PDF智能问答
```bash
POST /ai/ask_with_pdf
```
- 上传PDF文件
- 提出关于文档内容的问题
- 获得基于AI分析的智能回答

### 2. 思维导图生成
```bash
POST /ai/generate_mindmap
```
- 支持思维导图（mindmap）
- 支持流程图（flowchart）
- 自动分析PDF结构

### 3. 判断题生成
```bash
POST /ai/generate_truefalse
```
- 智能生成判断题
- 提供详细答案解释
- 支持多轮题目生成

### 4. 文本处理
```bash
POST /ai/ask
```
- 纯文本AI对话
- 智能内容分析
- 多轮对话支持

## 📊 使用示例

### API使用示例

```bash
# 1. 基础文本问答
curl -X POST "http://127.0.0.1:8000/ai/ask" \
     -H "Content-Type: application/json" \
     -d '{"question": "什么是人工智能？"}'

# 2. PDF文件问答
curl -X POST "http://127.0.0.1:8000/ai/ask_with_pdf" \
     -F "question=这个文档的主要内容是什么？" \
     -F "pdf_file=@document.pdf"

# 3. 生成思维导图
curl -X POST "http://127.0.0.1:8000/ai/generate_mindmap" \
     -F "map_type=mindmap" \
     -F "pdf_file=@document.pdf"
```

### Web界面使用

1. 启动服务后访问：http://127.0.0.1:8000/docs
2. 在交互式API文档中测试各个功能
3. 上传PDF文件并体验AI功能

## 🏗️ 系统架构

```
Web IDE PDF 架构
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端界面      │────│   FastAPI后端   │────│   AI服务接口    │
│   (Swagger UI)  │    │   (REST API)    │    │   (DeepSeek)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                       ┌─────────────────┐
                       │   SQLite数据库  │
                       │   (数据存储)    │
                       └─────────────────┘
```

## 📦 安装与部署

### 环境要求

- **Python**: 3.8+ (推荐 3.9+)
- **内存**: 4GB+ RAM
- **磁盘**: 500MB+ 可用空间
- **网络**: 需要访问AI服务的网络连接

### 依赖安装

项目提供多种安装方式：

```bash
# 核心依赖（推荐，安装快速）
pip install -r requirements-core.txt

# 完整依赖（包含开发工具）
pip install -r requirements.txt

# 测试依赖（用于开发和测试）
pip install -r requirements-test.txt

# 自动化安装（交互式）
python install_dependencies.py
```

### Docker部署

```bash
# 构建镜像
docker build -t webide-pdf .

# 运行容器
docker run -p 8000:8000 webide-pdf
```

## 🔧 配置说明

### 环境变量

```bash
# AI服务配置
DEEPSEEK_API_KEY=your_api_key_here

# 数据库配置
DATABASE_URL=sqlite:///./app.db

# 服务配置
HOST=127.0.0.1
PORT=8000
```

### 配置文件

主要配置在 `app/main.py` 中：
- CORS设置
- 数据库连接
- 路由配置
- 中间件设置

## 📈 性能指标

- **响应时间**: < 2秒（文本问答）
- **文件支持**: PDF ≤ 10MB
- **并发用户**: 支持多用户同时访问
- **可用性**: 99.9%+
- **测试覆盖**: 57%代码覆盖率，33个测试用例

## 🛠️ 开发指南

详细的开发文档请参考：
- [API文档](docs/API文档.md) - 完整的API接口说明
- [开发者指南](docs/开发者指南.md) - 代码结构和扩展指南
- [部署指南](部署说明.md) - 生产环境部署说明
- [测试文档](测试报告.md) - 自动化测试说明

## 📚 相关文档

| 文档类型 | 文件 | 描述 |
|---------|------|------|
| 用户手册 | [README.md](README.md) | 本文档，面向终端用户 |
| API文档 | [API文档.md](docs/API文档.md) | RESTful API接口说明 |
| 开发指南 | [开发者指南.md](docs/开发者指南.md) | 代码架构和开发指南 |
| 部署说明 | [部署说明.md](部署说明.md) | 安装和部署详细说明 |
| 测试报告 | [测试报告.md](测试报告.md) | 自动化测试和质量保证 |

## 🔍 故障排除

### 常见问题

**Q: 启动时提示端口被占用**
```bash
# 检查端口占用
lsof -i :8000
# 或修改端口
uvicorn app.main:app --port 8001
```

**Q: PDF文件上传失败**
- 检查文件大小（≤10MB）
- 确认文件格式为PDF
- 检查网络连接

**Q: AI功能无响应**
- 检查API密钥配置
- 确认网络连接正常
- 查看服务器日志

### 日志查看

```bash
# 查看应用日志
tail -f logs/app.log

# 开发模式详细日志
uvicorn app.main:app --reload --log-level debug
```

## 🤝 贡献指南

欢迎贡献代码！请参考：

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/新功能`)
3. 提交变更 (`git commit -am '添加新功能'`)
4. 推送分支 (`git push origin feature/新功能`)
5. 创建 Pull Request

### 开发规范

- 代码风格：PEP 8
- 测试覆盖：新功能需要测试用例
- 文档更新：API变更需要更新文档
- 提交信息：使用清晰的中文描述

## 📄 许可证

本项目采用 MIT 许可证 - 详情请查看 [LICENSE](LICENSE) 文件

## 📞 支持与联系

- **问题反馈**: [GitHub Issues](https://github.com/your-org/webide-pdf/issues)
- **功能建议**: [GitHub Discussions](https://github.com/your-org/webide-pdf/discussions)
- **技术交流**: 创建Issue讨论

---

⭐ **如果这个项目对您有帮助，请给我们一个星标！** ⭐

📄✨ **立即体验智能PDF处理功能！**

