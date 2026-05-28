package com.astrolog.ui.panel;

import com.astrolog.dao.ObsDao;
import com.astrolog.model.Observation;
import com.astrolog.model.ObservationSite;
import com.astrolog.model.User;
import com.astrolog.service.ServiceResult;
import com.astrolog.service.SiteService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObservationSitePanel extends JPanel {

    private static final String[] COLUMNS = {
        "名称", "纬度", "经度", "海拔(m)", "波特尔暗空等级", "最佳时段", "观测次数"
    };
    private static final String[] BORTLE_DESC = {
        "", "1-极暗天空", "2-典型暗空", "3-乡村天空", "4-乡村/郊区过渡",
        "5-郊区天空", "6-明亮郊区", "7-郊区/城市过渡", "8-城市天空", "9-市中心"
    };

    private final User currentUser;
    private final SiteService siteService;
    private final ObsDao obsDao;

    private JTable siteTable;
    private SiteTableModel tableModel;
    private List<ObservationSite> siteList;
    private Map<Integer, Integer> obsCountMap;

    public ObservationSitePanel(User currentUser) {
        this.currentUser = currentUser;
        this.siteService = new SiteService();
        this.obsDao = new ObsDao();
        this.obsCountMap = new HashMap<>();

        initComponents();
        loadData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new SiteTableModel();
        siteTable = new JTable(tableModel);
        siteTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        siteTable.setRowHeight(28);
        siteTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        siteTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        siteTable.getColumnModel().getColumn(4).setCellRenderer(new BortleRenderer());
        siteTable.getColumnModel().getColumn(0).setPreferredWidth(120);
        siteTable.getColumnModel().getColumn(4).setPreferredWidth(130);
        siteTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        add(new JScrollPane(siteTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        btnPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JButton addBtn = new JButton("添加地点");
        addBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        addBtn.addActionListener(e -> showEditDialog(null));

        JButton editBtn = new JButton("编辑");
        editBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        editBtn.addActionListener(e -> {
            int row = siteTable.getSelectedRow();
            if (row >= 0) {
                showEditDialog(siteList.get(row));
            } else {
                JOptionPane.showMessageDialog(this, "请先选择一个观测地", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton delBtn = new JButton("删除");
        delBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        delBtn.addActionListener(e -> {
            int row = siteTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "请先选择一个观测地", "提示", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            ObservationSite site = siteList.get(row);
            int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除观测地 \"" + site.getName() + "\" 吗？\n删除后相关观测记录仍保留但不再关联此地点。",
                "确认删除", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                ServiceResult result = siteService.deleteSite(site.getSiteId());
                JOptionPane.showMessageDialog(this, result.getMessage());
                if (result.isSuccess()) {
                    loadData();
                }
            }
        });

        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadData() {
        siteList = siteService.listByUser(currentUser.getUserId());
        obsCountMap = loadObsCounts();
        tableModel.fireTableDataChanged();
    }

    private Map<Integer, Integer> loadObsCounts() {
        Map<Integer, Integer> counts = new HashMap<>();
        List<Observation> allObs = obsDao.findAllByUserId(currentUser.getUserId());
        for (Observation obs : allObs) {
            if (obs.getSiteId() != null) {
                counts.merge(obs.getSiteId(), 1, Integer::sum);
            }
        }
        return counts;
    }

    private void showEditDialog(ObservationSite existing) {
        boolean isEdit = existing != null;
        JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(this),
            isEdit ? "编辑观测地" : "添加观测地", true);
        dialog.setSize(420, 360);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField nameField = new JTextField(20);
        JTextField latField = new JTextField(10);
        JTextField lonField = new JTextField(10);
        JTextField altField = new JTextField(10);
        JComboBox<String> bortleCombo = new JComboBox<>();
        for (int i = 1; i <= 9; i++) {
            bortleCombo.addItem(BORTLE_DESC[i]);
        }
        JTextField bestTimeField = new JTextField(20);

        if (isEdit) {
            nameField.setText(existing.getName());
            latField.setText(existing.getLatitude() != null ? existing.getLatitude().toString() : "");
            lonField.setText(existing.getLongitude() != null ? existing.getLongitude().toString() : "");
            altField.setText(String.valueOf(existing.getAltitude()));
            bortleCombo.setSelectedIndex(existing.getBortleScale() - 1);
            bestTimeField.setText(existing.getBestTime() != null ? existing.getBestTime() : "");
        } else {
            bortleCombo.setSelectedIndex(4);
        }

        addFormRow(form, gbc, 0, "名称：", nameField);
        addFormRow(form, gbc, 1, "纬度(°):", latField);
        addFormRow(form, gbc, 2, "经度(°):", lonField);
        addFormRow(form, gbc, 3, "海拔(m):", altField);
        addFormRow(form, gbc, 4, "波特尔等级:", bortleCombo);
        addFormRow(form, gbc, 5, "最佳时段:", bestTimeField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("保存");
        saveBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        JButton cancelBtn = new JButton("取消");
        cancelBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));

        saveBtn.addActionListener(e -> {
            ObservationSite site = isEdit ? existing : new ObservationSite();
            site.setUserId(currentUser.getUserId());

            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "名称不能为空", "输入错误", JOptionPane.WARNING_MESSAGE);
                return;
            }
            site.setName(name);

            try {
                String latStr = latField.getText().trim();
                if (!latStr.isEmpty()) {
                    site.setLatitude(new BigDecimal(latStr));
                }
                String lonStr = lonField.getText().trim();
                if (!lonStr.isEmpty()) {
                    site.setLongitude(new BigDecimal(lonStr));
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "经纬度格式不正确", "输入错误", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                site.setAltitude(Integer.parseInt(altField.getText().trim()));
            } catch (NumberFormatException ex) {
                site.setAltitude(0);
            }

            site.setBortleScale(bortleCombo.getSelectedIndex() + 1);
            site.setBestTime(bestTimeField.getText().trim());

            ServiceResult result = isEdit
                ? siteService.updateSite(site)
                : siteService.addSite(site);

            JOptionPane.showMessageDialog(dialog, result.getMessage());
            if (result.isSuccess()) {
                dialog.dispose();
                loadData();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);

        dialog.setLayout(new BorderLayout());
        dialog.add(form, BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row,
                            String label, Component field) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        panel.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.8;
        field.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        panel.add(field, gbc);
    }

    private class SiteTableModel extends AbstractTableModel {
        @Override
        public int getRowCount() {
            return siteList != null ? siteList.size() : 0;
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int col) {
            return COLUMNS[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            ObservationSite site = siteList.get(row);
            switch (col) {
                case 0: return site.getName();
                case 1: return site.getLatitude() != null ? site.getLatitude().toString() : "";
                case 2: return site.getLongitude() != null ? site.getLongitude().toString() : "";
                case 3: return site.getAltitude();
                case 4: return site.getBortleScale();
                case 5: return site.getBestTime() != null ? site.getBestTime() : "";
                case 6:
                    int count = obsCountMap.getOrDefault(site.getSiteId(), 0);
                    return count > 0 ? String.valueOf(count) : "0";
                default: return "";
            }
        }
    }

    private static class BortleRenderer extends DefaultTableCellRenderer {
        private static final Color[] BORTLE_COLORS = {
            new Color(30, 30, 80),
            new Color(40, 60, 120),
            new Color(60, 90, 160),
            new Color(80, 130, 80),
            new Color(160, 160, 50),
            new Color(200, 160, 40),
            new Color(220, 120, 30),
            new Color(220, 70, 40),
            new Color(200, 40, 40)
        };

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (value instanceof Integer) {
                int bortle = (Integer) value;
                if (bortle >= 1 && bortle <= 9) {
                    Color bg = BORTLE_COLORS[bortle - 1];
                    if (isSelected) {
                        setBackground(bg.darker());
                    } else {
                        setBackground(bg);
                    }
                    setForeground(bortle <= 6 ? Color.WHITE : Color.BLACK);
                    setHorizontalAlignment(CENTER);
                }
            }
            return c;
        }
    }
}
