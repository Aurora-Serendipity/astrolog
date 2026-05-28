package com.astrolog.ui.panel;

import com.astrolog.model.CelestialBody;
import com.astrolog.model.NightSkyData;
import com.astrolog.model.ObservationSite;
import com.astrolog.model.User;
import com.astrolog.model.enums.MoonPhase;
import com.astrolog.service.NightSkyService;
import com.astrolog.service.SiteService;
import com.astrolog.ui.frame.MainFrame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class NightSkyPanel extends JPanel {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String[] MOON_SYMBOLS = {
        "🌑", "🌒", "🌓", "🌔",
        "🌕", "🌖", "🌗", "🌘"
    };

    private final User currentUser;
    private final MainFrame mainFrame;
    private final NightSkyService skyService;
    private final SiteService siteService;

    private JTextField dateField;
    private JTextField latField;
    private JTextField lonField;
    private JComboBox<String> siteCombo;
    private List<ObservationSite> siteList;

    private JTabbedPane resultTabs;
    private JPanel overviewPanel;
    private JLabel moonLabel;
    private JLabel sunsetLabel;
    private JLabel goldenLabel;
    private JLabel moonRiseLabel;
    private JLabel constellationLabel;
    private JTable starTable;
    private StarTableModel starTableModel;
    private JTable messierTable;
    private MessierTableModel messierTableModel;
    private JLabel statusLabel;
    private JButton queryBtn;

    private NightSkyData currentData;

    public NightSkyPanel(User currentUser, MainFrame mainFrame) {
        this.currentUser = currentUser;
        this.mainFrame = mainFrame;
        this.skyService = new NightSkyService();
        this.siteService = new SiteService();

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("查询条件"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(label("日期:"), gbc);
        dateField = new JTextField(LocalDate.now().format(DATE_FMT), 10);
        dateField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        gbc.gridx = 1;
        inputPanel.add(dateField, gbc);

        JButton todayBtn = smallBtn("今天");
        todayBtn.addActionListener(e -> dateField.setText(LocalDate.now().format(DATE_FMT)));
        gbc.gridx = 2;
        inputPanel.add(todayBtn, gbc);

        JButton yesterdayBtn = smallBtn("昨天");
        yesterdayBtn.addActionListener(e -> dateField.setText(LocalDate.now().minusDays(1).format(DATE_FMT)));
        gbc.gridx = 3;
        inputPanel.add(yesterdayBtn, gbc);

        JButton tomorrowBtn = smallBtn("明天");
        tomorrowBtn.addActionListener(e -> dateField.setText(LocalDate.now().plusDays(1).format(DATE_FMT)));
        gbc.gridx = 4;
        inputPanel.add(tomorrowBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(label("纬度(°):"), gbc);
        latField = new JTextField(8);
        latField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        if (currentUser.getDefaultLat() != null) {
            latField.setText(currentUser.getDefaultLat().toString());
        }
        gbc.gridx = 1;
        inputPanel.add(latField, gbc);

        gbc.gridx = 2;
        inputPanel.add(label("经度(°):"), gbc);
        lonField = new JTextField(8);
        lonField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        if (currentUser.getDefaultLon() != null) {
            lonField.setText(currentUser.getDefaultLon().toString());
        }
        gbc.gridx = 3;
        inputPanel.add(lonField, gbc);

        JButton defaultCoordBtn = smallBtn("使用默认坐标");
        defaultCoordBtn.addActionListener(e -> {
            if (currentUser.getDefaultLat() != null) {
                latField.setText(currentUser.getDefaultLat().toString());
            }
            if (currentUser.getDefaultLon() != null) {
                lonField.setText(currentUser.getDefaultLon().toString());
            }
        });
        gbc.gridx = 4;
        inputPanel.add(defaultCoordBtn, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(label("选择观测地:"), gbc);
        siteCombo = new JComboBox<>();
        siteCombo.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        siteCombo.addItem("-- 手动输入坐标 --");
        loadSiteCombo();
        siteCombo.addActionListener(e -> {
            int idx = siteCombo.getSelectedIndex();
            if (idx > 0 && idx - 1 < siteList.size()) {
                ObservationSite site = siteList.get(idx - 1);
                latField.setText(site.getLatitude() != null ? site.getLatitude().toString() : "");
                lonField.setText(site.getLongitude() != null ? site.getLongitude().toString() : "");
            }
        });
        gbc.gridx = 1; gbc.gridwidth = 2;
        inputPanel.add(siteCombo, gbc);

        queryBtn = new JButton("查询今夜星空");
        queryBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        gbc.gridx = 2; gbc.gridy = 3; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        inputPanel.add(queryBtn, gbc);

        add(inputPanel, BorderLayout.NORTH);

        resultTabs = new JTabbedPane();
        resultTabs.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

        overviewPanel = new JPanel(new GridBagLayout());
        overviewPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        resultTabs.addTab("概览", new JScrollPane(overviewPanel));

        starTableModel = new StarTableModel();
        starTable = new JTable(starTableModel);
        starTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        starTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        starTable.setRowHeight(26);
        JPanel starPanel = new JPanel(new BorderLayout());
        starPanel.add(new JScrollPane(starTable), BorderLayout.CENTER);
        JButton starObsBtn = new JButton("开始观测（选中亮星）");
        starObsBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        starObsBtn.addActionListener(e -> {
            int row = starTable.getSelectedRow();
            if (row >= 0 && currentData != null && currentData.getVisibleBodies() != null) {
                openAddObservation(currentData.getVisibleBodies().get(row));
            } else {
                JOptionPane.showMessageDialog(this, "请先在表格中选择一颗亮星", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        starPanel.add(starObsBtn, BorderLayout.SOUTH);
        resultTabs.addTab("亮星推荐", starPanel);

        messierTableModel = new MessierTableModel();
        messierTable = new JTable(messierTableModel);
        messierTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        messierTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        messierTable.setRowHeight(26);
        JPanel messierPanel = new JPanel(new BorderLayout());
        messierPanel.add(new JScrollPane(messierTable), BorderLayout.CENTER);
        JButton messierObsBtn = new JButton("开始观测（选中梅西耶天体）");
        messierObsBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        messierObsBtn.addActionListener(e -> {
            int row = messierTable.getSelectedRow();
            if (row >= 0 && currentData != null && currentData.getVisibleMessier() != null) {
                openAddObservation(currentData.getVisibleMessier().get(row));
            } else {
                JOptionPane.showMessageDialog(this, "请先在表格中选择一个梅西耶天体", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        messierPanel.add(messierObsBtn, BorderLayout.SOUTH);
        resultTabs.addTab("梅西耶天体", messierPanel);

        add(resultTabs, BorderLayout.CENTER);

        statusLabel = new JLabel("请输入日期和坐标，点击「查询今夜星空」");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        add(statusLabel, BorderLayout.SOUTH);

        queryBtn.addActionListener(e -> executeQuery());
    }

    private void loadSiteCombo() {
        siteList = siteService.listByUser(currentUser.getUserId());
        for (ObservationSite site : siteList) {
            siteCombo.addItem(site.getName());
        }
    }

    private void executeQuery() {
        LocalDate date;
        try {
            date = LocalDate.parse(dateField.getText().trim(), DATE_FMT);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "日期格式错误，请使用 yyyy-MM-dd",
                "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double lat, lon;
        try {
            lat = Double.parseDouble(latField.getText().trim());
            lon = Double.parseDouble(lonField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "经纬度格式不正确",
                "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (lat < -90 || lat > 90) {
            JOptionPane.showMessageDialog(this, "纬度必须在 -90 到 90 之间",
                "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (lon < -180 || lon > 180) {
            JOptionPane.showMessageDialog(this, "经度必须在 -180 到 180 之间",
                "输入错误", JOptionPane.WARNING_MESSAGE);
            return;
        }

        queryBtn.setEnabled(false);
        queryBtn.setText("查询中...");
        statusLabel.setText("正在计算今夜星空推荐...");

        SwingUtilities.invokeLater(() -> {
            try {
                currentData = skyService.recommend(date, lat, lon);
                renderResults();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "查询失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            } finally {
                queryBtn.setEnabled(true);
                queryBtn.setText("查询今夜星空");
            }
        });
    }

    private void renderResults() {
        overviewPanel.removeAll();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        MoonPhase phase = currentData.getMoonPhase();
        if (phase != null) {
            gbc.gridx = 0; gbc.gridy = row;
            String moonSymbol = MOON_SYMBOLS[phase.ordinal()];
            JLabel iconLabel = new JLabel(moonSymbol);
            iconLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 36));
            overviewPanel.add(iconLabel, gbc);
            gbc.gridx = 1;
            JLabel phaseLabel = new JLabel(phase.getDisplayName());
            phaseLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
            overviewPanel.add(phaseLabel, gbc);
            row++;
        }

        LocalTime sunset = currentData.getSunsetTime();
        if (sunset != null) {
            gbc.gridx = 0; gbc.gridy = row;
            overviewPanel.add(label("日落时间:"), gbc);
            gbc.gridx = 1;
            overviewPanel.add(valueLabel(sunset.toString()), gbc);
            row++;
        }

        LocalTime gwStart = currentData.getGoldenWindowStart();
        LocalTime gwEnd = currentData.getGoldenWindowEnd();
        if (gwStart != null && gwEnd != null) {
            gbc.gridx = 0; gbc.gridy = row;
            overviewPanel.add(label("黄金观测窗口:"), gbc);
            gbc.gridx = 1;
            JLabel gwLabel = valueLabel(gwStart + " — " + gwEnd);
            gwLabel.setForeground(new Color(180, 130, 30));
            gwLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
            overviewPanel.add(gwLabel, gbc);
            row++;
        }

        if (currentData.getMoonRise() != null) {
            gbc.gridx = 0; gbc.gridy = row;
            overviewPanel.add(label("月出:"), gbc);
            gbc.gridx = 1;
            overviewPanel.add(valueLabel(currentData.getMoonRise().toString()), gbc);
            row++;
        }

        if (currentData.getMoonSet() != null) {
            gbc.gridx = 0; gbc.gridy = row;
            overviewPanel.add(label("月落:"), gbc);
            gbc.gridx = 1;
            overviewPanel.add(valueLabel(currentData.getMoonSet().toString()), gbc);
            row++;
        }

        List<String> constellations = currentData.getVisibleConstellations();
        boolean hasConstellations = constellations != null && !constellations.isEmpty();
        boolean hasStars = currentData.getVisibleBodies() != null && !currentData.getVisibleBodies().isEmpty();
        boolean hasMessier = currentData.getVisibleMessier() != null && !currentData.getVisibleMessier().isEmpty();
        gbc.gridx = 0; gbc.gridy = row;
        overviewPanel.add(label("可见星座:"), gbc);
        gbc.gridx = 1;
        if (hasConstellations && (hasStars || hasMessier)) {
            JLabel consLabel = valueLabel(String.join("、", constellations));
            overviewPanel.add(consLabel, gbc);
        } else {
            String hint = hasConstellations
                ? "当前可见星座无推荐的亮星或梅西耶天体，建议选择其他日期或地点"
                : "今晚可见条件不佳，建议选择其他日期或地点";
            JLabel emptyLabel = valueLabel(hint);
            emptyLabel.setForeground(Color.GRAY);
            overviewPanel.add(emptyLabel, gbc);
        }
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        overviewPanel.add(new JLabel(" "), gbc);

        overviewPanel.revalidate();
        overviewPanel.repaint();

        starTableModel.fireTableDataChanged();
        messierTableModel.fireTableDataChanged();

        int starCount = currentData.getVisibleBodies() != null ? currentData.getVisibleBodies().size() : 0;
        int messierCount = currentData.getVisibleMessier() != null ? currentData.getVisibleMessier().size() : 0;
        int consCount = constellations != null ? constellations.size() : 0;
        statusLabel.setText(String.format("今晚可见 %d 个星座，推荐 %d 颗亮星，%d 个梅西耶天体",
            consCount, starCount, messierCount));
    }

    private void openAddObservation(CelestialBody body) {
        mainFrame.switchTo("我的观测");
        SwingUtilities.invokeLater(() -> mainFrame.openAddObservation(body));
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        return lbl;
    }

    private JLabel valueLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        return lbl;
    }

    private JButton smallBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        return btn;
    }

    private class StarTableModel extends AbstractTableModel {
        private final String[] columns = {"名称", "星座", "视星等", "类型"};

        @Override
        public int getRowCount() {
            if (currentData == null || currentData.getVisibleBodies() == null) return 0;
            return currentData.getVisibleBodies().size();
        }

        @Override
        public int getColumnCount() { return 4; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            CelestialBody b = currentData.getVisibleBodies().get(row);
            switch (col) {
                case 0: return b.getName();
                case 1: return b.getConstellation();
                case 2: return b.getMagnitude();
                case 3: return b.getType().getDisplayName();
                default: return "";
            }
        }
    }

    private class MessierTableModel extends AbstractTableModel {
        private final String[] columns = {"M#", "名称", "类型", "星座", "视星等"};

        @Override
        public int getRowCount() {
            if (currentData == null || currentData.getVisibleMessier() == null) return 0;
            return currentData.getVisibleMessier().size();
        }

        @Override
        public int getColumnCount() { return 5; }

        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            CelestialBody b = currentData.getVisibleMessier().get(row);
            switch (col) {
                case 0: return "M" + b.getMessierNumber();
                case 1: return b.getName();
                case 2: return b.getType().getDisplayName();
                case 3: return b.getConstellation();
                case 4: return b.getMagnitude();
                default: return "";
            }
        }
    }
}
