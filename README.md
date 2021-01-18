

[![](https://jitpack.io/v/Levin-Li/simple-dao.svg)](https://jitpack.io/#Levin-Li/simple-dao)


### 简介 
   
   SimpleDao是一个使用注解生成SQL语句和参数的组件。
   
   目前组件基于JPA/Hibernate，如果非JPA环境项目需要使用，可以使用  genFinalStatement()、 genFinalParamList() 方法以来获取SQL语句和参数。
   
   在项目中应用本组件能大量减少语句的编写和SQL参数的处理。组件支持Where子句、标量统计函数和Group By子句、Having子句、Order By子句、Select子句、Update Set子句、子查询等。

   SimpleDao的目标

   1、减少直接编写查询语句（SQL或JPQL），提升开发效率，减少代码量，降低编码能力要求。

   2、简化DAO层，或是直接放弃具体的Domain对象DAO层，使用支持泛型通用的Dao。

   设计思路

   通过在DTO对象中加入自定义注解自动生成查询语句。

   DAO层的在Web应用中的位置：

   client request --> spring mvc controller --> DTO(数据传输对象) --> service(含cache)  --> dao(手动编写查询语句并映射参数) --> (JDBC,MyBatis,JPA,QueryDsl)

   SimpleDao优化后的过程：

   client request --> spring mvc controller --> DTO(数据传输对象) --> service(含cache)  --> SimpleDao(使用DTO自动生成查询语句) --> (JDBC,MyBatis,JPA)
   
   测试用例类 [com.levin.commons.dao.DaoExamplesTest](./simple-dao-examples/src/test/java/com/levin/commons/dao/DaoExamplesTest.java)  
   
   
   
### 1 快速上手

#### 1.1 引入依赖
   二进制文件发布在[https://jitpack.io](https://jitpack.io)
   
           <repositories>
       
               <repository>
                   <id>jitpack.io</id>
                   <url>https://jitpack.io</url>
               </repository>
       
           </repositories>

        <dependency>
             <groupId>com.github.Levin-Li.simple-dao</groupId>
            <artifactId>simple-dao-jpa-starter</artifactId>
            <version>2.2.13-SNAPSHOT</version>
        </dependency>
        
        <dependency>
             <groupId>com.github.Levin-Li</groupId>
            <artifactId>simple-dao</artifactId>
            <version>2.2.13-SNAPSHOT</version>
        </dependency>
        
#### 1.2 定义DTO及注解

     /**
      * 数据传输对象(兼查询对象，通过注解产生SQL语句)
      */
    @Data
    @TargetOption(entityClass = TestEntity.class)
    public class TestEntityStatDto {
           
        @Min
        Long minScore; //当minScore字段名在实体对象中不存在时，会尝试自动去除注解的名字 minScore -> score
        
        @Max
        Long maxScore;
    
        @Avg
        Long avgScore;
    
        @Count
        Long countScore;
    
        @GroupBy
        @NotIn
        String[] state = {"A", "B", "C"}; 
    
        @Contains
        String name = "test"; 
    }
   
  测试用例入口类 [DaoExamplesTest](./simple-dao-examples/src/test/java/com/levin/commons/dao/DaoExamplesTest.java) 
  
  DTO用例类 [TestEntityStatDto](./simple-dao-examples/src/test/java/com/levin/commons/dao/dto/TestEntityStatDto.java) 
  
  其它DTO用例参考：[Dto注解](./simple-dao-examples/src/test/java/com/levin/commons/dao/dto) 
   

#### 1.3 配置JPA实体扫描 & 执行查询

  a) 在boot启动类上配置实体扫描注解
   
      @EntityScan({"com.levin.commons.dao","com.xxx.xxx.entities"})
  
        
  b) 执行查询
  
      @Autowired
      JpaDao jpaDao;
      
      jpaDao.findByQueryObj(TestEntityStatDto.class,new TestEntityStatDto());

   以上代码将生成并执行以下SQL：

       Select 
       Min( score ) , 
       Max( score ) , 
       Avg( score ) , 
       Count( 1 ) , 
       state  
       From com.levin.commons.dao.domain.support.TestEntity     
       Where 
       state NOT IN (  ?1 , ?2 , ?3  ) 
       AND name LIKE '%' ||  ?4  || '%'  
       Group By  state
  
#### 1.4 多表连接查询
  
##### 1.4.1 多表关联查询-用 JoinOption 注解关联实体对象 [TableJoinStatDTO](./simple-dao-examples/src/test/java/com/levin/commons/dao/dto/TableJoinStatDTO.java) 
   
   注解代码  @JoinOption(entityClass = Group.class, alias = E_Group.ALIAS)，会自动找实体对象之间的关联字段。
   对象 User 中有 Group类型的字段，但有多个Group类型的字段时，需要手动指定关联的字段
   
      //查询对象，和结果对象
      @Data
      @Accessors(chain = true)
      @TargetOption(
              entityClass = User.class, //主表
              alias = E_User.ALIAS, //主表别名
              resultClass = TableJoinStatDTO.class, //结果类
              isSafeMode = false, //是否安全模式，安全模式时无法执行无条件的查询
              //连接表
              joinOptions = {
                      @JoinOption(entityClass = Group.class, alias = E_Group.ALIAS)  //连接的表，和别名
              })
      public class TableJoinStatDTO {
      
          //统计部门人数，并且排序
          @Count(havingOp = Op.Gt, orderBy = @OrderBy)
          Integer userCnt = 5;
      
          //统计部门总得分
          @Sum
          Long sumScore;
      
          //统计部门平均分，并且排序
          @Avg(havingOp = Op.Gt, orderBy = @OrderBy,alias = "avg")
          Long avgScore = 20L;
      
          //按部门分组统计，结果排序
          @GroupBy(domain = E_Group.ALIAS, value = E_Group.name,orderBy = @OrderBy())
          String groupName;
      
      }
       
        //执行查询，并把查询结果放在TableJoinStatDTO对象中
       List<TableJoinStatDTO> objects = jpaDao.findByQueryObj(new TableJoinStatDTO());
       
       //生成的语句
       Select Count( 1 ) , Sum( u.score ) , Avg( u.score ) AS avg , g.name  
       From com.levin.commons.dao.domain.User u  Left join com.levin.commons.dao.domain.Group g on u.group = g.id     
       Group By  g.name 
       Having  Count( 1 ) >   ?1  AND Avg( u.score ) >   ?2  
       Order By  Count( 1 ) Desc , Avg( u.score ) Desc , g.name Desc
       
       
##### 1.4.2 多表关联查询-用 JoinOption 注解  [TableJoin3](./simple-dao-examples/src/test/java/com/levin/commons/dao/dto/TableJoin3.java)   
       
   以下 @TargetOption 注解部分，手动指定关联的别名和关联的字段，joinTargetAlias = E_User.ALIAS , joinTargetColumn = E_User.group。
        
       @Data
       @Accessors(chain = true)
       @TargetOption(tableName = E_User.CLASS_NAME,alias = E_User.ALIAS,
               joinOptions = {
               @JoinOption(tableOrStatement = E_Group.CLASS_NAME,
                       alias = E_Group.ALIAS,joinColumn = E_Group.id,joinTargetAlias = E_User.ALIAS,joinTargetColumn = E_User.group)
       })
       public class TableJoin3 {
       
           @Select(domain = E_User.ALIAS, value = E_User.id, isDistinct = true)
           @Gt(value = E_User.id, domain = E_User.ALIAS)
           Long uid = 1l;
       
           @Select(value = E_Group.id, domain = E_Group.ALIAS)
           @Gte(domain = E_Group.ALIAS,value = E_Group.id)
           Long gid;
       
           @Select
           String name;
       
           @Select(domain = E_Group.ALIAS, value = E_Group.name)
           String groupName;
       
       }
          
          
          
##### 1.4.3 多表关联查询-直接用TargetOption 注解的 tableName（或是fromStatement） 属性拼出连接语句 [FromStatementDTO](./simple-dao-examples/src/test/java/com/levin/commons/dao/dto/FromStatementDTO.java) 
   
   注解代码 @TargetOption( tableName = "jpa_dao_test_User u left join jpa_dao_test_Group g on u.group.id = g.id" )
      
       
       @Data
       @Accessors(chain = true)
       @TargetOption(
               tableName = "jpa_dao_test_User u left join jpa_dao_test_Group g on u.group.id = g.id" ,
       //        fromStatement = "from jpa_dao_test_User u left join jpa_dao_test_Group g on u.group = g.id"
               )
       public class FromStatementDTO {
       
           @Select(value = "u.id", isDistinct = true)
           @Gt(value = E_User.id, domain = "u")
           Long uid = 1l;
       
           @Select(value = E_Group.id, domain = "g")
           @Gte("g.id")
           Long gid;
       
           @Select(domain = "u")
           String name;
       
           @Select(domain = "g", value = E_Group.name)
           String groupName;
       
       } 


#### 1.5 分页查询支持
   
   查询辅助类 [PagingQueryHelper](./simple-dao-core/src/main/java/com/levin/commons/dao/support/PagingQueryHelper.java) 
   通过 PageOption 注解 实现分页大小、分页码，是否查询总数的参数的获取，查询成功后，也通过注解自动把查询结果注入到返回对象中。             
   
     //使用示例
     PagingData<TableJoinDTO> resp = PagingQueryHelper.findByPageOption(jpaDao, 
                             new PagingData<TableJoinDTO>(), new TableJoinDTO().setRequireTotals(true));
   
   
       
   Dao 方法查询结果和总记录数
       
       dao.findTotalsAndResultList(Object... queryObjs);
   
   
   分页查询请求，分页查询参数通过PageOption注解获取
   
     @Data
     @Accessors(chain = true)
     //@Builder
     @FieldNameConstants
     public class PagingQueryReq
             implements Paging, ServiceRequest {
     
         @Ignore
         @Schema(description = "是否查询总记录数")
         @PageOption(value = PageOption.Type.RequireTotals, remark = "通过注解获取是否查询总记录数，被标注字段值为 true 或是非空对象")
         boolean isRequireTotals = false;
     
         @Ignore
         @Schema(description = "是否查询结果集")
         @PageOption(value = PageOption.Type.RequireResultList, remark = "通过注解获取是否返回结果集列表，被标注字段值为 true 或是非空对象")
         boolean isRequireResultList = true;
     
         @Ignore
         @Schema(description = "页面索引")
         @PageOption(value = PageOption.Type.PageIndex, remark = "通过注解获取页面索引")
         int pageIndex = 1;
     
         @Ignore
         @Schema(description = "页面大小")
         @PageOption(value = PageOption.Type.PageSize, remark = "通过注解获取分页大小")
         int pageSize = 20;
     
         @Schema(description = "是否使用缓存")
         @Ignore
         Boolean fromCache;
     
         public PagingQueryReq() {
         }
     
         public PagingQueryReq(int pageIndex, int pageSize) {
             this.pageIndex = pageIndex;
             this.pageSize = pageSize;
         }
     
     }
   
       
   分页查询结果类 [PagingData](./simple-dao-core/src/main/java/com/levin/commons/dao/support/PagingData.java) 
   
      @Data
      @Accessors(chain = true)
      //@Builder
      @FieldNameConstants
      public class PagingData<T> implements Serializable {
      
          @Ignore
          @Schema(description = "总记录数")
          @PageOption(value = PageOption.Type.RequireTotals, remark = "查询结果会自动注入这个字段")
          long totals = -1;
      
          @Ignore
          @Schema(description = "页面编号")
          @PageOption(value = PageOption.Type.PageIndex, remark = "通过注解设置分页索引")
          int pageIndex = -1;
      
          @Ignore
          @Schema(description = "分页大小")
          @PageOption(value = PageOption.Type.PageSize, remark = "通过注解设置分页大小")
          int pageSize = -1;
      
          @Ignore
          @Schema(description = "数据集")
          @PageOption(value = PageOption.Type.RequireResultList, remark = "查询结果会自动注入这个字段")
          List<T> records;
      
          @Transient
          public T getFirst() {
              return isEmpty() ? null : records.get(0);
          }
      
          @Transient
          public boolean isEmpty() {
              return records == null || records.isEmpty();
          }
      
          public PagingData() {
          }
      
      }


### 2 组件使用方式

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
    SelectDao dao = jpaDao.selectFrom(Group.class);
    dao.find()

    //更新DAO
    UpdateDao dao = jpaDao.updateTo(Group.class);
    dao.update()

    //删除DAO
    DeleteDao dao = jpaDao.deleteFrom(Group.class)
     dao.delete()

#### 2.2 自定义DAO接口或是DAO类(不推荐，建议在服务类中直接使用JpaDao)

##### 2.2.1 自定义DAO接口

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
   在 pom.xml 文件中加入以下配置：

       <plugin>
           <artifactId>maven-compiler-plugin</artifactId>
           <inherited>true</inherited>
           <configuration>
               <!-- 在编译时表留方法的参数名称-->
               <parameters>true</parameters>
           </configuration>
       </plugin>


##### 2.2.2 自定义DAO类（和自定义接口的区别是可以对查询结果二次加工）

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

            //获取查询结果的关键点：RepositoryFactoryBean.getProxyInvokeResult()
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
           
##### 2.2.3 设置扫描包名 & 启用扫描  

     //设置 EntityRepository 注解的扫描范围        
     @ProxyBeanScan(scanType = EntityRepository.class, factoryBeanClass = RepositoryFactoryBean.class
                     , basePackages = {"com. levin. commons . dao.."})
                     
     //启用组件扫描                
     @EnableProxyBean(registerTypes = EntityRepository.class)                


### 3 组件接口及注解

#### 3.1 Dao接口

*    [SelectDao](./simple-dao-core/src/main/java/com/levin/commons/dao/SelectDao.java)

*    [UpdateDao](./simple-dao-core/src/main/java/com/levin/commons/dao/UpdateDao.java)

*    [DeleteDao](./simple-dao-core/src/main/java/com/levin/commons/dao/DeleteDao.java)

*    [SimpleDao](./simple-dao-core/src/main/java/com/levin/commons/dao/SimpleDao.java)
       
*    [JpaDao](./simple-dao-jpa/src/main/java/com/levin/commons/dao/JpaDao.java)
       

#### 3.2 注解的语句生成规则

  操作枚举类：[com.levin.commons.dao.annotation.Op](./simple-dao-annotations/src/main/java/com/levin/commons/dao/annotation/Op.java)，定义了常见的 sql 表达式。
  

  语句表达式生成规则： surroundPrefix + op.gen( funcs(fieldName), funcs([ paramExpr(优先) or 参数占位符 ])) +  surroundSuffix


### 4 简单查询
    
   查询注解 主要再 com.levin.commons.dao.annotation 包中，包括常见的 SQL 操作符。 
   
   注意若果字段没有注解，相当于是 Eq 注解，字段值为null值或是空字符串，将不会产生 SQL 语句。

   DTO类字段定义示例：

       @Desc("店铺id")
        Long storeId;

       @Desc("店铺名称")
       @Eq
        String storeName;

       @Desc("店铺所在区域")
        String storeArea;

       @Desc("店铺状态")
        StoreStatus storeStatus;

       @Desc("店铺库存预警")
       @Ignore // 生成的语句忽略该字段
        Boolean storageAlarm;

       @Desc("商品分类id")
       @Contains
       String classId;


### 5 统计查询(含Having字句)

   统计注解在com.levin.commons.dao.annotation.stat 包中，主要包括以下注解：
   
        @Avg
        @Max
        @Min
        @Sum
        @Count
        @GroupBy
        
   
   统计注解 有一个 havingOp 属性，用来表示 Having 查询字句，如：
     
         @Avg(havingOp = Op.Gt)
         Long avgScore = 10L;
         
         @GroupBy
         @Gt 
         int month = 5;
   
   意思注解将产生语句： select month , AVG(score) from XXX where month > 5 having AVG(score) > 10 
   
   Dao 支持多表统计，编码实现，如下例子：
   
       
       jpaDao.selectFrom(Group.class, "g")
                       .join("left join " + User.class.getName() + " u on g.id = u.group.id")
                       .join("left join " + Task.class.getName() + " t on u.id = t.user.id")
                       .count("1")
                       .avg("t.score")
                       .avg("u.score")
                       .avg("g.score")
                       .sum("t.score")
                       .groupByAsAnno(E_Group.name)
                       .find();
                       
 
   Dao 支持多表统计，通过注解实现
   
       @Data
       @Accessors(chain = true)
       @TargetOption(
               entityClass = User.class, alias = E_User.ALIAS,
               resultClass = TableJoinDTO.class,
               isSafeMode = false,
               //连接表
               joinOptions = {
                       @JoinOption(alias = E_Group.ALIAS, entityClass = Group.class)
               })
       public class TableJoinDTO {
       
           @Select(value = "u.id", isDistinct = true)
           @Gt(value = E_User.id, domain = E_User.ALIAS)
           Long uid;
       
           @Select(value = E_Group.id, domain = E_Group.ALIAS)
           @Gte("g.id")
           Long gid;
       
           @Select
           String name;
       
           @Select(domain = E_Group.ALIAS, value = E_Group.name)
           String groupName;
       
       }                      
      

### 6 列选择和列更新

  选择查询注解：

          @Select
          String field;

  产生的语句：

      select field from ...


  更新注解：

         @Update
         protected Date lastUpdateTime = new Date();

  产生的语句

         set lastUpdateTime = ?



### 7 复杂查询(逻辑嵌套)

   逻辑注解支持

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


  Dao 方法支持
    
     //逻辑嵌套
     jpaDao.selectFrom("table").and().or().and().end().end().end();

### 8 子查询
 

#### 8.1 手动指定子查询语句(paramExpr属性)

          @Ignore
          @SelectColumn("score")
          UserStatDTO selectSubQueryDTO = new UserStatDTO();

          @Ignore
          @SelectColumn(value = "score", paramExpr = "select 3000 from xxx.tab t where u.id = t.id")
          Map param = new HashMap();

          //子查询，并使用命名参数，命名参数从Map变量中取
          @NotExists(paramExpr = "select name from xxx.tab t where u.id = t.id and t.score > :minScore")
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


### 9 排序(OrderBy注解)

   排序使用OrderBy注解，OrderBy支持字段和参数。

    @Contains
    @OrderBy
    String name = "test";


    @OrderByList(
            {
                    @OrderBy(E_User.createTime),
                    @OrderBy(value = E_User.area, order = 5,type = OrderBy.Type.Asc),
                    @OrderBy(condition = C.NOT_NULL)
            }
    )
    String orderCode ="1111";

   以上将生成OrderBy将生成如下语句：

    Order By  area Asc , name Desc , createTime Desc , orderCode Desc
    
    
   简单排序注解 SimpleOrderBy 
   
         @SimpleOrderBy(condition = "state.length > 0")
         String[] orderBy = {"state desc", "name asc"};
     
         @SimpleOrderBy(condition = "name != null")
         String orderBy2 = "score desc , category asc";  


### 10 关于注解

#### 10.1  查询对象字段无注解


##### 10.1.1 基本类型字段无注解
  基本类型无注解示例：

       Long id;
       String name = "Echo";

  基本类型且无注解，将默认为等于操作。以上注解将产生如下语句：

      name = ?

  注意以上id字段并没有生产条件，默认情况下，字段值为null值或是空字符串，字段都将被忽略。



##### 10.1.2 复杂类型字段无注解

   复杂类型的定义为：

   a)非基本类型

   b)非基本类型的数组

   复杂类型无注解法例：

      //无注解
      DTO queryDTO = new DTO();

   以上字段将会被递归解析，所产生的语句将会被加入当前语句中。
   

