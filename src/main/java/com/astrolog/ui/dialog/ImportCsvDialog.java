package com.astrolog.ui.dialog;

import com.astrolog.service.BodyService;
import com.astrolog.service.ImportResult;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ImportCsvDialog extends JDialog {

    private static final String[] PREVIEW_COLUMNS = {
        "行号", "名称", "类型", "星座", "赤经", "赤纬", "视星等"
    };

    private final BodyService bodyService;
    private final int operatorUserId;
    private final Runnable onSuccess;

    private JTextField filePathField;
    private DefaultTableModel previewModel;
    private JTextArea resultArea;
    private JLabel statsLabel;
    private JButton importBtn;
    private String csvContent;

    public ImportCsvDialog(JFrame parent, BodyService bodyService,
                           int operatorUserId, Runnable onSuccess) {
        super(parent, "批量导入星体数据", true);
        this.bodyService = bodyService;
        this.operatorUserId = operatorUserId;
        this.onSuccess = onSuccess;
        initComponents();
        setSize(700, 550);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // 顶部：文件选择
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filePanel.add(new JLabel("文件:"));
        filePathField = new JTextField(35);
        filePathField.setEditable(false);
        filePanel.add(filePathField);

        JButton browseBtn = new JButton("浏览...");
        browseBtn.addActionListener(e -> browseFile());
        filePanel.add(browseBtn);
        add(filePanel, BorderLayout.NORTH);

        // 中间：预览表格
        previewModel = new DefaultTableModel(PREVIEW_COLUMNS, 0);
        JTable previewTable = new JTable(previewModel);
        previewTable.setRowHeight(22);
        previewTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
        previewTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        previewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane previewScroll = new JScrollPane(previewTable);
        previewScroll.setBorder(BorderFactory.createTitledBorder("预览"));
        add(previewScroll, BorderLayout.CENTER);

        // 底部：结果 + 按钮
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(BorderFactory.createTitledBorder("导入结果"));
        statsLabel = new JLabel("就绪");
        statsLabel.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
        resultPanel.add(statsLabel, BorderLayout.NORTH);

        resultArea = new JTextArea(5, 50);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultPanel.add(resultScroll, BorderLayout.CENTER);

        bottomPanel.add(resultPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        importBtn = new JButton("开始导入");
        importBtn.setEnabled(false);
        importBtn.addActionListener(e -> doImport());
        btnPanel.add(importBtn);

        JButton closeBtn = new JButton("关闭");
        closeBtn.addActionListener(e -> dispose());
        btnPanel.add(closeBtn);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void browseFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "CSV 文件 (*.csv)", "csv"));
        chooser.setDialogTitle("选择 CSV 文件");
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            filePathField.setText(file.getAbsolutePath());
            try {
                csvContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                updatePreview();
                importBtn.setEnabled(true);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "读取文件失败: " + ex.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updatePreview() {
        previewModel.setRowCount(0);
        resultArea.setText("");
        statsLabel.setText("就绪");

        String[] lines = csvContent.split("\\R");
        for (int i = 1; i < Math.min(lines.length, 50); i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] cols = line.split(",", -1);
            previewModel.addRow(new Object[]{
                i,
                cols.length > 0 ? cols[0].trim() : "",
                cols.length > 1 ? cols[1].trim() : "",
                cols.length > 2 ? cols[2].trim() : "",
                cols.length > 3 ? cols[3].trim() : "",
                cols.length > 4 ? cols[4].trim() : "",
                cols.length > 5 ? cols[5].trim() : ""
            });
        }
    }

    private void doImport() {
        if (csvContent == null || csvContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请先选择 CSV 文件", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        importBtn.setEnabled(false);
        ImportResult result = bodyService.importCsv(csvContent, operatorUserId);

        statsLabel.setText("成功: " + result.getSuccessCount() + " 条  |  失败: "
            + result.getErrorCount() + " 条");

        StringBuilder sb = new StringBuilder();
        for (String err : result.getErrors()) {
            sb.append(err).append("\n");
        }
        resultArea.setText(sb.toString());

        if (!result.hasErrors() && result.getSuccessCount() > 0) {
            JOptionPane.showMessageDialog(this,
                "导入完成！成功导入 " + result.getSuccessCount() + " 条星体数据。",
                "导入成功", JOptionPane.INFORMATION_MESSAGE);
            if (onSuccess != null) onSuccess.run();
        }
    }
}
