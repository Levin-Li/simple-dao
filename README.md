# Simple DAO

[![JitPack](https://www.jitpack.io/v/Levin-Li/simple-dao.svg)](https://www.jitpack.io/#Levin-Li/simple-dao)

**Simple DAO 是一个面向 Java 业务系统的 DAO 增强框架：把查询、分页、排序、更新、删除、统计、多表关联和代码生成都收敛到“请求 DTO + 注解”里。**

一句话：**你写请求对象，Simple DAO 负责把它变成可执行的 JPA/原生 SQL 查询；你保留 SQL 思维，但不用反复写样板代码。**

如果你的项目里经常出现这些场景，Simple DAO 会非常有吸引力：

- 列表页一个接口十几个筛选条件，`if (xxx != null)` 拼半天。
- 为了一个查询 DTO，又写 Mapper XML、Repository 方法、Service 转换、分页包装。
- 后台管理模块里重复写“实体 CRUD + 查询请求 + 更新请求 + Controller”。
- 报表统计里到处散落 `group by`、`having`、`case when`。
- 多租户/组织/个人数据范围条件靠人工记得加，一漏就是数据泄露。

Simple DAO 的核心价值不是“换一个 ORM”，而是把业务系统里最常见、最烦、最容易写错的 DAO 样板工作标准化：

- **更快交付**：普通列表、分页、详情、更新、统计接口可以少写大量 Mapper/Repository/Service 拼装代码。
- **更好维护**：接口参数和查询逻辑在同一个 DTO 上，读代码时不用在 Controller、Service、Mapper、XML 之间来回跳。
- **更安全**：关键条件可以声明为必填，避免空条件被忽略后查全表、改全表、删全表。
- **更适合后台系统**：CRUD、筛选、分页、统计、数据权限、代码生成都是中后台项目的高频工作。

## 完整操作手册

想系统了解分页查询、唯一查询、唯一更新、JSON 数组追加、`condition` 内置变量、DAO 注解默认策略、防数据泄露、统计报表、原生查询和 JPA 查询，请直接阅读：

**[Simple DAO 完整操作手册](./manual.md)**

## 30 秒看懂

把查询条件写进 DTO：

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = UserInfo.class)
@Data
@Accessors(chain = true)
public class QueryUserReq {

    Paging paging = new PagingQueryReq(1, 20);

    @Contains(value = "name")
    String keyword;

    @Gte("score")
    Integer minScore;

    @Lte("score")
    Integer maxScore;

    @In("state")
    String[] states;

    @OrderBy(type = OrderBy.Type.Desc)
    LocalDateTime createTime;
}
```

服务层只做一件事：

```java
PagingData<UserInfo> page = dao.findPagingDataByQueryObj(
        UserInfo.class,
        new QueryUserReq()
                .setKeyword("Echo")
                .setMinScore(60)
                .setMaxScore(100)
                .setStates(new String[]{"A", "B"})
);
```

这个 DTO 大致表达：

```sql
where name like ?
  and score >= ?
  and score <= ?
  and state in (?, ?)
order by createTime desc
limit ?, ?
```

更复杂的统计也可以写进 DTO：

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = UserStat.class)
@Data
public class UserStatReq {

    @GroupBy
    String state;

    @Count(alias = "userCnt")
    Long userCnt;

    @Avg(value = "score", havingOp = Op.Gt, alias = "avgScore")
    Integer avgScore = 80;
}
```

## 第一眼亮点

### 1. 一个 DTO 就能表达完整查询

传统写法通常是“请求 DTO + Service 判断 + Mapper/Repository 查询 + 结果转换”。Simple DAO 让 DTO 自己表达查询意图：

```java
@Contains("name")
String keyword;

@Gte("createTime")
Date beginTime;

@Lte("createTime")
Date endTime;

@OrderBy(type = OrderBy.Type.Desc)
Date createTime;
```

这几行就能表达名称模糊搜索、时间范围过滤和倒序排序。读者看到 DTO，就能立刻知道接口支持什么条件。

### 2. 查询 DTO 就是接口契约

在很多项目里，请求对象只是“参数袋子”，真正的查询逻辑藏在 Mapper、Repository 或 Service 里。Simple DAO 把筛选、排序、分页、统计、字段选择直接写在 DTO 上：

- 前端看到 DTO 字段，就知道能传什么参数。
- 后端看到 DTO 注解，就知道会生成什么查询。
- 同一个 DTO 可以被 Controller、Service、测试和代码生成复用。

### 3. 少写 SQL，但不是失控的魔法

Simple DAO 不是让你完全不懂 SQL，而是把常见 SQL 结构映射成可读的 Java 注解：

- `@Contains` -> `like`
- `@Between` -> `between`
- `@In` -> `in`
- `@Update` -> `set`
- `@GroupBy`、`@Count`、`@Sum`、`@Avg` -> 统计报表
- `@JoinOption` -> 多表关联
- `@TargetOption(nativeQL = true)` -> 必要时切换原生 SQL

需要原生 SQL、JSON 函数或特殊优化时仍然可以写，只是普通业务查询不用每次都手搓。

### 4. 内置安全思路，减少“空条件变全表”的事故

多数条件注解默认会忽略空值，这对列表筛选很方便。但详情、删除、状态变更这类接口必须防止关键条件缺失。

Simple DAO 支持把关键条件声明成必填：

```java
@Eq(value = "tenantId", require = true)
String tenantId;

@Eq(value = "id", require = true)
Long id;
```

当 `tenantId` 或 `id` 为空时，框架会抛异常，而不是静默忽略条件。配合 DAO 安全模式，可以降低误查、误删、误更新和数据越权风险。

### 5. CRUD 可以生成，业务扩展保持清晰

`simple-dao-codegen` 可以基于实体生成默认服务、默认控制器、请求对象和项目模板。默认 CRUD 交给生成器，真正的业务方法写到 Biz 服务和 Biz 控制器里，避免每个实体都重复铺一套基础代码。

## 和常见方案对比

表格能说明定位，但不如代码直观。下面用几个业务系统里最常见的需求对比一下。

### 案例一：用户列表筛选 + 排序 + 分页

需求：按关键字模糊查询用户，按分数区间过滤，按状态过滤，按创建时间倒序分页。

Simple DAO 写法：查询条件直接长在请求 DTO 上。

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = UserInfo.class)
@Data
@Accessors(chain = true)
public class QueryUserReq {

