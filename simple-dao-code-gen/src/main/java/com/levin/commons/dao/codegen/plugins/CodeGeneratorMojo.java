package com.levin.commons.dao.codegen.plugins;

import com.levin.commons.dao.codegen.ServiceModelCodeGenerator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;


@Mojo(name = "gen-code", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CodeGeneratorMojo extends BaseMojo {

    @Parameter
    private boolean useCustomClassLoader = true;

    @Parameter
    private String entitiesModuleDirName = "entities";

    @Parameter
    private String servicesModuleDirName = "services";

    @Parameter
    private String apiModuleDirName = "api";

    /**
     * 是否拆分模块
     * 否则按自动处理
     */
    @Parameter
    private boolean forceSplitDir = false;

    @Parameter
    protected Map<String, Object> genParams;

    @Override
    public void executeMojo() throws MojoExecutionException, MojoFailureException {

        try {

            if ("pom".equalsIgnoreCase(mavenProject.getArtifact().getType())) {
                return;
            }

            String outputDirectory = mavenProject.getBuild().getOutputDirectory();

            File file = new File(outputDirectory);

            if (!file.exists()) {
                return;
            }

            File basedir = mavenProject.getBasedir();

            boolean splitDir = forceSplitDir;

            String dirPrefix = "";

            final String basedirName = basedir.getName();

            //如果当前项目目录规则匹配
            if (StringUtils.hasText(entitiesModuleDirName)
                    && (basedirName.equals(entitiesModuleDirName) || basedirName.endsWith("-" + entitiesModuleDirName))) {

                //如果发现实体目录匹配，也自动分割目录

                int indexOf = basedirName.indexOf(entitiesModuleDirName);

                if (indexOf > 0) {
                    dirPrefix = basedirName.substring(0, indexOf);
                }

                splitDir = true;
            }

            if (!splitDir) {
                //如果发现目录存在，也自动分割目录
                splitDir = new File(basedir, "../" + servicesModuleDirName).exists()
                        || new File(basedir, "../" + apiModuleDirName).exists();
            }

            final String srcDir = mavenProject.getBuild().getSourceDirectory();

            final String mavenDirStyle = new File(srcDir).getCanonicalPath().substring(basedir.getCanonicalPath().length() + 1);

            String serviceDir = (splitDir && StringUtils.hasText(servicesModuleDirName)) ? dirPrefix + servicesModuleDirName : "";
            String controllerDir = (splitDir && StringUtils.hasText(apiModuleDirName)) ? dirPrefix + apiModuleDirName : "";

            serviceDir = StringUtils.hasLength(serviceDir) ? basedir.getAbsolutePath() + "/../" + serviceDir + "/" + mavenDirStyle : srcDir;

            controllerDir = StringUtils.hasLength(controllerDir) ? basedir.getAbsolutePath() + "/../" + controllerDir + "/" + mavenDirStyle : srcDir;

            serviceDir = new File(serviceDir).getCanonicalPath();
            controllerDir = new File(controllerDir).getCanonicalPath();

            //生成代码

            ServiceModelCodeGenerator.genCodeAsMavenStyle(useCustomClassLoader ? getClassLoader() : null
                    , outputDirectory, controllerDir, serviceDir, genParams);


            //尝试生成Pom 文件
            ServiceModelCodeGenerator.tryGenPomFile(mavenProject,dirPrefix,controllerDir,serviceDir,genParams);

            //生成
            ServiceModelCodeGenerator.tryGenSpringBootStarterFile(mavenProject,dirPrefix,controllerDir,serviceDir,genParams);


        } catch (Exception e) {
            getLog().error(mavenProject.getArtifactId() + " 模块代码生成错误：" + e.getMessage(), e);
        }
    }

}
