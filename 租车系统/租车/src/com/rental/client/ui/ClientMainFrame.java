package com.rental.client.ui;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rental.client.SocketClient;
import com.rental.model.RentalRecord;
import com.rental.model.Vehicle;
import com.rental.protocol.ApiResponse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * 客户端主窗口类
 * 通过Socket与服务端通信，获取车辆数据和租赁信息
 *
 * @author 系统
 * @version 1.0
 */
public class ClientMainFrame extends JFrame {
    private static final String[] VEHICLE_COLUMNS = {"ID", "类型", "车型", "日租金", "状态"};
    private static final String[] RENTAL_COLUMNS = {"记录ID", "车辆ID", "车型", "客户", "租金/天", "天数", "总费用", "状态"};

    private SocketClient socketClient;
    private JTable vehicleTable;
    private JTable rentalTable;
    private DefaultTableModel vehicleTableModel;
    private DefaultTableModel rentalTableModel;
    private JLabel connectionStatusLabel;

    /**
     * 构造函数
     */
    public ClientMainFrame() {
        socketClient = new SocketClient();

        setTitle("租车系统 - 客户端");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        checkConnectionAndLoadData();
    }

    /**
     * 初始化组件
     */
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 顶部状态栏
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionStatusLabel = new JLabel("连接状态: 检测中...");
        JButton refreshButton = new JButton("刷新数据");
        refreshButton.addActionListener(e -> checkConnectionAndLoadData());
        topPanel.add(connectionStatusLabel);
        topPanel.add(refreshButton);

        // 标签页
        JTabbedPane tabbedPane = new JTabbedPane();