    Paging paging = new PagingQueryReq(1, 20);

    @Contains("name")
    String keyword;

    @Gte("score")
    Integer minScore;

    @Lte("score")
    Integer maxScore;

    @In("state")
    String[] states;

    @OrderBy(type = OrderBy.Type.Desc)
    LocalDateTime createTime;
}
```

调用时基本没有查询拼装代码：

```java
PagingData<UserInfo> page = dao.findPagingDataByQueryObj(
        UserInfo.class,
        new QueryUserReq()
                .setKeyword("Echo")
                .setMinScore(60)
                .setMaxScore(100)
                .setStates(new String[]{"A", "B"})
);
```

如果用原生 JPA Criteria，通常会变成这样：

```java
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<User> cq = cb.createQuery(User.class);
Root<User> root = cq.from(User.class);
List<Predicate> predicates = new ArrayList<>();

if (StringUtils.hasText(req.getKeyword())) {
    predicates.add(cb.like(root.get("name"), "%" + req.getKeyword() + "%"));
}
if (req.getMinScore() != null) {
    predicates.add(cb.greaterThanOrEqualTo(root.get("score"), req.getMinScore()));
}
if (req.getMaxScore() != null) {
    predicates.add(cb.lessThanOrEqualTo(root.get("score"), req.getMaxScore()));
}
if (req.getStates() != null && req.getStates().length > 0) {
    predicates.add(root.get("state").in(Arrays.asList(req.getStates())));
}

cq.where(predicates.toArray(new Predicate[0]));
cq.orderBy(cb.desc(root.get("createTime")));

List<User> list = entityManager.createQuery(cq)
        .setFirstResult((pageNo - 1) * pageSize)
        .setMaxResults(pageSize)
        .getResultList();
