# 受信字典强制范围实现与验证

## 模块落点

1. `simple-dao-core` 的 `MandatoryCondition` 继续使用不可变 DNF，现接受同类型的 Enum、String、Integer、Long；混合类型、可变对象、空字符串及空值拒绝。`ConditionBuilderImpl` 对直接实体标量字段使用参数绑定 IN；显式标记 `@MandatoryJsonStringArray` 的 `List<String>` 使用非空且无任何越界成员的 JSON 谓词，所有选项值经 `passing` 参数绑定。原生 JSON 范围和未标记、非字符串集合失败关闭。
2. `simple-dao-jpa` 创建前校验上述类型与每一分支；JSON 字符串列表要求非空且全部成员允许。原有直接写入拒绝、更新前后范围、构建器跨上下文拒绝及枚举转换保持。
3. `simple-dao-examples` 的 `ScopePartner` 增加测试用标量与 JSON 字段，真实 PostgreSQL/JPA 场景覆盖 COUNT/列表、详情、更新、删除、创建，包含混合允许/拒绝成员、空列表、非字符串 JSON 成员与允许的更新后值。首版仅使用不等比较时，数据库 JSONPath 对数字成员未给出预期的拒绝结果；真实反例先失败，再增加 JSON 元素类型判定后通过。示例字段仅用于测试，不改变上游业务实体。

## 验证证据

1. 测试优先：新增 String/Integer 测试先因旧枚举白名单失败；实现后 `mvn -pl simple-dao-core -Dtest=MandatoryConditionTest test -P '!01-跳过测试' -q` 13/13 通过。
2. `mvn -pl simple-dao-examples -am -Dtest=MandatoryScopeExamplesTest -Dsurefire.failIfNoSpecifiedTests=false test -P '!01-跳过测试' -q` 7/7 通过，使用本地 PostgreSQL 的实际 SQL/JPA 执行。额外写入包含数字成员的 JSON 数组，首版谓词错误放行，修复后拒绝并再次通过 7/7。
3. 项目必跑命令 `mvn -pl simple-dao-examples -am -Dtest=DaoExamplesTest,DaoJsonExamplesTest,DaoQueryExamplesTest -Dsurefire.failIfNoSpecifiedTests=false test -P '!01-跳过测试'` BUILD SUCCESS：109 项，0 失败、0 错误；其中 DaoQueryExamplesTest 的 `testNativeSQL` 因 PostgreSQL 不支持 MySQL 风格 `UPDATE ... LIMIT` 按既有 assumption 跳过 1 项，不作为本功能通过证据。
4. `mvn -pl simple-dao-core,simple-dao-jpa -am install -P '!01-跳过测试'` 本地安装成功；所选模块及上游 core 测试共 112 项通过，0 失败、0 错误、0 跳过。安装只更新本机 Maven 缓存，不发布远端。`openspec validate c20261101-1354-mandatory-dictionary-scope --strict` 与 `git diff --check` 通过。未提交、发布远端构件或执行外部数据库迁移。工作区已有 code-gen 模板修改未触及。

## 限制

1. Simple DAO 不查字典目录；接入方负责验证字典 key、当前租户、禁用状态、实际字典码、字段映射及角色完整许可。`@MandatoryJsonStringArray` 只声明强制范围语义，仍须把实体字段实际映射为 JSON 列。
2. JSON 全成员谓词仅在本地 PostgreSQL/Hibernate 7 真实执行过；其它方言、任意原生 SQL、外部 EntityManager、共享缓存及关联对象懒加载不在自动保护范围。配置错误或方言不支持时必须失败，不退化为宽松查询。
