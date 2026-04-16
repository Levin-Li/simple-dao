# 2026-04-17 JSON 路径注解支持实现计划

## 目标

为 `simple-dao` 注解体系增加统一的 `jsonPath` 能力，覆盖 where、统计、`@Select`、`@Update`，并直接使用 Hibernate 7 标准 JSON 函数。

## 执行分解

1. 扩展注解模型
   - 为 `@C`、根包 where 条件注解、统计注解、`@Select`、`@Update` 增加 `jsonPath`

2. 接入表达式重写
   - 新增 JSON 路径辅助类
   - 在 `ExprUtils` 中按注解语义改写为 `json_value/json_query/json_exists/json_set`
   - 对 `@Select` 的非 wildcard 路径提供 `json_query + json_value` 兜底

3. 加入约束校验
   - 在表达式生成层统一拦截统计注解和 `@Update` 的 wildcard JSON 路径

4. 清理自定义函数包装
   - 删除此前临时加入的 `JsonFunctionContributor`
   - 全面切回 Hibernate 7 标准 JSON 函数名

5. 补充验证
   - 增加核心单测
   - 增加 examples 集成测试
   - 恢复 examples 测试运行所需的最小测试依赖

## 当前结果

- 已完成全部实现与验证
- 核心单测通过
- examples JSON 路径集成测试通过
