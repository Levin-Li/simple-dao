package com.levin.commons.plugins.jni;


import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 本类会被加密
 */
public abstract class HookAgent {

    //加密资源列表
    public static final String MF_ENCRYPT_RES_LIST = "META-INF/MANIFEST.ERL";

    public static final String UTF8 = "utf-8";

    public static final String META_INF_CLASSES = "META-INF/.cache_data/";

    private static final String DISABLE_DEBUG_OPTION = "-XX:+DisableAttachMechanism";
    private static final String JAVA_AGENT_PREFIX = "-javaagent:";

    private static final String AGENT_LIB_PREFIX = "-agentlib:";
    private static final String AGENT_PATH_PREFIX = "-agentpath:";

    public static final String DEFAULT_KEY = "09_HO#$%&^@OK_21";

    public static final String DEFAULT_KEY2 = "#$%&^@OK_2109_HO";

    private static Boolean isPrintLog = null;

    private static String fileHashcode = null;

    private static SortedSet<String> encryptedList = null;

    private HookAgent() {
    }

    /**
     * 快速确认资源是否存在
     *
     * @param loader
     * @param resName
     * @return
     */
    public static byte[] loadEncryptedRes(ClassLoader loader, String resName) {

        if (loader == null) {
            loader = Thread.currentThread().getContextClassLoader();
        }

        if (loader == null) {
            loader = HookAgent.class.getClassLoader();
        }

        synchronized (MF_ENCRYPT_RES_LIST) {
            if (encryptedList == null) {

                Enumeration<URL> resources = null;

                try {
                    resources = loader.getResources(MF_ENCRYPT_RES_LIST);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }

                encryptedList = new TreeSet<>();

                while (resources.hasMoreElements()) {
                    try {
                        URL url = resources.nextElement();

                        if (isPrintLog()) {
                            System.out.println("load list res:" + url);
                        }

                        String[] resList = new String(JniHelper.readAndClose(url.openStream()), Charset.forName(UTF8)).split("\\n");

                        Arrays.stream(resList)
                                .filter(str -> str != null && str.trim().length() > 0)
                                .forEachOrdered(encryptedList::add);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (isPrintLog()) {
                    System.out.println("encryptedList:" + encryptedList);
                }
            }
        }

        if (encryptedList == null
                || !encryptedList.contains(resName)) {
            return null;
        }

        return JniHelper.loadResource(loader, resName);

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

        if (fileHashcode == null
                || fileHashcode.trim().length() == 0) {
            //获取文件路径
            URL url = HookAgent.class.getClassLoader().getResource(HookAgent.class.getName().replace(".", "/") + ".class");

            //获取文件哈希值
            File file = new File(getRootPath(url.toString()));

            fileHashcode = (file.exists() && file.isFile()) ? toHexStr(getFileSHA256Hashcode(file)) : "";

            if (isPrintLog()) {
                System.out.println(file.getName() + " hash code: " + fileHashcode);
            }

        }

        if (SimpleLoaderAndTransformer.getEnvType(fileHashcode) == SimpleLoaderAndTransformer.AGENT
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
        return (META_INF_CLASSES + JniHelper.md5("C" + className.replace('/', '.')) + ".dat");
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

    public static int replace(byte[] data, byte[] target, byte[] replacement) {

        if (target.length != replacement.length
                || data.length < target.length) {
            return 0;
        }

        int count = 0;

        for (int i = 0; i < data.length; i++) {

            //如果长度不够
            if (data.length - i < data.length) {
                break;
            }

            int j = 0;

            for (; j < target.length; j++) {
                if (data[i + j] != target[j]) {
                    break;
                }
            }

            //匹配成功
            if (j >= target.length) {
                count++;
                //替换内容
                for (byte n : replacement) {
                    data[i++] = n;
                }
            }

        }

        return count;
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
