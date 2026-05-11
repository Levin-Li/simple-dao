# Simple DAO 项目使用手册

本文档面向两类读者：

- 在业务项目中使用 `simple-dao` 的开发者：重点看“快速接入”“查询对象”“更新删除”“统计与多表”“常见业务例子”。
- 维护本仓库或使用 `simple-dao-codegen` 生成项目的开发者：重点看“代码生成工作流”“生成目录边界”“本仓库模块说明”。

手册中的例子尽量使用仓库里已经存在的代码形态，便于你从文档跳回源码继续看：

- 核心接口：`simple-dao-core/src/main/java/com/levin/commons/dao`
- 注解定义：`simple-dao-annotations/src/main/java/com/levin/commons/dao`
- JPA 实现与自动配置：`simple-dao-jpa/src/main/java/com/levin/commons/dao`
- 可运行例子与测试：`simple-dao-examples/src/test/java/com/levin/commons/dao`
- 代码生成插件：`simple-dao-code-gen/src/main/java/com/levin/commons/dao/codegen/plugins`

## 1. Simple DAO 解决什么问题

Simple DAO 的核心目标是：把常见的查询、更新、删除、统计、排序、分页、多表连接等数据库操作表达在 Java DTO 或链式 API 中，减少手写 SQL/JPQL 的数量。

最常见的写法是：

1. 定义实体类，例如 `User`、`Group`。
2. 定义查询/更新 DTO，在字段上加 `@Eq`、`@Contains`、`@Between`、`@Update`、`@GroupBy` 等注解。
3. 在服务中注入 `SimpleDao` 或 `JpaDao`。
4. 调用 `dao.findByQueryObj(req)`、`dao.updateByQueryObj(req)`、`dao.deleteByQueryObj(req)`。

例如，一个“按姓名模糊查询用户、按分数区间过滤、按名称排序、分页返回”的需求，可以写成一个 DTO：

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = UserListItem.class)
@Data
@Accessors(chain = true)
public class QueryUserReq {

    Paging paging = new PagingQueryReq(1, 20);

    @Contains
    String name;

    @Between("score")
    Integer[] scoreRange;

    @OrderBy
    String orderCode;
}
```

服务层只需要：

```java
@Service
public class UserQueryService {

    @Autowired
    SimpleDao dao;

    public PagingData<UserListItem> query(QueryUserReq req) {
        return dao.findPagingDataByQueryObj(UserListItem.class, req);
    }
}
```

理解这个模型后，后面的大部分功能都只是“换注解、换字段、换结果类”。

## 2. 本仓库模块说明

| 模块 | 作用 |
| --- | --- |
| `simple-dao-annotations` | DTO、实体、仓储方法使用的注解，例如 `@Eq`、`@Contains`、`@Update`、`@TargetOption`。 |
| `simple-dao-core` | SQL/JPQL 语句构建核心、`SimpleDao`、`SelectDao`、`UpdateDao`、`DeleteDao` 等接口。 |
| `simple-dao-jpa` | 基于 JPA/Hibernate 的实现，提供 `JpaDao`、`JpaDaoImpl` 和自动配置。 |
| `simple-dao-jpa-starter` | Spring Boot Starter，接入业务项目时优先使用。 |
| `simple-dao-codegen` | Maven 插件，支持从实体生成服务、控制器、请求对象、项目模板等。 |
| `simple-dao-id-generator` | ID 生成器相关能力。 |
| `simple-dao-examples` | 示例实体、DTO、Repository、测试用例。建议把它当成“可搜索的使用手册”。 |
| `simple-dao-code-gen-example` | 代码生成插件示例项目。 |

当前根 `pom.xml` 中的关键版本：

```xml
<revision>4.3.0-SNAPSHOT</revision>
<spring-boot.version>4.0.5</spring-boot.version>
<maven.compiler.release>17</maven.compiler.release>
```

开发时建议使用 JDK 25 执行 Maven；项目会按 `release 17` 生成目标字节码。

## 3. 快速接入 Spring Boot 项目

### 3.1 引入依赖

如果你通过 JitPack 使用本项目，可以参考 `README.md` 中的坐标：

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://www.jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.Levin-Li.simple-dao</groupId>
    <artifactId>simple-dao-jpa-starter</artifactId>
    <version>4.3.0-SNAPSHOT</version>
</dependency>
```

如果在多模块项目中直接依赖本仓库构件，常见坐标是：

```xml
<dependency>
    <groupId>com.levin.commons</groupId>
    <artifactId>simple-dao-jpa-starter</artifactId>
    <version>4.3.0-SNAPSHOT</version>
</dependency>
```

业务项目仍然需要提供自己的数据库驱动和 JPA 基础配置，例如 PostgreSQL、MySQL、H2 等。

### 3.2 启用自动配置

`simple-dao-jpa-starter` 已通过 Spring Boot 自动配置加载 `JpaDaoConfiguration`。如果需要显式导入，也可以在启动类上使用：

```java
@SpringBootApplication
@EnableSimpleDao
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

自动配置会注册默认的 `JpaDao` Bean。业务代码里一般直接注入：

```java
@Service
public class DemoService {

    @Autowired
    JpaDao dao;

    // 或注入 SimpleDao，JpaDao 是 SimpleDao 的 JPA 实现。
}
```

### 3.3 推荐的 JPA 配置

开发环境可以参考 `simple-dao-examples/src/test/resources/application.yml`，核心思想是：

```yaml
spring:
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: update
      naming:
        physical-strategy: com.levin.commons.dao.support.EntityNamingStrategy
```

说明：

- `open-in-view: false` 可以减少视图层触发懒加载导致的 N + 1 风险。
- `EntityNamingStrategy` 用于实体名和物理表名转换。
- 生产环境是否使用 `ddl-auto` 需要按项目规范决定，不建议随意 `update` 生产库。

## 4. 核心心智模型

### 4.1 `@TargetOption` 决定操作目标

`@TargetOption` 通常加在 DTO 类上，用来说明这个 DTO 面向哪个实体、别名是什么、结果类型是什么。

```java
@TargetOption(
        entityClass = User.class,
        alias = "u",
        resultClass = UserInfo.class,
        maxResults = 100,
        safeMode = true
)
@Data
public class QueryUserReq {

    @Contains
    String name;
}
```

常用属性：

| 属性 | 说明 |
| --- | --- |
| `entityClass` | 主实体类。 |
| `alias` | 查询别名，复杂查询里建议显式指定。 |
| `resultClass` | 查询返回的 DTO 类型。 |
| `nativeQL` | 是否使用原生 SQL。默认是 JPQL/HQL 风格。 |
| `joinOptions` | 多表关联配置。 |
| `safeMode` | 安全模式，默认开启，避免无条件更新/删除等危险操作。 |
| `maxResults` | 限制最大结果数。 |

查询最佳实践：

- 字段名尽量使用生成的常量，例如 `E_Order.tenantId`、`E_Order.orgId`、`E_Order.name`，不要到处散落字符串字段名。这样实体字段重命名时，编译器会报错，重构更安全。
- 平时查询、更新、删除优先使用注解 DTO，把业务语义、必填条件、安全范围和结果映射固定下来；编程式链式 API 只建议用于少量动态组合、临时扩展、后台任务、调试或 DTO 注解不方便表达的场景。
- 没有特殊原因时，不建议使用原生查询。默认的 JPA/JPQL 模式面向实体类和实体属性，字段重构、DTO 映射、跨数据库兼容性都更稳。
- 只有确实需要数据库专用函数、复杂 SQL、性能调优、特殊原生统计或无法用 JPQL 表达的语句时，再开启 `nativeQL = true` 或使用 `selectByNative(...)`。

### 4.2 字段注解决定 SQL 片段

字段上最常见的注解：

| 注解 | 典型含义 | 示例 |
| --- | --- | --- |
| `@Eq` | 等于 | `id = ?` |
| `@NotEq` | 不等于 | `status <> ?` |
| `@Contains` | 包含 | `name like '%keyword%'` |
| `@StartsWith` | 前缀匹配 | `code like 'A%'` |
| `@EndsWith` | 后缀匹配 | `code like '%9'` |
| `@Gt` / `@Gte` | 大于 / 大于等于 | `score > ?` |
| `@Lt` / `@Lte` | 小于 / 小于等于 | `createTime <= ?` |
| `@In` / `@NotIn` | 包含于集合 / 不包含于集合 | `state in (?, ?)` |
| `@Between` | 区间 | `score between ? and ?` |
| `@IsNull` / `@IsNotNull` | 空值判断 | `deletedAt is null` |
| `@Select` | 选择列或表达式 | `select name` |
| `@Update` | 更新列 | `set name = ?` |
| `@OrderBy` | 排序 | `order by createTime desc` |
| `@GroupBy` | 分组 | `group by state` |
| `@Sum` / `@Avg` / `@Count` | 统计 | `sum(score)` |

字段没有注解时，默认按 `@Eq` 处理；但字段值为 `null` 或空字符串时，默认不生成条件。

### 4.3 DAO 注解的默认策略

理解默认策略很重要，因为它直接关系到“为什么字段为空时没有条件”和“怎么防止数据泄露”。

| 策略 | 说明 |
| --- | --- |
| 字段无注解 | 默认按 `@Eq` 处理。 |
| 字段值为空 | 多数条件注解默认 `condition = VALUE_NOT_EMPTY`，字段值为 `null` 或空字符串时不生成条件。 |
| 复杂对象字段无注解 | 会尝试递归解析为子查询对象，前提是字段不是简单值类型。 |
| `@Ignore` | 强制忽略字段，不参与查询、更新、排序、统计。 |
| `domain` | 指定字段属于哪个表别名，多表查询时尤其重要。 |
| `value` | 指定目标字段名；DTO 字段名和实体字段名不一致时必须写。 |
| `condition` | 控制注解是否生效，返回 `false` 时该注解不产生语句。 |
| `require = true` | 要求注解必须生效；如果 `condition` 不满足，会直接抛异常。 |
| 安全模式 | 默认开启，不允许无条件查询、更新、删除，或超出安全范围的大结果集查询。 |

例如：

```java
@Eq
Long id;
```

当 `id == null` 时不会生成 `id = ?`。这对“多条件搜索”很方便，但对详情、删除、状态变更这类接口可能有风险：如果本来必须按 `id` 查，却因为 `id` 为空被忽略，就可能退化成更宽的查询。

这类场景要写成：

```java
@Eq(require = true)
Long id;
```

含义是：`id` 条件必须生效。因为 `@Eq` 默认要求值非空，所以当 `id == null` 时，框架会抛出异常，而不是悄悄忽略条件。

### 4.4 重点理解 `value` 属性

DAO 注解里的 `value` 是“目标字段/表达式”，不是当前 DTO 字段值。DTO 字段值来自字段本身，`value` 用来告诉框架条件、选择、更新、统计要落到实体的哪个字段上。

如果 `value` 为空，框架会尝试自动推断目标字段名。它不是简单地永远使用 DTO 字段名，而是按下面的顺序处理：

1. 注解上显式写了 `value`：直接使用 `value`。
2. 没有写 `value`，并且实体类里存在同名字段：使用 DTO 字段名。
3. 没有写 `value`，实体类里不存在同名字段：尝试按“注解名作为前缀”截取字段名。
4. 截取后的字段名在实体类里存在：使用截取后的字段名。
5. 仍然找不到：退回使用 DTO 字段名，后续如果实体字段不存在，就可能在构建或执行查询时暴露问题。

```java
@Eq
Long id;

@Contains
String name;

@Update
String remark;
```

上面三个注解等价于：

```java
@Eq("id")
Long id;

@Contains("name")
String name;

@Update("remark")
String remark;
```

所以 DTO 字段名和实体字段名一致时，`value` 可以不写。这个“自动取字段名”的规则是 Simple DAO 用起来比较轻的原因之一：常规等值、模糊、更新、排序、选择字段，不需要每个注解都重复写一遍字段名。

更实用的是“按注解名前缀自动截取”。例如实体字段是 `createTime`，DTO 里为了表达查询语义写成 `gteCreateTime`、`lteCreateTime`：

```java
@Gte
LocalDateTime gteCreateTime;

@Lte
LocalDateTime lteCreateTime;
```

框架会发现实体里没有 `gteCreateTime` / `lteCreateTime`，然后根据注解名 `Gte` / `Lte` 去掉字段名前缀，自动推断出目标字段 `createTime`。它大致等价于：

```java
@Gte("createTime")
LocalDateTime gteCreateTime;

@Lte("createTime")
LocalDateTime lteCreateTime;
```

这个规则也适用于其它按注解名命名的字段，例如：

```java
@Contains
String containsName;      // 推断为 name

@StartsWith
String startsWithCode;   // 推断为 code

@NotEq
String notEqState;       // 推断为 state
```

注意：自动截取只会按注解名截取，例如 `Gte`、`Lte`、`Contains`、`StartsWith`、`NotEq`。像 `beginTime`、`endTime`、`minScore`、`maxScore` 这种业务化字段名，不是注解名前缀，不能稳定自动推断目标字段，建议显式写 `value`。

最佳实践：生产代码里只要需要显式写字段名，优先使用生成的实体常量，例如 `E_Order.tenantId`、`E_Order.orgId`、`E_Order.name`，而不是直接写 `"tenantId"`、`"orgId"`、`"name"`。这样实体字段重命名时，编译器会立刻报错；如果写字符串，字段名写错或重构遗漏通常要到运行时才暴露。

最常见场景是 DTO 字段名和实体字段名不一致：

```java
@Gte("createTime")
LocalDateTime beginTime;

@Lte("createTime")
LocalDateTime endTime;
```

大致生成：

```sql
where createTime >= ?
  and createTime <= ?
```

如果不写 `value = "createTime"`，框架会按 DTO 字段名生成 `beginTime >= ?`、`endTime <= ?`，这通常不是你想要的。

多表查询时，`value` 经常和 `domain` 一起使用：

```java
@Contains(value = E_Group.name, domain = E_Group.ALIAS)
String groupName;

@Select(value = E_User.name, domain = E_User.ALIAS)
String userName;
```

含义是：

- `groupName` 这个 DTO 字段用于过滤分组表的 `name`。
- `userName` 这个 DTO 字段用于选择用户表的 `name`。
- 多表场景里，别名本身也尽量使用 `E_XXX.ALIAS`，例如 `E_Group.ALIAS`、`E_User.ALIAS`。这样后续统一调整别名时，`@TargetOption(alias = ...)`、`@JoinOption(alias = ...)`、`domain = ...`、`joinTargetAlias = ...` 能保持一致。

更新时也一样：

```java
@Update(value = "state")
String newState;
```

含义是把 `newState` 的字段值更新到实体的 `state` 字段。

统计时，`value` 表示要统计的字段：

```java
@Avg(value = "score", alias = "avgScore")
Integer avgScore;
```

大致生成：

```sql
avg(score) as avgScore
```

使用建议：

- DTO 字段名和实体字段名一致时，可以省略 `value`。
- DTO 字段名带业务语义，如 `beginTime`、`minScore`、`newState`、`groupKeyword`，要显式写 `value`。
- 多表查询要同时写 `domain`，避免字段名落到主表上。
- 不建议把复杂 SQL 都塞进 `value`，能用字段名和注解表达时优先用字段名。

### 4.5 `condition` 控制注解是否生效

多数注解都有 `condition` 属性，表达式返回 `true` 时才生效：

```java
@Eq(condition = "#_fieldVal != null")
Long id;

@Contains(condition = "#isNotEmpty(name)")
String name;
```

如果你给 `condition` 配合 `require = true`，就能表达“这个业务条件必须满足，否则拒绝执行”：

```java
@Eq(value = "tenantId", require = true, condition = "#isNotEmpty(tenantId)")
String tenantId;

@Eq(value = "id", require = true)
Long id;
```

适合详情、编辑、删除、状态变更等接口，尤其是多租户或组织隔离的数据。

更多内置变量、参数绑定变量、文本替换变量的完整说明，见第 6 章“动态变量与上下文变量”。这里先记住一个原则：`condition` 只是“是否生成该注解语句”；如果业务要求条件必须生成，要加 `require = true`。

### 4.6 防止数据泄露的必填条件写法

业务接口里最容易出事故的是“条件为空后被忽略”，比如用户传了空 `tenantId` 或空 `id`，最后查询范围变宽。推荐写一个明确的安全请求对象：

```java
@TargetOption(entityClass = Order.class, alias = "o", resultClass = OrderInfo.class)
@Data
@Accessors(chain = true)
public class QueryOrderDetailReq {

    @Eq(value = "tenantId", require = true)
    String tenantId;

    @Eq(value = "orgId", require = true)
    String orgId;

    @Eq(require = true)
    Long id;
}
```

服务层：

```java
public OrderInfo detail(QueryOrderDetailReq req) {
    return dao.findUnique(OrderInfo.class, req);
}
```

这个写法有三层保护：

1. `tenantId`、`orgId`、`id` 为空时，`require = true` 会让对应注解抛异常。
2. `findUnique` 要求结果最多一条，超过一条会抛异常。
3. DAO 默认安全模式会阻止无条件查询、更新、删除。

列表查询也可以要求至少满足一个安全范围条件，例如“租户必须有，组织可选”：

```java
@TargetOption(entityClass = Order.class, alias = "o", resultClass = OrderInfo.class)
@Data
@Accessors(chain = true)
public class QueryOrderListReq {

    Paging paging = new PagingQueryReq(1, 20);

    @Eq(value = "tenantId", require = true)
    String tenantId;

    @Eq("orgId")
    String orgId;

    @Contains("name")
    String keyword;
}
```

如果业务要求“组织条件也必须有”，就把 `orgId` 改成 `@Eq(value = "orgId", require = true)`。不要依赖控制器里手写 `if` 后再传 DTO，最好把必填语义放进 DTO 注解里，这样服务层、测试、Repository 复用时都能保持同一套保护。

如果项目已经生成了 `E_XXX` 字段常量，上面的字段名建议写成常量形式：

```java
@TargetOption(entityClass = Order.class, alias = E_Order.ALIAS, resultClass = OrderInfo.class)
@Data
@Accessors(chain = true)
public class QueryOrderListReq {

    Paging paging = new PagingQueryReq(1, 20);

    @Eq(value = E_Order.tenantId, require = true)
    String tenantId;

    @Eq(E_Order.orgId)
    String orgId;

