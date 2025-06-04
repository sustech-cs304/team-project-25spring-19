# Web IDE PDF - API接口文档

[![FastAPI](https://img.shields.io/badge/FastAPI-Latest-009688.svg)](https://fastapi.tiangolo.com)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0-85EA2D.svg)](https://swagger.io/specification/)
[![Python](https://img.shields.io/badge/Python-3.8%2B-blue.svg)](https://python.org)

> 🔧 **面向开发者的RESTful API接口文档**

## 📖 API概述

Web IDE PDF提供了完整的RESTful API接口，支持PDF文档的智能处理和AI交互功能。所有API均基于FastAPI框架构建，提供OpenAPI 3.0标准文档。

### 🌐 基础信息

- **基础URL**: `http://127.0.0.1:8000`
- **API版本**: v1.0
- **认证方式**: 基于Token（可选）
- **内容类型**: `application/json`, `multipart/form-data`
- **字符编码**: UTF-8

### 📋 API分类

1. **AI助手API** - AI问答和智能处理
2. **文件处理API** - PDF文件上传和分析
3. **思维导图API** - 图表生成和格式转换
4. **用户认证API** - 用户管理和权限控制
5. **数据管理API** - 数据库操作和历史记录

## 🤖 AI助手API

### 1. 文本问答

**端点**: `POST /ai/ask`

**描述**: 处理纯文本问答，支持多轮对话

**请求格式**:
```json
{
  "question": "string"
}
```

**响应格式**:
```json
{
  "answer": "string"
}
```

**示例**:
```bash
curl -X POST "http://127.0.0.1:8000/ai/ask" \
     -H "Content-Type: application/json" \
     -d '{
       "question": "什么是人工智能？"
     }'
```

**响应示例**:
```json
{
  "answer": "人工智能（AI）是计算机科学的一个分支，旨在创建能够执行通常需要人类智能的任务的系统..."
}
```

**错误码**:
- `400`: 问题内容不能为空
- `500`: AI服务调用失败

### 2. PDF智能问答

**端点**: `POST /ai/ask_with_pdf`

**描述**: 上传PDF文件并进行基于文档内容的智能问答

**请求格式**: `multipart/form-data`
- `question` (string): 问题内容
- `pdf_file` (file): PDF文件

**响应格式**:
```json
{
  "answer": "string",
  "pdf_summary": "string"
}
```

**示例**:
```bash
curl -X POST "http://127.0.0.1:8000/ai/ask_with_pdf" \
     -F "question=这个文档的主要内容是什么？" \
     -F "pdf_file=@document.pdf"
```

**文件限制**:
- 格式：PDF
- 大小：≤ 10MB
- 页数：≤ 100页

**错误码**:
- `400`: 文件格式不支持或文件过大
- `422`: 请求参数验证失败

### 3. 思维导图生成

**端点**: `POST /ai/generate_mindmap`

**描述**: 基于PDF内容生成思维导图或流程图

**请求格式**: `multipart/form-data`
- `pdf_file` (file): PDF文件
- `map_type` (string): 图表类型 (`mindmap` | `flowchart`)

**响应格式**:
```json
{
  "diagram": "string",
  "diagram_type": "string",
  "format": "mermaid"
}
```

**支持的图表类型**:
- `mindmap`: 思维导图
- `flowchart`: 流程图

**示例**:
```bash
curl -X POST "http://127.0.0.1:8000/ai/generate_mindmap" \
     -F "map_type=mindmap" \
     -F "pdf_file=@document.pdf"
```

### 4. 判断题生成

**端点**: `POST /ai/generate_truefalse`

**描述**: 基于PDF内容生成判断题

**请求格式**: `multipart/form-data`
- `pdf_file` (file): PDF文件

**响应格式**:
```json
{
  "question_id": "string",
  "question": "string"
}
```

**获取答案**: `GET /ai/truefalse/{question_id}/answer`
```json
{
  "question_id": "string",
  "answer": "boolean"
}
```

**获取解释**: `GET /ai/truefalse/{question_id}/explanation`
```json
{
  "question_id": "string",
  "explanation": "string"
}
```

## 🔄 格式转换API

### Mermaid图表转文本

**端点**: `POST /ai/convert_mermaid_to_text`

**描述**: 将Mermaid格式的图表转换为纯文本格式

**请求格式**:
```json
{
  "mermaid_content": "string"
}
```

**响应格式**:
```json
{
  "text_diagram": "string"
}
```

**示例**:
```bash
curl -X POST "http://127.0.0.1:8000/ai/convert_mermaid_to_text" \
     -H "Content-Type: application/json" \
     -d '{
       "mermaid_content": "```mermaid\nmindmap\nroot((主题))\n  A(分支A)\n  B(分支B)\n```"
     }'
```

## 👤 用户认证API

### 用户注册

**端点**: `POST /auth/register`

**请求格式**:
```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "role": "string"
}
```

### 用户登录

**端点**: `POST /auth/login`

**请求格式**:
```json
{
  "username": "string",
  "password": "string"
}
```

**响应格式**:
```json
{
  "access_token": "string",
  "token_type": "bearer",
  "user_info": {
    "id": "integer",
    "username": "string",
    "role": "string"
  }
}
```

### 获取用户信息

**端点**: `GET /auth/me`

**认证**: Bearer Token

**响应格式**:
```json
{
  "id": "integer",
  "username": "string",
  "email": "string",
  "role": "string"
}
```

## 📊 数据模型

### AIRequestRecord

AI请求记录模型

```python
class AIRequestRecord:
    id: int              # 记录ID
    question: str        # 用户问题
    answer: str          # AI回答
    created_at: datetime # 创建时间
```

### User

用户模型

```python
class User:
    id: int              # 用户ID
    username: str        # 用户名
    email: str           # 邮箱
    hashed_password: str # 加密密码
    role: str            # 用户角色
```

### TrueFalseQuestion

判断题模型

```python
class TrueFalseQuestion:
    question_id: str     # 题目ID
    question: str        # 题目内容
    answer: bool         # 正确答案
    explanation: str     # 答案解释
```

## 🔒 认证和授权

### Token认证

API支持JWT Token认证：

```bash
# 1. 登录获取Token
curl -X POST "http://127.0.0.1:8000/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"username": "user", "password": "pass"}'

# 2. 使用Token访问受保护的API
curl -X GET "http://127.0.0.1:8000/auth/me" \
     -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 权限角色

- `student`: 普通用户，可使用AI功能
- `teacher`: 教师用户，可管理内容
- `admin`: 管理员，拥有所有权限

## 📈 错误处理

### 标准错误格式

```json
{
  "detail": "错误描述信息",
  "error_code": "ERROR_CODE",
  "timestamp": "2024-01-01T12:00:00Z"
}
```

### 常见错误码

| HTTP状态码 | 错误码 | 描述 |
|-----------|--------|------|
| 400 | INVALID_REQUEST | 请求参数无效 |
| 401 | UNAUTHORIZED | 未授权访问 |
| 403 | FORBIDDEN | 权限不足 |
| 404 | NOT_FOUND | 资源不存在 |
| 413 | FILE_TOO_LARGE | 文件过大 |
| 422 | VALIDATION_ERROR | 数据验证失败 |
| 500 | INTERNAL_ERROR | 服务器内部错误 |
| 503 | SERVICE_UNAVAILABLE | AI服务不可用 |

## 🔧 SDK和集成

### Python SDK示例

```python
import requests

class WebIDEClient:
    def __init__(self, base_url="http://127.0.0.1:8000"):
        self.base_url = base_url
        self.session = requests.Session()
    
    def ask_question(self, question: str):
        """文本问答"""
        response = self.session.post(
            f"{self.base_url}/ai/ask",
            json={"question": question}
        )
        return response.json()
    
    def ask_with_pdf(self, question: str, pdf_path: str):
        """PDF问答"""
        with open(pdf_path, 'rb') as f:
            files = {'pdf_file': f}
            data = {'question': question}
            response = self.session.post(
                f"{self.base_url}/ai/ask_with_pdf",
                files=files,
                data=data
            )
        return response.json()

# 使用示例
client = WebIDEClient()
result = client.ask_question("什么是机器学习？")
print(result['answer'])
```

### JavaScript SDK示例

```javascript
class WebIDEClient {
    constructor(baseUrl = 'http://127.0.0.1:8000') {
        this.baseUrl = baseUrl;
    }

    async askQuestion(question) {
        const response = await fetch(`${this.baseUrl}/ai/ask`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ question })
        });
        return await response.json();
    }

    async askWithPDF(question, pdfFile) {
        const formData = new FormData();
        formData.append('question', question);
        formData.append('pdf_file', pdfFile);

        const response = await fetch(`${this.baseUrl}/ai/ask_with_pdf`, {
            method: 'POST',
            body: formData
        });
        return await response.json();
    }
}

// 使用示例
const client = new WebIDEClient();
client.askQuestion('什么是人工智能？')
    .then(result => console.log(result.answer));
```

## 📊 API性能

### 响应时间基准

| API端点 | 平均响应时间 | 95%分位数 |
|---------|-------------|-----------|
| `/ai/ask` | 800ms | 1.5s |
| `/ai/ask_with_pdf` | 2.3s | 4.0s |
| `/ai/generate_mindmap` | 3.1s | 5.5s |
| `/ai/generate_truefalse` | 2.8s | 4.8s |

### 限制和配额

- **并发请求**: 100/秒
- **文件上传**: 10MB/文件
- **请求频率**: 1000/小时/用户
- **Token有效期**: 24小时

## 🔗 相关链接

- **交互式API文档**: http://127.0.0.1:8000/docs
- **OpenAPI规范**: http://127.0.0.1:8000/openapi.json
- **备用文档**: http://127.0.0.1:8000/redoc
- **GitHub仓库**: [项目链接]
- **问题反馈**: [Issues链接]

## 📞 开发者支持

- **技术问题**: 创建GitHub Issue
- **功能建议**: 提交Feature Request
- **API错误**: 查看错误日志和状态码
- **性能问题**: 提供请求详情和响应时间

---

📘 **这是一个完整的RESTful API，支持现代Web开发的所有需求！** 