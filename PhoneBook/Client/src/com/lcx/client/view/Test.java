package com.lcx.client.view;

import java.awt.*;
import java.net.URI;

/**
 * @author lcx
 * @version 1.0
 */
public class Test {
    public static void main(String[] args) {
        if(Desktop.isDesktopSupported()){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI("http://www.baidu.com"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Desktop is not supported.");
        }
    }
}


