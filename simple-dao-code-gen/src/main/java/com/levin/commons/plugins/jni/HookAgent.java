package com.levin.commons.plugins.jni;


import java.io.File;
import java.lang.management.ManagementFactory;
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

    private HookAgent() {

    }

    /**
     * 其它地方有引用，别删
     */
    public static void checkSecurity() {
        //nothing to do
    }

    public static void checkEnv() {
        if (SimpleLoaderAndTransformer.getEnvType() != SimpleLoaderAndTransformer.AGENT
                && !isEnvEnable()) {
            System.err.println("env type error");
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

    static {
        checkEnv();
    }

}
