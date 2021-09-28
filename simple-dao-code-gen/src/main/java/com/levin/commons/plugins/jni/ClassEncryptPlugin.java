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
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.CRC32;
import java.util.zip.ZipOutputStream;

import static org.springframework.asm.Opcodes.ACONST_NULL;


/**
 * 加密jar/war文件的maven插件
 *
 * @author roseboy
 */
@Mojo(name = "encrypt-class", defaultPhase = LifecyclePhase.PACKAGE)
public class ClassEncryptPlugin extends BaseMojo {

    private static final String MF_CRYPT_TIME = "Levin-Encrypt-Time";

    public static final String MANIFEST = "META-INF/MANIFEST.MF";

    /**
     * 加密密码文件
     */
    @Parameter
    String pwdFilePath;

    /**
     * 类加密密码，pwdFile参数优先
     */
    @Parameter
    String pwd;

    /**
     * 包含的包，如果没指定，默认包则整个jar的类
     */
    @Parameter
    String[] includeClasses = {};

    /**
     * 排除的包名
     */
    @Parameter
    String[] excludeClasses = {};

    /**
     * 排除有指定注解的类
     */
    @Parameter
    String[] excludeClassesByAnnotations = {Configuration.class.getName()};

    /**
     * 拷贝资源到指定的目标
     */
    @Parameter
    Map<String, String> copyResToFile = new LinkedHashMap<>();

    /**
     * 拷贝动态链接库到指定位置，没有指定则不拷贝
     */
    @Parameter(defaultValue = "${project.build.directory}/third-libs")
    String copyNativeLibToDir;

