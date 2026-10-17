# 实现与验证

## 模块落点

`ClassModel` 在构建时获取共享 `RbacDataAttrGroupMetadata` 的缓存结果；生成模板按源属性取得规范化组。源实体名、getter 注解、类级 attrList、重复和继承在共享解析器中统一解释，不复制原始注解源码。

`ServiceModelCodeGenerator.isMatch` 排除原始分组及容器注解，即使用户扩展 `keepAnnotationList` 也不重复或错误复制 attrList。八类模板覆盖 Create、SimpleUpdate/Update、SimpleQuery/Query、Info、通用 IdReq、DeleteReq。查询派生字段显式输出组，公共 Req/Info 基类承接的字段使用类级实际属性列表；SimpleQuery 的业务列规则经 TargetOption 源实体提供给 API。

## 验证方式

- `mvn -pl simple-dao-code-gen -am install -P '!01-跳过测试'`：生成器 649 项测试通过，上游 annotations/core 通过；插件已安装到本地 Maven 仓库。
- `RbacDataAttrGroupCodegenTest`：先编译实体并封装无源码 JAR，再走实际字段模型构建和八类模板，启用 Lombok 真编译后反射验证。覆盖同名合并、重复注解、getter 默认组名、等级取高、Java 字符串转义、父类字段、公共 tenantId/tenantName、查询派生和重复生成稳定。
- 兼容门禁已通过（109 项，0 失败、0 错误，1 项既有方言条件跳过）：`mvn -pl simple-dao-examples -am -Dtest=DaoExamplesTest,DaoJsonExamplesTest,DaoQueryExamplesTest -Dsurefire.failIfNoSpecifiedTests=false test -P '!01-跳过测试'`。PostgreSQL 条件跳过已有 MySQL UPDATE LIMIT 测试属于测试内显式方言假设，须在报告中说明。
- JAR 内容检查包含 `AGENTS.md`、`README.md` 和 `RBAC数据属性分组生成指南.md`；包内链接均可解析。

## 边界

生成器只传递属性权限元数据，不新增服务或 DAO 安全拦截；API 输入检查、JSON 写键前过滤、角色授权和运行时缓存由接入应用负责。SimpleQuery 不创建不存在的源字段。保持现有 Maven 版本；发布遵循仓库的 clean deploy 和兼容测试门禁。
