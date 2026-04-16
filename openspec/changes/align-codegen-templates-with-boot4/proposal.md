## Why

`simple-dao-code-gen` 当前已经把 Spring Boot 4 作为模板基线的一部分，但生成的根 POM、模块 POM 和部分模板文件里仍然保留了明显属于旧生态的依赖与配置，导致新生成工程并不能稳定落在 Boot 4 兼容线上。

现在需要把这些高置信不兼容项收敛掉，避免代码生成器继续产出带有 Boot 2、SpringFox 或旧 Jakarta 之前坐标的工程骨架。

## What Changes

- 调整代码生成器模板中的依赖坐标，移除明确不匹配 Spring Boot 4 的 starter 与版本线。
- 将 Sa-Token、Knife4j 等模板依赖切换到与 Jakarta / Boot 4 基线一致的坐标。
- 移除生成器模板中遗留的 SpringFox Swagger 配置模板与相关兼容路径，避免继续生成旧文档栈代码。
- 保持 `simple-dao-code-gen` 模块自身作为 Maven 插件的运行依赖最小变更，只处理会影响模板输出正确性的部分。

## Capabilities

### New Capabilities

无

### Modified Capabilities

- `simple-dao-codegen`: 当前版本模板必须输出与 Spring Boot 4 基线一致的依赖坐标、文档集成方式和自动配置约定

## Impact

- 影响 `simple-dao-code-gen` 模块中的 `pom.xml.ftl`、`root-pom.xml.ftl` 及 Swagger 相关模板文件。
- 影响后续通过 `simple-dao-code-gen` 生成的新工程依赖集合和默认文档集成方式。
- 不修改 `simple-dao-jpa`、`simple-dao-core` 等运行时模块的对外行为。
