package com.levin.commons.dao.codegen.plugins;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.levin.commons.dao.codegen.ServiceModelCodeGenerator;
import com.levin.commons.dao.codegen.db.*;
import com.levin.commons.dao.codegen.db.util.FieldUtil;
import com.levin.commons.plugins.BaseMojo;
import com.levin.commons.utils.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * springBootStarterParentVersion
     * 默认  "2.3.5.RELEASE"
     */
    @Parameter
    private String springBootStarterParentVersion = "2.7.8";

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
     * 要忽略的表
     */
    @Parameter
    private String[] ignoreTables;

    /**
     * 要生成实体类的表名称前缀
     */
    @Parameter
    private String defaultTableNamePrefix = "";

    {
        independentPluginClassLoader = false;
    }

    @Override
    public void executeMojo() throws MojoExecutionException, MojoFailureException {

        try {

            File basedir = mavenProject.getBasedir();

            boolean isPomModule = "pom".equalsIgnoreCase(mavenProject.getPackaging());

            if (!isPomModule) {
                logger.warn("***【表生成实体类插件】*** 请尽量在Pom模块中执行");
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

            new File(entitiesModuleDir, "src/main/resources").mkdirs();

            String resTemplateDir = "simple-dao/codegen/template/entity/";

            //拷贝 POM 文件

            MapUtils.Builder<String, String> mapBuilder =
                    MapUtils.put("CLASS_PACKAGE_NAME", modulePackageName + ".entities")
                            .put("modulePackageName", modulePackageName)
                            .put("now", new Date().toString());


            copyAndReplace(false, resTemplateDir + "实体类开发规范.md", new File(entitiesDir, "实体类开发规范.md"), mapBuilder.build());
            copyAndReplace(false, resTemplateDir + "package-info.java", new File(entitiesDir, "package-info.java"), mapBuilder.build());

            copyAndReplace(false, resTemplateDir + "EntityConst.java", new File(entitiesDir, "EntityConst.java"), mapBuilder.build());

            YamlPropertiesFactoryBean yamlProperties = new YamlPropertiesFactoryBean();

            File resDir = new File(basedir, "src/main/resources");

            if (!resDir.exists()) {
                resDir.mkdirs();
            }

            FileSystemResource dbConfigRes = StringUtils.hasText(springbootProfile)
                    ? new FileSystemResource(new File(resDir, "application-" + springbootProfile + ".yml"))
                    : new FileSystemResource(new File(resDir, "application.yml"));

            if (!dbConfigRes.exists()) {

                FileUtil.writeString("spring.datasource:\n" +
                                "  url: 'jdbc:mysql://127.0.0.1:3306/db'\n" +
                                "  #需要生成实体类的表前缀\n" +
                                "  genEntityTablePrefix: \n" +
                                "  username: root\n" +
                                "  password: \n",
                        dbConfigRes.getFile(),
                        "utf-8");

                // dbConfigRes = new ClassPathResource(dbConfigRes.getPath());

                logger.info("请在{}中配置数据库连接相关信息，目前支持 mysql oracle sqlserver dm 等数据库", dbConfigRes.getFile());

                return;
            }

            yamlProperties.setResources(dbConfigRes);

            yamlProperties.afterPropertiesSet();

            Properties props = yamlProperties.getObject();

            String dsPrefix = "spring.datasource.";

            DbConfig dbConfig = new DbConfig()
                    .setDbName(props.getProperty(dsPrefix + "dbName"))
                    .setJdbcUrl(props.getProperty(dsPrefix + "url", defaultJdbcUrl))
                    .setUsername(props.getProperty(dsPrefix + "username", defaultJdbcUsername))
                    .setPassword(props.getProperty(dsPrefix + "password", defaultJdbcPassword));

            String tablePrefix = props.getProperty(dsPrefix + "genEntityTablePrefix", null);

            if (!StringUtils.hasText(tablePrefix)) {
                tablePrefix = defaultTableNamePrefix;
            }

            if (!StringUtils.hasText(tablePrefix)) {
                getLog().warn("*** 可以定义：" + dsPrefix + "genEntityTablePrefix" + " 指定要生成实体类的表前缀，同事会去除前缀");
            } else {

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

                if (StringUtils.hasText(tablePrefix)) {

                    if (!tableName.toLowerCase().startsWith(tablePrefix.toLowerCase())) {
                        //如果不是指定前缀的表
                        logger.info("非指定前缀[{}],忽略表:{}", tablePrefix, tableName);
                        continue;
                    }
                }


                final String entityName = FieldUtil.upperFirstLetter(StrUtil.toCamelCase(hasText(tablePrefix) ? tableName.substring(tablePrefix.length()) : tableName));


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

                Map<String, Object> params = MapUtil
                        .builder("fields", (Object) tableDefinition.getColumnDefinitions())
                        .put("attrs", fun)
                        .put("keywordFun", keywordFun)
                        .put("entityName", entityName)
                        .put("serialVersionUID", "9876543210")
                        .put("entityComment", tableDefinition.getComment())
                        .put("entitySchema", tableDefinition.getSchema())
                        .put("entityPkName", StrUtil.toCamelCase(tableDefinition.getPkColumn().getColumnName()))
                        .map();

                params.putAll(mapBuilder.build());

                ServiceModelCodeGenerator.genFileByTemplate("entity/Entity.java.ftl", params, outFile.getCanonicalPath());
            }


            if (!isPomModule) {
                return;
            }

            final Map<String, String> pluginInfo = getPluginInfo("codegen-plugin.");

            mavenProject.getBuildPlugins().stream()
                    .filter(plugin -> "simple-dao-codegen".equalsIgnoreCase(plugin.getArtifactId()))
                    .filter(dependency -> dependency.getGroupId().toLowerCase().contains(".levin"))
                    .findAny()
                    .ifPresent(plugin -> {

                        logger.info("find codegen plugin: " + plugin);

                        pluginInfo.putIfAbsent("codegen-plugin.groupId", plugin.getGroupId());
                        pluginInfo.putIfAbsent("codegen-plugin.version", plugin.getVersion());

                        plugin.getDependencies().stream()
                                .filter(dependency -> "service-support".equalsIgnoreCase(dependency.getArtifactId()))
                                .filter(dependency -> dependency.getGroupId().toLowerCase().contains(".levin"))
                                .findAny().ifPresent(dependency -> {

                                    logger.info("find service-support: " + dependency);

                                    pluginInfo.putIfAbsent("service-support.groupId", dependency.getGroupId());
                                    pluginInfo.putIfAbsent("service-support.version", dependency.getVersion());
                                });
                    });


            mapBuilder
                    .put("parent.groupId", mavenProject.getGroupId())
                    .put("parent.artifactId", mavenProject.getArtifactId())
                    .put("parent.version", mavenProject.getVersion())
                    .put(pluginInfo);


            if (hasSubModule) {

                //生成 POM 文件

                mapBuilder.put("modules", "\n<module>" + entitiesModuleDir.getName() + "</module>\n");
                mapBuilder.put("project.artifactId", subModuleName);

                mapBuilder.put("project.packaging", "pom");

                copyAndReplace(false, resTemplateDir + "root-pom.xml", new File(new File(basedir, subModuleName), "pom.xml"), mapBuilder.build());

                //变更父构建名称
                mapBuilder.put("parent.artifactId", subModuleName);

            }

            mapBuilder.put("modules", "");

            mapBuilder.put("project.packaging", "jar");

            //设置构建名称为：父节点的名称加上本节点的名称
            mapBuilder.put("project.artifactId", (hasSubModule ? subModuleName : mavenProject.getArtifactId()) + "-" + entitiesModuleDir.getName());

            copyAndReplace(false, resTemplateDir + "entities-pom.xml", new File(entitiesModuleDir, "pom.xml"), mapBuilder.build());

            String moduleName = hasSubModule ? this.subModuleName : entitiesModuleDir.getName();

            //如果是 root 项目
            if (mavenProject.isExecutionRoot() && mavenProject.getParentFile() == null) {

                //直接整个覆盖
                mapBuilder.put("modules", hasText(moduleName) ? "<module>" + moduleName + "</module>\n" : "");

                if (mavenProject.getParent() == null
                        || !hasText(mavenProject.getParent().getGroupId())) {

                    mapBuilder.put("parent.groupId", "org.springframework.boot")
                            .put("parent.artifactId", "spring-boot-starter-parent")
                            .put("parent.version", springBootStarterParentVersion);
                } else {
                    mapBuilder.put("parent.groupId", mavenProject.getParent().getGroupId())
                            .put("parent.artifactId", mavenProject.getParent().getArtifactId())
                            .put("parent.version", mavenProject.getParent().getVersion());
                }

                //
                mapBuilder.put("project.groupId", mavenProject.getGroupId())
                        .put("project.artifactId", mavenProject.getArtifactId())
                        .put("project.version", mavenProject.getVersion());


                File pomFile = new File(basedir, "pom.xml");

                // 自动生成标记，请不要删除本行 simple-dao-codegen-flag=${parent.groupId}
                boolean overwrite = !FileUtils.readFileToString(pomFile, "utf-8").contains("simple-dao-codegen-flag=" + mapBuilder.build().get("parent.groupId"));

                copyAndReplace(overwrite, resTemplateDir + "root-pom.xml", pomFile, mapBuilder.build());

                ServiceModelCodeGenerator.genFileByTemplate("gitignore.ftl", new LinkedHashMap<>(mapBuilder.build()), new File(basedir, ".gitignore").getCanonicalPath());

            } else {
                updatePom(mavenProject, moduleName);
            }

        } catch (Exception e) {
            getLog().error(mavenProject.getArtifactId() + " 模块模板生成错误：" + e.getMessage(), e);
        }
    }

    private static void updatePom(MavenProject mavenProject, String... moduleNames) throws IOException {

        List<String> stringList = Stream.of(moduleNames)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.toList());

        if (stringList.isEmpty()) {
            return;
        }

        File basedir = mavenProject.getBasedir();

        //修改现有的 Pom 文件,增加模块名称
        String genFlags = "###simple-dao-code-gen-module-flag###";

        String moduleList = "\n<!-- " + genFlags + " -->\n";

        for (String moduleName : stringList) {
            moduleList += "<module>" + moduleName + "</module>\n";
        }

        File pomFile = new File(basedir, "pom.xml");

        StringBuilder pomContent = new StringBuilder(FileUtils.readFileToString(pomFile, "utf-8"));

        //如果没有生成标记
        if (pomContent.indexOf(genFlags) == -1) {

            int indexOf = pomContent.indexOf("</modules>");

            if (indexOf == -1) {
                pomContent.insert(pomContent.indexOf("</project>"), "<modules>\n" + moduleList + "\n</modules>\n");
            } else {
                pomContent.insert(indexOf, moduleList);
            }

            FileUtils.write(pomFile, pomContent, "utf-8");
        }
    }


}
