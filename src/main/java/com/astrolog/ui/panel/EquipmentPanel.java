package com.astrolog.ui.panel;

import com.astrolog.model.Equipment;
import com.astrolog.model.EquipmentMaintenance;
import com.astrolog.model.User;
import com.astrolog.model.enums.EquipStatus;
import com.astrolog.model.enums.EquipType;
import com.astrolog.service.EquipService;
import com.astrolog.service.ServiceResult;
import com.astrolog.ui.dialog.EquipmentMaintDialog;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EquipmentPanel extends JPanel {

    private static final String[] EQUIP_COLUMNS = {"名称", "类型", "口径(mm)", "焦距(mm)", "状态", "购买日期", "使用次数"};
    private static final String[] MAINT_COLUMNS = {"日期", "描述", "费用", "下次维护"};
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final User currentUser;
    private final EquipService equipService;

    private DefaultTableModel equipTableModel;
    private JTable equipTable;
    private DefaultTableModel maintTableModel;
    private JTable maintTable;
    private JTextField searchField;
    private JLabel statusLabel;

    private List<Equipment> equipmentList;

    public EquipmentPanel(User currentUser) {
        this.currentUser = currentUser;
        this.equipService = new EquipService();
        initComponents();
        loadEquipment();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === 上部分：器材列表 ===
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));

        // 工具栏
        JPanel equipToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        equipToolbar.add(new JLabel("关键字搜索:"));
        searchField = new JTextField(15);
        equipToolbar.add(searchField);

        JButton searchBtn = new JButton("搜索");
        searchBtn.addActionListener(e -> searchEquipment());
        equipToolbar.add(searchBtn);

        JButton sortByUsageBtn = new JButton("按使用次数排序");
        sortByUsageBtn.addActionListener(e -> sortByUsage());
        equipToolbar.add(sortByUsageBtn);

        equipToolbar.add(Box.createHorizontalStrut(20));

        JButton addBtn = new JButton("添加器材");
        addBtn.addActionListener(e -> showEquipDialog(null));
        equipToolbar.add(addBtn);

        JButton editBtn = new JButton("编辑");
        editBtn.addActionListener(e -> {
            int row = equipTable.getSelectedRow();
            if (row >= 0) {
                Equipment equip = equipmentList.get(row);
                if (currentUser.getUserId() == equip.getUserId()) {
                    showEquipDialog(equip);
                } else {
                    JOptionPane.showMessageDialog(this, "只能编辑自己的器材", "提示", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择器材", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        equipToolbar.add(editBtn);

        JButton deleteBtn = new JButton("删除");
        deleteBtn.addActionListener(e -> {
            int row = equipTable.getSelectedRow();
            if (row >= 0) {
                Equipment equip = equipmentList.get(row);
                if (currentUser.getUserId() == equip.getUserId()) {
                    deleteEquipment(equip);
                } else {
                    JOptionPane.showMessageDialog(this, "只能删除自己的器材", "提示", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择器材", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        equipToolbar.add(deleteBtn);

        topPanel.add(equipToolbar, BorderLayout.NORTH);

        // 器材表格
        equipTableModel = new DefaultTableModel(EQUIP_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        equipTable = new JTable(equipTableModel);
        equipTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        equipTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshMaintTable();
            }
        });
        JScrollPane equipScroll = new JScrollPane(equipTable);
        equipScroll.setPreferredSize(new Dimension(800, 250));
        topPanel.add(equipScroll, BorderLayout.CENTER);

        add(topPanel, BorderLayout.CENTER);

        // === 下部分：维护日志 ===
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "维护记录",
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Microsoft YaHei", Font.BOLD, 13)));

        JPanel maintToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton addMaintBtn = new JButton("添加维护");
        addMaintBtn.addActionListener(e -> {
            int row = equipTable.getSelectedRow();
            if (row >= 0) {
                Equipment equip = equipmentList.get(row);
                if (currentUser.getUserId() == equip.getUserId()) {
                    new EquipmentMaintDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                        equip.getEquipId(), null, this::refreshMaintTable);
                } else {
                    JOptionPane.showMessageDialog(this, "只能维护自己的器材", "提示", JOptionPane.WARNING_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "请先选择器材", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });
        maintToolbar.add(addMaintBtn);

        JButton editMaintBtn = new JButton("编辑");
        editMaintBtn.addActionListener(e -> editMaintenance());
        maintToolbar.add(editMaintBtn);

        JButton deleteMaintBtn = new JButton("删除");
        deleteMaintBtn.addActionListener(e -> deleteMaintenance());
        maintToolbar.add(deleteMaintBtn);

        bottomPanel.add(maintToolbar, BorderLayout.NORTH);

        maintTableModel = new DefaultTableModel(MAINT_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        maintTable = new JTable(maintTableModel);
        maintTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane maintScroll = new JScrollPane(maintTable);
        maintScroll.setPreferredSize(new Dimension(800, 150));
        bottomPanel.add(maintScroll, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // 状态栏
        statusLabel = new JLabel(" ");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        statusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
    }

    private Object[] toEquipRow(Equipment e, int usageCount) {
        return new Object[]{
            e.getName(),
            e.getType() != null ? e.getType().getDisplayName() : "",
            e.getAperture() != null ? e.getAperture().toString() : "",
            e.getFocalLength(),
            e.getStatus() != null ? e.getStatus().getDisplayName() : "",
            e.getPurchaseDate() != null ? e.getPurchaseDate().format(DATE_FMT) : "",
            usageCount
        };
    }

    private Map<Integer, Integer> batchFetchUsageCounts() {
        if (equipmentList.isEmpty()) {
            return Collections.emptyMap();
        }
        List<Integer> ids = equipmentList.stream()
            .map(Equipment::getEquipId)
            .collect(Collectors.toList());
        return equipService.batchGetUsageCounts(ids);
    }

    private void populateEquipTable() {
        Map<Integer, Integer> usageMap = batchFetchUsageCounts();
        equipTableModel.setRowCount(0);
        for (Equipment e : equipmentList) {
            int usageCount = usageMap.getOrDefault(e.getEquipId(), 0);
            equipTableModel.addRow(toEquipRow(e, usageCount));
        }
        maintTableModel.setRowCount(0);
    }

    private void loadEquipment() {
        equipmentList = equipService.listByUser(currentUser.getUserId());
        populateEquipTable();
        statusLabel.setText("共 " + equipmentList.size() + " 件器材");
    }

    private void searchEquipment() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadEquipment();
            return;
        }
        equipmentList = equipService.searchByName(currentUser.getUserId(), keyword);
        populateEquipTable();
        statusLabel.setText("搜索 '" + keyword + "': " + equipmentList.size() + " 件结果");
    }

    private void sortByUsage() {
        equipmentList = equipService.listByUsage(currentUser.getUserId());
        populateEquipTable();
        statusLabel.setText("按使用次数排序: " + equipmentList.size() + " 件器材");
    }

    private void refreshMaintTable() {
        int row = equipTable.getSelectedRow();
        maintTableModel.setRowCount(0);
        if (row < 0 || row >= equipmentList.size()) {
            return;
        }
        Equipment equip = equipmentList.get(row);
        List<EquipmentMaintenance> maintList = equipService.getMaintenanceHistory(equip.getEquipId());
        for (EquipmentMaintenance m : maintList) {
            maintTableModel.addRow(new Object[]{
                m.getMaintDate() != null ? m.getMaintDate().format(DATE_FMT) : "",
                m.getDescription() != null ? m.getDescription() : "",
                m.getCost() != null ? m.getCost().toString() : "",
                m.getNextMaintDate() != null ? m.getNextMaintDate().format(DATE_FMT) : ""
            });
        }
    }

    private void showEquipDialog(Equipment existing) {
        JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(this),
            existing == null ? "添加器材" : "编辑器材", true);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 名称
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("名称:"), gbc);
        JTextField nameField = new JTextField(20);
        gbc.gridx = 1;
        form.add(nameField, gbc);

        // 类型
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("类型:"), gbc);
        JComboBox<String> typeCombo = new JComboBox<>();
        for (EquipType t : EquipType.values()) {
            typeCombo.addItem(t.getDisplayName());
        }
        gbc.gridx = 1;
        form.add(typeCombo, gbc);

        // 口径
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("口径(mm):"), gbc);
        JTextField apertureField = new JTextField(20);
        gbc.gridx = 1;
        form.add(apertureField, gbc);

        // 焦距
        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("焦距(mm):"), gbc);
        JTextField focalField = new JTextField(20);
        gbc.gridx = 1;
        form.add(focalField, gbc);

        // 购买日期
        gbc.gridx = 0; gbc.gridy = 4;
        form.add(new JLabel("购买日期(yyyy-MM-dd):"), gbc);
        JTextField purchaseField = new JTextField(20);
        gbc.gridx = 1;
        form.add(purchaseField, gbc);

        // 状态
        gbc.gridx = 0; gbc.gridy = 5;
        form.add(new JLabel("状态:"), gbc);
        JComboBox<String> statusCombo = new JComboBox<>();
        for (EquipStatus s : EquipStatus.values()) {
            statusCombo.addItem(s.getDisplayName());
        }
        gbc.gridx = 1;
        form.add(statusCombo, gbc);

        // 描述
        gbc.gridx = 0; gbc.gridy = 6;
        form.add(new JLabel("描述:"), gbc);
        JTextArea descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        gbc.gridx = 1;
        form.add(descScroll, gbc);

        // 预填编辑数据
        if (existing != null) {
            nameField.setText(existing.getName());
            typeCombo.setSelectedItem(existing.getType() != null ? existing.getType().getDisplayName() : EquipType.OTHER.getDisplayName());
            apertureField.setText(existing.getAperture() != null ? existing.getAperture().toString() : "");
            focalField.setText(String.valueOf(existing.getFocalLength()));
            purchaseField.setText(existing.getPurchaseDate() != null ? existing.getPurchaseDate().format(DATE_FMT) : "");
            statusCombo.setSelectedItem(existing.getStatus() != null ? existing.getStatus().getDisplayName() : EquipStatus.ACTIVE.getDisplayName());
            descArea.setText(existing.getDescription() != null ? existing.getDescription() : "");
        }

        dialog.add(form, BorderLayout.CENTER);

        // 按钮面板
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("保存");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "器材名称不能为空", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Equipment equip = existing != null ? existing : new Equipment();
            equip.setName(name);
            equip.setUserId(currentUser.getUserId());
            equip.setType(EquipType.fromString((String) typeCombo.getSelectedItem()));

            try {
                String apText = apertureField.getText().trim();
                equip.setAperture(apText.isEmpty() ? null : new BigDecimal(apText));

                String flText = focalField.getText().trim();
                equip.setFocalLength(flText.isEmpty() ? 0 : Integer.parseInt(flText));

                String pdText = purchaseField.getText().trim();
                equip.setPurchaseDate(pdText.isEmpty() ? null : LocalDate.parse(pdText, DATE_FMT));
            } catch (NumberFormatException | DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "数值或日期格式错误: " + ex.getMessage(),
                    "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            equip.setStatus(EquipStatus.fromString((String) statusCombo.getSelectedItem()));
            equip.setDescription(descArea.getText().trim().isEmpty() ? null : descArea.getText().trim());

            ServiceResult result;
            if (existing == null) {
                result = equipService.addEquipment(equip);
            } else {
                result = equipService.updateEquipment(equip);
            }

            JOptionPane.showMessageDialog(dialog, result.getMessage(),
                result.isSuccess() ? "成功" : "失败",
                result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

            if (result.isSuccess()) {
                loadEquipment();
                dialog.dispose();
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void deleteEquipment(Equipment equip) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要删除器材 \"" + equip.getName() + "\" 吗？\n关联的维护记录也将被删除。",
            "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        ServiceResult result = equipService.deleteEquipment(equip.getEquipId());
        JOptionPane.showMessageDialog(this, result.getMessage(),
            result.isSuccess() ? "成功" : "失败",
            result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        if (result.isSuccess()) {
            loadEquipment();
        }
    }

    private void editMaintenance() {
        int equipRow = equipTable.getSelectedRow();
        int maintRow = maintTable.getSelectedRow();
        if (equipRow < 0 || maintRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择器材和维护记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Equipment equip = equipmentList.get(equipRow);
        if (currentUser.getUserId() != equip.getUserId()) {
            JOptionPane.showMessageDialog(this, "只能编辑自己器材的维护记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<EquipmentMaintenance> maintList = equipService.getMaintenanceHistory(equip.getEquipId());
        if (maintRow < 0 || maintRow >= maintList.size()) {
            return;
        }
        EquipmentMaintenance existing = maintList.get(maintRow);
        new EquipmentMaintDialog((JFrame) SwingUtilities.getWindowAncestor(this),
            equip.getEquipId(), existing, this::refreshMaintTable);
    }

    private void deleteMaintenance() {
        int equipRow = equipTable.getSelectedRow();
        int maintRow = maintTable.getSelectedRow();
        if (equipRow < 0 || maintRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择器材和维护记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Equipment equip = equipmentList.get(equipRow);
        if (currentUser.getUserId() != equip.getUserId()) {
            JOptionPane.showMessageDialog(this, "只能删除自己器材的维护记录", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<EquipmentMaintenance> maintList = equipService.getMaintenanceHistory(equip.getEquipId());
        if (maintRow < 0 || maintRow >= maintList.size()) {
            return;
        }
        EquipmentMaintenance maint = maintList.get(maintRow);
        int confirm = JOptionPane.showConfirmDialog(this,
            "确定要删除该维护记录吗？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        ServiceResult result = equipService.deleteMaintenance(maint.getMaintId());
        JOptionPane.showMessageDialog(this, result.getMessage(),
            result.isSuccess() ? "成功" : "失败",
            result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        if (result.isSuccess()) {
            refreshMaintTable();
        }
    }
}
