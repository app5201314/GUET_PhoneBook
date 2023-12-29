package com.lcx.phoneBook;

import com.lcx.server.utils.AESUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author lcx
 * @version 1.0
 */
public class PhoneBooks implements Serializable {
    private static final String path = "//root//phoneBook//phoneBooks";
//    private static final String path = "Server//src//resource//phoneBooks";
    private static Map<String, PhoneBook> phoneBooks = null;

    public static Map<String, PhoneBook> getPhoneBooks() {
        return phoneBooks;
    }

    public static void setPhoneBooks(Map<String, PhoneBook> phoneBooks) {
        PhoneBooks.phoneBooks = phoneBooks;
    }

    // 从文件中加载通讯录
    public static void load() {
        try {
            // 读取文件中的字节流
            Path path1 = Paths.get(path);
            // 如果文件不存在，则创建一个空的通讯录文件
            if (!Files.exists(path1)) {
                save();
            }

            byte[] encryptedBytes = Files.readAllBytes(path1);

            // 使用AES解密字节流
            byte[] bytes = AESUtils.decrypt(encryptedBytes);

            // 将字节流反序列化为对象
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            @SuppressWarnings("unchecked")
            Map<String, PhoneBook> phoneBooks = (ConcurrentMap<String, PhoneBook>) ois.readObject();
            ois.close();
            setPhoneBooks(phoneBooks);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("all")
    public synchronized static void save() {
        if (phoneBooks == null) {
            phoneBooks = new ConcurrentHashMap<>();
        }

        File file = new File(path);
        try {
            // 将contacts对象序列化为字节流
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(phoneBooks);
            oos.close();
            byte[] bytes = bos.toByteArray();

            // 使用AES加密字节流
            byte[] encryptedBytes = AESUtils.encrypt(bytes);

            // 将加密后的字节流写入到文件中
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(encryptedBytes);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}