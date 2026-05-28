package com.astrolog.ui.panel;

import com.astrolog.config.AppConfig;
import com.astrolog.config.ThemeConfig;
import com.astrolog.model.User;
import com.astrolog.service.ExportService;
import com.astrolog.ui.component.ThemeManager;
import com.astrolog.ui.dialog.BackupRestoreDialog;
import com.astrolog.ui.dialog.ReportViewDialog;
import com.astrolog.ui.frame.MainFrame;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

public class SettingsPanel extends JPanel {

    private final User currentUser;

    public SettingsPanel(User currentUser) {
        this.currentUser = currentUser;
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        contentPanel.setBackground(new Color(240, 240, 240));

        contentPanel.add(buildAppearanceSection());
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(buildDataSection());
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(buildReportSection());
        contentPanel.add(Box.createVerticalStrut(16));
        contentPanel.add(buildAboutSection());

        add(contentPanel, BorderLayout.NORTH);
    }

    private JPanel buildAppearanceSection() {
        JPanel panel = createSectionPanel("外观设置");

        // 主题切换
        JLabel themeLabel = new JLabel("主题:");
        themeLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        panel.add(themeLabel);

        ThemeConfig.Theme current = ThemeManager.getInstance().getCurrentTheme();
        JRadioButton darkBtn = new JRadioButton("暗色主题", current == ThemeConfig.DARK);
        JRadioButton lightBtn = new JRadioButton("亮色主题", current == ThemeConfig.LIGHT);
        JRadioButton starryBtn = new JRadioButton("星空主题", current == ThemeConfig.STARRY);

        darkBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        lightBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        starryBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

        ButtonGroup themeGroup = new ButtonGroup();
        themeGroup.add(darkBtn);
        themeGroup.add(lightBtn);
        themeGroup.add(starryBtn);

        darkBtn.addActionListener(e -> switchTheme("dark"));
        lightBtn.addActionListener(e -> switchTheme("light"));
        starryBtn.addActionListener(e -> switchTheme("starry"));

        JPanel themeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        themeRow.setOpaque(false);
        themeRow.add(themeLabel);
        themeRow.add(darkBtn);
        themeRow.add(lightBtn);
        themeRow.add(starryBtn);
        panel.add(themeRow);

        // 字体大小调节
        panel.add(Box.createVerticalStrut(8));
        JLabel fontLabel = new JLabel("字体大小:");
        fontLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        panel.add(fontLabel);

        JRadioButton smallBtn = new JRadioButton("小");
        JRadioButton mediumBtn = new JRadioButton("中(默认)", true);
        JRadioButton largeBtn = new JRadioButton("大");

        smallBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        mediumBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        largeBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

        ButtonGroup fontGroup = new ButtonGroup();
        fontGroup.add(smallBtn);
        fontGroup.add(mediumBtn);
        fontGroup.add(largeBtn);

        smallBtn.addActionListener(e -> applyFontSize(12));
        mediumBtn.addActionListener(e -> applyFontSize(14));
        largeBtn.addActionListener(e -> applyFontSize(16));

        JPanel fontRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        fontRow.setOpaque(false);
        fontRow.add(fontLabel);
        fontRow.add(smallBtn);
        fontRow.add(mediumBtn);
        fontRow.add(largeBtn);
        panel.add(fontRow);

        return panel;
    }

