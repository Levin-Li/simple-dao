# Simple-DAO 项目说明文档

## 1. 项目概述

**Simple-DAO** 是一个使用注解自动生成 SQL 语句和参数的 Java DAO 组件。通过在 DTO 对象中加入自定义注解，可自动生成查询语句，实现不编写 SQL 即可完成数据库操作。

### 主要特性
- **注解驱动**：通过 DTO 注解自动生成 SQL
- **支持多种 SQL 子句**：Where、Group By、Having、Order By、Select、Update Set、子查询、逻辑删除等
- **基于 JPA/Hibernate**：当前基于 JPA/Hibernate
- **非 JPA 环境支持**：提供 `genFinalStatement()`、`genFinalParamList()` 方法获取 SQL 和参数
- **代码生成插件**：支持双击生成代码

### 版本信息
- 当前版本：2.6.5-SNAPSHOT
- Java 版本：1.8+
- Spring Boot 版本：2.6.14

---

## 2. 项目模块结构

| 模块 | 说明 |
|------|------|
| `simple-dao-annotations` | 注解定义模块 |
| `simple-dao-core` | 核心 DAO 接口模块 |
| `simple-dao-jpa` | JPA 实现模块 |
| `simple-dao-id-generator` | 百度UidGenerator分布式ID生成器 |
| `simple-dao-jpa-starter` | Spring Boot 自动配置模块 |
| `simple-dao-examples` | 使用示例模块 |
| `simple-dao-code-gen` | Maven 代码生成插件 |

---

## 3. 核心接口

组件包含 4 个核心接口：

1. **SimpleDao** - 通用 DAO 接口
2. **SelectDao** - 查询 DAO 接口
3. **UpdateDao** - 更新 DAO 接口
4. **DeleteDao** - 删除 DAO 接口

---

## 4. 快速开始

### 4.1 Maven 依赖

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
    <version>2.6.5-SNAPSHOT</version>
</dependency>
```

### 4.2 基本使用示例

**实体类定义：**
```java
@Entity(name = "student")
@Data
public class Student {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
}
```

**查询 DTO 定义：**
```java
@Data
@TargetOption(
    entityClass = Student.class,
    alias = "s",
    resultClass = Student.class
)
public class StudentQueryDTO {
    @Contains
    String name;
}
```

**服务层调用：**
```java
@Service
public class StudentService {
    @Autowired
    SimpleDao dao;
    
    public List<Student> findStudents(StudentQueryDTO query) {
        return dao.findByQueryObj(query);
    }
}
```

---

## 5. 支持的注解

| 注解 | 说明 |
|------|------|
| `@Eq` | 等于 |
| `@Ne` | 不等于 |
| `@Gt` | 大于 |
| `@Lt` | 小于 |
| `@Gte` | 大于等于 |
| `@Lte` | 小于等于 |
| `@In` | IN 查询 |
| `@NotIn` | NOT IN 查询 |
| `@Contains` | 包含 |
| `@StartsWith` | 开头匹配 |
| `@EndsWith` | 结尾匹配 |
| `@Between` | 范围查询 |
| `@Sum` | 求和统计 |
| `@Avg` | 平均值统计 |
| `@Count` | 计数统计 |
| `@Max` | 最大值统计 |
| `@Min` | 最小值统计 |
| `@GroupBy` | 分组 |
| `@Having` | 分组过滤 |
| `@OrderBy` | 排序 |
| `@Select` | 列选择 |
| `@Update` | 列更新 |
| `@Desc` | 字段描述 |
| `@InjectVar` | 字段转换 |

---

## 6. 代码生成插件

项目提供了 Maven 插件，可通过以下步骤生成代码：

1. 创建 Maven 项目，配置 `simple-dao-codegen` 插件
2. 双击 `gen-project-template` 生成项目模板
3. 编译实体模块
4. 双击 `gen-code` 生成服务类、控制器类等

---

## 7. 技术栈

- Java 1.8+
- Spring Boot 2.6.14
- JPA/Hibernate
- Maven
- Fastjson2
- Druid
- Hutool
- Swagger

---

## 8. 许可与联系

- 作者邮箱：99668980@qq.com
- 项目地址：https://gitee.com/Levin-Li/simple-dao
