## Context

此前仓库通过 `annotationProcessorPaths` 明确列出 `lombok`、`service-support`、`swagger-annotations` 等处理器和其运行时依赖，以保证在当前 Maven/JDK 组合下编译通过。但用户现在明确要求不再设置该配置。

要满足这个要求，关键不是简单删除配置，而是确认删除后 Lombok 与 `service-support` 中的处理器仍能通过模块依赖被编译器执行，并且测试源码缺失的普通依赖不会再被误判为注解处理器问题。结合实际构建结果，当前真正的根因是 JDK 23+ 默认不再自动执行 classpath 上发现的注解处理器，因此需要显式开启 `proc=full`，而不是恢复 `annotationProcessorPaths`。

## Goals / Non-Goals

**Goals:**
- 删除父 POM 中的 `annotationProcessorPaths`。
- 保持 `compile` 和 `test-compile` 通过。
- 只在必要模块补充直接依赖，不引入新的构建插件。

**Non-Goals:**
- 不重新设计整个编译链。
- 不引入新的 APT 插件或用 `annotationProcessors` 代替 `annotationProcessorPaths`。
- 不修改测试代码行为。

## Decisions

### 1. 先保留模块依赖，再删除 `annotationProcessorPaths`

当前仓库里 `simple-dao-jpa` 已直接声明 `service-support`、`swagger-annotations`、`jakarta.persistence-api`，`simple-dao-examples` 也直接声明 `lombok`。因此优先保留这些模块直接依赖，确保处理器及其运行时类型都位于正常编译类路径。

### 2. 在父 POM 中显式开启 `proc=full`

在 JDK 23+ 下，即使处理器 JAR 已在 classpath 上，`javac` 也不会默认执行它们。为保持“不配置 `annotationProcessorPaths`”的前提，同时继续沿用模块依赖承载处理器，父 POM 中的 `maven-compiler-plugin` 需要显式设置 `<proc>full</proc>`。

### 3. 普通缺类问题与处理器问题分开处理

`hutool`、`jackson-annotations` 这类测试源码直接依赖继续保留在 `simple-dao-examples` 中，因为它们不是处理器链问题，而是普通测试编译依赖。

### 4. 用实际编译结果决定是否需要进一步补依赖

如果删除 `annotationProcessorPaths` 并启用 `proc=full` 后 `compile` 或 `test-compile` 仍失败，再根据具体缺类定位到模块级直接依赖，而不是回退到显式处理器路径。

## Risks / Trade-offs

- [JDK 版本差异导致处理器执行行为变化] → 在父 POM 中固定 `proc=full`，把行为显式化，并用真实构建验证。
- [模块依赖可能略微增加] → 这是把构建需求显式放回模块自身的代价，比父 POM 统一隐藏更清晰。
- [删除后若失败会暴露更多历史遗留问题] → 逐个按模块定位，避免一次性扩大范围。
