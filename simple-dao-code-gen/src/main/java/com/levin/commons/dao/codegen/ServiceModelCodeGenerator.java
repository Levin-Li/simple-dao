package com.levin.commons.dao.codegen;

import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.SecureUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.googlejavaformat.java.JavaFormatterOptions;
import com.levin.commons.dao.annotation.Contains;
import com.levin.commons.dao.annotation.EndsWith;
import com.levin.commons.dao.annotation.Ignore;
import com.levin.commons.dao.annotation.StartsWith;
import com.levin.commons.dao.annotation.update.Update;
import com.levin.commons.dao.codegen.db.util.CommentUtils;
import com.levin.commons.dao.codegen.model.ClassModel;
import com.levin.commons.dao.codegen.model.FieldModel;
import com.levin.commons.dao.domain.MultiTenantObject;
import com.levin.commons.dao.domain.OrganizedObject;
import com.levin.commons.plugins.Utils;
import com.levin.commons.service.domain.Desc;
import com.levin.commons.service.domain.InjectVar;
import com.levin.commons.service.support.ContextHolder;
import com.levin.commons.service.support.InjectConsts;
import com.levin.commons.utils.ExceptionUtils;
import com.levin.commons.utils.LangUtils;
import com.levin.commons.utils.MapUtils;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ResolvableType;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import org.apache.maven.project.MavenProject;

public final class ServiceModelCodeGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ServiceModelCodeGenerator.class);

    public static final String DEL_EVT_FTL = "services/req/del_evt.ftl";
    public static final String UPDATE_EVT_FTL = "services/req/update_evt.ftl";
    public static final String SIMPLE_UPDATE_EVT_FTL = "services/req/simple_update_evt.ftl";
    public static final String QUERY_EVT_FTL = "services/req/query_evt.ftl";
    public static final String STAT_EVT_FTL = "services/req/stat_evt.ftl";
    public static final String BASE_ID_EVT_FTL = "services/req/base_id_req.ftl";

    public static final String SERVICE_FTL = "services/service.ftl";

    public static final String BIZ_SERVICE_FTL = "biz/biz_service.ftl";
    public static final String BIZ_SERVICE_IMPL_FTL = "biz/biz_service_impl.ftl";

    public static final String SERVICE_IMPL_FTL = "services/service_impl.ftl";
    public static final String CREATE_EVT_FTL = "services/req/create_evt.ftl";
    public static final String SIMPLE_CREATE_EVT_FTL = "services/req/simple_create_evt.ftl";
    public static final String INFO_FTL = "services/info/info.ftl";
    public static final String SELECT_INFO_FTL = "services/info/simple_info.ftl";

    public static final String CONTROLLER_FTL = "controller/controller.ftl";
    public static final String BIZ_CONTROLLER_FTL = "controller/biz_controller.ftl";

    public static final String POM_XML_FTL = "pom.xml.ftl";

    public static final String TEMPLATE_PATH = "/simple.dao/codegen/template/";


//    private static Set<Class> baseTypes = new HashSet<>();

