package com.levin.commons.dao.codegen.plugins;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.levin.commons.dao.codegen.ServiceModelCodeGenerator;
import com.levin.commons.dao.codegen.db.*;
import com.levin.commons.dao.codegen.db.util.FieldUtil;
import com.levin.commons.plugins.BaseMojo;
import com.levin.commons.utils.MapUtils;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.PatternMatchUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.levin.commons.dao.codegen.ServiceModelCodeGenerator.TEMPLATE_PATH;
import static org.springframework.util.StringUtils.hasText;


/**
 * 从数据库生成项目实体类
 */
@Mojo(name = "gen-project-entity-form-db", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ProjectEntityGeneratorMojo extends BaseMojo {

    /**
     * 生成的子模块名称
     * 如果为空，则表示示例代码生成在当前模块,不会创建子模块
     * 如果当前模块不是 pom 模块，则subModuleName名称没有意义，也不会创建子模块。
     */
    @Parameter
    private String subModuleName = "";

    /**
     * 模块包名
     * 如果没有配置，则自动获取 pom 文件中定义的 GroupId
     */
    @Parameter
    private String modulePackageName = "";

    /**
     * Spring boot 数据库配置的 profile
     */
    @Parameter
    private String springbootProfile = "dev";

    /**
     * 默认数据库连接
     */
    @Parameter
    private String defaultJdbcUrl = "";

    /**
     * 默认数据库连接用户名
     */
    @Parameter
    private String defaultJdbcUsername = "";


    /**
     * 默认数据库连接密码
     */
    @Parameter
    private String defaultJdbcPassword = "";

    /**
     * 是否允许使用Dubbo，自动生成Dubbo相关的配置
     */
    @Parameter
    private boolean enableDubbo = false;

    /**
     * 是否导入oak_base 框架
     */
    @Parameter
    private boolean enableOakBaseFramework = false;

    /**
     * 生成代码的字段上的Schema注解，描述是否使用常量引用，默认使用。
     * <p>
     * 例子：使用时
     *
     * @Schema(description = L_planName )
     * <p>
     * 不使用时
     * @Schema(description = "计划名称" )
     */
    @Parameter(defaultValue = "true")
    private boolean isSchemaDescUseConstRef = true;

    /**
     * 生成的控制器类是否创建子目录
     */
    @Parameter
    private boolean isCreateControllerSubDir = false;

    /**
     * 是否生成业务控制器类
     */
    @Parameter(defaultValue = "true")
    private boolean isCreateBizController = true;

    /**
     * springboot 版本
     */
    @Parameter
    private String springBootStarterParentVersion = "4.0.5";

    /**
     * 要忽略的表
     */
    @Parameter
    private String[] ignoreTables;

    /**
     * 要生成实体类的表名称前缀
     */
    @Parameter
    private String defaultTableNameMatchPattern = "";

    {
        independentPluginClassLoader = false;
    }


    public static boolean anyMatch(ColumnDefinition column, List<String> nameKeywordList, List<String> labelKeywordList) {
        //如果匹配主关键字
        return anyMatch(column.getCamelCaseName(), nameKeywordList, null)
                || anyMatch(column.getComment(), labelKeywordList, null)
                ;
    }

    public static boolean anyMatch(String text, List<String> keywordList, List<String> suffixList) {

        if (StrUtil.isBlank(text) || keywordList == null || keywordList.isEmpty()) {
            return false;
        }

        //重点逻辑 匹配以什么结尾 或是 等于指定的关键字
        return anyMatch(text::endsWith, keywordList, suffixList)
                //首字母小写,等于比较
                || anyMatch(text::equals, keywordList.stream().map(StrUtil::lowerFirst).collect(Collectors.toList()), suffixList);
    }

    public static boolean anyMatch(Predicate<String> predicate, List<String> keywordList, List<String> suffixList) {
        //如果匹配主关键字
        return keywordList.stream().anyMatch(predicate)
                || (
                //主关键字不能为空
                !keywordList.isEmpty()
                        && suffixList != null
                        && suffixList.stream().anyMatch(suffix -> keywordList.stream().map(k -> k + suffix).anyMatch(predicate))
        );
    }


    @Override
    public void executeMojo() throws Exception {

        File basedir = mavenProject.getBasedir();

        boolean isPomModule = "pom".equalsIgnoreCase(mavenProject.getPackaging());

        if (!isPomModule) {
            logger.warn("***【表生成实体类插件】*** 请在Pom模块中执行本插件");
            // return;
        }

        if (this.subModuleName != null) {
            subModuleName = subModuleName.trim();
        }

        if (!hasText(this.modulePackageName)) {
            modulePackageName = mavenProject.getGroupId();
        }

        boolean hasSubModule = hasText(this.subModuleName);

        File entitiesModuleDir = new File(basedir, isPomModule ? this.subModuleName + "/entities" : "");

        entitiesModuleDir.mkdirs();

        File entitiesDir = new File(entitiesModuleDir, "src/main/java/" + modulePackageName.replace('.', '/') + "/entities");

        entitiesDir.mkdirs();

        new File(basedir, "docs").mkdirs();

        new File(entitiesModuleDir, "src/main/resources").mkdirs();

        String resTemplateDir = TEMPLATE_PATH + "entity/";

        Map<Object, Object> mavenProperties = new HashMap<>();

        mavenProperties.putAll(mavenSession.getSystemProperties());
        mavenProperties.putAll(mavenSession.getUserProperties());

        mavenProperties.putAll(mavenProject.getProperties());
        //拷贝 POM 文件

        MapUtils.Builder<String, Object> mapBuilder = MapUtils.putFirst("__mavenProject", mavenProject);

        //
        mavenProperties.forEach((k, v) -> mapBuilder.put(k.toString(), v));

        mapBuilder.put("spring_boot__version", mavenProperties.getOrDefault("spring-boot.version", this.springBootStarterParentVersion))
                .put("now", (Object) new Date().toString())
                .put("CLASS_PACKAGE_NAME", modulePackageName + ".entities")
                .put("modulePackageName", modulePackageName);

        mapBuilder.put("enableOakBaseFramework", this.enableOakBaseFramework);
        mapBuilder.put("enableDubbo", this.enableDubbo);

        mapBuilder.put("isCreateBizController", isCreateBizController);
        mapBuilder.put("isCreateControllerSubDir", isCreateControllerSubDir);
        mapBuilder.put("isSchemaDescUseConstRef", isSchemaDescUseConstRef);

        mapBuilder.put("mavenProject", mavenProject);
        mapBuilder.put("mavenSession", mavenSession);
        mapBuilder.put("artifactId", mavenSession.getCurrentProject().getArtifactId());
        mapBuilder.put("basedir", mavenProject.getBasedir());
        mapBuilder.put("projectName", mavenProject.getName());
        mapBuilder.put("projectDesc", mavenProject.getDescription());


        copyAndReplace(false, resTemplateDir + "实体类开发规范.md", new File(entitiesDir, "实体类开发规范.md"), mapBuilder.build());


        Map<String, Object> tempParams = new LinkedHashMap<>(mapBuilder.build());

        ServiceModelCodeGenerator.genFileByTemplate("entity/package-info.java", tempParams, new File(entitiesDir, "package-info.java").getCanonicalPath());
        ServiceModelCodeGenerator.genFileByTemplate("entity/EntityConst.java", tempParams, new File(entitiesDir, "EntityConst.java").getCanonicalPath());

        YamlPropertiesFactoryBean yamlProperties = new YamlPropertiesFactoryBean();

        File resDir = new File(basedir, "src/main/resources");

        if (!resDir.exists()) {
            resDir.mkdirs();
        }

        FileSystemResource dbConfigRes = StringUtils.hasText(springbootProfile)
                ? new FileSystemResource(new File(resDir, "application-" + springbootProfile + ".yml"))
                : new FileSystemResource(new File(resDir, "application.yml"));

        if (!dbConfigRes.exists()) {

            FileUtil.writeString(
                    "spring.datasource:\n" +
                            "  url: 'jdbc:postgresql://127.0.0.1:5432/db'\n" +
                            "  username: postgres\n" +
                            "  password: \n" +
                            "  driver-class-name: org.postgresql.Driver\n" +

                            "codegen:\n" +
                            "  #需要生成实体类的表前缀,可以*模糊匹配,默认所有的表都生成\n" +
                            "  tableNameMatchPattern: \n" +
                            "  #是否使用表名, 不进行表名转换, 重要配置\n" +
                            "  useTableName: false\n" +
                            "  #是否使用字段名, 不进行字段名转换, 重要配置\n" +
                            "  useColumnName: false",

                    dbConfigRes.getFile(),
                    "utf-8");

            // dbConfigRes = new ClassPathResource(dbConfigRes.getPath());

            logger.error("请在{}中配置数据库连接相关信息，目前支持 PostgreSQL、MySQL、Oracle、SQL Server、DM 等数据库。", dbConfigRes.getFile());

            return;
        }

        yamlProperties.setResources(dbConfigRes);

        yamlProperties.afterPropertiesSet();

        final Properties genProps = yamlProperties.getObject();

        String dsPrefix = "spring.datasource.";

        DbConfig dbConfig = new DbConfig()
                .setDbName(genProps.getProperty(dsPrefix + "dbName"))
                .setJdbcUrl(genProps.getProperty(dsPrefix + "url", defaultJdbcUrl))
                .setUsername(genProps.getProperty(dsPrefix + "username", defaultJdbcUsername))
                .setPassword(genProps.getProperty(dsPrefix + "password", defaultJdbcPassword));

        if (StrUtil.isBlank(dbConfig.getJdbcUrl())) {
            logger.error("请在{}中配置有效的数据库连接相关信息，目前支持 PostgreSQL、MySQL、Oracle、SQL Server、DM 等数据库。", dbConfigRes.getFile());
            return;
        }


        final String genPrefix = "codegen";

        String tableNameMatchPattern = genProps.getProperty(genPrefix + ".tableNameMatchPattern", null);

        if (!StringUtils.hasText(tableNameMatchPattern)) {
            tableNameMatchPattern = defaultTableNameMatchPattern;
        }

        if (!StringUtils.hasText(tableNameMatchPattern)) {
            logger.warn("*** 可以定义：" + dsPrefix + "genEntityTablePrefix" + " 指定要生成实体类的表前缀，同时会去除前缀");
        } else {
            logger.info("需要生成实体类的表名前缀：{}", tableNameMatchPattern);
        }

        getLog().info("开始读取数据库：" + dbConfig);

        SQLService sqlService = SQLServiceFactory.build(dbConfig);

        TableSelector tableSelector = sqlService.getTableSelector(dbConfig);

        for (TableDefinition tableDefinition : tableSelector.getTableDefinitions()) {

            final String tableName = tableDefinition.getTableName();

            if (ignoreTables != null && ignoreTables.length > 0
                    && Stream.of(ignoreTables).anyMatch(ignoreTable -> tableName.equalsIgnoreCase(ignoreTable))) {
                continue;
            }

            if (StringUtils.hasText(tableNameMatchPattern)
                    && !PatternMatchUtils.simpleMatch(tableNameMatchPattern.split(","), tableName)) {
                //如果不是指定前缀的表
                logger.info("指定要求前缀[{}],忽略表:{}", tableNameMatchPattern, tableName);
                continue;
            }

            final String entityName = FieldUtil.upperFirstLetter(StrUtil.toCamelCase(hasText(tableNameMatchPattern) ? tableName.substring(tableNameMatchPattern.length()) : tableName));

            File outFile = new File(entitiesDir, entityName + ".java");

            if (outFile.exists()) {
                continue;
            }

            logger.info("开始生成实体类:{} -> {}", entityName, outFile);

            /**
             *
             */
            Predicate<String> fun = (name) -> tableDefinition.getColumnDefinitions()
                    .parallelStream()
                    .filter(columnDefinition -> columnDefinition.getCamelCaseName().equals(name))
                    .findAny()
                    .isPresent();

            /**
             * 名称为关键字或是以关键字结尾
             */
            BiPredicate<String, String> keywordFun = (name, keywords) -> StrUtil.split(keywords, ',').parallelStream()
                    .map(keyword -> keyword.trim())
                    .filter(keyword -> !StrUtil.isBlank(keyword))
                    .filter(keyword -> name.equals(keyword) || name.endsWith(StrUtil.upperFirst(keyword)))
                    .findAny()
                    .isPresent();


            List<ColumnDefinition> embeddedIdColumns = tableDefinition.getEmbeddedIdColumns();

            //
            String entityBizName = tableDefinition.getComment().replaceAll("\\s", "");

            while (entityBizName.endsWith("表")) {
                entityBizName = entityBizName.substring(0, entityBizName.length() - 1);
            }

            if (StrUtil.isBlank(entityBizName)) {
                entityBizName = StrUtil.toCamelCase(tableName);
            }

            Map<String, Object> params = MapUtil
                    .builder("fields", (Object) tableDefinition.getColumnDefinitions().stream()
                            .filter(columnDefinition -> !embeddedIdColumns.contains(columnDefinition))
                            .sorted(Comparator.comparingInt(this::getOrder))
                            .collect(Collectors.toList())

                    )
                    .put("attrs", fun)
                    .put("embeddedIdColumns", embeddedIdColumns)


                    .put("entity", tableDefinition)

                    .put("useTableName", "true".equalsIgnoreCase(genProps.getProperty(genPrefix + ".useTableName", "false").trim()))
                    .put("useColumnName", "true".equalsIgnoreCase(genProps.getProperty(genPrefix + ".useColumnName", "false").trim()))

                    .put("keywordFun", keywordFun)
                    .put("entityName", entityName)
                    .put("serialVersionUID", "9876543210")

                    .put("entityComment", entityBizName)
                    .put("entitySchema", tableDefinition.getSchema())
                    .put("entityPkName", StrUtil.toCamelCase(tableDefinition.getPkColumn().getColumnName()))
                    .map();

            params.putAll(mapBuilder.build());

            ServiceModelCodeGenerator.genFileByTemplate("entity/Entity.java.ftl", params, outFile.getCanonicalPath());
        }

    }


    protected int getOrder(ColumnDefinition cd) {

        if (cd.getIsPk()) {
            return 1;
        } else if (cd.getCamelCaseName().endsWith("Name")
                || (StrUtil.isNotBlank(cd.getComment()) && Stream.of("名", "名称").anyMatch(name -> cd.getComment().endsWith(name)))
                || cd.getCamelCaseName().endsWith("name")) {
            return 2;
        }

        return 100;

    }


}