    {
        onlyExecutionRoot = false;
        isPrintException = true;
        allowPackageTypes = new String[]{"jar", "war", "ear", "aar", "zip"};
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

        if (!StringUtils.hasText(pwdFilePath)) {
            pwdFilePath = ".java_agent/.pwdFile.txt";
            logger.warn("密码文件没有指定 , 默认为" + pwdFilePath);
        }

        File pwdFile = findFile(mavenProject, this.pwdFilePath);

        if (pwdFile != null) {
            pwd = FileUtils.readFileToString(pwdFile, Charset.forName("UTF-8"));
            this.pwdFilePath = pwdFile.getAbsolutePath();
            getLog().info("从" + pwdFile + "读取密码进行加密...");
        }

        if (!StringUtils.hasText(pwd)) {
            logger.warn("加密打包密码没有设置，您可以在" + this.pwdFilePath + "配置密码");
            return;
        }

        //设置加密密码
        SimpleLoaderAndTransformer.setPwd(pwd, this.pwdFilePath);

        Build build = mavenProject.getBuild();

        boolean hasSpringBootPlugin = mavenProject.getBuildPlugins().stream().anyMatch(plugin ->
                "org.springframework.boot".equals(plugin.getGroupId()) && "spring-boot-maven-plugin".equals(plugin.getArtifactId()));

        File buildFile = new File(build.getDirectory(), build.getFinalName() + "." + mavenProject.getPackaging());

        if (!buildFile.exists()) {
            logger.error("文件" + buildFile + "不存在");
            return;
        }

        JarFile buildFileJar = new JarFile(buildFile);

        Manifest manifest = buildFileJar.getManifest();

        final String encryptFlag = "META-INF/" + JniHelper.md5("encrypt_hook_class=" + HookAgent.class.getName()) + ".dat";

        if (buildFileJar.getJarEntry(encryptFlag) != null) {
            logger.error("文件" + buildFile + "已经加密");
            buildFileJar.close();
            return;
        }


//        Manifest 样例
//        Manifest-Version: 1.0
//        Implementation-Title: superbag-testcase
//        Implementation-Version: 1.0.0-SNAPSHOT
//        Agent-Class: com.levin.commons.plugins.jni.JavaAgent
//        Can-Redefine-Classes: true
//        Spring-Boot-Version: 2.3.5.RELEASE
//        Main-Class: org.springframework.boot.loader.PropertiesLauncher
//        Spring-Boot-Classpath-Index: BOOT-INF/classpath.idx
//        Premain-Class: com.levin.commons.plugins.jni.JavaAgent
//        Start-Class: com.vma.superbag.Application
//        Spring-Boot-Classes: BOOT-INF/classes/
//        Can-Retransform-Classes: true
//        Spring-Boot-Lib: BOOT-INF/lib/
//        Build-Jdk-Spec: 1.8
//        Created-By: Maven Jar Plugin 3.2.0

        JarEntry jarClassPath = buildFileJar.getJarEntry((String) manifest.getMainAttributes().getOrDefault("Spring-Boot-Classes", "BOOT-INF/classes"));

        if (jarClassPath == null) {
            jarClassPath = buildFileJar.getJarEntry("WEB-INF/classes");
        }

        String path = (jarClassPath != null && jarClassPath.isDirectory()) ? jarClassPath.getName() : "";


        String mainClass = manifest.getMainAttributes().getValue("Main-Class");
        String startClass = manifest.getMainAttributes().getValue("Start-Class");

        manifest.getMainAttributes().putValue("Premain-Class", JavaAgent.class.getName());
        manifest.getMainAttributes().putValue("Agent-Class", JavaAgent.class.getName());

        manifest.getMainAttributes().putValue("Can-Redefine-Classes", "" + false);
        manifest.getMainAttributes().putValue("Can-Retransform-Classes", "" + false);
        manifest.getMainAttributes().putValue(MF_CRYPT_TIME, "" + System.currentTimeMillis());

        File encryptOutFile = new File(buildFile.getParentFile(), "Encrypt-" + buildFile.getName());

        if (encryptOutFile.exists()) {
            encryptOutFile.setWritable(true);
            encryptOutFile.delete();
        }

        final JarOutputStream newJarFileOutStream = new JarOutputStream(new FileOutputStream(encryptOutFile), manifest);

        newJarFileOutStream.putNextEntry(new JarEntry(encryptFlag));
        newJarFileOutStream.write(("" + System.currentTimeMillis()).getBytes("UTF-8"));

        Enumeration<JarEntry> entries = buildFileJar.entries();

        //检测如果有 main 函数，则不加密，但是加入代码

        getLog().info("***  includeClasses: " + Arrays.asList(includeClasses)
                + " , excludeClasses: " + Arrays.asList(excludeClasses)
                + " , excludeClassesByAnnotations: " + Arrays.asList(excludeClassesByAnnotations));

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

            if (name.endsWith(".class")) {
                name = name.substring(0, name.length() - 6);
            }

            //是否是有效的类文件
            boolean isClassFile = entry.getName().endsWith(".class")
                    && entry.getName().startsWith(path)
                    && !entry.isDirectory();

            //是否是主类
            boolean isMainClass = isClassFile && (name.equals(mainClass) || name.equals(startClass));

            byte[] fileContent = entry.getSize() > 0 ? IOUtils.readFully(buildFileJar.getInputStream(entry), (int) entry.getSize()) : emptyArray;

            boolean isChange = false;

            if (isClassFile) {
                //  getLog().info("process class " + name + " --> " + entry.getName() + "...");
            }

            if (isClassFile
                    && !isMainClass //主启动类不加密
                    && !isExclude(name)
                    && !isAnnotation(name)
                    && !isExcludeByAnnotation(name)
                    && isInclude(name)) {

                //获取类名的md5

                String resPath = HookAgent.getClassResPath(name);

                newJarFileOutStream.putNextEntry(new JarEntry(resPath));

                byte[] encryptData = SimpleLoaderAndTransformer.transform2(HookAgent.DEFAULT_KEY2, processMethodBody(fileContent, name, false, true));

                newJarFileOutStream.write(encryptData);

                //旧文件清空方法
                fileContent = processMethodBody(fileContent, name, true, false);

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
            } else if (isClassFile
                    && (isMainClass || isExcludeByAnnotation(name))) {
                //如果主类，或是 被排除的特定类
                entry2 = new JarEntry(entry.getName());
                fileContent = processMethodBody(fileContent, name, false, false);
            }

//            if (!isChange) {
            newJarFileOutStream.putNextEntry(entry2);
            newJarFileOutStream.write(fileContent);
//            }

        }

