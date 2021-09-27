package com.levin.commons.plugins.jni;

import lombok.SneakyThrows;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;


/**
 * AgentTransformer
 * jvm加载class时回调
 * <p>
 * 当有多个转换器时，转换由链接转换调用组成。也就是说，一个转换调用返回的字节数组将成为下一个调用的输入(通过classfileBuffer参数)。
 * <p>
 * 关于transform输入的classfileBuffer参数：
 * 如果实现方法确定不需要转换，则返回null。否则，它应该创建一个新的byte[]数组，将输入classfileBuffer连同所有所需的转换复制到其中，并返回新数组。不能修改输入classfileBuffer。
 *
 * @author roseboy
 */
public class SimpleLoaderAndTransformer extends ClassLoader implements ClassFileTransformer {

    static {
        loadLib();
    }

    private static final String LIB_NAME = "HookAgent";
    private static final String LIB_PREFIX = "lib";

    @SneakyThrows
    private static void loadLib() {

        String fileName = LIB_PREFIX + LIB_NAME;

        String osName = System.getProperty("os.name", "").toLowerCase().replace(" ", "");

        if (osName.contains("linux".toLowerCase())) {
            fileName = "linux/" + fileName + ".so";
        } else if (osName.contains("windows".toLowerCase())) {
            fileName = "windows/" + fileName + ".dll";
        } else if (osName.contains("MacOSX".toLowerCase())) {
            fileName = "macosx/" + fileName + ".dylib";
        } else {
            System.loadLibrary(LIB_NAME);
        }

        if (!fileName.contains("/")
                || !fileName.contains(".")) {
            return;
        }

        fileName = LIB_PREFIX + "/" + LIB_NAME + "/" + fileName;

        byte[] data = JniHelper.loadResource(SimpleLoaderAndTransformer.class.getClassLoader(), fileName);

        if (data == null) {
            //System.err.println("Warning:");
            return;
        }

        //隐藏目录
        File outLibFile = new File("." + fileName);

        //创建目录
        outLibFile.getParentFile().mkdirs();

        int n = 200;
        while (outLibFile.exists() && n-- > 0) {
            outLibFile.delete();
        }

        JniHelper.writeByteArrayToFile(outLibFile, data);

        outLibFile.setLastModified(System.currentTimeMillis());
        outLibFile.setExecutable(true);

        if (outLibFile.exists()) {
            //加载文件
            System.load(outLibFile.getCanonicalPath());
        }

    }

    protected static final int JNI = 1;
    protected static final int AGENT = 2;


    protected SimpleLoaderAndTransformer(ClassLoader parent) {
        super(parent);
    }

    protected SimpleLoaderAndTransformer() {
        super(Thread.currentThread().getContextClassLoader());
    }

    protected native static void setPwd(String pwd, String pwdFileName);

    /**
     * 获取环境类型
     * <p>
     * 0、未定义
     * 1、JNI
     * 2、Agent
     *
     * @return
     */
    protected native static int getEnvType();

    protected native static byte[] transform1(String pwd, byte[] data);

    protected native static byte[] transform2(String pwd, byte[] data);

    public native static byte[] encryptAes(int bits, String pwd, byte[] data);

    public native static byte[] decryptAes(int bits, String pwd, byte[] data);

    @Override
    public native byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classBuffer) throws IllegalClassFormatException;

    @Override
    protected native Class<?> findClass(String name) throws ClassNotFoundException;

}
