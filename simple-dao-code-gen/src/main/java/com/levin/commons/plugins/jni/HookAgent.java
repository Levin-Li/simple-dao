package com.levin.commons.plugins.jni;


import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.stream.Collectors;

public abstract class HookAgent {

    public static final String META_INF_CLASSES = "META-INF/.cache_data/";

    private static final String DISABLE_DEBUG_OPTION = "-XX:+DisableAttachMechanism";
    private static final String JAVA_AGENT_PREFIX = "-javaagent:";

    private static final String AGENT_LIB_PREFIX = "-agentlib:";
    private static final String AGENT_PATH_PREFIX = "-agentpath:";

    public static final String DEFAULT_PWD ="09_HO#$%&^@OK_21";
    public static final String DEFAULT_PWD2 ="#$%&^@OK_2109_HO";

    private HookAgent() {
    }

    public static byte[] loadClassData(String className) {
        return JniHelper.loadResource(getClassResPath(className));
    }

    public static String getClassResPath(String className) {
        return (META_INF_CLASSES + JniHelper.md5(new StringBuilder("C" + className.replace('/', '.') + ".dat").reverse().toString()));
    }

    public static boolean isEnvEnable() {


        List<String> inputArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();

        if (!inputArguments.contains(DISABLE_DEBUG_OPTION)) {
            System.out.println("" + DISABLE_DEBUG_OPTION + " arg must be exists.");
            return false;
        }

        if (inputArguments.stream()
                .anyMatch(arg -> arg.startsWith(AGENT_LIB_PREFIX))) {
            System.out.println("not allow " + AGENT_LIB_PREFIX + " arg.");
            return false;
        }

        long agentCnt = inputArguments.stream()
                .filter(arg -> arg.startsWith(AGENT_PATH_PREFIX)).count();

        if (agentCnt > 1) {
            System.out.println("only allow " + AGENT_PATH_PREFIX + " arg once.");
            return false;
        }

        List<File> javaAgentJars = inputArguments.stream().filter(arg -> arg.startsWith(JAVA_AGENT_PREFIX))
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
            System.out.println("only allow " + JAVA_AGENT_PREFIX + " arg once.");
        }

        //不允许同时大于 0

        return !(agentCnt > 0 && javaAgentJars.size() > 0);
    }

}
