package com.astrolog.ui.component;

import com.astrolog.model.CelestialBody;
import com.astrolog.model.ConstellationInfo;
import com.astrolog.model.ConstellationInfo.ConstellationLine;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class StarMapCanvas extends JPanel {

    private final List<CelestialBody> observedBodies;
    private final List<CelestialBody> allBodies;
    private final List<ConstellationInfo> constellations;
    private String highlightedConstellation;
    private double rotationOffset;
    private int lastMouseX;

    public StarMapCanvas(List<CelestialBody> observedBodies,
                         List<CelestialBody> allBodies,
                         List<ConstellationInfo> constellations) {
        this.observedBodies = observedBodies;
        this.allBodies = allBodies;
        this.constellations = constellations;
        this.rotationOffset = 0;
        setBackground(new Color(10, 10, 40));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastMouseX;
                rotationOffset += dx * 0.3;
                lastMouseX = e.getX();
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
    }

    public void setHighlightedConstellation(String name) {
        this.highlightedConstellation = name;
        repaint();
    }

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
        double radius = Math.min(w, h) / 2.0 * 0.85;

        drawGrid(g2, cx, cy, radius);
        drawAllBodies(g2, cx, cy, radius);
        drawConstellationLines(g2, cx, cy, radius);
        drawConstellationLabels(g2, cx, cy, radius);
    }

    private double toX(double raDeg, double decDeg, double radius, int cx) {
        double angle = Math.toRadians(raDeg - 90 - rotationOffset);
        double r = radius * (90 - Math.abs(decDeg)) / 90;
        return cx + r * Math.cos(angle);
    }

    private double toY(double raDeg, double decDeg, double radius, int cy) {
        double angle = Math.toRadians(raDeg - 90 - rotationOffset);
        double r = radius * (90 - Math.abs(decDeg)) / 90;
        return cy - r * Math.sin(angle);
    }

    private void drawGrid(Graphics2D g2, int cx, int cy, double radius) {
        g2.setColor(new Color(60, 60, 80));
        g2.setStroke(new BasicStroke(1f));
        double[] decDegrees = {0, 30, 60, 90};
        for (double decDeg : decDegrees) {
            double r = radius * (90 - Math.abs(decDeg)) / 90;
            int d = (int) (2 * r);
            g2.drawOval((int) (cx - r), (int) (cy - r), d, d);
        }
        for (int h = 0; h < 24; h += 6) {
            double angle = Math.toRadians(h * 15 - 90 - rotationOffset);
            g2.drawLine(cx, cy,
                cx + (int) (radius * Math.cos(angle)),
                cy + (int) (radius * Math.sin(angle)));
        }
    }

    private void drawAllBodies(Graphics2D g2, int cx, int cy, double radius) {
        if (allBodies == null) return;

        // Grey dots for all bodies
        g2.setColor(new Color(80, 80, 110, 120));
        for (CelestialBody body : allBodies) {
            double raDeg = (body.getRaH() + body.getRaM() / 60.0) * 15;
            double decDeg = body.getDecDeg()
                + (body.getDecDeg() < 0 ? -body.getDecMin() : body.getDecMin()) / 60.0;
            int x = (int) toX(raDeg, decDeg, radius, cx);
            int y = (int) toY(raDeg, decDeg, radius, cy);

            double mag = body.getMagnitude() != null ? body.getMagnitude().doubleValue() : 6.0;
            int size = (int) Math.max(1.5, (6.5 - mag) * 0.7);
            g2.fillOval(x - size / 2, y - size / 2, size, size);

            // Bright star labels (mag < 3.0)
            if (body.getMagnitude() != null && body.getMagnitude().doubleValue() < 3.0
                    && body.getName() != null) {
                g2.setColor(new Color(180, 180, 210, 160));
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 8));
                g2.drawString(body.getName(), x + 4, y - 4);
                g2.setColor(new Color(80, 80, 110, 120));
            }
        }

        // Gold larger dots for observed bodies
        if (observedBodies != null) {
            g2.setColor(new Color(255, 200, 50, 200));
            for (CelestialBody body : observedBodies) {
                double raDeg = (body.getRaH() + body.getRaM() / 60.0) * 15;
                double decDeg = body.getDecDeg()
                + (body.getDecDeg() < 0 ? -body.getDecMin() : body.getDecMin()) / 60.0;
                int x = (int) toX(raDeg, decDeg, radius, cx);
                int y = (int) toY(raDeg, decDeg, radius, cy);
                g2.fillOval(x - 3, y - 3, 7, 7);
            }
        }
    }

    private void drawConstellationLines(Graphics2D g2, int cx, int cy, double radius) {
        if (constellations == null) return;

        // Draw normal lines first
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(80, 80, 120, 100));

        for (ConstellationInfo ci : constellations) {
            if (ci.getName().equals(highlightedConstellation)) continue;
            List<ConstellationLine> lines = ci.getLines();
            if (lines == null) continue;
            drawLines(g2, lines, cx, cy, radius);
        }

        // Draw highlighted constellation with bright gold
        if (highlightedConstellation != null) {
            for (ConstellationInfo ci : constellations) {
                if (ci.getName().equals(highlightedConstellation)) {
                    List<ConstellationLine> lines = ci.getLines();
                    if (lines == null) continue;
                    g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.setColor(new Color(255, 200, 50, 220));
                    drawLines(g2, lines, cx, cy, radius);
                    break;
                }
            }
        }
    }

    private void drawLines(Graphics2D g2, List<ConstellationLine> lines,
                           int cx, int cy, double radius) {
        for (ConstellationLine line : lines) {
            double raDeg1 = line.getFromRA() * 15;
            double decDeg1 = line.getFromDec();
            double raDeg2 = line.getToRA() * 15;
            double decDeg2 = line.getToDec();

            int x1 = (int) toX(raDeg1, decDeg1, radius, cx);
            int y1 = (int) toY(raDeg1, decDeg1, radius, cy);
            int x2 = (int) toX(raDeg2, decDeg2, radius, cx);
            int y2 = (int) toY(raDeg2, decDeg2, radius, cy);

            g2.drawLine(x1, y1, x2, y2);
        }
    }

    private void drawConstellationLabels(Graphics2D g2, int cx, int cy, double radius) {
        if (constellations == null) return;

        for (ConstellationInfo ci : constellations) {
            List<ConstellationLine> lines = ci.getLines();
            if (lines == null || lines.isEmpty()) continue;

            ConstellationLine first = lines.get(0);
            double raDeg = (first.getFromRA() + first.getToRA()) / 2.0 * 15;
            double decDeg = (first.getFromDec() + first.getToDec()) / 2.0;

            int x = (int) toX(raDeg, decDeg, radius, cx);
            int y = (int) toY(raDeg, decDeg, radius, cy);

            boolean isHighlighted = ci.getName().equals(highlightedConstellation);
            g2.setColor(isHighlighted
                ? new Color(255, 220, 80, 240)
                : new Color(180, 180, 200, 140));
            g2.setFont(new Font("Microsoft YaHei",
                isHighlighted ? Font.BOLD : Font.PLAIN, isHighlighted ? 12 : 9));

            String name = ci.getName();
            if (name.length() > 4 && name.contains("(")) {
                name = name.substring(0, name.indexOf("("));
            }
            g2.drawString(name, x - name.length() * 4, y - 6);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 600);
    }
}
