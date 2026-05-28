package com.astrolog.ui.frame;

import com.astrolog.config.AppConfig;
import com.astrolog.service.LoginResult;
import com.astrolog.service.UserService;
import com.astrolog.ui.dialog.RegisterDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class LoginFrame extends JFrame {

    private static final ResourceBundle i18n =
        ResourceBundle.getBundle("i18n.messages_zh");

    private final List<Point> stars = new ArrayList<>();
    private final Random random = new Random();
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;

    public LoginFrame() {
        initStars();
        initComponents();
        setTitle(AppConfig.APP_NAME + " v" + AppConfig.APP_VERSION + " - " + i18n.getString("login.title"));
        setSize(800, 600);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void initStars() {
        for (int i = 0; i < 200; i++) {
            stars.add(new Point(random.nextInt(800), random.nextInt(600)));
        }
    }

    private void initComponents() {
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(10, 10, 40));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(Color.WHITE);
                for (Point star : stars) {
                    int size = random.nextInt(2) + 1;
                    g.fillOval(star.x, star.y, size, size);
                }
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());

        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(new Color(20, 20, 60, 200));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        loginPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(AppConfig.APP_NAME);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 28));
        titleLabel.setForeground(new Color(180, 150, 80));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        loginPanel.add(titleLabel, gbc);

        JLabel subtitleLabel = new JLabel(i18n.getString("app.title"));
        subtitleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(200, 210, 240));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        loginPanel.add(subtitleLabel, gbc);

        gbc.gridy = 2;
        loginPanel.add(Box.createVerticalStrut(20), gbc);

        gbc.gridwidth = 1;
        JLabel userLabel = new JLabel(i18n.getString("login.username") + ":");
        userLabel.setForeground(new Color(200, 210, 240));
        userLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 3;
        loginPanel.add(userLabel, gbc);

        usernameField = new JTextField(15);
        usernameField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        gbc.gridx = 1;
        loginPanel.add(usernameField, gbc);

        JLabel passLabel = new JLabel(i18n.getString("login.password") + ":");
        passLabel.setForeground(new Color(200, 210, 240));
        passLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        gbc.gridx = 0; gbc.gridy = 4;
        loginPanel.add(passLabel, gbc);

        passwordField = new JPasswordField(15);
        passwordField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);

        gbc.gridy = 5;
        loginPanel.add(Box.createVerticalStrut(15), gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);

        loginButton = new JButton(i18n.getString("login.button"));
        loginButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        loginButton.setPreferredSize(new Dimension(100, 35));
        loginButton.addActionListener(this::onLogin);
        buttonPanel.add(loginButton);

        registerButton = new JButton(i18n.getString("login.register"));
        registerButton.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        registerButton.setPreferredSize(new Dimension(100, 35));
        registerButton.addActionListener(this::onRegister);
        buttonPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        loginPanel.add(buttonPanel, gbc);

        backgroundPanel.add(loginPanel);
        setContentPane(backgroundPanel);
    }

    private void onLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, i18n.getString("login.emptyFields"),
                i18n.getString("login.title"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        UserService userService = new UserService();
        LoginResult result = userService.login(username, password);

        if (result.isSuccess()) {
            MainFrame mainFrame = new MainFrame(result.getUser());
            mainFrame.setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage(),
                i18n.getString("login.title"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onRegister(ActionEvent e) {
        RegisterDialog dialog = new RegisterDialog(this);
        dialog.setVisible(true);
    }

    public JTextField getUsernameField() { return usernameField; }
    public JPasswordField getPasswordField() { return passwordField; }
    public JButton getLoginButton() { return loginButton; }
    public JButton getRegisterButton() { return registerButton; }
}
