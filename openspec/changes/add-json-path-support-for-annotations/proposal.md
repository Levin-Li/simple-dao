## Why

当前 `simple-dao` 的注解驱动查询体系可以处理普通字段，但还不能直接表达 JSON 字段路径查询、投影、统计和更新。对于已经通过 Hibernate 7 映射为 JSON 类型的字段，业务侧需要继续复用 `@Eq`、`@Contains`、`@Select`、`@Update` 以及统计注解，而不是回退到手写原生 SQL。

现在需要为注解体系补充统一的 `jsonPath` 能力，并让运行时直接走 Hibernate 7 标准 JSON 函数。

## What Changes

- 为 `com.levin.commons.dao.annotation.*` 根包 where 条件注解统一增加 `jsonPath` 属性。
- 为统计注解、`@Select`、`@Update` 增加 `jsonPath` 属性。
- 在 `simple-dao-core` 中增加 JSON 路径表达式重写与限制校验。
- 在运行时直接输出 Hibernate 7 标准 JSON 函数，例如 `json_value`、`json_query`、`json_exists`、`json_set`。
- 补充核心单测与 examples 集成测试，并恢复这些测试所需的最小测试依赖。

## Capabilities

### Modified Capabilities

- `simple-dao-runtime`: 注解驱动查询对象必须支持基于 `jsonPath` 的 JSON 字段查询、投影、统计和更新
- `simple-dao-runtime`: 示例模块必须能够编译并执行 JSON 路径相关的集成测试

## Impact

- 影响 `simple-dao-annotations` 的公开注解 API。
- 影响 `simple-dao-core` 的表达式生成逻辑。
- 影响 `simple-dao-examples` 的测试依赖与 JSON 路径示例测试。
