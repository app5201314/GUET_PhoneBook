package com.lcx.server.utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * AES工具
 */
public class AESUtils {
    @SuppressWarnings("SpellCheckingInspection")
    private static final String key = "appstrwrfwwfwrw1";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String dv = "qwscfghytfsdvbnm";

    public static byte[] encrypt(byte[] plaintext) {
        return encrypt(plaintext, key.getBytes(), dv.getBytes());
    }

    public static byte[] decrypt(byte[] ciphertext) {
        return decrypt(ciphertext, key.getBytes(), dv.getBytes());
    }

    //加密函数
    public static String encrypt(String plaintext) {
        //将密码加密
        return encryptHex(plaintext, key, dv);
    }

    //解密函数
    public static String decrypt(String ciphertext) {
        //将密码解密
        return decryptHex(ciphertext, key, dv);
    }

    /**
     * 检查参数
     *
     * @param plaintext 明文（UTF-8字符串）
     * @param key       密钥
     * @param iv        向量（16位）
     * @return True：参数错误，False：参数正确
     */
    public static boolean isParamsFalse(String plaintext, String key, String iv) {
        // 检查参数，去除空格
        if (plaintext == null || plaintext.trim().replaceAll("\\s", "").isEmpty()) {
            return true;// 明文为空
        }

        if (key == null || key.trim().replaceAll("\\s", "").isEmpty()) {
            return true;// 密钥为空
        }

        return iv == null || (iv = iv.trim().replaceAll("\\s", "")).isEmpty() || iv.length() != 16;// 向量为空或长度不为16
    }

    /**
     * AES加密（16进制）
     *
     * @param plaintext 明文（UTF-8字符串）
     * @param key       密钥
     * @param iv        向量（16位）
     * @return 密文（16进制）
     * 将字节数组转化为十六进制字符串是一种常见的做法，可以提高数据的可读性，传输安全性，兼容性和存储方便性。
     * 在您选择的代码中，\\s是一个正则表达式，表示空白字符。replaceAll("\\s", "")将删除所有空白字符。
     */
    public static String encryptHex(String plaintext, String key, String iv) {
        // 检查参数，去除空格
        if (isParamsFalse(plaintext, key, iv)) {
            return null;
        }

        // 加密，getBytes("utf-8")：将字符串转换成字节数组
        return bytes2HexStr(encrypt(plaintext.getBytes(StandardCharsets.UTF_8), key.getBytes(StandardCharsets.UTF_8), iv.getBytes(StandardCharsets.UTF_8)));

    }

    /**
     * AES解密（16进制）
     *
     * @param ciphertext 密文（16进制）
     * @param key        密钥
     * @param iv         向量（16位）
     * @return 明文（UTF-8字符串）
     */
    public static String decryptHex(String ciphertext, String key, String iv) {
        // 检查参数
        if(isParamsFalse(ciphertext, key, iv)) {
            return null;
        }

        return new String(decrypt(hexStr2Bytes(ciphertext), key.getBytes(StandardCharsets.UTF_8), iv.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * 构造AES密钥生成器
     *
     * @param key 密钥
     * @return AES密钥
     */
    public static SecretKey getKeyGenerator(byte[] key) throws NoSuchAlgorithmException {
        // 构造密钥生成器，指定为 AES 算法,不区分大小写
        KeyGenerator gen = KeyGenerator.getInstance("AES");
        // 新增下面两行，处理 Linux 操作系统下随机数生成不一致的问题
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");// SHA1PRNG 强随机种子算法, 要区别4.2以上版本的调用方法

        random.setSeed(key);//将用户指定的密钥 作为随机数种子
        gen.init(128, random); // AES算法要求密钥长度为128位、192位或256位

        // 根据字节数组生成AES密钥，getEncoded()方法：返回基本编码格式的密钥，如果此密钥不支持编码，则返回 null
        return new SecretKeySpec(gen.generateKey().getEncoded(), "AES");
    }

    /**
     * AES加密
     *
     * @param plaintext 明文
     * @param key       密钥
     * @param iv        向量
     * @return 密文
     */
    public static byte[] encrypt(byte[] plaintext, byte[] key, byte[] iv) {
        try {
            // 根据字节数组生成AES密钥
            SecretKey secretKey = getKeyGenerator(key);
            // 根据指定算法AES自成密码器，密码器是用来加密和解密的工具，其内部包含了加密和解密算法
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");//这里指定加密算法的模式为CBC模式，PKCS5Padding是填充模式
            // 初始化密码器（向量必须是16位）
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // 返回密文
            return cipher.doFinal(plaintext);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * AES解密
     *
     * @param ciphertext 密文
     * @param key        密钥
     * @param iv         向量
     * @return 明文
     */
    public static byte[] decrypt(byte[] ciphertext, byte[] key, byte[] iv) {

        try {
            // 根据字节数组生成AES密钥
            SecretKey secretKey = getKeyGenerator(key);

            // 根据指定算法AES自成密码器
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            // 初始化密码器（向量必须是16位）
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            // 返回明文
            return cipher.doFinal(ciphertext);
        } catch (InvalidAlgorithmParameterException | NoSuchPaddingException | IllegalBlockSizeException |
                 NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 字节 --> 十六进制字符串
     *
     * @param bytes 字节
     * @return 十六进制字符串
     */
    public static String bytes2HexStr(byte[] bytes) {
        // 检查字节是否为空
        if (bytes.length < 1)
            return null;

        StringBuilder sb = new StringBuilder();

        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);

            if (hex.length() == 1) {// 如果是1位，补0
                hex = '0' + hex;
            }

            sb.append(hex.toUpperCase());
        }

        return sb.toString();
    }

    /**
     * 十六进制字符串 --> 字节
     *
     * @param hexStr 十六进制字符串
     * @return 字节
     */
    public static byte[] hexStr2Bytes(String hexStr) {
        // 检查十六进制字符串是否为空
        if (hexStr == null || hexStr.trim().isEmpty())
            return null;

        byte[] result = new byte[hexStr.length() / 2];

        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }

        return result;
    }
}