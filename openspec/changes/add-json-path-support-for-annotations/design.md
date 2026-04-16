## Context

这次能力建立在现有注解驱动表达式架构上，不引入新的组合注解，而是统一给现有注解增加 `jsonPath()`。运行时仍通过 `ConditionBuilderImpl -> ExprUtils` 这一条主链生成 JPQL/HQL 表达式。

用户已经确认以下边界：

- 只支持 `JPA + Hibernate 7`
- 业务侧不需要手写 native SQL
- `com.levin.commons.dao.annotation.*` 根包下所有 where 条件注解都要支持
- 统计注解、`@Select`、`@Update` 都要支持
- JSON 路径支持对象、数组、`[*]`
- 统计注解和 `@Update` 不支持 `[*]`
- 对于非 wildcard 路径，不在框架层静态推断“这是对象还是标量”，由 Hibernate/数据库在运行时决定

## Goals / Non-Goals

**Goals**
- 让现有注解直接声明 JSON 路径。
- 统一输出 Hibernate 7 标准 JSON 函数名。
- 覆盖 where、统计、选择、更新四类语义。
- 为 wildcard 受限场景提供明确的框架级报错。

**Non-Goals**
- 不引入新的注解组合模型。
- 不实现 wildcard 批量更新。
- 不实现统计注解上的数组展开聚合。
- 不重新设计为 Criteria/SQM AST 级构造器。

## Decisions

### 1. 注解模型统一增加 `jsonPath`

在 `@C`、根包 where 注解、统计注解、`@Select`、`@Update` 上统一增加 `jsonPath() default ""`。这样业务侧仍沿用既有注解体系，只是在需要处理 JSON 字段时额外声明路径。

### 2. 运行时直接使用 Hibernate 7 标准 JSON 函数

框架内部不再引入自定义 `sd_json_*` 函数别名，也不再通过 `FunctionContributor` 做包装。表达式层直接输出：

- `json_value(...)`
- `json_query(...)`
- `json_exists(...)`
- `json_set(...)`

这样实现更贴近 Hibernate 7 标准能力，也降低了额外适配层的维护成本。

### 3. 不同注解语义采用不同 JSON 函数

- 普通 where 条件默认对 JSON 路径提取结果做标量比较，因此走 `json_value`
- wildcard 路径 where 走 `json_query`
- `@Exists/@NotExists` 的 JSON 路径走 `json_exists`
- `@Select` 的非 wildcard 路径走 `COALESCE(json_query(...), json_value(...))`，兼容对象/数组与标量返回
- `@Select` 的 wildcard 路径直接走 `json_query`
- `@Update` 走 `json_set`

### 4. wildcard 限制在表达式生成层统一校验

在 `ExprUtils` 中统一校验：

- `@Update` + `jsonPath` 包含 `[*]` 时抛出 `StatementBuildException`
- `@Avg/@Sum/@Count/@Max/@Min/@GroupBy` + `jsonPath` 包含 `[*]` 时抛出 `StatementBuildException`

这样不需要把校验逻辑分散在每个注解处理点。

### 5. examples 测试基线按最小依赖恢复

为了让 Boot 4 下的示例模块测试上下文正常启动，只在 `simple-dao-examples` 增加最小必要的测试作用域依赖，如 `spring-webmvc` 与 `jackson-databind`，不扩大生产依赖面。

## Risks / Trade-offs

- `@Select` 的非 wildcard 路径同时包一层 `json_query/json_value`，表达式会比单纯标量提取更长，但能兼容对象、数组与标量结果。
- wildcard where 统一走 `json_query`，不同数据库对返回值再参与 like/eq 的细节可能存在差异，但当前运行边界是 Hibernate 7 负责下推。
- examples 集成测试依赖当前 Boot 4 测试基线，若后续示例模块继续裁剪测试依赖，需要同步维护这些最小测试依赖。