#### 10.2 强制忽略

   可以通过 Ignore 注解强制忽略指定的字段或是类。
   如果注解标注在 Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT 三种字段上也将被忽略。


     //忽略字段
     @Ignore
     String tempValue = "Echo";

     //忽略的类，整个类将被忽略
     @Ignore
     public class xxDTO{
     ...
     }

#### 10.3 有条件忽略(SPEL表达式)

   大部分的注解都有 condition 属性，以脚本的方式求值，目前只支持SpEL，当返回true时，表示注解有效，如：

      @Eq(condition="#_val != null")
      String name = "Echo";

#### 10.4 变量上下文

  SPEL 中可以使用，任意的查询语句中也都可以使用
   

  可用默认变量：

       #_val 表示字段的值
    
       #_this 表示DTO对象
    
       #_name 当前注解所在的字段名
    
       #_isSelect 表示当前是否是SelectDao
    
       #_isUpdate 表示当前是否是UpdateDao
    
       #_isDelete 表示当前是否是DeleteDao
   
   
   上下文列表（越后面优先级越高）：
   
      DaoContext.getGlobalContext(); //全局上下文
      
      DaoContext.getThreadContext(); //线程上下文
      
      jpaDao.selectFrom(User.class).setContext(); //dao 实例上下文
      
      //参数上下文
 
