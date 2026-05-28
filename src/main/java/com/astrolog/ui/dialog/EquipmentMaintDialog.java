package com.astrolog.ui.dialog;

import com.astrolog.model.EquipmentMaintenance;
import com.astrolog.service.EquipService;
import com.astrolog.service.ServiceResult;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EquipmentMaintDialog extends JDialog {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final EquipService equipService;
    private final int equipId;
    private final EquipmentMaintenance existing;
    private final Runnable onSaved;

    private JTextField dateField;
    private JTextArea descArea;
    private JTextField costField;
    private JTextField nextDateField;

    public EquipmentMaintDialog(JFrame parent, int equipId,
                                 EquipmentMaintenance existing,
                                 Runnable onSaved) {
        super(parent, existing == null ? "添加维护记录" : "编辑维护记录", true);
        this.equipId = equipId;
        this.existing = existing;
        this.onSaved = onSaved;
        this.equipService = new EquipService();
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // 维护日期
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("维护日期(yyyy-MM-dd):"), gbc);
        dateField = new JTextField(20);
        gbc.gridx = 1;
        form.add(dateField, gbc);

        // 描述
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("描述:"), gbc);
        descArea = new JTextArea(4, 20);
        descArea.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        gbc.gridx = 1;
        form.add(descScroll, gbc);

        // 费用
        gbc.gridx = 0; gbc.gridy = 2;
        form.add(new JLabel("费用:"), gbc);
        costField = new JTextField(20);
        gbc.gridx = 1;
        form.add(costField, gbc);

        // 下次维护日期
        gbc.gridx = 0; gbc.gridy = 3;
        form.add(new JLabel("下次维护日期(yyyy-MM-dd):"), gbc);
        nextDateField = new JTextField(20);
        gbc.gridx = 1;
        form.add(nextDateField, gbc);

        // 预填编辑数据
        if (existing != null) {
            dateField.setText(existing.getMaintDate() != null ? existing.getMaintDate().format(DATE_FMT) : "");
            descArea.setText(existing.getDescription() != null ? existing.getDescription() : "");
            costField.setText(existing.getCost() != null ? existing.getCost().toString() : "");
            nextDateField.setText(existing.getNextMaintDate() != null ? existing.getNextMaintDate().format(DATE_FMT) : "");
        }

        add(form, BorderLayout.CENTER);

        // 按钮面板
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("保存");
        JButton cancelBtn = new JButton("取消");
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        add(btnPanel, BorderLayout.SOUTH);

        saveBtn.addActionListener(e -> save());
        cancelBtn.addActionListener(e -> dispose());
    }

    private void save() {
        String dateText = dateField.getText().trim();
        if (dateText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "维护日期不能为空", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LocalDate maintDate;
        try {
            maintDate = LocalDate.parse(dateText, DATE_FMT);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "日期格式错误，请使用 yyyy-MM-dd", "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        EquipmentMaintenance m = existing != null ? existing : new EquipmentMaintenance();
        m.setEquipId(equipId);
        m.setMaintDate(maintDate);
        m.setDescription(descArea.getText().trim().isEmpty() ? null : descArea.getText().trim());

        try {
            String costText = costField.getText().trim();
            m.setCost(costText.isEmpty() ? null : new BigDecimal(costText));

            String nextText = nextDateField.getText().trim();
            m.setNextMaintDate(nextText.isEmpty() ? null : LocalDate.parse(nextText, DATE_FMT));
        } catch (NumberFormatException | DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                "数值或日期格式错误: " + ex.getMessage(),
                "输入错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ServiceResult result;
        if (existing == null) {
            result = equipService.addMaintenance(m);
        } else {
            result = equipService.updateMaintenance(m);
        }

        JOptionPane.showMessageDialog(this, result.getMessage(),
            result.isSuccess() ? "成功" : "失败",
            result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

        if (result.isSuccess()) {
            if (onSaved != null) {
                onSaved.run();
            }
            dispose();
        }
    }
}
