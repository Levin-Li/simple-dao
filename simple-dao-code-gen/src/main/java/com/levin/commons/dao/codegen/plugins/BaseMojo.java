package com.levin.commons.dao.codegen.plugins;


import com.levin.commons.service.support.ContextHolder;
import com.levin.commons.utils.ClassUtils;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.dependencies.resolve.DependencyResolver;
import org.apache.maven.shared.transfer.repository.RepositoryManager;
import org.codehaus.plexus.util.AbstractScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 可以用 _project.xxxx 来访问插件的属性
 */
public abstract class BaseMojo extends AbstractMojo {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject mavenProject;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    protected MavenSession mavenSession;

    /**
     * Maven ProjectHelper
     */
    @Component
    protected MavenProjectHelper projectHelper;

    @Component
    private ArtifactResolver artifactResolver;

    @Component
    private DependencyResolver dependencyResolver;

    @Component
    private RepositoryManager repositoryManager;

    /**
     * 为 true 跳过插件的执行
     */
    @Parameter
    protected boolean skip = false;

    /**
     * 插件仅在构建命令启动的模块中执行
     */
    @Parameter
    private boolean onlyExecutionRoot = true;

    /**
     * 是否打印异常
     */
    @Parameter
    protected boolean isPrintException = false;

    /**
     * 自定义变量
     */
    @Parameter
    protected Map<String, Object> vars;

    /**
     * 是否把所有的参数直接合并到脚本上下文中
     * 默认是不并入
     */
    @Parameter
    protected boolean mergeParamsIntoScriptContext = false;

    /**
     * 允许执行的匹配脚本
     * <p>
     * 如果脚本返回 false，则不允许执行
     * 如果没有，则默认允许所有
     */
    @Parameter
    protected String[] allowScripts = {};


    /**
     * 允许执行的关键字
     * 如果没有，则默认允许所有
     */
    @Parameter
    protected String keyword;


    /**
     * 允许执行的关键字
     * 如果没有，则默认允许所有
     */
    @Parameter
    protected String[] allowKeywords = {};

    /**
     * 允许执行的构件ID清单
     * <p>
     * 如果没有，则默认允许所有
     */
    @Parameter
    protected String[] allowArtifactIds = {};

    /**
     * 允许执行的项目packaging类型
     * <p>
     * 空为没有限制
     */
    @Parameter
    protected String[] allowPackageTypes = {};

    /**
     * 插件的类加载器是否是独立
     */
    @Parameter
    protected boolean independentPluginClassLoader = false;

    /**
     * 线程变量
     */
    protected static final ContextHolder<String, Object> threadContext = ContextHolder.buildThreadContext(true, false);
    protected static final ContextHolder<String, Object> inheritableThreadContext = ContextHolder.buildThreadContext(true, true);

    private final transient Map<String, Script> cachedScripts = new ConcurrentHashMap<>();

    /**
     * 类加载器
     */
    private URLClassLoader pluginClassLoader;

