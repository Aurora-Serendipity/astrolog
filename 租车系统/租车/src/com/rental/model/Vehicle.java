package com.rental.model;

import java.io.Serializable;

/**
 * 车辆数据模型类
 * 用于存储车辆的基本信息，包括ID、车型、具体车型、日租金和状态
 *
 * @author 系统
 * @version 1.0
 */
public class Vehicle implements Serializable {
    /** 序列化版本号，用于版本控制 */
    private static final long serialVersionUID = 1L;

    /** 车辆唯一标识符 */
    private String id;

    /** 车辆类型：轿车、客车、卡车 */
    private String type;

    /** 具体车型名称，如"丰田凯美瑞" */
    private String model;

    /** 每日租金价格 */
    private double dailyRent;

    /** 车辆状态：可租赁、已租赁 */
    private String status;

    /**
     * 构造函数，初始化车辆对象
     *
     * @param id 车辆ID
     * @param type 车辆类型
     * @param model 具体车型
     * @param dailyRent 日租金
     * @param status 车辆状态
     */
    public Vehicle(String id, String type, String model, double dailyRent, String status) {
        this.id = id;
        this.type = type;
        this.model = model;
        this.dailyRent = dailyRent;
        this.status = status;
    }

    /**
     * 获取车辆ID
     *
     * @return 车辆ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置车辆ID
     *
     * @param id 车辆ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取车辆类型
     *
     * @return 车辆类型
     */
    public String getType() {
        return type;
    }

    /**
     * 设置车辆类型
     *
     * @param type 车辆类型
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 获取具体车型
     *
     * @return 具体车型
     */
    public String getModel() {
        return model;
    }

    /**
     * 设置具体车型
     *
     * @param model 具体车型
     */
    public void setModel(String model) {
        this.model = model;
    }

    /**
     * 获取日租金
     *
     * @return 日租金
     */
    public double getDailyRent() {
        return dailyRent;
    }

    /**
     * 设置日租金
     *
     * @param dailyRent 日租金
     */
    public void setDailyRent(double dailyRent) {
        this.dailyRent = dailyRent;
    }

    /**
     * 获取车辆状态
     *
     * @return 车辆状态
     */
    public String getStatus() {
        return status;
    }

    /**
     * 设置车辆状态
     *
     * @param status 车辆状态
     */
    public void setStatus(String status) {
        this.status = status;
    }
}