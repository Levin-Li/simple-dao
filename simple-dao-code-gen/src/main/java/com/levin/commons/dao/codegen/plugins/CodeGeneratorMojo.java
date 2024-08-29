package com.levin.commons.dao.codegen.plugins;

import cn.hutool.core.map.MapUtil;
import com.levin.commons.dao.codegen.ServiceModelCodeGenerator;
import com.levin.commons.plugins.BaseMojo;
import com.levin.commons.plugins.Utils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
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
     * 生成的服务实现类模块
     * <p>
     * 如果目录不存在，则会自动创建
     */
    @Parameter(defaultValue = "services-impl")
    private String servicesImplModuleDirName = "services-impl";

    /**
     * 生成的服务类的存放位置
     * <p>
     * 如果目录不存在，则会自动创建
     */
    @Parameter(defaultValue = "services")
    private String servicesModuleDirName = "services";

    /**
     * 生成的Starter类的存放位置
     * <p>
     * 如果目录不存在，则会自动创建
     */
    @Parameter(defaultValue = "starter")
    private String starterModuleDirName = "starter";

    /**
     * bootstrap
     * 如果目录不存在，则会自动创建
     */
    @Parameter(defaultValue = "bootstrap")
    private String bootstrapModuleDirName = "bootstrap";


    /**
     * 客户端生成的控制器类的存放位置
     * 如果目录不存在，则会自动创建
     */
    @Parameter(defaultValue = "client-api")
    private String clientApiModuleDirName = "client-api";

    /**
     * 管理后台生成的控制器类的存放位置
     * 如果目录不存在，则会自动创建
     */
    @Parameter(defaultValue = "admin-api")
    private String adminApiModuleDirName = "admin-api";

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
     * 缓存注解中，SpelUtils的bean名称
     * <p>
     * 默认自动获取
     */
    @Parameter
    private String cacheSpelUtilsBeanName = "spelUtils";

    /**
     * 是否强制拆分模块，就是强制把服务类和控制器类生成在 servicesModuleDirName 和  apiModuleDirName 自动的目录中
     * 默认不强制拆分，自动处理
     */
    @Parameter(defaultValue = "false")
    private boolean forceSplitDir = false;

    /**
     * 是否输出格式化后Java代码
     */
    @Parameter
    private boolean isOutputFormatCode = false;

    /**
     * 是否忽略代码注释的变更，代码注释变更，被认为代码没有变更
     */
    @Parameter
    private boolean isIgnoreCodeCommentChange = true;

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
    private String springBootStarterParentVersion = "2.7.15";

    /**
     * 忽略的实体类，类名正则表达式
     * 默认忽略测试Demo类
     */
    @Parameter
    protected String[] ignoreEntities = {".+\\.TestOrg", ".+\\.TestRole"};

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

            Map<Object, Object> mavenProperties = new HashMap<>();

            mavenProperties.putAll(mavenSession.getSystemProperties());
            mavenProperties.putAll(mavenSession.getUserProperties());

            mavenProperties.putAll(mavenProject.getProperties());

            mavenProperties.putIfAbsent("spring_boot__version", mavenProperties.getOrDefault("spring-boot.version", this.springBootStarterParentVersion));

            //拷贝 POM 文件
            codeGenParams.putIfAbsent("mavenProject", mavenProject);
            codeGenParams.putIfAbsent("mavenSession", mavenSession);
            codeGenParams.putIfAbsent("artifactId", mavenSession.getCurrentProject().getArtifactId());
            codeGenParams.putIfAbsent("basedir", mavenProject.getBasedir());

            codeGenParams.putIfAbsent("moduleName", mavenProject.getName());
            codeGenParams.putIfAbsent("moduleDesc", mavenProject.getDescription());

            //父项目
            if (mavenProject.getParent() != null) {
                codeGenParams.putIfAbsent("projectName", mavenProject.getParent().getName());
                codeGenParams.putIfAbsent("projectDesc", mavenProject.getParent().getDescription());
            }

            mavenProperties.forEach((k, v) -> codeGenParams.putIfAbsent(k.toString(), v));

            if (!StringUtils.hasText(cacheSpelUtilsBeanName)) {
                logger.error("*** 代码生成插件 *** 插件属性[cacheSpelUtilsBeanName]不能为空");
                return;
            }

            if ("pom".equalsIgnoreCase(mavenProject.getArtifact().getType())) {
                logger.info("代码生成插件仅对非 pom 类型模块有效");
                return;
            }

            String outputDirectory = mavenProject.getBuild().getOutputDirectory();

            File file = new File(outputDirectory);

            if (!file.exists()) {
                logger.error("*** 代码生成插件 *** 请先编译实体模块[" + mavenProject.getArtifact() + "]");
                return;
            }

            logger.info("*** 开始生成代码，代码比较忽略注释:{}，格式化输出:{}，生成的控制器类是否创建子目录:{}，是否生成业务控制器类:{}",
                    isIgnoreCodeCommentChange, isOutputFormatCode, isCreateControllerSubDir, isCreateBizController);

            File basedir = mavenProject.getBasedir();

            ServiceModelCodeGenerator.baseDir(basedir.getParentFile());

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
                        || new File(basedir, "../" + adminApiModuleDirName).exists();
            }

            final String srcDir = mavenProject.getBuild().getSourceDirectory();

            final String mavenDirStyle = new File(srcDir).getCanonicalPath().substring(basedir.getCanonicalPath().length() + 1);

            String serviceDir = (splitDir && hasText(servicesModuleDirName)) ? dirPrefix + servicesModuleDirName : "";
            String serviceImplDir = (splitDir && hasText(servicesImplModuleDirName)) ? dirPrefix + servicesImplModuleDirName : "";

            String starterDir = (splitDir && hasText(starterModuleDirName)) ? dirPrefix + starterModuleDirName : "";
            String adminApiDir = (splitDir && hasText(adminApiModuleDirName)) ? dirPrefix + adminApiModuleDirName : "";
            String clientApiDir = (splitDir && hasText(clientApiModuleDirName)) ? dirPrefix + clientApiModuleDirName : "";
            String bootstrapDir = (splitDir && hasText(bootstrapModuleDirName)) ? dirPrefix + bootstrapModuleDirName : "";
            String adminUiDir = (splitDir && hasText(adminUiModuleDirName)) ? dirPrefix + adminUiModuleDirName : "";


            serviceDir = StringUtils.hasLength(serviceDir) ? basedir.getAbsolutePath() + "/../" + serviceDir + "/" + mavenDirStyle : srcDir;
            serviceImplDir = StringUtils.hasLength(serviceImplDir) ? basedir.getAbsolutePath() + "/../" + serviceImplDir + "/" + mavenDirStyle : srcDir;

            starterDir = StringUtils.hasLength(starterDir) ? basedir.getAbsolutePath() + "/../" + starterDir + "/" + mavenDirStyle : srcDir;
            adminApiDir = StringUtils.hasLength(adminApiDir) ? basedir.getAbsolutePath() + "/../" + adminApiDir + "/" + mavenDirStyle : srcDir;

            clientApiDir = StringUtils.hasLength(clientApiDir) ? basedir.getAbsolutePath() + "/../" + clientApiDir + "/" + mavenDirStyle : srcDir;

            bootstrapDir = StringUtils.hasLength(bootstrapDir) ? basedir.getAbsolutePath() + "/../" + bootstrapDir + "/" + mavenDirStyle : srcDir;

            adminUiDir = basedir.getAbsolutePath() + "/../" + adminUiDir;


            serviceDir = new File(serviceDir).getCanonicalPath();
            serviceImplDir = new File(serviceImplDir).getCanonicalPath();

            starterDir = new File(starterDir).getCanonicalPath();
            adminApiDir = new File(adminApiDir).getCanonicalPath();
            clientApiDir = new File(clientApiDir).getCanonicalPath();

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

                //自动去除 root 或是 parent
                moduleName = Utils.getModuleName(moduleName);

            }

            if (!hasText(moduleName)) {
                //下划线或是-号去除，并且转换为大写
                //自动获取模块名称为目录名称
                moduleName = ServiceModelCodeGenerator.splitAndFirstToUpperCase
                        (splitDir ? mavenProject.getBasedir().getParentFile().getName() : mavenProject.getBasedir().getName());
            }

            ServiceModelCodeGenerator.isOutputFormatCode(this.isOutputFormatCode);
            ServiceModelCodeGenerator.enableDubbo(this.enableDubbo);
            ServiceModelCodeGenerator.isIgnoreCodeCommentChange(this.isIgnoreCodeCommentChange);
            ServiceModelCodeGenerator.isCreateControllerSubDir(this.isCreateControllerSubDir);
            ServiceModelCodeGenerator.isCreateBizController(this.isCreateBizController);
            ServiceModelCodeGenerator.ignoreEntities(Arrays.asList(this.ignoreEntities));

            ServiceModelCodeGenerator.splitDir(splitDir);
            ServiceModelCodeGenerator.moduleName(moduleName);
            ServiceModelCodeGenerator.modulePackageName(modulePackageName);
            ServiceModelCodeGenerator.isSchemaDescUseConstRef(isSchemaDescUseConstRef);

            ServiceModelCodeGenerator.serviceDir(serviceDir);
            ServiceModelCodeGenerator.serviceImplDir(serviceImplDir);
            ServiceModelCodeGenerator.adminApiDir(adminApiDir);
            ServiceModelCodeGenerator.clientApiDir(clientApiDir);
            ServiceModelCodeGenerator.adminUiDir(adminUiDir);

            ServiceModelCodeGenerator.starterDir(starterDir);
            ServiceModelCodeGenerator.bootstrapDir(bootstrapDir);

            ServiceModelCodeGenerator.dirMap(
                    MapUtil.builder("services", serviceDir)
                            .put("starter", starterDir)
                            .put("admin-ui", adminUiDir)
                            .put("admin-api", adminApiDir)
                            .put("client-api", clientApiDir)
                            .put("bootstrap", bootstrapDir)
                            .put("services-impl", serviceImplDir)
                            .build()
            );

            getLog().info(String.format(" *** 模块名称：{%s} ，模块包名：{%s} ， 服务类生成路径：{%s}，控制器类生成路径：{%s}", moduleName, modulePackageName, serviceDir, adminApiDir));

            codeGenParams.putIfAbsent("mavenProject", mavenProject);
            codeGenParams.putIfAbsent("mavenSession", mavenSession);
            codeGenParams.putIfAbsent("artifactId", mavenSession.getCurrentProject().getArtifactId());
            codeGenParams.putIfAbsent("basedir", mavenProject.getBasedir());
            codeGenParams.putIfAbsent("projectName", mavenProject.getName());
            codeGenParams.putIfAbsent("projectDesc", mavenProject.getDescription());


            codeGenParams.putIfAbsent("modulePackageName", modulePackageName);
            codeGenParams.putIfAbsent("moduleName", moduleName);


            codeGenParams.putIfAbsent("serviceDir", serviceDir);
            codeGenParams.putIfAbsent("serviceImplDir", serviceImplDir);
            codeGenParams.putIfAbsent("starterDir", starterDir);
            codeGenParams.putIfAbsent("adminApiDir", adminApiDir);
            codeGenParams.putIfAbsent("clientApiDir", clientApiDir);
            codeGenParams.putIfAbsent("bootstrapDir", bootstrapDir);
            codeGenParams.putIfAbsent("adminUiDir", adminUiDir);

            codeGenParams.putIfAbsent("enableDubbo", enableDubbo);
            codeGenParams.putIfAbsent("enableOakBaseFramework", enableOakBaseFramework);

            codeGenParams.putIfAbsent("cacheSpelUtilsBeanName", cacheSpelUtilsBeanName);

            //1、生成代码
            ServiceModelCodeGenerator.genCodeAsMavenStyle(mavenProject, getClassLoader(), outputDirectory, codeGenParams);

            //2、生成辅助文件
            if (splitDir) { //尝试生成Pom 文件
                ServiceModelCodeGenerator.tryGenBootstrap(mavenProject, codeGenParams);
                ServiceModelCodeGenerator.tryGenPomFile(mavenProject, codeGenParams);
                //生成界面文件
                ServiceModelCodeGenerator.tryGenAdminUiFile(mavenProject, codeGenParams);
            }

            //3、生成
            ServiceModelCodeGenerator.tryGenSpringBootStarterFile(mavenProject, codeGenParams);

        } catch (Exception e) {
            getLog().error(mavenProject.getArtifactId() + " 模块代码生成错误：" + e.getMessage(), e);
        }
    }

}
