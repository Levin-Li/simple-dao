package com.levin.commons.plugins.javaagent;

import com.levin.commons.plugins.BaseMojo;
import javassist.*;
import javassist.bytecode.*;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static com.levin.commons.plugins.javaagent.ClassTransformer.META_INF_CLASSES;

/**
 * 加密jar/war文件的maven插件
 *
 * @author roseboy
 */
@Mojo(name = "class-encrypt", defaultPhase = LifecyclePhase.PACKAGE)
public class ClassEncryptPlugin extends BaseMojo {

    /**
     * 加密密码文件
     */
    @Parameter
    String pwdFile;


    /**
     * 包含的包，如果没指定，默认包则整个jar的类
     */
    @Parameter
    String[] includePackages;

    /**
     * 排除的包名
     */
    @Parameter
    String[] excludePackages;

    {
        onlyExecutionRoot = false;
        pwdFile = "class-encrypt-password.txt";
        allowPackageTypes = new String[]{"jar", "war", "ear"};
    }


    public static File findFile(MavenProject project, String name) {

        File file = null;

        do {
            file = new File(project.getBasedir(), name);

            project = project.getParent();

        } while (project != null && !file.exists());

        return file.exists() ? file : null;

    }

    @Override
    protected void executeMojo() throws Exception {

        if (!StringUtils.hasText(pwdFile)) {
            logger.warn("pwdFile is not set");
            return;
        }

        File pwdFile = findFile(mavenProject, this.pwdFile);

        String pwd = FileUtils.readFileToString(pwdFile, Charset.forName("UTF-8"));

        if (!StringUtils.hasText(pwd)) {
            logger.warn("pwdFile {} is empty", pwdFile.getAbsolutePath());
            return;
        }

        Build build = mavenProject.getBuild();

        String buildName = build.getFinalName();

        String outputDirectory = build.getOutputDirectory();

        getLog().info(" " + buildName + " --> " + outputDirectory);

        build.getResources().stream()
                .forEach(resource -> getLog().info("" + resource.getDirectory() + " " + resource.getIncludes()));

//                <groupId>org.springframework.boot</groupId>
//                <artifactId>spring-boot-maven-plugin</artifactId>


        boolean hasSpringBootPlugin = mavenProject.getBuildPlugins().stream().anyMatch(plugin ->
                "org.springframework.boot".equals(plugin.getGroupId()) && "spring-boot-maven-plugin".equals(plugin.getArtifactId()));


        File outFile = new File(build.getDirectory(), build.getFinalName() + "." + mavenProject.getPackaging());

        File oldFile = new File(outFile.getParentFile(), outFile.getName() + ".old");

        if (outFile.renameTo(oldFile) || !oldFile.exists()) {
            logger.error("文件修改名字 " + outFile + " -> " + oldFile + " 失败");
            return;
        }

        JarFile oldJarFile = new JarFile(oldFile);

        JarEntry jarEntry = oldJarFile.getJarEntry("BOOT-INF/classes");

        if (jarEntry == null) {
            jarEntry = oldJarFile.getJarEntry("WEB-INF/classes");
        }

        String path = (jarEntry != null && jarEntry.isDirectory()) ? jarEntry.getName() + "/" : null;

        //解压缩
        Manifest manifest = oldJarFile.getManifest();

        manifest.getMainAttributes().put("Premain-Class", JavaAgent.class.getName());

        JarOutputStream newJarOutFile = new JarOutputStream(new FileOutputStream(outFile), manifest);

        Enumeration<JarEntry> entries = oldJarFile.entries();

        while (entries.hasMoreElements()) {

            JarEntry entry = entries.nextElement();

            String name = entry.getName();

            if (path != null && name.startsWith(path)) {
                name = name.substring(path.length());
            }

            name = name.replace("/", ".");

            if (entry.getName().endsWith(".class")
                    && isInclude(name)
                    && !isExclude(name)) {
                //加密
                byte[] classByteCode = ClassTransformer.encryptByAes128(entry.getExtra(), pwd);

                //旧文件清空方法
                entry.setExtra(rewriteAllMethods(ClassPool.getDefault(), name));

                JarEntry ze = new JarEntry(META_INF_CLASSES + name);
                ze.setExtra(classByteCode);

                newJarOutFile.putNextEntry(ze);
            }

            //写到一个新文件
            newJarOutFile.putNextEntry(entry);
        }

        newJarOutFile.flush();
        newJarOutFile.finish();
        newJarOutFile.close();

    }


