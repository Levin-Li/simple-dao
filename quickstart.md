# Simple DAO Quick Start

这份文档面向第一次接入 Simple DAO 的开发者。目标不是讲完所有能力，而是让你在一个 Spring Boot + JPA 项目里快速跑通：

1. 引入依赖。
2. 定义实体。
3. 用 DTO 表达查询条件。
4. 调用 DAO 返回分页数据。
5. 再看唯一查询、唯一更新和下一步阅读路径。

完整能力请看：[Simple DAO 完整操作手册](./manual.md)。

## 1. 引入依赖

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

如果是在同一个多模块工程里直接依赖本仓库构件：

```xml
<dependency>
    <groupId>com.levin.commons</groupId>
    <artifactId>simple-dao-jpa-starter</artifactId>
    <version>4.3.0-SNAPSHOT</version>
</dependency>
```

业务项目还需要正常引入数据库驱动和 Spring Data JPA 相关依赖。

## 2. 准备 JPA 配置

开发环境可以先用最小配置跑通：

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

- `open-in-view: false` 可以减少接口返回阶段触发懒加载的问题。
- `EntityNamingStrategy` 用于实体名和物理表名转换。
- `ddl-auto: update` 只适合开发环境；生产环境按项目规范管理表结构。

启动类一般只需要正常的 Spring Boot 配置。如果需要显式启用，也可以加：

```java
@SpringBootApplication
@EnableSimpleDao
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

## 3. 定义实体

先用一个最普通的用户实体：

```java
@Entity
@Data
public class User {

    @Id
    @GeneratedValue
    Long id;

    String name;

    Integer score;

    String state;

    LocalDateTime createTime;
}
```

生产项目里建议配合代码生成的 `E_User.xxx` 字段常量，避免手写字段名。Quick Start 为了直观，先直接写字符串字段名。

## 4. 写一个分页查询 DTO

需求：按关键字模糊查询用户，按分数区间过滤，按状态过滤，按创建时间倒序分页。

```java
@TargetOption(entityClass = User.class, alias = "u", resultClass = QueryUserReq.Result.class)
@Data
@Accessors(chain = true)
public class QueryUserReq {

    @Contains("name")
    String keyword;

    @Gte("score")
    Integer minScore;

    @Lte("score")
    Integer maxScore;

    @In("state")
    List<String> states;

    @OrderBy(type = OrderBy.Type.Desc)
    LocalDateTime createTime;

    @Data
    public static class Result {

        @Select
        Long id;

        @Select
        String name;

        @Select
        Integer score;

        @Select
        String state;
    }
}
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

几个规则先记住：

- 字段值为空时，大多数查询注解默认不生成条件，适合列表筛选。
- 查询对象和分页对象可以分开传入；框架会从 `Object... queryObjs` 里自动识别 `Paging` 参数。
- `resultClass` 指定返回 DTO；不指定时通常返回实体。
- 当注解目标字段名和当前属性名一致时，`value` 可以不填，例如 `id` 字段上只写 `@Select`，`createTime` 字段上只写 `@OrderBy(type = OrderBy.Type.Desc)`。
- 简单场景优先使用“DTO + 注解”，复杂补充条件再用 Consumer 或链式 API。

## 5. 在服务里调用

```java
@Service
public class UserService {

    @Autowired
    JpaDao dao;

    public PagingData<QueryUserReq.Result> queryUsers(QueryUserReq req, Paging paging) {
        return dao.findPagingDataByQueryObj(QueryUserReq.Result.class, req, paging);
    }
}
```

调用示例：

```java
PagingQueryReq paging = new PagingQueryReq(1, 20);
paging.setRequireTotals(true);

PagingData<QueryUserReq.Result> page = userService.queryUsers(
        new QueryUserReq()
                .setKeyword("Echo")
                .setMinScore(60)
                .setMaxScore(100)
                .setStates(Arrays.asList("A", "B")),
        paging
);
```

这里是两个参数：

- `QueryUserReq req`：普通查询条件，例如关键字、分数区间、状态集合、排序字段。
- `Paging paging`：分页条件，例如 `pageIndex = 1`、`pageSize = 20`、`requireTotals = true`。