```

这段代码并不难，但每个列表页都会重复出现：判空、加条件、排序、分页、count 查询、结果转换都要再写一遍。

如果用 MyBatis-Plus，Wrapper 会简洁一些：

```java
LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery(User.class)
        .like(StringUtils.hasText(req.getKeyword()), User::getName, req.getKeyword())
        .ge(req.getMinScore() != null, User::getScore, req.getMinScore())
        .le(req.getMaxScore() != null, User::getScore, req.getMaxScore())
        .in(req.getStates() != null && req.getStates().length > 0,
                User::getState,
                Arrays.asList(req.getStates()))
        .orderByDesc(User::getCreateTime);

IPage<User> page = userMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
```

MyBatis-Plus 的问题不是不能写，而是这些 Wrapper 代码通常散在 Service 里。请求 DTO 是一份，查询构造又是一份，接口一多，重复逻辑还是会膨胀。

如果用 QueryDSL，类型安全更好：

```java
QUser user = QUser.user;
BooleanBuilder builder = new BooleanBuilder();

if (StringUtils.hasText(req.getKeyword())) {
    builder.and(user.name.contains(req.getKeyword()));
}
if (req.getMinScore() != null) {
    builder.and(user.score.goe(req.getMinScore()));
}
if (req.getMaxScore() != null) {
    builder.and(user.score.loe(req.getMaxScore()));
}
if (req.getStates() != null && req.getStates().length > 0) {
    builder.and(user.state.in(req.getStates()));
}

List<User> list = queryFactory
        .selectFrom(user)
        .where(builder)
        .orderBy(user.createTime.desc())
        .offset((long) (pageNo - 1) * pageSize)
        .limit(pageSize)
        .fetch();
```

QueryDSL 很强，但对普通列表页来说，DTO 和查询构造仍然是两套代码。Simple DAO 更适合把“接口入参”和“查询规则”合在一起，让列表接口更短、更直观。

### 案例二：状态变更必须只更新一条

需求：订单状态从 `WAIT_PAY` 改为 `PAID`，必须带租户 ID、订单号和乐观锁；如果没命中或命中多条，都应该失败。

Simple DAO 写法：

```java
@TargetOption(entityClass = Order.class)
@Data
public class PayOrderReq {

    @Eq(value = "tenantId", require = true)
    String tenantId;

    @Eq(value = "orderNo", require = true)
    String orderNo;

    @Eq(value = "state", require = true)
    String oldState = "WAIT_PAY";

    @Eq(value = "optimisticLock", require = true)
    Integer optimisticLock;

    @Update("state")
    String newState = "PAID";

    @Update(value = "payTime", paramExpr = "now()")
    Boolean setPayTime = true;
}
```

调用：

```java
dao.uniqueUpdateByQueryObj(req);
```

这里的重点是语义清楚：`uniqueUpdateByQueryObj` 不是“尽量更新”，而是“必须刚好更新一条”。0 条或多条都会抛异常，适合支付、审核、状态流转这类敏感操作。

如果手写 JPQL 或 SQL，通常要自己约束语义：

```java
int rows = entityManager.createQuery("""
        update Order o
           set o.state = :newState,
               o.payTime = current_timestamp
         where o.tenantId = :tenantId
           and o.orderNo = :orderNo
           and o.state = :oldState
           and o.optimisticLock = :optimisticLock
        """)
        .setParameter("newState", "PAID")
        .setParameter("tenantId", req.getTenantId())
        .setParameter("orderNo", req.getOrderNo())
        .setParameter("oldState", "WAIT_PAY")
        .setParameter("optimisticLock", req.getOptimisticLock())
        .executeUpdate();

if (rows != 1) {
    throw new IllegalStateException("Order update must affect exactly one row.");
}
```

这当然也能做，但每个关键更新都要手动记得三件事：必填条件、影响行数检查、异常语义。Simple DAO 把这些变成固定模式。

### 案例三：统计报表不是只能手写 SQL

需求：按订单状态统计订单数、订单金额、成功订单数、成功金额。

Simple DAO 可以用统计注解描述：

```java
@TargetOption(entityClass = Order.class, alias = "o", resultClass = OrderStat.class)
@Data
public class OrderStatReq {

    @GroupBy
    String state;

    @Count(alias = "orderCnt")
    Long orderCnt;

    @Sum(value = "amount", alias = "totalAmount")
    BigDecimal totalAmount;

