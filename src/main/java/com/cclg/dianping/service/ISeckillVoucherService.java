package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.SeckillVoucher;
import com.cclg.dianping.dto.Result;

import java.util.List;

/**
 * 秒杀券服务接口
 * 定义秒杀券相关的业务操作方法
 *
 * @author system
 */
public interface ISeckillVoucherService extends IService<SeckillVoucher> {

    /**
     * 新增秒杀券
     * 秒杀券关联到已存在的优惠券
     *
     * @param seckillVoucher 秒杀券信息
     * @return 操作结果
     */
    Result saveSeckillVoucher(SeckillVoucher seckillVoucher);

    /**
     * 更新秒杀券
     * 更新秒杀券的库存、时间等信息
     *
     * @param seckillVoucher 秒杀券信息
     * @return 操作结果
     */
    Result updateSeckillVoucher(SeckillVoucher seckillVoucher);

    /**
     * 根据ID查询秒杀券信息
     *
     * @param voucherId 优惠券ID（秒杀券主键）
     * @return 秒杀券信息
     */
    Result getSeckillVoucherById(Long voucherId);

    /**
     * 分页查询秒杀券信息
     *
     * @param current 当前页码
     * @param size    每页大小
     * @return 分页结果，包含数据列表和总记录数
     */
    Result querySeckillVoucherPage(Integer current, Integer size);

    /**
     * 根据ID删除秒杀券
     *
     * @param voucherId 优惠券ID（秒杀券主键）
     * @return 操作结果
     */
    Result deleteSeckillVoucherById(Long voucherId);

    /**
     * 批量删除秒杀券
     *
     * @param voucherIds 优惠券ID列表
     * @return 操作结果
     */
    Result deleteSeckillVoucherByIds(List<Long> voucherIds);

    /**
     * 扣减秒杀券库存
     * 用于秒杀下单时扣减库存
     *
     * @param voucherId 优惠券ID
     * @return 操作结果
     */
    Result decreaseStock(Long voucherId);

    /**
     * 查询秒杀券列表
     * 查询所有可用的秒杀券
     *
     * @return 秒杀券列表
     */
    Result querySeckillVoucherList();
}
