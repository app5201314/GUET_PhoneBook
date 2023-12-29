package com.lcx.server.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author lcx
 * @version 1.0
 */
public class PropertiesUtils {
//    public static String propertiesPath = "Server//src//resource//user.properties";
    public static String propertiesPath = "//root//phoneBook//user.properties";

    public static void store(Properties props, String key, String value) {
        try {
            props.setProperty(key, HashUtils.hashWithSHA256(value));
            FileOutputStream out = new FileOutputStream(propertiesPath);
            props.store(out, "---No Comment---");
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 从文件中加载所有属性
    public static Properties loadUserProps() {
        try {
            File file = new File(propertiesPath);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new RuntimeException("创建文件失败：" + file.getAbsolutePath());
                }
            }

            Properties props = new Properties();
            FileInputStream in = new FileInputStream(file);
            props.load(in);
            in.close();
            return props;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
