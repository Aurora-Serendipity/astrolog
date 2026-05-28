package com.rental.service;

import com.rental.model.Vehicle;
import com.rental.repository.VehicleRepository;
import java.util.List;
import java.util.UUID;

/**
 * 车辆业务服务类
 * 封装车辆相关的业务逻辑
 * 提供车辆查添加询、、更新、删除等功能
 *
 * @author 系统
 * @version 1.0
 */
public class VehicleService {
    private final VehicleRepository vehicleRepository;

    /**
     * 构造函数
     */
    public VehicleService() {
        this.vehicleRepository = new VehicleRepository();
    }

    /**
     * 获取所有车辆
     * @return 车辆列表
     */
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    /**
     * 获取可租赁的车辆
     * @return 可租赁车辆列表
     */
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findByStatus("可租赁");
    }

    /**
     * 根据ID获取车辆
     * @param id 车辆ID
     * @return 车辆对象，未找到返回null
     */
    public Vehicle getVehicleById(String id) {
        return vehicleRepository.findById(id);
    }

    /**
     * 添加车辆
     * @param vehicle 车辆对象
     * @return true表示添加成功
     */
    public boolean addVehicle(Vehicle vehicle) {
        if (vehicle.getId() == null || vehicle.getId().isEmpty()) {
            vehicle.setId("V" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        vehicleRepository.save(vehicle);
        return true;
    }

    /**
     * 更新车辆
     * @param vehicle 车辆对象
     * @return true表示更新成功
     */
    public boolean updateVehicle(Vehicle vehicle) {
        Vehicle existing = vehicleRepository.findById(vehicle.getId());
        if (existing == null) {
            return false;
        }
        vehicleRepository.update(vehicle);
        return true;
    }

    /**
     * 更新车辆状态
     * @param vehicleId 车辆ID
     * @param status 新状态
     * @return true表示更新成功
     */
    public boolean updateVehicleStatus(String vehicleId, String status) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle == null) {
            return false;
        }
        vehicle.setStatus(status);
        vehicleRepository.update(vehicle);
        return true;
    }

    /**
     * 删除车辆
     * @param id 车辆ID
     * @return true表示删除成功
     */
    public boolean deleteVehicle(String id) {
        Vehicle vehicle = vehicleRepository.findById(id);
        if (vehicle == null) {
            return false;
        }
        vehicleRepository.delete(id);
        return true;
    }

    /**
     * 初始化默认数据
     */
    public void initializeDefaultData() {
        if (vehicleRepository.findAll().isEmpty()) {
            vehicleRepository.saveAll(createDefaultVehicles());
        }
    }

    /**
     * 创建默认车辆数据
     * @return 默认车辆列表
     */
    private List<Vehicle> createDefaultVehicles() {
        return List.of(
            new Vehicle("V001", "轿车", "丰田凯美瑞", 300.0, "可租赁"),
            new Vehicle("V002", "轿车", "大众帕萨特", 280.0, "可租赁"),
            new Vehicle("V003", "轿车", "本田雅阁", 320.0, "可租赁"),
            new Vehicle("V004", "客车", "宇通大巴", 800.0, "可租赁"),
            new Vehicle("V005", "客车", "金龙客车", 750.0, "可租赁"),
            new Vehicle("V006", "卡车", "东风天龙", 1200.0, "可租赁"),
            new Vehicle("V007", "卡车", "解放J6", 1000.0, "可租赁")
        );
    }
}