package com.lcx.server.service;

import com.lcx.common.Message;
import com.lcx.common.MessageStatus;
import com.lcx.common.MessageTag;
import com.lcx.common.MessageType;
import com.lcx.phoneBook.Contact;
import com.lcx.phoneBook.PhoneBook;
import com.lcx.server.utils.MessageUtils;
import com.lcx.server.utils.PropertiesUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

/**
 * @author lcx
 * @version 1.0
 */
public class ServerConnectClientThread extends Thread {
    public static final int MAX_LIFE = 5;
    private final Socket socket;
    private final String account;
    private final Properties props;
    private PhoneBook phoneBook = null;
    private int life = MAX_LIFE;//生命值

    public void setPhoneBook(PhoneBook phoneBook) {
        this.phoneBook = phoneBook;
    }

    public ServerConnectClientThread(Socket socket, String account, Properties props) {
        this.socket = socket;
        this.account = account;
        this.props = props;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        this.life = life;
    }

    @SuppressWarnings("all")
    @Override
    public void run() {
        Message msg = null;
        try {
            while (true) {
                //读取客户端发送过来的消息
                msg = (Message) MessageUtils.acceptMsg(socket);
                if (msg == null) {
                    continue;
                }

                dealMsg(msg);
                msg = null;
            }
        } catch (Exception e) {
            if (msg != null) {//如果是服务端的逻辑错误，就需要将错误信息发送给客户端
                MessageUtils.sendMsg(socket, new Message(MessageStatus.FAIL, msg.getMsgType(), e.getMessage()));
            }

            if (!socket.isClosed()) {
                try {
                    socket.close();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            MessageUtils.closeAllStream(socket);
            UserServerService.connectClientThreadMap.remove(account);
        }
    }

    //处理客户端发送过来的消息
    private void dealMsg(Message msg) throws IOException {
        if (msg.getTag() == MessageTag.TAG_RESPONSE) {
            if (msg.getMsgType().equals(MessageType.HEART_BEAT) && msg.getStatus().equals(MessageStatus.SUCCESS)) {
                setLife(MAX_LIFE);
            }
        } else {
            switch (msg.getMsgType()) {
                case MessageType.DELETE_GROUP:
                    //删除分组
                    phoneBook.deleteGroup(msg.getContent());
                    MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.DELETE_GROUP, "删除分组成功"));
                    break;
                case MessageType.EXIST_CONTACT:
                    String existContact = phoneBook.searchContactById(msg.getContent());
                    if (existContact == null) {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.NOT_FOUND, MessageType.EXIST_CONTACT, "未找到指定联系人"));
                    } else {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.EXIST_CONTACT, "成功"));
                    }
                    break;
                case MessageType.UPDATE_CONTACT:
                    //修改联系人
                    String[] split1 = msg.getContent().split("，");
                    String id1 = split1[0];
                    String name1 = split1[1];
                    String phone1 = split1[2];
                    String workUnit1 = split1[3];
                    String address1 = split1[4];
                    String group1 = split1[5];
                    //检查是否已经存在该联系人，依据是姓名和电话同时一致
                    if (phoneBook.IsExisted(name1, phone1)) {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.CONFLICT, MessageType.UPDATE_CONTACT, "该联系人已存在"));
                        return;
                    }

                    phoneBook.updateContact(id1, name1, phone1, workUnit1, address1, group1);
                    MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.UPDATE_CONTACT, "修改联系人成功"));
                    break;
                case MessageType.DELETE_ALL_CONTACTS:
                    //删除所有联系人
                    phoneBook.deleteAllContacts();
                    MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.DELETE_ALL_CONTACTS, "删除所有联系人成功"));
                    break;
                case MessageType.ADD_CONTACT:
                    //添加联系人
                    String[] split = msg.getContent().split("，");
                    String name = split[0];
                    String phone = split[1];
                    String workUnit = split[2];
                    String address = split[3];
                    String group = split[4];
                    //遍历phoneBook，找到没有被占用的id
                    String newId = phoneBook.getNewId();
                    //检查是否已经存在该联系人，依据是姓名和电话同时一致
                    if (phoneBook.IsExisted(name, phone)) {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.CONFLICT, MessageType.ADD_CONTACT, "该联系人已存在"));
                        return;
                    }

                    phoneBook.getContacts().put(name + newId, new Contact(newId, name, phone, workUnit, address, group));
                    MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.ADD_CONTACT, "添加联系人成功"));
                    break;
                case MessageType.CHECK_CONTACT_BY_ID:
                    //根据id查看指定联系人
                    String getContact = phoneBook.searchContactById(msg.getContent());
                    if (getContact == null) {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.NOT_FOUND, MessageType.CHECK_CONTACT_BY_ID, null));
                    } else {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.CHECK_CONTACT_BY_ID, getContact));
                    }
                    break;
                case MessageType.CHECK_CONTACTS_BY_KEYWORD://查看指定联系人，模糊查询
                    //查看指定联系人，模糊查询
                    String getContacts = phoneBook.searchContacts(msg.getContent());
                    if (getContacts == null) {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.NOT_FOUND, MessageType.CHECK_CONTACTS_BY_KEYWORD, null));
                    } else {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.CHECK_CONTACTS_BY_KEYWORD, getContacts));
                    }
                    break;
                case MessageType.CHECK_ALL_CONTACTS:
                    //查看所有联系人
                    String allContacts = phoneBook.getAllContacts();
                    if (allContacts == null) {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.NOT_FOUND, MessageType.CHECK_ALL_CONTACTS, null));
                    } else {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.CHECK_ALL_CONTACTS, allContacts));
                    }
                    break;
                case MessageType.CHECK_GROUP:
                    //查看指定分组
                    String getGroup = phoneBook.searchGroup(msg.getContent());
                    if (getGroup == null) {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.NOT_FOUND, MessageType.CHECK_GROUP, null));
                    } else {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.CHECK_GROUP, getGroup));
                    }
                    break;
                case MessageType.DELETE_CONTACT:
                    //删除联系人
                    if (phoneBook.deleteContactById(msg.getContent())) {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.DELETE_CONTACT, "删除联系人成功"));
                    } else {
                        MessageUtils.sendMsg(socket, new Message(MessageStatus.NOT_FOUND, MessageType.DELETE_CONTACT, "未找到指定联系人"));
                    }
                    break;
                case MessageType.UPDATE_PWD:
                    //修改密码
                    String pwd = msg.getContent();
                    PropertiesUtils.store(props, account, pwd);
                    MessageUtils.sendMsg(socket, new Message(MessageStatus.SUCCESS, MessageType.UPDATE_PWD, "修改密码成功"));
                    break;
                case MessageType.CLIENT_EXIT://客户端退出
                    if (!socket.isClosed()) {
                        socket.close();
                    }
                    //将该用户的服务端进程从管理类中移除
                    UserServerService.connectClientThreadMap.remove(account);
                    break;
                default:
                    MessageUtils.sendMsg(socket, new Message(MessageStatus.FAIL, MessageType.ILLEGAL_OPERATION, "非法操作"));
                    break;
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }
}