    @Sum(value = "state", alias = "successCnt", fieldCases = {
            @Case(elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'SUCCESS'", thenExpr = "1")
            })
    })
    Long successCnt;

    @Sum(value = "amount", alias = "successAmount", fieldCases = {
            @Case(column = "state", elseExpr = "0", whenOptions = {
                    @Case.When(whenExpr = "'SUCCESS'", thenExpr = "F$:amount")
            })
    })
    BigDecimal successAmount;
}
```

手写 SQL 通常是这样：

```sql
select state,
       count(*) as order_cnt,
       sum(amount) as total_amount,
       sum(case when state = 'SUCCESS' then 1 else 0 end) as success_cnt,
       sum(case when state = 'SUCCESS' then amount else 0 end) as success_amount
  from t_order
 group by state
```

SQL 本身没问题，但一旦要把筛选条件、租户条件、分页、having、结果 DTO 映射也统一起来，代码就会变散。Simple DAO 的价值是让报表 DTO 同时表达“入参、统计字段、分组、having 和结果结构”。

所以，Simple DAO 和这些框架不是简单替代关系：

- 遇到极致复杂 SQL，可以继续写原生 SQL。
- 已经在 JPA/Hibernate 项目里，可以直接增强动态查询能力。
- 已经用 MyBatis-Plus，也能把 Simple DAO 用在筛选、统计、代码生成更密集的模块。
- QueryDSL 适合复杂类型安全查询，Simple DAO 更适合业务接口快速落地和统一 DTO 约定。

Simple DAO 不试图替代所有数据库访问方案。它最适合：

- 中后台管理系统。
- 多实体 CRUD 密集型系统。
- 列表筛选和分页接口很多的业务项目。
- 有多租户、组织、个人数据范围要求的系统。
- 需要快速生成默认服务/控制器，再在业务层扩展的项目。

## 核心能力

- 查询：`@Eq`、`@Contains`、`@Gt`、`@Gte`、`@Lt`、`@Lte`、`@In`、`@Between`。
- 选择列：`@Select`，支持映射到结果 DTO。
- 分页：DTO 内嵌 `Paging` 或单独传入 `PagingQueryReq`。
- 排序：`@OrderBy`、`@SimpleOrderBy`。
- 更新：`@Update`、增量更新、唯一更新。
- 删除：按查询对象删除，支持安全模式和逻辑删除。
- 统计：`@Count`、`@Sum`、`@Avg`、`@Min`、`@Max`、`@GroupBy`、`havingOp`。
- 多表：`@JoinOption`、链式 `join`、JPA `joinFetch`。
- JSON：注解式 `jsonPath`，以及链式 `jsonEq`、`jsonLike`、`jsonExists`、`jsonSelect`、`jsonValueSelect`、`jsonSet`、`jsonReplace`、`jsonArrayAppend` 等。
- 查询模式：默认 JPA/JPQL，可切换原生 SQL。
- 代码生成：实体驱动生成默认服务、控制器、请求对象和项目模板。

## 安装

通过 JitPack 使用：

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

如果直接使用本仓库构件：

```xml
<dependency>
    <groupId>com.levin.commons</groupId>
    <artifactId>simple-dao-jpa-starter</artifactId>
    <version>4.3.0-SNAPSHOT</version>
</dependency>
```

Spring 服务中注入：

```java
@Service
public class UserService {

    @Autowired
    JpaDao dao;
}
```

## 常见使用方式

### 唯一查询

```java
OrderInfo info = dao.findUnique(OrderInfo.class, req);
```

`findUnique` 表示“最多一条”：没有记录返回 `null`，超过一条抛异常。

### 唯一更新

```java
dao.uniqueUpdateByQueryObj(req);
```

`uniqueUpdateByQueryObj` 表示“必须刚好更新一条”：0 条或多条都会抛异常。

### JSON 数组追加

```java
@Update(value = "roleList", incrementMode = true)
String role;
```

会生成类似 `json_array_append(...)` 的追加语义，适合角色、标签等数组字段。

编程式 API 也可以直接处理 JSON 查询、选择和更新：

```java
dao.selectFrom(User.class, "u")
        .jsonEq("profile", "$.level", "VIP")
        .jsonValueSelect("profile", "$.nickName", "nickName")
        .find(UserInfo.class);

