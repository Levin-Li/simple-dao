## 1. 受信条件模型

- [x] 1.1 先补标量、混合类型和集合字段的失败回归，扩展 `MandatoryCondition` 值白名单并保持不可变语义。
- [x] 1.2 调整强制 SQL/JPQL 参数与属性类型校验，覆盖 String/Integer/Long 和原有 Enum；仅显式标记的 JSON 字符串列表使用全部成员绑定范围，其余类型失败关闭。

## 2. JPA 与真实执行

- [x] 2.1 调整 JPA 创建范围校验，补真实数据库查询、COUNT、更新、删除、创建及更新后值测试。
- [x] 2.2 运行 core 聚焦测试及 `DaoExamplesTest,DaoJsonExamplesTest,DaoQueryExamplesTest` 综合契约测试，修复回归。

## 3. 交付说明

- [x] 3.1 更新 README、manual 使用入口和本变更实现说明，记录 JSON 列表支持边界与上游字典来源责任。
