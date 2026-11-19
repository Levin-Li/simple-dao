# 实现与验证

`service.ftl` 只完善 `query(req, paging, Object... queryObjs)` 的 Javadoc：说明主查询请求、`Paging`/`SimplePaging`、默认分页、附加注解查询对象、`Consumer<SelectDao<实体>>` 回调和指定列的用法，并给出同时使用这些参数的示例。`selectQuery` 的注释明确其可变参数仅为列名。`service_impl.ftl` 补充与现有实现一致的简短说明。此前误加的扩展重载和对应测试已撤回，`README.md`、`manual.md` 的误导性新用法也已撤回。

验证：最终模板的 `SelfOverridableMatchCodegenTest` 通过；此前新增的重载回归测试随误加方法一并撤回。按仓库门禁重新运行 `DaoExamplesTest` 45 项、`DaoJsonExamplesTest` 28 项、`DaoQueryExamplesTest` 38 项，失败与错误均为 0，最后一组按现有条件跳过 1 项。模板差异仅含注释，`git diff --check` 与 OpenSpec 校验通过。

本次仅修改 4.3.0 分支的模板注释；未发布插件、未在下游项目重新生成默认服务。下游当前已生成类的注释不会自动更新，须在插件交付后按各项目的正式生成流程刷新。
