package com.levin.commons.dao.codegen.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;


/**
 * http协议明确规定，put、get、delete请求都是具有幂等性的，而post为非幂等性的。
 * 所以一般插入新数据的时候使用post方法，更新数据库时用put方法
 *
 */
@Mojo(name = "run-groovy", requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GroovyMojo extends BaseMojo {

    /**
     * 为 groovy运行环境增加的类路径
     * <p>
     * jar包,zip包，或是目录
     */
    @Parameter
    private String[] classpaths = {};

    /**
     * 需要加载 groovy 文件集
     */
    @Parameter
    private String[] groovyFiles = {};

    /**
     * 需要加载并且合并到txtScripts的groovy 文件
     */
    @Parameter
    private String[] contentMergeFiles = {};

    /**
     * 要执行的groovy脚本文本
     */
    @Parameter
    private String[] txtScripts = {};


    /**
     * 是否生成临时的 groovy 文件
     */
    @Parameter
    private boolean genTmpFile = true;


    @Override
    public void executeMojo() throws MojoExecutionException, MojoFailureException {
        try {
            Utils.runScript(this, "" + this.mavenProject.getGroupId() + "_" + this.mavenProject.getArtifactId(), genTmpFile, null, vars, classpaths, groovyFiles, contentMergeFiles, txtScripts);
        } catch (Exception e) {
            getLog().error("执行脚本错误：" + e.getMessage(), e);
            throw new MojoFailureException("run groovy error", e);
        }
    }

}