    @Contains(E_Order.name)
    String keyword;
}
```

这里的收益不是少写几个字符，而是把“字段名是否存在”交给编译器检查。以后 `tenantId`、`orgId`、`name` 如果在实体里被重命名，`E_Order.xxx` 会编译失败，提醒你同步修改查询 DTO；字符串常量则很容易漏掉。

## 5. 基础 CRUD 例子

### 5.1 保存实体

```java
User user = new User()
        .setName("Echo")
        .setScore(95);

User saved = dao.save(user);
```

如果需要保存前检查唯一约束：

```java
User saved = dao.save(user, true);
```

### 5.2 按主键查询与删除

```java
User user = dao.find(User.class, userId);

boolean deleted = dao.deleteById(User.class, userId);
```

如果实体配置了逻辑删除，`DeleteDao` 会按实体规则处理逻辑删除。

### 5.3 用查询对象查询列表

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class QueryUserReq {

    @Contains
    String name;

    @In
    String[] state;
}
```

调用：

```java
List<User> users = dao.findByQueryObj(
        new QueryUserReq()
                .setName("Echo")
                .setState(new String[]{"A", "B"})
);
```

大致生成：

```sql
from User u
where u.name like ? and u.state in (?, ?)
```

### 5.4 查询单条或唯一记录

```java
User one = dao.findOneByQueryObj(new QueryUserReq().setName("Echo"));
```

`findOneByQueryObj` 的语义是“取一条”：

- 内部最多查询 1 条。
- 没有记录时返回 `null`，不会抛异常。
- 如果真实数据有多条，因为只取 1 条，它不会帮你发现“不唯一”的问题。

如果你要求“结果最多只能有一条”，可以用：

```java
User unique = dao.findUnique(new QueryUserReq().setName("Echo"));
```

`findUnique` 的内部逻辑是：

1. `SimpleDao.findUnique(...)` 会调用 `findOneByQueryObj(true, ...)`。
2. `JpaDaoImpl` 根据查询对象和结果类型创建 `SelectDao`。
3. `SelectDaoImpl.findOne(true, resultType)` 会把查询上限设置为 2 条。
4. 查询结果为 0 条时返回 `null`。
5. 查询结果为 1 条时返回该记录。
6. 查询结果超过 1 条时抛 `IncorrectResultSizeDataAccessException`。

所以 `findUnique` 适合账号、编号、业务唯一键、详情页按唯一条件查询等场景。它检查的是“返回结果不能超过一条”，但“不存在”不是异常。

和唯一更新对比：

| 方法 | 0 条结果/影响 | 1 条结果/影响 | 超过 1 条 |
| --- | --- | --- | --- |
| `findOneByQueryObj` | 返回 `null` | 返回 1 条 | 不检查真实是否多条，取到一条就返回 |
| `findUnique` | 返回 `null` | 返回 1 条 | 抛异常 |
| `singleUpdateByQueryObj` | 返回 `false` | 返回 `true` | 抛异常并回滚 |
| `uniqueUpdateByQueryObj` | 抛异常 | 正常返回 | 抛异常并回滚 |

如果业务语义是“可以不存在，但不能重复”，用 `findUnique`。如果业务语义是“必须存在且必须只更新一条”，用 `uniqueUpdateByQueryObj`。

### 5.5 最常用分页入口

分页最常用的入口是 `findPagingDataByQueryObj(...)`，返回默认的 `PagingData<T>`：

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = UserListItem.class)
@Data
@Accessors(chain = true)
public class QueryUserPageReq {

    Paging paging = new PagingQueryReq(1, 20);

    @Contains
    String name;
}
```

调用：

```java
PagingData<UserListItem> page = dao.findPagingDataByQueryObj(
        UserListItem.class,
        new QueryUserPageReq().setName("Echo")
);
```

也可以把 `Paging` 单独作为参数传入：

```java
QueryUserReq req = new QueryUserReq().setName("Echo");
Paging paging = new PagingQueryReq(1, 20);

PagingData<UserListItem> page = dao.findPagingDataByQueryObj(
        UserListItem.class,
        req,
        paging
);
```

分页对象单独传入、自定义分页返回对象、只查总数、不查结果集、`PageOption` 等完整用法见下一节。

### 5.6 分页查询专题：`Paging`、`PagingData`、`PageOption`

分页是业务接口里最高频的能力之一，可以分成三件事理解：

1. `Paging` 描述分页请求：第几页、每页多少条、是否查总数、是否查结果集。
2. `PagingData` 描述分页返回：总数、当前页、页大小、数据列表。
3. `PageOption` 用注解把现有请求类或响应类接入分页机制。

#### 5.6.1 `Paging`

`Paging` 是分页输入接口，表示“要查第几页、每页多少条、是否需要总数、是否需要结果集”：

```java
Paging paging = new PagingQueryReq(1, 20)
        .setRequireTotals(true)
        .setRequireResultList(true);

PagingData<UserListItem> page = dao.findPagingDataByQueryObj(
        UserListItem.class,
        new QueryUserReq().setName("Echo"),
        paging
);
```

常用实现是 `PagingQueryReq` 和 `SimplePaging`。`pageIndex` 从 1 开始，`pageSize` 或 `pageIndex` 为负数时表示不限制。`requireTotals = true` 时会额外执行总数查询；只做导出或只要列表时，可以不查总数。

如果查询对象不想持有分页字段，可以把 `Paging` 单独作为参数传入：

```java
QueryUserReq req = new QueryUserReq().setName("Echo");
Paging paging = new PagingQueryReq(1, 20).setRequireTotals(true);

PagingData<UserListItem> page = dao.findPagingDataByQueryObj(
        UserListItem.class,
        req,
        paging
);
```

这种写法很适合一个查询对象同时服务“分页列表”和“导出全部”：

```java
// 分页列表
PagingData<UserListItem> page = dao.findPagingDataByQueryObj(
        UserListItem.class,
        req,
        new PagingQueryReq(1, 20)
);

// 导出全部：不传 Paging，或传 pageSize = -1 的 Paging
List<UserListItem> all = dao.findByQueryObj(UserListItem.class, req);
```

#### 5.6.2 `PagingData`

`PagingData<T>` 是分页输出接口。默认实现是 `DefaultPagingData<T>`，包含：

- `totals`：总记录数，未查询总数时通常为 `-1`。
- `pageIndex`：当前页码。
- `pageSize`：分页大小。
- `items`：当前页结果列表。
- `hasMore()`：根据 `items.size()` 和 `pageSize` 判断是否可能还有下一页。

常见用法：

```java
PagingData<UserListItem> page = dao.findPagingDataByQueryObj(
        UserListItem.class,
        req,
        new PagingQueryReq(1, 20).setRequireTotals(true)
);

Long totals = page.getTotals();
List<UserListItem> items = page.getItems();
```

如果只想用自己的返回对象，也可以调用 `findPageByQueryObj(...)`。

有些接口只需要总数，不需要结果集，可以关闭结果列表：

```java
Paging onlyCount = new PagingQueryReq(1, 20)
        .setRequireTotals(true)
        .setRequireResultList(false);

PagingData<UserListItem> page = dao.findPagingDataByQueryObj(
        UserListItem.class,
        req,
        onlyCount
);

Long totals = page.getTotals();
List<UserListItem> items = page.getItems(); // 通常为 null
```

反过来，如果只要列表、不需要总数，把 `requireTotals` 设为 `false`，可以少执行一次 count 查询：

```java
Paging listOnly = new PagingQueryReq(1, 20)
        .setRequireTotals(false)
        .setRequireResultList(true);

PagingData<UserListItem> page = dao.findPagingDataByQueryObj(
        UserListItem.class,
        req,
        listOnly
);
```

#### 5.6.3 `PageOption`

`PageOption` 是分页字段注解。它主要用于两类场景：

1. 从请求对象中读取 `PageIndex`、`PageSize`。
2. 把分页查询结果写入自定义分页返回对象。

例如自定义分页返回对象：

```java
@Data
public class UserPageResp {

    @PageOption(PageOption.Type.RequireTotals)
    Long total;

    @PageOption(PageOption.Type.PageIndex)
    Integer pageNo;

    @PageOption(PageOption.Type.PageSize)
    Integer size;

    @PageOption(PageOption.Type.RequireResultList)
    List<UserListItem> rows;
}
```

调用：

```java
UserPageResp page = dao.findPageByQueryObj(
        UserListItem.class,
        UserPageResp.class,
        req,
        new PagingQueryReq(1, 20).setRequireTotals(true)
);
```

这里 `UserPageResp.class` 是分页结果持有对象。查询完成后，框架会把总数、页码、页大小、结果集写回对应字段。

如果请求对象本身不实现 `Paging`，也可以用 `PageOption` 标记页码和页大小：

```java
@Data
public class QueryUserReq {

    @PageOption(PageOption.Type.PageIndex)
    Integer pageNo = 1;

    @PageOption(PageOption.Type.PageSize)
    Integer size = 20;

    @Contains("name")
    String keyword;
}
```

不过更推荐直接传 `Paging` 对象，因为 `Paging` 可以明确表达是否查询总数、是否查询结果集。

#### 5.6.4 分页写法选择建议

常规业务列表，优先使用：

```java
dao.findPagingDataByQueryObj(UserListItem.class, req, new PagingQueryReq(1, 20));
```

如果项目已有统一响应类，优先用 `PageOption` 接入：

```java
UserPageResp page = dao.findPageByQueryObj(UserListItem.class, UserPageResp.class, req, paging);
```

如果只是临时限制查询条数，不需要分页包装，直接把 `Paging` 当普通查询参数传给 `findByQueryObj(...)`：

```java
List<UserListItem> rows = dao.findByQueryObj(
        UserListItem.class,
        req,
        new PagingQueryReq(1, 20)
);
```

如果是后台统计或导出，要非常明确是否需要总数、是否需要结果集，不要默认把 `requireTotals` 打开。大数据量接口里，一次额外的 count 可能比当前页查询更重。

### 5.7 常用辅助类：`EntityClassSupplier`、`ResultClassSupplier`

这些类适合在“查询对象本身不方便写死实体类或结果类”时，动态告诉 DAO 本次查询的目标实体和返回类型。

#### 5.7.1 `EntityClassSupplier` 和 `ResultClassSupplier`

```java
EntityClassSupplier entity = () -> User.class;
ResultClassSupplier result = () -> UserListItem.class;

List<UserListItem> users = dao.findByQueryObj(
        entity,
        result,
        new QueryUserReq().setName("Echo"),
        new PagingQueryReq(1, 20)
);
```

`EntityClassSupplier` 提供实体类。框架还会根据实体类名推导默认别名，例如 `UserInfo` 会推导出类似 `u_i` 的别名；复杂查询里仍建议在 `@TargetOption(alias = "...")` 中显式写别名。

`ResultClassSupplier` 提供结果类，效果类似调用：

```java
dao.findByQueryObj(UserListItem.class, req);
```

它的价值在于可以把结果类型作为普通查询参数传递，适合通用方法、组合查询、运行时决定返回 DTO 的场景。普通业务查询如果类型固定，优先使用 `@TargetOption(resultClass = ...)` 或方法参数里的 `UserListItem.class`，代码更直观。

### 5.8 DAO 动态性的核心：方法参数自动识别

Simple DAO 的动态性，很大一部分来自 DAO 方法参数的自动识别机制。

它不是为“查询 DTO、分页、结果类、实体类、回调扩展”分别设计一堆复杂重载，而是让很多方法统一接收 `Object... queryObjs`，再在内部识别每个参数的真实角色。这样同一个方法就可以同时支持：

- 普通查询 DTO
- 分页对象 `Paging`
- 分页返回对象 `PagingData` / `PageOption`
- 查询实体 `Class` / `EntityClassSupplier`
- 结果类型 `Class` / `ResultClassSupplier`
- 编程式扩展 `Consumer<SelectDao>`、`Consumer<UpdateDao>`、`Consumer<DeleteDao>`
- Map 条件和其它带 DAO 注解的对象

所以它的重点不是“参数可以随便传”，而是“参数可以按类型组合”。这也是注解式 DAO 和编程式 DAO 能混合使用的基础。

很多 `SimpleDao` 方法都使用 `Object... queryObjs`，例如：

```java
dao.findOneByQueryObj(req, paging, callback);
dao.findByQueryObj(UserListItem.class, User.class, req, paging, callback);
dao.updateByQueryObj(updateReq, updateCallback);
dao.deleteByQueryObj(deleteReq, deleteCallback);
```

这里的 `Object...` 不是只支持普通 DTO。源码会根据参数类型自动识别它们的角色，所以可以把分页对象、结果类型、实体类型、回调、普通查询 DTO 混在一起传入。

源码里的大致处理流程是：

1. 先展开参数：数组、集合会被递归展开，`null` 会被过滤。
2. 再过滤简单值：字符串、数字、枚举、注解、根对象类型等不会被当成查询对象直接解析。
3. 如果参数实现了 `EntityClassSupplier`，会尝试用它提供查询实体和默认别名。
4. 如果参数里有 JPA 实体 `Class`，会优先作为查询实体，并从后续字段解析列表中移除。
5. 如果参数里有 `QueryOption` 或带 `@TargetOption` 的对象，会读取实体类、别名、是否原生查询、连接配置、安全模式等目标信息。
6. 如果参数实现了 `ResultClassSupplier`，会参与结果类型识别。
7. 如果参数实现了 `Paging`，会调用 `page(paging)` 设置分页。
8. 如果参数是 `Consumer`，会把当前正在构建的 DAO 传进去执行。
9. 如果参数是 `Map`，会按 Map 条件解析。
10. 其它对象会按普通查询 DTO 解析字段注解，例如 `@Eq`、`@Select`、`@Update`、`@Sum`、`@OrderBy` 等。

参数顺序也有明确影响，尤其是混合多个 DTO 和 `Consumer` 时要注意：

- 数组、集合展开后会保持原来的顺序，后续 DTO、Map、`Consumer` 基本按这个顺序处理。
- `EntityClassSupplier` 会在没有有效查询实体时尝试提供实体类；如果前面已经通过 DAO 创建方法或目标配置确定了实体，它不会再覆盖。
- JPA 实体 `Class` 会先被识别为查询实体，并从后续字段解析列表中移除。建议只传一个实体类，避免读者和维护者误解。
- `QueryOption`、`@TargetOption` 会在实体还没有确定时生效；一旦实体已经确定，后面的目标配置不会再覆盖它。
- 结果类型如果通过方法参数显式传入，例如 `findByQueryObj(UserListItem.class, ...)`，优先级最高。
- 如果结果类型放在 `queryObjs` 里，框架会按参数顺序找第一个有效的 `ResultClassSupplier`、`QueryOption`、`@TargetOption` 或 `@ResultOption`。
- 普通 DTO 和 Map 会按顺序追加条件；单个 DTO 内部字段按父类字段优先、再到子类字段的顺序解析。
- `Consumer` 也按它在参数列表中的位置执行。通常建议把 `Consumer` 放在最后，让它在 DTO 注解条件之后补充 OR 分组、排序、分页、安全模式等配置。
- 普通查询中如果传入多个 `Paging`，后执行的分页设置可能覆盖前面的分页设置；`findPageByQueryObj(...)` 会优先取参数中第一个 `Paging`。业务代码里不要传多个分页对象。

结果类型也有单独的识别逻辑。调用方法里显式传入的 `resultType` 优先级最高：

```java
List<UserListItem> rows = dao.findByQueryObj(
        UserListItem.class,
        req,
        paging
);
```

如果没有显式传 `resultType`，框架会从参数里继续尝试识别：

- `ResultClassSupplier#get()` 返回的结果类。
- `QueryOption#getResultClass()` 返回的结果类。
- 查询 DTO 类上的 `@TargetOption(resultClass = ...)`。
- 查询 DTO 类上的 `@ResultOption(resultClass = ...)`。

所以这种写法也是可以的：

```java
ResultClassSupplier result = () -> UserListItem.class;

List<UserListItem> rows = dao.findByQueryObj(
        result,
        new QueryUserReq().setName("Echo"),
        new PagingQueryReq(1, 20)
);
```

分页查询还有一层特殊处理：`findPageByQueryObj(...)` 会优先从参数中找 `Paging`。如果没有找到 `Paging`，会尝试从请求对象字段上的 `@PageOption(PageIndex)`、`@PageOption(PageSize)` 读取页码和页大小，并创建默认分页对象。

这就是下面这个方法为什么能工作：

```java
public StatExternalWorkflowOperationReq.Result stat(
        StatExternalWorkflowOperationReq req,
        Paging paging) {

    Consumer<SelectDao<ExternalWorkflowOperation>> callback = dao -> dao
            .or()
                .isNull(ExternalWorkflowOperation::getEnable)
                .eq(ExternalWorkflowOperation::getEnable, true)
            .end()
            .setSafeModeMaxLimit(-1)
            .disableSafeMode();

    return simpleDao.findOneByQueryObj(req, paging, callback);
}
```

参数识别结果是：

- `req`：普通查询 DTO，解析 `@TargetOption`、查询条件、统计字段、结果类。
- `paging`：分页对象，设置 `pageIndex`、`pageSize`、是否查总数和结果集。
- `callback`：编程式扩展回调，拿到 `SelectDao` 后追加 OR 分组、安全模式等链式配置。

使用建议：

- 参数顺序通常不需要死记，但建议按“目标/结果类型、查询 DTO、分页、Consumer 回调”的顺序写，最容易读。
- 不要把裸字符串、数字直接当查询参数传给 `queryObjs`；它们会被当成简单值过滤掉。业务条件要放在 DTO 字段、Map、或 `Consumer` 回调里。
- 一个方法里可以传多个 DTO，框架会按顺序解析字段；但公共条件和安全条件最好放在明确的 DTO 上，方便审计。
- `Consumer` 会拿到当前操作类型对应的 DAO：查询是 `SelectDao`，更新是 `UpdateDao`，删除是 `DeleteDao`。

## 6. 动态变量与上下文变量

动态变量是 Simple DAO 里很重要的一类能力。它主要解决的是：**查询对象和结果对象分开解析时，结果对象无法直接访问查询对象上的字段**。

例如列表请求对象里有 `queryName`、`weekBegin` 这类控制参数，而真正的 `@Select`、`@Sum`、`@Case` 写在结果 DTO 上。结果 DTO 不是请求对象的内部运行实例，它不能通过普通 SpEL 直接读取请求对象字段。这时就需要 `@CtxVar` 先把请求对象字段导出到当前查询 DAO 的上下文中，后续解析结果对象时再从上下文里读取。

常见来源有三类：

