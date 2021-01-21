package com.levin.commons.dao.codegen.plugins;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;


@Mojo(name = "run-groovy",requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GroovyMojo extends BaseMojo {

    /**
     * 类路径
     * <p>
     * jar包,zip包，或是目录
     */
    @Parameter
    private String[] classpaths = {};

    @Parameter
    private String[] groovyFiles = {};

    /**
     * 按顺序把多个文件的内容合并为一个文件
     */
    @Parameter
    private String[] contentMergeFiles = {};

    /**
     * 要执行的groovy脚本文本
     */
    @Parameter
    private String[] txtScripts = {};


    @Parameter
    private boolean genTmpFile = true;


    @Override
    public void executeMojo() throws MojoExecutionException, MojoFailureException {
        try {
            Utils.runScript(this,"" + this.groupId + "_" + this.artifactId, genTmpFile, null, vars, classpaths, groovyFiles, contentMergeFiles, txtScripts);
        } catch (Exception e) {
            getLog().error("执行脚本错误：" + e.getMessage(), e);
            throw new MojoFailureException("run groovy error", e);
        }
    }

}
