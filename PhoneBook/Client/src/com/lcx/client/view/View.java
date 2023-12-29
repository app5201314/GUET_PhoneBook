package com.lcx.client.view;

import com.lcx.client.service.UserClientService;
import com.lcx.client.utility.MessageUtils;
import com.lcx.client.utility.Utility;
import com.lcx.common.Message;
import com.lcx.common.MessageType;

import java.net.Socket;

import static java.lang.Thread.sleep;

/**
 * @author lcx
 * @version 1.0
 * 客户端的菜单界面
 */
public class View {
    static final int INPUT_LEN = 20;
    private final Object lock = new Object();//用于线程同步
    private Socket socket = null;
    private String contacts = null;
    private boolean isExist = false;

    public static void main(String[] args) throws InterruptedException {
        new View().menu();
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    @SuppressWarnings("all")
    public void menu() throws InterruptedException {
        while (true) {
            try {
                //清屏
                Utility.clearScreen();
                System.out.println("========菜单========");
                System.out.println("1.登录");
                System.out.println("2.注册");
                System.out.println("3.项目简介");
                System.out.println("4.退出系统");
                System.out.println("===================");
                System.out.println("请输入选择：");
                int cmd = Utility.readInt();
                String account;
                String passwd;
                switch (cmd) {
                    case 1:
                        System.out.println("请输入用户名：");
                        account = Utility.readString(INPUT_LEN);
                        System.out.println("请输入密码：");
                        passwd = Utility.readString(INPUT_LEN);
                        if (UserClientService.checkUser(account, passwd, this) != null) {
                            mainMenu(account, passwd);
                            continue;
                        }
                        break;
                    case 2:
                        System.out.println("请输入用户名：");
                        account = Utility.readString(INPUT_LEN);
                        passwd = Utility.checkPasswd();
                        System.out.println("请稍等，正在验证账号信息...");
                        UserClientService.registerUser(account, passwd);
                        break;
                    case 3:
                        //在浏览器中打开github项目主页
                        break;
                    case 4:
                        System.out.println("程序已退出~");
                        return;
                    default:
                        System.out.println("无此选项!");
                        break;
                }
                System.out.println("输入任意键返回主菜单...");
                Utility.readChar('a');
            } catch (Exception e) {
                System.out.println("网络连接异常，请重试...2秒后跳转到主菜单");
                sleep(2000);
            }
        }
    }



    @SuppressWarnings("BusyWait")
    public void mainMenu(String account, String passwd) throws InterruptedException {
        while (!socket.isClosed()) {
            Utility.clearScreen();
            System.out.println("(当前用户： " + account + ")");
            System.out.println("========菜单========");
            System.out.println("1.修改密码");
            System.out.println("2.查看通讯录");
            System.out.println("3.添加联系人");
            System.out.println("4.删除联系人");
            System.out.println("5.修改联系人");
            System.out.println("6.登出");
            System.out.println("===================");
            System.out.println("请输入选择：");
            char cmd = Utility.readChar();

            try {
                switch (cmd) {
                    case '1':
                        updatePwd(passwd);
                        break;
                    case '2':
                        checkPhoneBook(account);
                        continue;
                    case '3':
                        addContact();
                        break;
                    case '4':
                        delContact();
                        continue;
                    case '5':
                        updateContact();
                        break;
                    case '6':
                        MessageUtils.sendMsg(socket, new Message(MessageType.CLIENT_EXIT));
                        if (!socket.isClosed()) {
                            socket.close();
                        }
                        sleep(200);
                        return;
                    default:
                        System.out.println("无效输入，只能输入菜单选项");
                        System.out.println("输入任意键返回菜单...");
                        break;
                }
                System.out.println("输入任意键返回菜单...");
                Utility.readChar('a');

            } catch (Exception e) {
                System.out.println("网络异常，请重新登录，2秒后跳回主菜单...");
                sleep(2000);
                return;
            }
        }
    }

    //修改联系人
    private void updateContact() {
        System.out.println("请输入要修改的联系人id：");
        String id = Utility.readString(10);
        //根据id查询联系人
        MessageUtils.sendMsg(socket, new Message(MessageType.EXIST_CONTACT, id));
        try {
            waitMsg();
        } catch (Exception e) {
            System.out.println("网络异常，请重试...");
            return;
        }

        if (!isExist) {
            System.out.println("不存在该联系人");
            return;
        }

        Message msg = inputUpdateContactInfo(id);
        MessageUtils.sendMsg(socket, msg);
    }

    //输入联系人信息
    private Message inputUpdateContactInfo(String id) {
        System.out.println("请输入联系人姓名：(敲回车表示不修改)");
        String name = Utility.readString(INPUT_LEN, " ");
        System.out.println("请输入联系人电话：(敲回车表示不修改)");
        String phone = Utility.readString(INPUT_LEN, " ");
        System.out.println("请输入联系人工作单位：(敲回车表示不修改)");
        String workUnit = Utility.readString(INPUT_LEN, " ");
        System.out.println("请输入联系人住址：(敲回车表示不修改)");
        String address = Utility.readString(INPUT_LEN, " ");
        System.out.println("请输入联系人群组：(敲回车表示不修改)");
        String group = Utility.readString(INPUT_LEN, " ");
        return new Message(MessageType.UPDATE_CONTACT, id + "，" + name + "，" + phone + "，" + workUnit + "，" + address + "，" + group);
    }

    //输入联系人信息
    private Message inputContactInfo() {
        System.out.println("请输入联系人姓名：");
        String name = Utility.readString(INPUT_LEN);
        System.out.println("请输入联系人电话：");
        String phone = Utility.readString(INPUT_LEN);
        System.out.println("请输入联系人工作单位：（敲回车表示桂电）");
        String workUnit = Utility.readString(INPUT_LEN, "桂林电子科技大学");
        System.out.println("请输入联系人住址：（敲回车表示109宿舍）");
        String address = Utility.readString(INPUT_LEN, "b区24栋109号");
        System.out.println("请输入联系人群组：（敲回车表示同学）");
        String group = Utility.readString(INPUT_LEN, "同学");
        return new Message(MessageType.ADD_CONTACT, name + "，" + phone + "，" + workUnit + "，" + address + "，" + group);
    }

    //修改密码
    private void updatePwd(String passwd) {
        System.out.println("请输入原密码：");
        String oldPwd = Utility.readString(INPUT_LEN);
        if (!oldPwd.equals(passwd)) {
            System.out.println("原密码错误");
            return;
        }
        System.out.println("请输入新密码：");
        String newPwd = Utility.readString(INPUT_LEN);
        MessageUtils.sendMsg(socket, new Message(MessageType.UPDATE_PWD, newPwd));
    }

    private void addContact() {
        Message msg = inputContactInfo();
        MessageUtils.sendMsg(socket, msg);
    }

    private void delContact() {
        while (!socket.isClosed()) {
            Utility.clearScreen();
            //根据id删除指定联系人，或清空通讯录
            System.out.println("========菜单========");
            System.out.println("1.删除指定联系人");
            System.out.println("2.删除指定分组");
            System.out.println("3.删除所有联系人");
            System.out.println("4.返回上一级菜单");
            System.out.println("===================");
            System.out.println("请输入选择：");
            char cmd = Utility.readChar();
            switch (cmd) {
                case '1':
                    System.out.println("请输入要删除的联系人id：");
                    String id = Utility.readString(10);
                    MessageUtils.sendMsg(socket, new Message(MessageType.DELETE_CONTACT, id));
                    break;
                case '2':
                    System.out.println("请输入要删除的分组：");
                    String group = Utility.readString(10);
                    MessageUtils.sendMsg(socket, new Message(MessageType.DELETE_GROUP, group));
                    break;
                case '3':
                    System.out.println("请确认是否删除所有联系人？(y/n)");
                    char confirm = Utility.readChar();
                    if (confirm == 'y') {
                        MessageUtils.sendMsg(socket, new Message(MessageType.DELETE_ALL_CONTACTS));
                    }
                    break;
                case '4':
                    return;
                default:
                    System.out.println("无效输入，只能输入菜单选项");
                    break;
            }
            System.out.println("输入任意键返回菜单...");
            Utility.readChar('a');
        }
    }

    public void checkPhoneBook(String account) {
        while (!socket.isClosed()) {
            Utility.clearScreen();
            System.out.println("(当前用户： " + account + ")");
            System.out.println("========通讯录========");
            System.out.println("1.查看所有联系人");
            System.out.println("2.精确查询");
            System.out.println("3.模糊查询");
            System.out.println("4.查看指定分组");
            System.out.println("5.返回上一级菜单");
            System.out.println("====================");
            System.out.println("请输入选择：");
            char cmd = Utility.readChar();

            switch (cmd) {
                case '1':
                    MessageUtils.sendMsg(socket, new Message(MessageType.CHECK_ALL_CONTACTS));
                    try {
                        waitMsg();
                    } catch (Exception e) {
                        System.out.println("网络异常，请重试...");
                        return;
                    }
                    paginateContacts();
                    continue;
                case '2':
                    //根据id查询
                    System.out.println("请输入要查询的联系人id：");
                    String id = Utility.readString(10);
                    MessageUtils.sendMsg(socket, new Message(MessageType.CHECK_CONTACT_BY_ID, id));
                    try {
                        waitMsg();
                    } catch (Exception e) {
                        System.out.println("网络异常，请重试...");
                        return;
                    }
                    queryContactById();
                    break;
                case '3':
                    //根据姓名或电话查询
                    System.out.println("请输入要查询的联系人的相关信息：");
                    String nameOrPhone = Utility.readString(16);
                    MessageUtils.sendMsg(socket, new Message(MessageType.CHECK_CONTACTS_BY_KEYWORD, nameOrPhone));
                    try {
                        waitMsg();
                    } catch (Exception e) {
                        System.out.println("网络异常，请重试...");
                        return;
                    }
                    paginateContacts();
                    continue;
                case '4':
                    //根据分组查询
                    System.out.println("请输入要查询的分组：");
                    String group = Utility.readString(10);
                    MessageUtils.sendMsg(socket, new Message(MessageType.CHECK_GROUP, group));
                    try {
                        waitMsg();
                    } catch (Exception e) {
                        System.out.println("网络异常，请重试...");
                        return;
                    }
                    paginateContacts();
                    continue;
                case '5':
                    return;
                default:
                    System.out.println("无效输入，只能输入菜单选项");
                    break;
            }
            System.out.println("输入任意键返回菜单...");
            Utility.readChar('a');
        }
    }

    private void waitMsg() {
        synchronized (lock) {
            try {
                lock.wait(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void queryContactById() {
        if (contacts == null) {
            System.out.println("无此联系人");
            return;
        }

        Utility.clearScreen();
        String[] split = contacts.split("，");
        System.out.printf("%-10s%-10s%-15s%-15s%-13s%-10s\n", "id", "姓名", "电话", "工作单位", "住址", "群组");
        System.out.printf("%-10s%-9s%-16s%-12s%-12s%-10s\n", split[0], split[1], split[2], split[3], split[4], split[5]);
    }

    public void paginateContacts() {
        if (contacts == null) {
            System.out.println("无任何联系人");
            System.out.println("输入任意键返回菜单...");
            Utility.readChar('a');
            return;
        }

        String[] split = contacts.split(",");
        int len = split.length;
        int pageSize = 5;
        int pageNum = len / pageSize + (len % pageSize == 0 ? 0 : 1);
        int currentPage = 1;
        while (true) {
            //清屏
            Utility.clearScreen();
            System.out.println("第" + currentPage + "页");
            System.out.printf("%-10s%-10s%-15s%-15s%-13s%-10s\n", "id", "姓名", "电话", "工作单位", "住址", "群组");
            for (int i = (currentPage - 1) * pageSize; i < currentPage * pageSize; i++) {
                if (i < len) {
                    String[] contactInfo = split[i].split("，");
                    System.out.printf("%-10s%-9s%-16s%-12s%-12s%-10s\n", contactInfo[0], contactInfo[1], contactInfo[2], contactInfo[3], contactInfo[4], contactInfo[5]);
                }
            }
            System.out.println("共" + pageNum + "页");
            System.out.println("输入l键翻上一页，输入n键翻下一页，按q键退出...");
            char cmd = Utility.readChar();
            switch (cmd) {
                case 'l':
                    if (currentPage > 1) {
                        currentPage--;
                    } else {
                        currentPage = pageNum; // 如果已经是第一页，转到最后一页
                    }
                    break;
                case 'n':
                    if (currentPage < pageNum) {
                        currentPage++;
                    } else {
                        currentPage = 1; // 如果已经是最后一页，转到第一页
                    }
                    break;
                case 'q':
                    return;
                default:
                    break;
            }
        }
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
        synchronized (lock) {
            lock.notify();//唤醒等待的线程
        }
    }

    public void isExistContact(boolean isExist) {
        this.isExist = isExist;
        synchronized (lock) {
            lock.notify();//唤醒等待的线程
        }
    }
}