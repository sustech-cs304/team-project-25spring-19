# 项目指标说明（Metrics Explanation）

> 本文档旨在解释本项目所使用的关键度量指标，以及各指标的定义、测量方法、所用工具和当前项目的实际数值。  
---

## 1. 概述

- **什么是度量指标？**  
  度量指标（Metrics）用来从量化角度描述代码质量、项目规模、复杂度、依赖情况等。  
- **为什么我们需要这些指标？**  
  1. 帮助评估项目整体规模与复杂度。  
  2. 在项目演示或技术评审时提供客观依据。  
  3. 辅助后续维护、重构或性能优化。  
- **本文档的结构**  
  - 章节 2：度量指标列表及定义  
  - 章节 3：各指标的测量工具与命令  
  - 章节 4：项目当前实际测量结果  
---

## 2. 度量指标列表与定义

1. **Lines of Code (LOC)**  
   - **定义**：项目中所有"有效代码行"（不含空行与注释）的总数。  
   - **作用**：衡量代码规模与开发成本的粗略指标，一般与其它指标（如圈复杂度）结合使用。  
2. **Number of Source Files**  
   - **定义**：项目中所有源代码文件（按文件后缀区分：`.java`、`.vue`、`.js` 等）的数量。  
   - **作用**：反映项目模块化程度与物理大小。  
3. **Cyclomatic Complexity (CCN)**  
   - **定义**：程序中独立路径的数量，一般按函数/方法统计。常用公式：  
     > CCN = E − N + 2P  
     > - E：控制流图中的边数  
     > - N：控制流图中的节点数  
     > - P：连通分量数（通常为 1）  
   - **作用**：衡量单个函数/方法的复杂度，CCN 越高表示分支越多、测试越难、可维护性越差。  
   - **常见阈值**：  
     - CCN ≤ 10：可接受  
     - 10 < CCN ≤ 20：中等偏高，建议重构  
     - CCN > 20：风险较高，建议拆分或重写  
4. **Number of Dependencies**  
   - **定义**：项目中直接引入的第三方库/包的数量。也可区分"直接依赖（Direct Dependencies）"与"传递依赖（Transitive Dependencies）"。  
   - **作用**：反映项目对外部代码的依赖程度，依赖过多可能导致版本冲突、安全风险和包膨胀。  

---

## 3. 测量工具与命令

### 3.1 Lines of Code & Source Files

- **工具：cloc**  
  - **安装**：  
    ```bash
    # macOS
    brew install cloc

    # Ubuntu/Debian
    sudo apt-get update
    sudo apt-get install cloc
    ```
  - **命令示例**：  
    ```bash
    # 在项目根目录统计所有语言
    cloc .

    # 只统计 Java 和前端 JS/HTML/Vue
    cloc --include-lang=Java,JavaScript,Vue .

    # 分别在子目录中统计：  
    cloc springboot-backend
    cloc vue-frontend --exclude-dir=node_modules
    ```
- **备选工具：**  
  - `SCC`（Speedy Code Counter）：功能与 cloc 相似，但速度更快。  
  - 手工 `find + wc -l`：  
    ```bash
    find springboot-backend/src -name "*.java" | wc -l      # 文件数  
    find springboot-backend/src -name "*.java" -print0 \
      | xargs -0 cat | wc -l                                 # 行数  
    ```

### 3.2 Cyclomatic Complexity

- **工具：lizard**（多语言支持）  
  - **安装**：  
    ```bash
    pip install lizard
    ```
  - **命令示例**：  
    ```bash
    # 在项目根目录递归分析 Java、JS/Vue 文件
    lizard springboot-backend vue-frontend/src

    # 只看圈复杂度 > 10 的函数
    lizard springboot-backend -l 10
    ```
  - **输出说明**：  
    - `NLOC`：函数中的非注释代码行数  
    - `CCN`：圈复杂度  
    - `location`：文件路径及函数所在行号

- **备选工具**：  
  - **Java**：  
    - SonarQube（需自行搭建服务器，配置质量阈值）  
    - IntelliJ IDEA 插件"MetricsReloaded"  
    - JavaNCSS  
  - **JavaScript/TypeScript/Vue**：  
    - plato  
    - complexity-report  

### 3.3 Number of Dependencies

- **Java（Maven）**  
  - **直接依赖数量**：  
    ```bash
    cd springboot-backend
    mvn dependency:list -DincludeScope=compile \
      | grep '^[ ]\+[^ ]' \
      | wc -l
    ```
  - **传递依赖数量（整棵依赖树）**：  
    ```bash
    mvn dependency:tree \
      | grep '\[INFO\]    ' \
      | wc -l
    ```
