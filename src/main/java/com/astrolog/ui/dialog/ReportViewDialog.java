package com.astrolog.ui.dialog;

import com.astrolog.model.User;
import com.astrolog.service.ExportService;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ReportViewDialog extends JDialog {

    private final ExportService exportService;
    private final User user;
    private final int year;
    private final String htmlContent;

    public ReportViewDialog(JFrame parent, String htmlContent, User user, int year) {
        super(parent, "AstroLog 年度观测报告 [" + year + "]", true);
        this.exportService = new ExportService();
        this.user = user;
        this.year = year;
        this.htmlContent = htmlContent;

        setLayout(new BorderLayout());
        setSize(900, 650);
        setLocationRelativeTo(parent);

        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        editorPane.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));

        try {
            editorPane.setText(htmlContent);
        } catch (Exception ex) {
            editorPane.setText("<p>HTML 渲染失败: " + ex.getMessage() + "</p>");
        }

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton pdfBtn = new JButton("导出 PDF");
        JButton htmlBtn = new JButton("导出 HTML");
        JButton closeBtn = new JButton("关闭");

        pdfBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        htmlBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        closeBtn.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));

        pdfBtn.addActionListener(e -> {
            pdfBtn.setEnabled(false);
            new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return exportService.generatePdfReport(user, year);
                }

                @Override
                protected void done() {
                    try {
                        String path = get();
                        JOptionPane.showMessageDialog(ReportViewDialog.this,
                            "PDF 报告已生成:\n" + path, "导出成功",
                            JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        String msg = ex.getCause() != null
                            ? ex.getCause().getMessage() : ex.getMessage();
                        JOptionPane.showMessageDialog(ReportViewDialog.this,
                            "PDF 导出失败: " + msg,
                            "导出错误", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        pdfBtn.setEnabled(true);
                    }
                }
            }.execute();
        });

        htmlBtn.addActionListener(e -> {
            try {
                Path dir = ExportService.getReportsDir();
                Files.createDirectories(dir);
                Path file = dir.resolve("annual_report_" + year + ".html");
                Files.writeString(file, htmlContent, StandardCharsets.UTF_8);
                JOptionPane.showMessageDialog(this,
                    "HTML 报告已保存:\n" + file.toAbsolutePath(),
                    "导出成功", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "HTML 导出失败: " + ex.getMessage(),
                    "导出错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(pdfBtn);
        buttonPanel.add(htmlBtn);
        buttonPanel.add(closeBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        setVisible(true);
    }
}
