# Delta for Simple DAO Runtime

## ADDED Requirements

### Requirement: 注解驱动查询对象
系统 MUST 允许开发者在查询 DTO 或辅助类型上使用注解声明目标实体、关联关系、过滤条件、排序、分组和聚合表达式，以便从查询对象自动推导 SQL 或 JPQL 语句及其参数。

#### Scenario: 使用聚合查询对象执行统计查询
- WHEN 开发者在查询对象上使用 `@TargetOption`、`@JoinOption`、`@Sum`、`@Avg`、`@GroupBy` 等注解并调用 `findByQueryObj`
- THEN 运行时必须根据注解元数据和字段值构造等价的查询语句与参数
- AND 查询结果必须能够映射回声明的结果类型

### Requirement: 统一 DAO 执行接口
系统 MUST 提供统一的 DAO 接口，用于创建、保存、更新、删除、唯一性检查、列表查询、总数统计和分页查询。

#### Scenario: 使用统一接口执行分页查询
- WHEN 应用代码调用 `SimpleDao.findPagingDataByQueryObj` 或 `findPageByQueryObj`
- THEN 运行时必须执行查询并返回带有结果列表和分页信息的分页对象
- AND 当查询对象未显式提供分页定义时，运行时必须使用默认分页实现

#### Scenario: 使用统一接口执行唯一更新或删除
- WHEN 应用代码调用 `singleUpdateByQueryObj`、`uniqueUpdateByQueryObj`、`singleDeleteByQueryObj` 或 `uniqueDeleteByQueryObj`
- THEN 运行时必须根据受影响记录数返回布尔结果或抛出异常
- AND 相关操作必须在事务边界内执行

### Requirement: Spring Boot 与 JPA 自动配置
系统 MUST 能够通过 Spring Boot 自动配置为应用提供 `JpaDao`、DAO 事件总线、仓库代理和默认实体扫描能力。

#### Scenario: 引入 Starter 后启用默认自动配置
- WHEN Spring Boot 应用引入 `simple-dao-jpa-starter`
- THEN Starter 必须通过 `AutoConfiguration.imports` 注册 `JpaDaoConfiguration`
- AND 在满足条件时必须创建 `JpaDao` 与默认 `DaoEventBus` Bean
- AND 必须扫描框架提供的基础实体与仓库代理定义

### Requirement: JPA 基础领域模型支持
系统 MUST 提供可复用的基础实体模型与命名策略支持，以覆盖命名实体、多租户实体、树形实体和共享实体等常见建模场景。

#### Scenario: 继承框架提供的基础实体
- WHEN 开发者继承 `AbstractBaseEntityObject`、`AbstractTreeObject`、`AbstractMultiTenantObject` 等基础类型
- THEN 这些类型必须能够参与 JPA 实体映射与 Simple DAO 查询能力
- AND Starter 初始化过程必须允许初始化命名策略和表注释相关能力

### Requirement: 通用 ID 生成支持
系统 MUST 提供与运行时集成的 UID 生成能力，以支持 JPA 或 Hibernate 场景下的主键生成。

#### Scenario: 在持久化场景下使用默认 UID 生成器
- WHEN 应用引入 `simple-dao-id-generator` 并处于 JPA 或 Hibernate 运行环境
- THEN 系统必须提供基于百度 UID 生成逻辑封装的标识生成能力
- AND 该能力必须能够通过 Hibernate 标识生成接口接入持久化过程

### Requirement: Spring Boot 4 兼容运行时基线
系统 MUST 以当前仓库定义的 Spring Boot 4 版本基线完成编译、自动配置注册和发布。

#### Scenario: 构建运行时模块
- WHEN 仓库以父 POM 中声明的 Spring Boot 4 版本构建 `simple-dao-annotations`、`simple-dao-core`、`simple-dao-jpa`、`simple-dao-jpa-starter` 和 `simple-dao-id-generator`
- THEN 这些模块必须声明完成编译所需的直接依赖
- AND 自动配置相关模块必须采用 Spring Boot 4 兼容的注册方式
- AND Maven 构建流程必须能够生成并发布对应产物
