package com.levin.commons.dao.codegen.plugins;

import com.levin.commons.utils.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.util.StringUtils.hasText;


/**
 * 生成项目模板
 */
@Mojo(name = "gen-project-template", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ProjectTemplateGeneratorMojo extends BaseMojo {

    /**
     * 生成的子模块名称
     * 如果为空，则表示示例代码生成在当前模块,不会创建子模块
     * 如果当前模块不是 pom模块，则subModuleName名称没有意义。
     */
    @Parameter
    private String subModuleName = "";

    /**
     * 模块包名
     */
    @Parameter
    private String packageName = "";


    @Override
    public void executeMojo() throws MojoExecutionException, MojoFailureException {

        try {

            File basedir = mavenProject.getBasedir();

            boolean isPomModule = "pom".equalsIgnoreCase(mavenProject.getPackaging());

            if (this.subModuleName != null) {
                subModuleName = subModuleName.trim();
            }

            if (!hasText(this.packageName)) {
                packageName = mavenProject.getGroupId();
            }


            boolean hasSubModule = hasText(this.subModuleName);

            File entitiesModuleDir = new File(basedir, isPomModule ? this.subModuleName + "/entities" : "");

            entitiesModuleDir.mkdirs();

            File entitiesDir = new File(entitiesModuleDir, "src/main/java/" + packageName.replace('.', '/') + "/entities");

            entitiesDir.mkdirs();

            new File(entitiesModuleDir, "src/main/resources").mkdirs();

            String resTemplateDir = "simple-dao/codegen/template/example/";

            //拷贝 POM 文件

            MapUtils.Builder<String, String> mapBuilder =
                    MapUtils.put("CLASS_PACKAGE_NAME", packageName + ".entities")
                            .put("modulePackageName", packageName)
                            .put("now", new Date().toString());


            copyAndReplace(false, resTemplateDir + "TableOption.java", new File(entitiesDir, "TableOption.java"), mapBuilder.build());

            copyAndReplace(false, resTemplateDir + "Group.java", new File(entitiesDir, "Group.java"), mapBuilder.build());
            copyAndReplace(false, resTemplateDir + "User.java", new File(entitiesDir, "User.java"), mapBuilder.build());
            copyAndReplace(false, resTemplateDir + "Task.java", new File(entitiesDir, "Task.java"), mapBuilder.build());


            if (isPomModule) {

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


                String pomResFile = "pom.xml";

                if (hasSubModule) {

                    //生成 POM 文件

                    mapBuilder.put("modules", "\n<module>" + entitiesModuleDir.getName() + "</module>\n");
                    mapBuilder.put("project.artifactId", subModuleName);

                    mapBuilder.put("project.packaging", "pom");

                    copyAndReplace(false, resTemplateDir + pomResFile, new File(new File(basedir, subModuleName), "pom.xml"), mapBuilder.build());

                    //变更父构建名称
                    mapBuilder.put("parent.artifactId", subModuleName);

                    pomResFile = "simple-pom.xml";
                } else {

                }


                mapBuilder.put("modules", "");

                mapBuilder.put("project.packaging", "jar");

                //设置构建名称为：父节点的名称加上本节点的名称
                mapBuilder.put("project.artifactId", (hasSubModule ? subModuleName : mavenProject.getArtifactId()) + "-" + entitiesModuleDir.getName());

                copyAndReplace(false, resTemplateDir + pomResFile, new File(entitiesModuleDir, "pom.xml"), mapBuilder.build());

                updatePom(mavenProject, hasSubModule ? this.subModuleName : entitiesModuleDir.getName());

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
