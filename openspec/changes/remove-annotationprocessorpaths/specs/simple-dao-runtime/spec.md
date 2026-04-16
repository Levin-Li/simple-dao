## MODIFIED Requirements

### Requirement: Spring Boot 4 兼容运行时基线
系统 MUST 以当前仓库定义的 Spring Boot 4 版本基线完成编译、自动配置注册和发布。

#### Scenario: 构建运行时模块
- **WHEN** 仓库以父 POM 中声明的 Spring Boot 4 版本构建 `simple-dao-annotations`、`simple-dao-core`、`simple-dao-jpa`、`simple-dao-jpa-starter` 和 `simple-dao-id-generator`
- **THEN** 这些模块必须声明完成编译所需的直接依赖
- **AND** 自动配置相关模块必须采用 Spring Boot 4 兼容的注册方式
- **AND** Maven 构建流程必须能够生成并发布对应产物

#### Scenario: 不显式配置 annotationProcessorPaths
- **WHEN** 仓库在当前 Maven 与 JDK 基线下执行 `compile` 或 `test-compile`
- **THEN** 构建链必须能够在不配置 `maven-compiler-plugin.annotationProcessorPaths` 的前提下发现并执行所需注解处理器
- **AND** 各模块必须通过直接依赖声明支撑其主源码和测试源码编译
