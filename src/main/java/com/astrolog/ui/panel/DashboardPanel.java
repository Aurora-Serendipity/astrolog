package com.astrolog.ui.panel;

import com.astrolog.config.AppConfig;
import com.astrolog.config.ThemeConfig;
import com.astrolog.model.User;
import com.astrolog.model.enums.UserRole;
import com.astrolog.service.StatsService;
import com.astrolog.ui.component.ThemeManager;
import com.astrolog.ui.frame.MainFrame;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardPanel extends JPanel {

    private final MainFrame mainFrame;
    private final User currentUser;
    private final StatsService statsService;
    private JLabel dateTimeLabel;
    private JLabel statsLabel;

    private static final String[][] QUICK_ENTRIES = {
        {"星体库", "浏览和管理您的星体数据库", "★"},
        {"添加观测", "记录新的天文观测", "🔭"},
        {"今夜星空", "查看今晚可见星体推荐", "🌙"},
        {"统计图表", "查看观测统计与分析", "📊"},
        {"梅西耶马拉松", "追踪梅西耶天体观测进度", "🏃"},
        {"系统设置", "个性化应用配置", "⚙"},
        {"星体管理", "管理星体数据库条目", "✎"}
    };

    private static final String[] CARD_NAMES = {
        "星体库", "我的观测", "今夜星空", "统计图表", "梅西耶马拉松", "系统设置", "星体库"
    };

    public DashboardPanel(User user, MainFrame mainFrame) {
        this.currentUser = user;
        this.mainFrame = mainFrame;
        this.statsService = new StatsService();
        initComponents();
        updateDateTime();
        refreshStats();
    }

    private void initComponents() {
        ThemeConfig.Theme theme = ThemeManager.getInstance().getCurrentTheme();
        setLayout(new BorderLayout());
        setBackground(theme.bg());

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(theme.accent());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel welcomeLabel = new JLabel("欢迎回来，" + currentUser.getUsername());
        welcomeLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(welcomeLabel);

        headerPanel.add(Box.createVerticalStrut(10));

        dateTimeLabel = new JLabel();
        dateTimeLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        dateTimeLabel.setForeground(new Color(200, 220, 255));
        dateTimeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(dateTimeLabel);

        add(headerPanel, BorderLayout.NORTH);

        int cardCount = currentUser.getRole() == UserRole.ADMIN ? 7 : 6;
        JPanel cardsPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        cardsPanel.setBackground(theme.bg());
        cardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        for (int i = 0; i < cardCount; i++) {
            cardsPanel.add(createQuickEntryCard(i, theme));
        }

        add(cardsPanel, BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 10));
        summaryPanel.setBackground(theme.panelBg());
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        statsLabel = new JLabel();
        statsLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        statsLabel.setForeground(theme.fg());
        summaryPanel.add(statsLabel);

        add(summaryPanel, BorderLayout.SOUTH);
    }

    private JPanel createQuickEntryCard(int index, ThemeConfig.Theme theme) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(theme.tableBg());
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel iconLabel = new JLabel(QUICK_ENTRIES[index][2]);
        iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 28));
        iconLabel.setForeground(theme.fg());
        card.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(theme.tableBg());

        JLabel titleLabel = new JLabel(QUICK_ENTRIES[index][0]);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 16));
        titleLabel.setForeground(theme.fg());
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(titleLabel);

        textPanel.add(Box.createVerticalStrut(5));

        JLabel descLabel = new JLabel(QUICK_ENTRIES[index][1]);
        descLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        descLabel.setForeground(dimColor(theme.fg()));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        textPanel.add(descLabel);

        card.add(textPanel, BorderLayout.CENTER);

        String targetCard = CARD_NAMES[index];
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                mainFrame.switchTo(targetCard);
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(theme.accent().brighter().brighter());
                textPanel.setBackground(theme.accent().brighter().brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(theme.tableBg());
                textPanel.setBackground(theme.tableBg());
            }
        });

        return card;
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        String formatted = now.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日  EEEE  HH:mm"));
        dateTimeLabel.setText(formatted);
    }

    public void setStats(int obsCount, int bodyCount, int equipCount) {
        statsLabel.setText(obsCount + " 次观测 | " + bodyCount + " 个星体 | " + equipCount + " 件器材");
    }

    private Color dimColor(Color c) {
        return new Color(
            Math.max(0, c.getRed() - 80),
            Math.max(0, c.getGreen() - 80),
            Math.max(0, c.getBlue() - 80));
    }

    private void refreshStats() {
        int userId = currentUser.getUserId();
        long totalObs = statsService.totalObservations(userId);
        long distinctBodies = statsService.distinctBodiesObserved(userId);
        long totalEquip = statsService.totalEquipmentUsed(userId);
        setStats((int) totalObs, (int) distinctBodies, (int) totalEquip);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateDateTime();
        refreshStats();
    }
}
