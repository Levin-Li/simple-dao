package com.levin.commons.plugins.simple;

import com.levin.commons.plugins.Utils;
import com.trilead.ssh2.Connection;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.util.LinkedHashMap;


@Mojo(name = "run-ssh-groovy",requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class GroovySShMojo extends  AbstractSshMojo {


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
    protected void execute(Connection connection) throws Exception {
        try {
            initGroovyVars(connection);
            Utils.runScript(this,"" + this. mavenProject.getGroupId() + "_" + this.mavenProject.getArtifactId(),genTmpFile, null, vars, classpaths, groovyFiles, contentMergeFiles, txtScripts);
        } catch (Exception e) {
            getLog().error("执行脚本错误：" + e.getMessage(), e);
            throw new MojoFailureException("run groovy error", e);
        }
    }

    protected void initGroovyVars(Connection connection) throws Exception {

        if (!(vars instanceof LinkedHashMap)) {
            vars = new LinkedHashMap<>(vars);
        }

        vars.put("_session", connection);

        vars.put("_scp", connection.createSCPClient());

        vars.put("_mojo", this);

    }

}
