package com.cclg.dianping.controller;

import com.cclg.dianping.constant.VoucherConstants;
import com.cclg.dianping.domain.SeckillVoucher;
import com.cclg.dianping.domain.Voucher;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.service.ISeckillVoucherService;
import com.cclg.dianping.service.IVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 优惠券管理控制器
 * 提供优惠券和秒杀券的增删改查等接口
 * 支持普通券和秒杀券两种类型
 *
 * @author system
 */
@Slf4j
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    /**
     * 优惠券服务
     */
    @Resource
    private IVoucherService voucherService;

    /**
     * 秒杀券服务
     */
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    // ========== 优惠券相关接口 ==========

    /**
     * 新增优惠券
     * 支持普通券和秒杀券两种类型
     * 秒杀券需要同时传入库存、开始时间、结束时间等信息
     *
     * @param voucher 优惠券信息
     *                - type: 0-普通券，1-秒杀券
     *                - shopId: 关联的商铺ID
     *                - title: 优惠券标题
     *                - payValue: 支付金额
     *                - actualValue: 抵扣金额
     *                - stock: 库存（秒杀券必填）
     *                - beginTime: 开始时间（秒杀券必填）
     *                - endTime: 结束时间（秒杀券必填）
     * @return 操作结果，成功返回优惠券ID
     */
    @PostMapping
    public Result saveVoucher(@RequestBody Voucher voucher) {
        // 记录新增优惠券日志
        log.info(VoucherConstants.LOG_SAVE_VOUCHER, voucher.getTitle());
        // 调用服务层新增优惠券
        return voucherService.saveVoucher(voucher);
    }

    /**
     * 更新优惠券信息
     * 更新优惠券基本信息
     * 如果是秒杀券，同时可以更新秒杀券的库存、时间等信息
     *
     * @param voucher 优惠券信息
     * @return 操作结果
     */
    @PutMapping
    public Result updateVoucher(@RequestBody Voucher voucher) {
        // 记录更新优惠券日志
        log.info(VoucherConstants.LOG_UPDATE_VOUCHER, voucher.getId());
        // 调用服务层更新优惠券
        return voucherService.updateVoucher(voucher);
    }

    /**
     * 根据ID查询优惠券信息
     * 查询时会关联查询秒杀券信息（如果是秒杀券）
     *
     * @param id 优惠券ID
     * @return 优惠券信息
     */
    @GetMapping("/{id}")
    public Result getVoucherById(@PathVariable("id") Long id) {
        // 记录查询优惠券日志
        log.info(VoucherConstants.LOG_GET_VOUCHER, id);
        // 调用服务层查询优惠券
        return voucherService.getVoucherById(id);
    }

    /**
     * 分页查询优惠券信息
     * 支持按商铺ID和优惠券类型筛选
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10，最大100
     * @param shopId  商铺ID（可选，按商铺筛选）
     * @param type    优惠券类型（可选，0-普通券，1-秒杀券）
     * @return 分页结果，包含数据列表和总记录数
     */
    @GetMapping("/page")
    public Result queryVoucherPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "shopId", required = false) Long shopId,
            @RequestParam(value = "type", required = false) Integer type) {
        // 记录分页查询优惠券日志
        log.info(VoucherConstants.LOG_PAGE_QUERY_VOUCHER, current, size, shopId, type);
        // 调用服务层分页查询优惠券
        return voucherService.queryVoucherPage(current, size, shopId, type);
    }

    /**
     * 根据ID删除优惠券
     * 删除时同时删除关联的秒杀券信息
     *
     * @param id 优惠券ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result deleteVoucherById(@PathVariable("id") Long id) {
        // 记录删除优惠券日志
        log.info(VoucherConstants.LOG_DELETE_VOUCHER, id);
        // 调用服务层删除优惠券
        return voucherService.deleteVoucherById(id);
    }

    /**
     * 批量删除优惠券
     * 使用MyBatis-Plus的批量删除方法
     *
     * @param ids 优惠券ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result deleteVoucherByIds(@RequestBody List<Long> ids) {
        // 记录批量删除优惠券日志
        log.info(VoucherConstants.LOG_BATCH_DELETE_VOUCHER, ids.size());
        // 调用服务层批量删除优惠券
        return voucherService.deleteVoucherByIds(ids);
    }

    /**
     * 根据商铺ID查询优惠券列表
     * 查询该商铺下的所有优惠券
     *
     * @param shopId 商铺ID
     * @return 优惠券列表
     */
    @GetMapping("/shop/{shopId}")
    public Result queryVoucherByShopId(@PathVariable("shopId") Long shopId) {
        // 调用服务层根据商铺ID查询优惠券列表
        return voucherService.queryVoucherByShopId(shopId);
    }

    // ========== 秒杀券相关接口 ==========

    /**
     * 新增秒杀券
     * 秒杀券关联到已存在的优惠券
     * 注意：建议使用saveVoucher接口统一创建秒杀券
     *
     * @param seckillVoucher 秒杀券信息
     * @return 操作结果
     */
    @PostMapping("/seckill")
    public Result saveSeckillVoucher(@RequestBody SeckillVoucher seckillVoucher) {
        // 记录新增秒杀券日志
        log.info(VoucherConstants.LOG_SAVE_SECKILL_VOUCHER, seckillVoucher.getVoucherId());
        // 调用服务层新增秒杀券
        return seckillVoucherService.saveSeckillVoucher(seckillVoucher);
    }

    /**
     * 更新秒杀券
     * 更新秒杀券的库存、时间等信息
     *
     * @param seckillVoucher 秒杀券信息
     * @return 操作结果
     */
    @PutMapping("/seckill")
    public Result updateSeckillVoucher(@RequestBody SeckillVoucher seckillVoucher) {
        // 记录更新秒杀券日志
        log.info(VoucherConstants.LOG_UPDATE_SECKILL_VOUCHER, seckillVoucher.getVoucherId());
        // 调用服务层更新秒杀券
        return seckillVoucherService.updateSeckillVoucher(seckillVoucher);
    }

    /**
     * 根据ID查询秒杀券信息
     *
     * @param voucherId 优惠券ID（秒杀券主键）
     * @return 秒杀券信息
     */
    @GetMapping("/seckill/{voucherId}")
    public Result getSeckillVoucherById(@PathVariable("voucherId") Long voucherId) {
        // 记录查询秒杀券日志
        log.info(VoucherConstants.LOG_GET_SECKILL_VOUCHER, voucherId);
        // 调用服务层查询秒杀券
        return seckillVoucherService.getSeckillVoucherById(voucherId);
    }

    /**
     * 分页查询秒杀券信息
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10，最大100
     * @return 分页结果，包含数据列表和总记录数
     */
    @GetMapping("/seckill/page")
    public Result querySeckillVoucherPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        // 记录分页查询秒杀券日志
        log.info(VoucherConstants.LOG_PAGE_QUERY_SECKILL_VOUCHER, current, size);
        // 调用服务层分页查询秒杀券
        return seckillVoucherService.querySeckillVoucherPage(current, size);
    }

    /**
     * 根据ID删除秒杀券
     *
     * @param voucherId 优惠券ID（秒杀券主键）
     * @return 操作结果
     */
    @DeleteMapping("/seckill/{voucherId}")
    public Result deleteSeckillVoucherById(@PathVariable("voucherId") Long voucherId) {
        // 记录删除秒杀券日志
        log.info(VoucherConstants.LOG_DELETE_SECKILL_VOUCHER, voucherId);
        // 调用服务层删除秒杀券
        return seckillVoucherService.deleteSeckillVoucherById(voucherId);
    }

    /**
     * 批量删除秒杀券
     *
     * @param voucherIds 优惠券ID列表
     * @return 操作结果
     */
    @DeleteMapping("/seckill/batch")
    public Result deleteSeckillVoucherByIds(@RequestBody List<Long> voucherIds) {
        // 调用服务层批量删除秒杀券
        return seckillVoucherService.deleteSeckillVoucherByIds(voucherIds);
    }

    /**
     * 查询秒杀券列表
     * 查询所有可用的秒杀券（库存大于0）
     *
     * @return 秒杀券列表
     */
    @GetMapping("/seckill/list")
    public Result querySeckillVoucherList() {
        // 调用服务层查询秒杀券列表
        return seckillVoucherService.querySeckillVoucherList();
    }

    /**
     * 扣减秒杀券库存
     * 用于秒杀下单时扣减库存
     * 使用乐观锁保证并发安全
     *
     * @param voucherId 优惠券ID
     * @return 操作结果
     */
    @PostMapping("/seckill/decreaseStock/{voucherId}")
    public Result decreaseStock(@PathVariable("voucherId") Long voucherId) {
        // 调用服务层扣减秒杀券库存
        return seckillVoucherService.decreaseStock(voucherId);
    }
}
