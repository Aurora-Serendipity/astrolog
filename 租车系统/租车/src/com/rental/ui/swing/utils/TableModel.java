package com.rental.ui.swing.utils;

import com.rental.model.Vehicle;
import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * 车辆表格数据模型类
 * 继承AbstractTableModel，用于在JTable中显示车辆列表数据
 * 提供车辆数据的读取和更新功能
 *
 * @author 系统
 * @version 1.0
 */
public class TableModel extends AbstractTableModel {
    /** 车辆数据列表 */
    private List<Vehicle> vehicles;

    /** 表格列名数组 */
    private String[] columnNames = {"ID", "车型", "具体车型", "日租金", "状态"};

    /**
     * 构造函数
     *
     * @param vehicles 车辆列表数据
     */
    public TableModel(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }

    /**
     * 获取行数
     *
     * @return 车辆数量
     */
    @Override
    public int getRowCount() {
        return vehicles.size();
    }

    /**
     * 获取列数
     *
     * @return 列数量
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * 获取列名称
     *
     * @param column 列索引
     * @return 列名称
     */
    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * 获取指定单元格的值
     *
     * @param rowIndex 行索引
     * @param columnIndex 列索引
     * @return 单元格内容
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        // 获取对应行的车辆对象
        Vehicle vehicle = vehicles.get(rowIndex);
        // 根据列索引返回对应的属性值
        switch (columnIndex) {
            case 0:
                // 返回车辆ID
                return vehicle.getId();
            case 1:
                // 返回车辆类型
                return vehicle.getType();
            case 2:
                // 返回具体车型
                return vehicle.getModel();
            case 3:
                // 返回日租金
                return vehicle.getDailyRent();
            case 4:
                // 返回车辆状态
                return vehicle.getStatus();
            default:
                return null;
        }
    }

    /**
     * 获取指定行的车辆对象
     *
     * @param rowIndex 行索引
     * @return 车辆对象
     */
    public Vehicle getVehicleAt(int rowIndex) {
        return vehicles.get(rowIndex);
    }

    /**
     * 更新表格数据
     * 用于刷新表格显示最新的车辆列表
     *
     * @param newVehicles 新的车辆列表
     */
    public void updateData(List<Vehicle> newVehicles) {
        this.vehicles = newVehicles;
        // 通知表格数据已变更，触发界面刷新
        fireTableDataChanged();
    }
}