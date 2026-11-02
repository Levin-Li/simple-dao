## 1. 受信条件模型

- [x] 1.1 先补标量、混合类型和集合字段的失败回归，扩展 `MandatoryCondition` 值白名单并保持不可变语义。
- [x] 1.2 调整强制 SQL/JPQL 参数与属性类型校验，覆盖 String/Integer/Long 和原有 Enum；仅显式标记的 JSON 字符串列表使用全部成员绑定范围，其余类型失败关闭。

## 2. JPA 与真实执行

- [x] 2.1 调整 JPA 创建范围校验，补真实数据库查询、COUNT、更新、删除、创建及更新后值测试。
- [x] 2.2 运行 core 聚焦测试及 `DaoExamplesTest,DaoJsonExamplesTest,DaoQueryExamplesTest` 综合契约测试，修复回归。

## 3. 交付说明

- [x] 3.1 更新 README、manual 使用入口和本变更实现说明，记录 JSON 列表支持边界与上游字典来源责任。
- [x] 3.2 修复 PostgreSQL JSON 函数输入类型，兼容原生 JSON 与 Hypersistence 映射，并在隔离数据库运行 JSON 和强制范围真实执行回归及强制示例测试。
  - 2026-11-02：隔离 PostgreSQL 16 及当前源码执行核心测试 35 项和示例 116 项，失败、错误均为 0；其中示例 `testNativeSQL` 因 MySQL 专属 UPDATE LIMIT 在 PostgreSQL 不兼容而按测试前置条件跳过 1 项。
