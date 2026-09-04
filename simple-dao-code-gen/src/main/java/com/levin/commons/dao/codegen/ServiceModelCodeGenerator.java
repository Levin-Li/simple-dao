package com.levin.commons.dao.codegen;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.google.googlejavaformat.java.FormatterException;
import com.google.googlejavaformat.java.ImportOrderer;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.google.googlejavaformat.java.RemoveUnusedImports;
import com.levin.commons.dao.EntityCategory;
import com.levin.commons.dao.EntityOpConst;
import com.levin.commons.dao.EntityOption;
import com.levin.commons.dao.annotation.*;
import com.levin.commons.dao.annotation.misc.PrimitiveValue;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.codegen.db.util.CommentUtils;
import com.levin.commons.dao.codegen.model.ClassModel;
import com.levin.commons.dao.codegen.model.FieldModel;
import com.levin.commons.dao.domain.*;
import com.levin.commons.plugins.Utils;
import com.levin.commons.rbac.DataMasking;
import com.levin.commons.rbac.RbacRoleInfo;
import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.domain.RefInject;
import com.levin.commons.service.support.ContextHolder;
import com.levin.commons.service.support.InjectConst;
import com.levin.commons.service.support.ValueHolder;
import com.levin.commons.ui.annotation.FormItem;
import com.levin.commons.ui.annotation.Options;
import com.levin.commons.utils.ExceptionUtils;
import com.levin.commons.utils.ExpressionUtils;
import com.levin.commons.utils.LangUtils;
import com.levin.commons.utils.MapUtils;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.hibernate.annotations.JavaType;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.*;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import org.apache.maven.project.MavenProject;

public final class ServiceModelCodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ServiceModelCodeGenerator.class);

    private static final Set<String> UNBOUNDED_TEXT_COLUMN_TYPES = Set.of(
            "text", "clob", "nclob", "longtext", "mediumtext", "tinytext", "json", "jsonb", "xml");

    public static final String DEL_EVT_FTL = "services/req/del_evt.ftl";
    public static final String UPDATE_EVT_FTL = "services/req/update_evt.ftl";

    public static final String SIMPLE_UPDATE_EVT_FTL = "services/req/simple_update_evt.ftl";
    public static final String SIMPLE_QUERY_EVT_FTL = "services/req/simple_query_evt.ftl";
    public static final String QUERY_EVT_FTL = "services/req/query_evt.ftl";
    public static final String STAT_EVT_FTL = "biz/bo/stat_evt.ftl";
    public static final String BASE_ID_EVT_FTL = "services/req/base_id_req.ftl";

    public static final String SERVICE_FTL = "services/service.ftl";
    public static final String MAPPER_FTL = "services/mapper.ftl";

    public static final String BIZ_SERVICE_FTL = "biz/biz_service.ftl";
    public static final String BIZ_MAPPER_FTL = "biz/biz_mapper.ftl";
    public static final String BIZ_SERVICE_IMPL_FTL = "biz/biz_service_impl.ftl";

    public static final String SERVICE_IMPL_FTL = "services/service_impl.ftl";
    public static final String CREATE_EVT_FTL = "services/req/create_evt.ftl";
    public static final String SIMPLE_CREATE_EVT_FTL = "services/req/simple_create_evt.ftl";
    public static final String INFO_FTL = "services/info/info.ftl";
    public static final String SIMPLE_INFO_FTL = "services/info/simple_info.ftl";

    public static final String CONTROLLER_FTL = "controller/controller.ftl";
    public static final String BIZ_CONTROLLER_FTL = "controller/biz_controller.ftl";
    public static final String CLIENT_BIZ_CONTROLLER_FTL = "controller/client_biz_controller.ftl";

    public static final String POM_XML_FTL = "pom.xml.ftl";

    public static final String TEMPLATE_PATH = "/simple.dao/codegen/template/";


//    private static Set<Class> baseTypes = new HashSet<>();

