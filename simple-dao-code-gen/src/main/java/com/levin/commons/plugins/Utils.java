package com.levin.commons.plugins;

import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

@Slf4j
public abstract class Utils {

    /**
     * 获取名称
     *
     * @param moduleName
     * @return
     */
    public static String getModuleName(String moduleName) {

        //自动去除 root 或是 parent
        if (moduleName.endsWith("-root")
                || moduleName.endsWith("-parent")) {
            moduleName = moduleName.substring(0, moduleName.lastIndexOf("-"));
        }

        return moduleName;
    }

    /**
     * 替换并写入文件
     *
     * @param templateRes
     * @param target
     * @param varMaps
     * @throws IOException
     */
    public static void copyAndReplace(String prefix, boolean overwrite, String templateRes, File target, Map<String, String>... varMaps) throws IOException {

        //String prefix = mavenProject.getBasedir().getCanonicalPath();

        String path = target.getCanonicalPath();

        if (path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }

        if (!overwrite && target.exists()) {
            log.warn("*** 文件[" + path + "]已经存在，忽略代码生成。");
            return;
        }

        log.info("*** 开始生成 [" + path + "] 文件，替换变量：" + Arrays.asList(varMaps));

        ClassLoader classLoader = Utils.class.getClassLoader();

       // templateRes= templateRes.replaceAll("")

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
     * 执行 groovy 脚本
     *
     * @param mojo
     * @param name
     * @param isGenFile
     * @param shell
     * @param vars
     * @param classpaths
     * @param groovyFiles
     * @param contentMergeFiles
     * @param txtScripts
     * @return
     * @throws Exception
     */
    public static GroovyShell runScript(BaseMojo mojo, String name, boolean isGenFile, GroovyShell shell, Map<String, Object> vars, String[] classpaths, String[] groovyFiles, String[] contentMergeFiles, String... txtScripts) throws Exception {

        if (shell == null)
            shell = new GroovyShell();


        if (vars != null) {
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                shell.setVariable(entry.getKey(), entry.getValue());
            }
        }


        if (classpaths != null) {
            for (String classpath : classpaths) {
                if (!StringUtils.hasText(classpath)) {
                    continue;
                }
                shell.getClassLoader().addClasspath(classpath);
            }
        }

        if (groovyFiles != null) {
            for (String groovyFile : groovyFiles) {
                String[] content = loadFile(groovyFile);
                shell.evaluate(content[0], content[1]);
            }
        }


        StringBuilder builder = new StringBuilder();

        if (contentMergeFiles != null) {
            for (String contentFile : contentMergeFiles) {
                try {
                    builder.append(loadFile(contentFile)[0]);
                } catch (FileNotFoundException e) {
                    mojo.getLog().warn("***  脚本文件[" + contentFile + "]不存在");
                }
            }
        }

        if (txtScripts != null) {
            int n = 0;
            for (String script : txtScripts) {
                //shell.evaluate(script, "fileNameConvertScriptObj[" + n++ + "]", "mem://maven/fileNameConvertScriptObj/groovy/");

                if (script != null) {
                    builder.append("\n").append(script);
                }
            }
        }

        if (builder.length() > 0) {

            String script = builder.toString();

            String fn = "target/.tmp-merge-files_" + name + ".groovy";

            if (isGenFile) {
                try {
                    FileUtils.write(new File(fn), script, "utf-8");
                } catch (Exception e) {
                    mojo.getLog().warn(" 无法保存临时文件" + fn, e);
                }
            }

            shell.evaluate(script, fn);
        }

        return shell;

    }

    private static String[] loadFile(String groovyFile) throws IOException {

        String encoding = "utf-8";

        if (!StringUtils.hasText(groovyFile)) {
            return new String[]{"", ""};
        }

        int indexOf = groovyFile.indexOf('|');

        if (indexOf != -1) {
            encoding = groovyFile.substring(0, indexOf);
            groovyFile = groovyFile.substring(indexOf + 1);
        }

        byte[] content = IOUtils.toByteArray(ResourceUtils.getURL(groovyFile));

        return new String[]{new String(content, encoding), groovyFile};
    }

}
