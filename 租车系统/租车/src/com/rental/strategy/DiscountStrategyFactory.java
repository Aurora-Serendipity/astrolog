package com.rental.strategy;

/**
 * 折扣策略工厂类
 * 根据车辆类型创建对应的折扣策略实例
 * 采用工厂模式简化策略对象的创建过程
 *
 * @author 系统
 * @version 1.0
 */
public class DiscountStrategyFactory {

    /**
     * 根据车辆类型获取对应的折扣策略
     *
     * @param vehicleType 车辆类型（轿车、客车、卡车）
     * @return 对应车型的折扣策略实例，未知类型返回默认策略
     */
    public static DiscountStrategy getDiscountStrategy(String vehicleType) {
        // 根据车型类型返回对应的折扣策略
        switch (vehicleType) {
            case "轿车":
                // 轿车使用轿车折扣策略
                return new CarDiscountStrategy();
            case "客车":
                // 客车使用客车折扣策略
                return new BusDiscountStrategy();
            case "卡车":
                // 卡车使用卡车折扣策略
                return new TruckDiscountStrategy();
            default:
                // 未知类型使用默认策略（无折扣）
                return new DefaultDiscountStrategy();
        }
    }
}