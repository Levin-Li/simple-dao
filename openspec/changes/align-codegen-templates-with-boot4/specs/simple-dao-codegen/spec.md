## MODIFIED Requirements

### Requirement: 当前版本模板必须对齐 Spring Boot 4 基线
系统 MUST 确保当前模板和生成结果与仓库使用的 Spring Boot 4 基线保持一致。

#### Scenario: 生成新的 Starter 与工程模板
- **WHEN** 插件在当前仓库版本下生成 Starter、根 POM 或相关工程模板
- **THEN** 生成结果必须使用 Spring Boot 4 兼容的版本基线与自动配置注册方式
- **AND** 新生成的 Starter 注册文件必须采用 `AutoConfiguration.imports`

#### Scenario: 不输出已确认不兼容的旧 starter
- **WHEN** 插件生成根 POM、模块 POM 或默认文档集成模板
- **THEN** 生成结果不得包含明显面向 Spring Boot 2 的 starter 坐标
- **AND** 生成结果不得默认输出基于 SpringFox 的 Swagger 配置模板
- **AND** 涉及认证与 OpenAPI 增强的默认依赖必须使用 Jakarta / Spring Boot 4 兼容坐标