- 查询 DTO 字段或 getter 上的 `@CtxVar`：把字段值或表达式结果导出为本次 DAO 构建过程的局部变量。
- DAO 链式 API 的 `setContext(...)`：在编程式查询中手动放入上下文变量。
- `DaoContext.globalContext` / `DaoContext.threadContext`：放入全局或线程级变量，适合框架级配置或请求级公共上下文。

这些变量可以被 `condition`、SpEL 表达式、`${...}` / `${:...}` 占位、CASE 统计等地方读取。

### 6.1 先区分三类变量使用位置

Simple DAO 里常见的“变量”有三种使用位置，它们语法相近，但含义不一样：

| 使用位置 | 常见语法 | 作用 |
| --- | --- | --- |
| `condition` | `#_fieldVal != null`、`#_isUpdate`、`#queryName` | 判断当前注解是否生效。返回 `true` 才生成对应语句。 |
| 语句文本替换 | `${tab}`、`F$:amount`、`E$:User` | 把上下文变量、字段名、表名替换到语句文本中。 |
| 参数绑定 | `${:weekBegin}`、`${:_val}` | 从上下文取值并作为查询参数绑定，不直接拼值到 SQL/JPQL 文本。 |

最容易混淆的是 `${name}` 和 `${:name}`：

- `${name}` 是文本替换，适合替换表名、字段片段、固定 SQL/JPQL 片段。
- `${:name}` 是参数变量，适合替换用户输入、时间边界、金额、状态等运行时值。

业务值优先用 `${:name}` 绑定参数，不要用 `${name}` 直接拼进去。

### 6.2 `condition` 内置变量怎么用

`condition` 是最常见的动态表达式位置。它只决定“当前注解是否生成语句”，不负责校验业务必须满足；如果业务要求必须生成，要配合 `require = true`。

常用内置变量：

| 变量 | 说明 |
| --- | --- |
| `_this` | 当前正在解析的 DTO 对象。 |
| `_name` / `_fieldName` | 当前字段名。 |
| `_val` / `_fieldVal` | 当前字段值。 |
| `_isSelect` / `_isQuery` | 当前是否查询 DAO。 |
| `_isUpdate` | 当前是否更新 DAO。 |
| `_isDelete` | 当前是否删除 DAO。 |
| `VALUE_NOT_EMPTY` | 当前字段值非空。 |
| `VALUE_EMPTY` | 当前字段值为空。 |

常用函数：

```java
#isEmpty(value)
#isNotEmpty(value)
```

判断当前字段自己的值：

```java
@In(value = "id", condition = "#isNotEmpty(#_fieldVal)")
Long[] ids;

@Between(value = "score", condition = "#_fieldVal != null && #_fieldVal.length == 2")
Integer[] scoreRange;
```

读取当前 DTO 的其它字段：

```java
@Ignore
Boolean includeDisabled = false;

@Eq(value = "enable", condition = "#includeDisabled != true")
Boolean enable = true;
```

同一个 DTO 复用在查询和更新里：

```java
@Eq(value = "state", condition = "#_isQuery")
String queryState;

@Update(value = "state", condition = "#_isUpdate")
String newState;
```

强制条件必须生效：

```java
@Eq(value = "tenantId", require = true, condition = "#isNotEmpty(tenantId)")
String tenantId;
```

这里 `condition` 不满足时，因为有 `require = true`，框架会抛异常，而不是悄悄忽略租户条件。

### 6.3 语句文本替换变量：`${...}`、`F$:...`、`E$:...`

语句文本替换适合替换“语句结构”，例如表名、字段名、固定片段。

链式 API 中设置上下文：

```java
long cnt = dao.selectFrom(User.class)
        .setContext(MapUtils.put("tab", (Object) User.class.getName()).build())
        .exists("select count(1) from ${tab}")
        .count();
```

字段名替换：

```java
@Sum(E_Order.F_amount)
Long amount;
```

`F$:amount` 会按当前查询模式转换字段名；原生查询时也会参与物理列名转换。复杂表达式中建议使用这种写法，而不是自己拼死字段名。

和普通字段名一样，`F$:` 表达式也尽量使用生成常量。生成的 `E_XXX` 通常同时提供两类字段常量：

```java
E_Order.amount      // 普通实体字段名：amount
E_Order.F_amount    // 可替换字段表达式：F$:amount
```

所以不要优先写裸字符串：

```java
@Sum("F$:amount")
Long amount;
```

更推荐写：

```java
@Sum(E_Order.F_amount)
Long amount;
```

如果当前项目只拿到了普通字段常量，也可以显式拼出 `F$:`：

```java
@Sum("F$:" + E_Order.amount)
Long amount;
```

同理，`F$:score`、`F$:amount`、`F$:createTime` 这类写法，优先使用 `E_User.F_score`、`E_Order.F_amount`、`E_Order.F_createTime`。这样字段重命名时，生成常量会跟着变化或编译失败，比在表达式字符串里手写字段名更安全。

表名替换：

```java
@Where(paramExpr = "exists (select 1 from E$:User u2 where u2.id = u.id)")
Boolean existsUser;
```

`E$:User` 会按当前查询模式转换实体名或表名。普通业务查询一般不需要直接使用，复杂片段、原生查询、子查询里才会用到。

#### 6.3.1 特殊占位常量：`C.BLANK_VALUE`、`C.ORIGIN_EXPR`、`C.FIELD_VALUE`

这三个常量不是普通业务值，而是给语句构建器看的特殊标记。它们建议放在“动态变量与上下文变量”章节理解，因为它们改变的是语句生成规则。

| 常量 | 值 | 主要用途 |
| --- | --- | --- |
| `C.BLANK_VALUE` | `#BLANK#` | 强制指定“空值”，常用于 `domain`、`alias`，表示不要自动补默认别名或不要生成结果别名。 |
| `C.ORIGIN_EXPR` | `$$` | 表示“原始表达式”，目前主要用于 `@Func`、`@Case`，把当前字段表达式放进函数或 CASE 中。 |
| `C.FIELD_VALUE` | `$$FIELD_VALUE` | 表示“使用字段值作为选择字段”，主要用于动态选择列，例如前端传本次要返回的字段列表。 |

`C.BLANK_VALUE`：强制不使用默认 `domain` 或 `alias`。

```java
@Select(value = E_Group.name, alias = C.BLANK_VALUE)
String name;

@Sum(value = E_User.score, domain = C.BLANK_VALUE)
Long scoreSum;
```

第一段表示选择 `name` 时不生成结果别名；第二段表示统计字段不自动加主表别名前缀。多表查询、普通字段查询通常不要随意用它；只有确实要去掉默认别名行为时再使用。

`C.ORIGIN_EXPR`：在函数或 CASE 里引用当前字段表达式。

```java
@Select(
        value = E_User.createTime,
        alias = "createMonth",
        fieldFuncs = @Func(
                value = "DATE_FORMAT",
                params = {C.ORIGIN_EXPR, "'%Y-%m'"}
        )
)
String createMonth;
```

这里 `C.ORIGIN_EXPR` 会被替换成当前字段表达式，也就是 `createTime` 经过别名、字段名转换后的结果。大致效果是：

```sql
DATE_FORMAT(u.createTime, '%Y-%m') as createMonth
```

`@Case` 里也会用到它。`@Case(column = C.ORIGIN_EXPR, ...)` 表示简单 CASE 使用当前字段表达式作为 `CASE xxx WHEN ...` 的 `xxx`；`column = ""` 则是搜索 CASE，生成 `CASE WHEN 条件 THEN ...`。

```java
@Select(
        value = E_Order.state,
        alias = "stateText",
        fieldCases = @Case(
                column = C.ORIGIN_EXPR,
                whenOptions = {
                        @Case.When(whenExpr = "'PAID'", thenExpr = "'已支付'"),
                        @Case.When(whenExpr = "'CLOSED'", thenExpr = "'已关闭'")
                },
                elseExpr = "'其它'"
        )
)
String stateText;
```

`C.FIELD_VALUE`：字段值就是要选择的列。

```java
@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, alias = E_Group.ALIAS,
        resultClass = GroupDynamicItem.class)
public class QueryGroupDynamicReq {

    @Select(value = C.FIELD_VALUE, alias = C.BLANK_VALUE)
    String[] columns = new String[]{E_Group.name, E_Group.category};

    String name;
    String category;
    Integer score;
}
```

这里 `columns` 不是结果字段本身，而是“本次要选择哪些字段”的参数。`C.FIELD_VALUE` 告诉框架使用字段值里的 `name`、`category` 去生成 select 列；`alias = C.BLANK_VALUE` 表示这些动态列不要统一映射成 `columns` 这个字段名。动态字段仍建议来自白名单或 `E_XXX.xxx` 常量，不要直接信任前端传来的任意字符串。

### 6.4 参数绑定变量：`${:...}`

`${:name}` 会从 DAO 上下文中取值，并作为参数绑定到 SQL/JPQL，而不是直接把值拼到语句文本中。

它适合用户输入、时间、金额、状态、ID 等运行时值：

```java
@Ignore
@CtxVar
Date weekBegin;

@Sum(fieldCases = @Case(
        column = "",
        whenOptions = @Case.When(
                whenExpr = "o.payTime >= ${:weekBegin}",
                thenExpr = "o.amount"
        ),
        elseExpr = "0"
))
Long paidAmountThisWeek;
```

常见内置值也可以作为参数绑定：

```java
@Update(value = "score", paramExpr = "${_name} + ${:_val}")
Integer scoreDelta;
```

这里 `${_name}` 是字段文本替换，表示当前字段名；`${:_val}` 是当前字段值的参数绑定。更新时会生成类似“原字段 + 参数值”的表达式。

### 6.5 更新里的动态变量和 `paramExpr`

更新场景里，动态变量常用于“字段值不是直接覆盖，而是参与表达式计算”。

增量更新：

```java
@Update(value = "score", paramExpr = "${_name} + ${:_val}")
Integer scoreDelta;
```

乐观锁版本号加一：

```java
@Update(value = "version", incrementMode = true, paramExpr = "1", condition = "")
Integer version;
```

带条件的更新片段：

```java
@Update(value = "state", condition = "#_isUpdate")
String newState;
```

更新里要特别注意：

- `paramExpr` 是右侧表达式，适合字段计算、函数调用、JSON 更新等。
- 用户输入值要用 `${:_val}` 这类参数绑定形式。
- 如果只是普通赋值，直接 `@Update("fieldName")` 更清楚。
- 更新范围仍然要靠 `@Eq(require = true)`、租户条件、主键条件等保护。

### 6.6 `@CtxVar`：跨查询对象和结果对象传递变量

`@CtxVar` 最常见的用法是：请求 DTO 上有一个控制字段，结果 DTO 上的注解要读取这个控制字段。

```java
@TargetOption(entityClass = Group.class, alias = "g",
        resultClass = QueryGroupInfoReq.Info.class)
@Data
@Accessors(chain = true)
public class QueryGroupInfoReq {

    @Ignore
    @CtxVar
    Boolean queryName = true;

    @Contains("name")
    String keyword;

    @Data
    public static class Info {

        @Select
        String id;

        @Select(condition = "#queryName")
        String name;

        @Select(value = "parent.name")
        String parentName;
    }
}
```

调用：

```java
List<QueryGroupInfoReq.Info> withName = dao.findByQueryObj(
        new QueryGroupInfoReq().setQueryName(true)
);

List<QueryGroupInfoReq.Info> withoutName = dao.findByQueryObj(
        new QueryGroupInfoReq().setQueryName(false)
);
```

这里 `queryName` 在查询请求对象上，但 `@Select(condition = "#queryName")` 在结果对象 `Info` 上。如果不通过 `@CtxVar` 导出，结果对象解析时无法直接访问请求对象里的 `queryName`。

`@CtxVar` 的作用就是把 `queryName` 注入到当前 `SelectDao` 的局部上下文里。后续解析 `Info` 的 `@Select` 注解时，`condition = "#queryName"` 才能拿到这个变量。

所以这个字段通常也要加 `@Ignore`：它是控制变量，不是数据库查询条件。

### 6.7 `@CtxVar` 的常用属性

`@CtxVar` 可以标在字段上，也可以标在 getter 方法上；框架会先处理 getter 方法，再处理字段。它的常用属性如下：

- `varName`：导出的变量名；不写时默认使用字段名或属性名。
- `value`：导出的变量值表达式；不写时默认使用当前字段值或 getter 返回值。
- `condition`：满足条件时才导出变量。
- `forceOverride`：上下文已有同名变量时是否覆盖，默认覆盖。

例如把多个字段组合成一个变量：

```java
@Ignore
Boolean queryGroupName;

@Ignore
Boolean queryRoleName;

@Ignore
@CtxVar(
        varName = "queryExt",
        value = "#queryGroupName == true || #queryRoleName == true"
)
Boolean queryExt;
```

后续注解就可以使用：

```java
@Select(value = "group.name", condition = "#queryExt")
String groupName;
```

如果只想在值不为空时导出变量：

```java
@Ignore
@CtxVar(varName = "keyword", condition = "#isNotEmpty(#_fieldVal)")
String keyword;
```

### 6.8 使用场景一：动态控制结果对象上的选择字段

这个场景适合“同一个接口按开关决定是否返回扩展字段”。重点是：开关在请求对象上，选择字段在结果对象上。

```java
@TargetOption(entityClass = User.class, alias = "u",
        resultClass = QueryUserInfoReq.Result.class)
@Data
@Accessors(chain = true)
public class QueryUserInfoReq {

    @Ignore
    @CtxVar
    Boolean queryGroupName = false;

    @Contains("name")
    String keyword;

    @Data
    public static class Result {

        @Select
        String name;

        @Select(value = "group.name", condition = "#queryGroupName == true")
        String groupName;
    }
}
```

基础查询：

```java
List<QueryUserInfoReq.Result> list = dao.findByQueryObj(
        new QueryUserInfoReq().setKeyword("Echo")
);
```

带扩展字段：

```java
List<QueryUserInfoReq.Result> list = dao.findByQueryObj(
        new QueryUserInfoReq()
                .setKeyword("Echo")
                .setQueryGroupName(true)
);
```

### 6.9 使用场景二：结果对象 CASE 统计中的动态参数

统计经常需要“本周、本月、上月”这类运行时边界。通常这些时间边界在请求对象上，而 CASE 统计字段在结果对象上。不要把日期直接拼进 SQL，可以通过 `@CtxVar` 暴露为 DAO 上下文变量，再用 `${:varName}` 作为绑定参数。

```java
@TargetOption(entityClass = Order.class, alias = "o",
        resultClass = StatOrderReq.Result.class)
@Data
@Accessors(chain = true)
public class StatOrderReq {

    @Ignore
    @CtxVar
    Date weekBegin;

    @Ignore
    @CtxVar
    Date monthBegin;

    @Data
    public static class Result {

        @Sum(fieldCases = @Case(
                column = "",
                whenOptions = @Case.When(
                        whenExpr = "o.payTime >= ${:weekBegin}",
                        thenExpr = "o.amount"
                ),
                elseExpr = "0"
        ))
        Long paidAmountThisWeek;

        @Sum(fieldCases = @Case(
                column = "",
                whenOptions = @Case.When(
                        whenExpr = "o.payTime >= ${:monthBegin}",
                        thenExpr = "o.amount"
                ),
                elseExpr = "0"
        ))
        Long paidAmountThisMonth;
    }
}
```

调用：

```java
StatOrderReq req = new StatOrderReq()
        .setWeekBegin(weekBegin)
        .setMonthBegin(monthBegin);

StatOrderReq.Result stat = dao.findOneByQueryObj(req);
```

`weekBegin`、`monthBegin` 在请求对象上，不参与普通 where 条件；`Result` 上的 CASE 表达式通过 DAO 上下文读取它们，并作为绑定参数使用。

这个模式已在示例测试 `DaoExamplesTest.testCtxVarCaseStat` 中验证：请求 DTO 用 `@CtxVar` 暴露时间边界，结果 DTO 的 `@Sum(fieldCases = @Case(...))` 在 `whenExpr` 中使用 `${:beginTime}` 这类绑定参数，统计结果可以正确映射回结果对象字段。

### 6.10 使用场景三：链式 API 里手动设置上下文

编程式 DAO 可以直接用 `setContext(...)` 放变量。适合少量临时查询、存在子查询片段、或要在语句模板里替换变量的场景。

```java
long cnt = dao.selectFrom(User.class)
        .setContext(MapUtils.put("tab", (Object) User.class.getName()).build())
        .exists("select count(1) from ${tab}")
        .count();
```

这里 `tab` 来自当前 DAO 的局部上下文。`setContext(...)` 只影响当前 DAO 构建过程，不适合存放跨请求共享状态。

### 6.11 使用场景四：请求级或全局上下文

如果一些变量是请求级公共信息，可以放在线程上下文；如果是框架级默认配置，可以放在全局上下文：

```java
DaoContext.threadContext.put("orgId", 5L);
DaoContext.globalContext.put("DATE_FORMAT", "yyyy/MM/dd");
```

这些上下文会参与表达式求值和占位替换。使用时要注意：

- 线程上下文适合请求级变量，用完后要按项目规范清理，避免线程复用导致串值。
- 全局上下文适合真正全局稳定的配置，不要放用户、租户、组织这类请求级数据。
- 业务查询更推荐把关键安全条件放在 DTO 字段和 `require = true` 上，动态变量只做辅助。

### 6.12 动态变量使用建议

- 查询对象和结果对象分开时，结果对象需要读取查询对象参数：优先用 `@CtxVar`。
- 控制结果字段是否查询、是否统计：优先用 `@CtxVar + condition`。
- 给结果对象里的 CASE、子查询、表达式传运行时参数：优先用 `@CtxVar` 或 `setContext(...)`，并使用参数绑定形式。
- 多租户、组织隔离这类安全条件：不要只依赖动态变量，最好在 DTO 字段上用 `@Eq(require = true)` 明确表达。
- 标了 `@CtxVar` 的控制字段如果不想生成查询条件，要同时加 `@Ignore`。
- 变量名要业务化，例如 `queryGroupName`、`weekBegin`、`monthBegin`，不要用 `flag1`、`v1` 这类难维护名字。

## 7. 查询对象实用例子

### 7.1 等值、模糊、范围、集合组合查询

这个例子适合“列表筛选页”：

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = UserListItem.class)
@Data
@Accessors(chain = true)
public class QueryUserListReq {

    Paging paging = new PagingQueryReq(1, 20);

    @Eq
    Long id;

    @Contains
    String name;

    @In
    String[] state;

    @Gte("score")
    Integer minScore;

    @Lte("score")
    Integer maxScore;

