## Context

本次问题不是运行时行为错误，而是 Maven 构建链在依赖收敛后失去了测试源码编译所依赖的两类能力：

1. `pom.xml` 去掉 `annotationProcessorPaths` 后，Lombok 和相关注解处理器在当前 Maven/JDK 组合下不再稳定参与测试源码编译，导致测试 DTO、请求对象和 `@Slf4j` 日志字段大量缺失。
2. `simple-dao-examples` 的测试源码仍直接引用 `cn.hutool` 与 `com.fasterxml.jackson.annotation`，但模块依赖里已经没有对应坐标。

## Goals / Non-Goals

**Goals:**
- 恢复整个仓库 `test-compile` 的通过状态。
- 仅修复构建链缺失项，不改测试业务逻辑。
- 保持依赖恢复范围尽量小，只补回当前测试源码明确需要的内容。

**Non-Goals:**
- 不执行或修复所有测试用例本身。
- 不扩展新的测试框架或重构测试代码。
- 不借本次问题顺手调整其他模块与本问题无关的依赖。

## Decisions

### 1. 恢复显式 `annotationProcessorPaths`

选择在父 POM 中恢复 `maven-compiler-plugin.annotationProcessorPaths`，并保留 `lombok`、`service-support`、`swagger-annotations`，同时补上此前已验证需要的 `jakarta.persistence-api` 与 `spring-core`。

这样做的原因是当前仓库已经验证过：移除此配置后，Lombok 在现有 Maven/JDK 基线下不会稳定处理主源码和测试源码。

### 2. 只给 `simple-dao-examples` 补回测试直依赖

对 `hutool-all` 和 `jackson-annotations` 采用 `test` 作用域补回，因为当前缺类都来自 `src/test/java`。不把这两个依赖重新提升为整个仓库的公共编译依赖。

### 3. 用 `test-compile` 作为验收

本次验收不要求执行测试，只要求 `mvn -DskipTests test-compile` 通过。这样能够直接验证测试源码编译链是否恢复，又不会把运行时数据库或环境问题混进来。

## Risks / Trade-offs

- [显式处理器路径会继续增加一点维护成本] → 这是当前 Maven/JDK 组合下的必要代价，先保证可编译性。
- [恢复测试依赖可能被误解为“回滚依赖收敛”] → 只在测试作用域补回，范围最小。
- [如果还有别的测试模块存在同类问题] → 先用全仓 `test-compile` 验证，若有剩余缺失再按同样原则补齐。
