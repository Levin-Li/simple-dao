## ADDED Requirements

### Requirement: 关联列字段名使用编译期常量

生成脚手架提供的实体开发规范 SHALL 对映射到本实体 Java 字段的 `@JoinColumn(name = ...)` 使用本实体的 E_ 字段常量示例，并保留原有只读关联设置。

#### Scenario: 部门关联的外键字段

- **WHEN** 所属实体 `Employee` 以 `orgId` 字段保存外键
- **THEN** 示例使用 `@JoinColumn(name = E_Employee.orgId, insertable = false, updatable = false)`。
