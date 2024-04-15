package com.levin.commons.dao.codegen.plugins;

import com.levin.commons.dao.codegen.ServiceModelCodeGenerator;
import com.levin.commons.plugins.BaseMojo;
import com.levin.commons.plugins.Utils;
import com.levin.commons.utils.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;


/**
 * 生成项目模板
 */
@Mojo(name = "gen-demo-project-template", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ProjectTemplateGeneratorMojo extends BaseMojo {

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
     * springboot 版本
     */
    @Parameter
    private String springBootStarterParentVersion = "2.7.15";

    /**
     * 是否允许使用Dubbo，自动生成Dubbo相关的配置
     */
    @Parameter
    private boolean enableDubbo = false;


    /**
     * 是否导入oak_base 框架
     */
    @Parameter(defaultValue = "true")
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


    {
        independentPluginClassLoader = false;
    }

    @Override
    public void executeMojo() throws Exception {

        File basedir = mavenProject.getBasedir();

        boolean isPomModule = "pom".equalsIgnoreCase(mavenProject.getPackaging());

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

        String resTemplateRootDir = "simple.dao/codegen/template/";
        String resTemplateEntityDir = resTemplateRootDir + "entity/";


        Map<Object, Object> mavenProperties = new HashMap<>();

        mavenProperties.putAll(mavenSession.getSystemProperties());
        mavenProperties.putAll(mavenSession.getUserProperties());

        mavenProperties.putAll(mavenProject.getProperties());
        //拷贝 POM 文件

        MapUtils.Builder<String, Object> mapBuilder = MapUtils.putFirst("__mavenProject", mavenProject);

        //
        mavenProperties.forEach((k, v) -> mapBuilder.put(k.toString(), v));

        mapBuilder.put("spring_boot__version", mavenProperties.getOrDefault("spring-boot.version", this.springBootStarterParentVersion))
                .put("CLASS_PACKAGE_NAME", (modulePackageName + ".entities"))
                .put("modulePackageName", modulePackageName)
                .put("now", new Date().toString());


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


        copyAndReplace(false, resTemplateEntityDir + "实体类开发规范.md", new File(entitiesDir, "实体类开发规范.md"), mapBuilder.build());
//            copyAndReplace(false, resTemplateEntityDir + "package-info.java", new File(entitiesDir, "package-info.java"), mapBuilder.build());
//            copyAndReplace(false, resTemplateEntityDir + "EntityConst.java", new File(entitiesDir, "EntityConst.java"), mapBuilder.build());
//            copyAndReplace(false, resTemplateEntityDir + "TestOrg.java", new File(entitiesDir, "TestOrg.java"), mapBuilder.build());
//            copyAndReplace(false, resTemplateEntityDir + "TestRole.java", new File(entitiesDir, "TestRole.java"), mapBuilder.build());

        Map<String, Object> params = new LinkedHashMap<>(mapBuilder.build());

        ServiceModelCodeGenerator.genFileByTemplate("entity/package-info.java", params, new File(entitiesDir, "package-info.java").getCanonicalPath());
        ServiceModelCodeGenerator.genFileByTemplate("entity/EntityConst.java", params, new File(entitiesDir, "EntityConst.java").getCanonicalPath());
        ServiceModelCodeGenerator.genFileByTemplate("entity/TestOrg.java", params, new File(entitiesDir, "TestOrg.java").getCanonicalPath());
        ServiceModelCodeGenerator.genFileByTemplate("entity/TestRole.java", params, new File(entitiesDir, "TestRole.java").getCanonicalPath());


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

                    pluginInfo.putIfAbsent("codegen_plugin__groupId", plugin.getGroupId());
                    pluginInfo.putIfAbsent("codegen_plugin__version", plugin.getVersion());

                    plugin.getDependencies().stream()
                            .filter(dependency -> "service-support".equalsIgnoreCase(dependency.getArtifactId()))
                            .filter(dependency -> dependency.getGroupId().toLowerCase().contains(".levin"))
                            .findAny().ifPresent(dependency -> {

                                logger.info("find service-support: " + dependency);

                                pluginInfo.putIfAbsent("service_support__groupId", dependency.getGroupId());
                                pluginInfo.putIfAbsent("service_support__version", dependency.getVersion());
                            });
                });

        mapBuilder
                .put("parent__groupId", mavenProject.getGroupId())
                .put("parent__artifactId", mavenProject.getArtifactId())
                .put("parent__version", mavenProject.getVersion())

                .put(new LinkedHashMap<>(pluginInfo));

        if (hasSubModule) {

            //生成 POM 文件

            mapBuilder.put("modules", "\n<module>" + entitiesModuleDir.getName() + "</module>\n");
            mapBuilder.put("project__artifactId", subModuleName);

            mapBuilder.put("project__packaging", "pom");

            //copyAndReplace(false, resTemplateRootDir + "root-pom.xml", new File(new File(basedir, subModuleName), "pom.xml"), mapBuilder.build());

            //生成 POM 文件
            ServiceModelCodeGenerator.genFileByTemplate(true, "root-pom.xml.ftl", new HashMap<>(mapBuilder.build()), new File(new File(basedir, subModuleName), "pom.xml"));

            //变更父构建名称
            mapBuilder.put("parent__artifactId", subModuleName);

        }

        mapBuilder.put("modules", "");

        mapBuilder.put("project__packaging", "jar");

        //设置构建名称为：父节点的名称加上本节点的名称
        mapBuilder.put("project__artifactId", (hasSubModule ? subModuleName : Utils.getModuleName(mavenProject.getArtifactId())) + "-" + entitiesModuleDir.getName());

        //替换旧的生成方式
        //copyAndReplace(false, resTemplateEntityDir + "entities-pom.xml", new File(entitiesModuleDir, "pom.xml"), mapBuilder.build());

        //生成 POM 文件
        ServiceModelCodeGenerator.genFileByTemplate("entity/entities-pom.xml.ftl", new HashMap<>(mapBuilder.build()), new File(entitiesModuleDir, "pom.xml").getCanonicalPath());

        String moduleName = hasSubModule ? this.subModuleName : entitiesModuleDir.getName();

        //如果是 root 项目
        if (mavenProject.isExecutionRoot() && mavenProject.getParentFile() == null) {

            //直接整个覆盖
            mapBuilder.put("modules", hasText(moduleName) ? "<module>" + moduleName + "</module>\n" : "");

            if (mavenProject.getParent() == null
                    || !hasText(mavenProject.getParent().getGroupId())) {

                mapBuilder.put("parent__groupId", "org.springframework.boot")
                        .put("parent__artifactId", "spring-boot-starter-parent")
                        .put("parent__version", springBootStarterParentVersion);
            } else {
                mapBuilder.put("parent__groupId", mavenProject.getParent().getGroupId())
                        .put("parent__artifactId", mavenProject.getParent().getArtifactId())
                        .put("parent__version", mavenProject.getParent().getVersion());
            }

            //
            mapBuilder.put("project__groupId", mavenProject.getGroupId())
                    .put("project__artifactId", mavenProject.getArtifactId())
                    .put("project__version", mavenProject.getVersion());


            File pomFile = new File(basedir, "pom.xml");

            // 自动生成标记，请不要删除本行 simple-dao-codegen-flag=${parent.groupId}
            boolean overwrite = !FileUtils.readFileToString(pomFile, "utf-8").contains("simple-dao-codegen-flag=" + mapBuilder.build().get("parent.groupId"));

            //  copyAndReplace(overwrite, resTemplateRootDir + "root-pom.xml", pomFile, mapBuilder.build());

            //生成 POM 文件
            ServiceModelCodeGenerator.genFileByTemplate(true, "root-pom.xml.ftl", new HashMap<>(mapBuilder.build()), new File(basedir, "pom.xml"));

            ServiceModelCodeGenerator.genFileByTemplate("gitignore.ftl", new LinkedHashMap<>(mapBuilder.build()), new File(basedir, ".gitignore").getCanonicalPath());

        } else {
            updatePom(mavenProject, moduleName);
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
