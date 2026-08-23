# 项目开发规则

Simple DAO 是公共组件，改动影响的不只是当前业务代码，还包括外部项目对注解、链式 API、Repository 代理、JPA 实现、代码生成和示例用法的依赖。因此开发时要把示例测试当成兼容性测试，而不是可选 demo。

## 基本原则

- **Simple DAO 是公共组件，测试是所有变更的强制交付物和发布门禁。** 任何变更都必须有可执行的测试验证；任何影响行为、公共 API、配置、生成结果或兼容性的变更，都必须新增或更新能覆盖该变更的测试用例。
- 优先保持公共 API 的兼容性，避免随意改方法签名、注解语义、默认策略和生成 SQL/JPQL 的行为。
- 复用现有注解、构建器、表达式工具和测试风格，不轻易引入新依赖。
- 测试必须验证实际行为和预期结果；只断言字符串拼接不足以替代可执行的集成验证。对数据库、JPA 或方言相关能力，至少应有一个真实执行的测试。
- 修复缺陷时，先新增能稳定复现缺陷的回归测试；新增能力时，覆盖正常路径、关键边界及与既有注解/API 的组合路径。
- 重构或内部实现变更也必须运行覆盖受影响行为的既有测试；若既有测试无法覆盖，必须补充测试后才可合入。
- 生成目录如果存在 `code-gen.md`，说明该目录由生成器维护，不要手工改生成文件。
- 提交、合并、发布和交付前，相关测试以及本规范要求的综合测试必须全部通过。测试失败、跳过、无法执行或仅靠人工验证时，均不得提交、合并或发布。

## 后端开发规则

- 属性拷贝优先使用 MapStruct，不要使用 `cn.hutool.core.bean.BeanUtil`。如果 MapStruct 不满足场景要求，也应使用 Spring 的 `BeanUtils` 完成属性拷贝。

## 必跑测试

每次变更都必须运行与改动范围相关的测试；任何功能代码、构建配置、示例或测试代码变更，都必须同时新增或更新测试用例。纯文档改动至少应完成文档校验；如与代码变更同批交付，仍须通过该代码变更要求的全部测试。

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
- **每次提交、合并和发布前必须成功运行 `DaoExamplesTest`。** 不得用跳过测试、局部测试、人工验证或发布豁免替代该验证；外部依赖或环境阻塞时，必须先修复阻塞，不能提交、合并或发布。

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
3. 先新增或更新能够验证该变更的小而准的单元测试；修复问题时必须包含回归用例。
4. 能影响使用方式、SQL/JPQL 或数据库行为的能力，必须补 `DaoExamplesTest` 或示例模块的真实执行测试。
5. README 和 `manual.md` 要补充入口、使用示例、边界说明。
6. 运行相关模块测试和 `DaoExamplesTest`；全部通过后才能提交、发布或报告完成。
