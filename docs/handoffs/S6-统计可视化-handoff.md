# S6 — 统计可视化 交接文档

> **子阶段：** S6 统计可视化
> **所属阶段：** 阶段二 — 功能切片迭代（最后一个子阶段，完成后进入阶段二关口审查）
> **编制日期：** 2026-05-22
> **上级会话状态：** S5 已完成（103/103 测试通过，G1-G4 审核通过），S6 是阶段二收尾

---

## 1. 前置状态速览

### 1.1 已交付资产

| 资产                  | 路径                         | S6 用途                                          |
| --------------------- | ---------------------------- | ------------------------------------------------ |
| `observations` 表     | S5 ObsDao                    | 统计原始数据：obs_time/seeing/weather/moon_phase |
| `celestial_bodies` 表 | S3 BodyDao                   | 星体类型分布统计                                 |
| `equipment` 表        | S4 EquipDao                  | 器材使用排行统计                                 |
| `BodyType` 枚举       | `model/enums/BodyType.java`  | 饼图分类标签                                     |
| `MoonPhase` 枚举      | `model/enums/MoonPhase.java` | 月相维度统计                                     |
| `ObsDao`              | `dao/ObsDao.java`            | S5，提供 findAllByUserId / search                |
| `BodyDao`             | `dao/BodyDao.java`           | S3，提供 findAll / findById                      |
| `EquipDao`            | `dao/EquipDao.java`          | S4，提供 getUsageCount                           |
| `JFreeChart 1.5.4`    | pom.xml 依赖                 | 图表生成                                         |
| `ServiceResult`       | `service/ServiceResult.java` | S2                                               |
| `MainFrame`           | `ui/frame/MainFrame.java`    | `getCurrentUserObject()` / `switchTo()`          |

### 1.2 MainFrame 当前状态

NAV_LABELS[6] = "统计图表"，当前为占位 JPanel，需替换为 StatsPanel。
NAV_LABELS[9] = "系统设置"，仍为占位（S6 可顺便改成含主题切换的实际面板，或留到 S9）。

### 1.3 S2-S5 全部偏差经验（S6 必须遵循）

| 经验                                   | S6 应用                                         |
| -------------------------------------- | ----------------------------------------------- |
| COUNT 查询用原生 JDBC                  | StatsService 所有聚合查询                       |
| kill死代码（未使用的字段/import/参数） | G1 重点检查 ChartUtil/StatsPanel                |
| UI 文件禁用通配符 import               | StarMapCanvas / SkyCalendarHeatmap / StatsPanel |
| 构造器末尾 setVisible(true)            | 无 Dialog 新增，不适用                          |
| Service 需 package-private 测试构造器  | StatsService(ObsDao, BodyDao, EquipDao)         |
| N+1 避免 / 批量查询                    | StatsService 聚合查询一次返回                   |
| `@{argLine}` → `${argLine}`            | pom.xml 不做额外修改                            |

---

## 2. S6 目标与范围

### 2.1 核心目标

集成 JFreeChart，实现 7 种图表/可视化组件：柱状图（年/月）、饼图（类型分布）、折线图（年度趋势）、雷达图（观测条件）、热力日历、星座亮星分布图。

### 2.2 新增文件清单（6 个）

| #   | 文件                      | 包                          | 行数 |
| --- | ------------------------- | --------------------------- | ---- |
| 1   | `StatsService.java`       | `com.astrolog.service`      | ~280 |
| 2   | `ChartUtil.java`          | `com.astrolog.util`         | ~350 |
| 3   | `SkyCalendarHeatmap.java` | `com.astrolog.ui.component` | ~280 |
| 4   | `StarMapCanvas.java`      | `com.astrolog.ui.component` | ~250 |
| 5   | `StatsPanel.java`         | `com.astrolog.ui.panel`     | ~220 |
| 6   | `StatsServiceTest.java`   | test `unit/service`         | ~150 |

### 2.3 修改文件（1 个）

| #   | 文件             | 改动                        |
| --- | ---------------- | --------------------------- |
| 1   | `MainFrame.java` | "统计图表"占位 → StatsPanel |

