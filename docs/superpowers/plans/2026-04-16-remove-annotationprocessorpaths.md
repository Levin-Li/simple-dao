# Remove annotationProcessorPaths Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在不配置 `maven-compiler-plugin.annotationProcessorPaths` 的前提下恢复并保持仓库 `compile` 与 `test-compile` 通过。

**Architecture:** 先删除父 POM 中的显式处理器路径，再依赖模块自身直接依赖让 Maven 默认发现 Lombok 和其他处理器。若验证失败，只补模块级直接依赖，不重新引入显式处理器路径。

**Tech Stack:** Maven、Spring Boot 4、Lombok、service-support 注解处理器、OpenSpec

---

### Task 1: 移除父 POM 显式处理器路径

**Files:**
- Modify: `/Users/lilw/IdeaProjects/simple-dao-2/pom.xml`
- Test: `mvn -q -DskipTests compile`

- [ ] **Step 1: 删除 `annotationProcessorPaths` 配置**

```xml
<plugin>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <parameters>true</parameters>
        <release>${maven.compiler.release}</release>
        <source>${maven.compiler.source}</source>
        <target>${maven.compiler.target}</target>
    </configuration>
</plugin>
```

- [ ] **Step 2: 运行主源码编译验证**

Run: `"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -q -DskipTests compile`
Expected: `BUILD SUCCESS`

### Task 2: 验证测试源码编译并按需补模块依赖

**Files:**
- Modify: `/Users/lilw/IdeaProjects/simple-dao-2/simple-dao-examples/pom.xml`（仅当验证失败）
- Modify: `/Users/lilw/IdeaProjects/simple-dao-2/simple-dao-jpa/pom.xml`（仅当验证失败）
- Test: `mvn -q -DskipTests test-compile`

- [ ] **Step 1: 运行测试源码编译**

Run: `"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -q -DskipTests test-compile`
Expected: `BUILD SUCCESS`

- [ ] **Step 2: 若失败，只补模块级直接依赖**

```xml
<dependency>
    <groupId>cn.hutool</groupId>
    <artifactId>hutool-all</artifactId>
    <scope>test</scope>
</dependency>
```

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-annotations</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 3: 再次运行测试源码编译**

Run: `"/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn" -q -DskipTests test-compile`
Expected: `BUILD SUCCESS`

### Task 3: 校验 OpenSpec 变更

**Files:**
- Modify: `/Users/lilw/IdeaProjects/simple-dao-2/openspec/changes/remove-annotationprocessorpaths/tasks.md`
- Test: `openspec validate remove-annotationprocessorpaths`

- [ ] **Step 1: 校验 change**

Run: `openspec validate remove-annotationprocessorpaths`
Expected: `Change 'remove-annotationprocessorpaths' is valid`

- [ ] **Step 2: 勾选完成项**

```md
- [x] 1.1 从父 POM 中移除 `annotationProcessorPaths`
- [x] 1.2 保留并核对模块级直接依赖，确保删除后仍具备所需编译依赖
- [x] 2.1 执行主源码编译和全仓 `test-compile`，验证默认处理器发现可工作
- [x] 2.2 执行 `openspec validate` 校验本次 change
```
