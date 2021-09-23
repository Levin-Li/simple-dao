package com.levin.commons.plugins.jni;

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

        // System.loadLibrary("HookAgent");

        System.load("/Users/llw/open_source/JniHelpers/build/src/HookAgent/cpp/libHookAgent.dylib");

    }


    protected SimpleLoaderAndTransformer(ClassLoader parent) {
        super(parent);
    }

    protected SimpleLoaderAndTransformer() {
        super(Thread.currentThread().getContextClassLoader());
    }

    protected native static void setPwd(String pwd, String pwdFileName);

    public native static int getEnvType();

    protected native static byte[] transform1(String pwd, byte[] data);

    protected native static byte[] transform2(String pwd, byte[] data);

    public native static byte[] encryptAes(int bits, String pwd, byte[] data);

    public native static byte[] decryptAes(int bits, String pwd, byte[] data);

    @Override
    public native byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classBuffer) throws IllegalClassFormatException;

    @Override
    protected native Class<?> findClass(String name) throws ClassNotFoundException;

}
