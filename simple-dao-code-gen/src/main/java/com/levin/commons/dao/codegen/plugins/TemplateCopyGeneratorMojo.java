package com.levin.commons.dao.codegen.plugins;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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

        if (copyDirMap == null) {
            return;
        }

        if (copyDirMap.isEmpty()) {
            copyDirMap.put("docker-compose", "");
        }

        boolean isRootModule = mavenProject.getParent() != null;

        if (onlyRootModule && isRootModule) {
            return;
        }

        final File basedir = mavenProject.getBasedir();

        boolean isPomModule = "pom".equalsIgnoreCase(mavenProject.getPackaging());

        if (!hasText(this.modulePackageName)) {
            modulePackageName = mavenProject.getGroupId();
        }

        String resTemplateRootDir = "simple.dao/codegen/template/";


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

            for (Resource resource : resolver.getResources(ResourceUtils.CLASSPATH_URL_PREFIX + resTemplateRootDir + srcDir + "/**")) {

                if (resource instanceof ClassPathResource) {
                    String path = ((ClassPathResource) resource).getPath();
                    path = path.substring(resTemplateRootDir.length());
                    FileUtil.writeFromStream(resource.getInputStream(), new File(targetFile, path));
                } else {
                    logger.warn("{} 非类路径资源, 忽略处理", resource.getDescription());
                }
            }
        }

    }


}