        copyRes(StringUtils.hasText(mainClass), newJarFileOutStream);

        newJarFileOutStream.finish();
        newJarFileOutStream.flush();
        newJarFileOutStream.close();

        buildFileJar.close();

        rename(buildFile, new File(buildFile.getAbsolutePath() + ".old"));

//        变更名字
        rename(encryptOutFile, buildFile);


        getLog().info("" + buildFile + "  sha256 --> " + HookAgent.toHexStr(HookAgent.getFileSHA256Hashcode(buildFile)));

    }

    @SneakyThrows
    private void copyRes(boolean copyNativeLib, JarOutputStream jarOutputStream) {

        if (copyNativeLib) {

            writeClassToJar(jarOutputStream, "", JniHelper.class, JavaAgent.class, SimpleLoaderAndTransformer.class);
            //放入 hook 类
            jarOutputStream.putNextEntry(new JarEntry(HookAgent.class.getName().replace('.', '/') + ".class"));
            jarOutputStream.write(processMethodBody(JniHelper.loadData(HookAgent.class), HookAgent.class.getName(), true, true));

            //固定名称，故意混淆名称
            jarOutputStream.putNextEntry(new JarEntry("META-INF/MANIFEST.INF"));
            jarOutputStream.write(SimpleLoaderAndTransformer.transform1(HookAgent.DEFAULT_KEY, JniHelper.loadData(HookAgent.class)));

        }

        //拷贝资源
        Optional.ofNullable(copyResToFile)
                .orElse(Collections.emptyMap()).forEach((k, v) -> {

            boolean ok = JniHelper.copyResToFile(getClass().getClassLoader(), k, v)
                    || JniHelper.copyResToFile(getClassLoader(), k, v);

            getLog().info("copy res " + k + " --> " + v + " " + ok);

        });

        if (copyNativeLib && StringUtils.hasText(copyNativeLibToDir)) {

            String[] nativeLibs = {
                    "lib/HookAgent/linux/libHookAgent.so",
                    "lib/HookAgent/macosx/libHookAgent.dylib",
                    "lib/HookAgent/windows/libHookAgent.dll",
            };

            for (String nativeLib : nativeLibs) {


                byte[] data = JniHelper.loadResource(getClass().getClassLoader(), nativeLib);

                if (data != null) {
                    jarOutputStream.putNextEntry(new JarEntry(nativeLib));
                    jarOutputStream.write(data);
                }

                String outFile = copyNativeLibToDir + "/" + nativeLib.substring(nativeLib.lastIndexOf("/"));

                boolean ok = JniHelper.copyResToFile(getClass().getClassLoader(), nativeLib, outFile);

                getLog().info("copy native lib " + nativeLib + " --> " + outFile + " " + ok);
            }
        }
    }

    private boolean isAnnotation(String name) {
        Class aClass = loadClass(name);
        return aClass.isAnnotation();
    }

    @SneakyThrows
    private boolean isExcludeByAnnotation(String name) {

        //https://blog.csdn.net/qq_22845447/article/details/83210559
//    AnnotationUtils.getAnnotation
//　　从提供的AnnotatedElement获取annotationType的单个Annotation，其中注解在AnnotatedElement上存在或元存在。请注意，此方法仅支持单级元注解。要支持任意级别的元注解，请使用findAnnotation（AnnotatedElement，Class）。　　
//
//　　AnnotationUtils.findAnnotation
//　　在提供的AnnotatedElement上查找annotationType的单个Annotation。如果注解不直接出现在提供的元素上，则将搜索元注解。
//
//　　AnnotatedElementUtils.isAnnotated
//　　确定在提供的AnnotatedElement上或指定元素上方的注解层次结构中是否存在指定annotationType的注解。如果此方法返回true，则getMergedAnnotationAttributes方法将返回非null值。
//
//　　AnnotatedElementUtils.hasAnnotation
//　　确定指定的annotationType的注解是否在提供的AnnotatedElement上或在指定元素上方的注解层次结构中可用。如果此方法返回true，则findMergedAnnotationAttributes方法将返回非null值。
//
//　　AnnotatedElementUtils.getMergedAnnotation
//　　在提供的元素上方的注解层次结构中获取指定注解类型的第一个注解，将注解的属性与注解层次结构的较低级别中的注解的匹配属性合并，并将结果合成回指定注解类型的注解。完全支持@AliasFor语义，包括单个注解和注解层次结构。此方法委托给getMergedAnnotationAttributes（AnnotatedElement，Class）和AnnotationUtils.synthesizeAnnotation（Map，Class，AnnotatedElement）。

        try {
            Class aClass = loadClass(name);
            return Arrays.stream(excludeClassesByAnnotations).filter(StringUtils::hasText).anyMatch(annoClsName -> {
//                        Class<? extends Annotation> annoClass = loadClass(annoClsName);
//                                return  AnnotatedElementUtils.(aClass, annoClsName);
                        return AnnotationUtils.findAnnotation(aClass, loadClass(annoClsName)) != null;
                    }
            );
        } catch (Throwable e) {
            getLog().error(e);
            throw e;
        }
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
    protected byte[] processMethodBody(byte[] data, final String className, boolean isClearOldBody, boolean isEncrypt) {

        if (data == null || data.length == 0) {
            return data;
        }

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
                        boolean isAbstract = (access & Opcodes.ACC_ABSTRACT) == Opcodes.ACC_ABSTRACT;

                        //如果是抽象方法
                        String hookClassName = HookAgent.class.getName().replace('.', '/');

                        if (isAbstract
                                || !StringUtils.hasText(methodName)
                                || (methodName.startsWith("access$") && methodName.length() > "access$".length())
                                || methodName.equalsIgnoreCase("<init>")
                                || methodName.equalsIgnoreCase("<cinit>")
                                || methodName.equalsIgnoreCase("<clinit>")) {


                            if (methodName.equalsIgnoreCase("<clinit>")
                                    && !className.equals(hookClassName)) {
                                //在类构造方法中加入语句
                                isModified.set(true);


                                String invokeMethodName = isClearOldBody ? "unsafeClassInit" : "classInit";

                                if (isEncrypt) {
                                    invokeMethodName = "checkEnv";
                                }

                                mWriter.visitMethodInsn(Opcodes.INVOKESTATIC, hookClassName,
                                        invokeMethodName, "()V", false);
                            }

                            return;
                        }

                        if (isClearOldBody) {
                            this.mv = null;
                        }

                        //如果是 main 方法
                        boolean isMain = methodName.equals("main")
                                && isStatic
                                && descriptor.equals(Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType((new String[0]).getClass())));

                        if (isMain) {
                            isModified.set(true);
                            mWriter.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(HookAgent.class), "premain", Type.getMethodDescriptor(Type.VOID_TYPE), false);
                            getLog().info("*** 类 " + className + "  " + methodName + " 方法增加代码。");
                        }

                        if (isClearOldBody) {
                            isModified.set(true);
                            //增加返回处理
                            setDefaultValue(mWriter, returnType);
                            mWriter.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
                        }

                        mWriter.visitMaxs(15, 15);//设置局部表量表和操作数栈大小

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

            /**
             * 调用 super 方法
             * @param returnType
             * @param descriptor
             * @param mWriter
             * @param methodName
             */
            private void addCallSuper(int access, Type returnType, String descriptor, MethodVisitor mWriter, String
                    methodName) {

                boolean isStatic = (access & Opcodes.ACC_STATIC) == Opcodes.ACC_STATIC;

                Type[] argumentTypes = Type.getArgumentTypes(descriptor);
//                                ILOAD, LLOAD, FLOAD, DLOAD, ALOAD

                //静态方法从 0 开始
                int index = 0;
//
                if (!isStatic) {
                    //如果不是静态方法，加载 this 对象
                    mWriter.visitVarInsn(Opcodes.ALOAD, index++);
                }

                StringBuilder params = new StringBuilder();
                //加载参数
                for (Type argumentType : argumentTypes) {
                    mWriter.visitVarInsn(argumentType.getOpcode(Opcodes.ILOAD), index);
                    params.append(index).append(".").append(argumentType.getClassName()).append(",");
                    //参数
                    index += argumentType.getSize();
                }

//                                invokestatic：该指令用于调用静态方法，即使用 static 关键字修饰的方法；
//                                invokespecial：该指令用于三种场景：调用实例构造方法，调用私有方法（即private关键字修饰的方法）和父类方法（即super关键字调用的方法）；
//                                invokeinterface：该指令用于调用接口方法，在运行时再确定一个实现此接口的对象；
//                                invokevirtual：该指令用于调用虚方法（就是除了上述三种情况之外的方法）；通常是子类的方法，还未实现的抽象方法
//                                invokedynamic：在运行时动态解析出调用点限定符所引用的方法之后，调用该方法；在JDK1.7中推出，主要用于支持JVM上的动态脚本语言（如Groovy，Jython等）。
//
//              //执行自己方法
                mWriter.visitMethodInsn(isStatic ? Opcodes.INVOKESTATIC : Opcodes.INVOKESPECIAL, className.replace('.', '/'), methodName, descriptor, isInterface.get());

//              //返回
                mWriter.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
            }
        };

        // 4. 将 ClassVisitor 对象传入 ClassReader 中
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);