        // 车辆列表标签页
        vehicleTableModel = new DefaultTableModel(VEHICLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        vehicleTable = new JTable(vehicleTableModel);
        JScrollPane vehicleScrollPane = new JScrollPane(vehicleTable);

        JPanel vehicleButtonPanel = new JPanel();
        JButton rentButton = new JButton("租车");
        JButton viewDetailsButton = new JButton("查看详情");
        rentButton.addActionListener(e -> handleRentVehicle());
        viewDetailsButton.addActionListener(e -> handleViewDetails());
        vehicleButtonPanel.add(rentButton);
        vehicleButtonPanel.add(viewDetailsButton);

        JPanel vehiclePanel = new JPanel(new BorderLayout());
        vehiclePanel.add(vehicleScrollPane, BorderLayout.CENTER);
        vehiclePanel.add(vehicleButtonPanel, BorderLayout.SOUTH);

        // 租赁记录标签页
        rentalTableModel = new DefaultTableModel(RENTAL_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        rentalTable = new JTable(rentalTableModel);
        JScrollPane rentalScrollPane = new JScrollPane(rentalTable);

        JPanel rentalButtonPanel = new JPanel();
        JButton returnButton = new JButton("还车");
        JButton refreshRentalButton = new JButton("刷新记录");
        returnButton.addActionListener(e -> handleReturnVehicle());
        refreshRentalButton.addActionListener(e -> loadRentalData());
        rentalButtonPanel.add(returnButton);
        rentalButtonPanel.add(refreshRentalButton);

        JPanel rentalPanel = new JPanel(new BorderLayout());
        rentalPanel.add(rentalScrollPane, BorderLayout.CENTER);
        rentalPanel.add(rentalButtonPanel, BorderLayout.SOUTH);

        // 添加标签页
        tabbedPane.addTab("车辆列表", vehiclePanel);
        tabbedPane.addTab("租赁记录", rentalPanel);

        // 组装主面板
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    /**
     * 检查连接并加载数据
     */
    private void checkConnectionAndLoadData() {
        if (socketClient.connect()) {
            connectionStatusLabel.setText("连接状态: 已连接");
            connectionStatusLabel.setForeground(Color.GREEN);
            socketClient.disconnect();
            loadVehicleData();
            loadRentalData();
        } else {
            connectionStatusLabel.setText("连接状态: 未连接");
            connectionStatusLabel.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "无法连接到服务器，请确保服务器已启动", "连接失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载车辆数据
     */
    @SuppressWarnings("unchecked")
    private void loadVehicleData() {
        ApiResponse<List<Vehicle>> response = socketClient.queryVehicles();
        vehicleTableModel.setRowCount(0);

        if (response.isSuccess() && response.getData() != null) {
            Gson gson = new Gson();
            List<Vehicle> vehicles = gson.fromJson(gson.toJson(response.getData()),
                    new TypeToken<List<Vehicle>>(){}.getType());
            for (Vehicle v : vehicles) {
                vehicleTableModel.addRow(new Object[]{v.getId(), v.getType(), v.getModel(), v.getDailyRent(), v.getStatus()});
            }
        } else {
            JOptionPane.showMessageDialog(this, "加载车辆数据失败: " + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载租赁记录数据
     */
    @SuppressWarnings("unchecked")
    private void loadRentalData() {
        ApiResponse<List<RentalRecord>> response = socketClient.queryRentals();
        rentalTableModel.setRowCount(0);

        if (response.isSuccess() && response.getData() != null) {
            Gson gson = new Gson();
            List<RentalRecord> rentals = gson.fromJson(gson.toJson(response.getData()),
                    new TypeToken<List<RentalRecord>>(){}.getType());
            for (RentalRecord r : rentals) {
                rentalTableModel.addRow(new Object[]{r.getId(), r.getVehicleId(), r.getVehicleModel(),
                        r.getCustomerName(), r.getDailyRent(), r.getDays(), r.getTotalFee(), r.getStatus()});
            }
        }
    }

    /**
     * 处理租车
     */
    private void handleRentVehicle() {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一辆车辆");
            return;
        }

        String vehicleId = (String) vehicleTableModel.getValueAt(selectedRow, 0);
        String status = (String) vehicleTableModel.getValueAt(selectedRow, 4);

        if (!"可租赁".equals(status)) {
            JOptionPane.showMessageDialog(this, "该车辆不可租赁");
            return;
        }

        String customerName = JOptionPane.showInputDialog(this, "请输入客户姓名:", "租车信息", JOptionPane.QUESTION_MESSAGE);
        if (customerName == null || customerName.trim().isEmpty()) {
            return;
        }

        String daysStr = JOptionPane.showInputDialog(this, "请输入租赁天数:", "租车信息", JOptionPane.QUESTION_MESSAGE);
        if (daysStr == null || daysStr.trim().isEmpty()) {
            return;
        }

        try {
            int days = Integer.parseInt(daysStr);
            if (days <= 0) {
                JOptionPane.showMessageDialog(this, "天数必须大于0");
                return;
            }

            ApiResponse<RentalRecord> response = socketClient.rentVehicle(vehicleId, customerName.trim(), days);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "租车成功！总费用: " + response.getData().getTotalFee());
                loadVehicleData();
                loadRentalData();
            } else {
                JOptionPane.showMessageDialog(this, "租车失败: " + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "天数输入无效");
        }
    }

    /**
     * 处理还车
     */
    private void handleReturnVehicle() {
        int selectedRow = rentalTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一条租赁记录");
            return;
        }

        String status = (String) rentalTableModel.getValueAt(selectedRow, 7);
        if (!"租用中".equals(status)) {
            JOptionPane.showMessageDialog(this, "该车辆未在租赁中");
            return;
        }

        String vehicleId = (String) rentalTableModel.getValueAt(selectedRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this, "确认归还车辆 " + vehicleId + " 吗？", "还车确认", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            ApiResponse<Object> response = socketClient.returnVehicle(vehicleId);
            if (response.isSuccess()) {
                JOptionPane.showMessageDialog(this, "还车成功！");
                loadVehicleData();
                loadRentalData();
            } else {
                JOptionPane.showMessageDialog(this, "还车失败: " + response.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 处理查看车辆详情
     */
    private void handleViewDetails() {
        int selectedRow = vehicleTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一辆车辆");
            return;
        }

        String vehicleId = (String) vehicleTableModel.getValueAt(selectedRow, 0);
        String vehicleModel = (String) vehicleTableModel.getValueAt(selectedRow, 2);
        double dailyRent = (double) vehicleTableModel.getValueAt(selectedRow, 3);
        String status = (String) vehicleTableModel.getValueAt(selectedRow, 4);

        String info = "车辆ID: " + vehicleId + "\n" +
                      "车型: " + vehicleModel + "\n" +
                      "日租金: ¥" + dailyRent + "\n" +
                      "状态: " + status;

        JOptionPane.showMessageDialog(this, info, "车辆详情", JOptionPane.INFORMATION_MESSAGE);
    }
}