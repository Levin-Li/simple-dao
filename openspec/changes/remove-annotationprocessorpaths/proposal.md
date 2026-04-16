## Why

当前父 POM 通过 `maven-compiler-plugin.annotationProcessorPaths` 显式管理注解处理器类路径，虽然能让编译通过，但这会让编译链更重，也偏离“模块自己声明所需依赖、编译器按标准类路径执行处理器”的目标。

现在需要移除 `annotationProcessorPaths`，同时保持主源码和测试源码在当前 Spring Boot 4 / JDK 基线下仍然可以完成编译。

## What Changes

- 从父 POM 中移除 `annotationProcessorPaths` 配置。
- 用模块直接依赖和最小必要的测试依赖来支撑处理器执行。
- 在 JDK 23+ 基线下显式开启 `maven-compiler-plugin.proc=full`，恢复 classpath 上处理器的执行。
- 用 `compile` 与 `test-compile` 验证去除显式处理器路径后构建链仍可工作。

## Capabilities

### New Capabilities

无

### Modified Capabilities

- `simple-dao-runtime`: 构建链必须能够在不显式配置 `annotationProcessorPaths` 的前提下完成主源码和测试源码编译

## Impact

- 影响根 `pom.xml` 的编译器插件配置。
- 可能影响 `simple-dao-jpa`、`simple-dao-examples` 等依赖注解处理器的模块依赖声明。
- 不修改运行时业务行为，只调整构建方式。