//        if (hasContent(fieldName)) {
//            FieldVisitor fieldVisitor = writer.visitField(ACC_PRIVATE + ACC_STATIC + ACC_FINAL, fieldName, Type.getDescriptor(byte[].class), null, null);
//            fieldVisitor.visitEnd();
//        }

        return isModified.get() ? writer.toByteArray() : data;
    }

    public void setDefaultValue(MethodVisitor mWriter, Type returnType) {

        int typeSort = returnType.getSort();

        if (typeSort == Type.VOID) {
        } else if (typeSort == Type.BOOLEAN) {
//       ireturn、lreturn、freturn、dreturn、areturn
            //lreturn和dreturn,操作数栈中弹出两位
            mWriter.visitLdcInsn(Boolean.FALSE);
        } else if (typeSort == Type.BYTE) {
            mWriter.visitLdcInsn((byte) 0);
        } else if (typeSort == Type.SHORT) {
            mWriter.visitLdcInsn((short) 0);
        } else if (typeSort == Type.CHAR) {
            mWriter.visitLdcInsn('0');
        } else if (typeSort == Type.INT) {
            mWriter.visitLdcInsn(0);
        } else if (typeSort == Type.LONG) {
            mWriter.visitLdcInsn(0L);
        } else if (typeSort == Type.FLOAT) {
            mWriter.visitLdcInsn(0.0F);
        } else if (typeSort == Type.DOUBLE) {
            mWriter.visitLdcInsn(0.0D);
        } else if (typeSort >= Type.ARRAY) {
            //对象类型
            mWriter.visitInsn(ACONST_NULL);
        }
    }

    protected boolean isInclude(String str) {
        return isMatched(str, true, includeClasses);
    }

    protected boolean isExclude(String str) {
        return isMatched(str, false, excludeClasses);
    }

    protected boolean isMatched(String str, boolean defaultValue, String... patterns) {

        if (patterns == null
                || patterns.length == 0) {
            return defaultValue;
        }
        return Arrays.stream(patterns)
                .filter(StringUtils::hasText)
                .anyMatch(pattern -> antPathMatcher.match(pattern, str));
    }


}