    @OrderBy(type = OrderBy.Type.Desc)
    LocalDateTime createTime;
}
```

调用：

```java
QueryUserListReq req = new QueryUserListReq()
        .setName("Li")
        .setState(new String[]{"A", "B"})
        .setMinScore(60)
        .setMaxScore(100);

PagingData<UserListItem> page = dao.findPagingDataByQueryObj(UserListItem.class, req);
```

大致生成：

```sql
select ...
from User u
where u.name like ?
  and u.state in (?, ?)
  and u.score >= ?
  and u.score <= ?
order by u.createTime desc
```

说明：

- `@Gte("score")` 表示 DTO 字段名是 `minScore`，但目标实体字段是 `score`。
- 空字段默认不生成条件，所以同一个 DTO 可以同时服务多种筛选组合。

### 7.2 区间查询：`@Between`

`@Between` 适合前端一次传入范围值：

```java
@Between("score")
Integer[] scoreRange = new Integer[]{60, 100};
```

生成：

```sql
score between ? and ?
```

字符串也可以切分为多个参数：

```java
@Between(value = "createTime", paramDelimiter = "-", patterns = "yyyyMMdd")
String createTimeRange = "20260101-20260131";
```

适合搜索表单里传 `20260101-20260131` 这种轻量格式。

### 7.3 逻辑嵌套：`@AND`、`@OR`、`@END`

当需求是“状态必须匹配，并且姓名或描述命中关键字”：

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
public class QueryUserKeywordReq {

    @Eq
    String state;

    @AND(condition = "#isNotEmpty(keyword)")
    Boolean keywordBlock = true;

    @Contains("name")
    @OR
    String keyword;

    @Contains("description")
    @END
    String keywordInDescription;
}
```

更常见的写法是让两个字段使用同一个关键字值：

```java
QueryUserKeywordReq req = new QueryUserKeywordReq();
req.setState("A");
req.setKeyword("Echo");
req.setKeywordInDescription("Echo");
```

大致生成：

```sql
where state = ?
  and (name like ? or description like ?)
```

`@AND`、`@OR` 是“开启一个逻辑分组”，`@END` 是“关闭当前逻辑分组”。默认情况下，`@AND`、`@OR` 开启后会一直影响后面的字段，直到遇到 `@END`。

`autoClose` 用来处理更轻量的场景：逻辑分组只包住当前字段上的条件，字段处理完以后自动闭合，不需要再额外写一个 `@END` 字段或在后续字段上写 `@END`。

```java
@TargetOption(entityClass = Order.class, alias = "o")
@Data
public class QueryTenantOrderReq {

    @OR(autoClose = true)
    @Eq("tenantId")
    @IsNull(condition = "#_this.includePublicData")
    String tenantId;

    @Ignore
    boolean includePublicData;
}
```

上面的 `tenantId` 字段会生成类似：

```sql
(tenantId = ? or tenantId is null)
```

这里 `@OR(autoClose = true)` 的意思是：在当前字段开始一个 `OR` 分组，当前字段上的 `@Eq`、`@IsNull` 都放进这个分组，字段处理完成后自动关闭。它适合“同一个字段上挂了多个条件注解，并且这些条件之间要用 OR/AND 组合”的情况。

如果分组要跨多个字段，就不要用 `autoClose`，而是用显式 `@END`：

```java
@AND
Boolean keywordBlock = true;

@Contains("name")
@OR
String keyword;

@Contains("description")
@END
String keywordInDescription;
```

另外，`@END` 有一个 `containCurrentField` 属性，默认是 `true`，表示“当前字段也包含在要关闭的分组里”。少数情况下，如果当前字段只是用来关闭前面的分组，不希望它自己进入该分组，可以写：

```java
@END(containCurrentField = false)
String afterGroupField;
```

简单记忆：

- 同一个字段上的多个条件要临时分组：用 `@OR(autoClose = true)` 或 `@AND(autoClose = true)`。
- 多个字段共同组成一个分组：用 `@AND` / `@OR` 开始，用 `@END` 结束。
- `@END` 默认包含当前字段；不想包含当前字段时，使用 `containCurrentField = false`。

### 7.4 选择部分列并映射到结果 DTO

结果对象：

```java
@Data
public class UserListItem {

    String name;

    Integer score;

    String groupName;
}
```

查询对象：

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = UserListItem.class)
@Data
public class QueryUserSelectReq {

    @Select
    String name;

    @Select
    Integer score;

    @Select(value = "group.name")
    String groupName;

    @Contains("name")
    String keyword;
}
```

调用：

```java
List<UserListItem> list = dao.findByQueryObj(UserListItem.class, new QueryUserSelectReq());
```

说明：

- `@Select` 标识要返回的列。
- `resultClass` 或调用方法里的 `UserListItem.class` 决定结果映射类型。
- 查询条件字段和选择字段可以在同一个 DTO 中，也可以拆成多个查询对象传入。

#### 7.4.1 Hibernate 7 下的 DTO 字段映射优化

在 JPA 查询、返回 DTO、并且每个选择列都有明确映射关系时，Simple DAO 会优先让 Hibernate 以 `Map` 形式返回投影结果，再把 `Map` 拷贝到 DTO。这样可以减少对查询语句的额外解析，也更贴近 Hibernate 7 的投影能力。

典型写法如下：

```java
@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, alias = "g",
        resultClass = GroupListItem.class,
        maxResults = 10)
public class QueryGroupListReq {

    @Select("name")
    String name;

    @Select("category")
    String category;

    @Select("score")
    Integer score;
}

@Data
public class GroupListItem {
    String name;
    String category;
    Integer score;
}

List<GroupListItem> rows = dao.findByQueryObj(GroupListItem.class, new QueryGroupListReq());
```

这个场景里，`name`、`category`、`score` 都能按 DTO 字段名稳定映射。

#### 7.4.2 无别名字段的映射

有些场景会显式去掉别名，例如希望生成的选择片段更接近原始字段：

```java
@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, alias = "g",
        resultClass = GroupListItem.class,
        maxResults = 10)
public class QueryGroupNoAliasReq {

    @Select(value = "name", alias = C.BLANK_VALUE)
    String name;

    @Select(value = "category", alias = C.BLANK_VALUE)
    String category;

    @Select(value = "score", alias = C.BLANK_VALUE)
    Integer score;
}
```

Hibernate 对没有别名的投影，可能会返回 `"0"`、`"1"` 这类位置 key。Simple DAO 会在目标类型是 DTO 时，按 `@Select` 的顺序把这些位置 key 重新映射到 DTO 字段上：

- 第 1 个选择列 -> `name`
- 第 2 个选择列 -> `category`
- 第 3 个选择列 -> `score`

所以无别名查询仍然可以稳定返回 `GroupListItem`。

#### 7.4.3 表达式字段建议显式写别名

表达式字段最好显式写 `alias`，让结果字段含义清楚：

```java
@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, alias = "g",
        resultClass = GroupScoreItem.class)
public class QueryGroupScoreReq {

    @Select("name")
    String name;

    @Select(value = "score + 1", alias = "scorePlusOne")
    Integer scorePlusOne;
}

@Data
public class GroupScoreItem {
    String name;
    Integer scorePlusOne;
}
```

这里 `score + 1` 会映射到 `scorePlusOne`，比依赖表达式文本自动推断更清楚，也更适合后续维护。

#### 7.4.4 动态选择字段会自动走兼容路径

动态选择字段很常见，例如前端决定本次只返回哪些字段：

```java
@Data
@Accessors(chain = true)
@TargetOption(entityClass = Group.class, alias = E_Group.ALIAS,
        resultClass = GroupDynamicItem.class)
public class QueryGroupDynamicReq {

    @Select(value = C.FIELD_VALUE, alias = C.BLANK_VALUE)
    String[] columns = new String[]{E_Group.name, E_Group.category};

    String name;
    String category;
    Integer score;
}
```

这类查询的选择列数量和字段映射关系是运行时决定的。Simple DAO 不会强行启用 Hibernate Map 投影优化，而是走原来的兼容映射路径，避免把未选择的列误填到 DTO 上。

这里 `C.FIELD_VALUE` 表示“选择字段来自当前字段值”，`C.BLANK_VALUE` 表示不要把这些动态字段统一映射成 `columns` 这个别名。完整语义见第 6.3.1 节“特殊占位常量”。

上面的例子里只选择了 `name` 和 `category`，`score` 没有被选择，返回 DTO 的 `score` 应保持为空。

#### 7.4.5 数字别名和 Map 返回值

DTO 字段名本身不能是纯数字，所以 `"0"`、`"1"` 这类 key 只会在 DTO 映射时作为“无别名投影的位置兜底”。如果目标类型就是 `Map`，Simple DAO 不会把数字 key 改成 DTO 字段名。

例如：

```java
List<Map> rows = dao.selectFrom("jpa_dao_test_Group", "g")
        .selectByStatement(true, "g.name AS \"0\"")
        .limit(0, 1)
        .find(Map.class);
```

这个查询明确要求返回 `Map`。返回结果会保留数据库/Hibernate 给出的数字别名 key，例如 `"0"` 或带引号的别名形式，而不会被映射成 `name`。

### 7.5 动态字段选择

有时候是否返回某个字段由请求参数决定。推荐写法是：控制参数放在查询对象上，用 `@CtxVar` 导出；选择字段放在结果对象上，通过 `condition` 读取变量。完整原理见第 6 章“动态变量与上下文变量”。

```java
@TargetOption(entityClass = Group.class, alias = "g",
        resultClass = QueryGroupInfoReq.Info.class)
@Data
@Accessors(chain = true)
public class QueryGroupInfoReq {

    @Ignore
    @CtxVar
    boolean queryParentName;

    @Data
    public static class Info {

        @Select
        String name;

        @Select(value = "parent.name", condition = "#queryParentName")
        String parentName;
    }
}
```

调用：

```java
List<QueryGroupInfoReq.Info> basic = dao.findByQueryObj(new QueryGroupInfoReq());

List<QueryGroupInfoReq.Info> withParent = dao.findByQueryObj(
        new QueryGroupInfoReq().setQueryParentName(true)
);
```

这里 `queryParentName` 在查询对象上，`parentName` 的 `@Select` 在结果对象上。`@CtxVar` 会把控制字段注入当前 DAO 上下文，让结果对象解析时能够读取它。

## 8. 链式 API 例子

链式 API 是补充能力，不是默认推荐入口。普通业务接口尽量先定义查询/更新 DTO，并通过注解表达条件、更新字段、统计字段和安全约束；只有条件临时性强、组合逻辑很动态、服务内部少量扩展、后台任务或调试场景，才建议直接使用 `SelectDao`、`UpdateDao`、`DeleteDao`。

如果确实不想专门定义 DTO，可以用 `SelectDao`：

```java
List<User> users = dao.selectFrom(User.class, "u")
        .select("id", "name", "score")
        .where("u.score >= ?", 60)
        .where("u.name like ?", "%Echo%")
        .orderBy(OrderBy.Type.Desc, "u.createTime")
        .page(1, 20)
        .find(User.class);
```

统计也可以链式写：

```java
List<Object> rows = dao.selectFrom(User.class, "u")
        .count("1", "userCnt")
        .avg("u.score", "avgScore")
        .sum("u.score", "sumScore")
        .groupByAsAnno("u.state")
        .find();
```

使用建议：

- 可复用的业务查询优先写 DTO，便于复用、校验、生成接口文档。
- 临时后台任务、调试、少量动态条件可以用链式 API。
- 链式 API 里也尽量使用 `E_XXX.xxx` 字段常量，不要散落字符串字段名。
- 复杂原生 SQL 不要一开始就手写，先判断 DTO 注解或链式 API 是否已经能表达。

### 8.1 原生查询和 JPA 查询

Simple DAO 同时支持 JPA/JPQL 查询和原生 SQL 查询。默认使用 JPA 查询；只有显式开启 `nativeQL = true`、调用 `selectByNative(...)`，或用表名字符串创建 DAO 时，才进入原生查询模式。

这里要特别强调：**原生查询就是原 SQL 查询**。它最终交给数据库执行的是 SQL，而不是 JPQL/HQL；Hibernate/JPA 不会像处理 JPQL 那样，把实体类名翻译成表名、把实体属性名翻译成字段名，也不会替你适配数据库函数。数据库看到什么 SQL，就按什么 SQL 执行。

因此，两种模式最核心的差别不是调用方法名字，而是**表名和字段名的书写规则**：

- JPA/JPQL 查询面向实体模型，写的是实体类名、实体属性名，例如 `User`、`createTime`。
- 原生 SQL 查询面向数据库模型，写的是物理表名、物理列名，例如 `sys_user`、`create_time`。
- 如果 Simple DAO 根据实体元数据帮你生成原生 SQL，它可以推导一部分表名和列名；但你自己写在 `where(...)`、`select(...)`、`orderBy(...)` 或原生片段里的内容，不会再被 Hibernate/JPA 当成 JPQL 翻译。

常见入口：

```java
// JPA/JPQL 查询：from 使用实体类名，字段使用实体属性名
dao.selectFrom(User.class, "u")
        .where("u.createTime >= ?", beginTime)
        .find(User.class);

// 原生 SQL 查询：最终执行原 SQL，手写片段要按数据库表名、字段名理解
dao.selectByNative(User.class, "u")
        .where("u.create_time >= ?", beginTime)
        .find(User.class);

// 传表名字符串时，默认就是原生查询
dao.selectFrom("sys_user", "u")
        .where("u.create_time >= ?", beginTime)
        .find();
```

DTO 注解里通过 `@TargetOption(nativeQL = true)` 切换：

```java
@TargetOption(nativeQL = true, entityClass = User.class, alias = "u", resultClass = UserInfo.class)
@Data
public class NativeQueryUserReq {

    @Select
    String name;

    @Contains("name")
    String keyword;
}
```

两种模式的核心差异：

| 对比项 | JPA/JPQL 查询 | 原生 SQL 查询 |
| --- | --- | --- |
| 表达对象 | JPA 实体类和实体属性。 | 数据库物理表和物理列。 |
| 默认入口 | `selectFrom(User.class)`、`@TargetOption(nativeQL = false)`。 | `selectByNative(User.class)`、`selectFrom("table")`、`@TargetOption(nativeQL = true)`。 |
| From 语句 | 使用实体类名，例如 `com.xxx.User u`。 | 使用实体对应物理表名，例如 `sys_user u`。 |
| 字段名 | 使用实体字段，如 `createTime`，由 JPA/Hibernate 翻译。 | 使用数据库列名，如 `create_time`；手写 SQL 片段不会再被 JPA/Hibernate 翻译。 |
| Join Fetch | 支持 JPA 的 `joinFetch`，适合处理懒加载。 | 不适合使用 JPA `joinFetch`。 |
| 数据库函数 | 受 JPQL/HQL 和方言支持限制。 | 直接使用数据库原生函数，Hibernate/JPA 不负责改写，可移植性较弱。 |
| 结果映射 | 更适合实体和 DTO 转换。 | 返回实体时依赖 JPA 原生查询映射；复杂列建议映射 DTO 或 Object/Map 后再转换。 |

使用建议：

- 普通业务查询优先用 JPA/JPQL 模式，字段名跟实体保持一致，重构更安全。
- 需要数据库专用函数、JSON 函数、复杂 SQL 或性能调优时，再用原生查询。
- 原生查询里要特别注意字段名、表名和数据库方言；把它当成“直接写给数据库看的 SQL”，不要期待 Hibernate/JPA 再替你翻译。
- 多表查询时无论哪种模式，都建议显式写 `alias`、`domain`、`value`，避免字段落错表。

## 9. `Consumer` 扩展 DAO：混合注解和编程式查询

`Consumer` 扩展 DAO 的核心价值是：**把稳定的业务规则写在注解 DTO 上，把临时变化、复杂分支、少量链式条件写在编程式回调里**。

也就是说，它不是要替代注解查询，也不是要求所有条件都改成链式 API，而是让两种方式混合使用：

- 注解 DTO 负责稳定条件：租户、组织、主键、状态、统计字段、必填条件、结果类型。
- 编程式 `Consumer` 负责临时扩展：OR 分组、动态排序、额外分页、安全模式、JSON 条件、手写条件片段。
- 同一个 DTO 可以被多个服务方法复用，每个方法只通过 `Consumer` 补充自己的差异。

`SimpleDao` 的 `findByQueryObj(...)`、`findOneByQueryObj(...)`、`updateByQueryObj(...)`、`deleteByQueryObj(...)` 都可以接收 `Consumer` 作为额外参数。框架解析查询对象时，如果发现参数是 `Consumer`，会把当前正在构建的 DAO 传进去执行。

### 9.1 混合注解和编程式查询 `SelectDao`

下面这个例子和业务统计接口很接近：请求对象负责统计字段和基础过滤，`Paging` 单独传入，`Consumer<SelectDao<...>>` 追加额外条件。

```java
public StatExternalWorkflowOperationReq.Result stat(
        StatExternalWorkflowOperationReq req,
        Paging paging) {

    Consumer<SelectDao<ExternalWorkflowOperation>> callback = dao -> {
        dao
                // 回调中可以继续构造更多查询条件
                .or()
                    .isNull(ExternalWorkflowOperation::getEnable)
                    .eq(ExternalWorkflowOperation::getEnable, true)
                .end()

                // 统计场景可能需要放开默认 limit，务必确认前面的条件足够安全
                .setSafeModeMaxLimit(-1)
                .disableSafeMode();
    };

    return simpleDao.findOneByQueryObj(req, paging, callback);
}
```

这个调用顺序可以理解为：

1. `req` 先提供 `@TargetOption`、`@Eq`、`@Gte`、`@Sum`、`@Count` 等注解条件。
2. `paging` 提供分页或限制参数。
3. `callback` 拿到正在构建的 `SelectDao`，继续追加链式条件。
4. `findOneByQueryObj(...)` 执行查询，并按 `req` 上的 `resultClass` 或方法参数映射结果。

再比如只在参数存在时追加排序和查询条件：

```java
Consumer<SelectDao<User>> callback = dao -> dao
        .eq(Boolean.TRUE.equals(req.getOnlyEnabled()), User::getEnable, true)
        .where(req.getMinScore() != null, "score >= ?", req.getMinScore())
        .orderBy(OrderBy.Type.Desc, "createTime");

