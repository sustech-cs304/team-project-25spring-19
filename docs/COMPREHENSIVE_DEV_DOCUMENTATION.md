# 智能课程感知IDE - 开发人员文档

## 1. 项目概览

### 1.1 项目背景
智能课程感知IDE是一个集成课程学习与代码开发的现代化Web平台，旨在为学生和教师提供一体化的学习和教学环境。该系统支持在幻灯片中直接执行代码、实时协作编程、智能笔记管理以及AI辅助学习等先进功能。

### 1.2 技术架构图
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Vue 3 前端    │ ←→ │ Spring Boot 后端│ ←→ │ PostgreSQL数据库│
│                 │    │                 │    │                 │
│ - Vue Router    │    │ - Spring Data   │    │ - 用户数据      │
│ - Pinia状态管理 │    │ - WebSocket     │    │ - 课程数据      │
│ - CodeMirror    │    │ - AI智能服务    │    │ - 进度数据      │
│ - WebSocket     │    │ - 文件管理      │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │                       │                       │
        └───────────────────────┼───────────────────────┘
                                │
                    ┌─────────────────┐
                    │   MinIO 文件存储│
                    │                 │
                    │ - PPT/PDF文件   │
                    │ - 代码资源      │
                    │ - 媒体资源      │
                    └─────────────────┘
```

### 1.3 核心技术栈

#### 后端技术栈
- **框架**: Spring Boot 3.4.4
- **JDK版本**: Java 17
- **数据库**: PostgreSQL (生产环境), H2 (测试环境)
- **ORM框架**: Spring Data JPA
- **WebSocket**: Spring WebSocket
- **对象存储**: MinIO
- **构建工具**: Maven 3.x
- **AI服务**: 集成在Spring Boot中，支持代码分析、内容摘要、练习生成等

#### 前端技术栈
- **框架**: Vue 3.5.13 + TypeScript
- **状态管理**: Pinia 3.0.1
- **路由**: Vue Router 4.5.0
- **代码编辑器**: CodeMirror 6.0.1
- **HTTP客户端**: Axios 1.9.0
- **WebSocket**: STOMP.js 7.1.1
- **文档处理**: @vue-office/pptx, pdfjs-dist
- **构建工具**: Vite 6.2.1

## 2. 项目结构详解

### 2.1 后端项目结构
```
src/SpringBoot/
├── src/main/java/com/example/cs304project/
│   ├── Cs304ProjectApplication.java           # 应用启动类
│   ├── controller/                            # REST API控制器
│   │   ├── BookmarkController.java            # 书签管理
│   │   ├── CodeRoomController.java            # 代码协作房间
│   │   ├── CodeRoomWebSocketController.java   # WebSocket控制器
│   │   ├── CodeSnippetController.java         # 代码片段管理
│   │   ├── CourseController.java              # 课程管理
│   │   ├── CourseProgressController.java      # 学习进度
│   │   ├── DiagnosticController.java          # 代码诊断(AI功能)
│   │   ├── DiscussionController.java          # 讨论功能
│   │   ├── ExerciseController.java            # 练习题管理
│   │   ├── LectureController.java             # 讲座管理
│   │   ├── LectureSlideController.java        # 幻灯片管理
│   │   ├── NoteController.java                # 笔记管理
│   │   ├── ResourceController.java            # 资源文件管理
│   │   ├── SelectCourseController.java        # 选课功能
│   │   ├── SubmissionController.java          # 作业提交
│   │   └── UserController.java                # 用户管理
│   ├── service/                               # 业务逻辑层
│   │   ├── ai/                                # AI智能服务模块
│   │   │   ├── CodeAnalysisService.java       # 代码智能分析
│   │   │   ├── ContentSummaryService.java     # 内容摘要生成
│   │   │   ├── ExerciseGenerationService.java # 练习题自动生成
│   │   │   └── AIAssistantService.java        # AI学习助手
│   │   ├── CourseService.java                 # 课程业务逻辑
│   │   ├── UserService.java                   # 用户业务逻辑
│   │   └── ...
│   ├── repository/                            # 数据访问层
│   ├── entity/                                # JPA实体类
│   ├── dto/                                   # 数据传输对象
│   ├── config/                                # 配置类
│   │   ├── WebSocketConfig.java               # WebSocket配置
│   │   ├── CorsConfig.java                    # 跨域配置
│   │   └── MinIOConfig.java                   # 文件存储配置
│   └── exception/                             # 异常处理
├── src/main/resources/
│   ├── application.yml                        # 应用配置
│   └── schema.sql                             # 数据库初始化脚本
└── pom.xml                                    # Maven依赖配置
```

### 2.2 前端项目结构
```
src/Intelligent-IDE/
├── src/
│   ├── main.ts                                # 应用入口
│   ├── App.vue                                # 根组件
│   ├── config.ts                              # 配置文件
│   ├── views/                                 # 页面组件
│   │   ├── Dashboard.vue                      # 仪表盘
│   │   ├── CourseList.vue                     # 课程列表
│   │   ├── CourseDetail.vue                   # 课程详情
│   │   ├── CodeEditor.vue                     # 代码编辑器
│   │   ├── Collaboration.vue                  # 协作编程
│   │   └── AIAssistant.vue                    # AI助手界面
│   ├── components/                            # 可复用组件
│   │   ├── SlideViewer.vue                    # 幻灯片查看器
│   │   ├── CodeRunner.vue                     # 代码执行器
│   │   ├── NoteEditor.vue                     # 笔记编辑器
│   │   ├── ChatWidget.vue                     # 聊天组件
│   │   └── ProgressTracker.vue                # 进度追踪
│   ├── api/                                   # API调用封装
│   │   ├── courseApi.ts                       # 课程相关API
│   │   ├── userApi.ts                         # 用户相关API
│   │   ├── aiApi.ts                           # AI功能API
│   │   └── websocketApi.ts                    # WebSocket封装
│   ├── stores/                                # Pinia状态管理
│   │   ├── user.ts                            # 用户状态
│   │   ├── course.ts                          # 课程状态
│   │   └── collaboration.ts                   # 协作状态
│   ├── router/                                # 路由配置
│   │   └── index.ts
│   └── assets/                                # 静态资源
├── package.json                               # 项目依赖配置
└── vite.config.ts                             # Vite构建配置
```

## 3. 核心功能模块详解

### 3.1 AI智能服务模块

AI功能完全集成在Spring Boot后端中，提供以下核心能力：

#### 3.1.1 代码智能分析服务 (CodeAnalysisService)
```java
@Service
public class CodeAnalysisService {
    
