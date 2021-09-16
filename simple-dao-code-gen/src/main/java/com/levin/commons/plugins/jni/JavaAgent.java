package com.levin.commons.plugins.jni;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.jar.JarFile;

/**
 * MANIFEST.MF 文件配置
 * Agent-Class: sun.management.Agent
 * Premain-Class: sun.management.Agent
 */
public class JavaAgent {

    private static Instrumentation instrumentation;

    private static final String DISABLE_DEBUG_OPTION = "-XX:+DisableAttachMechanism";

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

//    public static void premain(String agentArgs, Instrumentation inst);[1]
//    public static void premain(String agentArgs);[2]


//    The JVM Tool Interface (JVMTI) 是一个由JVM提供的用于开发针对Java程序开发与监控工具的编程接口，通过JVMTI接口（Native API）可以创建代理程序（Agent）以监视和控制 Java 应用程序，包括剖析、调试、监控、分析线程等。
//jvmtiEventClassFileLoadHook ClassFileLoadHook;
//    /*   55 : Class Load */
//    jvmtiEventClassLoad ClassLoad;
//    /*   56 : Class Prepare */
//    jvmtiEventClassPrepare ClassPrepare;

    /**
     * man方法执行前调用
     * <p>
     * 一个java程序中-javaagent这个参数的个数是没有限制的，所以可以添加任意多个java agent。
     * <p>
     * java -javaagent:MyAgent1.jar -javaagent:MyAgent2.jar -jar MyProgram.jar
     *
     * @param agentArgs 参数
     * @param inst      inst
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        agentmain(agentArgs, inst);
    }

    /**
     * agentmain启动代理的方式：
     * <p>
     * MANIFEST.MF 文件中配置 Agent-Class: sun.management.Agent
     * <p>
     * 先通过VirtualMachine.attach(targetVmPid)连接到虚拟机，然后通过virtualmachine.loadAgent(jmxAgent, "com.sun.management.jmxremote");注册agent代理类。
     *
     * @param agentArgs
     * @param inst
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {

//        1）ClassFileTransformer：定义了类加载前的预处理类，可以在这个类中对要加载的类的字节码做一些处理，譬如进行字节码增强
//        2）Instrutmentation：增强器，由JVM在入口参数中传递给我们，提供了如下的功能
//        addTransformer/ removeTransformer：注册/删除ClassFileTransformer
//        retransformClasses：对于已经加载的类重新进行转换处理，即会触发重新加载类定义，需要注意的是，新加载的类不能修改旧有的类声明，譬如不能增加属性、不能修改方法声明
//        redefineClasses：与如上类似，但不是重新进行转换处理，而是直接把处理结果(bytecode)直接给JVM
//        getAllLoadedClasses：获得当前已经加载的Class，可配合retransformClasses使用
//        getInitiatedClasses：获得由某个特定的ClassLoader加载的类定义
//        getObjectSize：获得一个对象占用的空间，包括其引用的对象
//        appendToBootstrapClassLoaderSearch/appendToSystemClassLoaderSearch：增加BootstrapClassLoader/SystemClassLoader的搜索路径
//        isNativeMethodPrefixSupported/setNativeMethodPrefix：支持拦截Native Method

        instrumentation = inst;

        File resDir = new File(".java_agent");

        if (resDir.exists()) {
            Arrays.stream(resDir.listFiles())
                    .filter(file -> file.isFile() && !file.isDirectory())
                    .filter(file -> file.getName().toLowerCase().endsWith(".jar"))
                    .filter(file -> file.length() > 0)
                    .forEach(file -> {
                        try {
                            //1、BootstrapClassLoader
                            //2、ExtClassLoader
                            //3、AppSystemClassLoader
                            instrumentation.appendToSystemClassLoaderSearch(new JarFile(file.getCanonicalPath(), false));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }

        String fileName = agentArgs;

        if (fileName == null || fileName.trim().length() == 0) {
            fileName = ".java_agent/.pwdFile.txt";
        }

        File file = new File(fileName);

        //有密码才加入处理
        if (file.exists() && file.length() > 0) {
            //如果有密码，则确保禁止调试
            boolean disableDebug = ManagementFactory.getRuntimeMXBean().getInputArguments().contains(DISABLE_DEBUG_OPTION);

            if (!disableDebug) {
                System.out.println("启动参数有误，请检查参数是否包含:" + DISABLE_DEBUG_OPTION);
                System.exit(1);
            } else {
                instrumentation.addTransformer(new SimpleClassFileTransformer(), true);
            }
        } else {

        }
    }


}