List<UserListItem> users = simpleDao.findByQueryObj(
        UserListItem.class,
        req,
        new PagingQueryReq(1, 20),
        callback
);
```

这里 `req` 仍然是主角：它决定查询目标、稳定条件、返回 DTO。`callback` 只是给本次调用补充“额外条件”。这种写法比把所有条件都写成字符串更容易维护，也比为每个小差异都新建一个 DTO 更轻。

### 9.2 混合注解和编程式更新 `UpdateDao`

更新时，`Consumer<UpdateDao<T>>` 可以追加 `where` 条件，也可以补充 `set(...)`、`setByStatement(...)`、JSON 更新等链式更新内容。

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class UpdateUserStateReq {

    @Eq(require = true)
    Long id;

    @Update("state")
    String newState;
}
```

服务层：

```java
public boolean updateState(UpdateUserStateReq req, String tenantId) {

    Consumer<UpdateDao<User>> callback = dao -> dao
            // 追加租户保护条件
            .eq("tenantId", tenantId)

            // 补充通用更新时间
            .set("lastUpdateTime", new Date());

    return simpleDao.singleUpdateByQueryObj(req, callback);
}
```

如果更新 JSON 字段，也可以把 JSON 链式方法放在回调里：

```java
Consumer<UpdateDao<User>> callback = dao -> dao
        .jsonSet(User::getExtInfo, "$.profile.nickName", req.getNickName())
        .jsonArrayAppend(User::getRoleList, "$", "R_MEMBER")
        .eq(User::getId, req.getId());

int rows = simpleDao.updateByQueryObj(User.class, callback);
```

建议更新类回调仍然保留必要的 `@Eq(require = true)` 或显式 `eq(...)` 条件。`Consumer` 很灵活，但也更容易绕开 DTO 上的保护语义；凡是会影响数据范围的条件，都要明确写出来。

### 9.3 混合注解和编程式删除 `DeleteDao`

删除时，`Consumer<DeleteDao<T>>` 主要用于追加删除范围和安全保护条件：

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class DeleteDisabledUserReq {

    @Eq(value = "tenantId", require = true)
    String tenantId;

    @Eq("enable")
    Boolean enable = false;
}
```

调用：

```java
public int deleteDisabled(DeleteDisabledUserReq req, Date beforeTime) {

    Consumer<DeleteDao<User>> callback = dao -> dao
            .where("lastLoginTime < ?", beforeTime)
            .limit(0, 100);

    return simpleDao.deleteByQueryObj(req, callback);
}
```

删除回调尤其要谨慎。推荐至少满足一个强约束条件，例如 `tenantId`、`orgId`、主键、状态、时间范围；如果是单条删除，优先使用 `singleDelete()` / `uniqueDelete()` 或对应的 DAO 方法，避免条件意外放宽。

### 9.4 使用建议

- 可复用、可审计的业务规则优先写在 DTO 注解上。
- 方法级临时差异再放到 `Consumer`，不要为了一个排序或一个 OR 条件复制一整个 DTO。
- `Consumer` 的泛型要和操作类型匹配：查询用 `Consumer<SelectDao<T>>`，更新用 `Consumer<UpdateDao<T>>`，删除用 `Consumer<DeleteDao<T>>`。
- 如果回调里调用 `disableSafeMode()`，要在同一段代码里清楚地写出保护条件。公共组件里不要让安全模式禁用变成默认习惯。
- 当 `Consumer` 里出现很多手写 SQL 片段时，说明这个查询可能已经超出“补充差异”的范围，要考虑拆成专门的查询对象或链式 DAO 方法。

## 10. 更新例子

### 10.1 普通更新

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class UpdateUserNameReq {

    @Eq(require = true)
    Long id;

    @Update
    String name;
}
```

调用：

```java
int rows = dao.updateByQueryObj(
        new UpdateUserNameReq()
                .setId(1L)
                .setName("New Name")
);
```

大致生成：

```sql
update User
set name = ?
where id = ?
```

说明：

- `@Update` 表示更新列。
- `@Eq(require = true)` 表示必须有这个条件，适合保护更新范围。
- 更新和删除默认受安全模式保护，不建议禁用。

### 10.2 只允许更新一条

```java
boolean updated = dao.singleUpdateByQueryObj(req);
```

语义：

- 更新 0 条返回 `false`。
- 更新 1 条返回 `true`。
- 更新多条抛异常并回滚。

如果必须更新且只能更新一条：

```java
dao.uniqueUpdateByQueryObj(req);
```

`uniqueUpdateByQueryObj` 比 `singleUpdateByQueryObj` 更严格：更新 0 条或多条都会抛异常。它适合“这次操作必须命中且只能命中一条业务数据”的场景，例如提交订单、状态流转、乐观锁编辑、确认任务完成。

建议把唯一更新写成“唯一业务条件 + 必填条件 + 乐观锁”：

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class ConfirmUserReq {

    @Eq(value = "tenantId", require = true)
    String tenantId;

    @Eq(require = true)
    Long id;

    @Eq(value = "optimisticLock", require = true)
    Integer oldOptimisticLock;

    @Update(value = "state")
    String newState = "CONFIRMED";

    @Update(value = "optimisticLock", incrementMode = true, paramExpr = "1", condition = "")
    Integer optimisticLock;
}
```

调用：

```java
dao.uniqueUpdateByQueryObj(
        new ConfirmUserReq()
                .setTenantId("T001")
                .setId(1001L)
                .setOldOptimisticLock(3)
);
```

这个写法的语义是：

- `tenantId`、`id`、`oldOptimisticLock` 必须参与 `where` 条件，任何一个为空都会抛异常。
- 更新结果必须等于 1 条；0 条通常表示数据不存在、租户不匹配或乐观锁失败。
- 多于 1 条说明条件不够唯一，会抛异常并回滚。

### 10.3 增量更新

适合“计数器 +1”“分数增加 N”“乐观锁版本号 +1”：

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class IncreaseUserScoreReq {

    @Eq(require = true)
    Long id;

    @Update(value = "score", paramExpr = "${_name} + ${:_val}")
    Integer scoreDelta;

    @Update(value = "orderCode", incrementMode = true, paramExpr = "1", condition = "")
    Integer version;
}
```

调用：

```java
dao.singleUpdateByQueryObj(
        new IncreaseUserScoreReq()
                .setId(1L)
                .setScoreDelta(5)
);
```

大致生成：

```sql
set score = score + ?, orderCode = orderCode + 1
where id = ?
```

说明：

- `${_name}` 表示当前字段名或目标列名。
- `${:_val}` 表示当前字段值以参数方式绑定。
- `incrementMode = true` 会按增量模式处理字段。

### 10.4 JSON 字段更新

当前代码支持在部分注解上使用 `jsonPath`。例如更新 JSON 字段 `logs` 的第一条日志文本：

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class UpdateFirstLogReq {

    @Contains(value = "roleList", jsonPath = "$[*]")
    String role;

    @Update(value = "logs", jsonPath = "$[0].logText")
    String firstLogText;
}
```

调用：

```java
dao.updateByQueryObj(
        new UpdateFirstLogReq()
                .setRole("admin")
                .setFirstLogText("changed")
);
```

注意：

- 查询数组元素时可以用 `"$[*]"`。
- 统计和更新场景对通配路径有限制，通配路径不适合所有更新/统计操作。
- 相关测试可看 `DaoExamplesTest` 中的 `JsonPathSelectQO`、`JsonPathUpdateDTO`。

### 10.5 JSON 数组追加

如果实体字段本身是集合或数组类型，`@Update(incrementMode = true)` 可以生成 JSON 数组追加表达式。仓库测试里 `roleList` 就是这类场景。

DTO 写法：

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class AppendUserRoleReq {

    @Eq(value = "id", require = true)
    Long id;

    @Update(value = "roleList", incrementMode = true)
    String role;
}
```

调用：

```java
dao.singleUpdateByQueryObj(
        new AppendUserRoleReq()
                .setId(1L)
                .setRole("R_ADMIN")
);
```

大致生成：

```sql
set roleList = json_array_append(coalesce(roleList, json_array()), '$', ?)
where id = ?
```

链式 API 也可以追加：

```java
dao.updateTo(User.class)
        .set(true, true, User::getRoleList, "R_ADMIN")
        .eq(E_User.id, 1L)
        .singleUpdate();
```

注意：

- JSON 数组追加默认追加到根路径 `"$"`。
- 可以一次传集合，框架会尝试展开为多个追加参数。
- `@Update(value = "roleList", jsonPath = "$[*]", incrementMode = true)` 这类 wildcard 路径会被拒绝；追加数组时不要写 `"$[*]"`。
- 更新接口仍然要加 `@Eq(require = true)`、租户条件或其它唯一条件，避免把角色追加到过多数据上。

## 11. 删除例子

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class DeleteUserReq {

    @Eq(require = true)
    Long id;
}
```

调用：

```java
boolean deleted = dao.singleDeleteByQueryObj(new DeleteUserReq().setId(1L));
```

语义和单条更新类似：

- 删除 0 条返回 `false`。
- 删除 1 条返回 `true`。
- 删除多条抛异常并回滚。

如果实体上配置了 `EntityOption` 的逻辑删除字段，删除会按实体配置处理。业务接口里建议优先使用请求对象承载租户、组织、个人等数据范围，不要只传裸 `id`。

## 12. 统计查询例子

### 12.1 单表分组统计

需求：按用户状态分组，统计总分，过滤“总分大于 500”的状态。

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = UserStatDTO.class)
@Data
public class UserStatDTO {

    @GroupBy
    @OrderBy
    String state;

    @Sum(havingOp = Op.Gt)
    Integer score = 500;

    @Contains
    String name = "Echo";
}
```

调用：

```java
List<UserStatDTO> rows = dao.findByQueryObj(UserStatDTO.class, new UserStatDTO());
```

大致生成：

```sql
select u.state, sum(u.score)
from User u
where u.name like ?
group by u.state
having sum(u.score) > ?
order by u.state desc
```

### 12.2 多表分组统计

需求：按用户所在组统计人数、总分、平均分。

```java
@TargetOption(
        nativeQL = true,
        entityClass = User.class,
        alias = E_User.ALIAS,
        resultClass = GroupUserStat.class,
        joinOptions = {
                @JoinOption(entityClass = Group.class, alias = E_Group.ALIAS)
        }
)
@Data
@Accessors(chain = true)
public class GroupUserStat {

    @Count(havingOp = Op.Gt, orderBy = @OrderBy)
    Integer userCnt = 5;

    @Sum
    Long sumScore;

    @Avg(havingOp = Op.Gt, orderBy = @OrderBy, alias = "avgScore")
    Long avgScore = 60L;

    @GroupBy(domain = E_Group.ALIAS, value = E_Group.name,
            orderBy = @OrderBy(scope = OrderBy.Scope.OnlyForGroupBy))
    String groupName;
}
```

调用：

```java
List<GroupUserStat> stats = dao.findByQueryObj(new GroupUserStat());
```

大致生成：

```sql
select count(1), sum(u.score), avg(u.score), g.name
from user u
left join group g on ...
group by g.name
having count(1) > ? and avg(u.score) > ?
order by count(1) desc, avgScore desc, g.name desc
```

说明：

- `@JoinOption(entityClass = Group.class, alias = "g")` 会尽量根据实体关系推断连接条件。
- 关系不明确时，需要指定 `joinColumn`、`joinTargetAlias`、`joinTargetColumn` 或 `onExpr`。

### 12.3 多指标统计

需求：在一个请求里同时返回用户数、总分、平均分、最高分、最低分。

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = UserScoreSummary.class)
@Data
@Accessors(chain = true)
public class UserScoreSummaryReq {

    @Contains("name")
    String keyword;

    @In("state")
    String[] states;

    @Count(alias = "userCnt")
    Long userCnt;

    @Sum(value = "score", alias = "sumScore")
    Long sumScore;

    @Avg(value = "score", alias = "avgScore")
    Long avgScore;

    @Max(value = "score", alias = "maxScore")
    Integer maxScore;

    @Min(value = "score", alias = "minScore")
    Integer minScore;
}
```

调用：

```java
UserScoreSummaryReq req = new UserScoreSummaryReq()
        .setKeyword("Echo")
        .setStates(new String[]{"A", "B"});

List<UserScoreSummary> rows = dao.findByQueryObj(UserScoreSummary.class, req);
```

大致生成：

```sql
select count(1) as userCnt,
       sum(u.score) as sumScore,
       avg(u.score) as avgScore,
       max(u.score) as maxScore,
       min(u.score) as minScore
from User u
where u.name like ?
  and u.state in (?, ?)
```

说明：没有 `@GroupBy` 时，这是一个汇总行；加上 `@GroupBy` 后，就会变成按维度分组的多行统计。

### 12.4 分组 + Having + 排序

需求：按状态统计人数和平均分，只返回人数大于 10 且平均分大于 80 的状态，并按平均分倒序。

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = StateScoreStat.class)
@Data
@Accessors(chain = true)
public class StateScoreStatReq {

    @GroupBy
    String state;

    @Count(havingOp = Op.Gt)
    Integer userCnt = 10;

    @Avg(value = "score", havingOp = Op.Gt,
            alias = "avgScore",
            orderBy = @OrderBy(type = OrderBy.Type.Desc))
    Integer avgScore = 80;
}
```

大致生成：

```sql
select u.state, count(1), avg(u.score) as avgScore
from User u
group by u.state
having count(1) > ? and avg(u.score) > ?
order by avgScore desc
```

### 12.5 CASE 条件统计

CASE 表达式在统计里很实用，适合把“满足某条件记 1，否则记 0”，再通过 `@Sum` 汇总成数量。

这一节按常见业务场景组织，读的时候可以按需求直接跳：

- 要统计高分/低分人数，看第一个基础例子。
- 要按订单状态拆支付、退款、取消金额，看 `12.5.2`。
- 要按 `payTime` 是否为空决定金额是否计入统计，看 `12.5.3`。
- 要统计本周、本月金额，看 `12.5.5`。
- 要写真实交易流水统计 DTO，看 `12.5.7`。
- 要在服务层动态生成近 7 天、本月、上月、上上月统计列，看 `12.5.8`。

需求：统计每个状态下的用户数、高分人数、低分人数。

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = StateScoreBucketStat.class)
@Data
@Accessors(chain = true)
public class StateScoreBucketStatReq {

    @GroupBy
    String state;

    @Count(alias = "userCnt")
    Long userCnt;

    @Sum(value = "score", alias = "highScoreCnt", fieldCases = {
            @Case(column = "", elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "F$:score >= 90", thenExpr = "1")
            })
    })
    Long highScoreCnt;

    @Sum(value = "score", alias = "lowScoreCnt", fieldCases = {
            @Case(column = "", elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "F$:score < 60", thenExpr = "1")
            })
    })
    Long lowScoreCnt;
}
```

大致生成：

```sql
select u.state,
       count(1) as userCnt,
       sum(case when u.score >= 90 then 1 else 0 end) as highScoreCnt,
       sum(case when u.score < 60 then 1 else 0 end) as lowScoreCnt
from User u
group by u.state
```

#### 12.5.1 订单成功笔数和成功金额

再举一个更贴近业务报表的例子：统计交易总笔数、成功笔数、成功金额。

```java
@TargetOption(entityClass = TradeLog.class, alias = "t", resultClass = TradeSummary.class)
@Data
@Accessors(chain = true)
public class TradeSummaryReq {

    @Gte("createTime")
    LocalDateTime beginTime;

    @Lte("createTime")
    LocalDateTime endTime;

    @Count(alias = "tradeCnt")
    Long tradeCnt;

    @Sum(value = "tradingStatus", alias = "successCnt", fieldCases = {
            @Case(elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'SUCCESS'", thenExpr = "1")
            })
    })
    Long successCnt;

    @Sum(value = "amount", alias = "successAmount", fieldCases = {
            @Case(column = "tradingStatus", elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'SUCCESS'", thenExpr = "F$:amount")
            })
    })
    BigDecimal successAmount;
}
```

大致生成：

```sql
select count(1) as tradeCnt,
       sum(case t.tradingStatus when 'SUCCESS' then 1 else 0 end) as successCnt,
       sum(case t.tradingStatus when 'SUCCESS' then t.amount else 0 end) as successAmount
from TradeLog t
where t.createTime >= ?
  and t.createTime <= ?
```

#### 12.5.2 按订单状态拆分金额

很多订单报表会同时展示“支付成功金额、退款金额、取消金额、待支付金额”。这种场景不需要查多次，可以用多个 `@Sum + @Case` 一次统计出来。

```java
@TargetOption(entityClass = Order.class, alias = "o", resultClass = OrderAmountSummary.class)
@Data
@Accessors(chain = true)
public class OrderAmountSummaryReq {

    @Gte("createTime")
    LocalDateTime beginTime;

    @Lte("createTime")
    LocalDateTime endTime;

    @Count(alias = "orderCnt")
    Long orderCnt;

    @Sum(value = "amount", alias = "totalAmount")
    BigDecimal totalAmount;

    @Sum(value = "amount", alias = "paidAmount", fieldCases = {
            @Case(column = "state", elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'PAID'", thenExpr = "F$:amount")
            })
    })
    BigDecimal paidAmount;

    @Sum(value = "amount", alias = "refundAmount", fieldCases = {
            @Case(column = "state", elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'REFUNDED'", thenExpr = "F$:amount")
            })
    })
    BigDecimal refundAmount;

    @Sum(value = "amount", alias = "cancelAmount", fieldCases = {
            @Case(column = "state", elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'CANCELLED'", thenExpr = "F$:amount")
            })
    })
    BigDecimal cancelAmount;
}
```

大致生成：

```sql
select count(1) as orderCnt,
       sum(o.amount) as totalAmount,
       sum(case o.state when 'PAID' then o.amount else 0 end) as paidAmount,
       sum(case o.state when 'REFUNDED' then o.amount else 0 end) as refundAmount,
       sum(case o.state when 'CANCELLED' then o.amount else 0 end) as cancelAmount
from Order o
where o.createTime >= ?
  and o.createTime <= ?
```

这种写法适合首页看板、经营概览、支付渠道日报等场景。优点是查询一次就能得到多列指标，避免业务层查多次再拼。

#### 12.5.3 按支付时间判断成交金额和运费

有些订单不一定通过状态字段判断是否成交，而是通过 `payTime` / `pay_time` 是否为空来判断。比如：

- 未支付：`payTime is null`，成交金额记 0，运费记 0。
- 已支付：`payTime is not null`，成交金额取商品实付金额，运费取运费字段。

这种场景可以用 `column = ""` 写搜索 CASE。

```java
@TargetOption(entityClass = Order.class, alias = "o", resultClass = OrderPaidAmountSummary.class)
@Data
@Accessors(chain = true)
public class OrderPaidAmountSummaryReq {

