## ADDED Requirements

### Requirement: 安全引用属性分组常量
生成器 SHALL 仅在 DTO 属性存在于实体且同包 E_ 实体类包含同名、公开静态 final 字符串字段，其 class 文件 ConstantValue 与属性名完全相同时引用该常量；否则输出原属性名的字符串字面量。

#### Scenario: DTO 派生别名
- **WHEN** DTO 属性是实体中不存在的派生别名
- **THEN** 输出该别名的字符串字面量。

#### Scenario: 常量缺失或非编译期常量
- **WHEN** 常量缺失、不可访问、没有 ConstantValue 或值不匹配
- **THEN** 输出原属性名的字符串字面量。
