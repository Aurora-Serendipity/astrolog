package com.astrolog.ui.panel;

import com.astrolog.model.User;
import com.astrolog.model.enums.UserRole;
import com.astrolog.service.ServiceResult;
import com.astrolog.service.UserService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserPanel extends JPanel {

    private final User currentUser;
    private final UserService userService;

    private JTextField cityField;
    private JTextField latField;
    private JTextField lonField;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JPanel adminPanel;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public UserPanel(User currentUser) {
        this.currentUser = currentUser;
        this.userService = new UserService();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(240, 240, 240));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        add(createProfilePanel());
        add(Box.createVerticalStrut(15));
        add(createAdminPanel());
        add(Box.createVerticalGlue());
    }

    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("个人信息"));
        panel.setBackground(Color.WHITE);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Row 0: username (read-only)
        addReadOnlyRow(panel, gbc, 0, "用户名:", currentUser.getUsername());
        // Row 1: role (read-only)
        addReadOnlyRow(panel, gbc, 1, "角色:", currentUser.getRole().getDisplayName());
        // Row 2: create time (read-only)
        addReadOnlyRow(panel, gbc, 2, "注册时间:", format(currentUser.getCreateTime()));
        // Row 3: last login (read-only)
        addReadOnlyRow(panel, gbc, 3, "最后登录:", format(currentUser.getLastLogin()));

        // Row 4: city (editable)
        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(new JLabel("所在城市:"), gbc);
        cityField = new JTextField(currentUser.getCity() != null ? currentUser.getCity() : "", 15);
        gbc.gridx = 1;
        panel.add(cityField, gbc);

        // Row 5: lat (editable)
        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(new JLabel("默认纬度:"), gbc);
        latField = new JTextField(currentUser.getDefaultLat() != null
            ? currentUser.getDefaultLat().toString() : "", 15);
        gbc.gridx = 1;
        panel.add(latField, gbc);

        // Row 6: lon (editable)
        gbc.gridy = 6; gbc.gridx = 0;
        panel.add(new JLabel("默认经度:"), gbc);
        lonField = new JTextField(currentUser.getDefaultLon() != null
            ? currentUser.getDefaultLon().toString() : "", 15);
        gbc.gridx = 1;
        panel.add(lonField, gbc);

        // Row 7: buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton changePwdButton = new JButton("修改密码");
        changePwdButton.addActionListener(e -> onChangePassword());
        buttonPanel.add(changePwdButton);

        JButton saveButton = new JButton("保存修改");
        saveButton.addActionListener(e -> onSaveProfile());
        buttonPanel.add(saveButton);

        JButton avatarButton = new JButton("头像(开发中)");
        avatarButton.setEnabled(false);
        buttonPanel.add(avatarButton);

        gbc.gridy = 7; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void addReadOnlyRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridy = row; gbc.gridx = 0;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        JLabel valueLabel = new JLabel(value != null ? value : "—");
        valueLabel.setForeground(Color.DARK_GRAY);
        gbc.gridx = 1;
        panel.add(valueLabel, gbc);
    }

    private JPanel createAdminPanel() {
        adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBorder(BorderFactory.createTitledBorder("用户管理"));
        adminPanel.setBackground(Color.WHITE);
        adminPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] columns = {"用户名", "角色", "状态", "最后登录"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        adminPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        actionPanel.setBackground(Color.WHITE);

        JButton enableButton = new JButton("启用");
        enableButton.addActionListener(e -> onEnableUser());
        actionPanel.add(enableButton);

        JButton disableButton = new JButton("禁用");
        disableButton.addActionListener(e -> onDisableUser());
        actionPanel.add(disableButton);

        JButton resetPwdButton = new JButton("重置密码");
        resetPwdButton.addActionListener(e -> onResetPassword());
        actionPanel.add(resetPwdButton);

        adminPanel.add(actionPanel, BorderLayout.SOUTH);

        adminPanel.setVisible(currentUser.getRole() == UserRole.ADMIN);
        if (currentUser.getRole() == UserRole.ADMIN) {
            refreshUserTable();
        }

        return adminPanel;
    }

    private void onSaveProfile() {
        String city = cityField.getText().trim();
        String latText = latField.getText().trim();
        String lonText = lonField.getText().trim();

        BigDecimal lat = null;
        BigDecimal lon = null;
        if (!latText.isEmpty()) {
            try {
                lat = new BigDecimal(latText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "纬度格式不正确",
                    "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        if (!lonText.isEmpty()) {
            try {
                lon = new BigDecimal(lonText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "经度格式不正确",
                    "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        ServiceResult result = userService.updateProfile(
            currentUser.getUserId(), city, lat, lon);

        if (result.isSuccess()) {
            currentUser.setCity(city);
            currentUser.setDefaultLat(lat);
            currentUser.setDefaultLon(lon);
            JOptionPane.showMessageDialog(this, result.getMessage(),
                "成功", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                "更新失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onChangePassword() {
        JPanel pwdPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        JPasswordField oldPwdField = new JPasswordField(15);
        JPasswordField newPwdField = new JPasswordField(15);
        JPasswordField confirmPwdField = new JPasswordField(15);

        pwdPanel.add(new JLabel("原密码:"));
        pwdPanel.add(oldPwdField);
        pwdPanel.add(new JLabel("新密码:"));
        pwdPanel.add(newPwdField);
        pwdPanel.add(new JLabel("确认新密码:"));
        pwdPanel.add(confirmPwdField);

        int option = JOptionPane.showConfirmDialog(this, pwdPanel,
            "修改密码", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option != JOptionPane.OK_OPTION) return;

        String oldPwd = new String(oldPwdField.getPassword());
        String newPwd = new String(newPwdField.getPassword());
        String confirmPwd = new String(confirmPwdField.getPassword());

        if (!newPwd.equals(confirmPwd)) {
            JOptionPane.showMessageDialog(this, "两次密码不一致",
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ServiceResult result = userService.changePassword(
            currentUser.getUserId(), oldPwd, newPwd);

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                "成功", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                "修改失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshUserTable() {
        tableModel.setRowCount(0);
        List<User> users = userService.listAllUsers();
        for (User u : users) {
            String status = getStatusText(u);
            tableModel.addRow(new Object[]{
                u.getUsername(),
                u.getRole().getDisplayName(),
                status,
                format(u.getLastLogin())
            });
        }
    }

    private String getStatusText(User u) {
        if (u.getLockedUntil() == null
                || u.getLockedUntil().isBefore(LocalDateTime.now())) {
            return "正常";
        }
        if (u.getLockedUntil().getYear() >= 2099) {
            return "已禁用";
        }
        return "临时锁定";
    }

    private int getSelectedUserId() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个用户",
                "提示", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        String username = (String) tableModel.getValueAt(row, 0);
        if (username.equals(currentUser.getUsername())) {
            JOptionPane.showMessageDialog(this, "不能操作自己的账号",
                "提示", JOptionPane.WARNING_MESSAGE);
            return -1;
        }
        List<User> users = userService.listAllUsers();
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u.getUserId();
            }
        }
        return -1;
    }

    private void onEnableUser() {
        int userId = getSelectedUserId();
        if (userId < 0) return;
        ServiceResult result = userService.setUserEnabled(userId, true);
        JOptionPane.showMessageDialog(this, result.getMessage());
        refreshUserTable();
    }

    private void onDisableUser() {
        int userId = getSelectedUserId();
        if (userId < 0) return;
        ServiceResult result = userService.setUserEnabled(userId, false);
        JOptionPane.showMessageDialog(this, result.getMessage());
        refreshUserTable();
    }

    private void onResetPassword() {
        int userId = getSelectedUserId();
        if (userId < 0) return;
        String newPwd = JOptionPane.showInputDialog(this, "请输入新密码:");
        if (newPwd == null || newPwd.trim().isEmpty()) return;
        ServiceResult result = userService.resetPassword(userId, newPwd.trim());
        JOptionPane.showMessageDialog(this, result.getMessage());
        refreshUserTable();
    }

    private String format(LocalDateTime dt) {
        return dt != null ? dt.format(FORMATTER) : "—";
    }
}