    @Schema(description = "成交金额：分")
    @Sum(value = "productRealPrice", alias = "paidOrderAmount", fieldCases = {
            @Case(column = "", elseExpr = "F$:productRealPrice", whenOptions = {
                    @Case.When(whenExpr = "F$:payTime IS NULL", thenExpr = "0")
            })
    })
    Integer paidOrderAmount;

    @Schema(description = "运费金额：分")
    @Sum(value = "freight", alias = "freight", fieldCases = {
            @Case(column = "", elseExpr = "F$:freight", whenOptions = {
                    @Case.When(whenExpr = "F$:payTime IS NULL", thenExpr = "0")
            })
    })
    Integer freight;
}
```

大致生成：

```sql
select sum(case when o.payTime is null then 0 else o.productRealPrice end) as paidOrderAmount,
       sum(case when o.payTime is null then 0 else o.freight end) as freight
from Order o
```

如果项目里习惯使用生成的实体常量，也可以写成下面这种形态。核心意思一样：`pay_time is null` 时统计 0，否则统计对应金额字段。

```java
@Schema(description = "成交金额:分")
@Sum(fieldCases = @Case(
        column = "",
        whenOptions = @Case.When(
                whenExpr = E_Order.pay_time + " IS NULL",
                thenExpr = "0"
        ),
        elseExpr = E_Order.product_real_price
))
Integer paid_order_amount;

@Schema(description = "运费金额:分")
@Sum(fieldCases = @Case(
        column = "",
        whenOptions = @Case.When(
                whenExpr = E_Order.pay_time + " IS NULL",
                thenExpr = "0"
        ),
        elseExpr = E_Order.freight
))
Integer freight;
```

这个例子里，`@Sum` 统计的不是原始字段本身，而是 CASE 表达式的结果：

```sql
sum(case when pay_time is null then 0 else product_real_price end)
sum(case when pay_time is null then 0 else freight end)
```

这种写法特别适合“未达成条件就不计入金额”的业务统计，比如未支付订单、未结算订单、未核销记录等。

#### 12.5.4 同时统计不同状态的订单数量

如果只统计数量，不统计金额，可以让 `thenExpr = "1"`，`elseExpr = "0"`。

```java
@TargetOption(entityClass = Order.class, alias = "o", resultClass = OrderStateCountSummary.class)
@Data
@Accessors(chain = true)
public class OrderStateCountSummaryReq {

    @Count(alias = "orderCnt")
    Long orderCnt;

    @Sum(value = "state", alias = "paidCnt", fieldCases = {
            @Case(elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'PAID'", thenExpr = "1")
            })
    })
    Long paidCnt;

    @Sum(value = "state", alias = "waitPayCnt", fieldCases = {
            @Case(elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'WAIT_PAY'", thenExpr = "1")
            })
    })
    Long waitPayCnt;

    @Sum(value = "state", alias = "closedCnt", fieldCases = {
            @Case(elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'CLOSED'", thenExpr = "1")
            })
    })
    Long closedCnt;
}
```

大致生成：

```sql
select count(1) as orderCnt,
       sum(case o.state when 'PAID' then 1 else 0 end) as paidCnt,
       sum(case o.state when 'WAIT_PAY' then 1 else 0 end) as waitPayCnt,
       sum(case o.state when 'CLOSED' then 1 else 0 end) as closedCnt
from Order o
```

这种写法常用于“订单状态分布”卡片，比查出订单后在 Java 里循环计数更直接。

#### 12.5.5 本周、本月订单金额统计

如果报表要同时展示“总金额、本周金额、本月金额”，可以把时间边界作为上下文变量传给 CASE。这里用 `@CtxVar` 暴露 `weekBegin`、`monthBegin`，再在 `whenExpr` 中通过 `${:weekBegin}`、`${:monthBegin}` 作为参数使用。

```java
@TargetOption(entityClass = Order.class, alias = "o", resultClass = OrderPeriodAmountSummary.class)
@Data
@Accessors(chain = true)
public class OrderPeriodAmountSummaryReq {

    @CtxVar
    @Ignore
    LocalDateTime weekBegin;

    @CtxVar
    @Ignore
    LocalDateTime monthBegin;

    @Sum(value = "amount", alias = "totalAmount")
    BigDecimal totalAmount;

    @Sum(value = "amount", alias = "weekAmount", fieldCases = {
            @Case(column = "", elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "F$:createTime >= ${:weekBegin}", thenExpr = "F$:amount")
            })
    })
    BigDecimal weekAmount;

    @Sum(value = "amount", alias = "monthAmount", fieldCases = {
            @Case(column = "", elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "F$:createTime >= ${:monthBegin}", thenExpr = "F$:amount")
            })
    })
    BigDecimal monthAmount;
}
```

调用时由业务层计算时间边界：

```java
OrderPeriodAmountSummaryReq req = new OrderPeriodAmountSummaryReq()
        .setWeekBegin(LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay())
        .setMonthBegin(LocalDate.now().withDayOfMonth(1).atStartOfDay());

OrderPeriodAmountSummary stat = dao.findOneByQueryObj(
        OrderPeriodAmountSummary.class,
        req
);
```

大致生成：

```sql
select sum(o.amount) as totalAmount,
       sum(case when o.createTime >= ? then o.amount else 0 end) as weekAmount,
       sum(case when o.createTime >= ? then o.amount else 0 end) as monthAmount
from Order o
```

如果还要区分支付成功状态，可以把时间条件和状态条件写在同一个 `whenExpr` 里：

```java
@Sum(value = "amount", alias = "weekPaidAmount", fieldCases = {
        @Case(column = "", elseExpr = "0", whenOptions = {
                @Case.When(
                        whenExpr = "F$:createTime >= ${:weekBegin} AND F$:state = 'PAID'",
                        thenExpr = "F$:amount"
                )
        })
})
BigDecimal weekPaidAmount;
```

大致生成：

```sql
sum(case
        when o.createTime >= ? and o.state = 'PAID' then o.amount
        else 0
    end) as weekPaidAmount
```

#### 12.5.6 按渠道分组，再按状态拆金额

CASE 可以和 `@GroupBy` 一起使用。例如按支付渠道分组，同时统计每个渠道的总金额、成功金额、退款金额。

```java
@TargetOption(entityClass = Order.class, alias = "o", resultClass = ChannelOrderAmountStat.class)
@Data
@Accessors(chain = true)
public class ChannelOrderAmountStatReq {

    @GroupBy
    String payChannel;

    @Count(alias = "orderCnt")
    Long orderCnt;

    @Sum(value = "amount", alias = "totalAmount")
    BigDecimal totalAmount;

    @Sum(value = "amount", alias = "paidAmount", fieldCases = {
            @Case(column = "state", elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'PAID'", thenExpr = "F$:amount")
            })
    })
    BigDecimal paidAmount;

    @Sum(value = "amount", alias = "refundAmount", fieldCases = {
            @Case(column = "state", elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'REFUNDED'", thenExpr = "F$:amount")
            })
    })
    BigDecimal refundAmount;
}
```

大致生成：

```sql
select o.payChannel,
       count(1) as orderCnt,
       sum(o.amount) as totalAmount,
       sum(case o.state when 'PAID' then o.amount else 0 end) as paidAmount,
       sum(case o.state when 'REFUNDED' then o.amount else 0 end) as refundAmount
from Order o
group by o.payChannel
```

#### 12.5.7 交易流水统计：成功笔数、分账、入金、服务费

下面这个例子更接近实际项目里的写法：外层请求对象负责查询条件，内部 `Result` 类负责统计结果。比如按创建时间筛选交易流水，只统计入金和分账两类交易，并返回交易总笔数、成功笔数、分账金额、入金金额、服务费金额。

```java
@Data
@Accessors(chain = true)
@FieldNameConstants
@TargetOption(
        entityClass = MemberTradingLog.class,
        alias = E_MemberTradingLog.ALIAS,
        resultClass = QueryTenantTradingMemberInfoReq.Result.class
)
public class QueryTenantTradingMemberInfoReq
        extends MultiTenantOrgReq<QueryTenantTradingMemberInfoReq> {

    @NotNull
    @Schema(title = L_createTime, description = "大于等于" + L_createTime)
    @Gte
    Date gteCreateTime;

    @Schema(title = L_createTime, description = "小于等于" + L_createTime)
    @Lte
    Date lteCreateTime;

    @Schema(title = "交易类型", hidden = true)
    @In
    final List<TradingType> tradingType = Arrays.asList(
            TradingType.Income,
            TradingType.Split
    );

    @Data
    @NoArgsConstructor
    @Accessors(chain = true)
    public static class Result implements Serializable {

        @Schema(title = "交易总笔数")
        @Count
        Long tradeNum;

        @Schema(title = "交易成功笔数")
        @Sum(fieldCases = @Case(
                column = E_MemberTradingLog.F_tradingStatus,
                whenOptions = @Case.When(
                        whenExpr = E_TradingStatus.Succeed_STR,
                        thenExpr = "1"
                ),
                elseExpr = "0"
        ))
        Long tradeSuccessNum;

        @Schema(title = "分账金额")
        @Sum(fieldCases = @Case(
                column = E_MemberTradingLog.F_tradingType,
                whenOptions = @Case.When(
                        whenExpr = E_TradingType.Split_STR,
                        thenExpr = E_MemberTradingLog.F_tradingAmount
                ),
                elseExpr = "0"
        ))
        Long allocateAmount;

        @Schema(title = "入金金额")
        @Sum(fieldCases = @Case(
                column = E_MemberTradingLog.F_tradingType,
                whenOptions = @Case.When(
                        whenExpr = E_TradingType.Income_STR,
                        thenExpr = E_MemberTradingLog.F_tradingAmount
                ),
                elseExpr = "0"
        ))
        Long incomeAmount;

        @Schema(title = "服务费金额")
        @Sum(E_MemberTradingLog.baseServiceFee + " + " + E_MemberTradingLog.serviceFee)
        Long serviceCharge;
    }
}
```

大致生成：

```sql
select count(1) as tradeNum,
       sum(case t.tradingStatus when 'Succeed' then 1 else 0 end) as tradeSuccessNum,
       sum(case t.tradingType when 'Split' then t.tradingAmount else 0 end) as allocateAmount,
       sum(case t.tradingType when 'Income' then t.tradingAmount else 0 end) as incomeAmount,
       sum(t.baseServiceFee + t.serviceFee) as serviceCharge
from MemberTradingLog t
where t.createTime >= ?
  and t.createTime <= ?
  and t.tradingType in (?, ?)
```

这个例子里有几个常用技巧：

- `@TargetOption(resultClass = Result.class)` 表示查询结果不是实体，而是内部统计结果类。
- 外层字段 `gteCreateTime`、`lteCreateTime`、`tradingType` 都是查询条件，不会出现在 select 里。
- `@Schema(hidden = true) + final List + @In` 适合固定业务范围，比如这个接口只统计入金和分账。
- `@Sum(fieldCases = @Case(... thenExpr = "1" ...))` 可以把满足条件的记录转成 1，再求和得到数量。
- `@Sum(fieldCases = @Case(... thenExpr = F_tradingAmount ...))` 可以把满足条件的金额纳入统计，不满足条件按 0 处理。
- `@Sum("baseServiceFee + serviceFee")` 适合统计多个字段的表达式结果，例如基础服务费加服务费。

#### 12.5.8 编程式统计：近 7 天、本月、上月、上上月

有些报表的统计窗口不是固定字段，而是服务层动态算出来的日期范围，例如：

- 近 7 天入金金额、服务费金额。
- 本月入金金额、入金笔数、服务费金额。
- 上月入金金额、入金笔数、服务费金额。
- 上上月入金金额、入金笔数、服务费金额。

这种情况下可以保留请求对象里的租户、组织、基础条件，然后用 `Case` 和 `selectByStatement` 动态追加统计列。

```java
Date endDate = DateUtil.endOfDay(
        ObjUtil.defaultIfNull(req.getGteTradingTime(), new Date())
).toJdkDate();

Date start7Day = DateUtil.offsetDay(endDate, -7).toJdkDate();

Date startOfMonth = DateUtil.beginOfMonth(endDate).toJdkDate();
Date endOfMonth = DateUtil.endOfMonth(endDate).toJdkDate();

Date startOfPrevMonth = DateUtil.beginOfMonth(
        DateUtil.offsetMonth(startOfMonth, -1)
).toJdkDate();
Date endOfPrevMonth = DateUtil.endOfMonth(
        DateUtil.offsetMonth(startOfMonth, -1)
).toJdkDate();

Date startOfPrevPrevMonth = DateUtil.beginOfMonth(
        DateUtil.offsetMonth(startOfMonth, -2)
).toJdkDate();
Date endOfPrevPrevMonth = DateUtil.endOfMonth(
        DateUtil.offsetMonth(startOfMonth, -2)
).toJdkDate();

String placeholder = simpleDao.getParamPlaceholder(true);

String condition = "("
        + E_MemberTradingLog.F_tradingType + " = " + placeholder
        + " AND " + E_MemberTradingLog.F_tradingTime
        + " BETWEEN " + placeholder + " AND " + placeholder
        + ")";

String sumExpr = "SUM(" + new Case()
        .elseExpr("0")
        .when(condition, E_MemberTradingLog.F_tradingAmount)
        + ")";

String cntExpr = "COUNT(" + new Case()
        .elseExpr("NULL")
        .when(condition, "1")
        + ")";

TradingStatReq.StatResult stat = simpleDao.forSelect(req)
        .selectByStatement(
                sumExpr + " AS " + TradingStatReq.StatResult.Fields.incomeAmount7Day,
                TradingType.Income.name(), start7Day, endDate
        )
        .selectByStatement(
                sumExpr + " AS " + TradingStatReq.StatResult.Fields.serviceFee7Day,
                TradingType.ServiceFee.name(), start7Day, endDate
        )

        .selectByStatement(
                sumExpr + " AS " + TradingStatReq.StatResult.Fields.incomeAmountCM,
                TradingType.Income.name(), startOfMonth, endOfMonth
        )
        .selectByStatement(
                cntExpr + " AS " + TradingStatReq.StatResult.Fields.incomeCntCM,
                TradingType.Income.name(), startOfMonth, endOfMonth
        )
        .selectByStatement(
                sumExpr + " AS " + TradingStatReq.StatResult.Fields.serviceFeeCM,
                TradingType.ServiceFee.name(), startOfMonth, endOfMonth
        )

        .selectByStatement(
                sumExpr + " AS " + TradingStatReq.StatResult.Fields.incomeAmountPM,
                TradingType.Income.name(), startOfPrevMonth, endOfPrevMonth
        )
        .selectByStatement(
                cntExpr + " AS " + TradingStatReq.StatResult.Fields.incomeCntPM,
                TradingType.Income.name(), startOfPrevMonth, endOfPrevMonth
        )
        .selectByStatement(
                sumExpr + " AS " + TradingStatReq.StatResult.Fields.serviceFeePM,
                TradingType.ServiceFee.name(), startOfPrevMonth, endOfPrevMonth
        )

        .selectByStatement(
                sumExpr + " AS " + TradingStatReq.StatResult.Fields.incomeAmountPPM,
                TradingType.Income.name(), startOfPrevPrevMonth, endOfPrevPrevMonth
        )
        .selectByStatement(
                cntExpr + " AS " + TradingStatReq.StatResult.Fields.incomeCntPPM,
                TradingType.Income.name(), startOfPrevPrevMonth, endOfPrevPrevMonth
        )
        .selectByStatement(
                sumExpr + " AS " + TradingStatReq.StatResult.Fields.serviceFeePPM,
                TradingType.ServiceFee.name(), startOfPrevPrevMonth, endOfPrevPrevMonth
        )
        .findOne(TradingStatReq.StatResult.class);
```

大致生成：

```sql
select sum(case
               when t.tradingType = ?
                and t.tradingTime between ? and ?
               then t.tradingAmount
               else 0
           end) as incomeAmount7Day,
       count(case
                 when t.tradingType = ?
                  and t.tradingTime between ? and ?
                 then 1
                 else null
             end) as incomeCntCM,
       ...
from MemberTradingLog t
where ...
```

这个写法适合“统计列很多、每列的时间范围或交易类型不一样”的场景。几个注意点：

- `simpleDao.forSelect(req)` 会继续使用 `req` 上已有的查询条件，例如租户、组织、权限、基础时间范围等。
- `selectByStatement(expr, params...)` 里的参数数量，要和表达式中的占位符数量一致。
- 金额统计推荐 `SUM(CASE WHEN ... THEN amount ELSE 0 END)`。
- 数量统计推荐 `COUNT(CASE WHEN ... THEN 1 ELSE NULL END)`，或者 `SUM(CASE WHEN ... THEN 1 ELSE 0 END)`。
- 如果 CASE 表达式要复用，不要在同一个 `Case` 对象上反复修改 `elseExpr`；可以像上面一样分别 new 一个金额 CASE 和数量 CASE，避免后续维护时看错。
- 上上月字段别名要单独使用 `incomeCntPPM` 这类字段名，避免误写成上月的 `incomeCntPM`。

选择建议：

- 指标结构固定、接口入参也固定时，优先用注解式统计 DTO，代码更清楚，也方便生成接口文档。
- 指标列需要根据服务层日期、枚举、配置动态拼出来时，用 `Case + selectByStatement` 更自然。
- 不管哪种写法，都建议让请求对象继续承载租户、组织、权限等基础条件，避免统计绕过数据范围。

说明：

- `fieldCases` 是对统计字段左侧表达式做 CASE 包装。
- `F$:score`、`F$:amount`、`F$:createTime` 会按当前查询模式转换成正确字段表达式。
- 这些 `F$:` 字段表达式也有生成常量，优先写 `E_User.F_score`、`E_Order.F_amount`、`E_Order.F_createTime`，不要把字段名藏在字符串里。
- `column = "state"` 表示简单 CASE：`case state when 'PAID' then ... else ... end`。
- `column = ""` 表示搜索 CASE：`case when createTime >= ? then ... else ... end`，适合时间范围、复合条件等场景。
- `${:weekBegin}`、`${:monthBegin}` 这类写法表示从查询上下文中取值并作为参数绑定，通常配合 `@CtxVar` 使用。
- CASE 条件里尽量使用实体字段常量，避免字符串字段名写错。

## 13. 多表查询例子

### 13.1 自动关联实体

如果 `User` 中存在 `Group group` 这类实体关系，可以让 `JoinOption` 自动推断：

```java
@TargetOption(
        entityClass = User.class,
        alias = "u",
        resultClass = UserGroupInfo.class,
        joinOptions = {
                @JoinOption(entityClass = Group.class, alias = "g")
        }
)
@Data
public class QueryUserGroupReq {

