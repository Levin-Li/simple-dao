## 1. 注解模型扩展

- [x] 1.1 为 `@C`、根包 where 条件注解、统计注解、`@Select`、`@Update` 增加 `jsonPath`

## 2. 核心运行时实现

- [x] 2.1 新增 JSON 路径解析与表达式辅助类
- [x] 2.2 在 `ExprUtils` 中接入 JSON 路径表达式重写
- [x] 2.3 为 `@Exists/@NotExists` 增加 `json_exists` 语义
- [x] 2.4 为统计注解和 `@Update` 增加 wildcard 限制校验
- [x] 2.5 移除自定义 JSON `FunctionContributor`，统一改为 Hibernate 7 标准函数名

## 3. 测试与示例

- [x] 3.1 增加核心单测，覆盖注解签名、表达式重写与限制校验
- [x] 3.2 增加 examples 集成测试，覆盖 where/select/update/stat 四类场景
- [x] 3.3 补齐 examples 测试上下文所需的最小测试依赖

## 4. 规格与验证

- [x] 4.1 补充 OpenSpec change 文档与 runtime spec delta
- [x] 4.2 执行 `openspec validate`
- [x] 4.3 执行核心单测与 examples JSON 路径集成测试
