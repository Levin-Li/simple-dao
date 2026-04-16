## Why

最近对父 POM 和示例模块 POM 做了依赖收敛后，仓库主代码仍可编译，但 `test-compile` 已经失败。失败主要表现为测试源码中的 Lombok 生成方法缺失，以及 `simple-dao-examples` 测试直接使用的 `hutool`、`jackson-annotations` 类无法解析。

需要恢复当前 Maven / JDK 基线下测试源码的可编译性，避免发布或本地校验流程只能依赖跳过测试编译。

## What Changes

- 恢复父 POM 中 Maven 编译器对注解处理器的显式配置，使 Lombok 与相关处理器在 `test-compile` 阶段继续生效。
- 为 `simple-dao-examples` 补回测试源码直接依赖但当前已缺失的库。
- 使用 `test-compile` 验证当前仓库在不执行测试用例的前提下可以完成测试源码编译。

## Capabilities

### New Capabilities

无

### Modified Capabilities

- `simple-dao-runtime`: 当前仓库在 Spring Boot 4 / JDK 基线下不仅主源码可编译，测试源码也必须完成 `test-compile`

## Impact

- 影响根 `pom.xml` 的 `maven-compiler-plugin` 配置。
- 影响 `simple-dao-examples/pom.xml` 的测试依赖集合。
- 不修改运行时业务逻辑，只修复构建与测试编译链。
