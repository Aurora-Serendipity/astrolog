package com.rental.strategy;

/**
 * 客车折扣策略类
 * 实现折扣策略接口，提供客车车型的折扣计算逻辑
 *
 * 折扣规则：
 * - 租赁天数 >= 150天：6折
 * - 租赁天数 >= 30天：7折
 * - 租赁天数 >= 7天：8折
 * - 租赁天数 >= 3天：9折
 * - 其他：原价
 *
 * @author 系统
 * @version 1.0
 */
public class BusDiscountStrategy implements DiscountStrategy {

    /** 折扣率：6折 */
    private static final double RATE_6_DISCOUNT = 0.6;

    /** 折扣率：7折 */
    private static final double RATE_7_DISCOUNT = 0.7;

    /** 折扣率：8折 */
    private static final double RATE_8_DISCOUNT = 0.8;

    /** 折扣率：9折 */
    private static final double RATE_9_DISCOUNT = 0.9;

    /** 折扣率：原价 */
    private static final double RATE_FULL = 1.0;

    /** 折扣率分界阈值：150天 */
    private static final int THRESHOLD_LONG_TERM = 150;

    /** 折扣率分界阈值：30天 */
    private static final int THRESHOLD_MEDIUM_TERM = 30;

    /** 折扣率分界阈值：7天 */
    private static final int THRESHOLD_SHORT_TERM = 7;

    /** 折扣率分界阈值：3天 */
    private static final int THRESHOLD_VERY_SHORT_TERM = 3;

    /**
     * 获取客车折扣率
     *
     * @param days 租赁天数
     * @return 折扣率
     */
    @Override
    public double getDiscountRate(int days) {
        // 租赁天数达到150天及以上，享受6折优惠
        if (days >= THRESHOLD_LONG_TERM) {
            return RATE_6_DISCOUNT;
        // 租赁天数达到30天及以上，享受7折优惠
        } else if (days >= THRESHOLD_MEDIUM_TERM) {
            return RATE_7_DISCOUNT;
        // 租赁天数达到7天及以上，享受8折优惠
        } else if (days >= THRESHOLD_SHORT_TERM) {
            return RATE_8_DISCOUNT;
        // 租赁天数达到3天及以上，享受9折优惠
        } else if (days >= THRESHOLD_VERY_SHORT_TERM) {
            return RATE_9_DISCOUNT;
        // 其他情况原价
        } else {
            return RATE_FULL;
        }
    }
}