### 2.4 范围边界

**不做：** 图表导出为图片（S9）、报告嵌入图表（S9 JasperReports）、ChartUtil 5-10 个测试（可选为集成验证而非单元测试）

---

## 3. 详细实现规格

### 3.1 StatsService — 统计数据查询

**文件：** `src/main/java/com/astrolog/service/StatsService.java`

```java
package com.astrolog.service;

import com.astrolog.dao.ObsDao;
import com.astrolog.dao.BodyDao;
import com.astrolog.dao.EquipDao;
import com.astrolog.model.Observation;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.Equipment;
import com.astrolog.model.enums.BodyType;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

public class StatsService {

    private final ObsDao obsDao;
    private final BodyDao bodyDao;
    private final EquipDao equipDao;

    public StatsService() {
        this.obsDao = new ObsDao();
        this.bodyDao = new BodyDao();
        this.equipDao = new EquipDao();
    }

    // package-private 测试构造器
    StatsService(ObsDao obsDao, BodyDao bodyDao, EquipDao equipDao) {
        this.obsDao = obsDao;
        this.bodyDao = bodyDao;
        this.equipDao = equipDao;
    }

    // ==================== 观测频次统计 ====================

    // 年度频次：Map<年份, 次数>
    public Map<Integer, Long> countByYear(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getObsTime() != null)
            .collect(Collectors.groupingBy(
                o -> o.getObsTime().getYear(),
                TreeMap::new, Collectors.counting()));
    }

    // 月度频次：Map<YearMonth, 次数>
    public Map<YearMonth, Long> countByMonth(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getObsTime() != null)
            .collect(Collectors.groupingBy(
                o -> YearMonth.from(o.getObsTime()),
                TreeMap::new, Collectors.counting()));
    }

    // ==================== 类型分布统计 ====================

    // 星体类型分布：Map<BodyType, 次数>
    public Map<BodyType, Long> countByBodyType(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .map(o -> bodyDao.findById(o.getBodyId()))
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(
                CelestialBody::getType,
                () -> new LinkedHashMap<>(),
                Collectors.counting()));
    }

    // ==================== 观测条件分析 ====================

    // 视宁度分布：Map<视宁度等级(1-5), 次数>
    public Map<Integer, Long> countBySeeing(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getSeeing() > 0)
            .collect(Collectors.groupingBy(
                Observation::getSeeing,
                TreeMap::new, Collectors.counting()));
    }

    // 天气分布：Map<天气字符串, 次数>
    public Map<String, Long> countByWeather(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getWeather() != null && !o.getWeather().isEmpty())
            .collect(Collectors.groupingBy(
                Observation::getWeather,
                TreeMap::new, Collectors.counting()));
    }

    // 月相分布：Map<MoonPhase, 次数>
    public Map<String, Long> countByMoonPhase(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getMoonPhase() != null)
            .collect(Collectors.groupingBy(
                o -> o.getMoonPhase().getDisplayName(),
                TreeMap::new, Collectors.counting()));
    }

    // ==================== 器材使用排行 ====================

    // 器材使用次数排行（Top 10）：List<Map.Entry<器材名, 次数>>
    public List<Map.Entry<String, Long>> topEquipment(int userId) {
        List<Equipment> equipList = equipDao.findAllByUserId(userId);
        return equipList.stream()
            .collect(Collectors.toMap(
                Equipment::getName,
                e -> (long) equipDao.getUsageCount(e.getEquipId())))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(10)
            .collect(Collectors.toList());
    }

    // ==================== 热力图数据 ====================

    // 日历热力图：Map<LocalDate, 观测次数>
    public Map<java.time.LocalDate, Long> dailyCounts(int userId) {
        List<Observation> list = obsDao.findAllByUserId(userId);
        return list.stream()
            .filter(o -> o.getObsTime() != null)
            .collect(Collectors.groupingBy(
                o -> o.getObsTime().toLocalDate(),
                TreeMap::new, Collectors.counting()));
    }

    // ==================== 概览统计（Dashboard 摘要） ====================

    public long totalObservations(int userId) {
        return obsDao.findAllByUserId(userId).size();
    }

    public long distinctBodiesObserved(int userId) {
        return obsDao.findAllByUserId(userId).stream()
            .map(Observation::getBodyId)
            .distinct().count();
    }

    public long totalEquipmentUsed(int userId) {
        return obsDao.findAllByUserId(userId).stream()
            .flatMap(o -> obsDao.getLinkedEquipmentIds(o.getObsId()).stream())
            .distinct().count();
    }
}
```

