package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.VoucherOrder;
import com.cclg.dianping.dto.Result;

/**
 * 优惠券订单服务接口
 * 提供秒杀券下单等核心业务功能
 *
 * @author system
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    /**
     * 秒杀券下单
     * 实现一人一单、库存扣减一致性的保证
     *
     * @param voucherId 秒杀券ID
     * @return 操作结果，成功返回订单ID
     */
    Result seckillVoucher(Long voucherId);

    /**
     * 创建优惠券订单
     * 在分布式锁保护下执行，保证数据一致性
     *
     * @param voucherId 秒杀券ID
     * @param userId    用户ID
     * @return 订单ID
     */
    Long createVoucherOrder(Long voucherId, Long userId);
}
