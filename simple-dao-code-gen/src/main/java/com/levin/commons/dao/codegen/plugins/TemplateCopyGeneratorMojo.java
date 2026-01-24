package com.levin.commons.dao.codegen.plugins;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import com.levin.commons.plugins.BaseMojo;
import com.levin.commons.utils.MapUtils;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;
import static org.springframework.util.StringUtils.hasText;


/**
 * 生成项目模板
 */
@Mojo(name = "copy-template", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class TemplateCopyGeneratorMojo extends BaseMojo {

    /**
     * 模块包名
     * 如果没有配置，则自动获取 pom 文件中定义的 GroupId
     */
    @Parameter
    private String modulePackageName = "";

    /**
     * 要拷贝的目录, key为源目录，value为目标目录
     */
    @Parameter
    protected Map<String, String> copyDirMap = new LinkedHashMap<>();


    /*
     *是否仅仅对根模块有效
     */
    @Parameter
    protected boolean onlyRootModule = true;

    {
        independentPluginClassLoader = false;
    }

    @Override
    public void executeMojo() throws Exception {

        final String resTemplateRootDir = "simple.dao/codegen/template/";

        if (copyDirMap == null) {
            copyDirMap = new HashMap<>();
        }

        logger.info("可复制的类资源根目录:", resTemplateRootDir);

        if (copyDirMap.isEmpty()) {

            logger.warn("插件没有配置要拷贝的目录, 配置参数(Map<String, String>): copyDirMap ,  key为源目录，value为目标目录(可不填) ");
            logger.warn("默认拷贝 docker-compose 资源");

            copyDirMap.put("docker-compose", "");
        }

        boolean isRootModule = mavenProject.getParent() != null;

        if (onlyRootModule && isRootModule) {
            logger.warn("当前模块[{}]非根模块，忽略处理", mavenProject.getArtifactId());
            return;
        }

        final File basedir = mavenProject.getBasedir();

        boolean isPomModule = "pom".equalsIgnoreCase(mavenProject.getPackaging());

        if (!hasText(this.modulePackageName)) {
            modulePackageName = mavenProject.getGroupId();
        }


        Map<Object, Object> mavenProperties = new HashMap<>();

        mavenProperties.putAll(mavenSession.getSystemProperties());
        mavenProperties.putAll(mavenSession.getUserProperties());

        mavenProperties.putAll(mavenProject.getProperties());
        //拷贝 POM 文件

        MapUtils.Builder<String, Object> mapBuilder = MapUtils.putFirst("__mavenProject", mavenProject);

        //
        mavenProperties.forEach((k, v) -> mapBuilder.put(k.toString(), v));

        mapBuilder
                .put("modulePackageName", modulePackageName)
                .put("now", new Date().toString());

        mapBuilder.put("mavenProject", mavenProject);
        mapBuilder.put("mavenSession", mavenSession);
        mapBuilder.put("artifactId", mavenSession.getCurrentProject().getArtifactId());
        mapBuilder.put("basedir", mavenProject.getBasedir());
        mapBuilder.put("projectName", mavenProject.getName());
        mapBuilder.put("projectDesc", mavenProject.getDescription());

        //递归必须用 **，而非 *；
        //第三方 Jar 包资源用 classpath*:；
        //中文 / 二进制文件需用字节流读取。
        //递归扫描：classpath:目录/**/文件名规则；
        //跨 Jar 包扫描：classpath*:目录/**/文件名规则；
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        for (Map.Entry<String, String> entry : copyDirMap.entrySet()) {

            String srcDir = entry.getKey();
            String targetDir = entry.getValue();
            targetDir = StrUtil.blankToDefault(targetDir, srcDir);

            File targetFile = new File(basedir, targetDir);

            logger.info("开始拷贝目录 {} --> {} ...", srcDir, targetFile);

            for (Resource resource : resolver.getResources(CLASSPATH_ALL_URL_PREFIX + resTemplateRootDir + srcDir + "/**")) {

                String path = null;

                if (resource instanceof ClassPathResource) {
                    path = ((ClassPathResource) resource).getPath();
                } else if (resource.isFile()) {
                    path = resource.getFile().getAbsolutePath();
                } else {
                    path = resource.getURI().toString();
                }

                if (StrUtil.isBlank(path) || !path.contains(resTemplateRootDir)) {
                    logger.warn("{} 非模板位置[{}]文件, 忽略处理...", path, resTemplateRootDir);
                    continue;
                }

                path = path.substring(path.lastIndexOf(resTemplateRootDir) + resTemplateRootDir.length());

                File dest = new File(basedir, path);

                dest.getParentFile().mkdirs();

                if (!dest.exists()) {
                    if (resource.isReadable()) {
                        logger.info("复制资源 [{}] -> [{}]...", path, dest.getAbsolutePath());
                        FileUtil.writeFromStream(resource.getInputStream(), dest);
                    } else {
                        logger.info("创建目录 {}...", dest.getAbsolutePath());
                        dest.mkdirs();
                    }
                } else {
                    logger.warn("{} 已存在, 忽略复制...", dest.getAbsolutePath());
                }


            }
        }

    }


}