    @Select(domain = "u", value = "id")
    Long userId;

    @Select(domain = "u", value = "name")
    String userName;

    @Select(domain = "g", value = "name")
    String groupName;

    @Contains(domain = "g", value = "name")
    String groupKeyword;
}
```

调用：

```java
List<UserGroupInfo> list = dao.findByQueryObj(
        UserGroupInfo.class,
        new QueryUserGroupReq().setGroupKeyword("研发")
);
```

### 13.2 手动指定连接字段

当实体里有多个同类型关联，或者你不想依赖自动推断时：

```java
@TargetOption(
        entityClass = User.class,
        alias = "u",
        resultClass = UserGroupInfo.class,
        joinOptions = {
                @JoinOption(
                        entityClass = Group.class,
                        alias = "g",
                        joinColumn = "id",
                        joinTargetAlias = "u",
                        joinTargetColumn = "group"
                )
        }
)
@Data
public class QueryUserGroupByManualJoinReq {

    @Select(domain = "u", value = "name")
    String userName;

    @Select(domain = "g", value = "name")
    String groupName;
}
```

如果连接条件非常特殊，可以使用 `onExpr`：

```java
@JoinOption(
        entityClass = Group.class,
        alias = "g",
        onExpr = "u.group = g.id and g.state = 'A'"
)
```

### 13.3 三表连接：用 `domain` 明确字段属于哪张表

多表查询最容易出错的地方是：几个表都有 `id`、`name`、`state`、`createTime` 这类字段。Simple DAO 通过 `domain` 指定字段归属的表别名，通过 `value` 指定这个表上的字段名。

下面例子查询订单，同时关联会员和商品：

- 主表：订单 `Order`，别名使用 `E_Order.ALIAS`。
- 连接表一：会员 `Member`，别名使用 `E_Member.ALIAS`。
- 连接表二：商品 `Product`，别名使用 `E_Product.ALIAS`。
- 查询条件分别来自订单、会员、商品三张表。
- 返回结果字段也分别来自三张表。

```java
@TargetOption(
        entityClass = Order.class,
        alias = E_Order.ALIAS,
        resultClass = QueryOrderMemberProductReq.Result.class,
        joinOptions = {
                @JoinOption(
                        entityClass = Member.class,
                        alias = E_Member.ALIAS,
                        joinColumn = E_Member.id,
                        joinTargetAlias = E_Order.ALIAS,
                        joinTargetColumn = E_Order.member
                ),
                @JoinOption(
                        entityClass = Product.class,
                        alias = E_Product.ALIAS,
                        joinColumn = E_Product.id,
                        joinTargetAlias = E_Order.ALIAS,
                        joinTargetColumn = E_Order.product
                )
        }
)
@Data
@Accessors(chain = true)
public class QueryOrderMemberProductReq {

    Paging paging = new PagingQueryReq(1, 20);

    @Eq(domain = E_Order.ALIAS, value = E_Order.state)
    String orderState;

    @Gte(domain = E_Order.ALIAS, value = E_Order.createTime)
    LocalDateTime orderBeginTime;

    @Eq(domain = E_Member.ALIAS, value = E_Member.level)
    String memberLevel;

    @Contains(domain = E_Member.ALIAS, value = E_Member.name)
    String memberKeyword;

    @Eq(domain = E_Product.ALIAS, value = E_Product.category)
    String productCategory;

    @Data
    public static class Result {

        @Select(domain = E_Order.ALIAS, value = E_Order.id)
        Long orderId;

        @Select(domain = E_Order.ALIAS, value = E_Order.orderNo)
        String orderNo;

        @Select(domain = E_Order.ALIAS, value = E_Order.amount)
        Long orderAmount;

        @Select(domain = E_Member.ALIAS, value = E_Member.id)
        Long memberId;

        @Select(domain = E_Member.ALIAS, value = E_Member.name)
        String memberName;

        @Select(domain = E_Product.ALIAS, value = E_Product.id)
        Long productId;

        @Select(domain = E_Product.ALIAS, value = E_Product.name)
        String productName;
    }
}
```

这个 DTO 里每个字段的归属很明确：

| 字段 | 注解 | `domain` | 使用哪张表 |
| --- | --- | --- | --- |
| `orderState` | `@Eq` | `E_Order.ALIAS` | 订单表 `Order` |
| `orderBeginTime` | `@Gte` | `E_Order.ALIAS` | 订单表 `Order` |
| `memberLevel` | `@Eq` | `E_Member.ALIAS` | 会员表 `Member` |
| `memberKeyword` | `@Contains` | `E_Member.ALIAS` | 会员表 `Member` |
| `productCategory` | `@Eq` | `E_Product.ALIAS` | 商品表 `Product` |
| `Result.orderNo` | `@Select` | `E_Order.ALIAS` | 从订单表取返回字段 |
| `Result.memberName` | `@Select` | `E_Member.ALIAS` | 从会员表取返回字段 |
| `Result.productName` | `@Select` | `E_Product.ALIAS` | 从商品表取返回字段 |

大致会生成类似下面的 JPQL/HQL 结构。为了阅读方便，这里用 `o`、`m`、`p` 表示订单、会员、商品三个别名；实际项目里建议继续用 `E_XXX.ALIAS` 常量。

```sql
select o.id as orderId,
       o.orderNo as orderNo,
       o.amount as orderAmount,
       m.id as memberId,
       m.name as memberName,
       p.id as productId,
       p.name as productName
from Order o
left join Member m on m.id = o.member
left join Product p on p.id = o.product
where o.state = ?
  and o.createTime >= ?
  and m.level = ?
  and m.name like ?
  and p.category = ?
```

这里最重要的是：

- `domain = E_Order.ALIAS`：条件或结果字段来自订单表。
- `domain = E_Member.ALIAS`：条件或结果字段来自会员表。
- `domain = E_Product.ALIAS`：条件或结果字段来自商品表。
- 多表关联时，`@TargetOption(alias = ...)`、`@JoinOption(alias = ...)`、`domain = ...`、`joinTargetAlias = ...` 都尽量使用 `E_XXX.ALIAS` 常量，不要手写 `"o"`、`"m"`、`"p"` 这类字符串别名。
- `value = E_XXX.xxx`：表示这个 `domain` 对应实体上的字段名。
- 如果不写 `domain`，字段通常会落到主表别名上；多表里一旦字段重名，就很容易查错表或生成错误语句。

连接本身也要分清两个方向：

- `joinColumn = E_Member.id`：连接表 `Member` 自己用于关联的字段。
- `joinTargetAlias = E_Order.ALIAS`：要关联到哪个目标表别名。
- `joinTargetColumn = E_Order.member`：目标表 `Order` 上指向会员的字段。

如果连接条件还要额外限制，例如只连接有效商品，可以用 `onExpr` 显式写完整 ON 条件：

```java
@JoinOption(
        entityClass = Product.class,
        alias = E_Product.ALIAS,
        onExpr = E_Product.ALIAS + "." + E_Product.id
                + " = " + E_Order.ALIAS + "." + E_Order.product
                + " and " + E_Product.ALIAS + "." + E_Product.enable + " = true"
)
```

`onExpr` 是完整连接条件，写了以后框架不会再根据 `joinColumn`、`joinTargetColumn` 自动生成 ON 表达式。因此只在连接逻辑确实比较特殊时使用；普通关联优先使用 `joinColumn`、`joinTargetAlias`、`joinTargetColumn`，并继续用常量避免字段名写错。

## 14. 子查询

### 14.1 子查询选择列

需求：查询用户，同时返回用户任务数。

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = UserTaskSummary.class)
@Data
public class QueryUserTaskSummaryReq {

    Paging paging = new PagingQueryReq(1, 20);

    @Select
    String name;

    @Select(
            value = "select count(*) from " + E_Task.CLASS_NAME
                    + " where " + E_Task.user + " = u.id",
            alias = "taskCnt"
    )
    Integer taskCnt;
}
```

大致生成：

```sql
select u.name,
       (select count(*) from Task where user = u.id) as taskCnt
from User u
```

### 14.2 子查询条件

需求：只查询任务数大于 0 的用户。

```java
@Gt(value = "(select count(*) from " + E_Task.CLASS_NAME
        + " where " + E_Task.user + " = u.id)")
Integer minTaskCnt = 0;
```

生成：

```sql
where (select count(*) from Task where user = u.id) > ?
```

## 15. JSON Path 例子

当前支持两种 JSON Path 写法：

- 注解式：在 `@Contains`、`@Select`、`@Update` 等注解上声明 `jsonPath`。
- 编程式：链式 API 直接调用 `jsonEq`、`jsonContains`、`jsonExists`、`jsonSelect`、`jsonSet`、`jsonArrayAppend`。

### 15.1 JSON 数组包含查询

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class QueryUserByRoleReq {

    @Contains(value = "roleList", jsonPath = "$[*]")
    String role;
}
```

调用：

```java
List<User> admins = dao.findByQueryObj(
        User.class,
        new QueryUserByRoleReq().setRole("admin")
);
```

含义：在 `roleList` JSON 数组中匹配包含 `admin` 的元素。

### 15.2 JSON 字段存在性查询

```java
@Where(op = Op.Exists, value = "logs", jsonPath = "$[0].logText")
Boolean hasFirstLog = true;
```

含义：只查询第一条日志存在 `logText` 的用户。

### 15.3 JSON 字段选择

```java
@Select(value = "logs", jsonPath = "$", alias = "logsJson")
String logsJson;

@Select(value = "logs", jsonPath = "$[0].logText", alias = "firstLogText")
String firstLogText;
```

适合列表页只展示 JSON 中的一小部分内容，不把整个实体加载到业务层再拆。

### 15.4 JSON 数组追加

JSON 数组追加本质上是更新场景，推荐看 `10.5 JSON 数组追加`。这里再给一个完整请求对象形态：

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class AppendRoleReq {

    @Eq(value = "tenantId", require = true)
    String tenantId;

    @Eq(value = "id", require = true)
    Long userId;

    @Update(value = "roleList", incrementMode = true)
    String roleCode;
}
```

调用：

```java
dao.uniqueUpdateByQueryObj(
        new AppendRoleReq()
                .setTenantId("T001")
                .setUserId(1L)
                .setRoleCode("R_MANAGER")
);
```

说明：

- `incrementMode = true` 让集合字段走追加语义，而不是整体覆盖。
- 对集合或数组字段，框架会生成类似 `json_array_append(...)` 的表达式。
- 不要在追加场景写 `jsonPath = "$[*]"`；这个 wildcard 路径在 `@Update` 中会被拒绝。
- 如果你要替换数组中某个固定位置的对象属性，用明确路径，例如 `jsonPath = "$[0].logText"`；如果你要追加新元素，不写 `jsonPath`。

### 15.5 编程式 JSON Path API

如果不想创建 DTO，也可以直接使用链式 API。这个能力适合临时查询、服务内部动态拼装条件、少量 JSON 字段更新等场景。

方法分布：

| 方法 | 所属接口 | 适用场景 | 大致表达 |
| --- | --- | --- | --- |
| `jsonEq(field, path, value)` | `SimpleConditionBuilder` | JSON 子字段等值查询。 | `json_value(str(field), path) = ?` |
| `jsonContains(field, path, keyword)` | `SimpleConditionBuilder` | JSON 子字段或数组内容包含查询。 | `json_value/json_query(...) like ?` |
| `jsonExists(field, path)` | `SimpleConditionBuilder` | 判断 JSON 路径是否存在。 | `json_exists(str(field), path)` |
| `jsonNotExists(field, path)` | `SimpleConditionBuilder` | 判断 JSON 路径不存在。 | `not json_exists(str(field), path)` |
| `jsonSelect(field, path, alias)` | `SelectBuilder` | 通用选择 JSON 子字段，标量/对象/数组都尽量可用。 | `COALESCE(json_query(...), json_value(...)) as alias` |
| `jsonValueSelect(field, path, alias)` | `SelectBuilder` | 明确选择 JSON 标量，例如字符串、数字、状态。 | `json_value(str(field), path) as alias` |
| `jsonQuerySelect(field, path, alias)` | `SelectBuilder` | 明确选择 JSON 对象或数组。 | `json_query(str(field), path) as alias` |
| `jsonObjectSelect(alias, entries...)` | `SelectBuilder` | 构造 JSON 对象。 | `json_object(...) as alias` |
| `jsonArraySelect(alias, values...)` | `SelectBuilder` | 构造 JSON 数组。 | `json_array(...) as alias` |
| `jsonArrayAggSelect(valueExpr, alias, clauses...)` | `SelectBuilder` | 聚合为 JSON 数组。 | `json_arrayagg(...) as alias` |
| `jsonObjectAggSelect(keyExpr, valueExpr, alias, clauses...)` | `SelectBuilder` | 聚合为 JSON 对象。 | `json_objectagg(...) as alias` |
| `jsonSet(field, path, value)` | `UpdateBuilder` | 新增或替换 JSON 中某个明确路径的值。 | `field = json_set(field, path, ?)` |
| `jsonReplace(field, path, value)` | `UpdateBuilder` | 仅替换已存在的 JSON 路径。 | `field = json_replace(field, path, ?)` |
| `jsonInsert(field, path, value)` | `UpdateBuilder` | 仅在 JSON 路径不存在时插入。 | `field = json_insert(field, path, ?)` |
| `jsonRemove(field, paths...)` | `UpdateBuilder` | 删除一个或多个明确 JSON 路径。 | `field = json_remove(field, path...)` |
| `jsonMergePatch(field, patch)` | `UpdateBuilder` | 使用 RFC 7396 merge patch 合并 JSON。 | `field = json_mergepatch(field, ?)` |
| `jsonArrayAppend(field, value)` | `UpdateBuilder` | 向 JSON 数组根路径追加元素。 | `field = json_array_append(field, '$', ?)` |
| `jsonArrayAppend(field, path, value)` | `UpdateBuilder` | 向 JSON 中指定数组路径追加元素。 | `field = json_array_append(field, path, ?)` |
| `jsonArrayInsert(field, path, value)` | `UpdateBuilder` | 向 JSON 数组指定位置插入元素。 | `field = json_array_insert(field, path, ?)` |

JSON 条件查询：

```java
List<User> users = dao.selectFrom(User.class, "u")
        .jsonEq("logs", "$[0].logText", "created")
        .jsonContains("roleList", "$[*]", "admin")
        .jsonExists("logs", "$[0].logText")
        .jsonNotExists("profile", "$.deletedAt")
        .find(User.class);
```

大致语义：

```sql
where json_value(str(u.logs), '$[0].logText') = ?
  and json_query(str(u.roleList), '$[*]') like ?
  and json_exists(str(u.logs), '$[0].logText')
  and not json_exists(str(u.profile), '$.deletedAt')
```

JSON 子字段选择：

```java
List<UserInfo> list = dao.selectFrom(User.class, "u")
        .jsonSelect("logs", "$[0].logText", "firstLogText")
        .jsonValueSelect("profile", "$.age", "age", "returning Integer", "null on error")
        .jsonQuerySelect("profile", "$.address", "addressJson")
        .find(UserInfo.class);
```

大致语义：

```sql
select COALESCE(
           json_query(str(u.logs), '$[0].logText'),
           json_value(str(u.logs), '$[0].logText')
       ) as firstLogText,
       json_value(str(u.profile), '$.age' returning Integer null on error) as age,
       json_query(str(u.profile), '$.address') as addressJson
```

`jsonSelect` 和 `jsonValueSelect` 的区别：

- `jsonSelect` 是通用兜底版，适合不确定路径结果是对象、数组还是标量的场景。
- `jsonValueSelect` 是明确标量版，适合确定取字符串、数字、状态、时间等单值，并且需要 `returning`、`null on error` 等 Hibernate JSON 子句的场景。
- 如果明确取对象或数组，用 `jsonQuerySelect`，表达更直接。

JSON 构造和聚合选择：

```java
List<UserSummary> list = dao.selectFrom(User.class, "u")
        .jsonObjectSelect("userJson",
                "'name' value u.name",
                "'roles' value u.roleList")
        .jsonArraySelect("userArray", "u.name", "u.mobile")
        .jsonArrayAggSelect("u.name", "nameList", "order by u.name")
        .jsonObjectAggSelect("u.status", "u.id", "statusUserMap")
        .find(UserSummary.class);
```

这类方法适合报表、统计、接口聚合返回等场景。如果表达式已经很复杂，直接用 `selectByStatement(...)` 会更清楚。

JSON 子字段更新：

```java
dao.updateTo(User.class, "u")
        .jsonSet("logs", "$[0].logText", "updated")
        .jsonReplace("profile", "$.nickName", "newName")
        .jsonInsert("profile", "$.createdBy", "system")
        .jsonRemove("profile", "$.temp")
        .jsonMergePatch("profile", "{\"vip\":true}")
        .eq("id", userId)
        .limit(0, 1)
        .update();
```

大致语义：

```sql
update User u
   set u.logs = json_set(u.logs, '$[0].logText', ?),
       u.profile = json_replace(u.profile, '$.nickName', ?),
       u.profile = json_insert(u.profile, '$.createdBy', ?),
       u.profile = json_remove(u.profile, '$.temp'),
       u.profile = json_mergepatch(u.profile, ?)
 where u.id = ?
```

说明：

- `jsonSet`：路径不存在时新增，路径存在时替换。
- `jsonReplace`：只替换已存在路径，路径不存在时不新增。
- `jsonInsert`：只在路径不存在时插入，路径已存在时不覆盖。
- `jsonRemove`：删除一个或多个明确路径。
- `jsonMergePatch`：适合一次性合并多个 JSON 字段，patch 文档建议使用 JSON 字符串、Map 或简单 DTO。

JSON 数组追加：

```java
dao.updateTo(User.class, "u")
        .jsonArrayAppend("roleList", "R_MANAGER")
        .eq("id", userId)
        .limit(0, 1)
        .update();
```

大致语义：

```sql
update User u
   set u.roleList = json_array_append(
           COALESCE(u.roleList, json_array()),
           '$',
           ?
       )
 where u.id = ?
```

如果 JSON 数组不在根路径，可以显式传入数组路径：

```java
dao.updateTo(User.class, "u")
        .jsonArrayAppend("profile", "$.roles", "R_MANAGER")
        .eq("id", userId)
        .limit(0, 1)
        .update();
```

如果需要插入到数组指定位置，用 `jsonArrayInsert`：

