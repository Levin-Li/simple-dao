### 简介
   SimpleDao是一个使用注解生成SQL语句和参数的小组件，目前组件依赖Spring并结合JPA，如果非JPA环境项目需要使用，可以使用  genFinalStatement()、 genFinalParamList() 方法以来获取SQL语句和参数。
   在项目中应用本组件能大量减少语句的编写和SQL参数的处理。组件支持Where子句、标量统计函数和Group By子句、Having子句、Order By子句、Select子句、Update Set子句、子查询等。

   SimpleDao的目标

   1、基本不再编写查询语句（SQL或JPQL）。

   2、简化DAO层，或是直接放弃具体的Domain对象DAO层，使用支持泛型通用的Dao。

   开发思路

   通过在DTO对象中加入自定义注解自动生成查询语句。

   DAO层的在Web应用中的位置：

   client request --> spring mvc controller --> DTO(数据传输对象) --> service(含cache)  --> dao(手动编写查询语句) --> (JDBC,MyBatis,JPA)

   SimpleDao优化后的过程：

   client request --> spring mvc controller --> DTO(数据传输对象) --> service(含cache)  --> SimpleDao(使用DTO自动生成查询语句) --> (JDBC,MyBatis,JPA)


### 1 快速上手

#### 1.1 引入依赖
        <dependency>
            <groupId>com.levin.commons.maven</groupId>
            <artifactId>simple-dao-jpa-starter</artifactId>
            <version>2.x.x-SNAPSHOT</version>
        </dependency>
##### 1.2 定义DTO及注解

     /**
      * 数据传输对象(兼查询对象，通过注解产生SQL语句)
      */
     @TargetEntity(tableName = "t_users", alias = "u")
     @Data        
     public class UserStatDTO {

       @GroupBy
       String area;

       @Avg(havingOp = " > ")
       @Sum
       Integer score = 500;

       @Having
       Boolean enable = true;

       @Like
       String state = "Y";
    }

##### 1.3 执行查询

      @Autowired
      JpaDao jpaDao;
      
      jpaDao.findByQueryObj(UserStatDTO.class,new UserStatDTO());

   以上代码将生成并执行以下SQL：

      SELECT
      	u.area,
      	AVG(u.score),
      	SUM(u.score)
      FROM
      	t_users u
      WHERE
      	u.ENABLE = TRUE
      	AND u.state like ?
      GROUP BY
      	u.area
      HAVING
      	AVG(u.score) > ?
      AND u.ENABLE = ?
      ORDER BY
      	u.area


###  2、组件使用方式

#### 2.1 直接使用通用Dao（推荐）

##### 2.1.1 使用JpaDao
   在服务层代码中通过Spring注入JpaDao实例，通过JpaDao动态创建。

   使用示例：

      @Autowired
      JpaDao jpaDao;

      SelectDao selectDao = jpaDao.selectFrom("t_table_name","alias");

      List queryResult = selectDao.appendByQueryObj(new UserStatDTO()).find();

##### 2.1.1 使用SelectDao 、UpdateDao、DeleteDao

    //查询DAO
    SelectDao dao = new SelectDaoImpl();
    dao.find()

    //更新DAO
    UpdateDao dao = new UpdateDaoImpl();
    dao.update()

    //删除DAO
    DeleteDao dao = new DeleteDaoImpl();
     dao.delete()

#### 2.2 使用自定义DAO接口或是DAO类

#### 2.2.1 使用自定义DAO接口（推荐）

   接口DAO案例：

    //DAO 自动扫描注解
    @EntityRepository("用户DAO")

    //DAO默认操作目标注解
    @TargetEntity(entityClass = User.class, alias = "u"
            , fixedCondition = "u.enable = true", defaultOrderBy = "u.orderCode desc")

    public interface UserDao {

        List<User> find(@Eq Long id, @Like String name,
                        @Gt Integer score, Paging paging);

        @QueryRequest(joinFetchSetAttrs = {"group"})
        User findOne(@Eq Long id, @Like String name,
                     @Eq String category, Paging paging);

        @UpdateRequest
        int update(@Eq Long id, @UpdateColumn String name);

        @DeleteRequest
        int delete(@OR @Eq Long id, String name);

    }

接口DAO定义好后，直接在需要的服务类中直接通过Spring注入

         @Autowired
         UserDao userDao;

         userDao.delete(...)


   **特别说明：**
   需要在JDK1.8中编译，并增加编译参数：-parameters ，保留方法的参数名称。

      javac -parameters


