package com.rental.model;

import java.io.Serializable;

/**
 * 租赁记录数据模型类
 * 用于存储车辆租赁的完整记录信息
 *
 * @author 系统
 * @version 1.0
 */
public class RentalRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String vehicleId;
    private String vehicleModel;
    private String vehicleType;
    private String customerName;
    private double dailyRent;
    private int days;
    private int totalFee;
    private long rentalDate;
    private Long returnDate;
    private String status;

    /**
     * 构造函数
     */
    public RentalRecord() {
    }

    /**
     * 获取记录ID
     * @return 记录ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置记录ID
     * @param id 记录ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取车辆ID
     * @return 车辆ID
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * 设置车辆ID
     * @param vehicleId 车辆ID
     */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * 获取车型
     * @return 车型
     */
    public String getVehicleModel() {
        return vehicleModel;
    }

    /**
     * 设置车型
     * @param vehicleModel 车型
     */
    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    /**
     * 获取车辆类型
     * @return 车辆类型
     */
    public String getVehicleType() {
        return vehicleType;
    }

    /**
     * 设置车辆类型
     * @param vehicleType 车辆类型
     */
    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    /**
     * 获取客户姓名
     * @return 客户姓名
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * 设置客户姓名
     * @param customerName 客户姓名
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * 获取每日租金
     * @return 每日租金
     */
    public double getDailyRent() {
        return dailyRent;
    }

    /**
     * 设置每日租金
     * @param dailyRent 每日租金
     */
    public void setDailyRent(double dailyRent) {
        this.dailyRent = dailyRent;
    }

    /**
     * 获取租赁天数
     * @return 租赁天数
     */
    public int getDays() {
        return days;
    }

    /**
     * 设置租赁天数
     * @param days 租赁天数
     */
    public void setDays(int days) {
        this.days = days;
    }

    /**
     * 获取总费用
     * @return 总费用
     */
    public int getTotalFee() {
        return totalFee;
    }

    /**
     * 设置总费用
     * @param totalFee 总费用
     */
    public void setTotalFee(int totalFee) {
        this.totalFee = totalFee;
    }

    /**
     * 获取租赁日期（时间戳）
     * @return 租赁日期
     */
    public long getRentalDate() {
        return rentalDate;
    }

    /**
     * 设置租赁日期（时间戳）
     * @param rentalDate 租赁日期
     */
    public void setRentalDate(long rentalDate) {
        this.rentalDate = rentalDate;
    }

    /**
     * 获取归还日期（时间戳）
     * @return 归还日期，未归还为null
     */
    public Long getReturnDate() {
        return returnDate;
    }

    /**
     * 设置归还日期（时间戳）
     * @param returnDate 归还日期
     */
    public void setReturnDate(Long returnDate) {
        this.returnDate = returnDate;
    }

    /**
     * 获取状态
     * @return 状态（租用中、已归还）
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置状态
     * @param status 状态（租用中、已归还）
     */
    public void setStatus(String status) {
        this.status = status;
    }
}