package com.astrolog.ui.panel;

import com.astrolog.model.CelestialBody;
import com.astrolog.model.User;
import com.astrolog.model.enums.BodyType;
import com.astrolog.model.enums.UserRole;
import com.astrolog.service.BodyService;
import com.astrolog.service.ServiceResult;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CelestialBodyPanel extends JPanel {

    private static final String[] COLUMNS = {
        "名称", "类型", "星座", "赤经", "赤纬", "视星等", "最佳季节",
        "梅西耶#", "NGC#", "收藏"
    };

    private static final String[] TYPE_OPTIONS = {"全部", "恒星", "行星", "星云", "星团", "星系"};
    private static final String[] SEASON_OPTIONS = {"全部", "春", "夏", "秋", "冬"};

    private final User currentUser;
    private final BodyService bodyService;
    private final Set<Integer> favoriteIds = new HashSet<>();

    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> constellationCombo;
    private JComboBox<String> typeCombo;
    private JTextField minMagField;
    private JTextField maxMagField;
    private JComboBox<String> seasonCombo;
    private JTextField keywordField;
    private JButton addBtn;
    private JButton editBtn;
    private JButton deleteBtn;
    private JButton importBtn;
    private JLabel modeLabel;

    private String currentMode = "all";

    public CelestialBodyPanel(User currentUser) {
        this.currentUser = currentUser;
        this.bodyService = new BodyService();
        initComponents();
        refreshTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        add(buildSearchPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);
        add(buildActionPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildSearchPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        panel.setBackground(new Color(245, 245, 250));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridy = 0;

        // 第一行：过滤条件
        c.gridx = 0;
        panel.add(new JLabel("星座:"), c);
        c.gridx = 1;
        constellationCombo = new JComboBox<>();
        constellationCombo.addItem("全部");
        panel.add(constellationCombo, c);

        c.gridx = 2;
        panel.add(new JLabel("类型:"), c);
        c.gridx = 3;
        typeCombo = new JComboBox<>(TYPE_OPTIONS);
        panel.add(typeCombo, c);

        c.gridx = 4;
        panel.add(new JLabel("亮度:"), c);
        c.gridx = 5;
        minMagField = new JTextField(4);
        panel.add(minMagField, c);
        c.gridx = 6;
        panel.add(new JLabel("~"), c);
        c.gridx = 7;
        maxMagField = new JTextField(4);
        panel.add(maxMagField, c);

        c.gridx = 8;
        panel.add(new JLabel("季节:"), c);
        c.gridx = 9;
        seasonCombo = new JComboBox<>(SEASON_OPTIONS);
        panel.add(seasonCombo, c);

        // 第二行：关键词 + 搜索 + 模式切换
        c.gridy = 1;
        c.gridx = 0;
        panel.add(new JLabel("关键词:"), c);
        c.gridx = 1;
        c.gridwidth = 3;
        keywordField = new JTextField(15);
        panel.add(keywordField, c);

        c.gridx = 4;
        c.gridwidth = 1;
        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> doSearch());
        panel.add(searchBtn, c);

        c.gridx = 5;
        c.gridwidth = 2;
        JButton allBtn = new JButton("全部星体");
        allBtn.addActionListener(e -> { currentMode = "all"; refreshTable(); });
        panel.add(allBtn, c);

        c.gridx = 7;
        c.gridwidth = 2;
        JButton popularBtn = new JButton("收藏排行");
        popularBtn.addActionListener(e -> { currentMode = "popular"; refreshTable(); });
        panel.add(popularBtn, c);

        c.gridx = 9;
        c.gridwidth = 1;
        JButton myFavBtn = new JButton("我的收藏");
        myFavBtn.addActionListener(e -> { currentMode = "mine"; refreshTable(); });
        panel.add(myFavBtn, c);

        return panel;
    }

    private JScrollPane buildTablePanel() {
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        table.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.getColumnModel().getColumn(9).setCellRenderer(new FavoriteRenderer());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == 9 && row >= 0) {
                    toggleFavorite(row);
                }
                if (e.getClickCount() == 2 && row >= 0 && col != 9) {
                    showDetailDialog(row);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        return scrollPane;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panel.setBackground(new Color(240, 240, 240));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));

        boolean isAdmin = currentUser.getRole() == UserRole.ADMIN;

        addBtn = new JButton("添加");
        addBtn.setVisible(isAdmin);
        addBtn.addActionListener(e -> showEditDialog(null));
        panel.add(addBtn);

        editBtn = new JButton("编辑");
        editBtn.setVisible(isAdmin);
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) editSelectedRow(row);
            else JOptionPane.showMessageDialog(this, "请先选择一行", "提示", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(editBtn);

        deleteBtn = new JButton("删除");
        deleteBtn.setVisible(isAdmin);
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) deleteSelectedRow(row);
            else JOptionPane.showMessageDialog(this, "请先选择一行", "提示", JOptionPane.INFORMATION_MESSAGE);
        });
        panel.add(deleteBtn);

        importBtn = new JButton("导入CSV");
        importBtn.setVisible(isAdmin);
        importBtn.addActionListener(e -> showImportDialog());
        panel.add(importBtn);

        modeLabel = new JLabel("  ");
        modeLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        panel.add(modeLabel);

        return panel;
    }

    // ==================== 数据刷新 ====================

    private void refreshTable() {
        tableModel.setRowCount(0);
        favoriteIds.clear();

        List<CelestialBody> bodies;
        switch (currentMode) {
            case "popular":
                bodies = bodyService.listByPopularity();
                modeLabel.setText("当前: 收藏排行");
                break;
            case "mine":
                bodies = bodyService.getFavorites(currentUser.getUserId());
                modeLabel.setText("当前: 我的收藏");
                break;
            default:
                bodies = bodyService.listAll();
                modeLabel.setText("当前: 全部星体");
                break;
        }

        for (CelestialBody b : bodies) {
            boolean isFav = bodyService.isFavorited(currentUser.getUserId(), b.getBodyId());
            if (isFav) favoriteIds.add(b.getBodyId());
            tableModel.addRow(new Object[]{
                b.getName(),
                b.getType() != null ? b.getType().getDisplayName() : "",
                b.getConstellation() != null ? b.getConstellation() : "",
                formatRA(b),
                formatDec(b),
                b.getMagnitude(),
                b.getBestSeason() != null ? b.getBestSeason() : "",
                b.getMessierNumber(),
                b.getNgcNumber(),
                isFav ? "★" : "☆"
            });
        }
        updateConstellationCombo(bodies);
    }

    private void updateConstellationCombo(List<CelestialBody> bodies) {
        String current = (String) constellationCombo.getSelectedItem();
        constellationCombo.removeAllItems();
        constellationCombo.addItem("全部");
        Set<String> seen = new HashSet<>();
        for (CelestialBody b : bodies) {
            if (b.getConstellation() != null && seen.add(b.getConstellation())) {
                constellationCombo.addItem(b.getConstellation());
            }
        }
        if (current != null) constellationCombo.setSelectedItem(current);
    }

    private void doSearch() {
        String constellation = null;
        if (constellationCombo.getSelectedIndex() > 0) {
            constellation = (String) constellationCombo.getSelectedItem();
        }
        String type = null;
        if (typeCombo.getSelectedIndex() > 0) {
            BodyType bodyType = BodyType.fromString((String) typeCombo.getSelectedItem());
            type = bodyType.name().toLowerCase();
        }
        BigDecimal minMag = parseBigDecimal(minMagField.getText());
        BigDecimal maxMag = parseBigDecimal(maxMagField.getText());
        String season = null;
        if (seasonCombo.getSelectedIndex() > 0) {
            season = (String) seasonCombo.getSelectedItem();
        }
        String keyword = keywordField.getText().trim();
        if (keyword.isEmpty()) keyword = null;

        List<CelestialBody> results = bodyService.search(
            constellation, type, minMag, maxMag, season, keyword);

        tableModel.setRowCount(0);
        favoriteIds.clear();
        for (CelestialBody b : results) {
            boolean isFav = bodyService.isFavorited(currentUser.getUserId(), b.getBodyId());
            if (isFav) favoriteIds.add(b.getBodyId());
            tableModel.addRow(new Object[]{
                b.getName(),
                b.getType() != null ? b.getType().getDisplayName() : "",
                b.getConstellation() != null ? b.getConstellation() : "",
                formatRA(b),
                formatDec(b),
                b.getMagnitude(),
                b.getBestSeason() != null ? b.getBestSeason() : "",
                b.getMessierNumber(),
                b.getNgcNumber(),
                isFav ? "★" : "☆"
            });
        }
        modeLabel.setText("搜索结果: " + results.size() + " 条");
    }

    // ==================== 收藏切换 ====================

    private void toggleFavorite(int row) {
        CelestialBody body = getBodyFromCurrentList(row);
        if (body == null) return;
        ServiceResult sr = bodyService.toggleFavorite(currentUser.getUserId(), body.getBodyId());
        if (sr.isSuccess()) {
            if (favoriteIds.contains(body.getBodyId())) {
                favoriteIds.remove(body.getBodyId());
                tableModel.setValueAt("☆", row, 9);
            } else {
                favoriteIds.add(body.getBodyId());
                tableModel.setValueAt("★", row, 9);
            }
        }
        modeLabel.setText(sr.getMessage());
    }

    // ==================== 详情/编辑/删除/导入 ====================

    private void showDetailDialog(int row) {
        CelestialBody body = getBodyFromCurrentList(row);
        if (body == null) return;
        JOptionPane.showMessageDialog(this,
            "名称: " + body.getName() + "\n"
          + "类型: " + (body.getType() != null ? body.getType().getDisplayName() : "") + "\n"
          + "星座: " + (body.getConstellation() != null ? body.getConstellation() : "") + "\n"
          + "赤经: " + formatRA(body) + "\n"
          + "赤纬: " + formatDec(body) + "\n"
          + "视星等: " + body.getMagnitude() + "\n"
          + "距离: " + body.getDistanceLy() + " 光年\n"
          + "梅西耶#: " + (body.getMessierNumber() != null ? body.getMessierNumber() : "-") + "\n"
          + "NGC#: " + (body.getNgcNumber() != null ? body.getNgcNumber() : "-") + "\n"
          + "最佳季节: " + (body.getBestSeason() != null ? body.getBestSeason() : "") + "\n"
          + "描述: " + (body.getDescription() != null ? body.getDescription() : ""),
          "星体详情 - " + body.getName(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void showEditDialog(CelestialBody existing) {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent instanceof JFrame ? (JFrame) parent : null,
            existing == null ? "添加星体" : "编辑星体", true);
        dialog.setSize(450, 520);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JComboBox<String> typeField = new JComboBox<>(new String[]{"恒星", "行星", "星云", "星团", "星系"});
        JTextField consField = new JTextField(20);
        JTextField raHField = new JTextField(5);
        JTextField raMField = new JTextField(5);
        JTextField decDegField = new JTextField(5);
        JTextField decMinField = new JTextField(5);
        JTextField magField = new JTextField(10);
        JTextField distField = new JTextField(10);
        JTextField messierField = new JTextField(10);
        JTextField ngcField = new JTextField(10);
        JComboBox<String> seasonField = new JComboBox<>(new String[]{"", "春", "夏", "秋", "冬"});
        JTextArea descField = new JTextArea(3, 20);
        descField.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(descField);

        if (existing != null) {
            nameField.setText(existing.getName());
            typeField.setSelectedItem(existing.getType() != null ? existing.getType().getDisplayName() : "恒星");
            consField.setText(existing.getConstellation());
            raHField.setText(String.valueOf(existing.getRaH()));
            raMField.setText(String.valueOf(existing.getRaM()));
            decDegField.setText(String.valueOf(existing.getDecDeg()));
            decMinField.setText(String.valueOf(existing.getDecMin()));
            magField.setText(existing.getMagnitude() != null ? existing.getMagnitude().toString() : "");
            distField.setText(existing.getDistanceLy() != null ? existing.getDistanceLy().toString() : "");
            messierField.setText(existing.getMessierNumber() != null ? existing.getMessierNumber().toString() : "");
            ngcField.setText(existing.getNgcNumber() != null ? existing.getNgcNumber().toString() : "");
            seasonField.setSelectedItem(existing.getBestSeason() != null ? existing.getBestSeason() : "");
            descField.setText(existing.getDescription());
        }

        int row = 0;
        addFormRow(formPanel, c, row++, "名称:", nameField);
        addFormRow(formPanel, c, row++, "类型:", typeField);
        addFormRow(formPanel, c, row++, "星座:", consField);
        addFormRow(formPanel, c, row++, "赤经 (h):", raHField);
        addFormRow(formPanel, c, row++, "赤经 (m):", raMField);
        addFormRow(formPanel, c, row++, "赤纬 (°):", decDegField);
        addFormRow(formPanel, c, row++, "赤纬 ('):", decMinField);
        addFormRow(formPanel, c, row++, "视星等:", magField);
        addFormRow(formPanel, c, row++, "距离 (ly):", distField);
        addFormRow(formPanel, c, row++, "梅西耶#:", messierField);
        addFormRow(formPanel, c, row++, "NGC#:", ngcField);
        addFormRow(formPanel, c, row++, "最佳季节:", seasonField);
        c.gridy = row; c.gridx = 0;
        formPanel.add(new JLabel("描述:"), c);
        c.gridx = 1;
        formPanel.add(descScroll, c);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("保存");
        JButton cancelBtn = new JButton("取消");

        saveBtn.addActionListener(ev -> {
            CelestialBody body = existing != null ? existing : new CelestialBody();
            body.setName(nameField.getText().trim());
            body.setType(BodyType.fromString((String) typeField.getSelectedItem()));
            body.setConstellation(consField.getText().trim());
            try {
                body.setRaH(Integer.parseInt(raHField.getText().trim()));
                body.setRaM(Integer.parseInt(raMField.getText().trim()));
                body.setDecDeg(Integer.parseInt(decDegField.getText().trim()));
                body.setDecMin(Integer.parseInt(decMinField.getText().trim()));
                body.setMagnitude(parseBigDecimal(magField.getText()));
                body.setDistanceLy(parseBigDecimal(distField.getText()));
                String mStr = messierField.getText().trim();
                body.setMessierNumber(mStr.isEmpty() ? null : Integer.parseInt(mStr));
                String nStr = ngcField.getText().trim();
                body.setNgcNumber(nStr.isEmpty() ? null : Integer.parseInt(nStr));
                body.setBestSeason((String) seasonField.getSelectedItem());
                body.setDescription(descField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "数值字段格式错误: " + ex.getMessage(),
                    "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            ServiceResult sr;
            if (existing == null) {
                sr = bodyService.addBody(body, currentUser.getUserId());
            } else {
                sr = bodyService.updateBody(body, currentUser.getUserId());
            }
            JOptionPane.showMessageDialog(dialog, sr.getMessage(),
                sr.isSuccess() ? "成功" : "失败",
                sr.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
            if (sr.isSuccess()) {
                dialog.dispose();
                refreshTable();
            }
        });

        cancelBtn.addActionListener(ev -> dialog.dispose());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void editSelectedRow(int row) {
        CelestialBody body = getBodyFromCurrentList(row);
        if (body != null) showEditDialog(body);
    }

    private void deleteSelectedRow(int row) {
        CelestialBody body = getBodyFromCurrentList(row);
        if (body == null) return;
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定删除星体 \"" + body.getName() + "\" 吗？\n关联的收藏记录也会一并删除。",
            "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            ServiceResult sr = bodyService.deleteBody(body.getBodyId(), currentUser.getUserId());
            JOptionPane.showMessageDialog(this, sr.getMessage(),
                sr.isSuccess() ? "成功" : "失败", JOptionPane.INFORMATION_MESSAGE);
            if (sr.isSuccess()) refreshTable();
        }
    }

    private void showImportDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        new com.astrolog.ui.dialog.ImportCsvDialog(
            parent instanceof JFrame ? (JFrame) parent : null,
            bodyService, currentUser.getUserId(), this::refreshTable);
    }

    // ==================== 辅助方法 ====================

    private CelestialBody getBodyFromCurrentList(int row) {
        if (row < 0 || row >= tableModel.getRowCount()) return null;
        String name = (String) tableModel.getValueAt(row, 0);
        List<CelestialBody> bodies = bodyService.listAll();
        for (CelestialBody b : bodies) {
            if (b.getName().equals(name)) return b;
        }
        return null;
    }

    public void highlightBody(int bodyId) {
        CelestialBody body = bodyService.getBody(bodyId);
        if (body == null) return;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String name = (String) tableModel.getValueAt(i, 0);
            if (body.getName().equals(name)) {
                int viewRow = table.convertRowIndexToView(i);
                table.setRowSelectionInterval(viewRow, viewRow);
                table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                return;
            }
        }
    }

    private void addFormRow(JPanel panel, GridBagConstraints c, int row,
                            String label, JComponent field) {
        c.gridy = row;
        c.gridx = 0;
        panel.add(new JLabel(label), c);
        c.gridx = 1;
        panel.add(field, c);
    }

    private String formatRA(CelestialBody b) {
        return b.getRaH() + "h" + b.getRaM() + "m";
    }

    private String formatDec(CelestialBody b) {
        int deg = b.getDecDeg();
        return deg + "°" + b.getDecMin() + "'";
    }

    private BigDecimal parseBigDecimal(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return new BigDecimal(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==================== 收藏列渲染 ====================

    private static class FavoriteRenderer extends JLabel implements TableCellRenderer {
        public FavoriteRenderer() {
            setHorizontalAlignment(CENTER);
            setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value != null ? value.toString() : "☆");
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            setOpaque(true);
            return this;
        }
    }
}
