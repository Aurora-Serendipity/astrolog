package com.rental.ui.swing;

import javax.swing.*;

/**
 * Swing图形界面入口类
 * 负责初始化Swing GUI并启动应用程序
 * 使用系统外观设置，确保跨平台一致性
 *
 * @author 系统
 * @version 1.0
 */
public class SwingUI {

    /**
     * 程序主入口
     * 在EDT线程上启动Swing应用程序，确保线程安全
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 使用SwingUtilities确保在EDT上执行GUI操作，保证线程安全
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 设置系统外观，使用系统默认的LookAndFeel
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    // 如果设置外观失败，打印异常信息
                    e.printStackTrace();
                }

                // 创建并显示主窗口
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}