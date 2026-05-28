package com.rental.ui.swing;

import com.rental.model.Vehicle;
import com.rental.service.VehicleManager;
import com.rental.ui.swing.utils.TableModel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 主窗口类
 * 租车管理系统的图形界面主窗口
 * 显示车辆列表，并提供租车、还车、刷新等功能按钮
 *
 * @author 系统
 * @version 1.0
 */
public class MainFrame extends JFrame {
    /** 车辆管理器，用于处理车辆业务逻辑 */
    private VehicleManager vehicleManager;

    /** 车辆列表表格组件 */
    private JTable vehicleTable;

    /** 表格数据模型 */
    private TableModel tableModel;

    /**
     * 构造函数
     * 初始化主窗口、车辆管理器并加载车辆数据
     */
    public MainFrame() {
        // 创建车辆管理器实例
        vehicleManager = new VehicleManager();
        // 初始化车辆数据（从文件加载或创建默认数据）
        vehicleManager.initializeVehicles();

        // 设置窗口标题
        setTitle("租车管理系统");
        // 设置窗口大小
        setSize(800, 600);
        // 设置关闭按钮行为
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 窗口居中显示
        setLocationRelativeTo(null);

        // 初始化界面组件
        initComponents();
    }

    /**
     * 初始化界面组件
     * 创建表格、按钮面板并添加事件监听器
     */
    private void initComponents() {
        // 创建主面板，使用BorderLayout布局
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建表格，显示所有车辆
        tableModel = new TableModel(vehicleManager.getAllVehicles());
        vehicleTable = new JTable(tableModel);
        // 将表格放入滚动面板
        JScrollPane scrollPane = new JScrollPane(vehicleTable);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        // 创建功能按钮
        JButton rentalButton = new JButton("租车");
        JButton returnButton = new JButton("还车");
        JButton refreshButton = new JButton("刷新列表");

        // 添加按钮到面板
        buttonPanel.add(rentalButton);
        buttonPanel.add(returnButton);
        buttonPanel.add(refreshButton);

        // 将组件添加到主面板
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 将主面板添加到窗口
        add(mainPanel);

        // ===== 租车按钮事件处理 =====
        rentalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取用户选中的行
                int selectedRow = vehicleTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // 获取选中的车辆
                    Vehicle selectedVehicle = tableModel.getVehicleAt(selectedRow);
                    // 检查车辆是否可租赁
                    if ("可租赁".equals(selectedVehicle.getStatus())) {
                        // 打开租车对话框
                        new RentalDialog(MainFrame.this, selectedVehicle, vehicleManager);
                        // 刷新列表显示最新状态
                        tableModel.updateData(vehicleManager.getAllVehicles());
                    } else {
                        // 提示车辆已被租赁
                        JOptionPane.showMessageDialog(MainFrame.this, "该车辆已被租赁，无法再次租车");
                    }
                } else {
                    // 提示请先选择车辆
                    JOptionPane.showMessageDialog(MainFrame.this, "请先选择一辆车辆");
                }
            }
        });

        // ===== 还车按钮事件处理 =====
        returnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 获取用户选中的行
                int selectedRow = vehicleTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // 获取选中的车辆
                    Vehicle selectedVehicle = tableModel.getVehicleAt(selectedRow);
                    // 检查车辆是否已租赁
                    if ("已租赁".equals(selectedVehicle.getStatus())) {
                        // 弹出确认对话框
                        int confirm = JOptionPane.showConfirmDialog(MainFrame.this,
                                "确认归还车辆 " + selectedVehicle.getId() + " (" + selectedVehicle.getModel() + ") 吗？",
                                "还车确认", JOptionPane.YES_NO_OPTION);
                        // 用户确认还车
                        if (confirm == JOptionPane.YES_OPTION) {
                            // 更新车辆状态为可租赁
                            vehicleManager.updateVehicleStatus(selectedVehicle.getId(), "可租赁");
                            // 刷新表格显示最新状态
                            tableModel.updateData(vehicleManager.getAllVehicles());
                            // 提示还车成功
                            JOptionPane.showMessageDialog(MainFrame.this, "还车成功！");
                        }
                    } else {
                        // 提示车辆未租赁
                        JOptionPane.showMessageDialog(MainFrame.this, "该车辆未被租赁，无需还车");
                    }
                } else {
                    // 提示请先选择车辆
                    JOptionPane.showMessageDialog(MainFrame.this, "请先选择一辆车辆");
                }
            }
        });

        // ===== 刷新按钮事件处理 =====
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 刷新表格数据
                tableModel.updateData(vehicleManager.getAllVehicles());
                // 提示刷新成功
                JOptionPane.showMessageDialog(MainFrame.this, "列表已刷新");
            }
        });
    }

    /**
     * 获取车辆管理器实例
     *
     * @return 车辆管理器
     */
    public VehicleManager getVehicleManager() {
        return vehicleManager;
    }
}