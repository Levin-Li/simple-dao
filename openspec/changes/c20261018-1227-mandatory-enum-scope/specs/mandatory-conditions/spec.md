# 强制条件规范

## ADDED Requirements
### Requirement: 强制条件与业务条件独立
受信提供器 MUST 在受控实体操作时提供不可变范围。DAO MUST 将其与业务条件外层 AND 组合，不允许其满足安全模式要求的业务定位条件。COUNT 与查询 MUST 应用相同范围和参数。空许可 MUST 拒绝或生成恒假条件。

#### Scenario: 无提供器
- WHEN 未注册提供器
- THEN 原语句、参数及安全模式语义保持不变

#### Scenario: 多字段多角色
- WHEN 提供多个分支
- THEN 分支间 OR、同分支字段间 AND，保留组合关系