#### 10.5 字段值自动转换

##### 10.5.1 查询结果对象值转换  
 
   组件集成Spring的值转换功能，如果可以把字符串转换成数组，把字符串转换成日期、数值等。
   日期类型转换使用 Spring 的注解 DateTimeFormat 
   数值类型转换使用 Spring 的注解 NumberFormat

   如下例子：
    
    //实体对象
     class Entity{
      Long id; 
      Date createTime;
      }
    
     //查询结果对象
     class ResultDto {
     
       //自动转换为字符串
       String id; 
      
      @GroupBy
      @DateTimeFormat(pattern = "yyyy-MM-dd")  
      String createTime;
      
      }
      
##### 10.5.2 查询对象值转换   
   
   把 DTO 对象的值转换成数据库查询需要的值类型
    
   日期字段转换
   
      @Between(paramDelimiter = "-", patterns = "yyyyMMdd")  // 参数将用 - 号分隔
      String betweenCreateTime = "20190101-20220201"; //生成语句 createTime between ? AND ?
          
       
   In ,NotIn ,Between 等注解自动切割字符串为多个参数，如果数据库字段定义是字符串，则不自动用逗号分割，也可以强制指定分隔符 paramDelimiter。
   
      @NotIn(paramDelimiter = ",")
      String notInName = "A,B,C";  //生成语句 name not in (:?,:?,:?)
      
      
       @In(not = true, having = true)
       String[] state = new String[]{"A", "B", "C"};   //生成语句 Not(state in (:?,:?,:?)) 
       
      
    
