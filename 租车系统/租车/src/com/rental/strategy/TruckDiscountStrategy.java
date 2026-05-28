package com.rental.strategy;

/**
 * 卡车折扣策略类
 * 实现折扣策略接口，提供卡车车型的折扣计算逻辑
 *
 * 折扣规则：
 * - 租赁天数 > 30天：75折
 * - 租赁天数 > 15天：85折
 * - 租赁天数 > 7天：95折
 * - 其他：原价
 *
 * @author 系统
 * @version 1.0
 */
public class TruckDiscountStrategy implements DiscountStrategy {

    /** 折扣率：75折 */
    private static final double RATE_75_DISCOUNT = 0.75;

    /** 折扣率：85折 */
    private static final double RATE_85_DISCOUNT = 0.85;

    /** 折扣率：95折 */
    private static final double RATE_95_DISCOUNT = 0.95;

    /** 折扣率：原价 */
    private static final double RATE_FULL = 1.0;

    /** 折扣率分界阈值：30天 */
    private static final int THRESHOLD_LONG_TERM = 30;

    /** 折扣率分界阈值：15天 */
    private static final int THRESHOLD_MEDIUM_TERM = 15;

    /** 折扣率分界阈值：7天 */
    private static final int THRESHOLD_SHORT_TERM = 7;

    /**
     * 获取卡车折扣率
     *
     * @param days 租赁天数
     * @return 折扣率
     */
    @Override
    public double getDiscountRate(int days) {
        // 租赁天数超过30天，享受75折优惠
        if (days > THRESHOLD_LONG_TERM) {
            return RATE_75_DISCOUNT;
        // 租赁天数超过15天，享受85折优惠
        } else if (days > THRESHOLD_MEDIUM_TERM) {
            return RATE_85_DISCOUNT;
        // 租赁天数超过7天，享受95折优惠
        } else if (days > THRESHOLD_SHORT_TERM) {
            return RATE_95_DISCOUNT;
        // 其他情况原价
        } else {
            return RATE_FULL;
        }
    }
}