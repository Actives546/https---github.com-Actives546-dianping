package com.cclg.dianping.handler;

import com.cclg.dianping.constant.VoucherConstants;
import com.cclg.dianping.domain.Voucher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 普通券处理器
 * 处理普通券的新增、更新等逻辑
 *
 * @author system
 */
@Slf4j
@Component
public class NormalVoucherHandler implements VoucherHandler {

    /**
     * 获取处理器支持的优惠券类型
     *
     * @return 普通券类型
     */
    @Override
    public Integer getType() {
        return VoucherConstants.VOUCHER_TYPE_NORMAL;
    }

    /**
     * 新增普通券时的校验逻辑
     * 普通券不需要额外校验
     *
     * @param voucher 优惠券信息
     * @return 校验结果，普通券始终返回null（无需额外校验）
     */
    @Override
    public String validateSave(Voucher voucher) {
        // 普通券不需要额外校验，在Service层已经校验了通用参数
        return null;
    }

    /**
     * 新增普通券时的额外处理逻辑
     * 普通券不需要额外处理
     *
     * @param voucher 优惠券信息
     */
    @Override
    public void afterSave(Voucher voucher) {
        // 普通券不需要额外处理
        log.info("普通券新增完成，优惠券ID：{}", voucher.getId());
    }

    /**
     * 更新普通券时的校验逻辑
     * 普通券不需要额外校验
     *
     * @param voucher       待更新的优惠券信息
     * @param existVoucher  已存在的优惠券信息
     * @return 校验结果，普通券始终返回null
     */
    @Override
    public String validateUpdate(Voucher voucher, Voucher existVoucher) {
        // 普通券不需要额外校验
        return null;
    }

    /**
     * 更新普通券时的额外处理逻辑
     * 普通券不需要额外处理
     *
     * @param voucher       待更新的优惠券信息
     * @param existVoucher  已存在的优惠券信息
     */
    @Override
    public void afterUpdate(Voucher voucher, Voucher existVoucher) {
        // 普通券不需要额外处理
        log.info("普通券更新完成，优惠券ID：{}", voucher.getId());
    }
}