### 11  避免 N + 1 查询         

#### 11.1 通过实体配置立刻抓取

   一对多，多对一模型定义 fetch = FetchType.EAGER
  
        @Entity    
        class User{
                ...
               @ManyToOne(fetch = FetchType.EAGER) 
               @JoinColumn(name = "group_id")
               Group group;
        }     
        
        @Entity   
         class Group {
                  ...
                
                 @ManyToOne(fetch = FetchType.EAGER) 
                 protected T parent; 
                 
                 @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE) 
                 protected Set<T> children;
         }   
     
#### 11.2 通过代码抓取    
        
         //查询 User 实，直接通过连接获取所有的孩子节点，避免 N+1 查询   
         jpa.selectFrom(User.class,"u")
            .joinFetch("group.children") //设置立刻抓取 避免 N+1 查询 
            .find()   
        
  
#### 11.3 通过注解抓取
  
  查询对象和结果对象都可以增加抓取注解
    
        @Data
        @Accessors(chain = true)
        public class UserInfo {
      
            @Fetch //设置立刻抓取 避免 N+1 查询 
            Group group;
        
            @Fetch(value = "group.name") //设置立刻抓取 避免 N+1 查询 
            String groupName;
        
            @Fetch(value = "group.children" ) //设置立刻抓取 避免 N+1 查询 
            Collection<Group> parentChildren;
        
        }      
        
        //避免 N+1 查询
        List<UserInfo> userInfoList jpaDao.selectFrom(User.class, "u").find(UserInfo.class)     
        
### 12 安全模式

   数据安全是非常重要的事情，DAO 增加安全模式能避免一些因为疏忽导致的数据安全问题。

   在安全模式下，必须指定部分条件，不允许无条件的更新、删除、查询。
    
  默认情况下 Dao 都是安全模式，可以调用 disableSafeMode() 禁用安全模式，如下：
    
    jpaDao.deleteFrom(User.class)
                   .disableSafeMode()
                   .delete();
   
   
  安全控制接口定义
   
       public interface SafeController<T> {
       
           /**
            * 禁止安全模式
            */
           T disableSafeMode();
       
           /**
            * 安全模式
            * <p>
            * 在安全模式下，不允许无条件的查询、更新和删除
            *
            * @return
            */
           boolean isSafeMode();
           
       }
   
### 13 附录

#### 13.1 测试用例

 请参考测试用例： [com.levin.commons.dao.DaoExamplesTest](./simple-dao-examples/src/test/java/com/levin/commons/dao/DaoExamplesTest.java) 
  
#### 13.2 联系作者

 邮箱：99668980@qq.com


