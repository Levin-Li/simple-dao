# DAO 强制数据范围条件

需求事实源：levin-framework-base 的 `openspec/changes/c20261018-1227-rbac-enum-value-scope/`。

新增与 Servlet/RBAC 无关的三态条件 SPI。受信提供器按实体、操作返回不可变 DNF 范围。条件独立于业务条件树与安全模式，统一参与查询、COUNT、更新、删除参数生成。无提供器或不受控实体保持原行为。更新需同时满足旧记录范围和替换赋值后的范围；不可判定表达式拒绝。JPA 直接入口需保护或明确拒绝受控实体。原生 SQL 字符串和外部 EntityManager 不具备实体元信息，不承诺自动改写，集成方须限制访问。
