## Context

仓库当前是一个多模块 Maven 工程，模块职责并不完全同质：

- `simple-dao-annotations`、`simple-dao-core`、`simple-dao-jpa`、`simple-dao-jpa-starter`、`simple-dao-id-generator` 共同组成运行时能力。
- `simple-dao-code-gen` 和 `simple-dao-code-gen-example` 主要负责模板化工程生成和数据库到代码的转换能力。
- `simple-dao-examples` 主要承担示例与验证作用，不适合作为一级产品能力规格。

如果把整个仓库直接写成一个大规格，后续任何运行时改动和生成器改动都会互相污染，导致规格边界不清晰。

## Decision

本次采用“两份当前态规格 + 一个本轮变更”的建模方式：

1. `simple-dao-runtime`
   负责描述查询注解、统一 DAO、JPA 集成、Starter 自动配置、基础领域模型和 UID 生成等运行时能力。
2. `simple-dao-codegen`
   负责描述项目模板生成、基于实体生成服务代码、数据库元数据解析、生成参数和 Spring Boot 4 模板对齐要求。
3. `document-current-project-baseline-and-boot4-compatibility`
   负责记录为什么要建立这两份规格，以及为什么当前规格需要显式覆盖 Spring Boot 4 兼容与发布基线。

## Rationale

这样拆分有三个好处：

- 能和当前仓库的模块边界基本对齐，后续查找“需求归属”更直接。
- 能把运行时需求和生成器需求分离，避免未来在一个规格里同时维护框架行为和脚手架行为。
- 能把本轮 Spring Boot 4 兼容升级视作“已沉淀进当前基线的要求”，而不是孤立的一次性说明。

## Non-Goals

- 不为 `simple-dao-examples` 和 `simple-dao-code-gen-example` 单独建立一级规格。
- 不在本次文档工作中补充新的功能需求，只记录仓库当前已经体现出的能力与约束。
- 不在本次 change 中修改业务代码、模板代码或发布脚本。