    /**
     * 分析Java代码质量和潜在问题
     * @param code Java源代码
     * @return 分析结果，包括代码质量评分、改进建议等
     */
    public CodeAnalysisResult analyzeJavaCode(String code) {
        // 实现代码静态分析逻辑
        // 包括语法检查、性能分析、安全扫描等
    }
    
    /**
     * 提供代码优化建议
     * @param code 原始代码
     * @return 优化建议和改进代码示例
     */
    public CodeOptimizationSuggestion suggestOptimization(String code) {
        // 基于最佳实践提供代码优化建议
    }
}
```

#### 3.1.2 内容摘要生成服务 (ContentSummaryService)
```java
@Service
public class ContentSummaryService {
    
    /**
     * 为课程内容生成智能摘要
     * @param courseContent 课程内容文本
     * @return 结构化摘要，包括要点、关键概念等
     */
    public ContentSummary generateSummary(String courseContent) {
        // 使用自然语言处理技术生成摘要
    }
    
    /**
     * 生成思维导图数据
     * @param content 学习内容
     * @return 思维导图的JSON结构数据
     */
    public MindMapData generateMindMap(String content) {
        // 提取关键概念并构建思维导图结构
    }
}
```

#### 3.1.3 练习题自动生成服务 (ExerciseGenerationService)
```java
@Service
public class ExerciseGenerationService {
    
    /**
     * 根据课程内容自动生成练习题
     * @param topicContent 主题内容
     * @param difficulty 难度级别 (EASY, MEDIUM, HARD)
     * @param questionCount 题目数量
     * @return 生成的练习题列表
     */
    public List<Exercise> generateExercises(String topicContent, 
                                          DifficultyLevel difficulty, 
                                          int questionCount) {
        // 基于内容智能生成选择题、编程题等
    }
}
```

### 3.2 实时协作模块

#### 3.2.1 WebSocket协作控制器
```java
@Controller
public class CodeRoomWebSocketController {
    
    @MessageMapping("/coderoom/{roomId}/join")
    @SendTo("/topic/coderoom/{roomId}")
    public CollaborationMessage joinRoom(
            @DestinationVariable String roomId,
            CollaborationMessage message,
            SimpMessageHeaderAccessor headerAccessor) {
        // 处理用户加入协作房间逻辑
    }
    
