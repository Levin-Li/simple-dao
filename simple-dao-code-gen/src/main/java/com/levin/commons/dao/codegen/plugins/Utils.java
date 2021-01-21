package com.levin.commons.dao.codegen.plugins;

import com.trilead.ssh2.Connection;
import groovy.lang.GroovyShell;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.AbstractScanner;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public abstract class Utils {

    public static boolean isMatch(AntPathMatcher matcher, String path, String... patterns) {

        if (patterns == null
                || patterns.length == 0)
            return false;

        for (String pattern : patterns) {

            if (matcher == null && AbstractScanner.match(pattern, path)) {
                return true;
            }

            if (matcher != null && matcher.match(pattern, path)) {
                return true;
            }
        }

        return false;

    }


    public static GroovyShell runScript( BaseMojo mojo, String name, boolean isGenFile, GroovyShell shell, Map<String, Object> vars, String[] classpaths, String[] groovyFiles, String[] contentMergeFiles, String... txtScripts) throws Exception {

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

                if (script != null)
                    builder.append("\n").append(script);
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


    /**
     * @param host
     * @param port
     * @param username
     * @param password
     * @param pemPrivateKey
     * @return
     * @throws IOException
     */
    public static Connection connect(String host, int port, String username, String password, String pemPrivateKey) throws IOException {

        Connection conn = new Connection(host, port);

        conn.connect();

        if ((password != null)) {
            if (pemPrivateKey != null && pemPrivateKey.trim().length() > 0)
                conn.authenticateWithPublicKey(username, pemPrivateKey.toCharArray(), password);
            else
                conn.authenticateWithPassword(username, password);
        } else
            conn.authenticateWithNone(username);

        conn.getConnectionInfo();

        conn.sendIgnorePacket();

        return conn;
    }

}