**关键设计决策：**

- 所有统计方法先拉取用户全部观测记录（`obsDao.findAllByUserId`），在内存中用 Stream 聚合。
  数据量 < 10,000 条时性能可接受，避免每个统计维度写独立 SQL。
- 器材排行使用 EquipDao.getUsageCount（S4 已有），但注意 N+1：每次调用一次 SQL。如果器材数量 < 50 可接受。
- 返回类型统一使用 `Map<K, Long>`，ChartUtil 据此生成图表。

### 3.2 ChartUtil — JFreeChart 封装工厂

**文件：** `src/main/java/com/astrolog/util/ChartUtil.java`

```java
package com.astrolog.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public final class ChartUtil {

    private ChartUtil() {}

    // ==================== 柱状图 ====================

    /**
     * @param title    图表标题
     * @param data     Map<类别名(String), 数值(Long)>
     * @param xLabel   X 轴标签
     * @param yLabel   Y 轴标签
     * @return 嵌入 ChartPanel 的 JPanel
     */
    public static JPanel createBarChart(String title, Map<String, Long> data,
                                         String xLabel, String yLabel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Long> e : data.entrySet()) {
            dataset.addValue(e.getValue(), yLabel, e.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            title, xLabel, yLabel, dataset,
            PlotOrientation.VERTICAL, false, true, false);

        // 配色：蓝色渐变条
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(70, 130, 180));
        renderer.setItemMargin(0.02);  // 柱子间距

        return new ChartPanel(chart);
    }

    // ==================== 饼图 ====================

    /**
     * @param title  图表标题
     * @param data   Map<类别名(String), 数值(Long)>
     * @return 嵌入 ChartPanel 的 JPanel
     */
    public static JPanel createPieChart(String title, Map<String, Long> data) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Long> e : data.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
            title, dataset, true, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        // 各扇区颜色使用内置默认色板 + 自定义
        plot.setSectionPaint("恒星", new Color(255, 200, 50));
        plot.setSectionPaint("行星", new Color(100, 180, 255));
        plot.setSectionPaint("星云", new Color(255, 100, 150));
        plot.setSectionPaint("星团", new Color(150, 220, 100));
        plot.setSectionPaint("星系", new Color(180, 130, 255));
        plot.setLabelGenerator(null);  // 图例代替标签

        return new ChartPanel(chart);
    }

    // ==================== 折线图 ====================

    /**
     * @param title   图表标题
     * @param data    Map<X轴值(String), Y轴值(Long)>
     * @param xLabel  X 轴标签
     * @param yLabel  Y 轴标签
     */
    public static JPanel createLineChart(String title, Map<String, Long> data,
                                          String xLabel, String yLabel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Long> e : data.entrySet()) {
            dataset.addValue(e.getValue(), yLabel, e.getKey());
        }

        JFreeChart chart = ChartFactory.createLineChart(
            title, xLabel, yLabel, dataset,
            PlotOrientation.VERTICAL, false, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(220, 80, 60));
        renderer.setSeriesShapesVisible(0, true);

        return new ChartPanel(chart);
    }

    // ==================== 雷达图 ====================

    /**
     * 绘制雷达图（蜘蛛网图），自定义 Graphics2D 绘制。
     * JFreeChart 无原生雷达图支持，使用 SpiderWebPlot 或自绘。
     *
     * @param title  标题
     * @param data   Map<维度名, 数值>，数值范围建议 0-10 归一化
     */
    public static JPanel createRadarChart(String title, Map<String, Double> data) {
        // 方案 A：JFreeChart SpiderWebPlot（需 org.jfree.chart.plot.SpiderWebPlot）
        // 方案 B：自定义 JPanel paintComponent 绘制多边形网格

        // 推荐方案 B（避免额外依赖）：
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int cx = w / 2, cy = h / 2;
                int radius = Math.min(w, h) / 3;

                int n = data.size();
                double angleStep = 2 * Math.PI / n;
                String[] keys = data.keySet().toArray(new String[0]);
                double[] values = data.values().stream()
                    .mapToDouble(Double::doubleValue).toArray();

                // 绘制网格
                g2.setColor(Color.LIGHT_GRAY);
                for (int level = 1; level <= 5; level++) {
                    double r = radius * level / 5.0;
                    int[] xs = new int[n], ys = new int[n];
                    for (int i = 0; i < n; i++) {
                        double angle = -Math.PI / 2 + i * angleStep;
                        xs[i] = cx + (int)(r * Math.cos(angle));
                        ys[i] = cy - (int)(r * Math.sin(angle));
                    }
                    g2.drawPolygon(xs, ys, n);
                }

                // 绘制轴线
                g2.setColor(Color.GRAY);
                for (int i = 0; i < n; i++) {
                    double angle = -Math.PI / 2 + i * angleStep;
                    g2.drawLine(cx, cy,
                        cx + (int)(radius * Math.cos(angle)),
                        cy - (int)(radius * Math.sin(angle)));
                }

                // 绘制数据多边形
                int[] dxs = new int[n], dys = new int[n];
                for (int i = 0; i < n; i++) {
                    double angle = -Math.PI / 2 + i * angleStep;
                    double r = radius * values[i] / 10.0;  // 归一化到 0-10
                    dxs[i] = cx + (int)(r * Math.cos(angle));
                    dys[i] = cy - (int)(r * Math.sin(angle));
                }
                g2.setColor(new Color(220, 80, 60, 150));
                g2.fillPolygon(dxs, dys, n);
                g2.setColor(new Color(200, 50, 30));
                g2.setStroke(new BasicStroke(2f));
                g2.drawPolygon(dxs, dys, n);

                // 绘制标签
                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
                for (int i = 0; i < n; i++) {
                    double angle = -Math.PI / 2 + i * angleStep;
                    int lx = cx + (int)((radius + 25) * Math.cos(angle));
                    int ly = cy - (int)((radius + 25) * Math.sin(angle));
                    g2.drawString(keys[i], lx - 15, ly + 5);
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(400, 400);
            }
        };
    }
}
```

