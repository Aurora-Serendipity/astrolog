package com.astrolog.ui.frame;

import com.astrolog.config.AppConfig;
import com.astrolog.config.ThemeConfig;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.User;
import com.astrolog.service.ExportService;
import com.astrolog.ui.component.ThemeManager;
import com.astrolog.ui.dialog.ReportViewDialog;
import com.astrolog.ui.panel.CelestialBodyPanel;
import com.astrolog.ui.panel.ConstellationPanel;
import com.astrolog.ui.panel.EquipmentPanel;
import com.astrolog.ui.panel.DashboardPanel;
import com.astrolog.ui.panel.MessierPanel;
import com.astrolog.ui.panel.NightSkyPanel;
import com.astrolog.ui.panel.ObservationPanel;
import com.astrolog.ui.panel.ObservationSitePanel;
import com.astrolog.ui.panel.SettingsPanel;
import com.astrolog.ui.panel.StatsPanel;
import com.astrolog.ui.panel.UserPanel;
import com.astrolog.util.DBUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class MainFrame extends JFrame {

    private static final ResourceBundle i18n =
        ResourceBundle.getBundle("i18n.messages_zh");

    private static final String[] NAV_LABELS = {
        "仪表盘", "星体库", "我的观测", "器材柜", "观测地",
        "今夜星空", "统计图表", "梅西耶马拉松", "星座文化馆", "系统设置", "用户中心"
    };

    private static final String[] PLACEHOLDER_TEXTS = {
        "", "星体库 - 将在S3实现", "我的观测 - 将在S5实现", "",
        "观测地 - 将在S7实现", "今夜星空 - 将在S7实现", "",
        "梅西耶马拉松 - 将在S8实现", "星座文化馆 - 将在S8实现", "系统设置 - 将在S9实现",
        ""
    };

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel statusLabel;
    private User currentUser;
    private ObservationPanel obsPanel;
    private CelestialBodyPanel bodyPanel;

    public MainFrame(User user) {
        this.currentUser = user;
        initComponents();
        setTitle(AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1024, 700));
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DBUtil.getInstance().shutdown();
                System.exit(0);
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu(i18n.getString("menu.file"));
        JMenuItem exitItem = new JMenuItem(i18n.getString("menu.file.exit"));
        exitItem.addActionListener(e -> {
            DBUtil.getInstance().shutdown();
            System.exit(0);
        });
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu dataMenu = new JMenu(i18n.getString("menu.data"));
        JMenuItem backupItem = new JMenuItem(i18n.getString("settings.backup"));
        backupItem.addActionListener(e -> cardLayout.show(contentPanel, "系统设置"));
        dataMenu.add(backupItem);
        menuBar.add(dataMenu);

        JMenu statsMenu = new JMenu(i18n.getString("menu.stats"));
        JMenuItem reportItem = new JMenuItem(i18n.getString("settings.annualReport"));
        reportItem.addActionListener(e -> generateAndShowReport());
        statsMenu.add(reportItem);
        menuBar.add(statsMenu);

        JMenu helpMenu = new JMenu(i18n.getString("menu.help"));
        JMenuItem aboutItem = new JMenuItem(i18n.getString("menu.help.about"));
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(this,
            AppConfig.APP_NAME + " v1.0\n" + i18n.getString("app.title") + "\n\n"
                + i18n.getString("settings.techStack"),
            i18n.getString("menu.help.about"), JOptionPane.INFORMATION_MESSAGE));
        helpMenu.add(aboutItem);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        ThemeConfig.Theme theme = ThemeManager.getInstance().getCurrentTheme();

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new GridLayout(NAV_LABELS.length, 1, 5, 5));
        navPanel.setPreferredSize(new Dimension(180, getHeight()));
        navPanel.setBackground(theme.panelBg());
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout) {
            private final java.util.List<Point> stars = new java.util.ArrayList<>();
            private final java.util.Random rand = new java.util.Random(42);
            {
                for (int i = 0; i < 200; i++) {
                    stars.add(new Point(rand.nextInt(2000), rand.nextInt(2000)));
                }
            }
            @Override
            protected void paintComponent(Graphics g) {
                if (ThemeManager.getInstance().getCurrentTheme() == ThemeConfig.STARRY) {
                    g.setColor(new Color(10, 10, 40));
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(new Color(255, 255, 255, 60));
                    for (Point star : stars) {
                        int sx = star.x % Math.max(1, getWidth());
                        int sy = star.y % Math.max(1, getHeight());
                        int size = rand.nextInt(2) + 1;
                        g.fillOval(sx, sy, size, size);
                    }
                } else {
                    super.paintComponent(g);
                }
            }
        };
        contentPanel.setBackground(theme.tableBg());

        DashboardPanel dashboardPanel = new DashboardPanel(currentUser, this);
        contentPanel.add(dashboardPanel, "仪表盘");

        for (int i = 1; i < NAV_LABELS.length; i++) {
            if (NAV_LABELS[i].equals("星体库")) {
                bodyPanel = new CelestialBodyPanel(currentUser);
                contentPanel.add(bodyPanel, "星体库");
            } else if (NAV_LABELS[i].equals("器材柜")) {
                EquipmentPanel equipPanel = new EquipmentPanel(currentUser);
                contentPanel.add(equipPanel, "器材柜");
            } else if (NAV_LABELS[i].equals("我的观测")) {
                obsPanel = new ObservationPanel(currentUser);
                contentPanel.add(obsPanel, "我的观测");
            } else if (NAV_LABELS[i].equals("统计图表")) {
                StatsPanel statsPanel = new StatsPanel(currentUser);
                contentPanel.add(statsPanel, "统计图表");
            } else if (NAV_LABELS[i].equals("观测地")) {
                ObservationSitePanel sitePanel = new ObservationSitePanel(currentUser);
                contentPanel.add(sitePanel, "观测地");
            } else if (NAV_LABELS[i].equals("今夜星空")) {
                NightSkyPanel skyPanel = new NightSkyPanel(currentUser, this);
                contentPanel.add(skyPanel, "今夜星空");
            } else if (NAV_LABELS[i].equals("梅西耶马拉松")) {
                MessierPanel messierPanel = new MessierPanel(currentUser, this);
                contentPanel.add(messierPanel, "梅西耶马拉松");
            } else if (NAV_LABELS[i].equals("星座文化馆")) {
                ConstellationPanel constPanel = new ConstellationPanel(currentUser);
                contentPanel.add(constPanel, "星座文化馆");
            } else if (NAV_LABELS[i].equals("系统设置")) {
                SettingsPanel settingsPanel = new SettingsPanel(currentUser);
                contentPanel.add(settingsPanel, "系统设置");
            } else {
                JPanel placeholder = new JPanel(new GridBagLayout());
                placeholder.setBackground(new Color(240, 240, 240));
                JLabel label = new JLabel(PLACEHOLDER_TEXTS[i]);
                label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
                label.setForeground(Color.GRAY);
                placeholder.add(label);
                contentPanel.add(placeholder, NAV_LABELS[i]);
            }
        }

        UserPanel userPanel = new UserPanel(currentUser);
        contentPanel.add(userPanel, "用户中心");

        // Apply theme background to all content panels; STARRY: transparent for stars
        for (Component comp : contentPanel.getComponents()) {
            if (theme == ThemeConfig.STARRY) {
                ((JComponent) comp).setOpaque(false);
            } else {
                comp.setBackground(theme.tableBg());
            }
        }

        for (int i = 0; i < NAV_LABELS.length; i++) {
            JButton navButton = new JButton(NAV_LABELS[i]);
            navButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
            navButton.setBackground(theme.buttonBg());
            navButton.setForeground(theme.fg());
            navButton.setFocusPainted(false);
            final String cardName = NAV_LABELS[i];
            navButton.addActionListener(e -> cardLayout.show(contentPanel, cardName));
            navPanel.add(navButton);
        }

        add(navPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        statusLabel = new JLabel(i18n.getString("status.currentUser") + ": " + currentUser.getUsername()
            + " | " + i18n.getString("status.role") + ": " + currentUser.getRole().getDisplayName()
            + " | " + i18n.getString("status.welcome"));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setBackground(theme.panelBg());
        statusLabel.setForeground(theme.fg());
        statusLabel.setOpaque(true);
        add(statusLabel, BorderLayout.SOUTH);

        String savedFontSize = SettingsPanel.loadSetting("font.size");
        if (savedFontSize != null) {
            try {
                SettingsPanel.applyFontToTree(this, Integer.parseInt(savedFontSize));
            } catch (NumberFormatException ignored) {}
        }
    }

    public void switchTo(String cardName) {
        cardLayout.show(contentPanel, cardName);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
    }

    public String getCurrentUser() { return currentUser.getUsername(); }
    public User getCurrentUserObject() { return currentUser; }

    public void openAddObservation(CelestialBody body) {
        if (obsPanel != null) {
            obsPanel.showAddDialogForBody(body);
        }
    }

    public CelestialBodyPanel getCelestialBodyPanel() {
        return bodyPanel;
    }

    private void generateAndShowReport() {
        try {
            ExportService es = new ExportService();
            int year = LocalDate.now().getYear();
            String html = es.buildHtmlContent(currentUser, year);
            new ReportViewDialog(this, html, currentUser, year);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "生成报告失败: " + ex.getMessage(),
                "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