    /**
     * 插件隔离的类加载器
     *
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public synchronized Class loadClass(String className) throws ClassNotFoundException {

        addClasspath();

        return (pluginClassLoader == null) ? getClass().getClassLoader().loadClass(className)
                : pluginClassLoader.loadClass(className);
    }

    public ClassLoader getClassLoader() {
        addClasspath();
        return pluginClassLoader;
    }


    public synchronized void addClasspath(String... filePaths) {
        addClasspath(Arrays.asList(filePaths));
    }

    public synchronized void addClasspath(List<String> filePaths) {

        List<URL> urlList = new ArrayList<>(50);

        if (pluginClassLoader == null) {

            try {
                urlList.add(new File(mavenProject.getBuild().getOutputDirectory()).toURI().toURL());
            } catch (MalformedURLException e) {

            }

            Map<String, Artifact> artifactMap = new LinkedHashMap<>();

            for (Artifact artifact : mavenProject.getArtifacts()) {

                String key = artifact.getGroupId()
                        + ":" + artifact.getArtifactId()
                        + (artifact.hasClassifier() ? ":" + artifact.getClassifier() : "");

                if (artifactMap.containsKey(key)) {
                    logger.error(" ****  " + mavenProject.getArtifact() + " 的依赖中包含冲突的构件：" + artifact + " <---> " + artifactMap.get(key));
                } else {
                    artifactMap.put(key, artifact);
                }

                if (artifact.isResolved() && artifact.getFile() != null) {

                    try {
                        urlList.add(artifact.getFile().toURI().toURL());
                    } catch (MalformedURLException e) {
                        logger.warn(" ****  " + mavenProject.getArtifact() + " 依赖包不可用 --> " + artifact + " --> path: " + artifact.getFile());
                    }

                } else {
                    logger.warn(" ****  " + mavenProject.getArtifact() + " 依赖包不可用 --> " + artifact);
                }
            }

        }


        if (filePaths != null) {

            for (String filePath : filePaths) {
                try {
                    urlList.add(new File(filePath).toURI().toURL());
                } catch (MalformedURLException e) {
                    logger.warn(" ****  " + mavenProject.getArtifact() + " 添加的路径不可用 --> " + filePath);
                }
            }
        }

        if (urlList.size() > 0) {

            //是否要独立的类加载器
            ClassLoader parent = (pluginClassLoader != null || independentPluginClassLoader) ? pluginClassLoader : getClass().getClassLoader();

            pluginClassLoader = new URLClassLoader(urlList.toArray(new URL[urlList.size()]), parent);

            logger.info(" **** 本次加载到类路径的依赖包: " + urlList);
        }

    }

    protected final String getInvokeMethodName() {
        return getInvokeMethodName(1);
    }

    protected final String getInvokeMethodName(int index) {
        return new Throwable().getStackTrace()[index].getMethodName();
    }

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {


//        接下来在mojo的execute()方法中，调用project的getCompileClasspathElements()方法即可得到该string数组。
//
//        将该数组中的字符串依次转换成URL数组，即可作为自定义的类加载器的构造方法中的参数，该类加载器可以达到实现在maven插件中动态加载目标项目类及第三方引用包的目的。

        // mavenProject.getCompileClasspathElements();
//        mavenProject.getRuntimeClasspathElements();
//        mavenProject.getTestClasspathElements();


        String info = getBaseInfo();

        if (skip) {
            getLog().warn(info + " skip ，you can config true by [skip] parameter.");
            return;
        }


        if (onlyExecutionRoot
                && !mavenProject.isExecutionRoot()) {

            getLog().warn(info + " 插件仅仅在命令启动的模块中[" + new File("").getAbsolutePath() + "]启用 ，可以配置插件参数[onlyExecutionRoot = false]禁用.");

            return;
        }

        getLog().info("Execute " + info + "...");

        initVars();

        String artifactId = mavenProject.getArtifactId();
        String packageType = mavenProject.getPackaging();

        if (!isAllow(artifactId, allowArtifactIds)) {
            getLog().info("not allow project artifactId:" + artifactId + ",skip");
            return;
        }

        if (!isAllow(packageType, allowPackageTypes)) {
            getLog().info("not allow project type:" + packageType + ",skip");
            return;
        }

        if (!isAllow(keyword, allowKeywords)) {
            getLog().info("not allow keyword :" + keyword + ",skip");
            return;
        }


        if (allowScripts != null) {
            for (String allowScript : allowScripts) {
                if (!Boolean.TRUE.equals(getScript(allowScript).run())) {
                    getLog().info("not allow script :[" + allowScript + "],skip");
                    return;
                }
            }
        }


        try {
            executeMojo();
        } catch (MojoExecutionException e) {
            printException(e);
            throw e;
        } catch (MojoFailureException e) {
            printException(e);
            throw e;
        } catch (Exception e) {
            printException(e);
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
        }

    }

    private void initVars() {

        Map<String, Field> fieldMap = ClassUtils.findFields(getClass());

        if (!(vars instanceof LinkedHashMap)) {
            vars = (vars != null) ? new LinkedHashMap<>(vars) : new LinkedHashMap<>();
        }


        Map projectParams = new LinkedHashMap<>();

        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
            try {

                Field field = entry.getValue();
                field.setAccessible(true);

                if (!Modifier.isTransient(field.getModifiers())
                        && !Modifier.isStatic(field.getModifiers())) {
                    projectParams.put(field.getName(), field.get(this));
                }
            } catch (Exception e) {
                logger.warn(getClass().getName(), "copyFields2Map", e);
            }
        }

        projectParams.remove("vars");

        vars.put("_project", projectParams);

        if (mergeParamsIntoScriptContext) {
            vars.putAll(projectParams);
        }

        vars.put("log", getLog());

        vars.put("_vars", vars);

        vars.put("_mojo", this);
        vars.put("_thisPlugin", this);

        //关键变量
        vars.put("project", mavenProject);

        vars.put("mavenProject", mavenProject);

        vars.put("mavenSession", mavenSession);

        getLog().info("*** " + getBaseInfo() + " 插件默认可用变量：" + vars.keySet());
        getLog().info("*** " + getBaseInfo() + " [_project]可用子变量：" + projectParams.keySet());

    }


    Script getScript(String scriptTxt, Map<String, Object>... ctxs) {

        if (!hasContent(scriptTxt)) {
            scriptTxt = "";
        }

        Script scriptObj = cachedScripts.get(scriptTxt);

        if (scriptObj != null) {
            return scriptObj;
        }

        scriptObj = new GroovyShell().parse(scriptTxt);

        if (vars != null) {
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                scriptObj.setProperty(entry.getKey(), entry.getValue());
            }
            // vars.remove("_vars");
        }

        if (ctxs != null) {
            for (Map<String, Object> ctx : ctxs) {

                if (ctx == null) {
                    continue;
                }
                for (Map.Entry<String, Object> entry : ctx.entrySet()) {
                    scriptObj.setProperty(entry.getKey(), entry.getValue());
                }
            }
        }
        //
        scriptObj.setProperty("_mojo", this);

        cachedScripts.put(scriptTxt, scriptObj);

        return scriptObj;
    }


    protected static boolean isAllow(String allowName, String... allowList) {

        if (allowList == null
                || allowList.length == 0
                || allowName == null
                || allowName.trim().length() == 0) {
            return true;
        }

        for (String allow : allowList) {
            if (allowName.equals(allow)) {
                return true;
            }
        }

        return false;
    }


    public boolean matchPath(String pattern, String path) {
        return AbstractScanner.match(pattern, path);
    }


    protected void printException(Exception e) {
        if (isPrintException) {
            logger.error(getBaseInfo(), e);
            getLog().error(getBaseInfo(), e);
        }
    }


    protected String getBaseInfo() {
        return "plugin " + getClass().getSimpleName() + "[" + mavenProject.getGroupId() + ":" + mavenProject.getArtifactId() + ":" + mavenProject.getVersion() + "(" + mavenProject.getBasedir() + ")]";
    }


    /**
     * 替换并写入文件
     *
     * @param templateRes
     * @param target
     * @param varMaps
     * @throws IOException
     */
    protected void copyAndReplace(boolean overwrite, String templateRes, File target, Map<String, String>... varMaps) throws IOException {

        String prefix = mavenProject.getBasedir().getCanonicalPath();

        String path = target.getCanonicalPath();

        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }

        if (!overwrite && target.exists()) {
            logger.warn("*** 文件[" + path + "]已经存在，忽略代码生成。");
            return;
        }

        logger.info("*** 开始生成 [" + path + "] 文件，替换变量：" + Arrays.asList(varMaps));

        ClassLoader classLoader = getClass().getClassLoader();

        while (templateRes.trim().startsWith("/")) {
            templateRes = templateRes.trim().substring(1);
        }

        String resText = IOUtils.resourceToString(templateRes, Charset.forName("utf-8"), classLoader);

        if (varMaps != null) {
            for (Map<String, String> varMap : varMaps) {
                if (varMap != null) {
                    for (Map.Entry<String, String> entry : varMap.entrySet()) {
                        resText = resText.replace("${" + entry.getKey().trim() + "}", entry.getValue());
                    }
                }
            }
        }

        target.getParentFile().mkdirs();

        FileUtils.write(target, resText, "utf-8");
    }


    /**
     * 获取本插件的包名
     *
     * @return
     * @throws IOException
     */
    protected Map<String, String> getPluginInfo(String prefix) throws IOException {

        String xmlContent = IOUtils.resourceToString("META-INF/maven/plugin.xml", Charset.forName("utf-8"), getClass().getClassLoader());

        if (!StringUtils.hasText(prefix)) {
            prefix = "";
        }

        Map<String, String> info = new LinkedHashMap<>();

        int indexOf = xmlContent.indexOf("</groupId>");

        if (indexOf != -1) {

            String groupId = "<groupId>";

            groupId = xmlContent.substring(xmlContent.indexOf(groupId) + groupId.length(), indexOf);

            info.put(prefix + "groupId", groupId);
        }

        /////////////////////////////////////////////////////////////

        indexOf = xmlContent.indexOf("</artifactId>");

        if (indexOf != -1) {

            String artifactId = "<artifactId>";
            artifactId = xmlContent.substring(xmlContent.indexOf(artifactId) + artifactId.length(), indexOf);

            info.put(prefix + "artifactId", artifactId);
        }

        /////////////////////////////////////////////////////////////

        indexOf = xmlContent.indexOf("</version>");

        if (indexOf != -1) {

            String version = "<version>";
            version = xmlContent.substring(xmlContent.indexOf(version) + version.length(), indexOf);

            info.put(prefix + "version", version);
        }
        ///////////////////////////////////////////////////////////////

        return info;
    }


    protected boolean hasContent(String txt) {
        return txt != null && txt.trim().length() > 0;
    }


    abstract protected void executeMojo() throws Exception;

}