`PagingQueryReq` 是框架提供的一个 `Paging` 实现类，把它作为 `queryObjs` 参数之一传进去，DAO 会自动识别并应用分页。

返回值 `PagingData` 通常包含：

- 当前页数据列表。
- 总记录数。
- 页码、页大小等分页信息。

更完整的分页写法见：[分页查询专题](./manual.md#56-分页查询专题pagingpagingdatapageoption)。

## 6. 查询一条记录

如果业务语义是“最多只允许一条”，用 `findUnique`：

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class QueryUserDetailReq {

    @Eq(value = "id", require = true)
    Long id;
}
```

```java
User user = dao.findUnique(new QueryUserDetailReq().setId(1L));
```

`findUnique` 的语义：

- 没有记录：返回 `null`。
- 刚好一条：返回这条记录。
- 超过一条：抛异常。

如果业务上要求“必须存在”，可以在服务层对 `null` 做业务异常处理。

更多说明见：[查询单条或唯一记录](./manual.md#54-查询单条或唯一记录)。

## 7. 更新一条记录

更新建议把关键条件声明为 `require = true`，避免空条件被忽略。

```java
@TargetOption(entityClass = User.class, alias = "u")
@Data
@Accessors(chain = true)
public class UpdateUserStateReq {

    @Eq(value = "id", require = true)
    Long id;

    @Update("state")
    String state;
}
```

普通更新：

```java
int n = dao.updateByQueryObj(new UpdateUserStateReq()
        .setId(1L)
        .setState("DISABLED"));
```

如果业务要求“必须刚好更新一条”，用唯一更新：

```java
dao.uniqueUpdateByQueryObj(new UpdateUserStateReq()
        .setId(1L)
        .setState("DISABLED"));
```

`uniqueUpdateByQueryObj` 的语义更强：

- 更新 0 条：抛异常。
- 更新 1 条：成功。
- 更新多条：抛异常。

更多说明见：[唯一更新](./manual.md#102-只允许更新一条)。

## 8. 混合注解和编程条件

大多数业务查询可以只靠 DTO 注解完成。如果某个接口需要临时追加复杂条件，可以用 Consumer 扩展 DAO：

```java
public PagingData<QueryUserReq.Result> queryUsers(QueryUserReq req) {

    Consumer<SelectDao<User>> callback = dao -> dao
            .or()
                .eq("state", "A")
                .eq("state", "B")
            .end();

    return dao.findPagingDataByQueryObj(QueryUserReq.Result.class, req, callback);
}
```

这表示：基础条件来自 `QueryUserReq` 注解，额外条件由 `callback` 补充。适合少量动态条件、调试条件或注解不方便表达的场景。

完整用法见：[Consumer 扩展 DAO](./manual.md#9-consumer-扩展-dao混合注解和编程式查询)。

## 9. 下一步看什么

按你的使用目标选择阅读路径：

- 只想写列表页：看 [查询对象实用例子](./manual.md#7-查询对象实用例子) 和 [分页查询专题](./manual.md#56-分页查询专题pagingpagingdatapageoption)。
- 要做详情、状态变更、删除：看 [查询单条或唯一记录](./manual.md#54-查询单条或唯一记录)、[更新例子](./manual.md#10-更新例子)、[删除例子](./manual.md#11-删除例子)。
- 要做报表：看 [统计查询例子](./manual.md#12-统计查询例子)。
- 要做多表：看 [多表查询例子](./manual.md#13-多表查询例子)。
- 要做 JSON 字段查询或更新：看 [JSON Path 例子](./manual.md#15-json-path-例子)。
- 要接入代码生成：看 [代码生成工作流](./manual.md#19-代码生成工作流)。

如果想直接看可运行用法，可以搜索：

- [DaoExamplesTest.java](./simple-dao-examples/src/test/java/com/levin/commons/dao/DaoExamplesTest.java)
- `simple-dao-examples/src/test/java/com/levin/commons/dao/dto`
