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
        System.load("/Users/llw/open_source/JniHelpers/build/src/HookAgent/cpp/libHookAgent.dylib");
    }

    private final ClassLoader proxy;

    protected SimpleLoaderAndTransformer(ClassLoader parent) {
        super(null);
        proxy = parent != null ? parent : getSystemClassLoader();
    }

    protected SimpleLoaderAndTransformer() {
        super(null);
        proxy = getSystemClassLoader();
    }

    protected native static void setPwd(String pwd, String pwdFileName);

    public native static int getEnvType();

    protected native static byte[] transform1(String random, byte[] data);

    protected native static byte[] transform2(String random, byte[] data);

    public native static byte[] encryptAes(int bits, String pwd, byte[] data);

    public native static byte[] decryptAes(int bits, String pwd, byte[] data);

    @Override
    public native byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classBuffer) throws IllegalClassFormatException;

    @Override
    protected final Class<?> findClass(String name) throws ClassNotFoundException {

        Class<?> c = null;

        try {
            //先找本地
            c = findClassByNative(getClass().getClassLoader(), name);

            if (c != null) {
                return c;
            }

        } catch (ClassNotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        c = proxy != null ? proxy.loadClass(name) : null;

        if (c != null) {
            return c;
        }

        throw new ClassNotFoundException(name);
    }

    /**
     * @param name
     * @return
     * @throws ClassNotFoundException
     */
    protected native Class<?> findClassByNative(ClassLoader loader, String name) throws ClassNotFoundException;

    public static void main(String[] args) {

        SimpleLoaderAndTransformer loader = new SimpleLoaderAndTransformer();

    }
}