    @MessageMapping("/coderoom/{roomId}/edit")
    @SendTo("/topic/coderoom/{roomId}")
    public CodeEditMessage handleCodeEdit(
            @DestinationVariable String roomId,
            CodeEditMessage message) {
        // 处理实时代码编辑同步
    }
}
```

### 3.3 文件管理模块

系统使用MinIO进行分布式文件存储，支持PPT、PDF、图片等多种格式：

```java
@Service
public class FileStorageService {
    
    @Autowired
    private MinioClient minioClient;
    
    /**
     * 上传课程资源文件
     * @param file 文件对象
     * @param courseId 课程ID
     * @return 文件访问URL
     */
    public String uploadCourseResource(MultipartFile file, Long courseId) {
        // 实现文件上传逻辑
    }
    
    /**
     * 获取文件预览URL
     * @param fileName 文件名
     * @return 预览URL
     */
    public String getPreviewUrl(String fileName) {
        // 生成文件预览链接
    }
}
```

## 4. API接口文档

### 4.1 课程管理接口

| 接口路径 | 请求方法 | 功能描述 | 请求参数 | 响应示例 |
|---------|---------|---------|---------|---------|
| `/api/courses` | GET | 获取课程列表 | `page`, `size` | `{"courses": [...], "total": 100}` |
| `/api/courses/{id}` | GET | 获取课程详情 | `id` (路径参数) | `{"id": 1, "title": "Java编程", "slides": [...]}` |
| `/api/courses` | POST | 创建新课程 | `{"title": "...", "description": "..."}` | `{"success": true, "courseId": 123}` |
| `/api/courses/{id}/progress` | GET | 获取学习进度 | `id`, `userId` | `{"completedSlides": 5, "totalSlides": 20}` |

### 4.2 AI功能接口

| 接口路径 | 请求方法 | 功能描述 | 请求参数 | 响应示例 |
|---------|---------|---------|---------|---------|
| `/api/ai/analyze-code` | POST | Java代码分析 | `{"code": "...", "language": "java"}` | `{"score": 85, "suggestions": [...]}` |
| `/api/ai/generate-summary` | POST | 内容摘要生成 | `{"content": "..."}` | `{"summary": "...", "keyPoints": [...]}` |
| `/api/ai/generate-exercises` | POST | 练习题生成 | `{"topic": "...", "difficulty": "medium", "count": 5}` | `{"exercises": [...]}` |
| `/api/ai/generate-mindmap` | POST | 思维导图生成 | `{"content": "..."}` | `{"nodes": [...], "edges": [...]}` |

### 4.3 协作功能接口

| 接口路径 | 请求方法 | 功能描述 | 请求参数 | 响应示例 |
|---------|---------|---------|---------|---------|
| `/api/coderooms` | POST | 创建协作房间 | `{"courseId": 1, "name": "..."}` | `{"roomId": "room123", "joinCode": "ABC123"}` |
| `/api/coderooms/{roomId}/join` | POST | 加入协作房间 | `roomId`, `joinCode` | `{"success": true, "participants": [...]}` |
| `/api/coderooms/{roomId}/code` | GET | 获取房间代码 | `roomId` | `{"code": "...", "language": "java"}` |

### 4.4 WebSocket消息协议

#### 协作编程消息格式
```json
{
  "type": "CODE_EDIT",
  "roomId": "room123",
  "userId": "user456",
  "operation": {
    "type": "insert",
    "position": 100,
    "content": "System.out.println(\"Hello\");"
  },
  "timestamp": 1645123456789
}
```

#### 用户状态消息格式
```json
{
  "type": "USER_STATUS",
  "roomId": "room123",
  "userId": "user456",
  "status": "online",
  "cursorPosition": 150
}
```

## 5. 数据库设计

### 5.1 核心实体关系图
```
Users ──┐
        │
        ├── UserCourses ── Courses ── Lectures ── LectureSlides
        │                     │
        ├── Notes            │
        │                     ├── Resources
        ├── Bookmarks        │
        │                     └── Exercises ── Submissions
        ├── CourseProgress   
        │
        └── CodeRooms ── CodeRoomMembers
                │
                └── CodeSnapshots
```

### 5.2 主要数据表结构

#### 用户表 (users)
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    avatar_url VARCHAR(255),
    role VARCHAR(20) DEFAULT 'STUDENT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 课程表 (courses)
```sql
CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    instructor_id BIGINT REFERENCES users(id),
    cover_image_url VARCHAR(255),
    difficulty_level VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 讲座幻灯片表 (lecture_slides)
