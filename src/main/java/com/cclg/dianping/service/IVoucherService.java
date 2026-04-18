package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.Voucher;
import com.cclg.dianping.dto.Result;

import java.util.List;

/**
 * 优惠券服务接口
 * 定义优惠券相关的业务操作方法
 *
 * @author system
 */
public interface IVoucherService extends IService<Voucher> {

    /**
     * 新增优惠券
     * 支持普通券和秒杀券两种类型
     * 秒杀券需要同时保存秒杀券信息
     *
     * @param voucher 优惠券信息
     * @return 操作结果，成功返回优惠券ID
     */
    Result saveVoucher(Voucher voucher);

    /**
     * 更新优惠券
     * 更新优惠券基本信息
     * 如果是秒杀券，同时更新秒杀券信息
     *
     * @param voucher 优惠券信息
     * @return 操作结果
     */
    Result updateVoucher(Voucher voucher);

    /**
     * 根据ID查询优惠券信息
     * 查询时会关联查询秒杀券信息（如果是秒杀券）
     *
     * @param id 优惠券ID
     * @return 优惠券信息
     */
    Result getVoucherById(Long id);

    /**
     * 分页查询优惠券信息
     * 支持按商铺ID和优惠券类型筛选
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param shopId  商铺ID（可选，按商铺筛选）
     * @param type    优惠券类型（可选，0-普通券，1-秒杀券）
     * @return 分页结果，包含数据列表和总记录数
     */
    Result queryVoucherPage(Integer current, Integer size, Long shopId, Integer type);

    /**
     * 根据ID删除优惠券
     * 删除时同时删除关联的秒杀券信息
     *
     * @param id 优惠券ID
     * @return 操作结果
     */
    Result deleteVoucherById(Long id);

    /**
     * 批量删除优惠券
     * 注意：有一个删除失败则整体失败
     *
     * @param ids 优惠券ID列表
     * @return 操作结果
     */
    Result deleteVoucherByIds(List<Long> ids);

    /**
     * 根据商铺ID查询优惠券列表
     * 查询该商铺下的所有优惠券
     *
     * @param shopId 商铺ID
     * @return 优惠券列表
     */
    Result queryVoucherByShopId(Long shopId);
}
