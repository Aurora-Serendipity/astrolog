package com.rental.client.ui;

import javax.swing.*;

/**
 * 客户端UI入口类
 * 负责初始化Swing GUI并启动应用程序
 *
 * @author 系统
 * @version 1.0
 */
public class ClientUI {

    /**
     * 主方法
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ClientMainFrame mainFrame = new ClientMainFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}