package com.astrolog.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.text.DecimalFormat;
import java.util.Map;

public final class ChartUtil {

    private static final Font CHART_FONT = new Font("Microsoft YaHei", Font.PLAIN, 12);
    private static final Font TITLE_FONT = new Font("Microsoft YaHei", Font.BOLD, 15);
    private static final int BAR_WIDTH = 50;

    private ChartUtil() {}

    private static ChartPanel wrapChart(JFreeChart chart) {
        ChartPanel panel = new ChartPanel(chart);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    private static void applyChineseFont(JFreeChart chart) {
        if (chart.getTitle() != null) {
            chart.setTitle(new TextTitle(chart.getTitle().getText(), TITLE_FONT));
        }
        if (chart.getLegend() != null) {
            chart.getLegend().setItemFont(CHART_FONT);
        }
    }

    private static void applyAxisFont(CategoryPlot plot) {
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(CHART_FONT);
        domainAxis.setTickLabelFont(CHART_FONT);
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setLabelFont(CHART_FONT);
        rangeAxis.setTickLabelFont(CHART_FONT);
    }

    private static JScrollPane wrapWithScroll(ChartPanel chartPanel, int categoryCount) {
        int chartWidth = Math.max(600, categoryCount * BAR_WIDTH + 120);
        chartPanel.setPreferredSize(new Dimension(chartWidth, 420));
        JScrollPane scrollPane = new JScrollPane(chartPanel,
            JScrollPane.VERTICAL_SCROLLBAR_NEVER,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    // ==================== 柱状图 ====================

    public static Component createBarChart(String title, Map<String, Long> data,
                                            String xLabel, String yLabel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<String, Long> e : data.entrySet()) {
            dataset.addValue(e.getValue(), yLabel, e.getKey());
        }

        JFreeChart chart = ChartFactory.createBarChart(
            title, xLabel, yLabel, dataset,
            PlotOrientation.VERTICAL, false, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(70, 130, 180));
        renderer.setItemMargin(0.02);

        applyChineseFont(chart);
        applyAxisFont(plot);

        ChartPanel panel = wrapChart(chart);
        return wrapWithScroll(panel, data.size());
    }

    // ==================== 饼图 ====================

    public static Component createPieChart(String title, Map<String, Long> data) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        for (Map.Entry<String, Long> e : data.entrySet()) {
            dataset.setValue(e.getKey(), e.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
            title, dataset, true, true, false);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("恒星", new Color(255, 200, 50));
        plot.setSectionPaint("行星", new Color(100, 180, 255));
        plot.setSectionPaint("星云", new Color(255, 100, 150));
        plot.setSectionPaint("星团", new Color(150, 220, 100));
        plot.setSectionPaint("星系", new Color(180, 130, 255));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
            "{0}: {1}次 ({2})",
            new DecimalFormat("0"), new DecimalFormat("0.0%")));
        plot.setLabelFont(CHART_FONT);
        plot.setLabelBackgroundPaint(new Color(255, 255, 255, 200));
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setSimpleLabels(true);

        applyChineseFont(chart);

        return wrapChart(chart);
    }

    // ==================== 折线图 ====================

    public static Component createLineChart(String title, Map<String, Long> data,
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

        applyChineseFont(chart);
        applyAxisFont(plot);

        ChartPanel panel = wrapChart(chart);
        return wrapWithScroll(panel, data.size());
    }

    // ==================== 雷达图 ====================

    public static Component createRadarChart(String title, Map<String, Double> data) {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                    RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int cx = w / 2;
                int cy = h / 2;
                int radius = Math.min(w, h) / 3;

                int n = data.size();
                if (n == 0) {
                    return;
                }
                double angleStep = 2 * Math.PI / n;
                String[] keys = data.keySet().toArray(new String[0]);
                double[] values = data.values().stream()
                    .mapToDouble(Double::doubleValue).toArray();

                g2.setColor(Color.LIGHT_GRAY);
                for (int level = 1; level <= 5; level++) {
                    double r = radius * level / 5.0;
                    int[] xs = new int[n];
                    int[] ys = new int[n];
                    for (int i = 0; i < n; i++) {
                        double angle = -Math.PI / 2 + i * angleStep;
                        xs[i] = cx + (int) (r * Math.cos(angle));
                        ys[i] = cy - (int) (r * Math.sin(angle));
                    }
                    g2.drawPolygon(xs, ys, n);
                }

                g2.setColor(Color.GRAY);
                for (int i = 0; i < n; i++) {
                    double angle = -Math.PI / 2 + i * angleStep;
                    g2.drawLine(cx, cy,
                        cx + (int) (radius * Math.cos(angle)),
                        cy - (int) (radius * Math.sin(angle)));
                }

                int[] dxs = new int[n];
                int[] dys = new int[n];
                for (int i = 0; i < n; i++) {
                    double angle = -Math.PI / 2 + i * angleStep;
                    double r = radius * values[i] / 10.0;
                    dxs[i] = cx + (int) (r * Math.cos(angle));
                    dys[i] = cy - (int) (r * Math.sin(angle));
                }
                g2.setColor(new Color(220, 80, 60, 150));
                g2.fillPolygon(dxs, dys, n);
                g2.setColor(new Color(200, 50, 30));
                g2.setStroke(new BasicStroke(2f));
                g2.drawPolygon(dxs, dys, n);

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
                for (int i = 0; i < n; i++) {
                    double angle = -Math.PI / 2 + i * angleStep;
                    int lx = cx + (int) ((radius + 25) * Math.cos(angle));
                    int ly = cy - (int) ((radius + 25) * Math.sin(angle));
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
