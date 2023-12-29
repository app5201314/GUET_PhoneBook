package com.lcx.server.service;

import com.lcx.common.Message;
import com.lcx.common.MessageStatus;
import com.lcx.common.MessageType;
import com.lcx.phoneBook.ChineseNameComparator;
import com.lcx.phoneBook.PhoneBook;
import com.lcx.phoneBook.PhoneBooks;
import com.lcx.server.utils.HashUtils;
import com.lcx.server.utils.MessageUtils;
import com.lcx.server.utils.PropertiesUtils;
import com.lcx.server.view.ServerInit;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lcx
 * @version 1.0
 * 这是服务端，监听端口，等待客户端连接，并保持通讯
 */
public class UserServerService extends Thread {
    private boolean isException = true;
    private ServerSocket serverSocket = null;
    public static final Map<String, ServerConnectClientThread> connectClientThreadMap = new ConcurrentHashMap<>();
    private Properties userProps = null;
    private ServerFileSaveThread serverFileSaveThread = null;

    public void setException(boolean exception) {
        isException = exception;
    }

    public void breakLink() {
        serverFileSaveThread.setLoop(false);
        System.out.println("正在保存服务器相关文件，请稍后...");
        //将通讯录保存到文件中
        PhoneBooks.save();
        System.out.println("保存成功！");
        //服务端进程结束或崩溃,将所有的服务端进程关闭
        System.out.println("正在关闭所有客户端连接，请稍后...");
        for (ServerConnectClientThread thread : connectClientThreadMap.values()) {
            Socket socket = thread.getSocket();
            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }

        //清除掉所有与客户端通信的线程
        connectClientThreadMap.clear();

        //再次确保serverSocket是关了的
        if (!serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        MessageUtils.oisMap.clear();
        MessageUtils.oosMap.clear();
        System.out.println("关闭成功！");
    }

    //载入账号密码，及所有通讯录
    public void loadAll() {
        System.out.println("正在加载服务器相关文件...");
        userProps = PropertiesUtils.loadUserProps();
        PhoneBooks.load();
        System.out.println("加载成功！");
    }

    public void startService() {
        System.out.println("正在启动服务器...");
        loadAll();
        try {
            serverSocket = new ServerSocket(6666);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("服务器启动成功！欢迎使用本通讯录系统！");
        this.start();
        serverFileSaveThread = new ServerFileSaveThread();
        serverFileSaveThread.start();
    }

    @SuppressWarnings("all")
    @Override
    public void run() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                Message msg = (Message) MessageUtils.acceptMsg(socket);
                dealUser(msg, socket);
            } catch (Exception e) {
                if (e instanceof java.io.StreamCorruptedException) {
                    // 如果是 StreamCorruptedException，就不做处理
                    continue;
                }

                if (isException) {
                    System.out.println("异常：");
                    e.printStackTrace();
                    System.out.println("检测到服务器异常，正在尝试重启...");

                    breakLink();
                    try {
                        sleep(1000);
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                    ServerInit.setUserServerService(new UserServerService());
                    ServerInit.getUserServerService().startService();
                    System.out.println("重启成功！");
                } else {
                    System.out.println("服务器已关闭！");
                }
            }
        }
    }

    private void dealUser(Message msg, Socket socket) {
        //判断用户是注册还是登录
        if (msg.getMsgType().equals(MessageType.REGISTER)) {
            //注册
            register(msg, socket);
        } else if (msg.getMsgType().equals(MessageType.LOGIN)) {
            //登录
            login(msg, socket);
        } else {
            //其他情况
            MessageUtils.sendMsg(socket, new Message(MessageStatus.FAIL, MessageType.ILLEGAL_OPERATION, "非法操作"));
        }
    }

    //注册
    private void register(Message msg, Socket socket) {
        //获取账号和密码，是以“,”分割的字符串
        String[] split = msg.getContent().split(",");
        String account = split[0];//账号不加密，密码加密存储
        String passwd = split[1];

        if (userProps.containsKey(account)) {
            //账号已存在
            MessageUtils.sendMsg(socket, new Message(MessageStatus.CONFLICT, MessageType.REGISTER, "账号已存在"));
        } else {
            //可注册
            PropertiesUtils.store(userProps, account, passwd);
            //创建一个通讯录
            PhoneBooks.getPhoneBooks().put(account, new PhoneBook(new TreeMap<>(new ChineseNameComparator())));
            MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.REGISTER, "注册成功"));
        }
    }

    //登录
    private void login(Message msg, Socket socket) {
        String[] split = msg.getContent().split(",");
        String account = split[0];
        String passwd = HashUtils.hashWithSHA256(split[1]);//密码加密后再比对

        if (userProps.containsKey(account)) {
            //账号存在
            //检查该用户是否已经登录过了
            if (connectClientThreadMap.containsKey(account)) {
                //已经登录
                MessageUtils.sendMsg(socket, new Message(MessageStatus.CONFLICT, MessageType.LOGIN, "该账号已经登录"));
                return;
            }

            String passwd2 = userProps.getProperty(account);
            if (passwd2.equals(passwd)) {
                //密码正确

                MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.LOGIN, "登录成功"));
                //创建一个和客户端保持联系的线程在后台保持和客户端通信
                ServerConnectClientThread serverConnectClientThread = new ServerConnectClientThread(socket, account, userProps);
                serverConnectClientThread.setPhoneBook(PhoneBooks.getPhoneBooks().get(account));
                ServerSendHeartBeatThread serverSendHeartBeatThread = new ServerSendHeartBeatThread();
                serverSendHeartBeatThread.setServerConnectClientThread(socket, serverConnectClientThread);
                serverSendHeartBeatThread.setDaemon(true);//设置为守护线程，当主线程结束时，该线程也会结束
                serverConnectClientThread.setDaemon(true);

                serverSendHeartBeatThread.start();
                serverConnectClientThread.start();
                //将该用户的服务端进程加入到管理类中
                connectClientThreadMap.put(account, serverConnectClientThread);
            } else {
                //密码错误
                MessageUtils.sendMsg(socket, new Message(MessageStatus.FAIL, MessageType.LOGIN, "密码错误"));
            }
        } else {
            //账号不存在
            MessageUtils.sendMsg(socket, new Message(MessageStatus.NOT_FOUND, MessageType.LOGIN, "账号不存在"));
        }
    }
}