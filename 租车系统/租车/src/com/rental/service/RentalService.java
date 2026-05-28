package com.rental.service;

import com.rental.model.RentalRecord;
import com.rental.model.Vehicle;
import com.rental.repository.RentalRepository;
import com.rental.repository.VehicleRepository;
import java.util.List;
import java.util.UUID;

/**
 * 租赁业务服务类
 * 封装租赁相关的业务逻辑
 * 提供租车、还车、费用计算等功能
 *
 * @author 系统
 * @version 1.0
 */
public class RentalService {
    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;

    /**
     * 构造函数
     */
    public RentalService() {
        this.rentalRepository = new RentalRepository();
        this.vehicleRepository = new VehicleRepository();
    }

    /**
     * 租车
     * @param vehicleId 车辆ID
     * @param customerName 客户姓名
     * @param days 租赁天数
     * @return 租赁记录，失败返回null
     */
    public RentalRecord rentVehicle(String vehicleId, String customerName, int days) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle == null || !"可租赁".equals(vehicle.getStatus())) {
            return null;
        }

        int totalFee = RentalCalculator.calculateRentalFee(vehicle.getType(), vehicle.getDailyRent(), days);

        RentalRecord record = new RentalRecord();
        record.setId("R" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        record.setVehicleId(vehicleId);
        record.setVehicleModel(vehicle.getModel());
        record.setVehicleType(vehicle.getType());
        record.setCustomerName(customerName);
        record.setDailyRent(vehicle.getDailyRent());
        record.setDays(days);
        record.setTotalFee(totalFee);
        record.setRentalDate(System.currentTimeMillis());
        record.setStatus("租用中");

        rentalRepository.save(record);

        vehicle.setStatus("已租赁");
        vehicleRepository.update(vehicle);

        return record;
    }

    /**
     * 还车
     * @param vehicleId 车辆ID
     * @return true表示还车成功
     */
    public boolean returnVehicle(String vehicleId) {
        RentalRecord record = rentalRepository.findActiveRental(vehicleId);
        if (record == null) {
            return false;
        }

        record.setReturnDate(System.currentTimeMillis());
        record.setStatus("已归还");
        rentalRepository.update(record);

        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle != null) {
            vehicle.setStatus("可租赁");
            vehicleRepository.update(vehicle);
        }

        return true;
    }

    /**
     * 获取车辆的当前租赁记录
     * @param vehicleId 车辆ID
     * @return 租赁记录，未找到返回null
     */
    public RentalRecord getActiveRental(String vehicleId) {
        return rentalRepository.findActiveRental(vehicleId);
    }

    /**
     * 获取所有租赁记录
     * @return 租赁记录列表
     */
    public List<RentalRecord> getAllRentals() {
        return rentalRepository.findAll();
    }

    /**
     * 根据车辆ID获取租赁记录
     * @param vehicleId 车辆ID
     * @return 租赁记录列表
     */
    public List<RentalRecord> getRentalsByVehicle(String vehicleId) {
        return rentalRepository.findByVehicleId(vehicleId);
    }

    /**
     * 计算租赁费用
     * @param vehicleType 车辆类型
     * @param dailyRent 每日租金
     * @param days 租赁天数
     * @return 总费用
     */
    public int calculateFee(String vehicleType, double dailyRent, int days) {
        return RentalCalculator.calculateRentalFee(vehicleType, dailyRent, days);
    }
}