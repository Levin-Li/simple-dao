# 实现与验证

## 实现落点

- core公开 `MandatoryCondition`、`MandatoryConditionProvider`、`MandatoryConditions`：不可变三态DNF、可信注册、多个提供器交集。
- ConditionBuilderImpl：目标明确后准备范围，业务条件树之外保存权限，SQL与参数边界重新核对当前可信范围，变化时拒绝复用；等价分支保持首次渲染顺序。
- SelectDaoImpl：列表、JPQL/native/count/stat/fetch分支均带同一强制参数。
- UpdateDaoImpl：旧范围AND写后范围；单字段赋值代入，未变字段保留数据库谓词；未知受控表达式和复合赋值拒绝。
- DeleteDaoImpl：物理/逻辑删除和预检查使用删除范围。
- JpaDaoImpl：受限实体find/deleteById走条件构建；create检查值；直接save/delete(Object)拒绝。普通实体不改旧行为。

## 回归事实

- 核心新测试覆盖不可变、DNF交集、多角色字段组合、业务OR括号、空范围、安全模式、渲染幂等、上下文变窄或撤销、SQL先/参数先，以及部分更新表达式。
- H2新权限场景4/4通过；H2既有JSON wildcard测试不支持，未修改或跳过它，改用隔离PostgreSQL完整验证。
- PostgreSQL EnumDesc业务code不等ordinal且展示toString中文的原生查询回归先红（count预期1实际0）；仅强制条件绑定器修复为name/ordinal，未改旧ObjectUtil转换行为。
- 隔离PostgreSQL随机本机端口、专用库、随机凭据，未访问本机5432或真实业务库。

## 覆盖边界

详见[使用手册](../../../manual.md#受信的枚举记录范围mandatorycondition)。任意QL字符串、外部EntityManager、关联lazy和服务缓存不会因注册该SPI而自动获得保护；框架集成必须限制或适配这些入口。自定义枚举AttributeConverter不能仅由@Enumerated规则推断。

完整模块首轮运行224项Examples，3项DDL安全配置测试因验证环境变量 `SPRING_JPA_HIBERNATE_DDL_AUTO=update` 覆盖其临时YAML失败。移除该环境覆盖后按相同全套范围重跑；未修改或放宽既有安全规则、测试。失败证据另存 `/tmp/rbac-dao-ddl-env-failure/`。

## 最终验证结果

使用JDK25、停用跳过测试profile，运行 `mvn -q -nsu -pl simple-dao-core,simple-dao-jpa,simple-dao-code-gen,simple-dao-examples -am test -P '!01-跳过测试'`，数据源仅指向隔离PostgreSQL：退出0。

- annotations：2项，零失败/错误。
- core：65项（含强制范围8项），零失败/错误。
- codegen：653项，零失败/错误。
- Examples：224项（含新增真实数据库权限5项），零失败/错误；1项既有MySQL方言条件跳过。
- 总计944项，零失败/错误；新增权限测试无跳过。
- `DaoExamplesTest` 44项、`DaoJsonExamplesTest` 27项、`DaoQueryExamplesTest` 38项均执行，其中Query仅1项既有方言跳过。
- 全套日志：`/tmp/rbac-dao-final-all-tests.log`；原生EnumDesc绑定红灯：`/tmp/rbac-dao-native-enum-red.log`。
- `git diff --check` 通过。未提交、推送或发布；供主框架统一本地安装与联调。
