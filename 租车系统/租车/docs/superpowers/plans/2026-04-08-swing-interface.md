# Swing界面实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为租车系统实现基于Swing的多窗口界面，包含车辆列表、租车流程和费用计算功能。

**架构：** 采用多窗口设计，主窗口显示车辆列表，租车和费用详情使用对话框。复用现有的业务逻辑类，实现MVC模式。

**技术栈：** Java Swing、现有租车系统业务逻辑、MVC模式。

---

## 文件结构

```
com.rental.ui.swing/
├── MainFrame.java          # 主窗口（车辆列表）
├── RentalDialog.java       # 租车对话框
├── FeeDetailDialog.java    # 费用详情对话框
├── SwingUI.java            # 启动类
└── utils/
    └── TableModel.java     # 表格模型工具类
```

## 任务1：创建Swing包结构

**文件：**
- 创建：`src/com/rental/ui/swing/`
- 创建：`src/com/rental/ui/swing/utils/`

- [ ] **步骤1：创建目录结构**

```bash
mkdir -p src/com/rental/ui/swing/utils
```

- [ ] **步骤2：验证目录创建**

```bash
ls -la src/com/rental/ui/
```

预期：显示swing目录

- [ ] **步骤3：Commit**

```bash
git add src/com/rental/ui/swing/
git commit -m "feat: create swing directory structure"
```

## 任务2：创建表格模型工具类

**文件：**
- 创建：`src/com/rental/ui/swing/utils/TableModel.java`

- [ ] **步骤1：编写TableModel类**

```java
package com.rental.ui.swing.utils;

import com.rental.model.Vehicle;
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class TableModel extends AbstractTableModel {
    private List<Vehicle> vehicles;
    private String[] columnNames = {"ID", "车型", "具体车型", "日租金", "状态"};
    
    public TableModel(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }
    
    @Override
    public int getRowCount() {
        return vehicles.size();
    }
    
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Vehicle vehicle = vehicles.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return vehicle.getId();
            case 1:
                return vehicle.getType();
            case 2:
                return vehicle.getModel();
            case 3:
                return vehicle.getDailyRent();
            case 4:
                return vehicle.getStatus();
            default:
                return null;
        }
    }
    
    public Vehicle getVehicleAt(int rowIndex) {
        return vehicles.get(rowIndex);
    }
    
    public void updateData(List<Vehicle> newVehicles) {
        this.vehicles = newVehicles;
        fireTableDataChanged();
    }
}
```

- [ ] **步骤2：Commit**

```bash
git add src/com/rental/ui/swing/utils/TableModel.java
git commit -m "feat: create TableModel utility class"
```

## 任务3：创建主窗口（车辆列表）

**文件：**
- 创建：`src/com/rental/ui/swing/MainFrame.java`

- [ ] **步骤1：编写MainFrame类**

```java
package com.rental.ui.swing;

import com.rental.model.Vehicle;
import com.rental.service.VehicleManager;
import com.rental.ui.swing.utils.TableModel;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {
    private VehicleManager vehicleManager;
    private JTable vehicleTable;
    private TableModel tableModel;
    
    public MainFrame() {
        vehicleManager = new VehicleManager();
        vehicleManager.initializeVehicles();
        
        setTitle("租车管理系统");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initComponents();
    }
    
    private void initComponents() {
        // 创建主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // 创建表格
        tableModel = new TableModel(vehicleManager.getAvailableVehicles());
        vehicleTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        
        // 创建按钮面板
        JPanel buttonPanel = new JPanel();
        JButton rentalButton = new JButton("租车");
        JButton refreshButton = new JButton("刷新列表");
        
        buttonPanel.add(rentalButton);
        buttonPanel.add(refreshButton);
        
        // 添加组件到主面板
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 添加主面板到窗口
        add(mainPanel);
        
        // 添加上车按钮事件
        rentalButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = vehicleTable.getSelectedRow();
                if (selectedRow >= 0) {
                    Vehicle selectedVehicle = tableModel.getVehicleAt(selectedRow);
                    new RentalDialog(MainFrame.this, selectedVehicle);
                } else {
                    JOptionPane.showMessageDialog(MainFrame.this, "请先选择一辆车辆");
                }
            }
        });
        
        // 添加刷新按钮事件
        refreshButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.updateData(vehicleManager.getAvailableVehicles());
                JOptionPane.showMessageDialog(MainFrame.this, "列表已刷新");
            }
        });
    }
    
    public VehicleManager getVehicleManager() {
        return vehicleManager;
    }
}
```

- [ ] **步骤2：Commit**

```bash
git add src/com/rental/ui/swing/MainFrame.java
git commit -m "feat: create MainFrame with vehicle list"
```

## 任务4：创建租车对话框

**文件：**
- 创建：`src/com/rental/ui/swing/RentalDialog.java`

- [ ] **步骤1：编写RentalDialog类**

