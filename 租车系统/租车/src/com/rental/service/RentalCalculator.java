package com.rental.service;

import com.rental.strategy.DiscountStrategy;
import com.rental.strategy.DiscountStrategyFactory;

/**
 * 租金计算服务类
 * 负责根据车辆类型、每日租金和租赁天数计算最终租金
 * 采用策略模式获取不同车型的折扣率
 *
 * @author 系统
 * @version 1.0
 */
public class RentalCalculator {

    /**
     * 获取指定车型的折扣率
     * 根据租赁天数和车辆类型，通过策略工厂获取对应的折扣策略
     *
     * @param vehicleType 车辆类型（轿车、客车、卡车）
     * @param days 租赁天数
     * @return 折扣率（0.0-1.0之间，1.0表示无折扣）
     */
    public static double getDiscountRate(String vehicleType, int days) {
        // 通过策略工厂获取对应车型的折扣策略
        DiscountStrategy strategy = DiscountStrategyFactory.getDiscountStrategy(vehicleType);
        return strategy.getDiscountRate(days);
    }

    /**
     * 计算租赁总费用
     * 计算公式：总费用 = 每日租金 × 租赁天数 × 折扣率
     *
     * @param vehicleType 车辆类型
     * @param dailyRent 每日租金
     * @param days 租赁天数
     * @return 四舍五入后的最终费用
     */
    public static int calculateRentalFee(String vehicleType, double dailyRent, int days) {
        // 计算基础费用：每日租金 × 租赁天数
        double baseFee = dailyRent * days;

        // 获取对应车型的折扣率
        double discountRate = getDiscountRate(vehicleType, days);

        // 计算折后费用
        double discountedFee = baseFee * discountRate;

        // 返回四舍五入后的整数费用
        return (int) Math.round(discountedFee);
    }
}