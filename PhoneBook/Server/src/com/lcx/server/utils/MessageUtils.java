package com.lcx.server.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class MessageUtils {
    public static final ConcurrentHashMap<Socket, ObjectInputStream> oisMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<Socket, ObjectOutputStream> oosMap = new ConcurrentHashMap<>();


    public static Object acceptMsg(final Socket socket) {
        try {
            ObjectInputStream ois;
            if (!oisMap.containsKey(socket)) {
                ois = new ObjectInputStream(socket.getInputStream());
                oisMap.put(socket, ois);
            } else {
                ois = oisMap.get(socket);
            }

            Object temp;
            Object r = null;
            while ((temp = ois.readObject()) != null) {
                r = temp;
            }

            return r;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("all")
    public static void sendMsg(Socket socket, Object msg) {
        try {
            ObjectOutputStream oos;
            if (!oosMap.containsKey(socket)) {
                oos = new ObjectOutputStream(socket.getOutputStream());
                oosMap.put(socket, oos);
            } else {
                oos = oosMap.get(socket);
            }

            synchronized (socket) {
                oos.writeObject(msg);
                oos.writeObject(null);
                oos.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //关闭对应socket的输入输出流
    public static void closeAllStream(Socket socket) {
        oisMap.remove(socket);
        oosMap.remove(socket);
    }
}