### 3.3 SkyCalendarHeatmap — 观测日历热力图

**文件：** `src/main/java/com/astrolog/ui/component/SkyCalendarHeatmap.java`

**功能：** 类似 GitHub 贡献图，按日显示观测密度。

**核心设计：**

```java
public class SkyCalendarHeatmap extends JPanel {

    private Map<LocalDate, Long> dailyCounts;  // 日期 → 观测次数
    private int year;

    public SkyCalendarHeatmap(Map<LocalDate, Long> dailyCounts) {
        this.dailyCounts = dailyCounts;
        this.year = LocalDate.now().getYear();
        setBackground(new Color(30, 30, 50));  // 深色背景
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int cellSize = 14, cellGap = 3;
        int leftMargin = 60, topMargin = 30;

        // 计算起始偏移（年份第一天是周几）
        LocalDate start = LocalDate.of(year, 1, 1);
        int startDayOfWeek = start.getDayOfWeek().getValue(); // Mon=1 ... Sun=7

        // 绘制 12 个月 × 7 天的网格
        for (int month = 1; month <= 12; month++) {
            LocalDate firstOfMonth = LocalDate.of(year, month, 1);
            int daysInMonth = firstOfMonth.lengthOfMonth();

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(year, month, day);
                int dayOfYear = date.getDayOfYear();

                int col = (dayOfYear + startDayOfWeek - 1) / 7;
                int row = (dayOfYear + startDayOfWeek - 1) % 7;

                int x = leftMargin + col * (cellSize + cellGap);
                int y = topMargin + row * (cellSize + cellGap);

                // 颜色深浅表示观测密度
                long count = dailyCounts.getOrDefault(date, 0L);
                Color color = count > 0
                    ? new Color(40, Math.min(255, 100 + (int)(count * 40)), 80)
                    : new Color(50, 50, 60);

                g2.setColor(color);
                g2.fillRoundRect(x, y, cellSize, cellSize, 3, 3);
            }
        }

        // 月份标签
        g2.setColor(Color.LIGHT_GRAY);
        g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 10));
        String[] months = {"1月","2月","3月","4月","5月","6月",
                          "7月","8月","9月","10月","11月","12月"};
        for (int i = 0; i < 12; i++) {
            LocalDate fd = LocalDate.of(year, i + 1, 1);
            int col = (fd.getDayOfYear() + startDayOfWeek - 1) / 7;
            g2.drawString(months[i],
                leftMargin + col * (cellSize + cellGap), topMargin - 10);
        }

        // 星期标签
        String[] weekdays = {"一","二","三","四","五","六","日"};
        for (int i = 0; i < 7; i++) {
            g2.drawString(weekdays[i], 5,
                topMargin + i * (cellSize + cellGap) + cellSize);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(900, 180);
    }
}
```

