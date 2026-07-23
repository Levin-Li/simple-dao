# 项目开发规则

Simple DAO 是公共组件，改动影响的不只是当前业务代码，还包括外部项目对注解、链式 API、Repository 代理、JPA 实现、代码生成和示例用法的依赖。因此开发时要把示例测试当成兼容性测试，而不是可选 demo。

## 基本原则

- 优先保持公共 API 的兼容性，避免随意改方法签名、注解语义、默认策略和生成 SQL/JPQL 的行为。
- 复用现有注解、构建器、表达式工具和测试风格，不轻易引入新依赖。
- 对查询、更新、删除、统计、JSON Path、Repository 代理、JPA/原生查询等公共能力的改动，要补充或更新测试。
- 生成目录如果存在 `code-gen.md`，说明该目录由生成器维护，不要手工改生成文件。
- 提交或交付前要说明做了什么验证；如果某个关键测试无法运行，要明确写出原因。

## 后端开发规则

- 属性拷贝优先使用 MapStruct，不要使用 `cn.hutool.core.bean.BeanUtil`。如果 MapStruct 不满足场景要求，也应使用 Spring 的 `BeanUtils` 完成属性拷贝。

## 必跑测试

每次代码变更后，至少运行与改动模块相关的测试。只改 README、手册、注释等纯文档内容时，可以不跑完整测试。

凡是改动下面任一范围，必须运行 `DaoExamplesTest`：

- DAO 行为或公共接口。
- 注解解析、条件构建、查询/更新/删除语句生成。
- 统计、CASE 表达式、分组、having、排序。
- JSON Path 查询、选择、更新、数组追加。
- JPA 查询、原生查询、Repository 代理。
- 示例模块、默认服务、默认控制器、代码生成规则。

命令：

```bash
mvn -pl simple-dao-examples -am -Dtest=DaoExamplesTest -Dsurefire.failIfNoSpecifiedTests=false test -P '!01-跳过测试'
```

说明：

- `DaoExamplesTest` 位于 `simple-dao-examples/src/test/java/com/levin/commons/dao/DaoExamplesTest.java`。
- 这个测试覆盖大量真实使用方式，应视为端到端使用契约测试。
- 使用 `-Dsurefire.failIfNoSpecifiedTests=false` 是为了避免 `-am` 带上的上游模块因为没有同名测试而提前失败。
- 如果当前分支存在已知生成代码问题导致测试无法通过，交付说明里必须写清楚失败模块、失败类和核心错误。

## 常用验证命令

```bash
# 编译全部模块
mvn compile

# 运行全部测试
mvn test -P '!01-跳过测试'

# 运行 core 中当前重点测试
mvn -pl simple-dao-core -Dtest=JsonProgrammaticBuilderTest,ExprUtilsTest,JsonPathAnnotationSupportTest test -P '!01-跳过测试'

# 运行综合示例契约测试
mvn -pl simple-dao-examples -am -Dtest=DaoExamplesTest -Dsurefire.failIfNoSpecifiedTests=false test -P '!01-跳过测试'
```

## 新增能力流程

1. 先确认能力属于注解式、链式 API、Repository 代理、JPA 实现、原生查询、代码生成中的哪一层。
2. 优先用现有表达式工具和注解解析链路扩展，不绕过已有安全模式。
3. 给核心工具或构建器补小而准的单元测试。
4. 能影响使用方式的能力，要补 `DaoExamplesTest` 或示例模块测试。
5. README 和 `manual.md` 要补充入口、使用示例、边界说明。
6. 运行相关模块测试和 `DaoExamplesTest`，再报告完成情况。
