## ADDED Requirements

### Requirement: 生成的租户基础字段不附带脱敏注解
代码生成器 SHALL 在 `MultiTenantReq` 的租户 ID 字段，以及 `MultiTenantInfo` 的租户 ID 和租户名称字段上不生成 `DataMasking`。生成器 MUST 保持这些字段已有的租户注入、引用注入、范围过滤和字段定义。

#### Scenario: 生成多租户请求对象
- **WHEN** 生成器渲染并编译 `MultiTenantReq`
- **THEN** `tenantId` 不含 `DataMasking`，其 `InjectVar` 和租户查询条件仍存在

#### Scenario: 生成多租户信息对象
- **WHEN** 生成器渲染并编译 `MultiTenantInfo`
- **THEN** `tenantId` 与 `tenantName` 均不含 `DataMasking`，`tenantName` 的 `RefInject` 仍存在
