package com.levin.commons.dao.codegen.plugins;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;


/**
 * 生成项目模板
 */
@Mojo(name = "gen-project-template", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class ProjectTemplateGeneratorMojo extends BaseMojo {

    /**
     * 项目名称
     */
    @Parameter
    private String projectName = "example";

    /**
     * 是否分拆目录
     */
    @Parameter
    private boolean isSplitDir = true;

    /**
     * 项目包名
     */
//    @Parameter
    private String packageName = "com.levin.commons.dao.example";


    @Override
    public void executeMojo() throws MojoExecutionException, MojoFailureException {

        try {
            //如果当前是 POM 项目
            if (!"pomContent".equalsIgnoreCase(mavenProject.getPackaging())) {
                logger.warn("*** 当前模块不是一个POM模块，将忽略项目模板生成。");
                return;
            }

            File basedir = mavenProject.getBasedir();

            File projectDir = new File(basedir, isSplitDir ? projectName + "/entities" : projectName);

            projectDir.mkdirs();

            File entitiesDir = new File(projectDir, "src/main/java/" + packageName + "/entities");

            entitiesDir.mkdirs();

            new File(projectDir, "src/main/resources").mkdirs();

            String resDir = "/simple-dao/codegen/template/example/";

            //拷贝 POM 文件

            FileUtils.copyToFile(getClassLoader().getResourceAsStream(resDir + "pom.xml"), new File(projectDir, "pom.xml"));

            //拷贝示例Java文件

            FileUtils.copyToFile(getClassLoader().getResourceAsStream(resDir + "Group.java"), new File(entitiesDir, "Group.java"));
            FileUtils.copyToFile(getClassLoader().getResourceAsStream(resDir + "User.java"), new File(entitiesDir, "User.java"));
            FileUtils.copyToFile(getClassLoader().getResourceAsStream(resDir + "Task.java"), new File(entitiesDir, "Task.java"));

            //修改现有的 Pom 文件,增加模块名称

            String genFlags = "###simple-dao-code-gen-module-flag###";

            String module = "\n<!-- " + genFlags + " -->\n<module>\n" + projectName + "<module>\n";

            File pomFile = new File(basedir, "pomContent.xml");

            StringBuilder pomContent = new StringBuilder(FileUtils.readFileToString(pomFile, "utf-8"));

            //如果没有生成标记
            if (pomContent.indexOf(genFlags) == -1) {

                int indexOf = pomContent.indexOf("</modules>");

                if (indexOf == -1) {
                    pomContent.insert(pomContent.indexOf("</project>"), "<modules>\n" + module + "</modules>\n");
                } else {
                    pomContent.insert(indexOf, module);
                }

                FileUtils.write(pomFile, pomContent, "utf-8");
            }

        } catch (Exception e) {
            getLog().error(mavenProject.getArtifactId() + " 模板生成错误：" + e.getMessage(), e);
        }
    }


}
