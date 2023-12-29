package com.lcx.common;

public interface MessageType {
    // 登录相关
    String LOGIN = "a1";//请求登录
    String CLIENT_EXIT = "a2";//表示该消息为客户端请求退出

    // 注册相关
    String REGISTER = "b1";//请求注册

    // 密码修改相关
    String UPDATE_PWD = "c1";//表示修改密码

    // 联系人查询相关
    String CHECK_ALL_CONTACTS = "d1";//查看所有联系人
    String CHECK_CONTACTS_BY_KEYWORD = "d2";//关键字查询
    String CHECK_CONTACT_BY_ID = "d3";//通过id查看指定联系人
    String CHECK_GROUP = "d4";//查看指定分组

    // 联系人修改相关
    String ADD_CONTACT = "e1";//添加联系人
    String UPDATE_CONTACT = "e2";//修改联系人
    String DELETE_ALL_CONTACTS = "e3";//删除所有联系人
    String DELETE_CONTACT = "e4";//删除指定联系人
    String EXIST_CONTACT = "e5";//是否存在联系人
    String DELETE_GROUP = "e6";//删除分组

    // 其他
    String ILLEGAL_OPERATION = "f1";//非法操作
    String HEART_BEAT = "f2";//心跳包
}