//    private static Set<Class> collectionsTypes = new HashSet<>();

    private static Set<String> notUpdateNames = new HashSet<>();

    static {

//        baseTypes.add(String.class);
//        baseTypes.add(Date.class);

//        collectionsTypes.add(Collection.class);
//        collectionsTypes.add(Map.class);

        notUpdateNames.add("addTime");
        notUpdateNames.add("creator");
        notUpdateNames.add("createBy");
        notUpdateNames.add("createTime");
        notUpdateNames.add("createDate");

//        notUpdateNames.add("updateTime");
//        notUpdateNames.add("lastUpdateTime");

        notUpdateNames.add("sn");
    }


    private static final ContextHolder<String, Object> threadContext = ContextHolder.buildThreadContext(true);


    private static String genPom(String moduleNamePrefix, String moduleType, String srcDir, Map<String, Object> params, List<String> modules) throws Exception {

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
                .put("parent", mavenProject.getParent())
                .put("groupId", mavenProject.getGroupId())
                .put("version", mavenProject.getVersion())
                .put("packaging", mavenProject.getPackaging())
                .put("entities", mavenProject.getArtifact())
                .build();

        /////////////////////////////生成说明文件///////////////////////////////////
        String template = "模块开发说明.md";
        genFileByTemplate(template, params, mavenProject.getBasedir().getParentFile().getAbsolutePath() + File.separator + template);

        ////////////////////////////////////////////////

        final List<String> modules = new ArrayList<>(2);

        //  String moduleName = moduleName();// mavenProject.getBasedir().getParentFile().getName();

        ////////////////////////服务层////////////////////////////////

        genPom(null, "service", serviceDir(), params, modules);
        /////////////////////////////////////自举模块///////////////////////////////////////////////////////////////////

        genPom(null, "service_impl", serviceImplDir(), params, modules);

        genPom(null, "controller", controllerDir(), params, modules);
        //////////////////////////启动模块//////////////////////////////////////////

        genPom(null, "starter", starterDir(), params, modules);
        /////////////////////////////////////控制器/////////////////////////////////////////////////////////////////////////////

        genPom(null, "bootstrap", bootstrapDir(), params, modules);
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


    public static void tryGenBootstrap(MavenProject mavenProject, Map<String, Object> params) throws Exception {

        //如果没有包名，也没有发现实体类
        if (!StringUtils.hasText(modulePackageName())
                || !hasEntityClass()
        ) {
            return;
        }

        logger.info("开始生成[Bootstrap]模块代码...");

        String controllerDir = controllerDir();
        String serviceDir = serviceDir();
        String bootstrapDir = bootstrapDir();

        params.putAll(threadContext.getAll(false));

        //是否 bootstrap
        params.put("isBootstrap", true);

        params.put("camelStyleModuleName", splitAndFirstToUpperCase(moduleName()));

        String prefix = bootstrapDir + File.separator
                + modulePackageName().replace('.', File.separatorChar)
                + File.separator;

        genFileByTemplate("bootstrap/AppWebMvcConfigurer.java", params, prefix + "AppWebMvcConfigurer.java");
        genFileByTemplate("bootstrap/AppDataInitializer.java", params, prefix + "AppDataInitializer.java");
//        genFileByTemplate("bootstrap/PluginManagerController.java", params, prefix + "PluginManagerController.java");
        genFileByTemplate("bootstrap/Application.java", params, prefix + "Application.java");
        genFileByTemplate("bootstrap/BlockingFilter.java", params, prefix + "BlockingFilter.java");

        String resPath = new File(bootstrapDir).getParentFile().getCanonicalPath() + File.separator + "resources" + File.separator;

        genFileByTemplate("bootstrap/application.properties", params, resPath + "application.properties");
        genFileByTemplate("bootstrap/application.yml", params, resPath + "application.yml");
        genFileByTemplate("bootstrap/application-local.yml", params, resPath + "application-local.yml");
        genFileByTemplate("bootstrap/application-dev.yml", params, resPath + "application-dev.yml");
        genFileByTemplate("bootstrap/application-test.yml", params, resPath + "application-test.yml");
        genFileByTemplate("bootstrap/application-prod.yml", params, resPath + "application-prod.yml");

        genFileByTemplate("bootstrap/shell/startup.sh", params, resPath + "shell" + File.separator + "startup.sh");
        genFileByTemplate("bootstrap/shell/restart.sh", params, resPath + "shell" + File.separator + "restart.sh");
        genFileByTemplate("bootstrap/shell/shutdown.sh", params, resPath + "shell" + File.separator + "shutdown.sh");

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

        genFileByTemplate("bootstrap/application.properties", params, resPath + "application.properties");
        genFileByTemplate("bootstrap/application.yml", params, resPath + "application.yml");
        genFileByTemplate("bootstrap/application-local.yml", params, resPath + "application-local.yml");

        Utils.copyAndReplace(prefix, false, resTemplateDir + "bootstrap/logback.xml", new File(resPath + "logback.xml"), new HashMap<>());

        for (Class entityClass : entityClassList()) {
            genTestCode(entityClass, bootstrapDir, null);
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

        String controllerDir = controllerDir();

        String serviceDir = serviceDir();
        String serviceImplDir = serviceImplDir();

        String starterDir = starterDir();

        params.putAll(threadContext.getAll(true));

        params.put("camelStyleModuleName", splitAndFirstToUpperCase(moduleName()));

        String fileName = "index.html";
        genFileByTemplate(fileName, params, String.join(File.separator,
                controllerDir, "..", "resources", "public", modulePackageName(), "admin", fileName));

        //生成控制器配置文件
        Arrays.asList("ModuleWebMvcConfigurer"
                , "ModuleWebControllerAdvice"
                , "ModuleSwaggerConfigurer"
                , "ModuleVariableResolverConfigurer"
                , "ModuleWebSocketConfigurer"
        ).forEach(className -> genJavaFile(controllerDir, "config", className, params));

        Arrays.asList("ModulePlugin"
                , "ModuleWebInjectVarServiceImpl"
        ).forEach(className -> genJavaFile(controllerDir, "", className, params));

        genJavaFile(controllerDir, "aspect", "ModuleWebControllerAspect", params);

        //生成服务模块的文件
        Arrays.asList(
                "ModuleOption"
        ).forEach(className -> genJavaFile(serviceDir, "", className, params));

        Arrays.asList(
                "ModuleDataInitializer"
        ).forEach(className -> genJavaFile(serviceImplDir, "", className, params));

        Arrays.asList("ModuleStarterConfiguration"
        ).forEach(className -> genJavaFile(starterDir, "", className, params));

        simpleGen("starter/resources/META-INF/spring.factories.ftl", params, mavenProject);
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

    /**
     * 根据Maven目录样式生成 控制器，服务接口，请求和返回值
     *
     * @param classLoader
     * @param genParams
     */
    public static void genCodeAsMavenStyle(MavenProject mavenProject, ClassLoader classLoader
            , String buildOutputDirectory
            , Map<String, Object> genParams) throws Exception {

//            File file = new File(project.getBuild().getOutputDirectory());
        File file = new File(buildOutputDirectory);

        if (!file.exists()) {
            logger.error("***" + buildOutputDirectory + "目录不存在，请先编译实体模块。");
            return;
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
                .filter(clazz -> clazz.isAnnotationPresent(javax.persistence.Entity.class))
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


        String controllerDir = controllerDir();
        String starterDir = starterDir();
        String serviceDir = serviceDir();
        String serviceImplDir = serviceImplDir();

        ///////////////////////////////////////////////

//        genFileByTemplate("controller/BaseController.java",
//                MapUtils.put(genParams).put("modulePackageName", modulePackageName()).build(), controllerDir + File.separatorChar
//                        + modulePackageName().replace('.', File.separatorChar) + File.separatorChar
//                        + "controller" + File.separatorChar + "BaseController.java");

        genFileByTemplate(genParams, controllerDir, "controller", "BaseController.java");

//        genFileByTemplate("services/BaseService.java",
//                MapUtils.put(genParams).put("modulePackageName", modulePackageName()).build(), serviceDir + File.separatorChar
//                        + modulePackageName().replace('.', File.separatorChar) + File.separatorChar
//                        + "services" + File.separatorChar + "BaseService.java");


        genFileByTemplate(genParams, serviceImplDir, "services", "BaseService.java");
        genFileByTemplate(genParams, serviceImplDir, "services", "基础服务类开发规范.md");

        genFileByTemplate(genParams, serviceImplDir, "job", "DemoJob.java");

        //genFileByTemplate(genParams, serviceImplDir, "biz", "InjectVarServiceImpl.java");

        genFileByTemplate(genParams, serviceImplDir, "biz", "业务服务类开发规范.md");


        genFileByTemplate(genParams, serviceDir, "biz", "InjectVarService.java");
        genFileByTemplate(genParams, serviceDir, "services", "commons", "req", "BaseReq.java");
        genFileByTemplate(genParams, serviceDir, "services", "commons", "req", "MultiTenantReq.java");
        genFileByTemplate(genParams, serviceDir, "services", "commons", "req", "MultiTenantOrgReq.java");

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

            logger.info("*** 开始尝试生成实体类[" + clazz.getName() + "]相关的代码，服务目录[" + serviceDir + "],控制器目录[" + controllerDir + "]...");

            try {
                genCodeByEntityClass(clazz, serviceDir, controllerDir, genParams);
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

    ///////////////////////////////////////////////////
    public static Class<?> entityClass(Class<?> newValue) {
        return putThreadVar(newValue);
    }

    public static Class<?> entityClass() {
        return getThreadVar(null);
    }

    ///////////////////////////////////////////////////

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


    ///////////////////////////////////////////////////
    public static String moduleName(String newValue) {
        return putThreadVar(newValue);
    }

    public static String moduleName() {
        return getThreadVar(null);
    }
    ///////////////////////////////////////////////////

    public static String serviceDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String serviceDir() {
        return getThreadVar(null);
    }

    ///////////////////////////////////////////////////

    public static String serviceImplDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String serviceImplDir() {
        return getThreadVar(null);
    }

    ///////////////////////////////////////////////////


    public static String starterDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String starterDir() {
        return getThreadVar(null);
    }

    ///////////////////////////////////////////////////
    public static String bootstrapDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String bootstrapDir() {
        return getThreadVar(null);
    }

    ///////////////////////////////////////////////////
    public static String controllerDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String controllerDir() {
        return getThreadVar(null);
    }

    ///////////////////////////////////////////////////
    public static String adminUiDir(String newValue) {
        return putThreadVar(newValue);
    }

    public static String adminUiDir() {
        return getThreadVar(null);
    }

    ///////////////////////////////////////////////////
    public static String modulePackageName(String newValue) {
        return putThreadVar(newValue);
    }

    public static String modulePackageName() {
        return getThreadVar(null);
    }

    ///////////////////////////////////////////////////

    public static void isSchemaDescUseConstRef(boolean isSchemaDescUseConstRef) {
        threadContext.put(ExceptionUtils.getInvokeMethodName(), isSchemaDescUseConstRef);
    }

    public static boolean isSchemaDescUseConstRef() {
        return threadContext.getOrDefault(ExceptionUtils.getInvokeMethodName(), true);
    }

    ///////////////////////////////////////////////////

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
    public static void genCodeByEntityClass(Class entityClass, String serviceDir, String controllerDir
            , Map<String, Object> entityMapping) throws Exception {

        entityClass(entityClass);

        if (entityMapping == null) {
            entityMapping = new LinkedHashMap<>();
        }

        boolean isMultiTenant = MultiTenantObject.class.isAssignableFrom(entityClass);
        boolean isOrg = OrganizedObject.class.isAssignableFrom(entityClass);
        Map<String, Object> params = MapUtils.put(threadContext.getAll(true))
                .put("modulePackageName", modulePackageName())
                .put("isMultiTenantObject", isMultiTenant)
                .put("isOrganizedObject", isOrg)
                //设置请求对象继承的类
                .put("reqExtendClass", ((isMultiTenant && isOrg) ? "MultiTenantOrgReq" : (isMultiTenant ? "MultiTenantReq" : "BaseReq")))
                .build();

        List<FieldModel> fields = buildFieldModel(entityClass, entityMapping, false, "info");

        //info 对象按完整的字段生成
        buildInfo(entityClass, fields, serviceDir, params);

        //请求对象会忽略继承的属性
        fields = buildFieldModel(entityClass, entityMapping, true, "evt");

        buildEvt(entityClass, fields, serviceDir, params);

        buildService(entityClass, fields, params);

        buildController(entityClass, fields, controllerDir, params);

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
        };

        genCode(entityClass, INFO_FTL, fields, srcDir,
                servicePackage() + ".info",
                entityClass.getSimpleName() + "Info", mapConsumer);

        genCode(entityClass, SELECT_INFO_FTL, fields, srcDir,
                servicePackage() + ".info",
                "Simple" + entityClass.getSimpleName() + "Info", mapConsumer);
    }

    private static void buildEvt(Class entityClass, List<FieldModel> fields, String srcDir, Map<String, Object> paramsMap) throws Exception {

        // List<FieldModel> tempFiles = copyAndFilter(fields, "createTime", "updateTime", "lastUpdateTime");

        final String pkgName = servicePackage() + ".req";

        final Consumer<Map<String, Object>> mapConsumer = (map) -> {
            map.putAll(paramsMap);
            map.put("servicePackageName", servicePackage());
        };


        genCode(entityClass, CREATE_EVT_FTL, fields, srcDir,
                pkgName, "Create" + entityClass.getSimpleName() + "Req", mapConsumer);


        genCode(entityClass, SIMPLE_CREATE_EVT_FTL, fields, srcDir,
                pkgName, "SimpleCreate" + entityClass.getSimpleName() + "Req", mapConsumer);


        genCode(entityClass, UPDATE_EVT_FTL, fields, srcDir,
                pkgName, "Update" + entityClass.getSimpleName() + "Req", mapConsumer);

        genCode(entityClass, SIMPLE_UPDATE_EVT_FTL, fields, srcDir,
                pkgName, "SimpleUpdate" + entityClass.getSimpleName() + "Req", mapConsumer);

        //删除
        genCode(entityClass, DEL_EVT_FTL, fields, srcDir,
                pkgName, "Delete" + entityClass.getSimpleName() + "Req", mapConsumer);

        //ID查询
        genCode(entityClass, BASE_ID_EVT_FTL, fields, srcDir,
                pkgName, entityClass.getSimpleName() + "IdReq", mapConsumer);

        //查询
        genCode(entityClass, QUERY_EVT_FTL, fields, srcDir,
                pkgName, "Query" + entityClass.getSimpleName() + "Req", mapConsumer);

        //统计
        genCode(entityClass, STAT_EVT_FTL, fields, srcDir,
                pkgName, "Stat" + entityClass.getSimpleName() + "Req", mapConsumer);


    }

    private static void buildService(Class entityClass, List<FieldModel> fields, Map<String, Object> paramsMap) throws Exception {

        final String pkgName = servicePackage();

        final String serviceName = entityClass.getSimpleName() + "Service";

        final Consumer<Map<String, Object>> setVars = params -> {
            params.put("servicePackageName", pkgName);
            params.put("serviceName", serviceName);
            params.putAll(paramsMap);
            params.put("isService", true);
        };

        String serviceDir = serviceDir();
        String serviceImplDir = serviceImplDir();
        String starterDir = starterDir();

        //生成通用服务类
        genCode(entityClass, SERVICE_FTL, fields, serviceDir, pkgName, serviceName, setVars);

        //生成业务服务类
        genCode(entityClass, BIZ_SERVICE_FTL, fields, serviceDir, bizServicePackage(), "Biz" + serviceName, setVars);

        genCode(entityClass, BIZ_SERVICE_IMPL_FTL, fields, serviceImplDir, bizServicePackage(), "Biz" + serviceName + "Impl", setVars);

        //加入服务类
        serviceClassList((pkgName + "." + serviceName).replace("..", "."));

        serviceClassNameList(serviceName);

        genCode(entityClass, SERVICE_IMPL_FTL, fields, serviceImplDir, pkgName, serviceName + "Impl", setVars);

    }


    private static void buildController(Class entityClass, List<FieldModel> fields, String srcDir, Map<String, Object> paramsMap) throws Exception {

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

        String genFilePath = srcDir + File.separator
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

        params.put("classModel", classModel);

        if (MultiTenantObject.class.isAssignableFrom(entityClass)) {
            classModel.getImports().add(JsonIgnoreProperties.class.getName());
            classModel.getAnnotations().add("@JsonIgnoreProperties({\"tenantId\"})");
        }

        //分解字段类型
        LinkedMultiValueMap<String, FieldModel> multiValueMap = new LinkedMultiValueMap();

        Set<String> impList = fields.stream().map(f -> f.imports.stream().filter(t -> !t.trim().startsWith("java.lang.")).collect(Collectors.toSet()))
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
        params.put("fields", multiValueMap.remove(FieldModel.CRUD.DEFAULT.name()));

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
     * 生成文件，如果文件存在已经被修改，则直接返回。
     *
     * @param template
     * @param params
     * @param fileName
     * @throws Exception
     */
    public static void genFileByTemplate(final String template, Map<String, Object> params, String fileName) throws Exception {

        //复制
        params = new LinkedHashMap<>(params);

        File file = new File(fileName);

        String path = file.getAbsoluteFile().getCanonicalPath();

        File baseDir = baseDir();
        if (baseDir != null && baseDir.exists()) {
            path = path.substring(baseDir.getCanonicalPath().length());
        }

        String fileOldCompactContent = "";

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

        if (file.exists()) {

            //读取旧文件内容
            List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("utf-8"));

            //如果文件存在，将自动去除空行后然后比较文件内容，通过md5校验文件是否被修改过
            //如果被修改过，则直接放回，如果没有被修改，则继续

            boolean skip = true;

            //文件内容
            final String md5Line = lines.stream().filter(StringUtils::hasText)
                    .filter(line -> line.contains(prefix))
                    .findFirst().orElse(null);

            int startIdx = StringUtils.hasText(md5Line) ? md5Line.indexOf(prefix) : -1;

            if (startIdx != -1) {

                final String md5 = md5Line.substring(startIdx, md5Line.indexOf("]", startIdx))
                        .substring(prefix.length());

                //去除空行，去除关键字后的文件内容
                fileOldCompactContent = linesFilter.apply(lines);

                //垃圾回收
                lines = null;

                //关键逻辑，如果文件存在，但是文件没有被修改过，则可以覆盖
                if (md5.equals(SecureUtil.md5(fileOldCompactContent))) {
                    skip = false;
                    logger.debug("目标文件：" + path + "(MD5={}) 已经存在，但是没有被修改过。", md5);
                } else {
                    logger.info("目标文件：" + path + " 已经存在，并且被修改过，不覆盖。");
                }

            } else {
                logger.warn("目标文件：" + path + " 已经存在，但没有发现生成关键字<<<{}>>>, {}, 将被忽略。", prefix, startIdx);
            }

            if (skip) {
                return;
            }
        }

        file.getParentFile().mkdirs();

        //文件名
        params.put("fileName", file.getName());
        params.put("templateFileName", template.replace("\\", "/"));

        StringWriter stringWriter = new StringWriter();

        getTemplate(template).process(params, stringWriter);

        //文件内容
        String fileContent = stringWriter.toString();

        if (fileName.endsWith(".java")) {
            //如果是Java类文件，自动格式化
            try {
                fileContent = new com.google.googlejavaformat.java.Formatter(
                        JavaFormatterOptions.builder().style(JavaFormatterOptions.Style.AOSP).build()
                ).formatSource(fileContent);
            } catch (Exception e) {
                logger.warn("无法格式化生成的代码，" + path);
            }
        }

        int startIdx = fileContent.indexOf(prefix);

        if (startIdx != -1) {
            //需要hash的部分
            String newCompactContent = linesFilter.apply(Arrays.asList(fileContent.split("[\r\n]")));

            //如果文件内容相同，没有变化，则直接返回
            if (newCompactContent.contentEquals(fileOldCompactContent)) {
                logger.debug("目标文件：" + path + " 已经存在，且新旧内容相同，跳过。");
                return;
            }

            String md5 = SecureUtil.md5(newCompactContent);

            int endIndex = fileContent.indexOf("]", startIdx);

            fileContent = fileContent.substring(0, startIdx + prefix.length()) + md5 + fileContent.substring(endIndex);
        }

        //写入文件
        FileUtil.writeString(fileContent, file, "utf-8");

        fileContent = null;
        fileOldCompactContent = null;

        logger.info("目标文件：" + path + " 写入成功。");
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

    private static List<FieldModel> buildFieldModel(Class entityClass, Map<String, Object> entityMapping, boolean ignoreSpecificField/*是否生成约定处理字段，如：枚举新增以Desc结尾的字段*/, String action) throws Exception {

        Object obj = entityClass.newInstance();

        List<FieldModel> fieldModelList = new ArrayList<>();

        final List<Field> declaredFields = new LinkedList<>();

        ResolvableType resolvableTypeForClass = ResolvableType.forClass(entityClass);

        //  System.out.println("found " + clzss + " : " + field);
        ReflectionUtils.doWithFields(entityClass, declaredFields::add);

        boolean isMultiTenantObject = MultiTenantObject.class.isAssignableFrom(entityClass);
        boolean isOrganizedObject = OrganizedObject.class.isAssignableFrom(entityClass);

        for (Field field : declaredFields) {

            field.setAccessible(true);

            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }

            ResolvableType forField = ResolvableType.forField(field, resolvableTypeForClass);
            final Class<?> fieldType = forField.resolve(field.getType());

            if (field.getType() != fieldType) {
                logger.info("*** " + entityClass + "[" + action + "] 发现泛型字段 : " + field + " --> " + fieldType);
            }

            if (fieldType == Object.class) {
                logger.warn("*** " + entityClass + "[" + action + "] 发现根基类型字段 : " + field + " --> " + fieldType);
            }

            if (Map.class.isAssignableFrom(fieldType)) {
                //暂不支持Map
                logger.warn("*** " + entityClass + "[" + action + "] 发现不支持的字段 : " + field + " --> " + fieldType);
                continue;
            }

            if (ignoreSpecificField
                    && isMultiTenantObject
                    && field.getName().equals("tenantId")) {
                //多租户字段
                logger.info("*** " + entityClass + "[" + action + "] 忽略多租户字段 tenantId : " + field + " --> " + fieldType);
                continue;
            }

            if (ignoreSpecificField
                    && isOrganizedObject
                    && field.getName().equals("orgId")) {
                //多租户字段
                logger.info("*** " + entityClass + "[" + action + "] 忽略组织字段 orgId : " + field + " --> " + fieldType);
                continue;
            }

            boolean isCollection = fieldType.isArray() || Collection.class.isAssignableFrom(fieldType);

            Class subType = isCollection ? (fieldType.isArray() ? forField.getComponentType().resolve() : forField.resolveGeneric()) : null;

            FieldModel fieldModel = new FieldModel(entityClass)
                    .setSchemaDescUseConstRef(isSchemaDescUseConstRef());

            fieldModel.setField(field)
                    .addImport(InjectVar.class)
                    .addImport(InjectConsts.class);
            fieldModel.setName(field.getName());
            fieldModel.setLength(field.isAnnotationPresent(Column.class) ? field.getAnnotation(Column.class).length() : -1);

            fieldModel.setTypeName(fieldType.getSimpleName());

            fieldModel.setType(fieldType);
            fieldModel.setEleType(subType);

            fieldModel.setBaseType(isBaseType(forField, fieldType));

            fieldModel.setEnumType(fieldType.isEnum());

            fieldModel.setJpaEntity(fieldType.isAnnotationPresent(Entity.class));

            fieldModel.addImport(fieldType);

            if (subType != null) {
                fieldModel.addImport(subType);
            }

            if (fieldModel.isJpaEntity()) {

                fieldModel.getImports().add(getInfoClassImport(fieldType));
                fieldModel.setTypeName(fieldType.getSimpleName() + "Info");

            }

            setLazy(fieldModel);

            if (isCollection && subType != null) {

                String subTypeName = subType.getSimpleName();

                if (subType.isAnnotationPresent(Entity.class)) {
                    subTypeName = subTypeName + "Info";
                    fieldModel.getImports().add(getInfoClassImport(subType));
                    fieldModel.setLazy(true);
                    fieldModel.setBaseType(false);
                } else {
                    fieldModel.addImport(subType);
                    fieldModel.setBaseType(isBaseType(forField, subType));
                }

                fieldModel.setTypeName(fieldType.isArray() ? subTypeName + "[]" : fieldType.getSimpleName() + "<" + subTypeName + ">");
            }

            //是否乐观锁字段
            fieldModel.setOptimisticLock(field.isAnnotationPresent(Version.class));

            if (field.isAnnotationPresent(Schema.class)) {
                Schema schema = field.getAnnotation(Schema.class);

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

            fieldModel.setPk(field.isAnnotationPresent(Id.class));

            fieldModel.setNotUpdate(fieldModel.isPk() || notUpdateNames.contains(fieldModel.getName()) || fieldModel.isJpaEntity());
            if (fieldModel.isPk()) {
                fieldModel.setRequired(true);
                fieldModel.setAutoGenValue(field.isAnnotationPresent(GeneratedValue.class)
                        && !field.getAnnotation(GeneratedValue.class).strategy().equals(GenerationType.AUTO));
            } else {
                fieldModel.setUk(field.isAnnotationPresent(Column.class) && field.getAnnotation(Column.class).unique());
                fieldModel.setRequired(field.isAnnotationPresent(Column.class) && !field.getAnnotation(Column.class).nullable());
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
            ArrayList<String> annotations = new ArrayList<>();

            if (fieldModel.isRequired()) {
                annotations.add(CharSequence.class.isAssignableFrom(fieldType) ? "@NotBlank" : "@NotNull");
            }

            Consumer<List<Class<? extends Annotation>>> addAnnotation =
                    classes -> classes.stream().filter(Objects::nonNull)
                            //.filter(cls -> CharSequence.class.isAssignableFrom(fieldType))
                            .filter(field::isAnnotationPresent)
                            .forEachOrdered(
                                    annotationClass -> {

                                        InjectVar injectVar = field.getAnnotation(InjectVar.class);

                                        String domainStr = "";

                                        String[] domain = injectVar.domain();

                                        if (domain == null || domain.length == 0) {
                                        } else if (domain.length == 1) {
                                            if (!"default".equals(domain[0])) {
                                                domainStr = "domain = \"" + domain[0] + "\"";
                                            }
                                        } else {
                                            //加上挂号
                                            for (int i = 0; i < domain.length; i++) {
                                                domain[i] = "\"" + domain[i] + "\"";
                                            }
                                            domainStr = "domain = {" + String.join(",", domain) + "}";
                                        }

                                        if (!BeanUtils.isSimpleValueType(injectVar.expectBaseType())) {

                                            //  String domain = injectVar.domain().equals("default") ? "" : "domain = \"" + injectVar.domain() + "\"";

                                            String params = "";

                                            if ("evt".equalsIgnoreCase(action)) {
                                                fieldModel.addImport(fieldType);
                                                params = String.format(" expectBaseType = %s.class, ", fieldType.getSimpleName());
                                            }

                                            if (GenericConverter.class != injectVar.converter()) {
                                                fieldModel.addImport(injectVar.converter());

                                                String simpleName = injectVar.converter().getSimpleName();

                                                if (StringUtils.hasText(domainStr)) {
                                                    domainStr = domainStr + ",";
                                                }
                                                annotations.add("@" + annotationClass.getSimpleName() + String.format("(%s %s converter = %s.class, isRequired = \"false\")", domainStr, params, simpleName));
                                            } else if (!BeanUtils.isSimpleValueType(fieldType) && !"info".equalsIgnoreCase(action)) {
                                                if (StringUtils.hasText(domainStr)) {
                                                    domainStr = String.format("(%s)", domainStr);
                                                }
                                                annotations.add("@" + annotationClass.getSimpleName() + domainStr);
                                            }
                                        }

                                        //默认类型
                                        if (injectVar.expectBaseType() != Void.class
                                                && (PatternMatchUtils.simpleMatch(injectVar.domain(), "dao") || injectVar.expectBaseType() != Object.class)) {
                                            //转换数据类型
                                            fieldModel.addImport(injectVar.expectBaseType());

                                            for (Class<?> aType : injectVar.expectGenericTypes()) {
                                                fieldModel.addImport(aType);
                                            }

                                            fieldModel.typeName = injectVar.expectBaseType().getSimpleName();

                                            String sub = Arrays.stream(injectVar.expectGenericTypes()).map(Class::getSimpleName).collect(Collectors.joining(","));

                                            if (StringUtils.hasText(sub)) {
                                                fieldModel.typeName += "<" + sub + ">";
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

            if (fieldModel.getType().equals(String.class)
                    && fieldModel.getLength() != -1
                    && !fieldModel.getName().endsWith("Body")) {
                boolean isLob = field.isAnnotationPresent(Lob.class);
                if (isLob) {
                    //fieldModel.setLength(4000);
                    fieldModel.setTestValue("\"这是长文本正文\"");
                }
                if (fieldModel.getLength() != 255) {
                    annotations.add("@Size(max = " + fieldModel.getLength() + ")");
                    fieldModel.setTestValue("\"这是文本" + fieldModel.getLength() + "\"");
                }
            }

            //默认处理密码字段
            if (field.isAnnotationPresent(JsonIgnore.class)) {
                fieldModel.addAnnotation(field.getAnnotation(JsonIgnore.class));
            } else if (Stream.of("password", "passwd", "pwd")
                    .anyMatch(txt -> field.getName().toLowerCase().endsWith(txt))) {
                fieldModel.addAnnotation(JsonIgnore.class);
            }

            if (field.isAnnotationPresent(JsonIgnoreProperties.class)) {
                fieldModel.addAnnotation(field.getAnnotation(JsonIgnoreProperties.class));
            }

            if (field.isAnnotationPresent(Update.class)) {
                Update update = field.getAnnotation(Update.class);

                //增量更新
                if(update.incrementMode()){

                }
            }

            //加入所有的校验规则
            fieldModel.addAnnotations(
                    an -> an.annotationType().getPackage().equals(NotBlank.class.getPackage())
                    , field.getAnnotations());
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

            String fieldValue = getFieldValue(field.getName(), obj);

            if (fieldValue != null) {
                fieldModel.setHasDefValue(true);
                fieldModel.setTestValue(fieldValue);
            }

            if (fieldModel.getTestValue() == null) {
                if (fieldModel.getName().equals("sn")) {
                    String sn = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10).toUpperCase();
                    fieldModel.setTestValue("\"" + sn + "\"");
                } else if (fieldModel.getName().equals("areaId")) {
                    fieldModel.setTestValue("\"1\"");
                } else if (fieldModel.isEnumType()) {
                    fieldModel.setTestValue(fieldType.getSimpleName() + "." + getEnumByVal(fieldType, 0).name());
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
                } else if (fieldModel.getType().equals(Date.class)) {
                    fieldModel.setTestValue("new Date()");
                } else {
                    // fieldModel.setTestValue("null");
                }
            }

            fieldModelList.add(fieldModel);

        }
        return fieldModelList;
    }

    private static boolean isBaseType(ResolvableType parent, Class type) {

        return ClassUtils.isPrimitiveOrWrapper(type)
                || CharSequence.class.isAssignableFrom(type)
                || type.isEnum()
                || Number.class.isAssignableFrom(type)
                || Date.class.isAssignableFrom(type)
                || (type.isArray() && ClassUtils.isPrimitiveWrapper(parent.getComponentType().resolve()))
                || BeanUtils.isSimpleProperty(type)
                ;

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
