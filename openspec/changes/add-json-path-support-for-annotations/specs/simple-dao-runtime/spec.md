## MODIFIED Requirements

### Requirement: 注解驱动查询对象
系统 MUST 允许开发者在查询 DTO 或辅助类型上使用注解声明目标实体、关联关系、过滤条件、排序、分组和聚合表达式，以便从查询对象自动推导 SQL 或 JPQL 语句及其参数。

#### Scenario: 使用 JSON 路径注解生成 where 条件
- **WHEN** 开发者在 `com.levin.commons.dao.annotation.*` 根包下的 where 条件注解上声明 `jsonPath`
- **THEN** 运行时必须能够基于 JSON 路径改写字段表达式
- **AND** 非 wildcard JSON 路径必须能够参与普通比较条件
- **AND** wildcard JSON 路径必须能够参与 where 条件表达式生成

#### Scenario: 使用 JSON 路径 Exists 条件
- **WHEN** 开发者在 `@Exists`、`@NotExists` 或 `@Where(op = Op.Exists/NotExists)` 上声明 `jsonPath`
- **THEN** 运行时必须生成基于 `json_exists(...)` 的表达式
- **AND** 不得继续按子查询 `EXISTS (...)` 语义处理该 JSON 路径

#### Scenario: 使用 JSON 路径选择与更新
- **WHEN** 开发者在 `@Select` 或 `@Update` 上声明 `jsonPath`
- **THEN** `@Select` 必须能够支持标量、对象和数组路径结果
- **AND** `@Update` 必须能够通过 `json_set(...)` 更新指定 JSON 路径
- **AND** `@Update` 在 `jsonPath` 包含 `[*]` 时必须抛出明确异常

#### Scenario: 使用 JSON 路径统计
- **WHEN** 开发者在 `@Avg`、`@Sum`、`@Count`、`@Max`、`@Min` 或 `@GroupBy` 上声明 `jsonPath`
- **THEN** 运行时必须支持单值路径统计表达式
- **AND** 当 `jsonPath` 包含 `[*]` 时必须抛出明确异常

### Requirement: Spring Boot 4 兼容运行时基线
系统 MUST 以当前仓库定义的 Spring Boot 4 版本基线完成编译、自动配置注册和发布。

#### Scenario: 构建并验证 JSON 路径能力
- **WHEN** 仓库在当前 Spring Boot 4 / Hibernate 7 基线下构建 `simple-dao-annotations`、`simple-dao-core`、`simple-dao-jpa` 与 `simple-dao-examples`
- **THEN** JSON 路径相关主代码和测试代码必须能够通过编译
- **AND** 运行时必须直接使用 Hibernate 7 标准 JSON 函数
- **AND** 示例模块必须具备执行 JSON 路径集成测试所需的最小测试依赖
