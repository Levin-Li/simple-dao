## ADDED Requirements

### Requirement: 强制范围支持受信字典标量
`MandatoryCondition` MUST 在保留枚举值语义的同时，接受受信提供器给出的非空 String、Integer 或 Long 标量值，以及显式受控 JSON `List<String>` 的允许集合；每个直接实体属性的值类型 MUST 与字段类型相符，参数 MUST 绑定，非法类型、空许可与不受支持的 JSON 集合属性 MUST 失败关闭。

#### Scenario: 字符串字典值过滤
- **WHEN** 受信提供器对实体 String 字段只允许两个实际字典码
- **THEN** SELECT、COUNT、UPDATE、DELETE 使用同一绑定 IN 范围，未获准记录和写入后的越界值不可访问

#### Scenario: 数值字典值过滤
- **WHEN** 受信提供器对实体 Integer 或 Long 字段指定允许值
- **THEN** 数据库参数保持匹配数值类型，创建及更新后的值仍须位于允许范围

#### Scenario: JSON 字符串列表全部成员受控
- **WHEN** 显式标记的 JSON `List<String>` 含多个成员，部分值不在同一角色允许集合中
- **THEN** 整条记录被数据库强制范围排除，创建和更新后的越界列表被拒绝；空列表及非字符串元素不得作为绕过方式

#### Scenario: 非法或未支持属性
- **WHEN** 提供器传入混合类型、任意对象、属性类型不符或未支持的 JSON 字段/方言
- **THEN** 构建或执行前拒绝，不生成宽松查询、应用层读后过滤或无条件放行

### Requirement: 原有范围兼容
新增标量能力 MUST 保留已有 Enum 强制范围的不可变 DNF、同字段交集、分支并集、查询与统计参数一致、更新前后范围及直接 JPA 写入保护；无提供器或不受控实体 MUST 保持原行为。

#### Scenario: 枚举与字典标量联合
- **WHEN** 同一分支包含一个枚举属性和一个 String 字典属性
- **THEN** 两字段取交集，多个完整分支取并集，切换身份时旧构建器拒绝沿用旧范围
