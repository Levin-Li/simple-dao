package com.levin.commons.plugins;


import com.levin.commons.service.support.ContextHolder;
import com.levin.commons.utils.ClassUtils;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.*;
import org.codehaus.plexus.util.AbstractScanner;
import org.eclipse.aether.graph.DependencyNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
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
    protected ProjectBuildingHelper projectBuildingHelper;

    @Component
    ProjectDependenciesResolver dependenciesResolver;

    /**
     * 为 true 跳过插件的执行
     */
    @Parameter
    protected boolean skip = false;

    /**
     * 插件仅在构建命令启动的模块中执行
     */
    @Parameter
    protected boolean onlyExecutionRoot = true;

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
    protected boolean independentPluginClassLoader = true;

    /**
     * 插件类加载器，需要加载的Scopes
     */
    @Parameter
    protected String[] dependenciesScopes = {};

    /**
     * 插件类加载器，需要加载的Scopes
     */
    @Parameter
    protected String[] includeArtifacts = {};


    protected final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * 线程变量
     */
    protected static final ContextHolder<String, Object> threadContext = ContextHolder.buildThreadContext(true);


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
    @SneakyThrows
    public synchronized Class loadClass(String className) {

        URLClassLoader classLoader = getClassLoader();

        return (classLoader == null) ? getClass().getClassLoader().loadClass(className)
                : classLoader.loadClass(className);
    }

    public URLClassLoader getClassLoader(String... filePaths) {
        return getClassLoader(Arrays.asList(filePaths));
    }

    /**
     * @param filePaths
     * @return
     */
    public synchronized URLClassLoader getClassLoader(List<String> filePaths) {

        List<URL> urlList = new ArrayList<>(50);

        if (pluginClassLoader == null) {

            try {
                urlList.add(new File(mavenProject.getBuild().getOutputDirectory()).toURI().toURL());
            } catch (MalformedURLException e) {
            }

            //首次默认加入类路径
            getMavenProjectClasspaths(urlList);
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
            //运行多次继承
            ClassLoader parent = (pluginClassLoader != null || independentPluginClassLoader) ? pluginClassLoader : getClass().getClassLoader();

            pluginClassLoader = new URLClassLoader(urlList.toArray(new URL[urlList.size()]), parent);

            logger.info(" **** 创建新的类加载器 ，类路径的依赖包: " + urlList);
        }

        return pluginClassLoader;
    }


    static String toString(Artifact d) {
        return d.getGroupId() + ":" + d.getArtifactId()
                + (StringUtils.hasText(d.getClassifier()) ? (":" + d.getClassifier()) : "");
    }

    static String toString(Dependency d) {
        return d.getGroupId() + ":" + d.getArtifactId()
                + (StringUtils.hasText(d.getClassifier()) ? (":" + d.getClassifier()) : "");
    }


    static String toString(org.eclipse.aether.artifact.Artifact d) {
        return d.getGroupId() + ":" + d.getArtifactId()
                + (StringUtils.hasText(d.getClassifier()) ? (":" + d.getClassifier()) : "");
    }

    /**
     * 递归获取项目的依赖库
     *
     * @param dependenciesMap
     * @param node
     * @return
     */
    MultiValueMap<String, org.eclipse.aether.artifact.Artifact> getDependencies(MultiValueMap<String, org.eclipse.aether.artifact.Artifact> dependenciesMap, DependencyNode node) {

        synchronized (this) {
            if (dependenciesMap == null) {
                dependenciesMap = new LinkedMultiValueMap();
            }
        }

        //递归获取依赖库
        if (node != null) {
            for (DependencyNode child : node.getChildren()) {
                getDependencies(dependenciesMap, child);
            }
            dependenciesMap.add(toString(node.getArtifact()), node.getArtifact());
        }

        return dependenciesMap;
    }


    @SneakyThrows
    public MultiValueMap<String, org.eclipse.aether.artifact.Artifact> getDependencies() {

        DependencyResolutionResult resolutionResult
                = dependenciesResolver.resolve(new DefaultDependencyResolutionRequest(mavenSession.getCurrentProject(), mavenSession.getRepositorySession()));

        return getDependencies(null, resolutionResult.getDependencyGraph());
    }


    @SneakyThrows
    protected List<URL> getMavenProjectClasspaths(List<URL> urlList) {

        MultiValueMap<String, org.eclipse.aether.artifact.Artifact> multiValueMap = getDependencies();

        getLog().info("*** dependenciesResolver getDependencies: " + multiValueMap);
        getLog().info("***         mavenProject getDependencies: " + mavenProject.getDependencies());

/*

        Set<Artifact> artifactSet = new HashSet<>();

        artifactSet.addAll(mavenProject.getArtifacts());

        if (artifactSet.isEmpty()) {
            artifactSet.addAll(mavenProject.getArtifactMap().values());
            getLog().info("classpath use getArtifactMap :" + mavenProject.getArtifactMap());
        }

        if (artifactSet.isEmpty()) {
            artifactSet.addAll(mavenProject.getDependencyArtifacts());
            getLog().info("classpath use getDependencyArtifacts :" + artifactSet);
        }

        if (artifactSet.isEmpty()) {

            artifactSet.addAll(mavenProject.getRuntimeArtifacts());
            artifactSet.addAll(mavenProject.getSystemArtifacts());
            artifactSet.addAll(mavenProject.getCompileArtifacts());
            artifactSet.addAll(mavenProject.getExtensionArtifacts());
            artifactSet.addAll(mavenProject.getAttachedArtifacts());

            getLog().info("classpath use getCompileArtifacts getSystemArtifacts getRuntimeArtifacts :" + artifactSet);
        }
*/

/*
        List<String> desList = mavenProject.getDependencies()
                .parallelStream().map(BaseMojo::toString)
                .collect(Collectors.toList());

        Map<String, Artifact> artifactMap = new LinkedHashMap<>();

        for (Artifact artifact : artifactSet) {

            String artifactKey = toString(artifact);

            boolean isDirectDepend = desList.contains(artifactKey);

            //如果不是依赖库，也不是
            if (!isDirectDepend
                    && !Arrays.stream(includeArtifacts)
                    .filter(StringUtils::hasText)
                    .anyMatch(str -> antPathMatcher.match(str, artifactKey))) {
                getLog().info("*** 忽略[" + artifact.getScope() + "] " + artifact);
                continue;
            }

            if (!artifactMap.containsKey(artifactKey)) {
                if (artifact.getFile() != null) {
                    try {
                        urlList.add(artifact.getFile().toURI().toURL());
                        artifactMap.put(artifactKey, artifact);
                    } catch (MalformedURLException e) {
                        logger.warn(" ****  " + mavenProject.getArtifact() + " 依赖包文件不可用 --> " + artifact + " --> path: " + artifact.getFile());
                    }
                } else {
                    logger.warn(" ****  " + mavenProject.getArtifact() + " 依赖包不可用 --> " + artifact);
                }
            }
        }*/


        for (String key : multiValueMap.keySet()) {

            List<org.eclipse.aether.artifact.Artifact> artifacts = multiValueMap.get(key);

            // Artifact artifact = artifactMap.get(key);

            if (artifacts == null || artifacts.isEmpty()) {
                continue;
            } else if (artifacts.size() > 1) {
                getLog().warn("构件版本冲突：" + key + " --> " + artifacts);
            }

            try {
                urlList.add(artifacts.get(0).getFile().toURI().toURL());
            } catch (Throwable e) {
                getLog().error("加入 " + key + " 失败" + e.getMessage());
            }

        }

        if (urlList.isEmpty()) {
            logger.warn("*** 当前模块没有获取到依赖列表。");
        }

        return urlList;
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


        initVars();

        String artifactId = mavenProject.getArtifactId();
        String packageType = mavenProject.getPackaging();

        if (!isAllow(artifactId, allowArtifactIds)) {
            getLog().info(info + "not allow project artifactId:" + artifactId + ",skip");
            return;
        }

        if (!isAllow(packageType, allowPackageTypes)) {
            getLog().info(info + "not allow project type:" + packageType + ",skip");
            return;
        }

        if (!isAllow(keyword, allowKeywords)) {
            getLog().info(info + "not allow keyword :" + keyword + ",skip");
            return;
        }


        if (allowScripts != null) {
            for (String allowScript : allowScripts) {
                if (!Boolean.TRUE.equals(getScript(allowScript).run())) {
                    getLog().info(info + "not allow script :[" + allowScript + "],skip");
                    return;
                }
            }
        }


        try {

            Thread.currentThread().setContextClassLoader(getClassLoader());

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
                || !StringUtils.hasText(allowName)) {
            return true;
        }

        return Arrays.asList(allowList).contains(allowName);
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
        return " [" + mavenProject.getArtifactId() + "] ";
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

    /**
     * 获取文件的相对路径
     *
     * @param currentFilePath 当前文件的路径，不包含文件名
     * @param targetFilePath  要导入的目标文件的路径，不包含文件名
     * @return
     */
    public static StringBuilder getRelativePath(String currentFilePath, String targetFilePath) {

        Object[] currentFilePaths = Arrays.stream(currentFilePath.split("[\\/]"))
                .filter(t -> !".".equals(t))
                .filter(StringUtils::hasText)
                .toArray();

        Object[] targetFilePaths = Arrays.stream(targetFilePath.split("[\\/]"))
                .filter(t -> !".".equals(t))
                .filter(StringUtils::hasText)
                .toArray();

        StringBuilder result = new StringBuilder();

        int idx = 0;
        while (idx < currentFilePaths.length
                && idx < targetFilePaths.length
                && targetFilePaths[idx].equals(currentFilePaths[idx])) {
            idx++;
        }

        int temp = idx;
        while (temp++ < currentFilePaths.length) {
            result.append("..").append(File.separator);
        }

        while (idx < targetFilePaths.length) {
            result.append(targetFilePaths[idx++]).append(File.separator);
        }

        System.out.println("[" + targetFilePath + "] --> [" + currentFilePath + "] --> [" + result + "]");

        return result;
    }


    protected boolean hasContent(String txt) {
        return txt != null && txt.trim().length() > 0;
    }


    abstract protected void executeMojo() throws Exception;


}
