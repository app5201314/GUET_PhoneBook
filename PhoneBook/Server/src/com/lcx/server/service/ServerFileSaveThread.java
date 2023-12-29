package com.lcx.server.service;

import com.lcx.phoneBook.PhoneBooks;

/**
 * @author lcx
 * @version 1.0
 */
public class ServerFileSaveThread extends Thread {
    private boolean loop = true;

    @SuppressWarnings("all")
    @Override
    public void run() {
        try {
            while (loop) {
                PhoneBooks.save();
                sleep(1000 * 60 * 5);//每隔5分钟保存一次
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }
}