//    private static Set<Class> collectionsTypes = new HashSet<>();

    private static Set<String> notUpdateNames = new HashSet<>();

    static {

        notUpdateNames.add("creator");
        notUpdateNames.add("createBy");

        notUpdateNames.add("addTime");
        notUpdateNames.add("createTime");
        notUpdateNames.add("createDate");

    }


    private static final ContextHolder<String, Object> threadContext = ContextHolder.buildThreadContext(true);


    private static String genPom(String moduleNamePrefix, String moduleType, String srcDir, Map<String, Object> params, List<String> modules) throws Exception {

        if (!StringUtils.hasText(srcDir)) {
            logger.warn("模块[{}]未指定Pom文件的目录，将忽略生成", moduleType);
            return null;
        }

        if (!StringUtils.hasText(moduleNamePrefix)) {
            moduleNamePrefix = moduleName();
        }

        final String key = "artifactId";
        File pomFile = new File(srcDir, "../../../pom.xml").getCanonicalFile();

        modules.add(pomFile.getParentFile().getName());

        params.put(key, (moduleNamePrefix + "-" + pomFile.getParentFile().getName()).toLowerCase());

        params.put("moduleType", moduleType);
        params.put(moduleType, MapUtils.put(key, params.get(key)).build());

        genFileByTemplate(POM_XML_FTL, params, pomFile.getAbsolutePath());

        return pomFile.getParentFile().getName();
    }

    /**
     * 生成 POM 文件
     *
     * @param mavenProject
     * @param genParams
     */
    public static void tryGenPomFile(MavenProject mavenProject, Map<String, Object> genParams) throws Exception {

        //如果没有包名，也没有发现实体类
        if (!StringUtils.hasText(modulePackageName())
                || !hasEntityClass()
        ) {
            return;
        }

        logger.info("开始生成模块的Pom文件...");

//        String controllerDir = controllerDir();
//        String serviceDir = serviceDir();
//        String starterDir = starterDir();
//        String bootstrapDir = bootstrapDir();

        Map<String, Object> params = MapUtils.put(threadContext.getAll(false))
                .put(genParams)
                .put("parent", mavenProject.getParent())
                .put("groupId", mavenProject.getGroupId())
                .put("version", mavenProject.getVersion())
                .put("packaging", mavenProject.getPackaging())
                .put("entities", mavenProject.getArtifact())
                .build();

        /////////////////////////////生成说明文件///////////////////////////////////
        String template = "模块开发说明.md";

        genFileByTemplate(template, params, mavenProject.getBasedir().getParentFile().getAbsolutePath() + File.separator + template);

        template = "代码生成说明.md";
        genFileByTemplate(template, params, mavenProject.getBasedir().getParentFile().getAbsolutePath() + File.separator + template);

        ///////////////////////////////////////////////////////////////////////////


        final List<String> modules = new ArrayList<>(2);

        //  String moduleName = moduleName();// mavenProject.getBasedir().getParentFile().getName();

        ////////////////////////服务层////////////////////////////////

        genPom(null, "service", serviceDir(), params, modules);
        /////////////////////////////////////自举模块///////////////////////////////////////////////////////////////////

        genPom(null, "service_impl", serviceImplDir(), params, modules);

        genPom(null, "starter", starterDir(), params, modules);
        /////////////////////////////////////控制器/////////////////////////////////////////////////////////////////////////////

        params.put("isAdminModule", true);
        genPom(null, "api", adminApiDir(), params, modules);
        genPom(null, "bootstrap", adminBootstrapDir(), params, modules);

        params.put("isAdminModule", false);
        genPom(null, "api", clientApiDir(), params, modules);
        genPom(null, "bootstrap", clientBootstrapDir(), params, modules);
        ///////////////////////// 修改项目根POM ////////////////////////////

        File parent = new File(serviceDir(), "../../../../pom.xml").getCanonicalFile();

        StringBuilder pomContent = new StringBuilder(FileUtils.readFileToString(parent, "utf-8"));

        String modInfo = modules.stream()
                .filter(m -> !java.util.regex.Pattern.compile("<module>\\s*" + m + "\\s*</module>").matcher(pomContent).find())
                .map(m -> "\n        <module>" + m + "</module>")
                .collect(Collectors.joining());

        if (StringUtils.hasText(modInfo)) {

            int indexOf = pomContent.indexOf("</modules>");

            if (indexOf == -1) {
                pomContent.insert(pomContent.indexOf("</project>"), "\n    <modules>\n" + modInfo + "\n    </modules>\n");
            } else {
                pomContent.insert(indexOf, modInfo + "\n    ");
            }
            //写入模块
            FileUtils.write(parent, pomContent, "utf-8");
        }

    }


    public static void tryGenBootstrap(MavenProject mavenProject, String bootstrapDir, Map<String, Object> params) throws Exception {

        //如果没有包名，也没有发现实体类
        if (!StringUtils.hasText(modulePackageName())
                || !hasEntityClass()
                || !StringUtils.hasText(bootstrapDir)) {
            return;
        }

        logger.info("开始生成[{}]模块代码...", bootstrapDir);

        params.putAll(threadContext.getAll(false));

        //是否 bootstrap
        params.put("isBootstrap", true);

        params.put("camelStyleModuleName", splitAndFirstToUpperCase(moduleName()));

        String prefix = bootstrapDir + File.separator
                + modulePackageName().replace('.', File.separatorChar)
                + File.separator;

        genSameNameFileByTemplate("bootstrap/AppWebMvcConfigurer.java", params, prefix);
        genSameNameFileByTemplate("bootstrap/AppDataInitializer.java", params, prefix);
//        genFileByTemplate("bootstrap/PluginManagerController.java", params, prefix + "PluginManagerController.java");
        genSameNameFileByTemplate("bootstrap/Application.java", params, prefix);
        genSameNameFileByTemplate("bootstrap/SpelUtils.java", params, prefix);
        genSameNameFileByTemplate("bootstrap/BlockingFilter.java", params, prefix);

        String resPath = new File(bootstrapDir).getParentFile().getCanonicalPath() + File.separator + "resources" + File.separator;

        genSameNameFileByTemplate("bootstrap/application.properties", params, resPath);
        genSameNameFileByTemplate("bootstrap/caffeine.properties", params, resPath);
        genSameNameFileByTemplate("bootstrap/application.yml", params, resPath);
        genSameNameFileByTemplate("bootstrap/application-local.yml", params, resPath);
        genSameNameFileByTemplate("bootstrap/application-dev.yml", params, resPath);
        genSameNameFileByTemplate("bootstrap/application-test.yml", params, resPath);
        genSameNameFileByTemplate("bootstrap/application-prod.yml", params, resPath);

        genSameNameFileByTemplate("bootstrap/shell/env.sh", params, resPath + "shell");
        genSameNameFileByTemplate("bootstrap/shell/startup.sh", params, resPath + "shell");
        genSameNameFileByTemplate("bootstrap/shell/restart.sh", params, resPath + "shell");
        genSameNameFileByTemplate("bootstrap/shell/shutdown.sh", params, resPath + "shell");

        genSameNameFileByTemplate("bootstrap/shell/installService.sh", params, resPath + "shell");

        //Java 线程 CPU占用排行
        genSameNameFileByTemplate("bootstrap/shell/jtCpuTopN.sh", params, resPath + "shell");

        //Jvm 分析工具
        genSameNameFileByTemplate("bootstrap/shell/jcmd.sh", params, resPath + "shell");
        genSameNameFileByTemplate("bootstrap/shell/jstat.sh", params, resPath + "shell");

        final String resTemplateDir = "simple.dao/codegen/template/";

        Utils.copyAndReplace(prefix, false, resTemplateDir + "bootstrap/logback.xml", new File(resPath + "logback.xml"), new HashMap<>());

        //开始生成测试相关文件
        //替换成 test
        prefix = prefix.replace(File.separator + "main" + File.separator, File.separator + "test" + File.separator);
        new File(prefix).mkdirs();

        // genFileByTemplate("test/TestCase.java", params, prefix + "TestCase.java");

        //测试目录
        bootstrapDir = bootstrapDir.replace(File.separator + "main" + File.separator, File.separator + "test" + File.separator);
        resPath = new File(bootstrapDir).getParentFile().getCanonicalPath() + File.separator + "resources" + File.separator;

        genSameNameFileByTemplate("bootstrap/application.properties", params, resPath);
        genSameNameFileByTemplate("bootstrap/application.yml", params, resPath);
        genSameNameFileByTemplate("bootstrap/application-local.yml", params, resPath);

        Utils.copyAndReplace(prefix, false, resTemplateDir + "bootstrap/logback.xml", new File(resPath + "logback.xml"), new HashMap<>());

        for (Class entityClass : entityClassList()) {
            //  genTestCode(entityClass, bootstrapDir, null);
        }

    }

    @SneakyThrows
    public static void genJavaFile(String moduleDir, String templateDir, String className, Map<String, Object> params) {

        String fileName = StringUtils.hasText(templateDir) ?
                String.join(File.separator, templateDir, className + ".java")
                : className + ".java";

        params.put("className", className);

        params.put("moduleDir", moduleDir);

        genFileByTemplate(fileName, params, String.join(File.separator,
                moduleDir, modulePackageName().replace('.', File.separatorChar), fileName));

    }

    /**
     * 生成 Spring boot auto stater 文件
     *
     * @param mavenProject
     * @param params
     */
    public static void tryGenSpringBootStarterFile(MavenProject mavenProject, Map<String, Object> params) throws Exception {

        //如果没有包名，也没有发现实体类
        if (!StringUtils.hasText(modulePackageName())
                || !hasEntityClass()
        ) {
            return;
        }

        logger.info("开始生成模块的通用代码...");

        String adminApiDir = adminApiDir();
        String clientApiDir = clientApiDir();

        String serviceDir = serviceDir();
        String serviceImplDir = serviceImplDir();

        String starterDir = starterDir();

        params.putAll(threadContext.getAll(true));

        params.put("camelStyleModuleName", splitAndFirstToUpperCase(moduleName()));


        String fileName = "index.html";

        genFileByTemplate(fileName, params, String.join(File.separator,
                adminApiDir, "..", "resources", "public", modulePackageName(), "admin", fileName));

        ////////////////////////////////adminApiDir/////////////////////////////////////////////
        //生成控制器配置文件
        Arrays.asList("ModuleWebMvcConfigurer"
                , "ModuleWebControllerAdvice"
                , "ModuleSwaggerConfigurer"
                , "ModuleVariableResolverConfigurer"
                , "ModuleWebSocketConfigurer"
        ).forEach(className -> genJavaFile(adminApiDir, "config", className, params));

        Arrays.asList("ModulePlugin"
                , "ModuleWebInjectVarServiceImpl"
        ).forEach(className -> genJavaFile(adminApiDir, "", className, params));

        genJavaFile(adminApiDir, "aspect", "ModuleWebControllerAspect", params);

        //////////////////////////////////clientApiDir///////////////////////////////////////////////////

        if (StrUtil.isNotBlank(clientApiDir)) {

            //生成控制器配置文件
            Arrays.asList("ModuleWebMvcConfigurer"
                    , "ModuleWebControllerAdvice"
                    , "ModuleSwaggerConfigurer"
                    , "ModuleVariableResolverConfigurer"
                    , "ModuleWebSocketConfigurer"
            ).forEach(className -> genJavaFile(clientApiDir, "config", className, params));

            Arrays.asList("ModulePlugin"
                    , "ModuleWebInjectVarServiceImpl"
            ).forEach(className -> genJavaFile(clientApiDir, "", className, params));

            genJavaFile(clientApiDir, "aspect", "ModuleWebControllerAspect", params);

        }
        ////////////////////////////////////  serviceDir & serviceImplDir////////////////////////////////////

        Arrays.asList("ModuleCacheService"
        ).forEach(className -> genJavaFile(serviceDir, "cache", className, params));
        //生成控制器配置文件
        Arrays.asList("ModuleSpringCacheResolver"
        ).forEach(className -> genJavaFile(serviceImplDir, "cache", className, params));


        //生成服务模块的文件
        Arrays.asList(
                "ModuleOption"
        ).forEach(className -> genJavaFile(serviceDir, "", className, params));

        Arrays.asList(
                "ModuleDataInitializer"
        ).forEach(className -> genJavaFile(serviceImplDir, "", className, params));


        ///////////////////////////////////  starterDir ///////////////////////////////////////////

        Arrays.asList("ModuleStarterConfiguration"
        ).forEach(className -> genJavaFile(starterDir, "", className, params));

        simpleGen("starter/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports", params, mavenProject);
    }

    public static void simpleGen(final String templatePath, Map<String, Object> params, MavenProject mavenProject) throws Exception {

        int idx = templatePath.indexOf("/");

        String targetDir = null;
        if (idx != -1) {
            targetDir = ServiceModelCodeGenerator.dirMap().get(templatePath.substring(0, idx).trim());
        }

        String filePath = templatePath;

        //如果没有目录
        if (StringUtils.hasText(targetDir)) {
            //默认这个值是Java源码的值，项目目录在上一级
            filePath = targetDir + "/../" + filePath.substring(idx + 1);
        } else {
            //默认路径
            filePath = mavenProject.getBasedir().getCanonicalPath() + "/" + filePath;
        }

        if (filePath.endsWith(".ftl")) {
            filePath = filePath.substring(0, filePath.length() - 4);
        }

        //转换成本地路径
        filePath = filePath.replace("/", File.separator);

        genFileByTemplate(templatePath, params, filePath);

    }

    public static String splitAndFirstToUpperCase(String moduleName) {
        return splitAndFirstToUpperCase(moduleName, "-", "_");
    }

    /**
     * 用指定的分隔符分隔，并且把首字母大写
     *
     * @param str
     * @return
     */
    public static String splitAndFirstToUpperCase(String str, String... regexDelimiters) {

        return Stream.of(str.split(String.format("[%s]", Stream.of(regexDelimiters).collect(Collectors.joining()))))
                .map(txt -> txt.trim())
                .filter(StringUtils::hasText)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());

    }


    @SneakyThrows
    public static Map<String, CUnit> parseSrcFile(File srcFileDir) {

        Map<String, CUnit> srcFileMap = new LinkedHashMap<>();

        if (srcFileDir.isDirectory() && srcFileDir.exists()) {

            logger.info("*** 准备开始解析Java源文件: " + srcFileDir.getAbsolutePath());

            final String prefix = srcFileDir.getCanonicalPath();

            for (File javaFile : FileUtils.listFiles(srcFileDir, new String[]{"java"}, true)) {

                final String classFilePath = javaFile.getCanonicalPath().substring(prefix.length() + 1);

                final String classPkgName = javaFile.getParentFile().getCanonicalPath().substring(prefix.length() + 1).replace(File.separator, ".");

                logger.info("*** 解析Java源文件：" + classFilePath);

                CompilationUnit unit = StaticJavaParser.parse(javaFile);

                for (TypeDeclaration<?> type : unit.getTypes()) {
                    srcFileMap.put(classPkgName + "." + type.getNameAsString(), new CUnit().setFileName(classFilePath).setType(type).setCompilationUnit(unit));
                }
            }

            logger.info("*** 解析Java源文件完成，共解析到：" + srcFileMap.size() + "个类，" + srcFileMap.keySet());

        } else {
            logger.error("*** 解析Java源文件失败，目录：" + srcFileDir.getAbsolutePath() + "不存在。");
        }

        return srcFileMap;
    }


    @Data
    @Accessors(chain = true)
    static class CUnit {
        String fileName;

        TypeDeclaration type;

        CompilationUnit compilationUnit;
    }

    private static Map<String, CUnit> srcFileCompilationMap;

    /**
     * 根据Maven目录样式生成 控制器，服务接口，请求和返回值
     *
     * @param classLoader
     * @param genParams
     */
    public static void genCodeAsMavenStyle(MavenProject mavenProject, ClassLoader classLoader
            , String buildOutputDirectory, Map<String, Object> genParams) throws Exception {

//            File file = new File(project.getBuild().getOutputDirectory());
        File file = new File(buildOutputDirectory);

        if (!file.exists()) {
            logger.error("***" + buildOutputDirectory + "目录不存在，请先编译实体模块。");
            return;
        }

        if (srcFileCompilationMap == null) {
            srcFileCompilationMap = parseSrcFile(new File(mavenProject.getBuild().getSourceDirectory()));
        }

        String canonicalPath = file.getCanonicalPath();

        file = new File(canonicalPath);

        final int suffixLen = ".class".length();

        // logger.info("Files:" + FileUtils.listFiles(file, new String[]{"class"}, true));

        final List<Class<?>> classList = FileUtils.listFiles(file, new String[]{"class"}, true)
                .stream().filter(File::isFile)

                .map(f -> f.getAbsolutePath().substring(canonicalPath.length() + 1)
                        .replace('/', '.')
                        .replace('\\', '.')
                        .replace("..", "."))
                .map(fn -> fn.substring(0, fn.length() - suffixLen))
                .map(n -> {
                    try {
                        return classLoader != null ? classLoader.loadClass(n) : Class.forName(n);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(clazz -> clazz.isAnnotationPresent(jakarta.persistence.Entity.class))
                .filter(clazz -> !clazz.isAnnotationPresent(Ignore.class))
                .collect(Collectors.toList());

        if (classList.isEmpty()) {
            logger.error("*** [" + file + "] 没有发现 Jpa 实体类，忽略代码生成。");
            return;
        }


        hasEntityClass(true);

        //获取包名最端的类，把最短的包名，做为模块的包名
        Class tempClass = null;

        //如果包名没有确定，尝试获取实体类包名最短的为包名
        if (!StringUtils.hasText(modulePackageName())) {

            for (Class<?> entityClass : classList) {
                if (tempClass == null
                        || tempClass.getPackage().getName().length() > entityClass.getPackage().getName().length()) {
                    tempClass = entityClass;
                }
            }

            modulePackageName(upPackage(tempClass.getPackage().getName()));
        }

        //如果模块名没有确定
        if (!StringUtils.hasText(moduleName())) {

            String modulePackageName = modulePackageName();

            String moduleName = "";

            if (modulePackageName != null
                    && modulePackageName.contains(".")) {
                //自动获取模块的包名的最后一个包名为模块的包名, eg.  com.levin.xx.member --> member
                moduleName = modulePackageName.substring(modulePackageName.lastIndexOf('.') + 1);
            } else {
                //自动获取项目目录的上级目录做为模块的包名
                //要考虑为服务类和控制器类和实体在同一个项目的情况
                moduleName = splitDir() ? mavenProject.getBasedir().getParentFile().getName() : mavenProject.getBasedir().getName();
                moduleName = Utils.getModuleName(moduleName);
            }

            moduleName(moduleName);
        }

        logger.info(" *** 开始代码生成 *** 当前Pom模块: {} , modulePackageName = {} , moduleName = {}", mavenProject.getArtifactId(), modulePackageName(), moduleName());

        if (genParams != null) {
            genParams.put("moduleNameHashCode", "" + Math.abs(modulePackageName().hashCode()));
        }


        String adminApiDir = adminApiDir();
        String clientApiDir = clientApiDir();
        String starterDir = starterDir();
        String serviceDir = serviceDir();
        String serviceImplDir = serviceImplDir();

        ///////////////////////////////////////////////

//        genFileByTemplate("controller/BaseController.java",
//                MapUtils.put(genParams).put("modulePackageName", modulePackageName()).build(), adminApiDir + File.separatorChar
//                        + modulePackageName().replace('.', File.separatorChar) + File.separatorChar
//                        + "controller" + File.separatorChar + "BaseController.java");

        genFileByTemplate(genParams, adminApiDir, "controller", "BaseController.java");
        genFileByTemplate(genParams, adminApiDir, "controller/base", "code-gen.md");

        genFileByTemplate(genParams, clientApiDir, "controller", "BaseController.java");
        genFileByTemplate(genParams, clientApiDir, "controller/base", "code-gen.md");

//        genFileByTemplate("services/BaseService.java",
//                MapUtils.put(genParams).put("modulePackageName", modulePackageName()).build(), serviceDir + File.separatorChar
//                        + modulePackageName().replace('.', File.separatorChar) + File.separatorChar
//                        + "services" + File.separatorChar + "BaseService.java");


        genFileByTemplate(genParams, serviceImplDir, "services", "BaseService.java");
        genFileByTemplate(genParams, serviceImplDir, "services", "code-gen.md");
        genFileByTemplate(genParams, serviceImplDir, "services", "基础服务类开发规范.md");

        genFileByTemplate(genParams, serviceImplDir, "job", "DemoJob.java");

        //genFileByTemplate(genParams, serviceImplDir, "biz", "InjectVarServiceImpl.java");

        genFileByTemplate(genParams, serviceImplDir, "biz", "业务服务类开发规范.md");


        genFileByTemplate(genParams, serviceDir, "services", "package-info.java");
        genFileByTemplate(genParams, serviceDir, "services", "ModuleVersion.java");
        genFileByTemplate(genParams, serviceDir, "services", "code-gen.md");
        genFileByTemplate(genParams, serviceDir, "biz", "InjectVarService.java");

        genFileByTemplate(genParams, serviceDir, "services", "commons", "req", "BaseReq.java");
        genFileByTemplate(genParams, serviceDir, "services", "commons", "info", "BaseInfo.java");

        genFileByTemplate(genParams, serviceDir, "services", "commons", "req", "MultiTenantReq.java");
        genFileByTemplate(genParams, serviceDir, "services", "commons", "info", "MultiTenantInfo.java");

        genFileByTemplate(genParams, serviceDir, "services", "commons", "req", "MultiTenantOrgReq.java");
        genFileByTemplate(genParams, serviceDir, "services", "commons", "info", "MultiTenantOrgInfo.java");

        genFileByTemplate(genParams, serviceDir, "services", "commons", "req", "MultiTenantPersonalReq.java");
        genFileByTemplate(genParams, serviceDir, "services", "commons", "info", "MultiTenantPersonalInfo.java");

        genFileByTemplate(genParams, serviceDir, "services", "commons", "req", "MultiTenantOrgPersonalReq.java");
        genFileByTemplate(genParams, serviceDir, "services", "commons", "info", "MultiTenantOrgPersonalInfo.java");

        ////////////////////////////////////////////////////////////////////////////////////////////

        ///////////////////////////////////////////////
        List<String> ignoreEntities = ignoreEntities();

        for (Class<?> clazz : classList) {

            //忽略测试类
//            if (clazz.getSimpleName().equals("TestOrg")
//                    || clazz.getSimpleName().equals("TestRole")) {
//                continue;
//            }

            if (ignoreEntities.stream().anyMatch(regex -> clazz.getName().matches(regex))) {
                logger.info("忽略实体类:{}", clazz.getName());
                continue;
            }

            entityClassList(clazz);

            logger.info("*** 开始尝试生成实体类[" + clazz.getName() + "]相关的代码，服务目录[" + serviceDir + "],控制器目录[" + adminApiDir + "]...");

            try {
                genCodeByEntityClass(clazz, serviceDir, adminApiDir, genParams);
            } catch (CodeGenInteruptException e) {

                logger.warn(" *** 实体类" + clazz + " 代码生成错误", e);

                throw e;
            } catch (Exception e) {

                logger.warn(" *** 实体类" + clazz + " 代码生成错误", e);

            }
        }
        ///////////////////////////////////////////////
    }

    private static void genFileByTemplate(Map<String, Object> genParams, String srcDir, String... templatePaths) throws Exception {

        String fn = String.join(File.separator, templatePaths);

        genFileByTemplate(fn,
                MapUtils.put(genParams).put("modulePackageName", modulePackageName()).build(), srcDir + File.separatorChar
                        + modulePackageName().replace('.', File.separatorChar) + File.separatorChar
                        + fn);
    }


    private static String servicePackage() {
        return modulePackageName() + ".services." + subPkgName();
    }

    private static String bizServicePackage() {
//        return modulePackageName() + ".biz" + (isCreateControllerSubDir() ? "." + subPkgName() : "");
        return modulePackageName() + ".biz";
    }

    private static String controllerPackage() {
        return modulePackageName() + ".controller"
                + (Boolean.TRUE.equals(isCreateBizController()) ? ".base" : "")
                + (Boolean.TRUE.equals(isCreateControllerSubDir()) ? ("." + subPkgName()) : "");
    }

    private static String bizControllerPackage() {

        //业务控制器不创建目录
        return modulePackageName() + ".controller"
                //20230812 修改，业务控制器不分目录
                //  + (Boolean.TRUE.equals(isCreateControllerSubDir()) ? "." + subPkgName() : "")
                ;
    }

    public static Boolean isCreateControllerSubDir(Boolean newValue) {
        return putThreadVar(newValue);
    }

    public static Boolean isOutputFormatCode(Boolean newValue) {
        return putThreadVar(newValue);
    }

    public static boolean isOutputFormatCode() {
        return getThreadVar(false);
    }

    public static List<String> keepAnnotationList(String... newValue) {
        return putThreadVar(Arrays.asList(newValue));
    }

    public static List<String> keepAnnotationList() {
        return getThreadVar(Collections.emptyList());
    }


    public static Map<String, String> annotationContentReplaceMap(Map<String, String> newValue) {
        return putThreadVar((newValue));
    }

    public static Map<String, String> annotationContentReplaceMap() {
        return getThreadVar(Collections.emptyMap());
    }


    public static Boolean enableDubbo(Boolean newValue) {
        return putThreadVar(newValue);
    }

    public static boolean enableDubbo() {
        return getThreadVar(false);
    }

    public static Boolean isIgnoreCodeCommentChange(Boolean newValue) {
        return putThreadVar(newValue);
    }

    public static boolean isIgnoreCodeCommentChange() {
        return getThreadVar(false);
    }

    public static Boolean isCreateControllerSubDir() {
        return getThreadVar(false);
    }

    public static Boolean isCreateBizController(Boolean newValue) {
        return putThreadVar(newValue);
    }

    public static Boolean isCreateBizController() {
        return getThreadVar(false);
    }

    public static File baseDir(File dir) {
        return putThreadVar(dir);
    }

    public static File baseDir() {
        return getThreadVar(null);
    }

    public static List<String> ignoreEntities(List<String> ignoreEntities) {
        return putThreadVar(ignoreEntities);
    }

    public static List<String> ignoreEntities() {
        return getThreadVar(Collections.<String>emptyList());
    }

    private static Boolean hasEntityClass(boolean newValue) {
        return putThreadVar(newValue);
    }

    private static Boolean hasEntityClass() {
        return getThreadVar(null);
    }


    public static List<Class> entityClassList(Class... addValues) {
        return addAndGetValueList(ExceptionUtils.getInvokeMethodName(), addValues);
    }

    public static List<String> serviceClassList(String... addValues) {
        return addAndGetValueList(ExceptionUtils.getInvokeMethodName(), addValues);
    }

    public static List<String> serviceClassNameList(String... addValues) {
        return addAndGetValueList(ExceptionUtils.getInvokeMethodName(), addValues);
    }

    public static List<String> controllerClassList(String... addValues) {
        return addAndGetValueList(ExceptionUtils.getInvokeMethodName(), addValues);
    }

    /**
     * @param key
     * @param addValues
     * @param <T>
     * @return
     */
    protected static <T> List<T> addAndGetValueList(String key, T... addValues) {

        List<T> valueList = threadContext.get(key);

        if (valueList == null) {
            valueList = new LinkedList<>();
            threadContext.put(key, valueList);
        }

        if (addValues != null) {
            for (T value : addValues) {
                if (!valueList.contains(value)) {
                    valueList.add(value);
                }
            }
        }

        return valueList;
    }

    public static String getInvokeMethodName(int level) {
        return (new Exception()).getStackTrace()[level].getMethodName();
    }

    private static <T> T putThreadVar(T value) {
        return threadContext.put(getInvokeMethodName(2), value);
    }

    private static <T> T getThreadVar(T defaultValue) {
        return threadContext.getOrDefault(getInvokeMethodName(2), defaultValue);
    }

    /// ////////////////////////////////////////////////
    public static Class<?> entityClass(Class<?> newValue) {
        return putThreadVar(newValue);
    }

    public static Class<?> entityClass() {
        return getThreadVar(null);
    }

    /// ////////////////////////////////////////////////

    public static Map<String, String> dirMap(Map<String, String> newValue) {
        return putThreadVar(newValue);
    }

    public static Map<String, String> dirMap() {
        return getThreadVar(Collections.emptyMap());
    }

    public static Boolean splitDir(boolean newValue) {
        return putThreadVar(newValue);
    }

    public static Boolean splitDir() {
        return getThreadVar(null);
    }


    /// ////////////////////////////////////////////////
    public static String moduleName(String newValue) {
        return putThreadVar(newValue);
    }

    public static String moduleName() {
        return getThreadVar(null);
    }

    /// ////////////////////////////////////////////////

    public static String serviceDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String serviceDir() {
        return getThreadVar(null);
    }

    ///////////////////////////////////////////////////

    /// ////////////////////////////////////////////////

    public static String serviceImplDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String serviceImplDir() {
        return getThreadVar(null);
    }

    /// ////////////////////////////////////////////////


    public static String starterDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String starterDir() {
        return getThreadVar(null);
    }

    /// ////////////////////////////////////////////////
    public static String adminBootstrapDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String adminBootstrapDir() {
        return getThreadVar(null);
    }


    public static String clientBootstrapDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String clientBootstrapDir() {
        return getThreadVar(null);
    }

    public static String isInterruptWhenTargetFileChangedByGroovyScript(String allowGenCodeOverrideFailEntityClassList) {
        return putThreadVar(allowGenCodeOverrideFailEntityClassList);
    }

    public static String isInterruptWhenTargetFileChangedByGroovyScript() {
        return getThreadVar(null);
    }

    /// ////////////////////////////////////////////////
    public static String adminApiDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String adminApiDir() {
        return getThreadVar(null);
    }

    public static String clientApiDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String clientApiDir() {
        return getThreadVar(null);
    }

    /// ////////////////////////////////////////////////
    public static String adminUiDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String adminUiDir() {
        return getThreadVar(null);
    }

    /// ////////////////////////////////////////////////
    public static String modulePackageName(String newValue) {
        return putThreadVar(newValue);
    }

    public static String modulePackageName() {
        return getThreadVar(null);
    }

    /// ////////////////////////////////////////////////

    public static void isSchemaDescUseConstRef(boolean isSchemaDescUseConstRef) {
        threadContext.put(ExceptionUtils.getInvokeMethodName(), isSchemaDescUseConstRef);
    }

    public static boolean isSchemaDescUseConstRef() {
        return threadContext.getOrDefault(ExceptionUtils.getInvokeMethodName(), true);
    }

    /// ////////////////////////////////////////////////

    public static String upPackage(String packageName) {
        return upLevel(packageName, '.');
    }

    /**
     * 包名或是目录向上一级
     * 根为空字符串
     *
     * @param packageName
     * @return
     */
    public static String upLevel(String packageName, char delim) {

        int lastIndexOf = packageName.replace("" + delim + delim, "" + delim).lastIndexOf(delim);

        //.eg  ""  "." "com" ".a" ".com" "com.a.b.c"

        if (lastIndexOf <= 0) {
            return "";
        }

        return packageName.substring(0, lastIndexOf);
    }

    /**
     * 实体转服务模型
     *
     * @param entityClass 实体类
     */
    public static void genCodeByEntityClass(Class<?> entityClass, String serviceDir, String adminApiDir
            , Map<String, Object> entityMapping) throws Exception {

        logger.info(" ***提示*** 可以通过源码注释中包含 @CopyToGenCode @* 关键字，原样复制字段上的注解到生成的代码之中，还可以区分目标类型，如：@CopyToGenCode @query 表示复制到查询对象");

        entityClass(entityClass);

        if (entityMapping == null) {
            entityMapping = new LinkedHashMap<>();
        }

        boolean isMultiTenant = MultiTenantObject.class.isAssignableFrom(entityClass);
        boolean isOrg = OrganizedObject.class.isAssignableFrom(entityClass);
        boolean isPersonal = PersonalObject.class.isAssignableFrom(entityClass);


        String reqExtendClass = "";

        if (isMultiTenant) {
            reqExtendClass = "MultiTenant";
        }

        if (isOrg) {
            reqExtendClass += "Org";
        }

        if (isPersonal) {
            reqExtendClass += "Personal";
        }

        String infoExtendClass = reqExtendClass;

        if (!StringUtils.hasText(reqExtendClass)) {
            reqExtendClass = "BaseReq";
        } else {
            reqExtendClass += "Req<";
        }

        if (!StringUtils.hasText(infoExtendClass)) {
            infoExtendClass = "BaseInfo";
        } else {
            infoExtendClass += "Info";
        }

        Map<String, Object> params = MapUtils
                .put(entityMapping)
                .put(threadContext.getAll(true))
                .put("modulePackageName", modulePackageName())
                .put("entityClass", entityClass)

                .put("isMultiTenantObject", isMultiTenant)
                .put("isOrganizedObject", isOrg)
                .put("isPersonalObject", isPersonal)

                .put("isMultiTenantSharedObject", MultiTenantSharedObject.class.isAssignableFrom(entityClass))
                .put("isMultiTenantPublicObject", MultiTenantPublicObject.class.isAssignableFrom(entityClass))

                .put("isOrganizedPublicObject", OrganizedPublicObject.class.isAssignableFrom(entityClass))
                .put("isOrganizedSharedObject", OrganizedSharedObject.class.isAssignableFrom(entityClass))
                //设置请求对象继承的类
                .put("reqExtendClass", reqExtendClass)
                .put("infoExtendClass", infoExtendClass)
                .build();

        String boDir = File.separator + "bo" + File.separator + entityClass.getSimpleName().toLowerCase();

        params.put("bizBoPackageName", bizServicePackage() + boDir.replace(File.separator, "."));
        params.put("bizBoSubPackageName", boDir.replace(File.separator, "."));

        EntityCategory category = entityClass.getAnnotation(EntityCategory.class);

        if (category != null && category.queryObjectExtendType() != Void.class) {
            reqExtendClass = category.queryObjectExtendType().getSimpleName();
            params.put("reqExtendClass", reqExtendClass);
        }
        if (category != null && category.infoObjectExtendType() != Void.class) {
            reqExtendClass = category.infoObjectExtendType().getSimpleName();
            params.put("infoExtendClass", infoExtendClass);
        }

        if (category != null && StringUtils.hasText(category.value())) {

            Map<String, String> map = MapUtils.put(EntityOpConst.BIZ_TYPE_NAME, "BIZ_TYPE_NAME")
                    .put(EntityOpConst.COMMON_TYPE_NAME, "COMMON_TYPE_NAME")
                    .put(EntityOpConst.SYS_TYPE_NAME, "SYS_TYPE_NAME")
                    .put(EntityOpConst.PLATFORM_TYPE_NAME, "PLATFORM_TYPE_NAME")
                    .put(EntityOpConst.EXPERT_TYPE_NAME, "EXPERT_TYPE_NAME")
                    .build();

            params.put("entityCategory", map.getOrDefault(category.value(), "\"" + category.value() + "\""));

        } else {
            //默认是业务类型
            params.put("entityCategory", "BIZ_TYPE_NAME");
        }

        boolean isCacheableEntity = !entityClass.isAnnotationPresent(Cacheable.class) || (entityClass.getAnnotation(Cacheable.class)).value();
        params.put("isCacheableEntity", isCacheableEntity);


        if (isCacheableEntity) {
            logger.info("默认缓存实体类：{} ，可以设置 @Cacheable(false) 禁用缓存", entityClass.getSimpleName());
        }

        String action = "info";

        List<FieldModel> fields = buildFieldModel(entityClass, entityMapping, false, action);
        params.put("selfOverridableMatchFields", getSelfOverridableMatchFields(entityClass, fields));

//        postProcess(fields, action);

        //info 对象按完整的字段生成
        buildInfo(entityClass, fields, serviceDir, params);

        //////////////////////////////////////////////////////////////
        action = "query";

        //请求对象会忽略继承的属性
        fields = buildFieldModel(entityClass, entityMapping, true, action);
//        postProcess(fields, action);

        //查询相关的独立处理
        buildEvt(entityClass, fields, serviceDir, params, action);

        /////////////////////////////////////////////////////////////////////////////////
        action = "create";

        fields = buildFieldModel(entityClass, entityMapping, true, action);
//        postProcess(fields, action);

        buildEvt(entityClass, fields, serviceDir, params, action);

        ////////////////////////////////////////////////////////
        action = "update";

        fields = buildFieldModel(entityClass, entityMapping, true, action);
//        postProcess(fields, action);

        buildEvt(entityClass, fields, serviceDir, params, action);

        ////////////////////////////////////////////////////////
        action = "delete";

        fields = buildFieldModel(entityClass, entityMapping, true, action);
//        postProcess(fields, action);

        buildEvt(entityClass, fields, serviceDir, params, action);

        /////////////////////////////////////////////////////////////////

        buildService(entityClass, fields, params);

        if (shouldGenerateController(entityClass)) {
            buildAdminApiController(entityClass, fields, adminApiDir, params);
            buildClientApiController(entityClass, fields, clientApiDir(), params);
        } else {
            logger.info("实体类 {} 标记为仅内部访问，跳过控制器生成", entityClass.getName());
        }

    }

    static boolean shouldGenerateController(Class<?> entityClass) {
        EntityOption entityOption = AnnotatedElementUtils.findMergedAnnotation(entityClass, EntityOption.class);
        return entityOption == null || !entityOption.innerAccessOnly();
    }

    /**
     * 计算 {@link SelfOverridableObject} 对应的最匹配查询参数。
     * <p>
     * 公开租户和公开组织字段分别固定排在第一、第二位；其余字段严格遵循注解中的声明顺序。
     * 相同字段只保留一次，避免生成重复的参数、筛选和排序条件。
     */
    static List<FieldModel> getSelfOverridableMatchFields(Class<?> entityClass, List<FieldModel> fields) {

        SelfOverridableObject selfOverridable = AnnotatedElementUtils.findMergedAnnotation(entityClass, SelfOverridableObject.class);

        if (selfOverridable == null || selfOverridable.overrideColumnNames().length < 1) {
            return Collections.emptyList();
        }

        Map<String, FieldModel> fieldsByName = fields.stream()
                .collect(Collectors.toMap(FieldModel::getName, Function.identity(), (left, right) -> left, LinkedHashMap::new));

        List<String> matchFieldNames = new ArrayList<>();

        if (MultiTenantObject.class.isAssignableFrom(entityClass)) {
            // 部分实体通过继承或接口暴露 tenantId，字段模型在生成请求对象时可能不会保留该继承字段。
            fieldsByName.computeIfAbsent("tenantId", fieldName -> new FieldModel(entityClass)
                    .setName(fieldName)
                    .setType(String.class)
                    .setTypeName(String.class.getSimpleName()));
            matchFieldNames.add("tenantId");
        }

        if (OrganizedObject.class.isAssignableFrom(entityClass)) {
            matchFieldNames.add("orgId");
        }

        for (String fieldName : selfOverridable.overrideColumnNames()) {
            Assert.hasText(fieldName, () -> "实体类 " + entityClass.getName()
                    + " 的 @SelfOverridableObject.overrideColumnNames 不能包含空字段名");
            if (!matchFieldNames.contains(fieldName)) {
                matchFieldNames.add(fieldName);
            }
        }

        return matchFieldNames.stream()
                .map(fieldName -> {
                    FieldModel fieldModel = fieldsByName.get(fieldName);
                    Assert.notNull(fieldModel, () -> "实体类 " + entityClass.getName()
                            + " 的 @SelfOverridableObject 匹配字段不存在: " + fieldName);
                    return fieldModel;
                })
                .collect(Collectors.toList());
    }

    private static void genTestCode(Class entityClass, String srcDir, Map<String, Object> entityMapping) throws Exception {

        if (entityMapping == null) {
            entityMapping = new LinkedHashMap<>();
        }

        List<FieldModel> fields = buildFieldModel(entityClass, entityMapping, false, "test");

        fields = copyAndFilter(fields, "createTime", "updateTime", "lastUpdateTime");

        Map<String, Object> paramsMap = MapUtils.put(threadContext.getAll(true)).build();

        String serviceName = entityClass.getSimpleName() + "Service";

        //切换实体类
        entityClass(entityClass);

        genCode(entityClass, "test/service_test.ftl", fields, srcDir, modulePackageName(), serviceName + "Test"
                , params -> {
                    params.put("servicePackageName", servicePackage());
                    params.put("bizServicePackageName", bizServicePackage());
                    params.put("serviceName", serviceName);
                    params.putAll(paramsMap);

                    params.put("isServiceTest", true);
                });
    }


    private static String subPkgName() {
        return subPkgName(entityClass(), modulePackageName());
    }


    /***
     * //获取 模块包名 往下一级剩下的包名部分
     * 如类名   com.levin.member.entities.weixin.User
     *         模块包名是 com.levin.member
     *         返回 weixin.user
     *
     * @param entityClass
     * @param modulePackageName
     * @return
     */
    private static String subPkgName(Class entityClass, final String modulePackageName) {

        String name = entityClass.getName();

        if (name.startsWith(modulePackageName)) {
            //获取 模块包名 往下一级剩下的包名部分
            name = name.substring(modulePackageName.length() + 1).toLowerCase();

            //取下一级剩下的包名部分
            return name.contains(".") ? name.substring(name.indexOf('.') + 1) : name;

        } else {
            return entityClass.getSimpleName().toLowerCase();
        }
    }

    private static void buildInfo(Class entityClass, List<FieldModel> fields, String srcDir, Map<String, Object> paramsMap) throws Exception {

        final Consumer<Map<String, Object>> mapConsumer = (map) -> {
            map.putAll(paramsMap);
            map.put("servicePackageName", servicePackage());
        };

        genCode(entityClass, INFO_FTL, fields, srcDir,
                servicePackage() + ".info",
                entityClass.getSimpleName() + "Info", mapConsumer);

//        genCode(entityClass, SIMPLE_INFO_FTL, fields, srcDir,
//                servicePackage() + ".info",
//                "Simple" + entityClass.getSimpleName() + "Info", mapConsumer);
    }

    private static void buildEvt(Class entityClass, List<FieldModel> fields, String srcDir, Map<String, Object> paramsMap, String type) throws Exception {

        // List<FieldModel> tempFiles = copyAndFilter(fields, "createTime", "updateTime", "lastUpdateTime");

        final String pkgName = servicePackage() + ".req";

        final Consumer<Map<String, Object>> mapConsumer = (map) -> {
            map.putAll(paramsMap);
            map.put("servicePackageName", servicePackage());
        };

        if ("query".equalsIgnoreCase(type)) {

            final String simpleQueryName = "SimpleQuery" + entityClass.getSimpleName() + "Req";
            final String queryName = "Query" + entityClass.getSimpleName() + "Req";
            Object reqExtendClass = paramsMap.get("reqExtendClass");

            paramsMap.put("reqExtendClass", resolveGenericRequestExtendClass(reqExtendClass, "T"));
            genCode(entityClass, SIMPLE_QUERY_EVT_FTL, fields, srcDir, pkgName, simpleQueryName, mapConsumer);

            paramsMap.put("reqExtendClass", simpleQueryName + "<" + queryName + ">");

            //查询
            genCode(entityClass, QUERY_EVT_FTL, fields, srcDir,
                    pkgName, queryName, mapConsumer);

            paramsMap.put("reqExtendClass", reqExtendClass);

//            //统计
//            genCode(entityClass, STAT_EVT_FTL, fields, srcDir,
//                    pkgName, "Stat" + entityClass.getSimpleName() + "Req", mapConsumer);

        } else if ("create".equalsIgnoreCase(type)) {
            genCode(entityClass, CREATE_EVT_FTL, fields, srcDir,
                    pkgName, "Create" + entityClass.getSimpleName() + "Req", mapConsumer);

//            genCode(entityClass, SIMPLE_CREATE_EVT_FTL, fields, srcDir,
//                    pkgName, "SimpleCreate" + entityClass.getSimpleName() + "Req", mapConsumer);

        } else if ("update".equalsIgnoreCase(type)) {

            final String tempName = "SimpleUpdate" + entityClass.getSimpleName() + "Req";
            genCode(entityClass, SIMPLE_UPDATE_EVT_FTL, fields, srcDir, pkgName, tempName, mapConsumer);


            Object reqExtendClass = paramsMap.get("reqExtendClass");

            paramsMap.put("reqExtendClass", tempName);

            genCode(entityClass, UPDATE_EVT_FTL, fields, srcDir,
                    pkgName, "Update" + entityClass.getSimpleName() + "Req", mapConsumer);

            paramsMap.put("reqExtendClass", reqExtendClass);

        } else if ("delete".equalsIgnoreCase(type)) {

            //删除
            genCode(entityClass, DEL_EVT_FTL, fields, srcDir,
                    pkgName, "Delete" + entityClass.getSimpleName() + "Req", mapConsumer);
            //ID查询
            genCode(entityClass, BASE_ID_EVT_FTL, fields, srcDir,
                    pkgName, entityClass.getSimpleName() + "IdReq", mapConsumer);
        }

    }

    private static String resolveGenericRequestExtendClass(Object reqExtendClass, String typeParameter) {
        String extendClass = String.valueOf(reqExtendClass);
        return extendClass.endsWith("<") ? extendClass + typeParameter + ">" : extendClass;
    }

    private static void buildService(Class entityClass, List<FieldModel> fields, Map<String, Object> paramsMap) throws Exception {

        final String pkgName = servicePackage();


        String serviceDir = serviceDir();
        String serviceImplDir = serviceImplDir();
        String starterDir = starterDir();

        String boDir = File.separator + "bo" + File.separator + entityClass.getSimpleName().toLowerCase();

        final String serviceName = entityClass.getSimpleName() + "Service";
        final String mapperName = entityClass.getSimpleName() + "Mapper";

        final Consumer<Map<String, Object>> genParams = params -> {
            params.put("servicePackageName", pkgName);
            params.put("serviceName", serviceName);
            params.put("mapperName", mapperName);
            params.putAll(paramsMap);
            params.put("isService", true);
        };


        //生成通用服务类
        genCode(entityClass, SERVICE_FTL, fields, serviceDir, pkgName, serviceName, genParams);
        genCode(entityClass, MAPPER_FTL, fields, serviceDir, pkgName, mapperName, genParams);

        //生成业务服务类
        genCode(entityClass, BIZ_SERVICE_FTL, fields, serviceDir, bizServicePackage(), "Biz" + serviceName, genParams);
        genCode(entityClass, BIZ_MAPPER_FTL, fields, serviceDir, bizServicePackage(), "Biz" + mapperName, genParams);

        //统计
        genCode(entityClass, STAT_EVT_FTL, fields, serviceDir,
                bizServicePackage() + boDir.replace(File.separator, "."), "Stat" + entityClass.getSimpleName() + "Req", genParams);

        genCode(entityClass, BIZ_SERVICE_IMPL_FTL, fields, serviceImplDir, bizServicePackage(), "Biz" + serviceName + "Impl", genParams);

        //加入服务类
        serviceClassList((pkgName + "." + serviceName).replace("..", "."));

        serviceClassNameList(serviceName);

        genCode(entityClass, SERVICE_IMPL_FTL, fields, serviceImplDir, pkgName, serviceName + "Impl", genParams);

    }


    private static void buildAdminApiController(Class entityClass, List<FieldModel> fields, String srcDir, Map<String, Object> paramsMap) throws Exception {

        final Consumer<Map<String, Object>> mapConsumer = (params) -> {
            params.put("servicePackageName", servicePackage());
            params.put("bizServicePackageName", bizServicePackage());
            params.put("isCreateBizController", isCreateBizController());
            params.put("controllerPackageName", controllerPackage());
            params.put("serviceName", entityClass.getSimpleName() + "Service");
            params.putAll(paramsMap);
            params.put("isController", true);
        };

        //加入控制器类
        String className = entityClass.getSimpleName() + "Controller";

        controllerClassList((controllerPackage() + "." + className).replace("..", "."));

        genCode(entityClass, CONTROLLER_FTL, fields, srcDir, controllerPackage(), className, mapConsumer);

        if (isCreateBizController()) {

            String bizClassName = "Biz" + className;

            controllerClassList((bizControllerPackage() + "." + bizClassName).replace("..", "."));

            genCode(entityClass, BIZ_CONTROLLER_FTL, fields, srcDir, bizControllerPackage(), bizClassName, mapConsumer);
        }
    }

    private static void buildClientApiController(Class entityClass, List<FieldModel> fields, String srcDir, Map<String, Object> paramsMap) throws Exception {

        if (StrUtil.isBlank(srcDir)) {
            // logger.warn("未指定客户端控制器生成目录，将不生成客户端控制器");
            return;
        }

        final Consumer<Map<String, Object>> mapConsumer = (params) -> {
            params.put("servicePackageName", servicePackage());
            params.put("bizServicePackageName", bizServicePackage());
            params.put("isCreateBizController", isCreateBizController());
            params.put("controllerPackageName", controllerPackage());
            params.put("serviceName", entityClass.getSimpleName() + "Service");
            params.putAll(paramsMap);
            params.put("isController", true);
        };

        //加入控制器类
        String className = entityClass.getSimpleName() + "Controller";

        controllerClassList((controllerPackage() + "." + className).replace("..", "."));

        // genCode(entityClass, CONTROLLER_FTL, fields, srcDir, controllerPackage(), className, mapConsumer);

        if (isCreateBizController()) {

            String bizClassName = "Biz" + className;

            controllerClassList((bizControllerPackage() + "." + bizClassName).replace("..", "."));

            genCode(entityClass, CLIENT_BIZ_CONTROLLER_FTL, fields, srcDir, bizControllerPackage(), bizClassName, mapConsumer);
        }

    }


    /**
     * @param mavenProject
     * @param codeGenParams
     */
    public static void tryGenAdminUiFile(MavenProject mavenProject, Map<String, Object> codeGenParams) {

//        File adminDir = new File(adminUiDir);
//        adminDir.mkdirs();

        //  controllerDir, serviceDir, adminUiDir;

        try {
//            if (!new File(adminDir, ".gitignore").exists()) {
//                Runtime.getRuntime().exec("git clone https://gitee.com/zhuox/vma-antd-vue-demo .", new String[0], adminDir).waitFor();
//
//                FileUtils.deleteDirectory(new File(adminDir, ".git"));
//            }
        } catch (Exception e) {
            logger.info("git clone fail", e);
        }

    }

    /**
     * @param entityClass
     * @param template
     * @param fields
     * @param srcDir
     * @param classPackageName
     * @param className
     * @param callbacks
     * @throws Exception
     */
    private static void genCode(Class entityClass, final String template, List<FieldModel> fields, String srcDir,
                                String classPackageName, String className, Consumer<Map<String, Object>>... callbacks) throws Exception {

        //去除
        classPackageName = classPackageName.replace("..", ".");

        Map<String, Object> params = getBaseInfo(entityClass, fields, classPackageName, className);

        if (callbacks != null) {
            for (Consumer<Map<String, Object>> callback : callbacks) {
                callback.accept(params);
            }
        }

        if (params.containsKey("reqExtendClass")
                && params.get("reqExtendClass").toString().trim().endsWith("<")) {
            params.put("reqExtendClass", params.get("reqExtendClass") + className + ">");
        }

        String genFilePath = srcDir.replace(File.separator + File.separator, File.separator)
                + File.separator
                + classPackageName.replace(".", File.separator)
                + File.separator + className + ".java";

        genFileByTemplate(template, params, genFilePath);
    }


    private static Map<String, Object> getBaseInfo(Class entityClass, List<FieldModel> fields, String packageName, String genClassName) {

        String entityTitle = "";

        String entityDesc = "";

        Schema schema = (Schema) entityClass.getAnnotation(Schema.class);
        if (schema != null) {
            entityDesc = schema.description();
            entityTitle = schema.title();
        }

        if (!StringUtils.hasText(entityTitle)
                && StringUtils.hasText(entityDesc)) {
            String[] splitDesc = LangUtils.splitDesc(entityDesc);
            entityTitle = splitDesc[0];
            entityDesc = splitDesc[1];
        }

        if (!StringUtils.hasText(entityTitle)) {
            entityTitle = entityClass.getSimpleName();
        }

        Map<String, Object> params = new LinkedHashMap<>();

        params.put("modulePackageName", modulePackageName());

        params.put("entityClassPackage", entityClass.getPackage().getName());
        params.put("entityClassName", entityClass.getName());
        params.put("entityName", entityClass.getSimpleName());

        params.put("packageName", packageName);
        params.put("className", genClassName);

        params.put("entityTitle", entityTitle);
        params.put("entityDesc", entityDesc);

        params.put("camelStyleModuleName", splitAndFirstToUpperCase(moduleName()));

        params.put("serialVersionUID", "" + entityClass.getName().hashCode());

        params.put("pkField", fields.stream().filter(FieldModel::isPk).findFirst().orElse(null));

        ClassModel classModel = new ClassModel(entityClass).setFieldModels(fields);

        classModel.getImports().add(Serializable.class.getName());
        classModel.getImplementsList().add("Serializable");

        Set<Class<?>> visited = new HashSet<>();

        // 从当前类开始，一直往上遍历所有父类
        for (ResolvableType currentType = ResolvableType.forClass(entityClass);
             currentType != ResolvableType.NONE;
             currentType = currentType.getSuperType()) {

            // 获取当前类实现的所有接口
            for (ResolvableType interfaceType : currentType.getInterfaces()) {

                Class<?> rawInterface = interfaceType.getRawClass();

                if (rawInterface == null || visited.contains(rawInterface)) {
                    continue;
                }

                visited.add(rawInterface);

                // 解析接口上的泛型实际类型
                final String genericStr = com.levin.commons.utils.ClassUtils.resolvableType2GenericStr(interfaceType, resolve -> {

                    classModel.getImports().add(resolve.getName());

                    if (resolve.isAnnotationPresent(Entity.class)) { // || resolve.isAnnotationPresent(MappedSuperclass.class)

                        classModel.getImports().add(getInfoClassImport(resolve));

                        return resolve.getSimpleName() + "Info";
                    } else {
                        return resolve.getSimpleName();
                    }

                });

                classModel.getImplementsList().add(genericStr);

            }
        }

        params.put("classModel", classModel);

        params.put("implementsListStr", classModel.getImplementsList().stream().collect(Collectors.joining(", ")));
        params.put("implementsList", classModel.getImplementsList());


        //默认

        if (MultiTenantObject.class.isAssignableFrom(entityClass)) {
//            classModel.getImports().add(JsonIgnoreProperties.class.getName());
//            classModel.getAnnotations().add("@JsonIgnoreProperties({\"tenantId\"})");
        }

        //分解字段类型
        LinkedMultiValueMap<String, FieldModel> multiValueMap = new LinkedMultiValueMap<>();

        Set<String> impList = fields.stream().map(f -> f.imports.stream().map(t -> t.trim()).filter(t -> !t.startsWith("java.lang.")).collect(Collectors.toSet()))
                .reduce(new LinkedHashSet<>(), (f, s) -> {
                    f.addAll(s);
                    return f;
                });

        classModel.getImports().addAll(impList);

        params.put("importList", classModel.getImports().stream()
                .filter(t -> !t.trim().startsWith("java.lang."))
                .collect(Collectors.toSet())
        );

        for (FieldModel fieldModel : fields) {
            multiValueMap.add(fieldModel.crud.name(), fieldModel);
        }

        //放入空的列表
        Arrays.stream(FieldModel.CRUD.values()).forEach(action -> params.put(action.name() + "_fields", Collections.emptyList()));

        //默认的字段
        params.put("fields", multiValueMap.containsKey(FieldModel.CRUD.DEFAULT.name()) ? multiValueMap.remove(FieldModel.CRUD.DEFAULT.name()) : Collections.emptyList());

        //覆盖
        multiValueMap.forEach((name, list) -> params.put(name + "_fields", list));

        return params;
    }


    private static List<FieldModel> copyAndFilter(List<FieldModel> fields, String... filterNames) {
        return fields.stream()
                .filter(fm -> !Arrays.asList(filterNames).contains(fm.name))
                .collect(Collectors.toList());
    }

    /**
     * 获取未变更的内容
     *
     * @param overwriteWhenMd5IsEmpty
     * @param file
     * @param errorInfoHolder
     * @param prefix
     * @param linesFilter
     * @return
     */
    @SneakyThrows
    public static String getCompactContent(boolean overwriteWhenMd5IsEmpty, File file, ValueHolder<String> errorInfoHolder, String prefix, Function<List<String>, String> linesFilter) {

        if (file == null || !file.exists()) {
            return "";
        }

        //读取旧文件内容
        List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

        final String md5Line = lines.stream().filter(StringUtils::hasText)
                .filter(line -> line.contains(prefix))
                .findFirst()
                .orElse(null);

        int startIdx = StringUtils.hasText(md5Line) ? md5Line.indexOf(prefix) : -1;

        if (startIdx == -1) {

            logger.warn("目标文件：" + file + " 已经存在，但没有发现生成关键字<<<{}>>>, {}, 将被忽略。", prefix, startIdx);

            errorInfoHolder.setHasValue(true).setValue("目标文件：" + file.getAbsolutePath() + " 已经存在, 但没有发现生成关键字:" + prefix);

            return null;
        }

        StringBuilder info = new StringBuilder();

        //提取md5
        final String md5 = md5Line.substring(startIdx, md5Line.indexOf("]", startIdx))
                .substring(prefix.length());

        String fileOldCompactContent = linesFilter.apply(lines);

        //1、去除空行，trim 去除关键字后的文件内容
        if (md5.equals(SecureUtil.md5(fileOldCompactContent))) {
            return fileOldCompactContent;
        } else {
            info.append("去除空行 -> 裁剪空字符串");
        }

        //如果是Java文件
        if (file.getName().trim().toLowerCase().endsWith(".java")) {
            try {

                CompilationUnit cu = StaticJavaParser.parse(String.join("\n", lines));

                //2、格式化后比较
                lines = Arrays.asList(cu.toString().split("[\r\n]"));

                fileOldCompactContent = linesFilter.apply(lines);

                if (md5.equals(SecureUtil.md5(fileOldCompactContent))) {
                    return fileOldCompactContent;
                }

                info.append(" -> ").append("格式化");

                //3、删除注释代码后再比较
                cu.getAllComments().forEach(com.github.javaparser.ast.Node::remove);

                lines = Arrays.asList(cu.toString().split("[\r\n]"));

                fileOldCompactContent = linesFilter.apply(lines);

                if (md5.equals(SecureUtil.md5(fileOldCompactContent))) {
                    return fileOldCompactContent;
                }

                info.append(" -> ").append("去除代码所有注释");

            } catch (Exception e) {
                logger.error("Java文件{}代码解析失败，{}", file.getAbsolutePath(), e.getMessage());
            }
        }

        if (overwriteWhenMd5IsEmpty && !StringUtils.hasText(md5)) {
            logger.warn("目标文件：{}已经存在，但校验的MD5为空，文件内容将会被替换。", file);
            return fileOldCompactContent;
        }

        logger.error("目标文件：{}已经存在，并且被修改过，跳过。校验md5：{}，内容逐步校验逻辑：{}。", file, md5, info);

        errorInfoHolder.setHasValue(true).setValue("目标文件：" + file.getAbsolutePath() + " 并且被修改过, 无法覆盖.");

        //实现

        return null;
    }

    public static void genSameNameFileByTemplate(final String template, Map<String, Object> params, String path) throws Exception {

        Assert.hasText(template, "模板不能为空");

        Assert.hasText(path, "路径不能为空");

        genFileByTemplate(false, template, params, new File(path, new File(template).getName()));

    }

    public static void genFileByTemplate(final String template, Map<String, Object> params, String filePath) throws Exception {
        genFileByTemplate(false, template, params, new File(filePath));
    }


    /**
     * 生成文件，如果文件存在已经被修改，则直接返回。
     *
     * @param template
     * @param params
     * @param outFile
     * @throws Exception
     */
    public static void genFileByTemplate(boolean overwriteWhenMd5IsEmpty, final String template, Map<String, Object> params, File outFile) throws Exception {


        //复制
        params = new LinkedHashMap<>(params);

        final boolean isJavaSrcFile = outFile.getName().trim().toLowerCase().endsWith(".java");

        String path = outFile.getAbsoluteFile().getCanonicalPath();

        File baseDir = baseDir();
        if (baseDir != null && baseDir.exists()) {
            path = path.substring(baseDir.getCanonicalPath().length());
        }

        final String prefix = "代码生成哈希校验码：[";

        final String keyword = "@author Auto gen by simple-dao-codegen, @time:";

        //内容过滤器
        final Function<List<String>, String> linesFilter = lines -> lines.stream()
                //去除空行
                .filter(StringUtils::hasText)
                //不包含生成标记行，里面有动态时间
                .filter(line -> !line.contains(keyword) && !line.contains(prefix))
                //去除空格
                .map(StringUtils::trimWhitespace)
                .collect(Collectors.joining());


        //获取旧文件内容，并且判断是否跳过，比如文件被修改过
        final ValueHolder<String> errorInfoHolder = new ValueHolder<>();
        final String fileOldCompactContent = getCompactContent(overwriteWhenMd5IsEmpty, outFile, errorInfoHolder, prefix, linesFilter);

        params.put("fileName", outFile.getName());
        params.put("templateFileName", template.replace("\\", "/"));


        if (errorInfoHolder.hasValue()) {

            String groovyScript = isInterruptWhenTargetFileChangedByGroovyScript();

            params.put("filePath", outFile.getAbsolutePath());

            final String msg = "请配置代码插件的属性[isInterruptWhenTargetFileChangedByGroovyScript] " +
                    ", 这是一个groovy脚本, 可用变量: entityClassName ,fileName , filePath, 配置空就是不中断." +
                    " \n默认配置: fileName.endsWith('.java') && filePath.contains('/services/') &&  !filePath.contains('/biz/') ";

            try {
                //生成实体类相关代码的时候,才能中断
                boolean interrupt = params.containsKey("entityClassName")
                        //                    && outFile.getName().endsWith(".java")
                        //                    && (template.contains("/service/"))
                        && StrUtil.isNotBlank(groovyScript)
                        && (Boolean) ExpressionUtils.evalGroovy(groovyScript, null, params);

                if (interrupt) {
                    logger.info(msg);
                    logger.info("代码生成已经中断, 请检查错误信息或是调整配置[isInterruptWhenTargetFileChangedByGroovyScript], 当前配置的脚本:<<<{}>>>", groovyScript);
                }

                cn.hutool.core.lang.Assert.isTrue(!interrupt, () -> new CodeGenInteruptException(errorInfoHolder.getValue()));

            } catch (CodeGenInteruptException e) {
                throw e;
            } catch (Exception e) {
                logger.error(msg);
                throw new CodeGenInteruptException(e.getMessage(), e);
            }

            return;
        }

        outFile.getParentFile().mkdirs();

        StringWriter stringWriter = new StringWriter();

        getTemplate(template).process(params, stringWriter);

        //文件内容
        String fileContent = stringWriter.toString();

        if (isJavaSrcFile) {

            try {
                //google-format 并不会移除 .* 的导入
                fileContent = RemoveUnusedImports.removeUnusedImports(fileContent);
            } catch (Throwable e) {
                logger.warn("[{}] google-format优化导入失败，{}", path, e.getMessage());
            }

            //如果是Java类文件，自动格式化

            if (isOutputFormatCode()) {
                try {
                    fileContent = new com.google.googlejavaformat.java.Formatter(
                            JavaFormatterOptions.builder()
                                    //   .style(JavaFormatterOptions.Style.AOSP)
                                    .build()
                    ).formatSource(fileContent);
                } catch (Throwable e) {
                    logger.warn("[{}]生成的代码无法格式化，{}", path, e.getMessage());
                }
            }

        }

        final int startIdx = fileContent.indexOf(prefix);

        String newMd5 = "";

        String newCompactContent = "";

        if (startIdx != -1) {

            //需要hash的部分
            newCompactContent = fileContent;

            if (isJavaSrcFile) {
                try {
                    CompilationUnit cu = StaticJavaParser.parse(newCompactContent);

                    //删除注释代码后再比较
                    if (isIgnoreCodeCommentChange()) {
                        //删除注释代码后再比较
                        cu.getAllComments().forEach(com.github.javaparser.ast.Node::remove);
                    }

                    newCompactContent = cu.toString();

                } catch (Exception e) {
                    logger.error("文件{}的新内容解析失败,{}，新文件内容：<<<{}>>>", outFile.getAbsolutePath(), e.getMessage(), newCompactContent);
                    return;
                }
            }

            newCompactContent = linesFilter.apply(Arrays.asList(newCompactContent.split("[\r\n]")));

            //如果文件内容相同，没有变化，则直接返回
            if (newCompactContent.contentEquals(fileOldCompactContent)) {
                logger.debug("目标文件：" + path + " 已经存在，新生成的代码内容和旧内容相同，跳过。");
                return;
            }

            newMd5 = SecureUtil.md5(newCompactContent);

            int endIndex = fileContent.indexOf("]", startIdx);

            fileContent = fileContent.substring(0, startIdx + prefix.length()) + newMd5 + fileContent.substring(endIndex);

        }

        //写入文件
        FileUtil.writeString(fileContent, outFile, StandardCharsets.UTF_8);

        logger.info("目标文件：{} 写入成功，新内容压缩后的MD5：<{}>。", path, newMd5);

        fileContent = newCompactContent = null;

    }

    private static String getInfoClassImport(Class entity) {

        String typePackageName = entity.getPackage().getName();

        typePackageName = typePackageName.replace("entities", "services") + "."
                + entity.getSimpleName().toLowerCase() + ".info";

        return (typePackageName + ".*");

    }

    private static String getFirst(String... values) {
        return Arrays.stream(values).filter(StringUtils::hasText).findFirst().orElse(null);
    }

    public static void setLazy(FieldModel fieldModel) {
//        if (FetchType.LAZY.equals(tryGetFetchType(fieldModel.getField()))) {
//            fieldModel.setLazy(true);
//        }

        //只要是支持 FetchType 的属性
        fieldModel.setLazy(tryGetFetchType(fieldModel.getField()) != null);
    }

    public static FetchType tryGetFetchType(Field field) {
        return Stream.of(field.getAnnotations())
                .filter(Objects::nonNull)
                //排查 Basic 注解
                .filter(an -> !(an instanceof Basic))
                .map(annotation -> com.levin.commons.utils.ClassUtils.getValue(annotation, "fetch", false))
                //.filter(Objects::nonNull)
                .filter(v -> v instanceof FetchType)
                .map(v -> (FetchType) v)
                .findFirst()
                .orElse(null);
    }


    private static List<String> parseInjectAnnotationParams(InjectVar injectVar, FieldModel fieldModel) {

        List<String> result = new ArrayList<>();

        String[] domain = injectVar.domain();

        if (domain == null || domain.length == 0) {
        } else if (domain.length == 1) {
            if (!"default".equals(domain[0])) {
                result.add("domain = \"" + domain[0] + "\"");
            }
        } else {
            for (int i = 0; i < domain.length; i++) {
                if (!StringUtils.hasText(domain[i])) {
                    continue;
                }
                domain[i] = "\"" + domain[i] + "\"";
            }
            //加上挂号
            result.add("domain = {" + String.join(",", domain) + "}");
        }

        //如果不是默认值，则添加
        if (StringUtils.hasText(injectVar.value())) {

            Map<String, String> injectConstsFieldMap = new LinkedHashMap<>();
            //获取类InjectConst的字段列表
            ReflectionUtils.doWithFields(InjectConst.class, tmpField -> {
                Object v = tmpField.get(null);
                if (v instanceof String) {
                    injectConstsFieldMap.put((String) v, "InjectConst." + tmpField.getName());
                }
            });

            fieldModel.addImport(InjectConst.class);

            result.add("value = " + injectConstsFieldMap.getOrDefault(injectVar.value(), "\"" + injectVar.value() + "\""));
        }

        //如果不是默认值，则添加
        if (StringUtils.hasText(injectVar.isRequired()) && !"true".equals(injectVar.isRequired())) {
            result.add("isRequired = \"" + injectVar.isRequired() + "\"");
        }

        //如果不是默认值，则添加
        if (StringUtils.hasText(injectVar.isOverride()) && !"true".equals(injectVar.isOverride())) {
            result.add("isOverride = \"" + injectVar.isOverride() + "\"");
        }

        //如果不是默认值，则添加
        if (StringUtils.hasText(injectVar.outputVarName())) {
            result.add("outputVarName = \"" + injectVar.outputVarName() + "\"");
        }

        // //如果不是默认值，则添加
        if (StringUtils.hasText(injectVar.remark())) {
            result.add("remark = \"" + injectVar.remark() + "\"");
        }

        //如果不是默认值，则添加
        if (Object.class != injectVar.expectBaseType()) {
            fieldModel.addImport(injectVar.expectBaseType());
            result.add(String.format("expectBaseType = %s.class", injectVar.expectBaseType().getSimpleName()));
        }

        //如果不是默认值，则添加
        if (injectVar.expectGenericTypes() == null || injectVar.expectGenericTypes().length > 0) {
            for (Class<?> expectGenericType : injectVar.expectGenericTypes()) {
                fieldModel.addImport(expectGenericType);
            }
            result.add(String.format("expectGenericTypes = {%s}"
                    , Stream.of(injectVar.expectGenericTypes()).filter(Objects::nonNull).map(c -> c.getSimpleName() + ".class").collect(Collectors.joining(","))));
        }

        //如果不是默认值，则添加
        if (GenericConverter.class != injectVar.converter()) {
            fieldModel.addImport(injectVar.converter());
            result.add(String.format("converter = %s.class", injectVar.converter().getSimpleName()));
        }

        return result;
    }


    /**
     * 从 CompilationUnit 获取不包含 import 关键字和分号的导入列表
     *
     * @param compilationUnit 编译单元
     * @return 导入列表
     */
    private static List<String> getImportList(CompilationUnit compilationUnit) {

        List<String> importList = new ArrayList<>();

        // 遍历所有导入声明
        for (ImportDeclaration importDeclaration : compilationUnit.getImports()) {

            // 提取导入的类名
            String importName = importDeclaration.getName().asString();

            if (importDeclaration.isStatic()) {
                // 如果是静态导入，添加静态成员
                importName = "static " + importName;
            }

            if (importDeclaration.isAsterisk()) {
                // 如果是通配符导入，添加类名
                importName += ".*";
            }

            importList.add(importName);
        }
        return importList;
    }


    protected static boolean isMatch(String content, String action) {

        boolean anyMatch = keepAnnotationList().stream().filter(StringUtils::hasText).anyMatch(p -> PatternMatchUtils.simpleMatch(p, content));

        return (anyMatch || (content.contains("@CopyToGenCode ")
                && (
                !StringUtils.hasText(action)
                        || content.contains(" @*")
                        || content.contains(" @" + action.trim()
                )
        )));

    }

    protected static Set<String> getCopyAnnotation(MultiValueMap<Class<?>, FieldModel> nonSrcClassFieldMap, FieldModel fieldModel, String action) {

        Set<String> result = new LinkedHashSet<>();


        //innerClass.isMemberClass() || innerClass.isLocalClass() || innerClass.isAnonymousClass()

        final Class<?> cls = fieldModel.getField() != null ? fieldModel.getField().getDeclaringClass() : fieldModel.getEntityType();

        CUnit cUnit = srcFileCompilationMap.get(cls.getName());

        if (cUnit == null) {

//            logger.warn("*** 无源码类，类：{}，字段：{}"
//                    , cls.getName() + (fieldModel.getEntityType() != cls ? " <- " + fieldModel.getEntityType().getName() : "")
//                    , fieldModel.getName());

            nonSrcClassFieldMap.add(cls, fieldModel);

            //如果没有源码，则读取类的定义
            if (fieldModel.getField() != null) {
                Set<String> fieldAnnotationList = com.levin.commons.utils.ClassUtils.getFieldAnnotationList(fieldModel.getField());

                Set<String> tempResult = fieldAnnotationList.stream().filter(c -> isMatch(c.trim(), action)).collect(Collectors.toSet());

                if (!tempResult.isEmpty()) {
                    //添加导入
                    fieldModel.getImports().addAll(com.levin.commons.utils.ClassUtils.getFieldAnnotationImportList(fieldModel.getField()));
                }

                return tempResult;
            }


            return result;
        }

        FieldDeclaration field = (FieldDeclaration) (cUnit.type.getFieldByName(fieldModel.getName())).orElse(null);

        if (field == null) {
            logger.warn("*** 未发现源码解析字段，类：{}，字段：{}", cls.getName(), fieldModel.getName());
            return result;
        }

        for (AnnotationExpr annotation : field.getAnnotations()) {

            final String content = annotation.toString();

            if (isMatch(content.trim(), action)) {

                fieldModel.getImports().addAll(getImportList(cUnit.getCompilationUnit()).stream().filter(s -> !s.contains("jakarta.persistence.")).collect(Collectors.toList()));

                //复制原样的注解内容
                result.add(content);
            }

            //logger.info("{} 解析到注解：{}", fieldModel.getName(), annotation.getComment().orElse(null));
        }


        return result;
    }


    private static List<FieldModel> buildFieldModel(Class entityClass, Map<String, Object> entityMapping
            , boolean ignoreSpecificField/*是否生成约定处理字段，如：枚举新增以Desc结尾的字段*/, String action) throws Exception {

        Object defaultEntityInstance = entityClass.newInstance();

        //初始化
        com.levin.commons.utils.ClassUtils.invokeMethodByAnnotationTag(defaultEntityInstance, true, PostConstruct.class);
        com.levin.commons.utils.ClassUtils.invokeMethodByAnnotationTag(defaultEntityInstance, true, PrePersist.class);

        List<FieldModel> fieldModelList = new ArrayList<>();

        final List<Field> declaredFields = new LinkedList<>();

        final ResolvableType resolvableTypeForClass = ResolvableType.forClass(entityClass);

        //  System.out.println("found " + clzss + " : " + field);
        ReflectionUtils.doWithFields(entityClass, declaredFields::add);

        boolean isMultiTenantObject = MultiTenantObject.class.isAssignableFrom(entityClass);

        boolean isOrganizedObject = OrganizedObject.class.isAssignableFrom(entityClass);
        boolean isPersonalObject = PersonalObject.class.isAssignableFrom(entityClass);

        final boolean isQueryObj = "query".equalsIgnoreCase(action);
        final boolean isInfoObj = "info".equalsIgnoreCase(action);
        final boolean isCreateObj = "create".equalsIgnoreCase(action);
        final boolean isUpdateObj = "update".equalsIgnoreCase(action);
        final boolean isDeleteObj = "delete".equalsIgnoreCase(action);


        final DiscriminatorColumn discriminatorColumn = AnnotatedElementUtils.findMergedAnnotation(entityClass, DiscriminatorColumn.class);

        MultiValueMap<Class<?>, FieldModel> nonSrcClassFieldMap = new LinkedMultiValueMap<>();

        for (Field field : declaredFields) {

            field.setAccessible(true);

            final Object defaultFieldValue = field.get(defaultEntityInstance);

            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

//            if (field.isAnnotationPresent(Ignore.class)) {
//                continue;
//            }


            final boolean isFieldIgnore = field.isAnnotationPresent(Ignore.class);


            final ResolvableType forField = ResolvableType.forField(field, resolvableTypeForClass);


            final Class<?> fieldType = forField.resolve(field.getType());


            if (field.getType() != fieldType) {
                logger.info("*** " + entityClass + "[" + action + "] 发现泛型字段 : " + field + " --> " + fieldType);
            }

            if (fieldType == Object.class) {
                logger.warn("*** " + entityClass + "[" + action + "] 发现根基类型字段 : " + field + " --> " + fieldType);
            }


            if (ignoreSpecificField
                    && isMultiTenantObject
                    && field.getName().equals("tenantId")) {
                //多租户字段
                logger.debug("*** " + entityClass + "[" + action + "] 忽略多租户字段 tenantId : " + field + " --> " + fieldType);
                continue;
            }

            if (ignoreSpecificField
                    && isOrganizedObject
                    && field.getName().equals("orgId")) {
                //多租户字段
                logger.debug("*** " + entityClass + "[" + action + "] 忽略组织字段 orgId : " + field + " --> " + fieldType);
                continue;
            }

            if (ignoreSpecificField
                    && isPersonalObject
                    && field.getName().equals("ownerId")) {
                //多租户字段
                logger.debug("*** " + entityClass + "[" + action + "] 忽略个人字段 ownerId : " + field + " --> " + fieldType);
                continue;
            }

            boolean isIterable = fieldType.isArray() || Iterable.class.isAssignableFrom(fieldType);

            Class<?> subType = isIterable ? (fieldType.isArray() ? forField.getComponentType().resolve() : forField.resolveGeneric()) : null;

            FieldModel fieldModel = new FieldModel(entityClass)
                    .setSchemaDescUseConstRef(isSchemaDescUseConstRef());

            fieldModel.setField(field)
                    .setResolvableType(forField)
                    .addImport(InjectVar.class)
                    .addImport(InjectConst.class);
            fieldModel.setName(field.getName());


            fieldModel.setPrimitiveAttrAnnotation(Stream.of(field.getAnnotations()).filter(an ->
                            Stream.of(PrimitiveValue.class, Convert.class, EmbeddedId.class, Embedded.class
                                            , JavaType.class, JdbcType.class, JdbcTypeCode.class
                                            , org.hibernate.annotations.Type.class)
                                    .anyMatch(t -> t == an.annotationType()))
                    .findFirst()
                    .orElse(null)
            );

            if (Map.class.isAssignableFrom(fieldType)
                    && !fieldModel.isPrimitiveAttr()) {
                //暂不支持Map
//                logger.warn("*** " + entityClass + "[" + action + "] 发现不支持的字段 : " + field + " --> " + fieldType);
//                continue;
            }

            if (CharSequence.class.isAssignableFrom(fieldType)) {
                if (isUnboundedTextField(field)) {
                    fieldModel.setTextLength(null);
                } else if (field.isAnnotationPresent(Column.class)) {
                    fieldModel.setTextLength(field.getAnnotation(Column.class).length());
                } else {
                    fieldModel.setTextLength(null);
                }
            }


            fieldModel.setType(fieldType);
            fieldModel.setEleType(subType);

            fieldModel.setBaseType(isBaseType(forField, fieldType));

            fieldModel.setEnumerable(fieldType.isEnum());

            //fieldModel.setJpaEntity(fieldType.isAnnotationPresent(Entity.class));

            fieldModel.addImport(fieldType);

            if (subType != null) {
                fieldModel.addImport(subType);
            }

            //  字段
            fieldModelList.add(fieldModel);


            setLazy(fieldModel);

            fieldModel.setTypeName(com.levin.commons.utils.ClassUtils.resolvableType2GenericStr(forField, resolve -> {

                fieldModel.addImport(resolve);

                if (resolve.isAnnotationPresent(Entity.class)) { // || resolve.isAnnotationPresent(MappedSuperclass.class)

                    fieldModel.getImports().add(getInfoClassImport(resolve));

                    fieldModel.setLazy(true);
                    fieldModel.setBaseType(false);

                    fieldModel.setJpaEntity(true);

                    return resolve.getSimpleName() + "Info";
                } else {
                    return resolve.getSimpleName();
                }

            }));

            //是否乐观锁字段
            fieldModel.setOptimisticLock(field.isAnnotationPresent(Version.class));

            fieldModel.setReadOnly(Modifier.isFinal(field.getModifiers()));

            if (field.isAnnotationPresent(Schema.class)) {
                Schema schema = field.getAnnotation(Schema.class);

                // 只读字段
                fieldModel.setReadOnly(fieldModel.isReadOnly() || schema.readOnly() || (Schema.AccessMode.READ_ONLY.equals(schema.accessMode())));

                fieldModel.setDefaultValue(toJsonStr(schema.defaultValue()));

                if (StringUtils.hasText(schema.title())) {

                    fieldModel.setTitle(schema.title())
                            .setDesc(schema.description())
                    //  .setDescDetail(schema.title() + ":" + schema.description())
                    ;

                } else if (StringUtils.hasText(schema.description())) {

                    String[] splitDesc = CommentUtils.splitDesc(schema.description());

                    fieldModel.setTitle(splitDesc[0])
                            .setDesc(splitDesc[1])
                    //    .setDescDetail(splitDesc[0] + ":" + splitDesc[1])
                    ;
                }

//                fieldModel.setTitle(schema.title())
//                        .setDesc(getFirst(schema.description(), schema.title(), field.getName()))
//                        .setDescDetail(schema.title() + schema.description());

            } else if (field.isAnnotationPresent(Desc.class)) {
                Desc desc = field.getAnnotation(Desc.class);
                fieldModel.setTitle(desc.value());
                fieldModel.setDesc(desc.detail());
            } else {
                fieldModel.setTitle(field.getName());
            }

            if (fieldModel.getPrimitiveAttrAnnotation() != null) {
                fieldModel.addAnnotation(PrimitiveValue.class, "refDefinition=\"" + fieldModel.getPrimitiveAttrAnnotation().annotationType().getName() + "\"");
            }

            if (!fieldModel.isEnumerable()
                    && (field.isAnnotationPresent(FormItem.class) || field.isAnnotationPresent(Options.class))) {

                Options[] options = field.getAnnotationsByType(Options.class);

                if (options == null || options.length < 1) {

                    FormItem formItem = field.getAnnotation(FormItem.class);

                    if (formItem != null) {
                        options = formItem.options();
                    }
                }

                //是否可枚举
                fieldModel.setEnumerable(options != null &&
                        Stream.of(options)
                                .anyMatch(op -> StringUtils.hasText(op.dictCode()) || (op.items() != null && op.items().length > 0))
                );
            }

            fieldModel.setPk(field.isAnnotationPresent(Id.class) || field.isAnnotationPresent(EmbeddedId.class));

            if (fieldModel.isPk()) {
                fieldModel.addAnnotation(Id.class);
            }

            fieldModel.addImport(field.getType());

            final boolean isDiscriminatorColumn = discriminatorColumn != null && fieldModel.getName().equals(discriminatorColumn.name());

            fieldModel.setNotCreate(isDiscriminatorColumn);

            fieldModel.setNotUpdate(isNotUpdateField(fieldModel, field, isDiscriminatorColumn));

            if (fieldModel.isPk()) {
                fieldModel.setRequired(true);
                fieldModel.setAutoGenValue(field.isAnnotationPresent(GeneratedValue.class)
                        && !field.getAnnotation(GeneratedValue.class).strategy().equals(GenerationType.AUTO));
            } else {
                fieldModel.setUk(field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).unique());
                fieldModel.setRequired(isRequiredField(entityClass, field));
            }

            //是否是自动生成字段
            fieldModel.setAutoGenValue(field.isAnnotationPresent(GeneratedValue.class));

            if (field.isAnnotationPresent(ManyToOne.class) ||
                    field.isAnnotationPresent(OneToOne.class)) {
                fieldModel.setJpaEntity(true);
//                if (field.isAnnotationPresent(ManyToOne.class)) {
//                    fieldModel.setLazy(field.getAnnotation(ManyToOne.class).fetch().equals(FetchType.LAZY));
//                } else if (field.isAnnotationPresent(OneToOne.class)) {
//                    fieldModel.setLazy(field.getAnnotation(OneToOne.class).fetch().equals(FetchType.LAZY));
//                }
                Object aClass = entityMapping.get(field.getName());
                if (aClass instanceof Class) {
                    fieldModel.setInfoClassName(((Class) aClass).getPackage().getName() + "." + ((Class) aClass).getSimpleName());
                }
                // fieldModel.setTestValue("null");
            }

            //生成注解
            ArrayList<String> annotations = new ArrayList<>(getCopyAnnotation(nonSrcClassFieldMap, fieldModel, action));

            if (fieldModel.isRequired() && !isQueryObj && !isUpdateObj) {
                annotations.add(CharSequence.class.isAssignableFrom(fieldType) ? "@NotBlank" : "@NotNull");
            }

            //Dao 忽略字段
            if (fieldModel.isTransient()
                    || field.isAnnotationPresent(Ignore.class)) {
                fieldModel.addAnnotation(Ignore.class);
            }

            Consumer<List<Class<? extends Annotation>>> addAnnotation =
                    classes -> classes.stream().filter(Objects::nonNull)
                            //.filter(cls -> CharSequence.class.isAssignableFrom(fieldType))
                            .filter(field::isAnnotationPresent)
                            .forEachOrdered(
                                    annotationClass -> {

                                        InjectVar injectVar = field.getAnnotation(InjectVar.class);

                                        boolean isDefaultType = injectVar.expectBaseType() == Object.class;
                                        boolean isVoidType = injectVar.expectBaseType() == void.class || injectVar.expectBaseType() == Void.class;

                                        List<String> parsedParams = parseInjectAnnotationParams(injectVar, fieldModel);

                                        if (!BeanUtils.isSimpleValueType(injectVar.expectBaseType())) {

                                            //如果是请求对象

                                            fieldModel.addImport(fieldType);

                                            parsedParams.removeIf(s -> s.trim().startsWith("expectBaseType"));
                                            parsedParams.removeIf(s -> s.trim().startsWith("expectGenericTypes"));

                                            //如果有特别指定类型，则添加expectBaseType
                                            if (!isDefaultType && !isVoidType) {
                                                parsedParams.add(String.format("expectBaseType = %s.class", fieldType.getSimpleName()));
                                            }
                                            //

                                        }

                                        annotations.add((isQueryObj ? "//" : "") + "@" + annotationClass.getSimpleName() + "(" + parsedParams.stream().collect(Collectors.joining(", ")) + ")");

                                        //如果是有效的类型，或是 domain 为 dao
                                        if (!isVoidType && (PatternMatchUtils.simpleMatch(injectVar.domain(), "dao") || !isDefaultType)) {

                                            //转换数据类型
                                            fieldModel.addImport(injectVar.expectBaseType());

                                            for (Class<?> aType : injectVar.expectGenericTypes()) {
                                                fieldModel.addImport(aType);
                                            }

                                            fieldModel.setInjectBaseType(injectVar.expectBaseType());

                                            //如果是集合
                                            if (fieldModel.isRequired() && !isQueryObj && !isUpdateObj
                                                    && Collection.class.isAssignableFrom(injectVar.expectBaseType())) {
                                                annotations.remove("@NotBlank");
                                                annotations.add("@NotEmpty");
                                            }

                                            fieldModel.typeName = injectVar.expectBaseType().getSimpleName();

                                            //基本类型或是集合
                                            fieldModel.setBaseType(BeanUtils.isSimpleProperty(injectVar.expectBaseType())
                                                    || Collection.class.isAssignableFrom(injectVar.expectBaseType()));

                                            String sub = Arrays.stream(injectVar.expectGenericTypes()).map(Class::getSimpleName).collect(Collectors.joining(","));

                                            if (StringUtils.hasText(sub)) {
                                                fieldModel.typeName += "<" + sub + ">";
                                                fieldModel.setBaseType(
                                                        fieldModel.isBaseType()
                                                                && injectVar.expectGenericTypes().length == 1
                                                                && BeanUtils.isSimpleProperty(injectVar.expectGenericTypes()[0])
                                                );
                                            }
                                            //转换数据类型
                                        }

                                        fieldModel.addImport(annotationClass);
                                    }
                            );


            addAnnotation.accept(Arrays.asList(InjectVar.class));

            Consumer<List<Class<? extends Annotation>>> addLikeAnnotation =
                    classes -> classes.stream().filter(Objects::nonNull)
                            //.filter(cls -> CharSequence.class.isAssignableFrom(fieldType))
                            .filter(field::isAnnotationPresent)
                            .forEachOrdered(
                                    annotationClass -> {
                                        fieldModel.setContains(true);
                                        fieldModel.getExtras().put("nameSuffix", annotationClass.getSimpleName());
                                    }
                            );

            addLikeAnnotation.accept(Arrays.asList(StartsWith.class, EndsWith.class, Contains.class));


            if (!isCreateObj && !isUpdateObj) {

                //默认处理密码字段
                if (field.isAnnotationPresent(JsonIgnore.class)) {
                    fieldModel.addAnnotation(field.getAnnotation(JsonIgnore.class));
                } else if (field.isAnnotationPresent(JsonIgnoreProperties.class)) {
                    fieldModel.addAnnotation(field.getAnnotation(JsonIgnoreProperties.class));
                } else if (Stream.of("password", "passwd", "pwd")
                        .anyMatch(txt -> field.getName().toLowerCase().endsWith(txt))) {
                    logger.warn("*** 类模型{}的密码字段({})，未加上忽略注解：@{}", entityClass.getSimpleName(), field.getName(), JsonIgnore.class.getSimpleName());
                    //fieldModel.addAnnotation(JsonIgnore.class);
                }
            }

            //乐观锁字段， 不允许加上忽略注解
            if (fieldModel.isOptimisticLock()) {
                fieldModel.getAnnotations().removeIf(an -> an.startsWith("@JsonIgnore"));
            }

            if (field.isAnnotationPresent(Update.class)) {
                Update update = field.getAnnotation(Update.class);

                //增量更新
                if (update.incrementMode()) {

                }
            }

            //加入所有的校验规则
            fieldModel.addAnnotations(
                    an ->  //是否为校验注解
                            an.annotationType().getPackage().equals(NotBlank.class.getPackage())
                                    //对于复杂对象，只能支持NotNull注解
                                    && (ClassUtils.isPrimitiveOrWrapper(fieldType) || an.annotationType().equals(NotNull.class))

                    , field.getAnnotations());


            //如果是JSON字段, 且没有Dao注解
            if (!isFieldIgnore
                    && fieldModel.isSimpleCollectionType()
                    && fieldModel.isJsonColumn()
                    && !fieldModel.hasDaoAnnotation()) {

                if (isQueryObj) {
                    fieldModel.addAnnotation(Eq.class, "jsonPath = \"$[*]\"");
                    logger.info("*** 类模型 {} 的JSON字段({})，加上默认的注解：@{}", entityClass.getSimpleName(), field.getName(), Eq.class.getSimpleName());
                }
            }

            //打印上面的语句的条件
            //logger.info("*** {} isFieldIgnore:{} isQueryObj:{} isSimpleCollectionType:{} isJsonColumn:{} hasDaoAnnotation:{}", field, isFieldIgnore, isQueryObj, fieldModel.isSimpleCollectionType(), fieldModel.isJsonColumn(), fieldModel.hasDaoAnnotation());

            //jakarta.validation.constraints.Size
            //jakarta.validation.constraints


            if (fieldModel.getTextLength() != null) {

                if (field.isAnnotationPresent(Lob.class)) {
                }

                //最小长度限制，通过@NotBlank，来控制
                annotations.add(String.format("@Size(%smax = %s)", "", fieldModel.getTextLength()));

                fieldModel.setTestValue("\"这是文本" + fieldModel.getTextLength() + "\"");

            }

/*
            //是否约定
            if (fieldModel.getName().endsWith("Pct")) {
                annotations.add("@Min(0)");
                annotations.add("@Max(100)");
                fieldModel.setTestValue("50");
            } else if (fieldModel.getName().endsWith("Ppt")) {
                annotations.add("@Min(0)");
                annotations.add("@Max(1000)");
                fieldModel.setTestValue("500");
            } else if (field.isAnnotationPresent(Pattern.class)) {
                String regexp = field.getAnnotation(Pattern.class).regexp();
                if (!StringUtils.isEmpty(regexp)) {
                    regexp = regexp.replace("\\", "\\\\");
                    annotations.add("@Pattern(regexp = \"" + regexp + "\")");
                }
            } else if (field.isAnnotationPresent(Size.class)) {
                annotations.add("@Size(min = " + field.getAnnotation(Size.class).min() + " , max = " + field.getAnnotation(Size.class).max() + ")");
            } else if (field.isAnnotationPresent(Min.class)) {
                annotations.add("@Min(" + field.getAnnotation(Min.class).value() + ")");
                fieldModel.setTestValue(field.getAnnotation(Min.class).value() + "");
            } else if (field.isAnnotationPresent(Max.class)) {
                annotations.add("@Max(" + field.getAnnotation(Max.class).value() + ")");
                fieldModel.setTestValue(field.getAnnotation(Max.class).value() + "");
            }*/

            fieldModel.getAnnotations().addAll(annotations);

//            if (ignoreSpecificField) {
//                buildExpandInfo(entityClass, fieldModel);
//            }

            if (fieldModel.getTestValue() == null) {
                if (fieldModel.getName().equals("sn")) {
                    String sn = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
                    fieldModel.setTestValue("\"" + sn + "\"");
                } else if (fieldModel.getName().equals("areaId")) {
                    fieldModel.setTestValue("\"1\"");
                } else if (fieldModel.isEnumerable()) {

                    if (fieldType.isEnum()) {
                        fieldModel.setTestValue(fieldType.getSimpleName() + "." + getEnumByVal(fieldType, 0).name());
                    }

                } else if (fieldModel.getType().equals(Boolean.class)) {
                    fieldModel.setTestValue("true");
                } else if (fieldModel.getType().equals(String.class)) {
                    fieldModel.setTestValue("\"" + fieldModel.getTitle() + "_1\"");
                } else if (fieldModel.getType().equals(Integer.class) || fieldModel.getType().equals(Long.class)) {
                    fieldModel.setTestValue(fieldModel.getName().endsWith("Id")
                            ? "null" : ("1" + (fieldModel.getType().equals(Long.class) ? "L" : "")));
                } else if (fieldModel.getType().equals(Double.class)) {
                    fieldModel.setTestValue("0.1d");
                } else if (fieldModel.getType().equals(Float.class)) {
                    fieldModel.setTestValue("0.1f");
                } else if (fieldModel.getType().equals(LocalDateTime.class)) {
                    fieldModel.setTestValue("LocalDateTime.now()");
                } else {
                    // fieldModel.setTestValue("null");
                }
            }


            //如果是创建对象，但是有初始化默认值

            if (isQueryObj || isUpdateObj || (isCreateObj && defaultFieldValue != null)) {
                //如果是创建对象，但是有初始化默认值，则认为允许为空
                //查询对象和更新对象，允许空值
                fieldModel.getAnnotations().removeIf(annotation -> annotation.trim().startsWith("@NotNull"));
                fieldModel.getAnnotations().removeIf(annotation -> annotation.trim().startsWith("@NotBlank"));
            }

            //如果不是字符串
            if (!CharSequence.class.isAssignableFrom(fieldType)) {
                fieldModel.getAnnotations().removeIf(annotation -> annotation.trim().startsWith("@NotBlank"));
            }

            //对应对象类型，查询对象要忽略
            if ((isQueryObj) && !fieldModel.isBaseType() && !fieldModel.isPrimitiveAttr()) {
                fieldModel.addAnnotation(Ignore.class);
            }

            //如果是创建对象，但是有初始化默认值
            if (isCreateObj && defaultFieldValue != null) {

                fieldModel.setHasDefaultValue(true)
                        .setDefaultValue(toJsonStr(defaultFieldValue.toString()));

                if (defaultFieldValue instanceof CharSequence) {
                    fieldModel.setDefaultValue(toJsonStr(defaultFieldValue.toString()));
                } else if (defaultFieldValue instanceof Long) {
                    fieldModel.setDefaultValue(defaultFieldValue + "");
                } else if (defaultFieldValue instanceof Float) {
                    fieldModel.setDefaultValue(defaultFieldValue + "");
                } else if (defaultFieldValue instanceof Double) {
                    fieldModel.setDefaultValue(defaultFieldValue + "");
                } else if (defaultFieldValue instanceof Enum) {
                    fieldModel.setDefaultValue(fieldType.getSimpleName() + "." + ((Enum<?>) defaultFieldValue).name());
                    fieldModel.setDefaultValue(toJsonStr(((Enum<?>) defaultFieldValue).name()));
                } else if (defaultFieldValue instanceof LocalDateTime) {
                    fieldModel.setDefaultValue("new Date()");
                    fieldModel.setDefaultValue("");
                }

            }

        }

        nonSrcClassFieldMap.forEach((cls, fieldModels) -> {
            logger.warn(" *** 发现[{}]依赖的无源码类：{} 关联字段: {}", entityClass.getName(), cls, fieldModels.stream().map(FieldModel::getName).collect(Collectors.joining(",")));
        });

        if (isInfoObj && isMultiTenantObject) {
            autoAddTenantNameField(entityClass, fieldModelList);
        }

        if (isInfoObj && isOrganizedObject) {
            autoAddOrgNameField(entityClass, fieldModelList);
        }

        if (isInfoObj) {

            fieldModelList.removeIf(fieldModel -> isMultiTenantObject && (Stream.of(InjectConst.TENANT_ID, InjectConst.TENANT_NAME).anyMatch(name -> fieldModel.getName().equals(name))));
            fieldModelList.removeIf(fieldModel -> isOrganizedObject && (Stream.of(InjectConst.ORG_ID, InjectConst.ORG_NAME).anyMatch(name -> fieldModel.getName().equals(name))));

            fieldModelList.removeIf(fieldModel -> isPersonalObject && (Stream.of("ownerId", "ownerName").anyMatch(name -> fieldModel.getName().equals(name))));

        }

        if (isCreateObj || isUpdateObj) {
            //移除只读字段
            fieldModelList.removeIf(FieldModel::isReadOnly);

        }

        //替换注解中的内容
        replaceAnno(fieldModelList);

        postProcess(fieldModelList, action);

        //如果字段是对象类型, 要移除 @Size @Max注解等注解
        fieldModelList.forEach(fieldModel -> {

            fieldModel.getAnnotations().removeIf(an -> fieldModel.getInjectBaseType() != null
                    && fieldModel.getInjectBaseType() != Object.class
                    && !BeanUtils.isSimpleValueType(fieldModel.getInjectBaseType())
                    && Stream.of("@Size", "@Max", "@Min").anyMatch(key -> an.trim().startsWith(key)));
        });

        return fieldModelList;
    }

    static boolean isRequiredField(Class<?> entityClass, Field field) {

        if (field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).nullable()) {
            return true;
        }

        return Arrays.stream(entityClass.getAnnotationsByType(AttributeOverride.class))
                .anyMatch(override -> field.getName().equals(override.name()) && !override.column().nullable());
    }

    static boolean isColumnUpdatable(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column == null || column.updatable();
    }

    static boolean isNotUpdateField(FieldModel fieldModel, Field field, boolean isDiscriminatorColumn) {
        return fieldModel.isPk()
                || notUpdateNames.contains(fieldModel.getName())
                || fieldModel.isJpaEntity()
                || !isColumnUpdatable(field)
                || isDiscriminatorColumn;
    }


    protected static void postProcess(List<FieldModel> fieldModels, String action) {

        for (FieldModel fieldModel : fieldModels) {

            // 设置字段关联关系
            JoinColumn joinColumn = fieldModel.getField().getAnnotation(JoinColumn.class);

            if (joinColumn != null) {

                final String name = StringUtils.hasText(joinColumn.name()) ? joinColumn.name() : StrUtil.lowerFirst(fieldModel.getType().getSimpleName()) + "Id";

                logger.info("---{}--- 设置字段关联关系：{} -> {}  : {}", action, fieldModel.getName(), name, fieldModel.getType().getName());

                fieldModels.stream()
                        .filter(fm -> fm.getName().equals(name))
                        .findFirst()
                        .ifPresent(fm -> {
                            fm.setOptionsRefTargetType(fieldModel.getType());

                            // 如果已经存在Options注解，则不添加
                            if (!fm.getField().isAnnotationPresent(Options.class)) {

                                fm.addImport(Options.class);

                                fm.addAnnotation(Options.class, "refTargetType = " + fieldModel.getType().getSimpleName() + ".class");

                            }

                        });

                //设置关联关系
            }

        }
    }


    public static String toJsonStr(String txt) {

        if (StrUtil.isBlank(txt)) {
            return "";
        }

        String jsonString = JSON.toJSONString(txt, JSONWriter.Feature.BrowserCompatible);

        return jsonString.substring(1, jsonString.length() - 1);
    }

    private static void replaceAnno(List<FieldModel> fieldModelList) {

        fieldModelList.forEach(fieldModel -> {

            fieldModel.addImport(RbacRoleInfo.class)
            // .addImport(InjectVar.class)
            //  .addImport(InjectConst.class)
            ;

            Set<String> set = new LinkedHashSet<>();

            fieldModel.getAnnotations().stream()
                    //  .filter(txt -> txt.contains("@" + ResAuthorize.class.getSimpleName()))
                    .forEach(annotation -> {
                        set.add(
                                annotationContentReplace(annotation)
                                        .replace("\"R_SA\"", "RbacRoleInfo.SA_ROLE")
                                        .replace("\"R_SAAS_*\"", "RbacRoleInfo.SAAS_ROLE_PREFIX + \"*\"")
                        );
                    });

            fieldModel.getAnnotations().clear();

            fieldModel.getAnnotations().addAll(set);
        });
    }

    private static void autoAddOrgNameField(Class entityClass, List<FieldModel> fieldModelList) {

        FieldModel orgFM = fieldModelList.stream().filter(fm -> fm.getName().equals(InjectConst.ORG_ID)).findFirst().orElse(null);

        boolean hasOrgName = fieldModelList.stream().anyMatch(fieldModel -> fieldModel.getName().equals(InjectConst.ORG_NAME));

        if (!hasOrgName && orgFM != null) {

            FieldModel fieldModel = new FieldModel(entityClass)
                    .setName(InjectConst.ORG_NAME)
                    .setTitle("组织名称")
                    .setType(String.class)
                    .setTypeName("String")
                    .setBaseType(true)
                    .setNotUpdate(true)
                    .setDesc("")
                    .setSchemaDescUseConstRef(false)
                    .setTestValue("\"组织名称\"");

            fieldModel.addImport(RefInject.class)
                    .addImport(InjectConst.class);

            fieldModel.getAnnotations().add("@RefInject(refObjectType = \"Org\", idExpr = InjectConst.ORG_ID, valueExpr = \"name\")");

            //加入和租户ID的一样的DataMasking注解
            orgFM.getAnnotations().stream()
                    .filter(annotation -> annotation.trim().startsWith("@" + DataMasking.class.getSimpleName()))
                    .forEach(fieldModel.getAnnotations()::add);

            int indexOf = fieldModelList.indexOf(orgFM);

            // 插入到ORG_ID后面
            fieldModelList.add(indexOf + 1, fieldModel);
        }
    }

    private static void autoAddTenantNameField(Class entityClass, List<FieldModel> fieldModelList) {

        FieldModel tenantFm = fieldModelList.stream().filter(fm -> fm.getName().equals(InjectConst.TENANT_ID)).findFirst().orElse(null);

        boolean hasTenantName = fieldModelList.stream().anyMatch(fieldModel -> fieldModel.getName().equals(InjectConst.TENANT_NAME));

        if (!hasTenantName && tenantFm != null) {

            FieldModel fieldModel = new FieldModel(entityClass)
                    .setName(InjectConst.TENANT_NAME)
                    .setTitle("租户名称")
                    .setType(String.class)
                    .setTypeName("String")
                    .setBaseType(true)
                    .setNotUpdate(true)
                    .setDesc("")
                    .setSchemaDescUseConstRef(false)
                    .setTestValue("\"租户名称\"");

            fieldModel.addImport(RefInject.class)
                    .addImport(InjectConst.class);

            fieldModel.getAnnotations().add("@RefInject(refObjectType = \"Tenant\", idExpr = InjectConst.TENANT_ID, valueExpr = \"name\")");

            //加入和租户ID的一样的DataMasking注解
            tenantFm.getAnnotations().stream()
                    .filter(annotation -> annotation.trim().startsWith("@" + DataMasking.class.getSimpleName()))
                    .forEach(fieldModel.getAnnotations()::add);

            int indexOf = fieldModelList.indexOf(tenantFm);

            // 插入到租户ID后面
            fieldModelList.add(indexOf + 1, fieldModel);

        }
    }

    private static String annotationContentReplace(String aTxt) {

        Map<String, String> map = annotationContentReplaceMap();

        if (map == null) {
            return aTxt;
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            aTxt = aTxt.replace(entry.getKey(), entry.getValue());
        }

        return aTxt;
    }

    private static boolean isBaseType(ResolvableType parent, Class<?> type) {

        return ClassUtils.isPrimitiveOrWrapper(type)
                || CharSequence.class.isAssignableFrom(type)
                || type.isEnum()
                || Number.class.isAssignableFrom(type)
                || Date.class.isAssignableFrom(type)
                || Temporal.class.isAssignableFrom(type) //时间
                || (type.isArray() && ClassUtils.isPrimitiveWrapper(parent.getComponentType().resolve()))
                || BeanUtils.isSimpleProperty(type)
                ;

    }

    private static boolean isUnboundedTextField(Field field) {
        if (field == null) {
            return false;
        }

        if (field.isAnnotationPresent(Lob.class)) {
            return true;
        }

        Column column = field.getAnnotation(Column.class);
        if (column != null && isUnboundedTextColumnDefinition(column.columnDefinition())) {
            return true;
        }

        JdbcTypeCode jdbcTypeCode = field.getAnnotation(JdbcTypeCode.class);
        return jdbcTypeCode != null && isUnboundedTextJdbcType(jdbcTypeCode.value());
    }

    private static boolean isUnboundedTextColumnDefinition(String columnDefinition) {
        String definition = StrUtil.trimToEmpty(columnDefinition).toLowerCase(Locale.ROOT);
        if (StrUtil.isBlank(definition)) {
            return false;
        }

        return Stream.of(definition.split("[^a-z0-9_]+"))
                .anyMatch(UNBOUNDED_TEXT_COLUMN_TYPES::contains);
    }

    private static boolean isUnboundedTextJdbcType(int jdbcTypeCode) {
        return Stream.of(
                        SqlTypes.LONGVARCHAR,
                        SqlTypes.LONGNVARCHAR,
                        SqlTypes.LONG32VARCHAR,
                        SqlTypes.LONG32NVARCHAR,
                        SqlTypes.CLOB,
                        SqlTypes.NCLOB,
                        SqlTypes.MATERIALIZED_CLOB,
                        SqlTypes.MATERIALIZED_NCLOB,
                        SqlTypes.JSON,
                        SqlTypes.JSON_ARRAY)
                .anyMatch(type -> type == jdbcTypeCode);
    }


    public static String getFieldValue(String fieldName, Object obj) {

        if (fieldName == null || obj == null) {
            return null;
        }

        Field field = ReflectionUtils.findField(obj.getClass(), fieldName);

        assert field != null;

        Object value = ReflectionUtils.getField(field, obj);

        if (value == null) {
            return null;
        }

        return value.toString();

    }

    private static void buildExpandInfo(Class entityClass, FieldModel fieldModel) {

        String name = fieldModel.getName();
        Class type = fieldModel.getType();

//        if (fieldModel.isEnumType()
////                && DescriptiveEnum.class.isAssignableFrom(type)
//                && Enum.class.isAssignableFrom(type)
//        ) {
//            //枚举描述
//            fieldModel.setExcessSuffix("Desc");
//            fieldModel.setExcessReturnType("String");
//            fieldModel.setExcessReturn("return " + name + " != null ? " + name + ".getDesc() : \"\";");
//        } else if ((type.equals(Integer.class) || type.equals(Long.class))
//                && name.endsWith("Fen")) {
//            //分转元
//            fieldModel.setExcessSuffix("2Yuan");
//            fieldModel.setExcessReturnType("Double");
//            fieldModel.setExcessReturn("return " + name + " != null ? new java.math.BigDecimal(" + name + ")\n" +
//                    "                .divide(new java.math.BigDecimal(100), 2, java.math.BigDecimal.ROUND_HALF_UP)\n" +
//                    "                .doubleValue() : null;");
//        } else if ((type.equals(Integer.class) || type.equals(Long.class))
//                && name.endsWith("Ppt")) {
//            //千分比转百分比
//            fieldModel.setExcessSuffix("2Pct");
//            fieldModel.setExcessReturnType("Double");
//            fieldModel.setExcessReturn("return " + name + " != null ? new java.math.BigDecimal(" + name + ")\n" +
//                    "                .divide(new java.math.BigDecimal(10), 1, java.math.BigDecimal.ROUND_HALF_UP)\n" +
//                    "                .doubleValue() : null;");
//        }

//        if (fieldModel.isJpaEntity()) {
//
//            String returnName = type.getSimpleName().substring(0, 1).toUpperCase() + type.getSimpleName().substring(1)
//                    + "Info";
//            String complexName = name.substring(0, 1).toUpperCase() + name.substring(1)
//                    + "Info";
//
//            fieldModel.setExcessSuffix("Info");
//            fieldModel.setExcessReturnType(returnName);
//
//            fieldModel.setExcessReturn("return " + name + " != null ? " + name + ".get" + complexName + "() : null;");
//        }

    }

    private static Template getTemplate(String templatePath) throws IOException {

        //freemark 模板路径只支持正斜杠
        templatePath = templatePath.replace("\\", "/").replace("//", "/");

        //创建一个合适的Configuration对象
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        DefaultObjectWrapper objectWrapper = new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_28).build();
        configuration.setObjectWrapper(objectWrapper);

        //这个一定要设置，不然在生成的页面中 会乱码
        configuration.setDefaultEncoding("UTF-8");

        //支持从jar中加载模板
        configuration.setClassForTemplateLoading(ServiceModelCodeGenerator.class, "/");

        if (!templatePath.startsWith(TEMPLATE_PATH)) {
            templatePath = TEMPLATE_PATH + templatePath;
        }

        //获取页面模版。
        return configuration.getTemplate(templatePath);
    }

    private static Enum getEnumByVal(Class ec, int i) {
        Iterator iter = EnumSet.allOf(ec).iterator();

        Enum e;
        do {
            if (!iter.hasNext()) {
                return null;
            }
            e = (Enum) iter.next();
        } while (e.ordinal() != i);

        return e;
    }


}
