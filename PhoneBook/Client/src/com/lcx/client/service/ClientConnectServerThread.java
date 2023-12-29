package com.lcx.client.service;

import com.lcx.client.utility.MessageUtils;
import com.lcx.client.view.View;
import com.lcx.common.Message;
import com.lcx.common.MessageStatus;
import com.lcx.common.MessageTag;
import com.lcx.common.MessageType;

import java.io.IOException;
import java.net.Socket;

/**
 * @author lcx
 * @version 1.0
 * 该线程只负责接收服务端发来的消息
 */
public class ClientConnectServerThread extends Thread {
    //该线程需要持有socket
    private final Socket socket;
    private final View view;
    private final String account;
    private final String passwd;

    public ClientConnectServerThread(Socket socket, View view, String account, String passwd) {
        this.socket = socket;
        this.view = view;
        this.account = account;
        this.passwd = passwd;
    }

    @SuppressWarnings("all")
    @Override
    public void run() {
        //因为Thread需要在后台一直与服务器通讯，所以用一个while循环
        try {
            while (true) {
                Message msg = (Message) MessageUtils.acceptMsg(socket);
                if (msg == null) {
                    continue;
                }

                if (msg.getTag() == MessageTag.TAG_REQUEST) {
                    if (msg.getMsgType().equals(MessageType.HEART_BEAT)) {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.HEART_BEAT, "还活着"));
                    }
                } else {
                    switch (msg.getMsgType()) {
                        //需要将服务器发来的消息显示到客户端的控制台
                        case MessageType.CHECK_CONTACT_BY_ID:
                        case MessageType.CHECK_ALL_CONTACTS:
                        case MessageType.CHECK_CONTACTS_BY_KEYWORD:
                        case MessageType.CHECK_GROUP:
                            view.setContacts(msg.getContent());
                            break;
                        //只用告诉用户操作的结果即可
                        case MessageType.UPDATE_CONTACT:
                        case MessageType.DELETE_CONTACT:
                        case MessageType.DELETE_GROUP:
                        case MessageType.ADD_CONTACT:
                        case MessageType.DELETE_ALL_CONTACTS:
                        case MessageType.UPDATE_PWD:
                            switch (msg.getStatus()) {
                                case MessageStatus.SUCCESS:
                                case MessageStatus.NOT_FOUND:
                                case MessageStatus.CONFLICT:
                                    System.out.println(msg.getContent());
                                    break;
                                default:
                                    System.out.println("未知错误！");
                                    break;
                            }
                            break;
                        case MessageType.EXIST_CONTACT:
                            if (msg.getStatus().equals(MessageStatus.SUCCESS)) {
                                view.isExistContact(true);
                            } else if (msg.getStatus().equals(MessageStatus.NOT_FOUND)) {
                                view.isExistContact(false);
                            } else {
                                System.out.println("未知错误！");
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            MessageUtils.closeAllStream(socket);
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }

            System.out.println("服务器异常，正在尝试重连...");
            try {
                sleep(2000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            if (UserClientService.checkUser(account, passwd, view) != null) {
                System.out.println("请继续操作：");
            } else {
                System.out.println("重连失败！");
            }
        }
    }
}