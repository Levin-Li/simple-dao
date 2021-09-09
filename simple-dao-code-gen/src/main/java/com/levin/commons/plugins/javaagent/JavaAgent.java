package com.levin.commons.plugins.javaagent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.jar.JarFile;

/**
 * MANIFEST.MF 文件配置
 * Agent-Class: sun.management.Agent
 * Premain-Class: sun.management.Agent
 */
public class JavaAgent {

    private static Instrumentation instrumentation;

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

//    public static void premain(String agentArgs, Instrumentation inst);[1]
//    public static void premain(String agentArgs);[2]

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

        //系统参数种获取文件
        instrumentation.addTransformer(new ClassTransformer(readPwd()));

    }

    /**
     * @return
     */
    private static char[] readPwd() {

        char[] pwd = new char[0];

        try {
            String pwdFile = System.getProperty("pwdFile", ".java_agent/.pwdFile.txt");

            File file = new File(pwdFile);

            if (file.exists() && file.length() > 0) {

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("utf-8")));
                try {
                    pwd = bufferedReader.readLine().toCharArray();
                } finally {
                    bufferedReader.close();
                    file.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pwd;
    }

}
