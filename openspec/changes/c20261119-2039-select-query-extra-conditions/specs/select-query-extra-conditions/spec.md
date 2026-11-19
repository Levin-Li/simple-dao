## ADDED Requirements

### Requirement: 查询参数注释完整

生成的默认服务接口与实现 MUST 保持全部现有方法签名和行为。`query(req, paging, Object... queryObjs)` 的模板注释 MUST 明确主查询对象、独立分页参数、可附加的普通注解查询对象与 `Consumer<SelectDao<实体>>` 条件回调，并提供可复制理解的调用示例。`selectQuery` 的注释 MUST 明确其当前只接收列名，不得暗示它也接受扩展查询对象。

#### Scenario: 使用者阅读生成代码
- **WHEN** 使用者查看生成服务的 `query` Javadoc
- **THEN** 能知道如何传入分页、普通查询条件对象和回调，及如何在需要指定列时使用主请求的 `selectColumns`

#### Scenario: 生成结果兼容
- **WHEN** 用更新后的模板生成默认服务
- **THEN** 只有注释内容变化；所有方法签名与方法体保持原样
