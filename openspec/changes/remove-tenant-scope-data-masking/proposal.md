## Why

生成的租户请求和信息对象对租户 ID、租户名称附带 `DataMasking`，业务项目重生成时会恢复这些注解。当前需要暂停这层字段脱敏，同时保持租户注入、范围限制和其他字段语义不变。

## What Changes

- 从 `MultiTenantReq` 的 `tenantId` 删除 `DataMasking`。
- 从 `MultiTenantInfo` 的 `tenantId`、`tenantName` 删除 `DataMasking`。
- 清理这两个模板中仅供上述注解使用的 import，并验证实际渲染、编译后的字段不再携带该注解。

## Capabilities

### New Capabilities

无。

### Modified Capabilities

- `simple-dao-codegen`：调整生成的租户基础请求和信息对象的字段注解契约。

## Impact

仅影响 `simple-dao-code-gen` 内的两个源模板及其生成测试。生成的租户字段将不再由这两处模板附加脱敏注解；现有 `@InjectVar`、`@RefInject`、租户范围条件及公开字段保持原状。业务项目需要使用更新后的生成器重新生成，不能手改生成目录。
