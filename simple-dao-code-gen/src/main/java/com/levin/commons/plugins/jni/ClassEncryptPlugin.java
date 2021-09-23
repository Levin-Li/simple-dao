package com.levin.commons.plugins.jni;

import com.levin.commons.plugins.BaseMojo;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.model.Build;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.springframework.asm.*;
import org.springframework.cglib.core.Constants;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipOutputStream;


/**
 * 加密jar/war文件的maven插件
 *
 * @author roseboy
 */
@Mojo(name = "encrypt-class", defaultPhase = LifecyclePhase.PACKAGE)
public class ClassEncryptPlugin extends BaseMojo {

    public static final String MANIFEST = "META-INF/MANIFEST.MF";

    /**
     * 加密密码文件
     */
    @Parameter
    String pwdFile;

    /**
     * 类加密密码，pwdFile参数优先
     */
    @Parameter
    String pwd;

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
        isPrintException = true;
        pwdFile = ".java_agent/.pwdFile.txt";
        allowPackageTypes = new String[]{"jar", "war", "ear"};
    }


    public static File findFile(MavenProject project, String name) {

        if (!StringUtils.hasText(name)) {
            return null;
        }

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
        }

        File pwdFile = findFile(mavenProject, this.pwdFile);

        if (pwdFile != null) {
            pwd = FileUtils.readFileToString(pwdFile, Charset.forName("UTF-8"));
            getLog().info("从" + pwdFile + "读取密码进行加密...");
        }

        if (!StringUtils.hasText(pwd)) {
            logger.warn("password not set , ignore.");
            return;
        }

        //设置加密密码
        SimpleLoaderAndTransformer.setPwd(pwd, this.pwdFile);

        Build build = mavenProject.getBuild();

        boolean hasSpringBootPlugin = mavenProject.getBuildPlugins().stream().anyMatch(plugin ->
                "org.springframework.boot".equals(plugin.getGroupId()) && "spring-boot-maven-plugin".equals(plugin.getArtifactId()));

        File buildFile = new File(build.getDirectory(), build.getFinalName() + "." + mavenProject.getPackaging());

        if (!buildFile.exists()) {
            logger.error("文件" + buildFile + "不存在");
            return;
        }

        JarFile buildFileJar = new JarFile(buildFile);

        JarEntry testEntry = buildFileJar.getJarEntry(SimpleLoaderAndTransformer.class.getName().replace(".", "/") + ".class");

        if (testEntry != null && !testEntry.isDirectory()) {
            logger.error("文件" + buildFile + "已经加密");
            return;
        }

        JarEntry jarEntry = buildFileJar.getJarEntry("BOOT-INF/classes");

        if (jarEntry == null) {
            jarEntry = buildFileJar.getJarEntry("WEB-INF/classes");
        }

        String path = (jarEntry != null && jarEntry.isDirectory()) ? jarEntry.getName() : "";

        //解压缩
        Manifest manifest = buildFileJar.getManifest();

        manifest.getMainAttributes().putValue("Premain-Class", JavaAgent.class.getName());
        manifest.getMainAttributes().putValue("Agent-Class", JavaAgent.class.getName());
        manifest.getMainAttributes().putValue("Can-Redefine-Classes", "" + true);
        manifest.getMainAttributes().putValue("Can-Retransform-Classes", "" + true);


        File encryptOutFile = new File(buildFile.getParentFile(), "Encrypt-" + buildFile.getName());

        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(encryptOutFile), manifest);

        //
        writeClassToJar(jarOutputStream, "", JniHelper.class, JavaAgent.class, SimpleLoaderAndTransformer.class);

        Enumeration<JarEntry> entries = buildFileJar.entries();

        //检测如果有 main 函数，则不加密，但是加入代码

        byte[] emptyArray = new byte[0];

        while (entries.hasMoreElements()) {

            JarEntry entry = entries.nextElement();

            String name = entry.getName();

            if (MANIFEST.equals(name)) {
                continue;
            }

            if (name.startsWith(path)) {
                name = name.substring(path.length());
            }

            name = name.replace("/", ".");

            byte[] fileContent = entry.getSize() > 0 ? IOUtils.readFully(buildFileJar.getInputStream(entry), (int) entry.getSize()) : emptyArray;

            boolean isChange = false;

            // getLog().info("Path:" + path + " " + entry.getName());


            if (entry.getName().endsWith(".class")
                    && entry.getName().startsWith(path)
                    && !entry.isDirectory()
                    && isInclude(name)
                    && !isExclude(name)) {

                //获取类名的md5
                name = name.substring(0, name.length() - 6);

                String resPath = HookAgent.getClassResPath(name);

                jarOutputStream.putNextEntry(new JarEntry(resPath));

                byte[] encryptData = SimpleLoaderAndTransformer.transform2(HookAgent.DEFAULT_KEY2, processMethodBody(fileContent, name, false, false));

                jarOutputStream.write(encryptData);

                //旧文件清空方法
                fileContent = processMethodBody(fileContent, name, true, true);

                isChange = true;

                getLog().info("加密类文件 " + entry.getName() + " -- > " + name + " 原大小： " + entry.getSize() + " , 加密后的大小：" + encryptData.length + " , 方法清空后的大小：" + fileContent.length);
            }

            JarEntry entry2 = isChange ? new JarEntry(entry.getName()) : new JarEntry(entry);

            //如果被改变
            if (isChange) {
                boolean compressed = entry.getMethod() == JarEntry.DEFLATED || entry.getSize() > entry.getCompressedSize();
                //spring boot 不允许 jar , zip 进行二次压缩
                entry2.setMethod(compressed ? JarEntry.DEFLATED : JarEntry.STORED);

                //如果不压缩，CRC32 重新计算
                if (!compressed) {
                    entry2.setSize(fileContent.length);
                    CRC32 crc32 = new CRC32();
                    crc32.update(fileContent);
                    entry2.setCrc(crc32.getValue());
                }

                entry2.setTime(entry.getTime());
            }

//            if (!isChange) {
            jarOutputStream.putNextEntry(entry2);
            jarOutputStream.write(fileContent);
//            }

        }

        //放入 hook 类
        jarOutputStream.putNextEntry(new JarEntry(HookAgent.class.getName().replace('.', '/') + ".class"));
        jarOutputStream.write(processMethodBody(JniHelper.loadData(HookAgent.class), HookAgent.class.getName(), true, false));

        jarOutputStream.putNextEntry(new JarEntry("META-INF/MANIFEST.INF"));
        jarOutputStream.write(SimpleLoaderAndTransformer.transform1(HookAgent.DEFAULT_KEY, JniHelper.loadData(HookAgent.class)));

        jarOutputStream.finish();
        jarOutputStream.flush();
        jarOutputStream.close();

        buildFileJar.close();

        rename(buildFile, new File(buildFile.getAbsolutePath() + ".old"));
