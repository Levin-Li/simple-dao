# 任务

- [x] 为值对象、泛型 JSON、List/Set 容器及循环图加入生成器回归测试，覆盖 nullable Boolean 计算 getter、已知 Map 键/值静态复制、未知 Map 值共享和具体 HashMap 类型。
- [x] 在 core 增加可注入 `JsonCodec` 的 `MapperJsonUtils` 与空值契约测试。
- [x] 在 4.3.0 的生成器元数据与 Mapper 模板中实现规则，值复制候选复用 Info 的 `FieldModel`。
- [x] 验证 core、生成器和示例兼容测试：core 与 codegen 模块全测试通过，`DaoExamplesTest` 44/44、`DaoJsonExamplesTest` 27/27、`DaoQueryExamplesTest` 37/38（1 项既有跳过）；下游 6.0 实体重生成及全量构建通过。根 POM 声明的 `simple-dao-code-gen-example` 在 4.3 分支不存在，故本线使用子模块 POM 运行等效门禁。
- [x] 按 core 再 codegen 的顺序发布变更模块：`simple-dao-core:4.3.0-20261010.163804-82`、`simple-dao-codegen:4.3.0-20261010.163819-133`；推送见本次 Git 交付。
- [x] 将只读/重载值属性改为静态复制并移除无用动态分派；覆盖 nullable Boolean 三态、Date 独立副本、未知可变字段（有/无 setter）生成失败。
- [x] 验证生成器和示例兼容测试；6.0 下游 86 个实体重新生成无实体错误，生成 Mapper 的 `copyDynamicValue` 数量为 0，`GeneratedValueCopyContractTest` 3/3、`OrgMapperCycleTest` 1/1 通过。仅发布 codegen 新快照 `4.3.0-20261010.171445-134` 并推送 4.3.0。
