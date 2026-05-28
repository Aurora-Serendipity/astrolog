package com.astrolog;

import com.astrolog.config.ThemeConfig;
import com.astrolog.ui.component.ThemeManager;
import com.astrolog.ui.frame.LoginFrame;
import com.astrolog.util.DBUtil;

import javax.swing.*;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppMain {

    private static final String SETTINGS_FILE = "astrolog_settings.properties";

    public static void main(String[] args) {
        try {
            DBUtil.getInstance();
            System.out.println("数据库连接池初始化成功");
        } catch (Exception e) {
            System.err.println("数据库连接池初始化失败: " + e.getMessage());
            System.err.println("请确保 MySQL 已启动且 astrolog 数据库已创建（执行 sql/init.sql）");
        }

        loadSavedSettings();

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("设置系统外观失败，使用默认外观: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    private static void loadSavedSettings() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            props.load(fis);
        } catch (IOException ignored) {}

        String themeName = props.getProperty("theme", "dark");
        ThemeConfig.Theme theme;
        switch (themeName) {
            case "light": theme = ThemeConfig.LIGHT; break;
            case "starry": theme = ThemeConfig.STARRY; break;
            default: theme = ThemeConfig.DARK; break;
        }
        ThemeManager.getInstance().setTheme(theme);

        String fontSize = props.getProperty("font.size", "14");
        try {
            int size = Integer.parseInt(fontSize);
            Font defaultFont = new Font("Microsoft YaHei", Font.PLAIN, size);
            UIManager.put("Label.font", defaultFont);
            UIManager.put("Button.font", defaultFont);
            UIManager.put("TextField.font", defaultFont);
            UIManager.put("TextArea.font", defaultFont);
            UIManager.put("Table.font", defaultFont);
            UIManager.put("TableHeader.font", defaultFont);
            UIManager.put("TabbedPane.font", defaultFont);
        } catch (NumberFormatException ignored) {}
    }
}