    private JPanel buildDataSection() {
        JPanel panel = createSectionPanel("数据管理");

        JButton backupBtn = new JButton("备份数据");
        backupBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        backupBtn.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new BackupRestoreDialog(parent);
        });

        JButton restoreBtn = new JButton("恢复数据");
        restoreBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        restoreBtn.addActionListener(e -> {
            JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
            new BackupRestoreDialog(parent);
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        btnRow.setOpaque(false);
        btnRow.add(backupBtn);
        btnRow.add(restoreBtn);
        panel.add(btnRow);

        return panel;
    }

    private JPanel buildReportSection() {
        JPanel panel = createSectionPanel("报告导出");

        JButton annualReportBtn = new JButton("生成年度报告");
        annualReportBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        annualReportBtn.addActionListener(e -> {
            try {
                ExportService exportService = new ExportService();
                int year = java.time.LocalDate.now().getYear();
                String html = exportService.buildHtmlContent(currentUser, year);
                JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
                new ReportViewDialog(parent, html, currentUser, year);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "生成报告失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton certBtn = new JButton("梅西耶证书");
        certBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        certBtn.addActionListener(e -> {
            try {
                ExportService exportService = new ExportService();
                String path = exportService.generateMessierCert(currentUser);
                JOptionPane.showMessageDialog(this,
                    "梅西耶证书已生成:\n" + path,
                    "证书生成成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage() + "\n\n请继续观测，完成全部 110 个梅西耶天体后可生成证书。",
                    "尚未满足条件", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "证书生成失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        btnRow.setOpaque(false);
        btnRow.add(annualReportBtn);
        btnRow.add(certBtn);
        panel.add(btnRow);

        return panel;
    }

    private JPanel buildAboutSection() {
        JPanel panel = createSectionPanel("关于");

        JLabel titleLabel = new JLabel(AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createVerticalStrut(4));

        JLabel descLabel = new JLabel("天文观测日志与星体管理系统");
        descLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        descLabel.setForeground(Color.GRAY);
        panel.add(descLabel);

        panel.add(Box.createVerticalStrut(4));

        JLabel techLabel = new JLabel("Java SE 17 + Swing + MySQL 8.0");
        techLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        techLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        techLabel.setForeground(Color.GRAY);
        panel.add(techLabel);

        panel.add(Box.createVerticalStrut(4));

        JLabel copyLabel = new JLabel("2026  AstroLog Team");
        copyLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        copyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        copyLabel.setForeground(Color.GRAY);
        panel.add(copyLabel);

        return panel;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(title),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private void applyFontSize(int size) {
        saveSetting("font.size", String.valueOf(size));

        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parent != null) {
            applyFontToTree(parent, size);
        }
    }

    public static void applyFontToTree(java.awt.Container root, int size) {
        Font df = new Font("Microsoft YaHei", Font.PLAIN, size);
        Font bf = new Font("Microsoft YaHei", Font.BOLD, size);
        javax.swing.UIManager.put("Label.font", df);
        javax.swing.UIManager.put("Button.font", df);
        javax.swing.UIManager.put("TextField.font", df);
        javax.swing.UIManager.put("TextArea.font", df);
        javax.swing.UIManager.put("Table.font", df);
        javax.swing.UIManager.put("TableHeader.font", bf);
        javax.swing.UIManager.put("TabbedPane.font", df);
        applyFontRecursive(root, df, size);
    }

    private static void applyFontRecursive(java.awt.Container c, Font df, int size) {
        for (java.awt.Component child : c.getComponents()) {
            Font f = child.getFont();
            if (f != null) {
                // Preserve SANS_SERIF for unicode symbol components (icons, emoji)
                if ("SansSerif".equals(f.getFamily()) || Font.SANS_SERIF.equals(f.getName())) {
                    child.setFont(new Font(Font.SANS_SERIF, f.getStyle(), size));
                } else {
                    child.setFont(new Font("Microsoft YaHei", f.getStyle(), size));
                }
            }
            if (child instanceof java.awt.Container) {
                applyFontRecursive((java.awt.Container) child, df, size);
            }
        }
    }

    private void switchTheme(String themeName) {
        ThemeConfig.Theme theme;
        switch (themeName) {
            case "light": theme = ThemeConfig.LIGHT; break;
            case "starry": theme = ThemeConfig.STARRY; break;
            default: theme = ThemeConfig.DARK; break;
        }
        ThemeManager.getInstance().setTheme(theme);
        saveSetting("theme", themeName);

        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        if (parent != null) {
            parent.dispose();
            MainFrame newFrame = new MainFrame(currentUser);
            newFrame.setVisible(true);
        }
    }

    private static final String SETTINGS_FILE = "astrolog_settings.properties";

    public static String loadSetting(String key) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            props.load(fis);
        } catch (IOException ignored) {}
        return props.getProperty(key);
    }

    private void saveSetting(String key, String value) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            props.load(fis);
        } catch (IOException ignored) {}
        props.setProperty(key, value);
        try (OutputStream os = new FileOutputStream(SETTINGS_FILE)) {
            props.store(os, "AstroLog Settings");
        } catch (IOException ignored) {}
    }
}