dao.updateTo(User.class, "u")
        .jsonSet("profile", "$.level", "VIP")
        .jsonArrayAppend("roleList", "R_MANAGER")
        .eq("id", userId)
        .limit(0, 1)
        .update();
```

复杂且不通用的 JSON 表达式仍建议使用 `selectByStatement`、`where` 或 `setByStatement` 明确表达。

## JPA 查询和原生查询

默认使用 JPA/JPQL：

```java
dao.selectFrom(User.class, "u").find(User.class);
```

需要数据库原生能力时切换原生 SQL：

```java
dao.selectByNative(User.class, "u").find(User.class);
```

DTO 上也可以声明：

```java
@TargetOption(nativeQL = true, entityClass = User.class, alias = "u")
```

两种模式的差异见：[manual.md - 原生查询和 JPA 查询](./manual.md#71-原生查询和-jpa-查询)。

## 代码生成

`simple-dao-codegen` 是 Maven 插件，可以基于实体生成默认服务、默认控制器、请求对象和项目模板。

插件配置示例：

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

常用 goal：

| Goal | 作用 |
| --- | --- |
| `gen-demo-project-template` | 生成示例项目模板。 |
| `gen-code` | 扫描编译后的实体类，生成默认服务、默认控制器、请求对象等。 |
| `gen-project-entity-form-db` | 根据数据库表生成项目实体。 |
| `copy-template` | 拷贝模板资源，例如开发环境 docker-compose 模板。 |

实体变更后的标准顺序：

```bash
cd <实体模块目录>
mvn compile
mvn com.levin.commons:simple-dao-codegen:4.3.0-SNAPSHOT:gen-code
```

生成目录中如果存在 `code-gen.md`，表示该目录归生成器维护，不应手工修改。详细规则见：[manual.md - 代码生成工作流](./manual.md#17-代码生成工作流)。

## 项目结构

| 模块 | 说明 |
| --- | --- |
| `simple-dao-annotations` | DAO 注解定义。 |
| `simple-dao-core` | 核心 DAO API 和语句构建器。 |
| `simple-dao-jpa` | JPA/Hibernate 实现。 |
| `simple-dao-jpa-starter` | Spring Boot Starter。 |
| `simple-dao-code-gen` | Maven 插件和代码模板。 |
| `simple-dao-id-generator` | ID 生成支持。 |
| `simple-dao-examples` | 示例实体、DTO、Repository 和测试。 |
| `simple-dao-code-gen-example` | 代码生成示例项目。 |

## 开发命令

完整开发规则见：[项目开发规则](./docs/project-development-rules.md)。

```bash
# 编译全部模块
mvn compile

# 显式运行测试
mvn test -P '!01-跳过测试'

# DAO 行为或公共 API 变更后，必须运行综合示例测试
mvn -pl simple-dao-examples -am -Dtest=DaoExamplesTest -Dsurefire.failIfNoSpecifiedTests=false test -P '!01-跳过测试'

# 只运行示例模块及其依赖
mvn -pl simple-dao-examples -am test -P '!01-跳过测试'
```

`DaoExamplesTest` 是这个公共组件最重要的综合使用测试，覆盖大量真实注解、链式 API、Repository、JSON Path、统计等用法。凡是改动 DAO 行为、注解解析、查询/更新语句生成、JPA 实现、JSON 支持或公共 API，都要运行它；如果因为已有环境或生成代码问题无法通过，需要在提交说明或交付说明里明确写出失败原因。

## 文档

- 完整操作手册：[manual.md](./manual.md)
- 项目开发规则：[docs/project-development-rules.md](./docs/project-development-rules.md)
- 核心入口：[SimpleDao.java](./simple-dao-core/src/main/java/com/levin/commons/dao/SimpleDao.java)
- JPA 实现：[JpaDaoImpl.java](./simple-dao-jpa/src/main/java/com/levin/commons/dao/support/JpaDaoImpl.java)
- 代码生成插件：[CodeGeneratorMojo.java](./simple-dao-code-gen/src/main/java/com/levin/commons/dao/codegen/plugins/CodeGeneratorMojo.java)
- 综合示例测试：[DaoExamplesTest.java](./simple-dao-examples/src/test/java/com/levin/commons/dao/DaoExamplesTest.java)

## 联系方式

99668980@qq.com
