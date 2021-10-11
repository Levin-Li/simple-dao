package com.levin.commons.dao.codegen.plugins;

import com.levin.commons.dao.codegen.ServiceModelCodeGenerator;
import com.levin.commons.plugins.BaseMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;


/**
 * 扫描当前项目构建后的输出目录，发现目录中的 jpa 实体类，尝试自动生成服务类、控制器类、POM 文件、模块插件类、SpringBoot配置类 、 SpringBoot 启动文件
 */
@Mojo(name = "gen-code", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class CodeGeneratorMojo extends BaseMojo {

    /**
     * 实体类的模块目录名称
     * 如果pom 模块的目录名称如果等于 entitiesModuleDirName 指定的值，则表示这个模块都是实体类
     * 那么就会在 pom 模块所在的同级目录 【servicesModuleDirName  apiModuleDirName】中生成服务类和控制器类
     */
    @Parameter(defaultValue = "entities")
    private String entitiesModuleDirName = "entities";

    /**
     * 生成的服务类的存放位置
     * <p>
     * 如果目录不存在，则会自动创建
     */
    @Parameter(defaultValue = "services")
    private String servicesModuleDirName = "services";

    /**
     * 生成的控制器类的存放位置
     * 如果目录不存在，则会自动创建
     */
    @Parameter(defaultValue = "api")
    private String apiModuleDirName = "api";

    /**
     * testcase
     * 如果目录不存在，则会自动创建
     */
    @Parameter(defaultValue = "testcase")
    private String testcaseModuleDirName = "testcase";

    /**
     * admin-ui
     * 如果目录不存在，则会自动创建
     */
    @Parameter(defaultValue = "admin-ui")
    private String adminUiModuleDirName = "admin-ui";

    /**
     * 强制指定模块的包名
     * 默认会自动识别
     */
    @Parameter
    private String modulePackageName = "";

    /**
     * 模块名称
     * <p>
     * 默认自动获取
     */
    @Parameter
    private String moduleName = "";

    /**
     * 是否强制拆分模块，就是强制把服务类和控制器类生成在 servicesModuleDirName 和  apiModuleDirName 自动的目录中
     * 默认不强制拆分，自动处理
     */
    @Parameter(defaultValue = "false")
    private boolean forceSplitDir = false;

    /**
     * 代码生成的附加参数
     */
    @Parameter
    protected Map<String, Object> codeGenParams;


    {
        independentPluginClassLoader = false;
    }

    @Override
    public void executeMojo() throws MojoExecutionException, MojoFailureException {
        try {

            if (codeGenParams == null) {
                codeGenParams = new LinkedHashMap<>();
            }

            if ("pom".equalsIgnoreCase(mavenProject.getArtifact().getType())) {
                logger.info("代码生成插件仅对非 pom 类型模块有效");
                return;
            }

            String outputDirectory = mavenProject.getBuild().getOutputDirectory();

            File file = new File(outputDirectory);

            if (!file.exists()) {
                logger.warn("*** 代码生成插件: 请先编译" + mavenProject.getArtifact());
                return;
            }

            File basedir = mavenProject.getBasedir();

            boolean splitDir = forceSplitDir;

            String dirPrefix = "";

            final String basedirName = basedir.getName();

            //如果当前项目目录规则匹配
            if (hasText(entitiesModuleDirName)
                    && (basedirName.equals(entitiesModuleDirName)
                    || basedirName.endsWith("-" + entitiesModuleDirName)
                    || basedirName.endsWith("_" + entitiesModuleDirName)
            )) {

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

            String serviceDir = (splitDir && hasText(servicesModuleDirName)) ? dirPrefix + servicesModuleDirName : "";
            String controllerDir = (splitDir && hasText(apiModuleDirName)) ? dirPrefix + apiModuleDirName : "";
            String testcaseDir = (splitDir && hasText(testcaseModuleDirName)) ? dirPrefix + testcaseModuleDirName : "";
            String adminUiDir = (splitDir && hasText(adminUiModuleDirName)) ? dirPrefix + adminUiModuleDirName : "";


            serviceDir = StringUtils.hasLength(serviceDir) ? basedir.getAbsolutePath() + "/../" + serviceDir + "/" + mavenDirStyle : srcDir;
            controllerDir = StringUtils.hasLength(controllerDir) ? basedir.getAbsolutePath() + "/../" + controllerDir + "/" + mavenDirStyle : srcDir;
            testcaseDir = StringUtils.hasLength(testcaseDir) ? basedir.getAbsolutePath() + "/../" + testcaseDir + "/" + mavenDirStyle : srcDir;

            adminUiDir = basedir.getAbsolutePath() + "/../" + adminUiDir;


            serviceDir = new File(serviceDir).getCanonicalPath();
            controllerDir = new File(controllerDir).getCanonicalPath();
            testcaseDir = new File(testcaseDir).getCanonicalPath();
            adminUiDir = new File(adminUiDir).getCanonicalPath();


            if (!hasText(moduleName)) {
                MavenProject projectParent = mavenProject.getParent();
                if (projectParent != null
                        && projectParent.getBasedir() != null
                        && new File(projectParent.getBasedir(), "pom.xml").exists()) {
                    moduleName = projectParent.getArtifactId();
                } else {
                    moduleName = mavenProject.getArtifactId();
                }
            }

            if (!hasText(moduleName)) {
                //下划线或是-号去除，并且转换为大写
                //自动获取模块名称为目录名称
                moduleName = ServiceModelCodeGenerator.splitAndFirstToUpperCase
                        (splitDir ? mavenProject.getBasedir().getParentFile().getName() : mavenProject.getBasedir().getName());
            }

            ServiceModelCodeGenerator.splitDir(splitDir);
            ServiceModelCodeGenerator.moduleName(moduleName);
            ServiceModelCodeGenerator.modulePackageName(modulePackageName);

            getLog().info(String.format(" *** 模块名称：{%s} ，模块包名：{%s} ， 服务类生成路径：{%s}，控制器类生成路径：{%s}", moduleName, modulePackageName, serviceDir, controllerDir));

            //生成代码
            ServiceModelCodeGenerator.genCodeAsMavenStyle(mavenProject, getClassLoader()
                    , outputDirectory, controllerDir, serviceDir, codeGenParams);

            if (splitDir) { //尝试生成Pom 文件

                ServiceModelCodeGenerator.tryGenTestcase(mavenProject, controllerDir, serviceDir, testcaseDir, codeGenParams);
                ServiceModelCodeGenerator.tryGenPomFile(mavenProject, controllerDir, serviceDir, testcaseDir, codeGenParams);

                //生成界面文件
                ServiceModelCodeGenerator.tryGenAdminUiFile(mavenProject, controllerDir, serviceDir, adminUiDir, codeGenParams);
            }

            //生成
            ServiceModelCodeGenerator.tryGenSpringBootStarterFile(mavenProject, controllerDir, serviceDir, codeGenParams);

        } catch (Exception e) {
            getLog().error(mavenProject.getArtifactId() + " 模块代码生成错误：" + e.getMessage(), e);
        }
    }


}
