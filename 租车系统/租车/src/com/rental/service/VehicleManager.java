package com.rental.service;

import com.rental.model.Vehicle;
import com.rental.repository.VehicleRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * 车辆管理服务类
 * 负责车辆的初始化、查询、状态更新等业务逻辑
 * 集成数据持久化功能，状态变更自动保存到文件
 *
 * @author 系统
 * @version 1.0
 */
public class VehicleManager {
    /** 车辆列表，存储所有车辆对象 */
    private List<Vehicle> vehicles;

    /** 车辆数据持久化操作类 */
    private VehicleRepository vehicleRepository;

    /**
     * 构造函数，初始化车辆管理器
     * 创建空的车辆列表和持久化操作实例
     */
    public VehicleManager() {
        this.vehicles = new ArrayList<>();
        this.vehicleRepository = new VehicleRepository();
    }

    /**
     * 初始化车辆数据
     * 优先从文件加载数据，如果文件不存在或加载失败，则创建默认车辆数据
     * 默认数据创建后会立即保存到文件
     */
    public void initializeVehicles() {
        // 尝试从文件加载车辆数据
        List<Vehicle> loadedVehicles = vehicleRepository.loadVehicles();
        if (loadedVehicles != null && !loadedVehicles.isEmpty()) {
            // 文件加载成功，使用加载的数据
            this.vehicles = loadedVehicles;
            System.out.println("从文件加载车辆数据完成，共 " + vehicles.size() + " 辆车");
        } else {
            // 文件不存在或加载失败，创建默认车辆数据
            vehicles.add(new Vehicle("V001", "轿车", "丰田凯美瑞", 300.0, "可租赁"));
            vehicles.add(new Vehicle("V002", "轿车", "大众帕萨特", 280.0, "可租赁"));
            vehicles.add(new Vehicle("V003", "轿车", "本田雅阁", 320.0, "可租赁"));
            vehicles.add(new Vehicle("V004", "客车", "宇通大巴", 800.0, "可租赁"));
            vehicles.add(new Vehicle("V005", "客车", "金龙客车", 750.0, "可租赁"));
            vehicles.add(new Vehicle("V006", "卡车", "东风天龙", 1200.0, "可租赁"));
            vehicles.add(new Vehicle("V007", "卡车", "解放J6", 1000.0, "可租赁"));
            // 保存默认数据到文件
            saveVehicles();
            System.out.println("初始化车辆数据完成");
        }
    }

    /**
     * 保存车辆数据到文件
     * 调用持久化层将当前车辆列表保存到data/vehicles.dat文件
     */
    private void saveVehicles() {
        vehicleRepository.saveVehicles(vehicles);
    }

    /**
     * 获取所有可租赁的车辆
     * 筛选出状态为"可租赁"的车辆列表
     *
     * @return 可租赁车辆列表
     */
    public List<Vehicle> getAvailableVehicles() {
        List<Vehicle> availableVehicles = new ArrayList<>();
        // 遍历所有车辆，筛选可租赁的
        for (Vehicle vehicle : vehicles) {
            if ("可租赁".equals(vehicle.getStatus())) {
                availableVehicles.add(vehicle);
            }
        }
        return availableVehicles;
    }

    /**
     * 根据车辆ID查询车辆
     *
     * @param vehicleId 车辆ID
     * @return 车辆对象，如果未找到返回null
     */
    public Vehicle getVehicleById(String vehicleId) {
        // 遍历车辆列表查找匹配的ID
        for (Vehicle vehicle : vehicles) {
            if (vehicleId.equals(vehicle.getId())) {
                return vehicle;
            }
        }
        return null;
    }

    /**
     * 更新车辆状态
     * 修改指定车辆的状态，并自动保存到文件
     *
     * @param vehicleId 车辆ID
     * @param status 新的状态（可租赁、已租赁）
     * @return true表示更新成功，false表示车辆不存在
     */
    public boolean updateVehicleStatus(String vehicleId, String status) {
        // 遍历车辆列表查找并更新状态
        for (Vehicle vehicle : vehicles) {
            if (vehicleId.equals(vehicle.getId())) {
                vehicle.setStatus(status);
                // 状态变更后自动保存到文件
                saveVehicles();
                return true;
            }
        }
        return false;
    }

    /**
     * 获取所有车辆
     *
     * @return 所有车辆的列表
     */
    public List<Vehicle> getAllVehicles() {
        return vehicles;
    }
}