### 3.4 StarMapCanvas — 星座亮星分布图画布

**文件：** `src/main/java/com/astrolog/ui/component/StarMapCanvas.java`

**功能：** 以北极星为中心的极坐标系，绘制已观测星体的圆点和星座连线。

```java
public class StarMapCanvas extends JPanel {

    private List<CelestialBody> observedBodies;   // 用户已观测的星体
    private List<CelestialBody> allBodies;         // 所有星体（来自 BodyDao）

    // 硬编码 88 星座简略连线数据：每星座一组坐标点
    // 实际数据量较大（~500 条连线），可用 JSON 文件或硬编码主要星座

    public StarMapCanvas(List<CelestialBody> observedBodies,
                         List<CelestialBody> allBodies) {
        this.observedBodies = observedBodies;
        this.allBodies = allBodies;
        setBackground(new Color(10, 10, 40));  // 星空深色背景
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int cx = w / 2, cy = h / 2;
        double radius = Math.min(w, h) / 2 * 0.85;

        // 1. 绘制背景网格（赤经圈 + 赤纬圈）
        drawGrid(g2, cx, cy, radius);

        // 2. 绘制所有亮星（灰色小点）
        drawStars(g2, cx, cy, radius, allBodies, new Color(100, 100, 130), 3);

        // 3. 绘制已观测星体（亮色大点）
        drawStars(g2, cx, cy, radius, observedBodies, new Color(255, 200, 50), 8);

        // 4. 绘制简化星座连线（仅已观测星体所在星座）
        drawConstellationLines(g2, cx, cy, radius);
    }

    private void drawGrid(Graphics2D g2, int cx, int cy, double radius) {
        g2.setColor(new Color(60, 60, 80));
        // 赤纬圈：0°, 30°, 60°, 90° (北极星)
        double[] decDegrees = {0, 30, 60, 90};
        for (double decDeg : decDegrees) {
            double r = radius * (90 - Math.abs(decDeg)) / 90;
            g2.drawOval((int)(cx - r), (int)(cy - r), (int)(2 * r), (int)(2 * r));
        }
        // 赤经线：0h, 6h, 12h, 18h
        for (int h = 0; h < 24; h += 6) {
            double angle = Math.toRadians(h * 15 - 90);  // 0h 在右侧
            g2.drawLine(cx, cy,
                cx + (int)(radius * Math.cos(angle)),
                cy + (int)(radius * Math.sin(angle)));
        }
    }

    private void drawStars(Graphics2D g2, int cx, int cy, double radius,
                           List<CelestialBody> bodies, Color color, int size) {
        g2.setColor(color);
        for (CelestialBody body : bodies) {
            // RA → x, Dec → y（简易等距投影）
            double raDeg = (body.getRaH() + body.getRaM() / 60.0) * 15;
            double decDeg = body.getDecDeg() + body.getDecMin() / 60.0;
            double angle = Math.toRadians(raDeg - 90);  // 0h = 右侧
            double r = radius * (90 - Math.abs(decDeg)) / 90;
            int x = cx + (int)(r * Math.cos(angle));
            int y = cy - (int)(r * Math.sin(angle));
            g2.fillOval(x - size/2, y - size/2, size, size);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }
}
```

