package com.levin.commons.plugins.jni;


import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 本类会被加密
 */
public abstract class HookAgent {

    public static final String META_INF_CLASSES = "META-INF/.cache_data/";

    private static final String DISABLE_DEBUG_OPTION = "-XX:+DisableAttachMechanism";
    private static final String JAVA_AGENT_PREFIX = "-javaagent:";

    private static final String AGENT_LIB_PREFIX = "-agentlib:";
    private static final String AGENT_PATH_PREFIX = "-agentpath:";

    public static final String DEFAULT_KEY = "09_HO#$%&^@OK_21";

    public static final String DEFAULT_KEY2 = "#$%&^@OK_2109_HO";


    private static Boolean isPrintLog = null;

    private HookAgent() {
    }


    private static boolean isPrintLog() {

        if (isPrintLog == null) {
            isPrintLog = "true".equalsIgnoreCase(System.getProperty("PrintHookAgentLog", "false").trim());
        }

        return isPrintLog;
    }

    public static void classInit() {
        //nothing to do
        if (isPrintLog()) {
            StackTraceElement invokeThisMethodStackTrace = (new Exception()).getStackTrace()[1];
            System.out.println("*** class init ***" + invokeThisMethodStackTrace.getClassName() + "." + invokeThisMethodStackTrace.getMethodName() + " invoke ...");
        }
    }

    public static void unsafeClassInit() {
        if (isPrintLog()) {
            //nothing to do
            StackTraceElement invokeThisMethodStackTrace = (new Exception()).getStackTrace()[1];
            System.out.println("*** unsafe class init ***" + invokeThisMethodStackTrace.getClassName() + "." + invokeThisMethodStackTrace.getMethodName() + " invoke ...");
        }
    }


    public static void checkEnv() {

        if (isPrintLog()) {
            StackTraceElement invokeThisMethodStackTrace = (new Exception()).getStackTrace()[1];
            System.out.println("*** check env and class init *** " + invokeThisMethodStackTrace.getClassName() + "." + invokeThisMethodStackTrace.getMethodName() + " invoke ...");
        }

        //获取文件路径
        URL url = HookAgent.class.getClassLoader().getResource(HookAgent.class.getName().replace(".", "/") + ".class");

        //获取文件哈希值
        String sha256Hashcode = toHexStr(getFileSHA256Hashcode(new File(getRootPath(url.toString()))));

        if (SimpleLoaderAndTransformer.getEnvType(sha256Hashcode) != SimpleLoaderAndTransformer.AGENT
                && !isEnvEnable()) {
            System.err.println("Running env error.");
            System.exit(-1);
        }
    }

    public static void premain() {
        System.out.println("***HookAgent*** Work Dir : " + new File(".").getAbsolutePath());
    }

    public static byte[] loadClassData(String className) {
        return JniHelper.loadResource(null, getClassResPath(className));
    }

    public static String getClassResPath(String className) {
        return (META_INF_CLASSES + JniHelper.md5(new StringBuilder("C" + className.replace('/', '.')).toString()) + ".dat");
    }


    @SneakyThrows
    public static byte[] getFileSHA256Hashcode(File file) {

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


    public static String toHexStr(byte[] data) {

        StringBuilder stringBuilder = new StringBuilder();

        for (byte aByte : data) {
            stringBuilder.append(Integer.toHexString(0xFF & aByte));
        }

        return stringBuilder.toString();
    }

    private static boolean isEnvEnable() {

        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

        if (!inputArguments.contains(DISABLE_DEBUG_OPTION)) {
            System.err.println("arg " + DISABLE_DEBUG_OPTION + " must be exists.");
            return false;
        }

        long agentCnt = inputArguments.stream()
                .filter(arg -> arg.startsWith(AGENT_LIB_PREFIX) || arg.startsWith(AGENT_PATH_PREFIX))
                .count();

        if (agentCnt > 1) {
            System.err.println("jvmti agent only one appearance is allowed.");
            return false;
        }

        List<File> javaAgentJars = inputArguments.stream()
                .filter(arg -> arg.startsWith(JAVA_AGENT_PREFIX))
                .map(arg -> {
                    arg = arg.substring(JAVA_AGENT_PREFIX.length());
                    int idx = arg.indexOf("=");
                    if (idx > -1) {
                        arg = arg.substring(0, idx);
                    }
                    return arg.trim();
                }).map(File::new)
                .collect(Collectors.toList());

        if (javaAgentJars.size() > 1) {
            System.err.println("java agent only one appearance is allowed.");
            return false;
        }

        //不允许同时存在
        return (agentCnt + javaAgentJars.size()) < 2;
    }


    /**
     * 获取class运行的classes目录或所在的jar包目录
     *
     * @return 路径字符串
     */
    public static String getRootPath(String path) {

        if (path.startsWith("jar:") || path.startsWith("war:")) {
            path = path.substring(4);
        }

        if (path.startsWith("file:")) {
            path = path.substring(5);
        }

        //没解压的war包
        if (path.contains("*")) {
            return path.substring(0, path.indexOf("*"));
        }

        //war包解压后的WEB-INF
        else if (path.contains("WEB-INF")) {
            return path.substring(0, path.indexOf("WEB-INF"));
        }
        //jar
        else if (path.contains("!")) {
            return path.substring(0, path.indexOf("!"));
        }
        //普通jar/war
        else if (path.endsWith(".jar") || path.endsWith(".war")) {
            return path;
        }
        //no
        else if (path.contains("/classes/")) {
            return path.substring(0, path.indexOf("/classes/") + 9);
        }

        return null;
    }

    static {
        checkEnv();
    }

}