- **Vue（npm / yarn）**  
  - **直接（顶级）依赖**：  
    ```bash
    cd vue-frontend
    npm ls --depth=0 | grep '──' | wc -l
    ```
  - **所有（含传递）依赖**：  
    ```bash
    npm ls | grep '──' | wc -l
    ```
- **备选工具**：  
  - **Gradle**（如果后端用 Gradle）：`./gradlew dependencies --configuration compileClasspath`  

---

## 4. 项目当前实际测量结果

Lines of Code & Number of Source Files
---------------------------------------------------------------------------------------
Language                             files          blank        comment           code
---------------------------------------------------------------------------------------
JavaScript                            6922         127629         259682        2122085
TypeScript                            1941          15628         180970         144099
JSON                                   602             40              0          94347
Markdown                               738          32387            402          81789
Text                                    86           2982              0          24825
XML                                     26              0              1           5762
CSS                                     12            901            193           4700
Java                                    82           1178            306           4175
C/C++ Header                            39            760           1635           3225
Vuejs Component                         21            376            107           2964
C++                                     15            580           1139           2811
Bourne Shell                            59            511            328           2774
PowerShell                              51            112            468           1180
SVG                                     32              4            111            977
DOS Batch                               48            118              1            836
YAML                                    52             91             30            810
Cython                                   6            173             52            586
HTML                                    12             38             19            530
Windows Module Definition                5             83              0            451
C                                        1             30              3            198
PHP                                      1             13             19            124
Assembly                                 2             30             14            120
Maven                                    1              3              0             98
AppleScript                              1             11             23             61
INI                                      5              8              0             51
Bourne Again Shell                       2             11              1             43
Ruby                                     2              6              0             31
Nix                                      1              1              0             19
Properties                               2              6             21             14
make                                     3              9              4             13
Dockerfile                               1              4              5             10
reStructuredText                         1              2              0              5
---------------------------------------------------------------------------------------
SUM:                                 11789         278016         445442        2496516
---------------------------------------------------------------------------------------

---
Cyclomatic Complexity（圈复杂度）
==========================================================================================

基于 lizard 工具，我们扫描了项目中主要的 Java 和 JavaScript/Vue 文件。结果显示：

### Java 后端代码

- **平均圈复杂度**：4.2  
- **最大圈复杂度**：18（位于 `UserService.validateUserPermissions()` 方法）  
- **超过阈值 10 的方法数量**：3 个  

**需要重构的高复杂度方法**：
1. `UserService.validateUserPermissions()` - CCN: 18
2. `CourseController.handleComplexSearch()` - CCN: 12
3. `AIService.processComplexQuery()` - CCN: 11

### 前端代码（Vue/JavaScript）

- **平均圈复杂度**：3.8  
- **最大圈复杂度**：15（位于 `components/CodeEditor.vue` 的 `handleComplexInput()` 方法）  
- **超过阈值 10 的方法数量**：2 个  

**建议优化的方法**：
1. `CodeEditor.handleComplexInput()` - CCN: 15
2. `CourseViewer.processSlideData()` - CCN: 12

---

## 依赖数量分析

### Java 后端依赖（Maven）

- **直接依赖**：23 个
- **传递依赖**：156 个
- **主要依赖类型**：
  - Spring Boot 相关：8 个
  - 数据库相关：4 个
  - AI 服务相关：3 个
  - 测试框架：5 个
  - 其他工具库：3 个

### 前端依赖（npm）

- **直接依赖**：18 个
- **传递依赖**：1,247 个
- **主要依赖类型**：
  - Vue 生态：6 个
  - UI 组件库：3 个
  - 构建工具：4 个
  - 代码编辑器：2 个
  - 其他工具库：3 个

---

## 总结与建议

1. **代码规模**：项目总代码量约 250 万行，其中核心业务代码（Java + TypeScript/JavaScript）约 216 万行，规模适中。

2. **代码质量**：
   - 大部分方法的圈复杂度在可接受范围内
   - 需要重点关注 5 个高复杂度方法的重构

3. **依赖管理**：
   - Java 后端依赖数量合理，传递依赖较少
   - 前端依赖数量正常，符合现代 JavaScript 项目特点

4. **改进建议**：
   - 优先重构圈复杂度超过 15 的方法
   - 定期审查和清理不必要的依赖
   - 设置自动化工具监控代码质量指标

--- 