**星座连线数据：** 简化处理——硬编码 10-15 个主要星座的亮星连线（如大熊座北斗七星、猎户座四边+三星、仙后座 W 形等）。约 50 条线段，编码在 StarMapCanvas 中或读取 JSON。

### 3.5 StatsPanel — 统计图表面板

**文件：** `src/main/java/com/astrolog/ui/panel/StatsPanel.java`

**布局结构（~220 行）：**

```
┌──────────────────────────────────────────────┐
│ JTabbedPane (TOP)                            │
│                                              │
│ ┌─ 年度统计 ───────────────────────────────┐ │
│ │ [柱状图]  年度观测频次                    │ │
│ └──────────────────────────────────────────┘ │
│ ┌─ 月度统计 ───────────────────────────────┐ │
│ │ [柱状图]  月度观测频次                    │ │
│ └──────────────────────────────────────────┘ │
│ ┌─ 类型分布 ───────────────────────────────┐ │
│ │ [饼图]    星体类型分布                    │ │
│ └──────────────────────────────────────────┘ │
│ ┌─ 年度趋势 ───────────────────────────────┐ │
│ │ [折线图]  年度变化趋势                    │ │
│ └──────────────────────────────────────────┘ │
│ ┌─ 观测条件 ───────────────────────────────┐ │
│ │ [雷达图]  视宁度/天气/月相/器材综合       │ │
│ └──────────────────────────────────────────┘ │
│ ┌─ 日历热力 ───────────────────────────────┐ │
│ │ [SkyCalendarHeatmap]  每日观测密度        │ │
│ └──────────────────────────────────────────┘ │
│ ┌─ 星座分布 ───────────────────────────────┐ │
│ │ [StarMapCanvas]  已观测星体分布           │ │
│ └──────────────────────────────────────────┘ │
│                                              │
│ 底部: 器材使用排行 JTable (名称/次数)        │
└──────────────────────────────────────────────┘
```

**实现要点：**

1. 构造器签名：`public StatsPanel(User currentUser)`
2. 构造器中初始化 StatsService，调用各统计方法获取数据
3. 7 个选项卡逐一创建 ChartPanel 并 `addTab()`
4. 器材排行使用 JTable 放在面板底部
5. 数据为空时显示 "暂无足够观测数据" 提示而非空图表
6. ChartUtil 生成的 ChartPanel 支持鼠标缩放/平移（JFreeChart 内置）
7. 所有 Swing 组件在 EDT 中创建

### 3.6 MainFrame 修改

```java
// "统计图表"占位（NAV_LABELS[6]）→ StatsPanel
if (NAV_LABELS[i].equals("统计图表")) {
    StatsPanel statsPanel = new StatsPanel(currentUser);
    contentPanel.add(statsPanel, "统计图表");
}
```

导入 `com.astrolog.ui.panel.StatsPanel`。

---

## 4. 测试规格

### 4.1 StatsServiceTest（10 例）

**文件：** `src/test/java/com/astrolog/unit/service/StatsServiceTest.java`

```
@Mock ObsDao, @Mock BodyDao, @Mock EquipDao; @InjectMocks StatsService

ST-001: totalObservations → 返回正确总数
ST-002: distinctBodiesObserved → 去重星体数正确
ST-003: totalEquipmentUsed → 去重器材数正确
ST-004: countByYear → 按年份分组正确
ST-005: countByMonth → 按月份分组正确
ST-006: countByBodyType → 按类型分组正确
ST-007: countBySeeing → 按视宁度分组正确
ST-008: countByWeather → 按天气分组正确
ST-009: countByMoonPhase → 按月相分组正确
ST-010: dailyCounts → 按日期分组正确
```

