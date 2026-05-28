package com.astrolog.ui.panel;

import com.astrolog.dao.BodyDao;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.ConstellationInfo;
import com.astrolog.model.User;
import com.astrolog.model.enums.BodyType;
import com.astrolog.service.StatsService;
import com.astrolog.ui.component.SkyCalendarHeatmap;
import com.astrolog.ui.component.StarMapCanvas;
import com.astrolog.util.ChartUtil;
import com.astrolog.util.JsonDataLoader;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class StatsPanel extends JPanel {

    private static final int REFRESH_INTERVAL_MS = 15000;

    private final StatsService statsService;
    private final BodyDao bodyDao;
    private final int userId;
    private final JTabbedPane tabbedPane;
    private final Timer refreshTimer;
    private JPanel bottomPanel;

    public StatsPanel(User currentUser) {
        this.userId = currentUser.getUserId();
        this.statsService = new StatsService();
        this.bodyDao = new BodyDao();
        this.tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        this.tabbedPane.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

        setLayout(new BorderLayout());
        buildAllTabs();
        add(tabbedPane, BorderLayout.CENTER);

        bottomPanel = createEquipmentRanking();
        add(bottomPanel, BorderLayout.SOUTH);

        refreshTimer = new Timer(REFRESH_INTERVAL_MS, e -> refreshAll());
        refreshTimer.start();
    }

    private void buildAllTabs() {
        tabbedPane.removeAll();
        tabbedPane.addTab("年度统计", createYearChart());
        tabbedPane.addTab("月度统计", createMonthChart());
        tabbedPane.addTab("类型分布", createTypePieChart());
        tabbedPane.addTab("年度趋势", createYearTrendChart());
        tabbedPane.addTab("观测条件", createConditionRadar());
        tabbedPane.addTab("日历热力", createHeatmap());
        tabbedPane.addTab("星座分布", createStarMap());
    }

    private void refreshAll() {
        int selected = tabbedPane.getSelectedIndex();
        buildAllTabs();
        if (selected >= 0 && selected < tabbedPane.getTabCount()) {
            tabbedPane.setSelectedIndex(selected);
        }
        remove(bottomPanel);
        bottomPanel = createEquipmentRanking();
        add(bottomPanel, BorderLayout.SOUTH);
        revalidate();
        repaint();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (refreshTimer != null) {
            refreshTimer.restart();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
    }

    // ==================== 年度统计 ====================

    private Component createYearChart() {
        Map<Integer, Long> data = statsService.countByYear(userId);
        if (data.isEmpty()) {
            return emptyPanel("暂无观测数据");
        }
        Map<String, Long> chartData = new LinkedHashMap<>();
        for (Map.Entry<Integer, Long> e : data.entrySet()) {
            chartData.put(String.valueOf(e.getKey()), e.getValue());
        }
        return ChartUtil.createBarChart("年度观测频次", chartData, "年份", "观测次数");
    }

    // ==================== 月度统计 ====================

    private Component createMonthChart() {
        Map<YearMonth, Long> data = statsService.countByMonth(userId);
        if (data.isEmpty()) {
            return emptyPanel("暂无观测数据");
        }
        Map<String, Long> chartData = new LinkedHashMap<>();
        for (Map.Entry<YearMonth, Long> e : data.entrySet()) {
            chartData.put(e.getKey().toString(), e.getValue());
        }
        return ChartUtil.createBarChart("月度观测频次", chartData, "月份", "观测次数");
    }

    // ==================== 类型分布 ====================

    private Component createTypePieChart() {
        Map<BodyType, Long> data = statsService.countByBodyType(userId);
        if (data.isEmpty()) {
            return emptyPanel("暂无观测数据");
        }
        Map<String, Long> chartData = new LinkedHashMap<>();
        for (Map.Entry<BodyType, Long> e : data.entrySet()) {
            chartData.put(e.getKey().getDisplayName(), e.getValue());
        }
        return ChartUtil.createPieChart("星体类型分布", chartData);
    }

    // ==================== 年度趋势 ====================

    private Component createYearTrendChart() {
        Map<Integer, Long> data = statsService.countByYear(userId);
        if (data.isEmpty()) {
            return emptyPanel("暂无观测数据");
        }
        Map<String, Long> chartData = new LinkedHashMap<>();
        for (Map.Entry<Integer, Long> e : data.entrySet()) {
            chartData.put(String.valueOf(e.getKey()), e.getValue());
        }
        return ChartUtil.createLineChart("年度变化趋势", chartData, "年份", "观测次数");
    }

    // ==================== 观测条件雷达图 ====================

    private Component createConditionRadar() {
        Map<Integer, Long> seeingData = statsService.countBySeeing(userId);
        if (seeingData.isEmpty()) {
            return emptyPanel("暂无观测数据");
        }

        double totalSeeing = 0;
        long seeingCount = 0;
        for (Map.Entry<Integer, Long> e : seeingData.entrySet()) {
            totalSeeing += e.getKey() * e.getValue();
            seeingCount += e.getValue();
        }
        double avgSeeing = seeingCount > 0 ? (totalSeeing / seeingCount) * 2 : 0;

        Map<String, Long> weatherData = statsService.countByWeather(userId);
        long clearCount = 0;
        long weatherTotal = 0;
        for (Map.Entry<String, Long> e : weatherData.entrySet()) {
            weatherTotal += e.getValue();
            if (e.getKey().contains("晴")) {
                clearCount += e.getValue();
            }
        }
        double clearRate = weatherTotal > 0 ? (double) clearCount / weatherTotal * 10 : 0;

        Map<String, Long> moonData = statsService.countByMoonPhase(userId);
        long favorableMoon = 0;
        long moonTotal = 0;
        for (Map.Entry<String, Long> e : moonData.entrySet()) {
            moonTotal += e.getValue();
            String name = e.getKey();
            if (name.equals("新月") || name.equals("残月") || name.equals("蛾眉月")) {
                favorableMoon += e.getValue();
            }
        }
        double moonScore = moonTotal > 0 ? (double) favorableMoon / moonTotal * 10 : 0;

        long equipCount = statsService.totalEquipmentUsed(userId);
        double equipScore = Math.min(10, equipCount * 2);

        Map<String, Double> radarData = new LinkedHashMap<>();
        radarData.put("视宁度", Math.min(10, avgSeeing));
        radarData.put("晴夜率", clearRate);
        radarData.put("月相适宜", moonScore);
        radarData.put("器材多样性", equipScore);

        return ChartUtil.createRadarChart("观测条件分析", radarData);
    }

    // ==================== 日历热力图 ====================

    private Component createHeatmap() {
        Map<LocalDate, Long> data = statsService.dailyCounts(userId);
        if (data.isEmpty()) {
            return emptyPanel("暂无观测数据");
        }

        SkyCalendarHeatmap heatmap = new SkyCalendarHeatmap(data);
        int currentYear = heatmap.getYear();

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(30, 30, 50));

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        navPanel.setBackground(new Color(30, 30, 50));
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        JLabel yearLabel = new JLabel(String.valueOf(currentYear));
        yearLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        yearLabel.setForeground(Color.LIGHT_GRAY);

        prevBtn.addActionListener(e -> {
            heatmap.setYear(heatmap.getYear() - 1);
            yearLabel.setText(String.valueOf(heatmap.getYear()));
        });
        nextBtn.addActionListener(e -> {
            heatmap.setYear(heatmap.getYear() + 1);
            yearLabel.setText(String.valueOf(heatmap.getYear()));
        });

        navPanel.add(prevBtn);
        navPanel.add(yearLabel);
        navPanel.add(nextBtn);
        container.add(navPanel, BorderLayout.NORTH);
        container.add(heatmap, BorderLayout.CENTER);

        return container;
    }

    // ==================== 星座分布图 ====================

    private Component createStarMap() {
        List<CelestialBody> allBodies = bodyDao.findAll();
        if (allBodies.isEmpty()) {
            return emptyPanel("暂无星体数据");
        }

        Set<Integer> observedIds = statsService.observedBodyIds(userId);
        List<CelestialBody> observedBodies = allBodies.stream()
            .filter(b -> observedIds.contains(b.getBodyId()))
            .collect(Collectors.toList());

        List<ConstellationInfo> constellations = JsonDataLoader.loadList(
            "data/constellations.json", ConstellationInfo[].class);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(new Color(10, 10, 40));
        wrapper.add(new StarMapCanvas(observedBodies, allBodies, constellations),
            BorderLayout.CENTER);
        return wrapper;
    }

    // ==================== 器材排行 ====================

    private JPanel createEquipmentRanking() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("器材使用排行 (Top 10)"));

        List<Map.Entry<String, Long>> ranking = statsService.topEquipment(userId);

        String[] columns = {"器材名称", "使用次数"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        for (Map.Entry<String, Long> e : ranking) {
            model.addRow(new Object[]{e.getKey(), e.getValue()});
        }

        if (ranking.isEmpty()) {
            model.addRow(new Object[]{"暂无器材使用记录", 0});
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        table.setRowHeight(24);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new java.awt.Dimension(0, 150));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // ==================== 空数据面板 ====================

    private JPanel emptyPanel(String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 240, 240));
        GridBagConstraints gbc = new GridBagConstraints();
        JLabel label = new JLabel(message, SwingConstants.CENTER);
        label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
        label.setForeground(Color.GRAY);
        panel.add(label, gbc);
        return panel;
    }
}
