package com.astrolog.ui.component;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.Year;
import java.util.Map;

public class SkyCalendarHeatmap extends JPanel {

    private final Map<LocalDate, Long> dailyCounts;
    private int year;

    public SkyCalendarHeatmap(Map<LocalDate, Long> dailyCounts) {
        this.dailyCounts = dailyCounts;
        this.year = LocalDate.now().getYear();
        setBackground(new Color(30, 30, 50));
    }

    public void setYear(int year) {
        this.year = year;
        repaint();
    }

    public int getYear() {
        return year;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int cellGap = 2;
        int leftMargin = 50;
        int topMargin = 28;

        LocalDate start = LocalDate.of(year, 1, 1);
        int startDayOfWeek = start.getDayOfWeek().getValue();
        int lastDayOfYear = Year.isLeap(year) ? 366 : 365;
        int maxCol = (lastDayOfYear + startDayOfWeek - 1) / 7 + 1;

        int availWidth = getWidth() - leftMargin - 15;
        int cellStep = Math.max(8, availWidth / maxCol);
        int cellSize = cellStep - cellGap;

        for (int month = 1; month <= 12; month++) {
            LocalDate firstOfMonth = LocalDate.of(year, month, 1);
            int daysInMonth = firstOfMonth.lengthOfMonth();

            for (int day = 1; day <= daysInMonth; day++) {
                LocalDate date = LocalDate.of(year, month, day);
                int dayOfYear = date.getDayOfYear();

                int col = (dayOfYear + startDayOfWeek - 1) / 7;
                int row = (dayOfYear + startDayOfWeek - 1) % 7;

                int x = leftMargin + col * cellStep;
                int y = topMargin + row * cellStep;

                long count = dailyCounts.getOrDefault(date, 0L);
                Color color;
                if (count == 0) {
                    color = new Color(50, 50, 60);
                } else {
                    int green = Math.min(255, 100 + (int) (count * 40));
                    color = new Color(40, green, 80);
                }

                g2.setColor(color);
                g2.fillRoundRect(x, y, cellSize, cellSize, 3, 3);
            }
        }

        g2.setColor(Color.LIGHT_GRAY);
        int fontSize = Math.max(9, cellSize * 7 / 10);
        g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, fontSize));
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月",
                          "7月", "8月", "9月", "10月", "11月", "12月"};
        for (int i = 0; i < 12; i++) {
            LocalDate fd = LocalDate.of(year, i + 1, 1);
            int col = (fd.getDayOfYear() + startDayOfWeek - 1) / 7;
            g2.drawString(months[i],
                leftMargin + col * cellStep, topMargin - 4);
        }

        String[] weekdays = {"一", "二", "三", "四", "五", "六", "日"};
        for (int i = 0; i < 7; i++) {
            g2.drawString(weekdays[i], 3,
                topMargin + i * cellStep + cellSize);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 150);
    }
}