### 4.2 ChartUtil 验证（不需要独立单元测试）

ChartUtil 的验证通过 G2 手动验收完成（图表可显示、颜色正确、数据映射正确），不强制编写单元测试（JFreeChart 的 ChartPanel 难以在无头环境中验证）。

---

## 5. 任务执行顺序

```
第 1 步 → StatsService.java（依赖 ObsDao + BodyDao + EquipDao）
    注意：保留 package-private 测试构造器
第 2 步 → ChartUtil.java（依赖 JFreeChart）
第 3 步 → SkyCalendarHeatmap.java（自定义 JPanel）
第 4 步 → StarMapCanvas.java（自定义 JPanel + 硬编码星座数据）
第 5 步 → StatsPanel.java（依赖以上全部 + StatsService）
第 6 步 → 修改 MainFrame.java（"统计图表" → StatsPanel）
第 7 步 → StatsServiceTest.java
```

---

## 6. 验收标准

| #   | 验收项                                   | 验证方法              |
| --- | ---------------------------------------- | --------------------- |
| 1   | `mvn clean compile` 零错误               | Maven 编译            |
| 2   | `mvn test` 全部通过（103 + 10 = 113 例） | 执行测试              |
| 3   | 年度观测频次柱状图正确显示               | Statistics → 年度统计 |
| 4   | 月度观测频次柱状图正确显示               | Statistics → 月度统计 |
| 5   | 星体类型分布饼图正确显示                 | Statistics → 类型分布 |
| 6   | 年度趋势折线图正确显示                   | Statistics → 年度趋势 |
| 7   | 观测条件雷达图正确显示                   | Statistics → 观测条件 |
| 8   | 日历热力图正确显示（色深表示密度）       | Statistics → 日历热力 |
| 9   | 星座分布图正确显示（已观测vs未观测）     | Statistics → 星座分布 |
| 10  | 器材使用排行表格正确排序                 | 查看排行表            |
| 11  | 无观测数据时显示友好提示                 | 新建用户查看统计      |

---

## 7. 阶段二关口审查

S6 完成后即进入阶段二关口审查（G1-G4），涵盖 S3-S6 全部内容：

| 关口 | 内容     | 标准                                 |
| ---- | -------- | ------------------------------------ |
| G1   | 代码审查 | S3-S6 所有新增/修改代码              |
| G2   | 功能验收 | 星体/器材/观测/可视化 4 个模块全功能 |
| G3   | 测试覆盖 | 全部 113 测试通过，覆盖率 > 70%      |
| G4   | 文档同步 | 完成报告 + 偏差日志                  |

---

## 8. 注意事项

1. **JFreeChart ChartPanel 默认启用缩放/平移：** 鼠标拖拽可平移，滚轮可缩放。这是内置功能，无需额外配置。但如果图表嵌入 JTabbedPane，可能需要 `setMouseWheelEnabled(true)`。
2. **数据为空处理：** 如果用户没有任何观测记录，所有统计方法返回空 Map。StatsPanel 需检查并显示友好提示，而非让 ChartUtil 生成空图表。
3. **雷达图自绘：** JFreeChart 不原生支持雷达图（SpiderWebPlot 在 1.5.4 中不完善）。S6 使用自定义 JPanel paintComponent 绘制——这是交接文档明确指定的方案。
4. **星座连线数据量：** 硬编码 10-15 个主要星座连线（而非全部 88 星座），约 200 行硬编码坐标数据。StarMapCanvas 类中定义静态数组。
5. **Stream 聚合性能：** 所有统计方法在内存中 Stream 聚合。用户观测记录 < 5,000 条时完全可接受。如果性能有问题，可改为 SQL 的 GROUP BY 查询（S6 不优先）。
6. **StatsService 构造器注入：** 公开构造器创建真实 DAO，package-private 构造器用于 Mockito 测试注入。这是 S2 以来的标准模式。
7. **ChartPanel 大小：** 通过 `setPreferredSize()` 设置合理的默认尺寸，嵌入 JTabbedPane 时会自动适配。
