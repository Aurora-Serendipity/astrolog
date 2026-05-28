package com.rental.strategy;

/**
 * 默认折扣策略类
 * 实现折扣策略接口，提供未知车型或不享受折扣的车辆的折扣计算逻辑
 * 当车辆类型无法匹配其他策略时使用此策略
 *
 * @author 系统
 * @version 1.0
 */
public class DefaultDiscountStrategy implements DiscountStrategy {

    /**
     * 获取默认折扣率
     * 默认策略不提供任何折扣，始终返回原价
     *
     * @param days 租赁天数（此参数未被使用）
     * @return 始终返回1.0（原价）
     */
    @Override
    public double getDiscountRate(int days) {
        // 默认策略不打折，始终返回原价
        return 1.0;
    }
}