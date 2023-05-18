package com.levin.commons.dao.codegen.plugins;

import com.levin.commons.dao.codegen.ServiceModelCodeGenerator;
import com.levin.commons.plugins.BaseMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Arrays;
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
     * bootstrap
     * 如果目录不存在，则会自动创建
     */
    @Parameter(defaultValue = "bootstrap")
    private String bootstrapModuleDirName = "bootstrap";

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
    @Parameter(defaultValue = "true")
    private boolean isCreateControllerSubDir = true;


    /**
     * 生成的控制器类是否创建子目录
     */
    @Parameter(defaultValue = "true")
    private boolean isCreateBizController = true;


    /**
     * 忽略的实体类，类名正则表达式
     */
    @Parameter
    protected String[] ignoreEntities = {};

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

            //是否分割目录
            boolean splitDir = forceSplitDir;

            String dirPrefix = "";

            //如果当前的项目目录名称
            final String basedirName = basedir.getName();

            //如果当前项目目录规则匹配，和配置指定的实体目录相匹配，则认为读独立的实体目录
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
            String bootstrapDir = (splitDir && hasText(bootstrapModuleDirName)) ? dirPrefix + bootstrapModuleDirName : "";
            String adminUiDir = (splitDir && hasText(adminUiModuleDirName)) ? dirPrefix + adminUiModuleDirName : "";


            serviceDir = StringUtils.hasLength(serviceDir) ? basedir.getAbsolutePath() + "/../" + serviceDir + "/" + mavenDirStyle : srcDir;
            controllerDir = StringUtils.hasLength(controllerDir) ? basedir.getAbsolutePath() + "/../" + controllerDir + "/" + mavenDirStyle : srcDir;
            bootstrapDir = StringUtils.hasLength(bootstrapDir) ? basedir.getAbsolutePath() + "/../" + bootstrapDir + "/" + mavenDirStyle : srcDir;

            adminUiDir = basedir.getAbsolutePath() + "/../" + adminUiDir;


            serviceDir = new File(serviceDir).getCanonicalPath();
            controllerDir = new File(controllerDir).getCanonicalPath();
            bootstrapDir = new File(bootstrapDir).getCanonicalPath();
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

            ServiceModelCodeGenerator.isCreateControllerSubDir(this.isCreateControllerSubDir);
            ServiceModelCodeGenerator.isCreateBizController(this.isCreateBizController);
            ServiceModelCodeGenerator.ignoreEntities(Arrays.asList(this.ignoreEntities));

            ServiceModelCodeGenerator.splitDir(splitDir);
            ServiceModelCodeGenerator.moduleName(moduleName);
            ServiceModelCodeGenerator.modulePackageName(modulePackageName);
            ServiceModelCodeGenerator.isSchemaDescUseConstRef(isSchemaDescUseConstRef);

            getLog().info(String.format(" *** 模块名称：{%s} ，模块包名：{%s} ， 服务类生成路径：{%s}，控制器类生成路径：{%s}", moduleName, modulePackageName, serviceDir, controllerDir));

            codeGenParams.putIfAbsent("mavenProject", mavenProject);
            codeGenParams.putIfAbsent("mavenSession", mavenSession);
            codeGenParams.putIfAbsent("artifactId", mavenSession.getCurrentProject().getArtifactId());
            codeGenParams.putIfAbsent("basedir", mavenProject.getBasedir());
            codeGenParams.putIfAbsent("modulePackageName", modulePackageName);
            codeGenParams.putIfAbsent("moduleName", moduleName);
            codeGenParams.putIfAbsent("projectName", mavenProject.getName());
            codeGenParams.putIfAbsent("projectDesc", mavenProject.getDescription());


            codeGenParams.putIfAbsent("serviceDir", serviceDir);
            codeGenParams.putIfAbsent("controllerDir", controllerDir);
            codeGenParams.putIfAbsent("bootstrapDir", bootstrapDir);
            codeGenParams.putIfAbsent("adminUiDir", adminUiDir);

            //1、生成代码
            ServiceModelCodeGenerator.genCodeAsMavenStyle(mavenProject, getClassLoader()
                    , outputDirectory, controllerDir, serviceDir, codeGenParams);

            //2、生成辅助文件
            if (splitDir) { //尝试生成Pom 文件

                ServiceModelCodeGenerator.tryGenBootstrap(mavenProject, controllerDir, serviceDir, bootstrapDir, codeGenParams);
                ServiceModelCodeGenerator.tryGenPomFile(mavenProject, controllerDir, serviceDir, bootstrapDir, codeGenParams);

                //生成界面文件
                ServiceModelCodeGenerator.tryGenAdminUiFile(mavenProject, controllerDir, serviceDir, adminUiDir, codeGenParams);
            }

            //3、生成
            ServiceModelCodeGenerator.tryGenSpringBootStarterFile(mavenProject, controllerDir, serviceDir, codeGenParams);

        } catch (Exception e) {
            getLog().error(mavenProject.getArtifactId() + " 模块代码生成错误：" + e.getMessage(), e);
        }
    }


}