```java
package com.rental.ui.swing;

import com.rental.model.Vehicle;
import com.rental.service.RentalCalculator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RentalDialog extends JDialog {
    private Vehicle vehicle;
    private JTextField daysField;
    private JLabel resultLabel;
    
    public RentalDialog(JFrame parent, Vehicle vehicle) {
        super(parent, "租车", true);
        this.vehicle = vehicle;
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 车辆信息
        mainPanel.add(new JLabel("车辆ID:"));
        mainPanel.add(new JLabel(vehicle.getId()));
        
        mainPanel.add(new JLabel("车型:"));
        mainPanel.add(new JLabel(vehicle.getType()));
        
        mainPanel.add(new JLabel("具体车型:"));
        mainPanel.add(new JLabel(vehicle.getModel()));
        
        mainPanel.add(new JLabel("日租金:"));
        mainPanel.add(new JLabel(String.format("¥%.2f", vehicle.getDailyRent())));
        
        // 租赁天数
        mainPanel.add(new JLabel("租赁天数:"));
        daysField = new JTextField();
        mainPanel.add(daysField);
        
        // 结果
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultLabel = new JLabel("请输入租赁天数并点击计算");
        resultPanel.add(resultLabel, BorderLayout.CENTER);
        
        // 按钮
        JPanel buttonPanel = new JPanel();
        JButton calculateButton = new JButton("计算费用");
        JButton cancelButton = new JButton("取消");
        
        buttonPanel.add(calculateButton);
        buttonPanel.add(cancelButton);
        
        // 添加组件
        add(mainPanel, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.PAGE_END);
        
        // 计算按钮事件
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int days = Integer.parseInt(daysField.getText());
                    if (days <= 0) {
                        JOptionPane.showMessageDialog(RentalDialog.this, "租赁天数必须大于0");
                        return;
                    }
                    
                    int fee = RentalCalculator.calculateRentalFee(
                            vehicle.getType(), 
                            vehicle.getDailyRent(), 
                            days
                    );
                    
                    new FeeDetailDialog(RentalDialog.this, vehicle, days, fee);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(RentalDialog.this, "请输入有效的天数");
                }
            }
        });
        
        // 取消按钮事件
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
}
```

- [ ] **步骤2：Commit**

```bash
git add src/com/rental/ui/swing/RentalDialog.java
git commit -m "feat: create RentalDialog for vehicle rental"
```

## 任务5：创建费用详情对话框

**文件：**
- 创建：`src/com/rental/ui/swing/FeeDetailDialog.java`

- [ ] **步骤1：编写FeeDetailDialog类**

```java
package com.rental.ui.swing;

import com.rental.model.Vehicle;
import com.rental.service.RentalCalculator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FeeDetailDialog extends JDialog {
    private Vehicle vehicle;
    private int days;
    private int totalFee;
    
    public FeeDetailDialog(JDialog parent, Vehicle vehicle, int days, int totalFee) {
        super(parent, "费用详情", true);
        this.vehicle = vehicle;
        this.days = days;
        this.totalFee = totalFee;
        
        setSize(400, 350);
        setLocationRelativeTo(parent);
        
        initComponents();
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // 车辆信息
        mainPanel.add(new JLabel("车辆ID:"));
        mainPanel.add(new JLabel(vehicle.getId()));
        
        mainPanel.add(new JLabel("车型:"));
        mainPanel.add(new JLabel(vehicle.getType()));
        
        mainPanel.add(new JLabel("具体车型:"));
        mainPanel.add(new JLabel(vehicle.getModel()));
        
        mainPanel.add(new JLabel("日租金:"));
        mainPanel.add(new JLabel(String.format("¥%.2f", vehicle.getDailyRent())));
        
        mainPanel.add(new JLabel("租赁天数:"));
        mainPanel.add(new JLabel(String.valueOf(days)));
        
        mainPanel.add(new JLabel("最终费用:"));
        JLabel feeLabel = new JLabel(String.format("¥%d", totalFee));
        feeLabel.setFont(new Font(feeLabel.getFont().getName(), Font.BOLD, 16));
        feeLabel.setForeground(Color.RED);
        mainPanel.add(feeLabel);
        
        // 按钮
        JPanel buttonPanel = new JPanel();
        JButton confirmButton = new JButton("确认");
        
        buttonPanel.add(confirmButton);
        
        // 添加组件
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.PAGE_END);
        
        // 确认按钮事件
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 这里可以添加更新车辆状态的逻辑
                // 例如：vehicle.setStatus("已租赁");
                
                JOptionPane.showMessageDialog(FeeDetailDialog.this, "租车成功！");
                dispose();
            }
        });
    }
}
```

- [ ] **步骤2：Commit**

```bash
git add src/com/rental/ui/swing/FeeDetailDialog.java
git commit -m "feat: create FeeDetailDialog for rental fee details"
```

## 任务6：创建启动类

**文件：**
- 创建：`src/com/rental/ui/swing/SwingUI.java`

- [ ] **步骤1：编写SwingUI类**

```java
package com.rental.ui.swing;

import javax.swing.*;

public class SwingUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    // 设置系统外观
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}
```

- [ ] **步骤2：Commit**

```bash
git add src/com/rental/ui/swing/SwingUI.java
git commit -m "feat: create SwingUI entry point"
```

## 任务7：编译和运行

**文件：**
- 无

- [ ] **步骤1：编译项目**

```bash
javac -d bin src/com/rental/model/Vehicle.java src/com/rental/strategy/*.java src/com/rental/service/*.java src/com/rental/ui/swing/utils/TableModel.java src/com/rental/ui/swing/MainFrame.java src/com/rental/ui/swing/RentalDialog.java src/com/rental/ui/swing/FeeDetailDialog.java src/com/rental/ui/swing/SwingUI.java
```

- [ ] **步骤2：运行Swing界面**

```bash
java -cp bin com.rental.ui.swing.SwingUI
```

- [ ] **步骤3：测试功能**

1. 验证车辆列表显示
2. 测试选择车辆并点击租车
3. 输入租赁天数并计算费用
4. 查看费用详情
5. 确认租车

- [ ] **步骤4：Commit**

```bash
git commit -m "feat: complete swing interface implementation"
```

---

## 执行交接

计划已完成并保存到 `docs/superpowers/plans/2026-04-08-swing-interface.md`。两种执行方式：

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点

选哪种方式？