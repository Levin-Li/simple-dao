## ADDED Requirements

### Requirement: 限定类型引用不需要内部类型导入
生成器 MUST 根据未限定类型引用识别单类型导入的使用情况。

#### Scenario: 同名内部类型被外部类限定
- **WHEN** 源码包含 `Customer.Type` 和 `Supplier.Type`，且不使用单独的 `Type`
- **THEN** 移除这两个内部类型的单类型导入，同时保留引用所需的外部类导入

#### Scenario: 简单类型引用与限定引用并存
- **WHEN** 源码同时使用简单类型 `Type` 和限定类型 `Customer.Type`
- **THEN** 保留实际需要的简单类型导入