//
//        变更名字
        rename(encryptOutFile, buildFile);

        getLog().info("" + buildFile + "  sha256 --> " + toHexStr(sha256Hash(buildFile)));

    }


    @SneakyThrows
    protected byte[] sha256Hash(File file) {

//        MD5
//        SHA-1
//        SHA-256
        FileInputStream fileInputStream = new FileInputStream(file);

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        byte[] buf = new byte[8192];

        try {
            int n = -1;
            while ((n = fileInputStream.read(buf)) > -1) {
                if (n > 0) {
                    messageDigest.digest(buf, 0, n);
                }
            }
        } finally {
            fileInputStream.close();
        }

        return messageDigest.digest();
    }


    protected static String toHexStr(byte[] data) {

        StringBuilder stringBuilder = new StringBuilder();

        for (byte aByte : data) {
            stringBuilder.append(Integer.toHexString(0xFF & aByte));
        }

        return stringBuilder.toString();
    }


    @SneakyThrows
    protected static void rename(File oldFile, File newFile) {

        int n = 0;

        while (n++ < 100) {

            newFile.delete();

            boolean ok = !newFile.exists() && oldFile.renameTo(newFile) && !oldFile.exists();
            if (!ok) {
                Thread.sleep(100);
            } else {
                break;
            }
        }

        if (oldFile.exists() || !newFile.exists()) {
            throw new RuntimeException(oldFile + " can't rename to " + newFile);
        }

    }


    @SneakyThrows
    protected void writeClassToJar(ZipOutputStream outputStream, String path, Class... classes) {

        for (Class clazz : classes) {

            String fn = path + clazz.getName().replace(".", "/") + ".class";

            outputStream.putNextEntry(new JarEntry(fn));

            IOUtils.copy(clazz.getClassLoader().getResourceAsStream(fn), outputStream);
        }

    }


    /**
     * 清除方法体
     *
     * @param data
     * @return
     */
    protected byte[] processMethodBody(byte[] data, final String className, boolean isClearOldBody, boolean isAddHookAgentMethod) {

        ClassReader reader = new ClassReader(data);


//        JVM 的执行模型。我们都知道，Java 代码是运行在线程中的，每条线程都拥有属于自己的运行栈，栈是由一个或多个帧组成的，也叫栈帧（StackFrame）。每个栈帧代表一个方法调用：每当线程调用一个Java方法时，JVM就会在该线程对应的栈中压入一个帧；当执行这个方法时，它使用这个帧来存储参数、局部变量、中间运算结果等等；当方法执行结束（无论是正常返回还是抛异常）时，该栈帧就会弹出，然后继续运行下一个栈帧（栈顶栈帧）的方法调用。
//        栈帧由三部分组成：局部变量表、操作数栈、帧数据区。


        // 2. 创建 ClassWriter 对象，将操作之后的字节码的字节数组回写
//        ClassWriter的构造函数需要传入一个 flag，其含义为：
//        ClassWriter(0)：表示 ASM 不会自动自动帮你计算栈帧和局部变量表和操作数栈大小。
//        ClassWriter(ClassWriter.COMPUTE_MAXS)：表示 ASM 会自动帮你计算局部变量表和操作数栈的大小，但是你还是需要调用visitMaxs方法，但是可以使用任意参数，因为它们会被忽略。带有这个标识，对于栈帧大小，还是需要你手动计算。
//        ClassWriter(ClassWriter.COMPUTE_FRAMES)：表示 ASM 会自动帮你计算所有的内容。你不必去调用visitFrame，但是你还是需要调用visitMaxs方法（参数可任意设置，同样会被忽略）。
//        使用这些标识很方便，但是会带来一些性能上的损失：COMPUTE_MAXS标识会使ClassWriter慢10%，COMPUTE_FRAMES标识会使ClassWriter慢2倍，
        final ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);


        final AtomicBoolean isModified = new AtomicBoolean(false);

        final AtomicBoolean isInterface = new AtomicBoolean(false);

        final ClassVisitor visitor = new ClassVisitor(Constants.ASM_API, writer) {

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                isInterface.set((access & Opcodes.ACC_INTERFACE) == Opcodes.ACC_INTERFACE);
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, final String methodName, String descriptor, String signature, String[] exceptions) {

                //返回 MethodWriter
                final MethodVisitor mWriter = writer.visitMethod(access, methodName, descriptor, signature, exceptions);

                final MethodVisitor methodVisitor = new MethodVisitor(Constants.ASM_API, mWriter) {

                    @Override
                    public void visitParameter(String name, int access) {
                        super.visitParameter(name, access);
                    }

                    @Override
                    public void visitCode() {

                        Type returnType = Type.getReturnType(descriptor);
                        boolean isStatic = (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;

                        //如果是接口
                        if (isInterface.get()) {
                            super.visitCode();
                            return;
                        }

                        //如果是 main 方法
                        boolean isMain = methodName.equals("main")
                                && isStatic
                                && returnType.getSort() == Type.VOID;


                        if (!isClearOldBody) {
                            if (isMain) {
                                mWriter.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(HookAgent.class), "premain", Type.getMethodDescriptor(Type.VOID_TYPE), false);

                                isModified.set(true);
                                getLog().info("*** 类 " + className + "  " + methodName + "方法增加代码。");

                            }

                            return;
                        }

                        if (isMain) {
                            return;
                        }


                        if (!StringUtils.hasText(methodName)
                                || (methodName.startsWith("access$") && methodName.length() > "access$".length())
                                || methodName.equalsIgnoreCase("<init>")
                                || methodName.equalsIgnoreCase("<cinit>")
                                || methodName.equals("{}")) {

                            //   isModified.set(true);
                            //   this.mv = null;
                            //   mWriter.visitInsn(returnType.getOpcode(Opcodes.IRETURN));

                        } else {

                            isModified.set(true);

                            //清空方法
                            this.mv = null;

                            //写入空操作
                            //调用静态方法抛出异常
                            if (isAddHookAgentMethod) {

                                Type[] argumentTypes = Type.getArgumentTypes(descriptor);

                                StringBuilder buf = new StringBuilder();
//                                ILOAD, LLOAD, FLOAD, DLOAD, ALOAD

                                //静态方法从 0 开始
                                int index = 0;

                                if (!isStatic) {
                                    mWriter.visitVarInsn(Opcodes.ALOAD, index++);
                                }

                                for (Type argumentType : argumentTypes) {

                                    mWriter.visitVarInsn(argumentType.getOpcode(Opcodes.ILOAD), index);

                                    buf.append(index).append(".").append(argumentType.getClassName()).append(",");

                                    //参数
                                    index += argumentType.getSize();
                                }

                                // getLog().info(className + "." + methodName + " isStatic:" + isStatic + " params:" + buf);

                                //执行方法
                                mWriter.visitMethodInsn(isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKESPECIAL, className.replace('.', '/'), methodName, descriptor, isInterface.get());

                                //返回
                                mWriter.visitInsn(returnType.getOpcode(Opcodes.IRETURN));

                            }

                            //  mWriter.visitMaxs(7, 7);//设置局部表量表和操作数栈大小
                        }
                    }

                    @Override
                    public void visitEnd() {
                        //方法结束，恢复写入内容
                        this.mv = mWriter;
                        super.visitEnd();
                    }
                };

                return methodVisitor;
            }
        };

        // 4. 将 ClassVisitor 对象传入 ClassReader 中
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
//
//        if (hasContent(fieldName)) {
//            FieldVisitor fieldVisitor = writer.visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL, fieldName, Type.getDescriptor(byte[].class), null, null);
//            fieldVisitor.visitEnd();
//        }

        return isModified.get() ? writer.toByteArray() : data;
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

}