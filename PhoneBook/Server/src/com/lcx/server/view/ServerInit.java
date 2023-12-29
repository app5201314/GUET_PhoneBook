package com.lcx.server.view;

import com.lcx.server.service.UserServerService;
import com.lcx.server.utils.Utility;

/**
 * @author lcx
 * @version 1.0
 * 实现对服务端的开启
 */
public class ServerInit {
    private static UserServerService userServerService = null;

    public static UserServerService getUserServerService() {
        return userServerService;
    }

    public static void setUserServerService(UserServerService userServerService) {
        ServerInit.userServerService = userServerService;
    }

    public static void main(String[] args) {
        while (true) {
            Utility.clearScreen();
            System.out.println("==========通讯录系统==========");
            System.out.println("1.启动服务");
            System.out.println("2.项目简介");
            System.out.println("3.退出系统");
            System.out.println("=============================");
            System.out.println("请输入你的选择：");
            char choice = Utility.readMenuSelection();
            switch (choice) {
                case '1':
                    start();
                    break;
                case '2':
                    Utility.clearScreen();
                    System.out.println("=======项目简介======");
                    System.out.println("项目名称：GUET通讯录管理系统");
                    System.out.println("项目负责人：李晨曦");
                    System.out.println("联系方式QQ：2543036788");
                    System.out.println("邮箱：2543036788@qq.com");
                    System.out.println("欢迎交流！");
                    System.out.println("====================");
                    System.out.println("请按任意键继续...");
                    Utility.readString(100);
                    break;
                case '3':
                    System.out.println("感谢使用本通讯录系统，再见！");
                    System.exit(0);
                default:
                    System.out.println("输入错误，请重新输入！");
            }
        }
    }

    public static void start() {
        userServerService = new UserServerService();
        userServerService.startService();
        boolean loop = true;

        while (loop) {
            Utility.clearScreen();
            //显示关闭服务或重启服务的选项
            System.out.println("当前服务状态：已开启");
            System.out.println("==========通讯录系统==========");
            System.out.println("1.关闭服务");
            System.out.println("2.重启服务");
            System.out.println("================================");
            System.out.println("请输入你的选择：");
            char choice = Utility.readMenuSelection();
            switch (choice) {
                case '1':
                    userServerService.setException(false);
                    userServerService.breakLink();
                    loop = false;
                    break;
                case '2':
                    userServerService.setException(false);
                    userServerService.breakLink();
                    userServerService = new UserServerService();
                    userServerService.startService();
                    System.out.println("服务重启成功！");
                    break;
                default:
                    System.out.println("输入错误，请重新输入！");
            }
            System.out.println("请按任意键继续...");
            Utility.readString(100);
        }
    }
}
