package com.astrolog.ui.dialog;

import com.astrolog.service.ServiceResult;
import com.astrolog.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class RegisterDialog extends JDialog {

    private final UserService userService = new UserService();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JComboBox<String> roleCombo;
    private JTextField cityField;
    private JTextField latField;
    private JTextField lonField;

    public RegisterDialog(JFrame parent) {
        super(parent, "用户注册", true);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("用户注册");
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        mainPanel.add(new JLabel("用户名:"), gbc);
        usernameField = new JTextField(15);
        gbc.gridx = 1;
        mainPanel.add(usernameField, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        mainPanel.add(new JLabel("密码:"), gbc);
        passwordField = new JPasswordField(15);
        gbc.gridx = 1;
        mainPanel.add(passwordField, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        mainPanel.add(new JLabel("确认密码:"), gbc);
        confirmPasswordField = new JPasswordField(15);
        gbc.gridx = 1;
        mainPanel.add(confirmPasswordField, gbc);

        gbc.gridy = 4; gbc.gridx = 0;
        mainPanel.add(new JLabel("角色:"), gbc);
        roleCombo = new JComboBox<>(new String[]{"观测者", "管理员"});
        gbc.gridx = 1;
        mainPanel.add(roleCombo, gbc);

        gbc.gridy = 5; gbc.gridx = 0;
        mainPanel.add(new JLabel("所在城市:"), gbc);
        cityField = new JTextField(15);
        gbc.gridx = 1;
        mainPanel.add(cityField, gbc);

        gbc.gridy = 6; gbc.gridx = 0;
        mainPanel.add(new JLabel("默认纬度:"), gbc);
        latField = new JTextField(15);
        gbc.gridx = 1;
        mainPanel.add(latField, gbc);

        gbc.gridy = 7; gbc.gridx = 0;
        mainPanel.add(new JLabel("默认经度:"), gbc);
        lonField = new JTextField(15);
        gbc.gridx = 1;
        mainPanel.add(lonField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton registerButton = new JButton("注册");
        registerButton.addActionListener(e -> onRegister());
        buttonPanel.add(registerButton);

        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);

        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        mainPanel.add(buttonPanel, gbc);

        gbc.gridy = 9;
        JLabel hintLabel = new JLabel("提示: 城市和坐标可选填");
        hintLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        hintLabel.setForeground(Color.GRAY);
        hintLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(hintLabel, gbc);

        setContentPane(mainPanel);
    }

    private void onRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名和密码为必填项",
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "两次密码不一致",
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String roleDisplay = (String) roleCombo.getSelectedItem();
        String role = roleDisplay.equals("管理员") ? "admin" : "observer";

        String city = cityField.getText().trim();

        BigDecimal lat = null;
        BigDecimal lon = null;
        String latText = latField.getText().trim();
        String lonText = lonField.getText().trim();

        if (!latText.isEmpty()) {
            try {
                lat = new BigDecimal(latText);
                if (lat.compareTo(new BigDecimal("-90")) < 0 || lat.compareTo(new BigDecimal("90")) > 0) {
                    JOptionPane.showMessageDialog(this, "纬度必须在 -90 到 90 之间",
                        "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "纬度格式不正确",
                    "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        if (!lonText.isEmpty()) {
            try {
                lon = new BigDecimal(lonText);
                if (lon.compareTo(new BigDecimal("-180")) < 0 || lon.compareTo(new BigDecimal("180")) > 0) {
                    JOptionPane.showMessageDialog(this, "经度必须在 -180 到 180 之间",
                        "提示", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "经度格式不正确",
                    "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        ServiceResult result = userService.register(username, password, role, city, lat, lon);

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this, "注册成功，请登录",
                "注册成功", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                "注册失败", JOptionPane.ERROR_MESSAGE);
        }
    }
}
