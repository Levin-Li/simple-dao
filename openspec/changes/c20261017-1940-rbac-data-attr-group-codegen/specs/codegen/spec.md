## ADDED Requirements

### Requirement: 保持源实体分组
生成器 MUST 将源实体规范化分组传递到实际存在的创建、修改、查询和 Info 属性，保留 TargetOption 源实体。

#### Scenario: 查询派生属性
- **WHEN** 源属性声明权限分组
- **THEN** between、gte、lte、in、notIn、contains、load 等派生属性保持同一显式分组名称，不根据生成标题重新命名。

#### Scenario: 无注解兼容
- **WHEN** 实体没有有效分组
- **THEN** 不生成分组注解。

#### Scenario: 默认机密级别
- **WHEN** 规范化分组的机密级别等于 `ConfidentialLevel.PLATFORM_PUBLIC_CODE`
- **THEN** 生成的 `RbacDataAttrGroup` MUST 省略 `confidentialLevel`，由注解默认值保持平台公开语义；非默认级别 MUST 显式输出。

#### Scenario: 基础查询
- **WHEN** SimpleQuery 不含源业务属性
- **THEN** 仅保留 TargetOption 供 API 从源实体获取选择列和排序规则，不生成引用不存在属性的 attrList。

#### Scenario: 主键与删除请求
- **WHEN** 源实体主键声明分组
- **THEN** 通用 IdReq 的主键和 DeleteReq 的主键列表 MUST 传递同一规范化分组，公共基类承接属性继续保留实际属性注解。
