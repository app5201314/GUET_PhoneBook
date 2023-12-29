package com.lcx.server.view;

import com.lcx.phoneBook.PhoneBook;
import com.lcx.server.utils.AESUtils;

import java.io.*;
import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.TreeMap;

/**
 * @author lcx
 * @version 1.0
 */
public class Test {
    public static void main(String[] args) throws IOException {
        TreeMap<String, PhoneBook> map = new TreeMap<String, PhoneBook>();
        //将map序列化
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(map);
        oos.close();
        byte[] bytes = bos.toByteArray();

        // 使用AES加密字节流
        byte[] encryptedBytes = AESUtils.encrypt(bytes);
        //写入文件
        // 将加密后的字节流写入到文件中
         FileOutputStream fos = new FileOutputStream("Server//src//resource//phoneBook//app");
         fos.write(encryptedBytes);
            fos.close();
            // 从文件中读取字节流
            FileInputStream fis = new FileInputStream("Server//src//resource//phoneBook//app");
            byte[] encryptedBytes2 = new byte[fis.available()];
            fis.read(encryptedBytes2);
            fis.close();
            // 使用AES解密字节流
            byte[] decryptedBytes = AESUtils.decrypt(encryptedBytes2);
        //比较两个字节数组是否相等
        System.out.println(Arrays.equals(bytes,decryptedBytes));
    }
}

