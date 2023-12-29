package com.lcx.common;

import com.lcx.server.utils.AESUtils;

import java.io.Serializable;

/**
 * @author lcx
 * @version 1.0
 */
@SuppressWarnings("all")
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private int tag;//标记这是请求还是响应
    private String status;//标记请求是否成功
    private String content = "";//发送的内容
    private String msgType;//消息类型

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    //请求包构造
    public Message(String msgType, String content) {
        this.tag = MessageTag.TAG_REQUEST;
        this.msgType = msgType;
        this.content = AESUtils.encrypt(content);
    }

    public Message(String msgType) {
        this.tag = MessageTag.TAG_REQUEST;
        this.msgType = msgType;
    }

    //响应包构造
    public Message(String status, String msgType, String content) {
        this.tag = MessageTag.TAG_RESPONSE;
        this.status = status;
        this.msgType = msgType;
        this.content = AESUtils.encrypt(content);
    }

    public Message() {
    }

    public String getContent() {
        return AESUtils.decrypt(content);
    }

    public void setContent(String content) {
        this.content = AESUtils.encrypt(content);
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
}
