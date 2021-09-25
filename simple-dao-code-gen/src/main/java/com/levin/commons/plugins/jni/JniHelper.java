package com.levin.commons.plugins.jni;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Arrays;

public abstract class JniHelper {

    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";//默认的加密算法

    public static final String DEFAULT_IV = "JniHelper-AES-IV";

    public static final String UTF_8 = "UTF-8";

    private JniHelper() {
    }

    public static void checkSecurity() {
        throw new SecurityException("unsafe");
    }

    public static ClassLoader getCurrentThreadClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    /**
     * @param resName
     * @return
     */
    public static byte[] loadResource(String resName) {
        return readAndClose(getCurrentThreadClassLoader().getResourceAsStream(resName));
    }

    public static byte[] loadData(Class clazz) {
        return loadResource(clazz.getName().replace('.', '/') + ".class");
    }

    @SneakyThrows
    public static String md5(final String data) {

        MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        byte[] digest = messageDigest.digest(data.getBytes(Charset.forName("UTF-8")));

        StringBuilder stringBuilder = new StringBuilder();

        for (byte aByte : digest) {
            stringBuilder.append(Integer.toHexString(0xFF & aByte));
        }

        return stringBuilder.toString();
    }

    /**
     * @param inputStream
     * @return
     */
    public static byte[] readAndClose(InputStream inputStream) {

        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buf = new byte[8192];

        int n = -1;

        try {
            while ((n = inputStream.read(buf)) > -1) {
                if (n > 0) {
                    outputStream.write(buf, 0, n);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return outputStream.toByteArray();
    }


    @SneakyThrows
    public static byte[] decryptByAes128(String password, byte[] content) {
        return doCipher(AES_CBC_PKCS5_PADDING, 128, Cipher.DECRYPT_MODE, password, DEFAULT_IV.getBytes(UTF_8), content);
    }

    @SneakyThrows
    public static byte[] encryptByAes128(String password, byte[] content) {
        return doCipher(AES_CBC_PKCS5_PADDING, 128, Cipher.ENCRYPT_MODE, password, DEFAULT_IV.getBytes(UTF_8), content);
    }

    @SneakyThrows
    public static byte[] doCipher(String algorithm, int bitSize, int mode, String password, byte[] iv, byte[] content) {
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
        IvParameterSpec ivParameterSpec = null;

        if (iv != null && iv.length > 0) {
            ivParameterSpec = new IvParameterSpec(autoFillOrTrim(bitSize, iv));
        }

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

}
