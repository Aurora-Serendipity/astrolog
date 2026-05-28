package com.astrolog.ui.panel;

import com.astrolog.config.ThemeConfig;
import com.astrolog.model.CelestialBody;
import com.astrolog.model.MessierObject;
import com.astrolog.model.User;
import com.astrolog.service.BodyService;
import com.astrolog.service.MessierService;
import com.astrolog.ui.component.ThemeManager;
import com.astrolog.ui.frame.MainFrame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessierPanel extends JPanel {

    private static final String[] COLUMNS = {"M#", "名称", "类型", "星座", "视星等", "最佳季节", "状态"};
    private static final String[] TYPE_FILTERS = {"全部", "星云", "星团", "星系"};
    private static final String[] SEASON_FILTERS = {"全部", "春", "夏", "秋", "冬"};
    private static final String[] STATUS_FILTERS = {"全部", "已观测", "未观测"};

    private final User currentUser;
    private final MainFrame mainFrame;
    private final MessierService messierService;
    private final Set<Integer> observedNumbers;
    private final List<MessierObject> allObjects;

    private DefaultTableModel tableModel;
    private JTable table;
    private JProgressBar progressBar;
    private JLabel progressLabel;
    private JLabel typeStatsLabel;
    private JButton certBtn;
    private JComboBox<String> typeCombo;
    private JComboBox<String> seasonCombo;
    private JComboBox<String> statusCombo;

    public MessierPanel(User currentUser, MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.currentUser = currentUser;
        this.messierService = new MessierService();
        this.allObjects = messierService.getFullCatalog();
        this.observedNumbers = messierService.getObservedNumbers(currentUser.getUserId());

        ThemeConfig.Theme theme = ThemeManager.getInstance().getCurrentTheme();

        setLayout(new BorderLayout(12, 12));
        setBackground(theme.bg());
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildTopPanel(theme), BorderLayout.NORTH);
        add(buildTablePanel(theme), BorderLayout.CENTER);
    }

    private JPanel buildTopPanel(ThemeConfig.Theme theme) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setOpaque(false);

        JPanel progressPanel = new JPanel(new BorderLayout(8, 4));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("观测进度"),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JPanel barRow = new JPanel(new BorderLayout(8, 0));
        barRow.setOpaque(false);
        progressBar = new JProgressBar(0, allObjects.size());
        progressBar.setStringPainted(true);
        barRow.add(progressBar, BorderLayout.CENTER);

        progressLabel = new JLabel();
        progressLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        barRow.add(progressLabel, BorderLayout.EAST);

        typeStatsLabel = new JLabel();
        typeStatsLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        typeStatsLabel.setForeground(theme.fg().darker());

        certBtn = new JButton("生成证书");
        certBtn.setEnabled(false);
        certBtn.addActionListener(e -> {
            try {
                com.astrolog.service.ExportService es = new com.astrolog.service.ExportService();
                String path = es.generateMessierCert(currentUser);
                JOptionPane.showMessageDialog(this,
                    "梅西耶证书已生成:\n" + path,
                    "证书生成成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IllegalStateException ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage() + "\n\n请继续观测，完成全部 110 个后可生成证书。",
                    "尚未满足条件", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "证书生成失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        progressPanel.add(barRow, BorderLayout.NORTH);
        progressPanel.add(typeStatsLabel, BorderLayout.CENTER);
        progressPanel.add(certBtn, BorderLayout.EAST);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        filterPanel.setOpaque(false);
        filterPanel.add(themedLabel("类型:", theme));
        typeCombo = new JComboBox<>(TYPE_FILTERS);
        filterPanel.add(typeCombo);
        filterPanel.add(themedLabel("季节:", theme));
        seasonCombo = new JComboBox<>(SEASON_FILTERS);
        filterPanel.add(seasonCombo);
        filterPanel.add(themedLabel("状态:", theme));
        statusCombo = new JComboBox<>(STATUS_FILTERS);
        filterPanel.add(statusCombo);

        JButton resetBtn = new JButton("清除筛选");
        resetBtn.addActionListener(e -> {
            typeCombo.setSelectedIndex(0);
            seasonCombo.setSelectedIndex(0);
            statusCombo.setSelectedIndex(0);
        });
        filterPanel.add(resetBtn);

        typeCombo.addActionListener(e -> refreshTable());
        seasonCombo.addActionListener(e -> refreshTable());
        statusCombo.addActionListener(e -> refreshTable());

        panel.add(progressPanel, BorderLayout.NORTH);
        panel.add(filterPanel, BorderLayout.SOUTH);

        refreshProgress();
        return panel;
    }

    private JPanel buildTablePanel(ThemeConfig.Theme theme) {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
            @Override
            public Class<?> getColumnClass(int col) {
                return col == 0 ? Integer.class : String.class;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);

        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(55);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(60);
        table.getColumnModel().getColumn(5).setPreferredWidth(65);
        table.getColumnModel().getColumn(6).setPreferredWidth(55);

        table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        for (int i = 0; i < 6; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(new RowColorRenderer());
        }

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                    int mNumber = (int) tableModel.getValueAt(modelRow, 0);
                    boolean observed = observedNumbers.contains(mNumber);
                    if (observed) {
                        jumpToBodyPanel(mNumber);
                    } else {
                        MessierObject obj = allObjects.stream()
                            .filter(m -> m.getMessierNumber() == mNumber)
                            .findFirst().orElse(null);
                        if (obj != null) showDetailDialog(obj);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("梅西耶天体目录"));
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(scrollPane, BorderLayout.CENTER);

        refreshTable();
        return panel;
    }

    private void refreshTable() {
        String typeFilter = (String) typeCombo.getSelectedItem();
        String seasonFilter = (String) seasonCombo.getSelectedItem();
        String statusFilter = (String) statusCombo.getSelectedItem();

        List<MessierObject> filtered = new ArrayList<>(allObjects);
        if (typeFilter != null && !"全部".equals(typeFilter)) {
            filtered.removeIf(m -> !m.getType().equals(typeFilter));
        }
        if (seasonFilter != null && !"全部".equals(seasonFilter)) {
            filtered.removeIf(m -> !m.getSeason().equals(seasonFilter));
        }
        if (statusFilter != null) {
            if ("已观测".equals(statusFilter)) {
                filtered.removeIf(m -> !observedNumbers.contains(m.getMessierNumber()));
            } else if ("未观测".equals(statusFilter)) {
                filtered.removeIf(m -> observedNumbers.contains(m.getMessierNumber()));
            }
        }

        filtered.sort((a, b) -> Integer.compare(a.getMessierNumber(), b.getMessierNumber()));

        tableModel.setRowCount(0);
        for (MessierObject m : filtered) {
            boolean observed = observedNumbers.contains(m.getMessierNumber());
            tableModel.addRow(new Object[]{
                m.getMessierNumber(),
                m.getName(),
                m.getType(),
                m.getConstellation(),
                m.getMagnitude(),
                m.getSeason(),
                observed ? "✓" : "○"
            });
        }
    }

    private void refreshProgress() {
        int count = observedNumbers.size();
        int total = allObjects.size();
        double pct = messierService.getProgress(observedNumbers);

        progressBar.setValue(count);
        progressBar.setString(String.format("%d/%d (%.1f%%)", count, total, pct));
        progressLabel.setText(String.format("%d / %d", count, total));

        Map<String, int[]> stats = messierService.getStatsByType(observedNumbers);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, int[]> e : stats.entrySet()) {
            sb.append(e.getKey()).append(": ")
              .append(e.getValue()[1]).append("/").append(e.getValue()[0])
              .append("  ");
        }
        typeStatsLabel.setText(sb.toString().trim());

        certBtn.setEnabled(messierService.isCertEligible(observedNumbers));
    }

    private void showDetailDialog(MessierObject obj) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
            obj.getName(), true);
        dialog.setLayout(new BorderLayout(12, 12));
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.weightx = 1;

        addInfoRow(infoPanel, gbc, 0, "梅西耶编号:", "M" + obj.getMessierNumber());
        addInfoRow(infoPanel, gbc, 1, "名称:", obj.getName());
        addInfoRow(infoPanel, gbc, 2, "类型:", obj.getType());
        addInfoRow(infoPanel, gbc, 3, "所在星座:", obj.getConstellation());
        addInfoRow(infoPanel, gbc, 4, "视星等:", String.valueOf(obj.getMagnitude()));
        addInfoRow(infoPanel, gbc, 5, "最佳季节:", obj.getSeason());

        boolean observed = observedNumbers.contains(obj.getMessierNumber());
        addInfoRow(infoPanel, gbc, 6, "观测状态:", observed ? "✓ 已观测" : "○ 未观测");

        JTextArea descArea = new JTextArea(obj.getDescription());
        descArea.setWrapStyleWord(true);
        descArea.setLineWrap(true);
        descArea.setEditable(false);
        descArea.setBackground(infoPanel.getBackground());
        descArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(350, 60));
        descScroll.setBorder(BorderFactory.createTitledBorder("描述"));

        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);

        dialog.add(infoPanel, BorderLayout.NORTH);
        dialog.add(descScroll, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addInfoRow(JPanel panel, GridBagConstraints gbc, int row,
                            String label, String value) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.gridy = row;
        gbc.weightx = 0.7;
        JLabel val = new JLabel(value);
        val.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        panel.add(val, gbc);
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(
                t, value, isSelected, hasFocus, row, col);
            if (value != null) {
                if ("✓".equals(value.toString())) {
                    setForeground(new Color(34, 139, 34));
                    setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
                } else {
                    setForeground(Color.GRAY);
                    setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
                }
            }
            setHorizontalAlignment(CENTER);
            return c;
        }
    }

    private class RowColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(
                t, value, isSelected, hasFocus, row, col);
            if (!isSelected) {
                int modelRow = t.convertRowIndexToModel(row);
                Object statusVal = t.getModel().getValueAt(modelRow, 6);
                if ("✓".equals(String.valueOf(statusVal))) {
                    c.setBackground(new Color(240, 255, 240));
                } else {
                    c.setBackground(Color.WHITE);
                }
            }
            return c;
        }
    }

    private JLabel themedLabel(String text, ThemeConfig.Theme theme) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(theme.fg());
        return lbl;
    }

    private void jumpToBodyPanel(int messierNumber) {
        BodyService bodyService = new BodyService();
        List<CelestialBody> bodies = bodyService.listAll();
        CelestialBody target = bodies.stream()
            .filter(b -> b.getMessierNumber() != null && b.getMessierNumber() == messierNumber)
            .findFirst().orElse(null);

        if (target == null) {
            JOptionPane.showMessageDialog(this,
                "未找到 M" + messierNumber + " 对应的星体记录",
                "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        mainFrame.switchTo("星体库");
        CelestialBodyPanel bodyPanel = mainFrame.getCelestialBodyPanel();
        if (bodyPanel != null) {
            bodyPanel.highlightBody(target.getBodyId());
        }
    }
}