```sql
CREATE TABLE lecture_slides (
    id BIGSERIAL PRIMARY KEY,
    lecture_id BIGINT REFERENCES lectures(id),
    slide_number INTEGER NOT NULL,
    title VARCHAR(200),
    content TEXT,
    code_snippet TEXT,
    slide_type VARCHAR(50),
    file_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 6. 开发环境搭建

### 6.1 环境要求
- **JDK**: Java 17 或更高版本
- **Node.js**: 18.0 或更高版本  
- **Maven**: 3.6 或更高版本
- **PostgreSQL**: 12.0 或更高版本
- **Docker**: 20.0 或更高版本 (可选)

### 6.2 本地开发启动步骤

#### 步骤1: 克隆项目
```bash
git clone https://github.com/sustech-cs304/team-project-25spring-19.git
cd team-project-25spring-19
```

#### 步骤2: 启动后端服务
```bash
cd src/SpringBoot

# 配置数据库连接 (编辑 src/main/resources/application.yml)
# 安装依赖并启动
mvn clean install
mvn spring-boot:run

# 后端将在 http://localhost:8080 启动
```

#### 步骤3: 启动前端服务
```bash
cd src/Intelligent-IDE

# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 前端将在 http://localhost:5173 启动
```

#### 步骤4: 配置MinIO文件存储 (可选)
```bash
# 使用Docker启动MinIO
docker run -d --name minio \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=admin \
  -e MINIO_ROOT_PASSWORD=password123 \
  minio/minio server /data --console-address ":9001"
```

### 6.3 Docker容器化部署

#### docker-compose.yml
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: courseide
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: password123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  minio:
    image: minio/minio
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: admin
      MINIO_ROOT_PASSWORD: password123
    command: server /data --console-address ":9001"
    volumes:
      - minio_data:/data

  backend:
    build: ./src/SpringBoot
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/courseide
      SPRING_DATASOURCE_USERNAME: admin
      SPRING_DATASOURCE_PASSWORD: password123
      MINIO_ENDPOINT: http://minio:9000
    depends_on:
      - postgres
      - minio

  frontend:
    build: ./src/Intelligent-IDE
    ports:
      - "80:80"
    depends_on:
      - backend

volumes:
  postgres_data:
  minio_data:
```

#### 部署命令
```bash
# 构建并启动所有服务
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f backend
```

## 7. 测试策略

### 7.1 后端测试

#### 单元测试示例
```java
@SpringBootTest
class CourseServiceTest {
    
    @Autowired
    private CourseService courseService;
    
    @MockBean
    private CourseRepository courseRepository;
    
    @Test
    void testCreateCourse() {
        // 测试课程创建逻辑
        Course course = new Course();
        course.setTitle("Test Course");
        
        when(courseRepository.save(any(Course.class))).thenReturn(course);
        
        Course result = courseService.createCourse(course);
        assertThat(result.getTitle()).isEqualTo("Test Course");
    }
}
```

#### 集成测试示例
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
class CourseControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testGetCourseList() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/courses", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

### 7.2 前端测试

#### 组件单元测试
```typescript
import { mount } from '@vue/test-utils'
import CourseCard from '@/components/CourseCard.vue'

describe('CourseCard', () => {
  it('renders course title correctly', () => {
    const wrapper = mount(CourseCard, {
      props: {
        course: {
          id: 1,
          title: 'Java Programming',
          description: 'Learn Java basics'
        }
      }
    })
    
    expect(wrapper.text()).toContain('Java Programming')
  })
})
```

#### E2E测试 (Playwright)
```typescript
import { test, expect } from '@playwright/test'

test('user can create a new course', async ({ page }) => {
  await page.goto('http://localhost:5173')
  
  // 登录
  await page.click('[data-testid="login-button"]')
  await page.fill('[data-testid="username"]', 'testuser')
  await page.fill('[data-testid="password"]', 'password123')
  await page.click('[data-testid="submit-login"]')
  
  // 创建课程
  await page.click('[data-testid="create-course"]')
  await page.fill('[data-testid="course-title"]', 'New Test Course')
  await page.click('[data-testid="save-course"]')
  
  await expect(page.locator('[data-testid="course-list"]')).toContainText('New Test Course')
})
```

## 8. 代码规范与最佳实践

### 8.1 Java后端代码规范