#### 2.2.2 使用自定义DAO类

   DAO抽象类案例：

    //DAO 自动扫描注解
    @EntityRepository("组DAO")

    //DAO默认操作目标注解
    @TargetEntity(entityClass = Group.class, fixedCondition = "enable = true", defaultOrderBy = "orderCode desc")
    public abstract class GroupDao {

        @Autowired
        private JpaDao jpaDao;

        @QueryRequest
        public Group findOne(@OR @Eq Long id, @Like String name,
                             @Eq String category, Paging paging) {

            //获取查询结果的关键方法：RepositoryFactoryBean.getProxyInvokeResult()
            Group result = RepositoryFactoryBean.getProxyInvokeResult();

            System.out.println(result);

            return (Group) result;
        }

        @QueryRequest
        public List<Group> find(@OR @Eq Long id, @Like String name,
                                @Eq String category, Paging paging) {

            List<Group> groups = RepositoryFactoryBean.getProxyInvokeResult();

            //...处理其它逻辑

            System.out.println(groups);

            return groups;
        }

        @UpdateRequest
        public int update(@Eq Long id, @UpdateColumn String name) {

            Integer r = RepositoryFactoryBean.getProxyInvokeResult();

            //...处理其它逻辑

            return r != null ? r : 0;
        }

        //没有注解方法将无效，如果调用RepositoryFactoryBean.getProxyInvokeResult(); 将会生产异常
        public Object noAnnoMethod(@Eq Long id, @UpdateColumn String name) {

            Object r = RepositoryFactoryBean.getProxyInvokeResult();

            return r;
        }

        @QueryRequest
        public Group findOneAndRepeatGetResult(@OR @Eq Long id, @Like String name,
                                               @Eq String category, Paging paging) {

            Object result = RepositoryFactoryBean.getProxyInvokeResult();

            System.out.println(result);

            RepositoryFactoryBean.getProxyInvokeResult();

            return (Group) result;
        }
    }

抽象DAO定义好后，直接在需要的服务类中直接通过Spring注入

          @Autowired
          GroupDao groupDao;

          //使用Dao
           groupDao.find() ...


### 3 组件核心接口

#### 3.1 Dao接口

       1) SelectDao

       2) UpdateDao

       3) DeleteDao

#### 3.2 JPA 接口

      JpaDao

#### 3.3 分页接口

      Paging

#### 3.4 结果转换接口(对单条查询结果进行转换)

       /**
        * 结果转换器
        *
        * @param <I> 输入
        * @param <O> 转换后的结果
        */
       public interface Converter<I, O> {
           O convert(I data);
       }

### 4、简单查询

#### 4.1 查询

   DTO类字段定义示例：

       @Desc("店铺id")
       private Long storeId;

       @Desc("店铺名称")
       @Like
       private String storeName;

       @Desc("店铺所在区域")
       private String storeArea;

       @Desc("店铺状态")
       private StoreStatus storeStatus;

       @Desc("店铺库存预警")
       @Ignore
       private Boolean storageAlarm;

       @Desc("商品分类id")
       @Like
       private String classId;


### 5、统计查询

   请参考快速上手

### 6、指定字段的查询和数据更新

  选择查询注解：

          @SelectColumn()
          String field;

  产生的语句：

      select field from ...


  更新注解：

         @UpdateColumn
         protected Date lastUpdateTime = new Date();

  产生的语句

         set lastUpdateTime = ?



### 7、逻辑注解(用于实现复杂的查询条件)

   逻辑注解：

     @AND
        @OR
        @END
     @END

     #逻辑注解可以嵌套使用，当有一个节条件不成立时，嵌套的所有子条件都将被忽略

    //以下为代码片段

    @AND(condition = "#_val == true")
    protected Boolean editable = true;

    @Lt
    @OR(condition = "#_val!=null")
    protected Date createTime = new Date();

    @Between("score")
    @End
    protected Integer[] scores = new Integer[]{200, 100};

    @Like
    @End
    protected String description = "keywords";

  以上注解将生成如下语句：

      editable = ?
      and ( createTime < ? or  scores Between ? and ? )
      and description like ?


### 8、子查询

   子查询可以用于Select子句，Update Set 子句，where子句和Having子句中。

#### 8.1 手动指定子查询语句(subQuery属性)

          @Ignore
          @SelectColumn("score")
          UserStatDTO selectSubQueryDTO = new UserStatDTO();

          @Ignore
          @SelectColumn(value = "score", subQuery = "select 3000 from xxx.tab t where u.id = t.id")
          Map param = new HashMap();

          //子查询，并使用命名参数，命名参数从Map变量中取
          @NotExists(subQuery = "select name from xxx.tab t where u.id = t.id and t.score > :minScore")
          Map<String, Object> namedParams = new HashMap<>();


          //子查询，子查询将从subQueryDTO查询对象中生成
          @NotExists
          UserStatDTO statDTO = new UserStatDTO();

          //子查询产生
          @Gt("score")
          UserStatDTO whereSubQueryDTO = new UserStatDTO();


#### 8.2  使用嵌套查询对象

        //子查询，子查询将从subQueryDTO查询对象中生成
        @NotExists
        DTO subQueryDTO = new DTO();

        //In注解，将生产子查询
        @In("status")
        DTO subQueryDTO = new DTO();

