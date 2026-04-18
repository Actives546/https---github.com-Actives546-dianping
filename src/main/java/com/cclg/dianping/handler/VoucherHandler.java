package com.cclg.dianping.handler;

import com.cclg.dianping.domain.Voucher;
import com.cclg.dianping.dto.Result;

/**
 * 优惠券处理器接口
 * 定义不同类型优惠券的处理逻辑
 * 使用策略模式，不同类型的优惠券有不同的实现
 *
 * @author system
 */
public interface VoucherHandler {

    /**
     * 获取处理器支持的优惠券类型
     *
     * @return 优惠券类型
     */
    Integer getType();

    /**
     * 新增优惠券时的校验逻辑
     *
     * @param voucher 优惠券信息
     * @return 校验结果，成功返回null，失败返回错误信息
     */
    String validateSave(Voucher voucher);

    /**
     * 新增优惠券时的额外处理逻辑
     *
     * @param voucher 优惠券信息
     */
    void afterSave(Voucher voucher);

    /**
     * 更新优惠券时的校验逻辑
     *
     * @param voucher       待更新的优惠券信息
     * @param existVoucher  已存在的优惠券信息
     * @return 校验结果，成功返回null，失败返回错误信息
     */
    String validateUpdate(Voucher voucher, Voucher existVoucher);

    /**
     * 更新优惠券时的额外处理逻辑
     *
     * @param voucher       待更新的优惠券信息
     * @param existVoucher  已存在的优惠券信息
     */
    void afterUpdate(Voucher voucher, Voucher existVoucher);
}
