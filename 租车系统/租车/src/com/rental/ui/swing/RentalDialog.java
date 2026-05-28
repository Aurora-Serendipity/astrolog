package com.rental.ui.swing;

import com.rental.model.Vehicle;
import com.rental.service.RentalCalculator;
import com.rental.service.VehicleManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 租车对话框类
 * 弹窗让用户输入租赁天数并计算费用
 * 输入天数后会打开费用详情对话框
 *
 * @author 系统
 * @version 1.0
 */
public class RentalDialog extends JDialog {
    /** 选中的车辆对象 */
    private Vehicle vehicle;

    /** 租赁天数输入框 */
    private JTextField daysField;

    /** 费用结果显示标签 */
    private JLabel resultLabel;

    /** 车辆管理器，用于费用计算 */
    private VehicleManager vehicleManager;

    /**
     * 构造函数
     *
     * @param parent 父窗口
     * @param vehicle 要租赁的车辆
     * @param vehicleManager 车辆管理器
     */
    public RentalDialog(JFrame parent, Vehicle vehicle, VehicleManager vehicleManager) {
        // 调用父构造函数，设置模态对话框
        super(parent, "租车", true);
        this.vehicle = vehicle;
        this.vehicleManager = vehicleManager;

        // 设置对话框大小
        setSize(400, 300);
        // 相对父窗口居中
        setLocationRelativeTo(parent);

        // 初始化界面组件
        initComponents();

        // 设置对话框可见
        setVisible(true);
    }

    /**
     * 初始化界面组件
     * 创建信息显示、输入框和按钮
     */
    private void initComponents() {
        // 创建主面板，使用GridLayout布局
        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 10, 10));
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

        // 租赁天数输入
        mainPanel.add(new JLabel("租赁天数:"));
        daysField = new JTextField();
        mainPanel.add(daysField);

        // 创建结果面板
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultLabel = new JLabel("请输入租赁天数并点击计算");
        resultPanel.add(resultLabel, BorderLayout.CENTER);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        JButton calculateButton = new JButton("计算费用");
        JButton cancelButton = new JButton("取消");

        // 添加按钮到面板
        buttonPanel.add(calculateButton);
        buttonPanel.add(cancelButton);

        // 添加组件到对话框
        add(mainPanel, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.PAGE_END);

        // ===== 计算费用按钮事件 =====
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // 解析用户输入的天数
                    int days = Integer.parseInt(daysField.getText());
                    // 验证天数必须大于0
                    if (days <= 0) {
                        JOptionPane.showMessageDialog(RentalDialog.this, "租赁天数必须大于0");
                        return;
                    }

                    // 计算租赁费用
                    int fee = RentalCalculator.calculateRentalFee(
                            vehicle.getType(),
                            vehicle.getDailyRent(),
                            days
                    );

                    // 打开费用详情对话框
                    new FeeDetailDialog(RentalDialog.this, vehicle, days, fee, vehicleManager);
                } catch (NumberFormatException ex) {
                    // 输入格式错误提示
                    JOptionPane.showMessageDialog(RentalDialog.this, "请输入有效的天数");
                }
            }
        });

        // ===== 取消按钮事件 =====
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 关闭对话框
                dispose();
            }
        });
    }
}