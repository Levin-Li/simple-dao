package com.levin.commons.plugins.javaagent;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.instrument.ClassFileTransformer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * AgentTransformer
 * jvm加载class时回调
 *
 * @author roseboy
 */
public class ClassTransformer implements ClassFileTransformer {

    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";//默认的加密算法

    public static final String DEFAULT_IV = "SimpleDao-AES-IV";

    public static final String UTF_8 = "UTF-8";

    public static final String META_INF_CLASSES = "META-INF/.cache_cls_data/";

    private static final Map<String, String> md5Caches = new ConcurrentHashMap<>();

    private char[] pwd;

    /**
     * 构造方法
     *
     * @param pwd 密码
     */
    public ClassTransformer(char[] pwd) {
        this.pwd = pwd;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain domain, byte[] classBuffer) {

        if (className == null || loader == null || pwd == null || pwd.length == 0) {
            return classBuffer;
        }

        //获取类所在的项目运行路径
        //String projectPath = domain.getCodeSource().getLocation().getPath();

        className = className.replace("/", ".").replace("\\", ".");

        String resName = getMd5(className);

        // System.out.println(new String(pwd) + " , " + className + " --> classBeingRedefined:" + classBeingRedefined + " ,domain = " + domain);

        InputStream inputStream = loader.getResourceAsStream(META_INF_CLASSES + resName);

        //如果资源存在
        if (inputStream != null) {
            try {
                //从字段读取内容
//                classBuffer = decryptByAes128((byte[]) classBeingRedefined.getDeclaredField(className).get(null), new String(pwd));
                classBuffer = decryptByAes128(readAndClose(inputStream), new String(pwd));

            } catch (Throwable ex) {
                ex.printStackTrace();
            } finally {
                close(inputStream);
            }
        }

        return classBuffer;
    }

    private void close(InputStream inputStream) {
        try {
            inputStream.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    public static String getMd5(final String data) {

        String md5 = md5Caches.get(data);

        if (md5 == null) {

            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] digest = messageDigest.digest(data.getBytes(Charset.forName("UTF-8")));

            StringBuilder stringBuilder = new StringBuilder();

            for (byte aByte : digest) {
                stringBuilder.append(Integer.toHexString(0xFF & aByte));
            }

            if (stringBuilder.length() > 0) {
                md5 = "C" + stringBuilder.toString();
                md5Caches.put(data, md5);
            }
        }

        return md5;
    }

    @SneakyThrows
    public static byte[] readAndClose(InputStream inputStream) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buf = new byte[8192];

        int n = -1;

        try {
            while ((n = inputStream.read(buf)) > -1) {
                if (n > 0) {
                    outputStream.write(buf, 0, n);
                }
            }
        } finally {
            inputStream.close();
        }

        return outputStream.toByteArray();
    }

    @SneakyThrows
    public static byte[] decryptByAes128(byte[] content, String password) {
        return doCipher(AES_CBC_PKCS5_PADDING, 128, Cipher.DECRYPT_MODE, content, password, DEFAULT_IV);
    }

    @SneakyThrows
    public static byte[] encryptByAes128(byte[] content, String password) {
        return doCipher(AES_CBC_PKCS5_PADDING, 128, Cipher.ENCRYPT_MODE, content, password, DEFAULT_IV);
    }

    @SneakyThrows
    public static byte[] doCipher(String algorithm, int bitSize, int mode, byte[] content, String password, String iv) {
        //创建密码器
        Cipher cipher = Cipher.getInstance(algorithm);

        //algorithm = "AES/CBC/PKCS5Padding"

        int index = algorithm.indexOf("/");

        if (index > 0) {
            algorithm = algorithm.substring(0, index);
        }

        //美国没有出口 256 位 加密算法
        //密码key(超过16字节即128bit的key，需要替换jre中的local_policy.jar和US_export_policy.jar，否则报错：Illegal key size)
        SecretKeySpec keySpec = new SecretKeySpec(autoFillOrTrim(bitSize, password.getBytes(UTF_8)), algorithm);

        //向量iv
        IvParameterSpec ivParameterSpec = new IvParameterSpec(autoFillOrTrim(bitSize, iv.getBytes(UTF_8)));

        //初始化，不使用随意
        cipher.init(mode, keySpec, ivParameterSpec, null);

        return cipher.doFinal(content);
    }


    /**
     * 自动填充或是截断
     *
     * @param bitSize
     * @param data
     * @return
     */
    public static byte[] autoFillOrTrim(int bitSize, byte[] data) {

        int len = bitSize / 8;

        if (data.length < len) {

            byte[] temp = new byte[len];

            Arrays.fill(temp, (byte) 33);

            System.arraycopy(data, 0, temp, 0, data.length);

            data = temp;

        } else if (data.length > len) {
            return Arrays.copyOf(data, len);
        }

        return data;
    }

    /**
     * 获取class运行的classes目录或所在的jar包目录
     *
     * @return 路径字符串
     */
    public static String getRootPath(String path) {

        if (path == null) {
            path = ClassTransformer.class.getResource("").getPath();
        }

        try {
            path = java.net.URLDecoder.decode(path, "utf-8");
        } catch (UnsupportedEncodingException e) {
        }

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

    public static void checkSecurity() {
        throw new SecurityException("not permission");
    }

}
