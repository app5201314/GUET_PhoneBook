package com.lcx.client.service;

import com.lcx.client.utility.MessageUtils;
import com.lcx.client.view.View;
import com.lcx.common.Message;
import com.lcx.common.MessageStatus;
import com.lcx.common.MessageTag;
import com.lcx.common.MessageType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author lcx
 * @version 1.0
 * 该类完成用户登录及用户注册的功能
 * 如果登录成功，就创建一个接收信息的线程
 */
public class UserClientService {
    public static Socket link() {
        try {
//            return new Socket(InetAddress.getByName("localhost"), 6666);
            return new Socket(InetAddress.getByName("47.115.227.17"), 6666);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerUser(String account, String pwd) {
        Socket socket = link();

        try {
            String msgContent = account + "," + pwd;
            Message msg = new Message(MessageType.REGISTER, msgContent);
            MessageUtils.sendMsg(socket, msg);
            msg = (Message) MessageUtils.acceptMsg(socket);

            if (msg == null) {
                return;
            }
            if (msg.getTag() == MessageTag.TAG_RESPONSE && msg.getMsgType().equals(MessageType.REGISTER)
            && msg.getStatus().equals(MessageStatus.SUCCESS)) {
                System.out.println(msg.getContent());
            } else {
                //注册失败
                System.out.println(msg.getContent());
            }

            socket.close();
            MessageUtils.closeAllStream(socket);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ClientConnectServerThread checkUser(String account, String pwd, View view) {
        String msgContent = account + "," + pwd;
        Message msg = new Message(MessageType.LOGIN, msgContent);
        Socket socket = link();

        try {
            MessageUtils.sendMsg(socket, msg);

            msg = (Message) MessageUtils.acceptMsg(socket);

            if (msg == null) {
                return null;
            }

            if (msg.getTag() == MessageTag.TAG_RESPONSE && msg.getMsgType().equals(MessageType.LOGIN)
            && msg.getStatus().equals(MessageStatus.SUCCESS)) {
                //登录成功
                view.setSocket(socket);
                //创建一个和服务器端保持联系的线程，开辟一个子线程，从主线程分离出来，让子线程在后台保持和服务器通信
                ClientConnectServerThread clientConnectServerThread = new ClientConnectServerThread(socket, view, account, pwd);
                clientConnectServerThread.setDaemon(true);
                clientConnectServerThread.start();
                System.out.println(msg.getContent());
                return clientConnectServerThread;
            } else {
                System.out.println(msg.getContent());
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}