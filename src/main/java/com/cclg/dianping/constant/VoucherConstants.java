package com.cclg.dianping.constant;

/**
 * 优惠券相关常量类
 * 定义优惠券和秒杀券的常量信息
 *
 * @author system
 */
public final class VoucherConstants {

    /**
     * 私有构造方法，防止实例化
     */
    private VoucherConstants() {
        throw new AssertionError("常量类不能被实例化");
    }

    // ========== 优惠券类型常量 ==========
    /**
     * 普通券类型
     */
    public static final Integer VOUCHER_TYPE_NORMAL = 0;

    /**
     * 秒杀券类型
     */
    public static final Integer VOUCHER_TYPE_SECKILL = 1;

    /**
     * 优惠券状态-正常
     */
    public static final Integer VOUCHER_STATUS_NORMAL = 1;

    /**
     * 优惠券状态-过期
     */
    public static final Integer VOUCHER_STATUS_EXPIRED = 2;

    // ========== 校验错误信息常量 ==========
    /**
     * 优惠券信息不能为空
     */
    public static final String VOUCHER_INFO_NOT_NULL = "优惠券信息不能为空";

    /**
     * 优惠券ID不能为空
     */
    public static final String VOUCHER_ID_NOT_NULL = "优惠券ID不能为空";

    /**
     * 优惠券ID列表不能为空
     */
    public static final String VOUCHER_ID_LIST_NOT_NULL = "优惠券ID列表不能为空";

    /**
     * 优惠券不存在
     */
    public static final String VOUCHER_NOT_EXIST = "优惠券不存在";

    /**
     * 优惠券标题不能为空
     */
    public static final String VOUCHER_TITLE_NOT_NULL = "优惠券标题不能为空";

    /**
     * 商铺ID不能为空
     */
    public static final String SHOP_ID_NOT_NULL = "商铺ID不能为空";

    /**
     * 商铺不存在
     */
    public static final String SHOP_NOT_EXIST = "商铺不存在";

    /**
     * 支付金额不能为空
     */
    public static final String PAY_VALUE_NOT_NULL = "支付金额不能为空";

    /**
     * 支付金额必须大于0
     */
    public static final String PAY_VALUE_MUST_POSITIVE = "支付金额必须大于0";

    /**
     * 抵扣金额不能为空
     */
    public static final String ACTUAL_VALUE_NOT_NULL = "抵扣金额不能为空";

    /**
     * 抵扣金额必须大于0
     */
    public static final String ACTUAL_VALUE_MUST_POSITIVE = "抵扣金额必须大于0";

    /**
     * 优惠券类型错误
     */
    public static final String VOUCHER_TYPE_ERROR = "优惠券类型错误";

    /**
     * 秒杀券信息不能为空
     */
    public static final String SECKILL_VOUCHER_INFO_NOT_NULL = "秒杀券信息不能为空";

    /**
     * 秒杀券库存不能为空
     */
    public static final String SECKILL_STOCK_NOT_NULL = "秒杀券库存不能为空";

    /**
     * 秒杀券库存必须大于等于0
     */
    public static final String SECKILL_STOCK_MUST_NON_NEGATIVE = "秒杀券库存必须大于等于0";

    /**
     * 秒杀开始时间不能为空
     */
    public static final String BEGIN_TIME_NOT_NULL = "秒杀开始时间不能为空";

    /**
     * 秒杀结束时间不能为空
     */
    public static final String END_TIME_NOT_NULL = "秒杀结束时间不能为空";

    /**
     * 秒杀开始时间必须早于结束时间
     */
    public static final String BEGIN_TIME_MUST_BEFORE_END_TIME = "秒杀开始时间必须早于结束时间";

    /**
     * 秒杀券不存在
     */
    public static final String SECKILL_VOUCHER_NOT_EXIST = "秒杀券不存在";

    // ========== 操作结果信息常量 ==========
    /**
     * 优惠券创建成功
     */
    public static final String VOUCHER_CREATE_SUCCESS = "优惠券创建成功，优惠券ID：{}";

    /**
     * 优惠券创建失败
     */
    public static final String VOUCHER_CREATE_FAIL = "优惠券创建失败";

