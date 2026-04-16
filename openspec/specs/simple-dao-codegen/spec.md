# Simple DAO Codegen Specification

## Purpose
为使用 Simple DAO 的项目提供 Maven 插件和模板能力，用于生成示例工程、数据库实体代码以及围绕实体的服务、控制器、Starter 与工程模块骨架。

## Requirements

### Requirement: 示例工程模板生成
系统 MUST 提供 Maven 插件目标，用于在当前项目或指定子模块下生成可运行的示例工程模板和实体开发约定文件。

#### Scenario: 生成演示工程模板
- WHEN 开发者执行 `gen-demo-project-template`
- THEN 插件必须创建实体模块目录、基础资源目录和模板化初始文件
- AND 生成内容必须包含实体开发规范与示例实体模板
- AND 生成过程必须能够继承当前 Maven 工程的坐标与关键属性

### Requirement: 基于编译后实体生成业务代码
系统 MUST 能够扫描编译后的实体类，并根据实体信息生成服务接口、服务实现、控制器、Starter、启动模块及相关 POM 文件。

#### Scenario: 从实体模块生成服务代码
- WHEN 开发者在包含实体类的模块上执行 `gen-code`
- THEN 插件必须识别实体模块及其输出目录中的实体类
- AND 必须生成与实体对应的服务、控制器和模块化工程骨架
- AND 生成结果必须遵循插件参数配置的模块拆分规则

### Requirement: 数据库元数据驱动实体生成
系统 MUST 支持从数据库元数据读取表与列定义，并将其转换为实体代码生成所需的中间模型。

#### Scenario: 面向多种数据库生成实体信息
- WHEN 开发者通过插件连接 MySQL、PostgreSQL、Oracle、SQL Server、达梦等受支持数据库
- THEN 系统必须能够读取表结构、列信息和类型定义
- AND 必须通过数据库方言适配器将数据库类型转换为目标语言字段类型

### Requirement: 代码生成行为可配置
系统 MUST 允许开发者通过插件参数控制模块命名、是否启用 Dubbo、是否拆分目录、是否格式化输出、保留注解规则以及目标文件变更保护策略。

#### Scenario: 配置化调整生成结果
- WHEN 开发者设置 `enableDubbo`、`forceSplitDir`、`isOutputFormatCode`、`keepAnnotationList` 或中断脚本等参数
- THEN 插件必须根据这些参数调整生成文件的内容与目录布局
- AND 当目标文件被视为不应覆盖时，插件必须允许中断生成过程

### Requirement: 当前版本模板必须对齐 Spring Boot 4 基线
系统 MUST 确保当前模板和生成结果与仓库使用的 Spring Boot 4 基线保持一致。

#### Scenario: 生成新的 Starter 与工程模板
- WHEN 插件在当前仓库版本下生成 Starter、根 POM 或相关工程模板
- THEN 生成结果必须使用 Spring Boot 4 兼容的版本基线与自动配置注册方式
- AND 新生成的 Starter 注册文件必须采用 `AutoConfiguration.imports`

### Requirement: 生成器与运行时协同
系统 MUST 确保生成器输出与运行时模块约定一致，使生成的工程能够依赖当前仓库发布的 Simple DAO 产物。

#### Scenario: 生成的工程使用仓库发布产物
- WHEN 开发者使用生成器生成新工程或新模块
- THEN 生成结果必须引用与当前仓库一致的 Simple DAO 组件、service-support 组件和相关版本约定
- AND 生成结果必须能够衔接运行时提供的查询注解、Starter 与基础实体模型