#### 8.3  嵌套查询对象特别情况(嵌套查询对象无注解)

        //无注解
        DTO queryDTO = new DTO();

   复杂对象无注解时，将不生成子查询，查询条件将直接增加在当前的查询条件中


### 9、排序(OrderBy注解)

   排序使用OrderBy注解，OrderBy支持字段和参数。

    @OrderBy(order = 3) //按数值从小到大排序
    String name = "lile";

    @Gt
    @OrderBy(order = 1)
    int scope = 10;

    @OrderBy(order = 2)
    protected Integer orderCode;

   以上将生成OrderBy将生成如下语句：

    Order by scope , orderCode, name


### 10、使用注意事项

#### 10.1 无注解的情况

##### 10.1.1 基本类型无注解

   基本类型的判断逻辑：

       public static boolean isPrimitive(Type type) {

           if (!(type instanceof Class))
               return false;

           Class clazz = (Class) type;

           return clazz.isPrimitive()
                   || String.class.isAssignableFrom(clazz)
                   || Boolean.class.isAssignableFrom(clazz)
                   || Date.class.isAssignableFrom(clazz)
                   || Double.class.isAssignableFrom(clazz)
                   || Float.class.isAssignableFrom(clazz)
                   || Enum.class.isAssignableFrom(clazz)
                   || Long.class.isAssignableFrom(clazz)
                   || Integer.class.isAssignableFrom(clazz)
                   || Short.class.isAssignableFrom(clazz)
                   || Byte.class.isAssignableFrom(clazz)
                   || Character.class.isAssignableFrom(clazz);
       }

  基本类型无注解示例：

       Long id;
       String name = "Echo";

  基本类型且无注解，将默认为等于操作。以上注解将产生如下语句：

      name = ?

  注意以上id字段并没有生产条件，默认情况下，字段值为null将忽略这个字段。  null值或是空字符串，字段都将被忽略。


##### 10.1.2 复杂类型无注解

   复杂类型的定义为：

   a)非基本类型

   b)非基本类型的数组

   复杂类型无注解法例：

      //无注解
      DTO queryDTO = new DTO();

   以上字段将会被递归解析，所有生产的语句将会被加入当前语句中。

#### 10.2 强制忽略

   可以通过Ignore注解强制忽略指定的字段或是类

     //忽略字段
     @Ignore
     String tempValue = "Echo";

     //忽略的类，整个类将被忽略
     @Ignore
     public class xxDTO{
     ...
     }

#### 10.3 有条件忽略

   大部分的注解都有condition属性，以脚本的方式求值，目前只支持SpEL，当返回true时，表示注解有效，如：

      @Eq(condition="#_val != null")
      String name = "Echo";

  默认变量：

   #_val 表示字段的值

   this 表示DTO对象

   #name 表示name属性，不一定是字段，也可以是getName()方法的返回值，具体参考SpEL。

   #_isSelect 表示当前是否是SelectDao

   #_isUpdate 表示当前是否是UpdateDao

   #_isDelete 表示当前是否是DeleteDao


#### 10.4 有效的注解

   如果注解标注在 Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT 三种字段上将被忽略。

#### 10.5 同一个字段或参数多个注解的问题

  同一个字段或参数是允许同时有多个和多种注解，Not 和 Having比较特别，当没有其它注解时，将被默认为相等的操作。


  示例一：

    @Not
    @Having
    String name = "Echo";

  以上将生成如下语句：

      Having name Not(name = ?)

   示例二：

     @Not
     String name = "Echo";

   以上将生成如下语句：

       Where name Not(name = ?)

  示例三：

    @Not
    @Having
    @Like
    @Eq
    @OR
    @END
    String name = "Echo";

  以上将生成如下语句：

      Having (NOT(u.name LIKE  ? ) OR NOT(u.name =  ? ))

  示例四：

    @GroupBy
    @OrderBy
    String name = "Echo";

  以上将生成如下语句：

      Group by name Order by name

  但不会生产Where语句。

#### 10.6 自动值转换

   组件集成Spring的值转换功能，如果可以把字符串转换成数组，把字符串转换成日期、数值等。

   JPA实体类字段定义：

      Long id;

      Date startTime;

   DTO类字段定义 ：

      String id;

      String startTime;

   以上字符串字段将被会自动转换成对应的类型。

#### 10.7 集合属性的自动抓取(仅对JPA有效)

   集合属性的自动抓取

        @FetchSet(attrs="users")
        int unusedField;
        
        
### 11、安全模式

   在安全模式下，不允许无条件的更新、删除、查询，或是必须指定部分条件。
   
   数据安全是非常重要的事情，DAO 增加安全模式能避免一些因为疏忽问题导致的数据安全问题。
   
            
         

### 12、附录



    有使用问题请联系：99668980@qq.com

