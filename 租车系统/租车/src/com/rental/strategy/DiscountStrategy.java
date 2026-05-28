package com.rental.strategy;

/**
 * 折扣策略接口
 * 定义获取折扣率的方法，不同车型实现此接口提供各自的折扣计算逻辑
 * 采用策略模式，支持灵活扩展不同车型的折扣策略
 *
 * @author 系统
 * @version 1.0
 */
public interface DiscountStrategy {

    /**
     * 根据租赁天数获取折扣率
     *
     * @param days 租赁天数
     * @return 折扣率（0.0-1.0之间，1.0表示无折扣）
     */
    double getDiscountRate(int days);
}