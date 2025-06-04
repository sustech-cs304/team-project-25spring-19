

## 用户文档（End‐User Documentation）格式框架

### 1. 文档存放及命名建议

* **文件位置**
  通常放在项目根目录下的 `docs/` 目录中，或者直接放在项目根目录下（若项目比较小，也可以放在根目录）。
  示例结构：

  ```
  your‐project/
  ├─ docs/
  │   └─ USER_GUIDE.md        # 用户文档
  ├─ src/
  ├─ ...
  └─ README.md                # 项目概览，可链接到 USER_GUIDE.md
  ```
* **文件命名**

  * 如果只有一个用户文档，可以直接命名为 `USER_GUIDE.md`、`USER_DOCUMENTATION.md` 或 `README_USER.md`。
  * 也可以沿用更通用的 `README.md`，但建议在 `README.md` 里只放 **概览+快速上手**，并把详细使用步骤链接到单独的 `docs/USER_GUIDE.md`。

### 2. 内容模块（章节）示例

下面是一份较为完整的用户文档大纲，供参考。具体可根据项目实际功能、目标用户水平进行删减或调整。

1. **标题与简介**

   * 项目名称
   * 一句话简介：告诉读者这是一个“课程感知智能 IDE”，它能做什么、适合谁用。
   * 主要功能概览：列举 2–3 条核心卖点，比如“可在幻灯片中执行代码”、“内置笔记与书签”、“AI 学习助理”等。

2. **安装与部署**

   * **环境要求**

     * 操作系统（Windows / macOS / Linux）
     * 前提依赖（Java 版本、Python 版本、Node.js 版本、Docker 等）
   * **下载/克隆项目**

     ```bash
     git clone
     cd 
     ```
   * **快速一键启动示例**

     * 说明如何使用可执行文件、Docker Compose 或者手动启动三部分服务（Spring Boot、Python、Vue）。
     * 举例：

       ```bash
       # 方式一：Docker Compose
       cd your‐repo
       docker-compose up --build -d

       # 方式二：本地运行
       # 1) 进入 springboot-backend 目录，生成可执行 Jar 并运行
       cd springboot-backend
       mvn clean package
       java -jar target/ide-backend.jar

       # 2) 进入 python-backend 目录，安装依赖并运行
       cd ../python-backend
       pip install -r requirements.txt
       python app.py

       # 3) 进入 vue-frontend 目录，生成静态并通过 http-server 预览
       cd ../vue-frontend
       npm install
       npm run build
       npx http-server dist -p 80
       ```
   * **配置说明**

     * 如需修改端口、数据库连接、AI API Key 等，在 `.env`、`application.yml` 或者 `config.py` 中进行相应配置，并示例如何修改。
     * 各配置项的含义（如 `SPRING_DATASOURCE_URL`、`AI_API_KEY`、`FRONTEND_BASE_URL`）。

3. **快速开始（Getting Started）**

   * **打开 IDE/访问界面**

     * 浏览器地址、桌面可执行程序入口。
   * **首次登录/注册**

     * 如果需要账号体系，演示如何注册、首次登录。
   * **创建/导入课程**

     * 如何在 IDE 中导入已有的课程资料（上传 PPT、PDF、作业）。
     * 界面截图示例（可选）。
   * **在幻灯片中执行代码示例**

     * 打开一页课程幻灯片，修改示例代码片段并运行。
     * 说明运行按钮或快捷键、结果输出区域。

4. **功能详解**
   下面每个子模块都可以分节说明，配图或配 GIF 会更直观（可选放在图片目录 `docs/images/`）。

   1. **资源管理**

      * 目录树、课程文件夹结构
      * 上传/删除/下载课件、笔记
      * 给关键内容加书签、跳转到对应代码位置
   2. **笔记与标注**

      * 如何在幻灯片上侧边或底部添加文字笔记
      * 如何高亮代码块或文字，便于复习
   3. **代码-幻灯片联动**

      * 在幻灯片中直接执行代码：示例命令、快捷键、输出结果
      * 如何在左侧 PPT 视图右侧面板编写并调试代码
   4. **练习与测验**

      * 访问“练习”标签，自动加载每节课练习题目
      * 直接在线编写并提交答案（如果有自动评测），示例截图 + 流程
   5. **协作与讨论**

      * 开启实时协作：如何邀请同学加入同一个房间
      * 在幻灯片/代码片段里发起讨论线程，查看历史评论
      * 内置编辑功能：文字消息、表情、代码片段分享
   6. **进度追踪**

      * 仪表盘示例：已完成的幻灯片、待完成的练习
      * 如何设置学习计划、提醒待完成内容
   7. **AI 学习助手**

      * 如何调用“摘要”功能，自动生成当前幻灯片要点
      * 自动生成练习题目：示例“生成 3 道选择题”流程
      * 自动绘制简单思维导图或流程图：截图示例
      * 自测模式：自动批阅 + 给出解析

5. **常见问题与故障排查（FAQ/Troubleshooting）**

   * **启动失败**

     * 常见的“端口被占用”报错，如何修改端口
     * “数据库连接失败” 的处理（检查 `application.yml` 或环境变量）
   * **无法执行代码**

     * Python 环境报错、依赖缺失时的处理方法
   * **前端页面白屏或 404**

     * 确保 `npm run build` 已生成 `dist/`，并且 Nginx / http-server 配置正确
   * **AI 助手无法调用**

     * 检查 AI API Key 是否正确、网络是否可访问外网

6. **附录**

   * **快捷键表**（
   * **参考文档链接**（
   * **版本更新日志（Changelog）**
   * **联系方式**（

---

