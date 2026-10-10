
[![](https://www.jitpack.io/v/Levin-Li/simple-dao.svg)](https://www.jitpack.io/#Levin-Li/simple-dao)

### 简介 
   
   SimpleDao是一个使用注解生成SQL语句和参数的Dao组件，通过在DTO对象中加入自定义注解自动生成查询语句。

   在项目中应用本组件能大量减少SQL语句的编写和参数的处理。组件支持Where子句、标量统计函数和Group By子句、Having子句、Order By子句、Select子句、Update Set子句、子查询、逻辑删除，安全模式等。
 
   目前组件基于JPA/Hibernate，如果非JPA环境，可以使用  genFinalStatement()、 genFinalParamList() 方法以来获取SQL语句和参数。
   
   说重点，组件的Maven插件可以双击生成代码哦。

   组件逻辑架构如下图：   
   
   ![类逻辑框图](./public/core-interface.png)   
   
    
### 1 使用预览

   实体类
   
      //1、学生表
      @Entity(name = "student")
      @Data
      @Accessors(chain = true)
      @FieldNameConstants 
      public class Student{
     
         @Id
         @GeneratedValue
         private Long id;
         
         //学生姓名 
         String name;  
         ...  
      }
      
       //2、考试成绩表
       @Entity(name = "exam_log")
       @Data
       @Accessors(chain = true)
       @FieldNameConstants 
       public class ExamLog{
      
          @Id
          @GeneratedValue
          private Long id;
          
          //学生ID 
          Long studentId;
          
          //学科
          String subject;
          
          //成绩分数
          Integer score;
 
          ... 
             
       }     
      
   需求：
   
   查询并统计出语数英三科考试中总分超过260分，学科平均分高于80分，学生姓名中包含特定字符的学生姓名、总分、平均分。
 
   解决方案：
   
   1）定义查询对象（表连接）
  
   数据传输对象(兼查询对象，通过注解产生SQL语句)
    
      @Data
      @TargetOption(
      
      entityClass =ExamLog.class ,  /* 目标主表 */ 
      
      alias = E_ExamLog.ALIAS, // 主表别名
      
      resultClass = ExamStatDto.class,  /* 结果类 */
      
      //表连接
      joinOptions = {
            @JoinOption(entityClass = Student.class, alias = E_Student.ALIAS, joinTargetColumn = E_ExamLog.studentId )  //连接的表
            //可以再连接表2 
         } 
      )
      public class ExamStatDto {
      
          @Sum(having=Op.Gt)
          Long sumScore = 260L;      
              
          @Avg(having=Op.Gt)
          Long avgScore = 80L; //当avgScore字段名在实体对象中不存在时，会尝试自动去除注解的名字 avgScore -> score

          @In
          String[] subject = {"语文", "数学", "英语"};  
        
          @Contains(domain = E_Student.ALIAS) // 过滤出学生名字中包含'李'字
          @GroupBy(domain = E_Student.ALIAS)  // 按名字分组
          String name = "李"; 
          
      }
      
       以上Dto等效的SQL语句如下：
       
          Select 
            s.name ,  Avg(e.score) ,  Sum(e.score) 
          From exam_log e Left Join student s on  s.id = e.studentId
          Where 
            e.subject IN ("语文", "数学", "英语")  AND s.name LIKE '%李%'  
          Group By s.name
          Having Avg(e.score) > 80 and Sum(e.score) > 260
   
   2） 服务层
       
        @Service
        public class ExamStatService {
        
         @Autowired
         SimpleDao dao; //通用 Dao
         
         public List<ExamStatDto> stat(ExamStatDto statDto){
           //一行代码，就一行！！！
           //查询结果自动绑定到 ExamStatDto对象。
           return dao.findByQueryObj(statDto);
         }
         
       }
         
   3）控制器 
     
     @RestController
     public class ExamStatController{
     
        @Autowired
        ExamStatService examStatService;
        
        @GetMapping("/exam_stat")
        public ApiResp<ExamStatDto> stat(ExamStatDto statDto){
            return ApiResp.ok(examStatService.stat(statDto));
        }
        
     }
     
   大功告成，用 postman 测试以一下。这个是组件的多表统计应用，组件还支复杂逻辑嵌套，子查询对象嵌套，逻辑删除等。  
        
 
### 2 快速上手

#### 2.1 一键代码生成

  如果文档中的图片不能显示，请访问 [https://gitee.com/Levin-Li/simple-dao](https://gitee.com/Levin-Li/simple-dao) 查看。
   
##### 2.1.1 创建示例项目
   
   建立一个空Maven项目，把 pom.xml 文件替换成以下内容
   
    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

     <!-- Auto gen by simple-dao-codegen, @time: ${.now}, 代码生成哈希校验码：[]，请不要修改和删除此行内容。 -->

    <modelVersion>4.0.0</modelVersion>
    
    <!-- 项目包名，将被作为模块的唯一标识  -->
    <groupId>com.levin.codegen.example</groupId>

    <artifactId>codegen-example</artifactId>
    <version>${revision}</version>

    <name>XX项目名称</name>
    <description>XX项目描述</description>
   
    <packaging>pom</packaging>
     
    <properties>
        <!-- 项目版本号-->
        <revision>1.0.0-SNAPSHOT</revision>

        <!-- Spring Boot 版本，请根据需要修改 -->
        <spring-boot.version>2.7.15</spring-boot.version>

        <levin.simple-dao.groupId>com.github.Levin-Li.simple-dao</levin.simple-dao.groupId>

        <!-- 本地版本包名，请先在本机maven安装simple-dao -->
        <levin.simple-dao.groupId>com.levin.commons</levin.simple-dao.groupId>
        <levin.simple-dao.version>2.6.5-SNAPSHOT</levin.simple-dao.version> 

        <levin.service-support.groupId>com.github.Levin-Li</levin.service-support.groupId>
        <levin.service-support.version>1.2.30-SNAPSHOT</levin.service-support.version>

    </properties>
  
    <repositories> 
        <repository>
            <id>jitpack.io</id>
            <url>https://www.jitpack.io</url>
        </repository> 
    </repositories>

    <pluginRepositories>
        <pluginRepository>
            <!--  插件库 -->
            <id>jitpack.io</id>
            <url>https://www.jitpack.io</url>
        </pluginRepository>
    </pluginRepositories>

    <build>
        <plugins>

            <plugin>
                <groupId>${levin.simple-dao.groupId}</groupId>
                <artifactId>simple-dao-codegen</artifactId>
                <version>${levin.simple-dao.version}</version>
                <configuration>
                    <!-- 生成的控制器代码是否包括目录-->
                    <isCreateControllerSubDir>true</isCreateControllerSubDir>

                    <!-- 是否生成BizController -->
                    <isCreateBizController>true</isCreateBizController>

                    <!-- 生成的DTO的Schema注解中描述的配置是否使用类引用-->
                    <isSchemaDescUseConstRef>true</isSchemaDescUseConstRef>

                    <!-- 集成 OakBaseFramework -->
                    <enableOakBaseFramework>false</enableOakBaseFramework>

                    <!-- 集成 DubboFramework -->
                    <enableDubbo>false</enableDubbo>

                </configuration>
                <dependencies>
                    <dependency>
                        <groupId>${levin.service-support.groupId}</groupId>
                        <artifactId>service-support</artifactId>
                        <version>${levin.service-support.version}</version>
                    </dependency>
                </dependencies>
            </plugin>
 
        </plugins>
    </build>
    
    </project>


##### 2.1.2 生成项目模板和示例文件

   在 IDEA 的 Maven 操作面板上双击插件的 gen-project-template 生成模板文件。
   
   ![Image text](./simple-dao-code-gen/src/main/resources/public/images/step-1.png)
   
   插件将会生成一个示例模块，生成成功后，请刷新项目。 
     
##### 2.1.3 编译实体模块

   在生成好的实体模块上编译项目。
    
   ![编译实体类](./simple-dao-code-gen/src/main/resources/public/images/step-2.png)

##### 2.1.4 生成代码(在实体模块上双击执行插件!)

   在编译成功后的实体模块上，双击插件的 gen-code 开始生成代码。(注意是在实体模块上双击执行插件!)
   
   ![生成代码](./simple-dao-code-gen/src/main/resources/public/images/step-3.png)    
    
   代码生成插件会生成服务类，控制器类，spring boot 自动配置文件，测试用例，插件类等，后续加入会生成 vue和 react 的页面代码。
         
##### 2.1.5 启动bootstrap程序和查看运行结果
   
   在Maven操作面板上刷新项目，然后启动项目，项目启动成功，点击控制台的链接查看运行结果。    
   
   ![查看结果](./simple-dao-code-gen/src/main/resources/public/images/step-5.png)    
   
   So Easy!  
        
        
### 3 用户手册

   生成的标准服务现在默认通过 MapStruct 把完整实体转换为 `Info`。
   `SelectDao.setDefaultResultConverter(Function)` 可设置单次查询的默认转换器；
   `setDefaultResultConverter(Info.class, Function)` 还能让显式查询该类型时使用转换器；
   `setAllowLazyLoading(false)` 默认跳过尚未加载的 `LAZY` 属性，已加载的属性照常映射。
   关联实体及集合中的实体元素交给各自的 Mapper 转换，共享本次转换的延迟加载设置和循环引用缓存。
   `Info` 拷贝时，关联的子 `Info` 和集合元素也由对应实体的 Mapper 递归拷贝。
   `CycleAvoidingMappingContext`、`JsonObjectMapping` 和 `JsonArrayMapping` 由 `simple-dao-core` 的 `com.levin.commons.dao.support` 包提供，代码生成器不再为每个模块生成这三个文件。
   指定列查询继续使用投影映射，详情见 [用户手册](./manual.md)。
   带条数限制的集合 `JOIN FETCH` 在最终 JPA 执行层自动执行 ID 分页和完整关联抓取，避免 Hibernate 内存分页；单值 fetch 和普通 JOIN 保持原路径。
     
   其它请查看 [用户手册](./manual.md) 
   

### 4 鼓励一下
     
   支付宝 微信   
   
  ![支付宝+微信](./public/pay.png)   
      

### 5 联系作者

 邮箱：99668980@qq.com   

## LocalDate 查询条件生成

实体的 `java.time.LocalDate` 字段生成同名 `@Eq` 精确相等条件，并保留 `betweenXxx`、`gteXxx`、`lteXxx`；已有 `@Eq` 配置继续保留。其他日期／时间类型不新增精确相等条件。

## Hibernate 数据库结构安全校验

JPA 模块在 Spring Boot 配置加载完成后、创建容器前校验 `spring.jpa.hibernate.ddl-auto`，仅允许 `none` 或 `update`，未配置时默认 `none`。`create`、`create-drop`、`validate` 等其他值直接导致启动失败。原生 `hibernate.hbm2ddl.auto` 属性也会校验；Hibernate 接收最终属性时再次校验，JPA schema-generation 的数据库动作同样只允许 `none` 或 Hibernate 支持的 `update` 扩展，拒绝创建／删除动作。

同一早期校验还要求 `spring.jpa.open-in-view=false`，未配置时默认 `false`。显式启用或配置其他值时，在创建 Spring 容器之前抛出异常。

## 发布流程

默认仅对被修改的模块执行 `clean deploy`；发布失败后，再对整个项目执行 `clean deploy`。发布时不跳过测试，不自动修改版本号。具体步骤见 [发布流程](docs/release-workflow.md)。

JPA 安全配置的 `none`、`update`、`false` 白名单比较忽略大小写及前后空格；检查不修改原始配置值。
