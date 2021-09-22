package com.levin.commons.plugins.jni;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

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
public class SimpleClassFileTransformer implements ClassFileTransformer {

    static {
        System.load("/Users/llw/open_source/JniHelpers/build/src/HookAgent/cpp/libHookAgent.dylib");
    }

    protected native static void setPwd(String pwd, String pwdFileName);

    public native static int getEnvType();

    protected native static byte[] transform1(String random, byte[] data);

    protected native static byte[] transform2(String random, byte[] data);

    public native static byte[] encryptAes(int bits, String pwd, byte[] data);

    public native static byte[] decryptAes(int bits, String pwd, byte[] data);

    @Override
    public native byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classBuffer) throws IllegalClassFormatException;


    @SneakyThrows
    public static void main(String[] args) {

        final SimpleClassFileTransformer transformer = new SimpleClassFileTransformer();

        String info = "我是中国人，我来自福建福州。";

//        info ="this is test data";

        byte[] data = info.getBytes();

        String pwd = "我是-密码-我是密码-我是密码-我是密码-我是密码-我是密码";
//          pwd = "我码-";
//          pwd = "1234567890123456";

        setPwd(pwd, "");

        data = JniHelper.loadData(SimpleClassFileTransformer.class);

        data = encryptAes(-1, pwd, data);

        File file = new File("SimpleClassFileTransformer.class.e");

        if (!file.exists()) {
            FileUtils.writeByteArrayToFile(file, data);
        } else {
            data = FileUtils.readFileToByteArray(file);
        }

        byte[] tempD = decryptAes(-1, pwd, data);
        tempD = decryptAes(-1, pwd, data);

        FileUtils.writeByteArrayToFile(new File("SimpleClassFileTransformer.class"), tempD);

        byte[] transformResult = transformer.transform(null, "aaaa", null, null, data);

        tempD = decryptAes(-1, pwd, data);
        tempD = decryptAes(-1, pwd, data);

        data = decryptAes(-1, pwd, data);

        String resule = new String(data);

        System.out.println("decrypt result : " + info.equals(resule));

        data = transform1(pwd, data);

        data = transform2(pwd, data);

    }

}
