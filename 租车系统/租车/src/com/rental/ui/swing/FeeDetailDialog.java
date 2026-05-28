package com.rental.ui.swing;

import com.rental.model.Vehicle;
import com.rental.service.RentalCalculator;
import com.rental.service.VehicleManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 费用详情对话框类
 * 显示租车的费用明细，用户确认后完成租车操作
 * 确认后会更新车辆状态为"已租赁"并持久化保存
 *
 * @author 系统
 * @version 1.0
 */
public class FeeDetailDialog extends JDialog {
    /** 车辆对象 */
    private Vehicle vehicle;

    /** 租赁天数 */
    private int days;

    /** 计算后的总费用 */
    private int totalFee;

    /** 车辆管理器，用于更新车辆状态 */
    private VehicleManager vehicleManager;

    /**
     * 构造函数
     *
     * @param parent 父窗口
     * @param vehicle 租赁的车辆
     * @param days 租赁天数
     * @param totalFee 总费用
     * @param vehicleManager 车辆管理器
     */
    public FeeDetailDialog(JDialog parent, Vehicle vehicle, int days, int totalFee, VehicleManager vehicleManager) {
        // 调用父构造函数，设置模态对话框
        super(parent, "费用详情", true);
        this.vehicle = vehicle;
        this.days = days;
        this.totalFee = totalFee;
        this.vehicleManager = vehicleManager;

        // 设置对话框大小
        setSize(400, 350);
        // 相对父窗口居中
        setLocationRelativeTo(parent);

        // 初始化界面组件
        initComponents();

        // 设置对话框可见
        setVisible(true);
    }

    /**
     * 初始化界面组件
     * 显示车辆信息、费用明细和确认按钮
     */
    private void initComponents() {
        // 创建主面板，使用GridLayout布局
        JPanel mainPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 显示车辆ID
        mainPanel.add(new JLabel("车辆ID:"));
        mainPanel.add(new JLabel(vehicle.getId()));

        // 显示车型
        mainPanel.add(new JLabel("车型:"));
        mainPanel.add(new JLabel(vehicle.getType()));

        // 显示具体车型
        mainPanel.add(new JLabel("具体车型:"));
        mainPanel.add(new JLabel(vehicle.getModel()));

        // 显示日租金
        mainPanel.add(new JLabel("日租金:"));
        mainPanel.add(new JLabel(String.format("¥%.2f", vehicle.getDailyRent())));

        // 显示租赁天数
        mainPanel.add(new JLabel("租赁天数:"));
        mainPanel.add(new JLabel(String.valueOf(days)));

        // 显示最终费用
        mainPanel.add(new JLabel("最终费用:"));
        JLabel feeLabel = new JLabel(String.format("¥%d", totalFee));
        // 设置费用标签样式：粗体、红色、大字号
        feeLabel.setFont(new Font(feeLabel.getFont().getName(), Font.BOLD, 16));
        feeLabel.setForeground(Color.RED);
        mainPanel.add(feeLabel);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确认租车");
        buttonPanel.add(confirmButton);

        // 添加组件到对话框
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.PAGE_END);

        // ===== 确认租车按钮事件 =====
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 更新车辆状态为已租赁
                vehicleManager.updateVehicleStatus(vehicle.getId(), "已租赁");
                // 显示成功提示
                JOptionPane.showMessageDialog(FeeDetailDialog.this, "租车成功！");
                // 关闭对话框
                dispose();
            }
        });
    }
}