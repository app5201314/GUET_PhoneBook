package com.lcx.server.service;

import com.lcx.common.Message;
import com.lcx.common.MessageType;
import com.lcx.server.utils.MessageUtils;

import java.net.Socket;

/**
 * @author lcx
 * @version 1.0
 */
public class ServerSendHeartBeatThread extends Thread {
    private Socket socket;
    private ServerConnectClientThread serverConnectClientThread;
    public void setServerConnectClientThread(Socket socket, ServerConnectClientThread serverConnectClientThread) {
        this.socket = socket;
        this.serverConnectClientThread = serverConnectClientThread;
    }

    @SuppressWarnings("all")
    @Override
    public void run() {
        try {
            while (true) {
                sleep(1000 * 5);
                MessageUtils.sendMsg(socket, new Message(MessageType.HEART_BEAT));
                serverConnectClientThread.setLife(serverConnectClientThread.getLife() - 1);
                if (serverConnectClientThread.getLife() <= 0) {//生命值为0，关闭socket
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                    break;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}