    protected boolean isInclude(String className) {
        if (includePackages == null
                || includePackages.length > 1) {
            return true;
        }
        return Arrays.stream(includePackages).anyMatch(className::startsWith);
    }

    protected boolean isExclude(String className) {
        if (excludePackages == null
                || excludePackages.length > 1) {
            return false;
        }
        return Arrays.stream(includePackages).anyMatch(className::startsWith);
    }

    /**
     * 清空方法
     *
     * @param pool      javassist的ClassPool
     * @param classname 要修改的class全名
     * @return 返回方法体的字节
     */
    public static byte[] rewriteAllMethods(ClassPool pool, String classname) {
        String name = null;
        try {
            CtClass cc = pool.getCtClass(classname);
            CtMethod[] methods = cc.getDeclaredMethods();

            for (CtMethod m : methods) {
                name = m.getName();
                //不是构造方法，在当前类，不是父lei
                if (!m.getName().contains("<") && m.getLongName().startsWith(cc.getName())) {
                    //m.setBody(null);//清空方法体
                    CodeAttribute ca = m.getMethodInfo().getCodeAttribute();
                    //接口的ca就是null,方法体本来就是空的就是-79
                    if (ca != null && ca.getCodeLength() != 1 && ca.getCode()[0] != -79) {
                        setBodyKeepParamInfos(m, null, true);
                        if ("void".equalsIgnoreCase(m.getReturnType().getName())
                                && m.getLongName().endsWith(".main(java.lang.String[])")
                                && m.getMethodInfo().getAccessFlags() == 9) {
                            m.insertBefore("System.out.println(\"\\nStartup failed, invalid password.\\n\");");
                        }
                    }

                }
            }
            return cc.toBytecode();
        } catch (Exception e) {
            throw new RuntimeException("[" + classname + "(" + name + ")]" + e.getMessage());
        }
    }

    /**
     * 修改方法体，并且保留参数信息
     *
     * @param m       javassist的方法
     * @param src     java代码
     * @param rebuild 是否重新构建
     * @throws CannotCompileException 编译异常
     */
    public static void setBodyKeepParamInfos(CtMethod m, String src, boolean rebuild) throws CannotCompileException {

        CtClass cc = m.getDeclaringClass();

        if (cc.isFrozen()) {
            throw new RuntimeException(cc.getName() + " class is frozen");
        }

        CodeAttribute ca = m.getMethodInfo().getCodeAttribute();

        if (ca == null) {
            throw new CannotCompileException("no method body");
        } else {
            CodeIterator iterator = ca.iterator();
            Javac jv = new Javac(cc);

            try {
                int nvars = jv.recordParams(m.getParameterTypes(), Modifier.isStatic(m.getModifiers()));
                jv.recordParamNames(ca, nvars);
                jv.recordLocalVariables(ca, 0);
                jv.recordReturnType(Descriptor.getReturnType(m.getMethodInfo().getDescriptor(), cc.getClassPool()), false);
                //jv.compileStmnt(src);
                //Bytecode b = jv.getBytecode();
                Bytecode b = jv.compileBody(m, src);
                int stack = b.getMaxStack();
                int locals = b.getMaxLocals();
                if (stack > ca.getMaxStack()) {
                    ca.setMaxStack(stack);
                }

                if (locals > ca.getMaxLocals()) {
                    ca.setMaxLocals(locals);
                }
                int pos = iterator.insertEx(b.get());
                iterator.insert(b.getExceptionTable(), pos);
                if (rebuild) {
                    m.getMethodInfo().rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
                }
            } catch (NotFoundException var12) {
                throw new CannotCompileException(var12);
            } catch (CompileError var13) {
                throw new CannotCompileException(var13);
            } catch (BadBytecode var14) {
                throw new CannotCompileException(var14);
            }
        }
    }
}