#### 命名规范
- **类名**: 使用PascalCase，如 `CourseService`, `UserController`
- **方法名**: 使用camelCase，如 `createCourse()`, `getUserById()`
- **常量**: 使用UPPER_SNAKE_CASE，如 `MAX_FILE_SIZE`
- **包名**: 使用小写字母，如 `com.example.cs304project.service`

#### 注释规范
```java
/**
 * 课程管理服务类
 * 提供课程的创建、查询、更新和删除功能
 * 
 * @author Development Team
 * @version 1.0
 * @since 2024-01-01
 */
@Service
public class CourseService {
    
    /**
     * 根据课程ID获取课程详细信息
     * 
     * @param courseId 课程唯一标识符
     * @return Course 课程对象，如果不存在则返回null
     * @throws IllegalArgumentException 当courseId为null或负数时抛出
     */
    public Course getCourseById(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new IllegalArgumentException("Course ID must be positive");
        }
        return courseRepository.findById(courseId).orElse(null);
    }
}
```

#### 异常处理最佳实践
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .code("ENTITY_NOT_FOUND")
            .message(ex.getMessage())
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .code("VALIDATION_ERROR")
            .message("Input validation failed")
            .details(ex.getErrors())
            .timestamp(Instant.now())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

### 8.2 前端代码规范

#### Vue组件规范
```vue
<template>
  <div class="course-card">
    <h3 class="course-card__title">{{ course.title }}</h3>
    <p class="course-card__description">{{ course.description }}</p>
    <button 
      class="course-card__button"
      @click="handleEnrollClick"
    >
      {{ isEnrolled ? '已选课' : '选课' }}
    </button>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Course {
  id: number
  title: string
  description: string
}

const props = defineProps<{
  course: Course
  isEnrolled?: boolean
}>()

const emit = defineEmits<{
  enroll: [courseId: number]
}>()

const handleEnrollClick = () => {
  if (!props.isEnrolled) {
    emit('enroll', props.course.id)
  }
}
</script>

<style scoped>
.course-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 16px;
  margin: 8px;
}

.course-card__title {
  font-size: 1.2em;
  font-weight: bold;
  margin-bottom: 8px;
}

.course-card__description {
  color: #666;
  margin-bottom: 12px;
}

.course-card__button {
  background-color: #007bff;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}

.course-card__button:disabled {
  background-color: #6c757d;
  cursor: not-allowed;
}
</style>
```

#### TypeScript类型定义
```typescript
// types/course.ts
export interface Course {
  id: number
  title: string
  description: string
  instructorId: number
  coverImageUrl?: string
  difficultyLevel: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
  status: 'ACTIVE' | 'INACTIVE' | 'DRAFT'
  createdAt: string
  updatedAt: string
}

export interface CreateCourseRequest {
  title: string
  description: string
  difficultyLevel: Course['difficultyLevel']
}

export interface CourseListResponse {
  courses: Course[]
  total: number
  page: number
  size: number
}
```

## 9. 部署与运维

### 9.1 生产环境部署

#### 服务器环境要求
- **操作系统**: Ubuntu 20.04 LTS 或 CentOS 8
- **内存**: 最少4GB，推荐8GB
- **存储**: 最少50GB SSD
- **网络**: 稳定的互联网连接

#### Nginx配置
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # 前端静态文件
    location / {
        root /var/www/html/intelligent-ide;
        try_files $uri $uri/ /index.html;
    }
    
    # API代理到后端
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # WebSocket代理
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
    }
}
```

### 9.2 监控与日志

#### 应用监控配置
```yaml
# application-prod.yml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

logging:
  level:
    com.example.cs304project: INFO
    org.springframework.web: DEBUG
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: ./logs/application.log
```

#### 健康检查端点
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "Available")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "Unavailable")
                .withException(e)
                .build();
        }
        return Health.down()
            .withDetail("database", "Connection validation failed")
            .build();
    }
}
```

## 10. 性能优化建议

### 10.1 后端性能优化

#### 数据库查询优化
```java
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    // 使用@Query优化复杂查询
    @Query("SELECT c FROM Course c JOIN FETCH c.lectures l WHERE c.status = 'ACTIVE'")
    List<Course> findActiveCoursesWithLectures();
    
    // 分页查询优化
    @Query(value = "SELECT * FROM courses WHERE status = :status ORDER BY created_at DESC",
           countQuery = "SELECT count(*) FROM courses WHERE status = :status",
           nativeQuery = true)
    Page<Course> findByStatusOrderByCreatedAtDesc(@Param("status") String status, Pageable pageable);
}
```