    /**
     * 优惠券更新成功
     */
    public static final String VOUCHER_UPDATE_SUCCESS = "优惠券更新成功，优惠券ID：{}";

    /**
     * 优惠券更新失败
     */
    public static final String VOUCHER_UPDATE_FAIL = "优惠券更新失败";

    /**
     * 优惠券删除成功
     */
    public static final String VOUCHER_DELETE_SUCCESS = "优惠券删除成功，优惠券ID：{}";

    /**
     * 优惠券删除失败
     */
    public static final String VOUCHER_DELETE_FAIL = "优惠券删除失败";

    /**
     * 批量删除优惠券成功
     */
    public static final String VOUCHER_BATCH_DELETE_SUCCESS = "批量删除优惠券成功，优惠券数量：{}";

    /**
     * 批量删除优惠券失败
     */
    public static final String VOUCHER_BATCH_DELETE_FAIL = "批量删除优惠券失败";

    /**
     * 秒杀券创建成功
     */
    public static final String SECKILL_VOUCHER_CREATE_SUCCESS = "秒杀券创建成功，优惠券ID：{}";

    /**
     * 秒杀券创建失败
     */
    public static final String SECKILL_VOUCHER_CREATE_FAIL = "秒杀券创建失败";

    /**
     * 秒杀券更新成功
     */
    public static final String SECKILL_VOUCHER_UPDATE_SUCCESS = "秒杀券更新成功，优惠券ID：{}";

    /**
     * 秒杀券更新失败
     */
    public static final String SECKILL_VOUCHER_UPDATE_FAIL = "秒杀券更新失败";

    /**
     * 秒杀券删除成功
     */
    public static final String SECKILL_VOUCHER_DELETE_SUCCESS = "秒杀券删除成功，优惠券ID：{}";

    /**
     * 秒杀券删除失败
     */
    public static final String SECKILL_VOUCHER_DELETE_FAIL = "秒杀券删除失败";

    // ========== 日志信息常量 ==========
    /**
     * 新增优惠券日志
     */
    public static final String LOG_SAVE_VOUCHER = "新增优惠券，优惠券标题：{}";

    /**
     * 更新优惠券日志
     */
    public static final String LOG_UPDATE_VOUCHER = "更新优惠券，优惠券ID：{}";

    /**
     * 根据ID查询优惠券日志
     */
    public static final String LOG_GET_VOUCHER = "根据ID查询优惠券，优惠券ID：{}";

    /**
     * 分页查询优惠券日志
     */
    public static final String LOG_PAGE_QUERY_VOUCHER = "分页查询优惠券，当前页：{}，每页大小：{}，商铺ID：{}，优惠券类型：{}";

    /**
     * 删除优惠券日志
     */
    public static final String LOG_DELETE_VOUCHER = "删除优惠券，优惠券ID：{}";

    /**
     * 批量删除优惠券日志
     */
    public static final String LOG_BATCH_DELETE_VOUCHER = "批量删除优惠券，优惠券数量：{}";

    /**
     * 新增秒杀券日志
     */
    public static final String LOG_SAVE_SECKILL_VOUCHER = "新增秒杀券，优惠券ID：{}";

    /**
     * 更新秒杀券日志
     */
    public static final String LOG_UPDATE_SECKILL_VOUCHER = "更新秒杀券，优惠券ID：{}";

    /**
     * 根据ID查询秒杀券日志
     */
    public static final String LOG_GET_SECKILL_VOUCHER = "根据ID查询秒杀券，优惠券ID：{}";

    /**
     * 分页查询秒杀券日志
     */
    public static final String LOG_PAGE_QUERY_SECKILL_VOUCHER = "分页查询秒杀券，当前页：{}，每页大小：{}";

    /**
     * 删除秒杀券日志
     */
    public static final String LOG_DELETE_SECKILL_VOUCHER = "删除秒杀券，优惠券ID：{}";

    // ========== 分页参数常量 ==========
    /**
     * 默认当前页码
     */
    public static final Integer DEFAULT_PAGE_CURRENT = 1;

    /**
     * 默认每页大小
     */
    public static final Integer DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小
     */
    public static final Integer MAX_PAGE_SIZE = 100;
}