```java
dao.updateTo(User.class, "u")
        .jsonArrayInsert("roleList", "$[0]", "R_OWNER")
        .eq("id", userId)
        .limit(0, 1)
        .update();
```

这些方法分别放在不同的链式接口里：

- `jsonEq`、`jsonContains`、`jsonExists`、`jsonNotExists`：条件构建器，查询、更新、删除都可以作为条件使用。
- `jsonSelect`、`jsonValueSelect`、`jsonQuerySelect`、`jsonObjectSelect`、`jsonArraySelect`、`jsonArrayAggSelect`、`jsonObjectAggSelect`：选择字段构建器，只用于查询。
- `jsonSet`、`jsonReplace`、`jsonInsert`、`jsonRemove`、`jsonMergePatch`、`jsonArrayAppend`、`jsonArrayInsert`：更新字段构建器，只用于更新。

使用建议：

- 如果 JSON 条件是接口入参的一部分，优先写 DTO 注解，便于复用和生成接口说明。
- 如果 JSON 条件只在某个服务方法里临时使用，优先用编程式 JSON API，不要手写 `json_value(...)`。
- 更新场景仍然建议配合 `eq("id", id)`、租户条件、乐观锁或 `limit(0, 1)` 使用，避免误更新。
- 编程式 API 复用了 `JsonExprSupport` 和 `JsonPathSpec`，和注解式 JSON Path 生成规则保持一致。
- `json_table(...)`、自定义 JSON 函数、复杂嵌套表达式这类不够通用的语句，本手册不建议封装成链式方法；可以用 `JsonExprSupport` 生成表达式，再交给 `selectByStatement(...)`、`where(...)` 或 `setByStatement(...)`。

### 15.6 JSON 使用限制

仓库中的测试说明了几个边界：

- `@Select` 和 `jsonSelect` 可以选择 JSON 根路径或具体路径。
- `@Contains` 和 `jsonContains` 可以对 JSON 数组使用通配路径。
- `@Update`、`jsonSet`、`jsonReplace`、`jsonInsert`、`jsonRemove`、`jsonArrayAppend`、`jsonArrayInsert` 更新具体 JSON 路径时应使用明确路径，例如 `$[0].logText`。
- `@Update(incrementMode = true)` 和 JSON 数组追加、插入方法不要使用 wildcard 路径。
- 统计注解和增量更新不应随意使用通配 JSON Path，例如 `$[*]`，框架会拒绝部分不安全组合。

可参考：

- 注解式 JSON Path 测试：`simple-dao-examples/src/test/java/com/levin/commons/dao/DaoExamplesTest.java`
- 编程式 JSON Path 测试：`simple-dao-core/src/test/java/com/levin/commons/dao/support/JsonProgrammaticBuilderTest.java`

## 16. Repository 代理例子

如果想把 DAO 能力包装成更像 Repository 的接口，可以使用 `@EntityRepository`、`@QueryRequest`、`@UpdateRequest`、`@DeleteRequest`。

```java
@EntityRepository("用户DAO")
@TargetOption(entityClass = User.class, alias = "u")
public interface UserDao {

    List<User> find(@Eq Long id,
                    @Contains String name,
                    @Gt Integer score,
                    Paging paging);

    @QueryRequest(joinFetchSetAttrs = {"group"})
    User findOne(@Eq Long id,
                 @Contains String name,
                 @Eq String category,
                 Paging paging);

    @UpdateRequest
    int update(@Eq Long id, @Update String name);

    @DeleteRequest
    int delete(@OR @Eq Long id, String name);
}
```

说明：

- 方法参数上的注解会被解析成查询、更新或删除条件。
- 方法上没有 `@QueryRequest`、`@UpdateRequest`、`@DeleteRequest` 时，默认行为要看代理配置，不建议依赖隐式规则。
- 复杂业务仍建议定义清晰的请求 DTO，Repository 方法适合轻量封装。

## 17. 避免 N + 1 查询

### 17.1 关闭 Open In View

```yaml
spring:
  jpa:
    open-in-view: false
```

这样可以避免视图层在事务外隐式触发懒加载。

### 17.2 链式 `joinFetch`

```java
List<User> users = dao.selectFrom(User.class, "u")
        .joinFetch("group")
        .joinFetch("group.children")
        .find(User.class);
```

### 17.3 结果 DTO 上使用抓取注解

```java
@Data
public class UserInfo {

    @Fetch
    Group group;

    @Fetch(value = "group.name", isBindToField = true)
    String groupName;
}
```

使用建议：

- 列表页优先返回 DTO，不直接返回复杂实体图。
- 必须展示关联对象时，明确 `joinFetch` 或 `@Fetch`。
- 不要靠前端序列化实体时触发懒加载。

## 18. 安全模式与数据范围

Simple DAO 默认强调安全模式，尤其是更新和删除。

### 18.1 不要无条件更新/删除

推荐：

```java
@Eq(require = true)
Long id;

@Update
String name;
```

如果接口有租户或组织隔离条件，也要把这些条件标记成必须生效：

```java
@Eq(value = "tenantId", require = true)
String tenantId;

@Eq(value = "orgId", require = true)
String orgId;

@Eq(value = "id", require = true)
Long id;
```

这样当 `tenantId`、`orgId` 或 `id` 为空时，注解条件不会被静默忽略，而是直接抛出异常。这个策略比“先让空值不生成条件，再靠人工检查 SQL”更可靠。

谨慎使用：

```java
dao.deleteFrom(User.class)
        .disableSafeMode()
        .delete();
```

禁用安全模式只适合本地维护脚本或明确受控的批处理，业务接口里不建议使用。

### 18.2 业务接口不要只传裸 ID

如果业务数据有租户、组织、个人归属，查询、修改、删除请求对象应承载这些上下文。不要只写：

```java
public void delete(Long id) {
    dao.deleteById(Order.class, id);
}
```

更推荐：

```java
@TargetOption(entityClass = Order.class, alias = "o")
@Data
@Accessors(chain = true)
public class DeleteOrderReq extends MultiTenantOrgReq<DeleteOrderReq> {

    @Eq(require = true)
    Long id;
}
```

这样租户、组织等数据范围可以前置到查询条件中，减少越权数据被加载后再判断的风险。

## 19. 代码生成工作流

`simple-dao-codegen` 是 Maven 插件，主要目标包括：

| Goal | 作用 |
| --- | --- |
| `gen-demo-project-template` | 生成示例项目模板。 |
| `gen-code` | 扫描编译后的实体类，生成默认服务、默认控制器、请求对象等。 |
| `gen-project-entity-form-db` | 根据数据库表生成项目实体。 |
| `copy-template` | 拷贝模板资源，例如开发环境 docker-compose 模板。 |

### 19.1 插件配置示例

```xml
<plugin>
    <groupId>com.levin.commons</groupId>
    <artifactId>simple-dao-codegen</artifactId>
    <version>4.3.0-SNAPSHOT</version>
    <configuration>
        <isCreateControllerSubDir>true</isCreateControllerSubDir>
        <isCreateBizController>true</isCreateBizController>
        <isSchemaDescUseConstRef>true</isSchemaDescUseConstRef>
        <enableOakBaseFramework>false</enableOakBaseFramework>
        <enableDubbo>false</enableDubbo>
    </configuration>
</plugin>
```

### 19.2 实体变更后的标准顺序

只要新增或修改实体类，按这个顺序执行：

1. 修改实体类。
2. 进入实体模块。
3. 单独编译实体模块，确保实体类编译通过。
4. 在实体模块执行 `simple-dao-codegen:gen-code`。
5. 检查默认服务类、默认控制器类、请求对象是否生成或刷新。
6. 业务扩展写到 Biz 服务或 Biz 控制器，不直接改默认生成类。

命令形态：

```bash
cd <实体模块目录>
mvn compile
mvn com.levin.commons:simple-dao-codegen:4.3.0-SNAPSHOT:gen-code
```

如果通过 JitPack 坐标使用插件，`groupId` 需要按项目实际配置调整。

### 19.3 生成文件边界

如果某个目录中存在 `code-gen.md`，表示该目录及子目录由代码生成器维护：

- 不要手改这些文件。
- 不要在这些目录下新增文件。
- 实体变更后应重新执行 `gen-code`。

默认服务和默认控制器也属于生成边界：

- 默认服务类：类名通常是 `<实体名>Service`。
- 默认控制器类：类名通常是 `<实体名>Controller`。
- 业务服务类：类名通常是 `Biz<实体名>Service`，接口放 `services` 模块的 `biz` 目录，实现放 `services-impl` 模块的 `biz` 目录。
- 业务控制器类：类名通常是 `Biz<实体名>Controller`。

### 19.4 生成项目模板

在一个空 Maven 项目中配置好插件后，可以执行：

```bash
mvn com.levin.commons:simple-dao-codegen:4.3.0-SNAPSHOT:gen-demo-project-template
```

插件会生成示例实体模块、基础目录、模板说明等。之后刷新 Maven 项目，再按“编译实体模块 -> gen-code”的顺序继续。

## 20. 实体类开发建议

代码生成模板中对实体类有比较明确的约束，常用规则如下。

推荐形态：

```java
@Data
@EqualsAndHashCode(of = {"id"})
@Accessors(chain = true)
@FieldNameConstants
@Schema(title = "区域")
@Entity(name = EntityConst.PREFIX + "area")
@Table(indexes = {
        @Index(columnList = AbstractBaseEntityObject.Fields.orderCode),
        @Index(columnList = AbstractNamedEntityObject.Fields.name)
})
@EntityCategory(EntityOpConst.BIZ_TYPE_NAME)
public class Area extends AbstractNamedEntityObject {

    @Schema(title = "区域编码")
    @Column(nullable = false, length = 64)
    String code;
}
```

注意：

- 表名通过 `@Entity(name = ...)` 设置，不建议通过 `@Table(name = ...)` 设置。
- 字段名通常由命名策略转换成物理列名，不建议在 `@Column(name = "...")` 中硬编码列名。
- 关联字段的 `@JoinColumn(name = "...")` 应写 Java 字段名，并按项目规范设置 `insertable`、`updatable`。
- 枚举字段应使用 `@Enumerated(EnumType.STRING)`，数据库存枚举常量名，不存 ordinal。

## 21. 常见业务场景速查

### 21.1 列表页查询

用法组合：

- `@TargetOption(entityClass = Xxx.class, resultClass = XxxInfo.class)`
- `Paging paging = new PagingQueryReq(pageIndex, pageSize)`
- 字符串搜索用 `@Contains`
- 枚举/状态多选用 `@In`
- 时间范围用 `@Gte` + `@Lte` 或 `@Between`
- 排序用 `@OrderBy`

### 21.2 详情页查询

用法组合：

- `@Eq(require = true) Long id`
- 租户/组织/个人字段放在请求对象基类或查询条件中。
- 返回 DTO 用 `@Select` 控制字段。
- 需要关联字段时用 `joinFetch`、`@Fetch` 或 `@JoinOption`。

### 21.3 状态变更

用法组合：

- `@Eq(require = true) Long id`
- 多租户或组织数据加 `@Eq(value = "tenantId", require = true)`、`@Eq(value = "orgId", require = true)`。
- `@Update String state`
- 乐观锁字段用 `@Eq` 做条件、`@Update(incrementMode = true)` 做版本号 +1。
- 普通可选命中用 `singleUpdateByQueryObj`；必须命中一条的状态流转用 `uniqueUpdateByQueryObj`。

### 21.4 批量修改

用法组合：

- `@In("id") Long[] ids`
- `@Update` 标记要改的字段。
- 谨慎设置 `maxResults` 或额外数据范围条件。
- 不要禁用安全模式。

### 21.5 报表统计

用法组合：

- `@GroupBy` 定义维度。
- `@Count`、`@Sum`、`@Avg`、`@Min`、`@Max` 定义指标。
- `havingOp = Op.Gt` 等定义指标过滤。
- `@OrderBy(scope = OrderBy.Scope.OnlyForGroupBy)` 或统计注解里的 `orderBy` 定义排序。
- 多表维度用 `@JoinOption`。
- 条件数量统计：`@Sum(fieldCases = @Case(... thenExpr = "1", elseExpr = "0"))`。
- 条件金额统计：`@Sum(fieldCases = @Case(... thenExpr = "F$:amount", elseExpr = "0"))`。
- 支付时间判断金额：`@Case(column = "", whenExpr = "F$:payTime IS NULL", thenExpr = "0", elseExpr = "F$:amount")`。
- 固定结果结构优先用注解 DTO；动态周期、动态枚举、动态统计列可用 `Case + selectByStatement`。
- 编程式统计仍建议从 `simpleDao.forSelect(req)` 开始，让租户、组织、权限、基础查询条件继续生效。

### 21.6 JSON 字段筛选

用法组合：

- JSON 数组包含：`@Contains(value = "roleList", jsonPath = "$[*]")`
- JSON 路径存在：`@Where(op = Op.Exists, value = "logs", jsonPath = "$[0].logText")`
- JSON 子字段返回：`@Select(value = "logs", jsonPath = "$[0].logText", alias = "firstLogText")`
- JSON 子字段更新：`@Update(value = "logs", jsonPath = "$[0].logText")`
- JSON 数组追加：`@Update(value = "roleList", incrementMode = true)`，不要写 `jsonPath = "$[*]"`。
- 编程式查询：`jsonEq("logs", "$[0].logText", value)`、`jsonContains("roleList", "$[*]", "admin")`、`jsonExists("logs", "$[0].logText")`
- 编程式选择：`jsonSelect("logs", "$[0].logText", "firstLogText")`
- 编程式更新：`jsonSet("logs", "$[0].logText", value)`、`jsonArrayAppend("roleList", "admin")`

## 22. 常见问题排查

### 22.1 DTO 字段没有生成条件

检查：

- 字段值是否为 `null` 或空字符串。
- 注解 `condition` 是否返回 `false`。
- 字段是否被 `@Ignore`、`static`、`final`、`transient` 忽略。
- 目标字段名是否写错，尤其是 DTO 字段名和实体字段名不一致时。

### 22.2 更新没有执行

检查：

- 是否有至少一个 `@Update` 字段非空。
- 是否缺少必要条件，安全模式是否阻止了无条件更新。
- 是否应该调用 `singleUpdateByQueryObj` 或 `uniqueUpdateByQueryObj` 来暴露异常。
- `@Update(condition = ...)` 是否导致字段被忽略。

### 22.3 多表关联生成不符合预期

检查：

- 主表和关联表是否都设置了别名。
- 实体关系是否唯一，是否需要手写 `joinColumn`、`joinTargetColumn` 或 `onExpr`。
- `domain` 是否写成了正确的别名。
- 原生 SQL 和 JPQL 模式下字段表达式是否不同。

### 22.4 代码生成没有更新文件

检查：

- 是否先编译了实体模块。
- 是否在实体模块目录执行 `gen-code`。
- 目标目录是否有 `code-gen.md` 保护说明。
- 生成器是否因为文件校验码不匹配、文件被手改而跳过覆盖。
- 实体注解、包名、模块名是否符合模板约定。

### 22.5 JSON Path 报错

检查：

- 路径是否以 `$` 开始。
- 更新和统计是否使用了不支持的通配路径。
- 当前数据库方言是否支持生成器使用的 JSON 函数。
- 测试里已有的 JSON Path 用法是否覆盖你的场景。

## 23. 推荐阅读源码路径

| 想了解 | 建议文件 |
| --- | --- |
| 查询、更新、删除入口 | `simple-dao-core/src/main/java/com/levin/commons/dao/SimpleDao.java` |
| 链式查询 API | `simple-dao-core/src/main/java/com/levin/commons/dao/SelectDao.java` |
| 条件构建 API | `simple-dao-core/src/main/java/com/levin/commons/dao/ConditionBuilder.java` |
| 更新 API | `simple-dao-core/src/main/java/com/levin/commons/dao/UpdateDao.java` |
| 删除 API | `simple-dao-core/src/main/java/com/levin/commons/dao/DeleteDao.java` |
| 目标实体配置 | `simple-dao-annotations/src/main/java/com/levin/commons/dao/TargetOption.java` |
| 多表连接配置 | `simple-dao-annotations/src/main/java/com/levin/commons/dao/JoinOption.java` |
| 注解到语句的解析 | `simple-dao-core/src/main/java/com/levin/commons/dao/util/QueryAnnotationUtil.java` |
| JSON Path 支持 | `simple-dao-core/src/main/java/com/levin/commons/dao/support/JsonPathSpec.java` |
| JPA 自动配置 | `simple-dao-jpa/src/main/java/com/levin/commons/dao/starter/JpaDaoConfiguration.java` |
| 代码生成插件 | `simple-dao-code-gen/src/main/java/com/levin/commons/dao/codegen/plugins/CodeGeneratorMojo.java` |
| 综合示例测试 | `simple-dao-examples/src/test/java/com/levin/commons/dao/DaoExamplesTest.java` |

## 24. 新增功能时的建议流程

完整开发和验证规则见：[docs/project-development-rules.md](./docs/project-development-rules.md)。下面是手册里的简版流程。

1. 先判断是不是普通 CRUD、列表筛选、状态变更或统计报表。
2. 能用现有默认服务和生成请求对象解决的，优先复用。
3. 需要扩展条件时，优先定义新的 DTO/Req，并使用 Simple DAO 注解表达。
4. 需要新增业务方法时，写在 Biz 服务或 Biz 控制器中，不改默认生成类。
5. 只有 DTO 注解和链式 API 都无法表达时，再考虑 DAO 扩展或手写 SQL。
6. 涉及实体变更时，先编译实体模块，再执行 `gen-code`。
7. 给业务服务补测试，至少覆盖正常路径、空条件、安全条件、边界条件。
8. 这是公共组件，凡是改动 DAO 行为、注解解析、查询/更新语句生成、JPA 实现、JSON 支持或公共 API，都必须运行综合示例测试：

```bash
mvn -pl simple-dao-examples -am -Dtest=DaoExamplesTest -Dsurefire.failIfNoSpecifiedTests=false test -P '!01-跳过测试'
```

`DaoExamplesTest` 位于 `simple-dao-examples/src/test/java/com/levin/commons/dao/DaoExamplesTest.java`，应视为项目的端到端使用契约测试。文档-only 变更可以不跑；但如果同一个工作会话里已经包含代码变更，完成前要跑这个测试，并在结果里说明是否通过。

用一句话总结：Simple DAO 的最佳实践是“实体描述数据结构，DTO 描述查询/更新意图，业务服务编排流程，生成代码提供默认 CRUD 能力”。