#### 缓存策略
```java
@Service
@CacheConfig(cacheNames = "courses")
public class CourseService {
    
    @Cacheable(key = "#courseId")
    public Course getCourseById(Long courseId) {
        return courseRepository.findById(courseId).orElse(null);
    }
    
    @CacheEvict(key = "#course.id")
    public Course updateCourse(Course course) {
        return courseRepository.save(course);
    }
    
    @Cacheable(key = "#status + '_' + #pageable.pageNumber")
    public Page<Course> getCoursesByStatus(String status, Pageable pageable) {
        return courseRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
    }
}
```

### 10.2 前端性能优化

#### 代码分割与懒加载
```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue') // 懒加载
    },
    {
      path: '/course/:id',
      name: 'CourseDetail',
      component: () => import('@/views/CourseDetail.vue')
    },
    {
      path: '/editor',
      name: 'CodeEditor',
      component: () => import('@/views/CodeEditor.vue')
    }
  ]
})
```

#### API请求优化
```typescript
// api/courseApi.ts
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use((config) => {
  // 添加认证token
  const token = localStorage.getItem('authToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response?.status === 401) {
      // 处理认证失败
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

// 缓存常用数据
const cache = new Map()

export const courseApi = {
  async getCourseList(params: CourseListParams) {
    const cacheKey = JSON.stringify(params)
    if (cache.has(cacheKey)) {
      return cache.get(cacheKey)
    }
    
    const result = await api.get('/courses', { params })
    cache.set(cacheKey, result)
    
    // 5分钟后清除缓存
    setTimeout(() => cache.delete(cacheKey), 5 * 60 * 1000)
    
    return result
  }
}
```

## 11. 常见问题解答

### 11.1 开发环境问题

**Q: 后端启动时提示数据库连接失败**
A: 检查以下配置：
1. 确保PostgreSQL服务正在运行
2. 验证`application.yml`中的数据库连接配置
3. 确认数据库用户权限和密码正确

**Q: 前端无法访问后端API**
A: 检查以下设置：
1. 确认后端服务已启动在正确端口(8080)
2. 检查前端代理配置或直接API调用的URL
3. 验证CORS配置是否正确

**Q: AI功能无法正常工作**
A: 确认以下配置：
1. 检查AI服务相关的配置项
2. 验证网络连接是否正常
3. 查看后端日志获取具体错误信息

### 11.2 部署相关问题

**Q: Docker容器启动失败**
A: 执行以下检查：
1. 检查Docker镜像构建是否成功
2. 验证docker-compose.yml配置
3. 查看容器日志: `docker-compose logs [service-name]`

**Q: 文件上传功能不工作**
A: 检查MinIO配置：
1. 确认MinIO服务正常运行
2. 验证访问密钥配置
3. 检查存储桶权限设置

## 12. 贡献指南

### 12.1 开发流程

1. **Fork项目** - 从主仓库fork到个人账户
2. **创建分支** - 基于develop分支创建功能分支
3. **编写代码** - 遵循代码规范，添加必要的测试
4. **提交代码** - 使用规范的commit message格式
5. **创建PR** - 向develop分支提交Pull Request
6. **代码审查** - 通过团队代码审查后合并

### 12.2 Commit消息规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

**类型说明:**
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 其他修改

**示例:**
```
feat(course): add course creation functionality

- Implement course creation API endpoint
- Add course creation form in frontend
- Include validation for course title and description

Closes #123
```

### 12.3 代码审查清单

- [ ] 代码符合项目编码规范
- [ ] 包含必要的单元测试
- [ ] API文档已更新
- [ ] 新功能有相应的用户文档
- [ ] 没有明显的性能问题
- [ ] 安全性检查通过
- [ ] 向后兼容性考虑

## 13. 版本更新日志

### v1.0.0 (2024-12-20)
**初始版本发布**
- ✨ 完整的课程管理系统
- ✨ 实时协作编程功能
- ✨ AI智能学习助手
- ✨ 幻灯片内代码执行
- ✨ 笔记和进度跟踪
- ✨ 文件存储和管理

### 未来规划
- 📱 移动端应用支持
- 🔍 高级搜索功能
- 📊 详细的学习分析报告
- 🎮 游戏化学习体验
- 🌐 多语言国际化支持

---

**文档维护**: 开发团队
**最后更新**: 2024年12月20日
**文档版本**: v1.0.0 