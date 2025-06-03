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
   - **定义**：项目中所有“有效代码行”（不含空行与注释）的总数。  
   - **作用**：衡量代码规模与开发成本的粗略指标，一般与其它指标（如圈复杂度）结合使用。  
2. **Number of Source Files**  
   - **定义**：项目中所有源代码文件（按文件后缀区分：`.java`、`.py`、`.vue`、`.js` 等）的数量。  
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
   - **定义**：项目中直接引入的第三方库/包的数量。也可区分“直接依赖（Direct Dependencies）”与“传递依赖（Transitive Dependencies）”。  
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

    # 只统计 Java、Python 和前端 JS/HTML/Vue
    cloc --include-lang=Java,Python,JavaScript,Vue .

    # 分别在子目录中统计：  
    cloc springboot-backend
    cloc python-backend
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
    # 在项目根目录递归分析 Java、Python、JS/Vue 文件
    lizard springboot-backend python-backend vue-frontend/src

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
    - IntelliJ IDEA 插件“MetricsReloaded”  
    - JavaNCSS  
  - **Python**：  
    - radon  
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
- **Python（pip / requirements.txt）**  
  - 如果有 `requirements.txt`：  
    ```bash
    cd python-backend
    grep -v '^\s*#' requirements.txt | grep -v '^\s*$' | wc -l
    ```
  - 如果用 Poetry：  
    ```bash
    cd python-backend
    poetry show --no-dev | wc -l
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
  - **pipdeptree**（可视化 Python 依赖树）  

---

## 4. 项目当前实际测量结果

Lines of Code & Number of Source Files
---------------------------------------------------------------------------------------
Language                             files          blank        comment           code
---------------------------------------------------------------------------------------
JavaScript                            6922         127629         259682        2122085
Python                                1982         120272         162529         434193
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
SUM:                                 12754         303997         608063        2933906
---------------------------------------------------------------------------------------

---
Cyclomatic Complexity（圈复杂度）
==========================================================================================
Total nloc   Avg.NLOC  AvgCCN  Avg.token   Fun Cnt  Warning cnt   Fun Rt   nloc Rt
------------------------------------------------------------------------------------------
   3717980      11.3     3.6       76.5   194264         3928      0.02    0.36
---
Number of Dependencies（依赖项数量）

java:
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- dependency:3.8.1:list (default-cli) @ cs304Project ---
[INFO]
[INFO] The following files have been resolved:
[INFO]    org.springframework.boot:spring-boot-starter-data-jpa:jar:3.4.4:compile -- module spring.boot.starter.data.jpa [auto]
[INFO]    org.springframework.boot:spring-boot-starter:jar:3.4.4:compile -- module spring.boot.starter [auto]
[INFO]    org.springframework.boot:spring-boot:jar:3.4.4:compile -- module spring.boot [auto]
[INFO]    org.springframework.boot:spring-boot-autoconfigure:jar:3.4.4:compile -- module spring.boot.autoconfigure [auto]
[INFO]    org.springframework.boot:spring-boot-starter-logging:jar:3.4.4:compile -- module spring.boot.starter.logging [auto]
[INFO]    ch.qos.logback:logback-classic:jar:1.5.18:compile -- module ch.qos.logback.classic
[INFO]    ch.qos.logback:logback-core:jar:1.5.18:compile -- module ch.qos.logback.core
[INFO]    org.apache.logging.log4j:log4j-to-slf4j:jar:2.24.3:compile -- module org.apache.logging.log4j.to.slf4j
[INFO]    org.apache.logging.log4j:log4j-api:jar:2.24.3:compile -- module org.apache.logging.log4j
[INFO]    org.slf4j:jul-to-slf4j:jar:2.0.17:compile -- module jul.to.slf4j
[INFO]    jakarta.annotation:jakarta.annotation-api:jar:2.1.1:compile -- module jakarta.annotation
[INFO]    org.yaml:snakeyaml:jar:2.3:compile -- module org.yaml.snakeyaml
[INFO]    org.springframework.boot:spring-boot-starter-jdbc:jar:3.4.4:compile -- module spring.boot.starter.jdbc [auto]
[INFO]    com.zaxxer:HikariCP:jar:5.1.0:compile -- module com.zaxxer.hikari
[INFO]    org.springframework:spring-jdbc:jar:6.2.5:compile -- module spring.jdbc [auto]
[INFO]    org.hibernate.orm:hibernate-core:jar:6.6.11.Final:compile -- module org.hibernate.orm.core [auto]
[INFO]    jakarta.persistence:jakarta.persistence-api:jar:3.1.0:compile -- module jakarta.persistence
[INFO]    jakarta.transaction:jakarta.transaction-api:jar:2.0.1:compile -- module jakarta.transaction
[INFO]    org.jboss.logging:jboss-logging:jar:3.6.1.Final:runtime -- module org.jboss.logging
[INFO]    org.hibernate.common:hibernate-commons-annotations:jar:7.0.3.Final:runtime -- module org.hibernate.commons.annotations
[INFO]    io.smallrye:jandex:jar:3.2.0:runtime -- module org.jboss.jandex [auto]
[INFO]    com.fasterxml:classmate:jar:1.7.0:runtime -- module com.fasterxml.classmate
[INFO]    net.bytebuddy:byte-buddy:jar:1.15.11:runtime -- module net.bytebuddy
[INFO]    org.glassfish.jaxb:jaxb-runtime:jar:4.0.5:runtime -- module org.glassfish.jaxb.runtime
[INFO]    org.glassfish.jaxb:jaxb-core:jar:4.0.5:runtime -- module org.glassfish.jaxb.core
[INFO]    org.eclipse.angus:angus-activation:jar:2.0.2:runtime -- module org.eclipse.angus.activation
[INFO]    org.glassfish.jaxb:txw2:jar:4.0.5:runtime -- module com.sun.xml.txw2
[INFO]    com.sun.istack:istack-commons-runtime:jar:4.1.2:runtime -- module com.sun.istack.runtime
[INFO]    jakarta.inject:jakarta.inject-api:jar:2.0.1:runtime -- module jakarta.inject
[INFO]    org.antlr:antlr4-runtime:jar:4.13.0:compile -- module org.antlr.antlr4.runtime [auto]
[INFO]    org.springframework.data:spring-data-jpa:jar:3.4.4:compile -- module spring.data.jpa [auto]
[INFO]    org.springframework.data:spring-data-commons:jar:3.4.4:compile -- module spring.data.commons [auto]
[INFO]    org.springframework:spring-orm:jar:6.2.5:compile -- module spring.orm [auto]
[INFO]    org.springframework:spring-context:jar:6.2.5:compile -- module spring.context [auto]
[INFO]    org.springframework:spring-tx:jar:6.2.5:compile -- module spring.tx [auto]
[INFO]    org.springframework:spring-beans:jar:6.2.5:compile -- module spring.beans [auto]
[INFO]    org.slf4j:slf4j-api:jar:2.0.17:compile -- module org.slf4j
[INFO]    org.springframework:spring-aspects:jar:6.2.5:compile -- module spring.aspects [auto]
[INFO]    org.aspectj:aspectjweaver:jar:1.9.23:compile -- module org.aspectj.weaver [auto]
[INFO]    org.springframework.boot:spring-boot-starter-security:jar:3.4.4:compile -- module spring.boot.starter.security [auto]
[INFO]    org.springframework:spring-aop:jar:6.2.5:compile -- module spring.aop [auto]
[INFO]    org.springframework.security:spring-security-config:jar:6.4.4:compile -- module spring.security.config [auto]
[INFO]    org.springframework.security:spring-security-web:jar:6.4.4:compile -- module spring.security.web [auto]
[INFO]    org.springframework:spring-expression:jar:6.2.5:compile -- module spring.expression [auto]
[INFO]    org.springframework.boot:spring-boot-starter-web:jar:3.4.4:compile -- module spring.boot.starter.web [auto]
[INFO]    org.springframework.boot:spring-boot-starter-json:jar:3.4.4:compile -- module spring.boot.starter.json [auto]
[INFO]    com.fasterxml.jackson.core:jackson-databind:jar:2.18.3:compile -- module com.fasterxml.jackson.databind
[INFO]    com.fasterxml.jackson.core:jackson-annotations:jar:2.18.3:compile -- module com.fasterxml.jackson.annotation
[INFO]    com.fasterxml.jackson.core:jackson-core:jar:2.18.3:compile -- module com.fasterxml.jackson.core
[INFO]    com.fasterxml.jackson.datatype:jackson-datatype-jdk8:jar:2.18.3:compile -- module com.fasterxml.jackson.datatype.jdk8
[INFO]    com.fasterxml.jackson.datatype:jackson-datatype-jsr310:jar:2.18.3:compile -- module com.fasterxml.jackson.datatype.jsr310
[INFO]    com.fasterxml.jackson.module:jackson-module-parameter-names:jar:2.18.3:compile -- module com.fasterxml.jackson.module.paramnames
[INFO]    org.springframework.boot:spring-boot-starter-tomcat:jar:3.4.4:compile -- module spring.boot.starter.tomcat [auto]
[INFO]    org.apache.tomcat.embed:tomcat-embed-core:jar:10.1.39:compile -- module org.apache.tomcat.embed.core
[INFO]    org.apache.tomcat.embed:tomcat-embed-el:jar:10.1.39:compile -- module org.apache.tomcat.embed.el
[INFO]    org.apache.tomcat.embed:tomcat-embed-websocket:jar:10.1.39:compile -- module org.apache.tomcat.embed.websocket
[INFO]    org.springframework:spring-web:jar:6.2.5:compile -- module spring.web [auto]
[INFO]    io.micrometer:micrometer-observation:jar:1.14.5:compile -- module micrometer.observation [auto]
[INFO]    io.micrometer:micrometer-commons:jar:1.14.5:compile -- module micrometer.commons [auto]
[INFO]    org.springframework:spring-webmvc:jar:6.2.5:compile -- module spring.webmvc [auto]
[INFO]    org.springframework.boot:spring-boot-starter-websocket:jar:3.4.4:compile -- module spring.boot.starter.websocket [auto]
[INFO]    org.springframework:spring-messaging:jar:6.2.5:compile -- module spring.messaging [auto]
[INFO]    org.springframework:spring-websocket:jar:6.2.5:compile -- module spring.websocket [auto]
[INFO]    org.postgresql:postgresql:jar:42.7.5:runtime -- module org.postgresql.jdbc [auto]
[INFO]    org.checkerframework:checker-qual:jar:3.48.3:runtime -- module org.checkerframework.checker.qual
[INFO]    org.projectlombok:lombok:jar:1.18.36:compile (optional) -- module lombok
[INFO]    org.springframework.boot:spring-boot-starter-test:jar:3.4.4:test -- module spring.boot.starter.test [auto]
[INFO]    org.springframework.boot:spring-boot-test:jar:3.4.4:test -- module spring.boot.test [auto]
[INFO]    org.springframework.boot:spring-boot-test-autoconfigure:jar:3.4.4:test -- module spring.boot.test.autoconfigure [auto]
[INFO]    com.jayway.jsonpath:json-path:jar:2.9.0:test -- module json.path [auto]
[INFO]    jakarta.xml.bind:jakarta.xml.bind-api:jar:4.0.2:runtime -- module jakarta.xml.bind
[INFO]    jakarta.activation:jakarta.activation-api:jar:2.1.3:runtime -- module jakarta.activation
[INFO]    net.minidev:json-smart:jar:2.5.2:test -- module json.smart (auto)
[INFO]    net.minidev:accessors-smart:jar:2.5.2:test -- module accessors.smart (auto)
[INFO]    org.ow2.asm:asm:jar:9.7.1:test -- module org.objectweb.asm
[INFO]    org.assertj:assertj-core:jar:3.26.3:test -- module org.assertj.core
[INFO]    org.awaitility:awaitility:jar:4.2.2:test -- module awaitility (auto)
[INFO]    org.hamcrest:hamcrest:jar:2.2:test -- module org.hamcrest [auto]
[INFO]    org.junit.jupiter:junit-jupiter:jar:5.11.4:test -- module org.junit.jupiter
[INFO]    org.junit.jupiter:junit-jupiter-api:jar:5.11.4:test -- module org.junit.jupiter.api
[INFO]    org.opentest4j:opentest4j:jar:1.3.0:test -- module org.opentest4j
[INFO]    org.junit.platform:junit-platform-commons:jar:1.11.4:test -- module org.junit.platform.commons
[INFO]    org.apiguardian:apiguardian-api:jar:1.1.2:test -- module org.apiguardian.api
[INFO]    org.junit.jupiter:junit-jupiter-params:jar:5.11.4:test -- module org.junit.jupiter.params
[INFO]    org.junit.jupiter:junit-jupiter-engine:jar:5.11.4:test -- module org.junit.jupiter.engine
[INFO]    org.junit.platform:junit-platform-engine:jar:1.11.4:test -- module org.junit.platform.engine
[INFO]    org.mockito:mockito-core:jar:5.14.2:test -- module org.mockito [auto]
[INFO]    net.bytebuddy:byte-buddy-agent:jar:1.15.11:test -- module net.bytebuddy.agent
[INFO]    org.objenesis:objenesis:jar:3.3:test -- module org.objenesis [auto]
[INFO]    org.mockito:mockito-junit-jupiter:jar:5.14.2:test -- module org.mockito.junit.jupiter [auto]
[INFO]    org.skyscreamer:jsonassert:jar:1.5.3:test -- module jsonassert (auto)
[INFO]    com.vaadin.external.google:android-json:jar:0.0.20131108.vaadin1:test -- module android.json (auto)
[INFO]    org.springframework:spring-core:jar:6.2.5:compile -- module spring.core [auto]
[INFO]    org.springframework:spring-jcl:jar:6.2.5:compile -- module spring.jcl [auto]
[INFO]    org.springframework:spring-test:jar:6.2.5:test -- module spring.test [auto]
[INFO]    org.xmlunit:xmlunit-core:jar:2.10.0:test -- module org.xmlunit [auto]
[INFO]    org.springframework.security:spring-security-test:jar:6.4.4:test -- module spring.security.test [auto]
[INFO]    org.springframework.security:spring-security-core:jar:6.4.4:compile -- module spring.security.core [auto]
[INFO]    org.springframework.security:spring-security-crypto:jar:6.4.4:compile -- module spring.security.crypto [auto]
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  3.675 s
[INFO] Finished at: 2025-06-03T17:41:14+08:00
[INFO] ------------------------------------------------------------------------



---
