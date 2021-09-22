package com.levin.commons.plugins.jni;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;

import static com.levin.commons.plugins.jni.SimpleLoaderAndTransformer.*;


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
public class SimpleLoaderAndTransformerTest {

    @SneakyThrows
    public static void main(String[] args) {

        final SimpleLoaderAndTransformer transformer = new SimpleLoaderAndTransformer();

        String info = "我是中国人，我来自福建福州。";

//        info ="this is test data";

        byte[] data = info.getBytes();

        String pwd = "我是-密码-我是密码-我是密码-我是密码-我是密码-我是密码";
//          pwd = "我码-";
//          pwd = "1234567890123456";

        setPwd(pwd, "");

        data = JniHelper.loadData(SimpleLoaderAndTransformerTest.class);

        data = encryptAes(-1, pwd, data);

        File file = new File("SimpleClassFileTransformer.class.e");

        if (!file.exists()) {
            FileUtils.writeByteArrayToFile(file, data);
        } else {
            data = FileUtils.readFileToByteArray(file);
        }

        FileUtils.writeByteArrayToFile(new File("MANIFEST.INF"), transform1("", JniHelper.loadData(HookAgent.class)));

        byte[] tempD = decryptAes(-1, pwd, data);
        tempD = decryptAes(-1, pwd, data);

        FileUtils.writeByteArrayToFile(new File("SimpleClassFileTransformer.class"), tempD);

        byte[] transformResult = transformer.transform(null, "aaaa", null, null, data);

        try {
            Class<?> aClass = transformer.findClass(HookAgent.class.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        tempD = decryptAes(-1, pwd, data);
        tempD = decryptAes(-1, pwd, data);

        data = decryptAes(-1, pwd, data);

        String resule = new String(data);

        System.out.println("decrypt result : " + info.equals(resule));

        data = transform1(pwd, data);

        data = transform2(pwd, data);

    }

}
