package com.astrolog.ui.dialog;

import com.astrolog.service.BackupService;
import com.astrolog.service.ServiceResult;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupRestoreDialog extends JDialog {

    private final BackupService backupService;
    private final JTextArea logArea;
    private final JTextField backupDirField;
    private final JTextField restoreFileField;

    public BackupRestoreDialog(JFrame parent) {
        super(parent, "数据备份与恢复", true);
        this.backupService = new BackupService();

        setLayout(new BorderLayout());
        setSize(750, 620);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.weightx = 1;

        // ── 备份区域 ──
        JPanel backupPanel = new JPanel(new GridBagLayout());
        backupPanel.setBorder(BorderFactory.createTitledBorder("备份 — 将全部数据导出为 SQL 文件"));
        GridBagConstraints bg = new GridBagConstraints();
        bg.fill = GridBagConstraints.HORIZONTAL;
        bg.insets = new Insets(4, 6, 4, 6);

        bg.gridx = 0; bg.gridy = 0; bg.weightx = 1;
        backupDirField = new JTextField("backups");
        backupDirField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        backupPanel.add(backupDirField, bg);

        bg.gridx = 1; bg.weightx = 0;
        JButton browseDirBtn = new JButton("浏览");
        browseDirBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        browseDirBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setDialogTitle("选择备份保存目录");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                backupDirField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        backupPanel.add(browseDirBtn, bg);

        bg.gridx = 0; bg.gridy = 1; bg.gridwidth = 2;
        JButton startBackupBtn = new JButton("开始备份");
        startBackupBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        startBackupBtn.addActionListener(e -> executeBackup());
        backupPanel.add(startBackupBtn, bg);

        // ── 恢复区域 ──
        JPanel restorePanel = new JPanel(new GridBagLayout());
        restorePanel.setBorder(BorderFactory.createTitledBorder("恢复 — 从 SQL 备份文件恢复数据（将覆盖当前数据！）"));
        GridBagConstraints rg = new GridBagConstraints();
        rg.fill = GridBagConstraints.HORIZONTAL;
        rg.insets = new Insets(4, 6, 4, 6);

        rg.gridx = 0; rg.gridy = 0; rg.weightx = 1;
        restoreFileField = new JTextField();
        restoreFileField.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        restorePanel.add(restoreFileField, rg);

        rg.gridx = 1; rg.weightx = 0;
        JButton browseFileBtn = new JButton("浏览");
        browseFileBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        browseFileBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("SQL 备份文件 (*.sql)", "sql"));
            chooser.setDialogTitle("选择备份文件");
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                restoreFileField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });
        restorePanel.add(browseFileBtn, rg);

        rg.gridx = 0; rg.gridy = 1; rg.gridwidth = 2;
        JButton startRestoreBtn = new JButton("开始恢复");
        startRestoreBtn.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        startRestoreBtn.addActionListener(e -> executeRestore());
        restorePanel.add(startRestoreBtn, rg);

        // ── 状态区域 ──
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("操作日志"));
        logArea = new JTextArea(12, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(650, 180));
        statusPanel.add(logScroll, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(backupPanel, gbc);
        gbc.gridy = 1;
        mainPanel.add(restorePanel, gbc);
        gbc.gridy = 2;
        mainPanel.add(statusPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeBtn = new JButton("关闭");
        closeBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void executeBackup() {
        String dir = backupDirField.getText().trim();
        if (dir.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择备份保存目录",
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        log("[" + now() + "] 开始备份到: " + dir);
        new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return backupService.backup(dir);
            }

            @Override
            protected void done() {
                try {
                    String path = get();
                    log("[" + now() + "] 备份完成: " + path);
                    JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                        "备份成功!\n" + path, "备份完成",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    log("[" + now() + "] 备份失败: " + ex.getMessage());
                    JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                        "备份失败: " + ex.getMessage(), "错误",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void executeRestore() {
        String path = restoreFileField.getText().trim();
        if (path.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请选择备份文件",
                "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "此操作将覆盖当前数据库全部数据，确定继续？",
            "严重警告", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        log("[" + now() + "] 开始恢复: " + path);
        new SwingWorker<ServiceResult, Void>() {
            @Override
            protected ServiceResult doInBackground() throws Exception {
                return backupService.restore(path);
            }

            @Override
            protected void done() {
                try {
                    ServiceResult result = get();
                    log("[" + now() + "] " + result.getMessage());
                    JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                        result.getMessage(),
                        result.isSuccess() ? "恢复完成" : "恢复失败",
                        result.isSuccess() ? JOptionPane.INFORMATION_MESSAGE
                                           : JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    log("[" + now() + "] 恢复失败: " + ex.getMessage());
                    JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                        "恢复失败: " + ex.getMessage(), "错误",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}
