package com.cclg.dianping.controller;

import com.cclg.dianping.constant.VoucherConstants;
import com.cclg.dianping.domain.Voucher;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.service.IVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 优惠券管理控制器
 * 提供优惠券的增删改查等接口
 * 支持普通券和秒杀券两种类型，通过type字段区 分
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
     * 新增优惠券
     * 根据type字段区分普通券和秒杀券
     * - type=0：普通券
     * - type=1：秒杀券（需同时传入stock、beginTime、endTime）
     *
     * @param voucher 优惠券信息
     *                - type: 0-普通券，1-秒杀券
     *                - shopId: 关联的商铺ID（必填）
     *                - title: 优惠券标题（必填）
     *                - payValue: 支付金额（必填）
     *                - actualValue: 抵扣金额（必填）
     *                - stock: 库存（秒杀券必填）
     *                - beginTime: 开始时间（秒杀券必填）
     *                - endTime: 结束时间（秒杀券必填）
     * @return 操作结果，成功返回优惠券ID
     */
    @PostMapping
    public Result saveVoucher(@RequestBody Voucher voucher) {
        // 记录新增优惠券日志
        log.info(VoucherConstants.LOG_SAVE_VOUCHER, voucher.getTitle());
        // 调用服务层新增优惠券（根据type自动处理普通券或秒杀券）
        return voucherService.saveVoucher(voucher);
    }

    /**
     * 更新优惠券信息
     * 根据type字段区分普通券和秒杀券
     * - 如果是秒杀券，可同时更新库存、开始时间、结束时间
     *
     * @param voucher 优惠券信息
     *                - id: 优惠券ID（必填）
     *                - 其他字段为可选更新字段
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
     * 查询时会自动关联秒杀券信息（如果是秒杀券）
     *
     * @param id 优惠券ID
     * @return 优惠券信息，包含秒杀券相关信息（如果是秒杀券）
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
     * - type=0：查询普通券
     * - type=1：查询秒杀券
     * - 不传type：查询所有类型
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
        // 调用服务层分页查询优惠券（根据type筛选）
        return voucherService.queryVoucherPage(current, size, shopId, type);
    }

    /**
     * 根据ID删除优惠券
     * 删除时会自动删除关联的秒杀券信息（如果是秒杀券）
     *
     * @param id 优惠券ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result deleteVoucherById(@PathVariable("id") Long id) {
        // 记录删除优惠券日志
        log.info(VoucherConstants.LOG_DELETE_VOUCHER, id);
        // 调用服务层删除优惠券（自动处理秒杀券）
        return voucherService.deleteVoucherById(id);
    }

    /**
     * 批量删除优惠券
     * 删除时会自动删除关联的秒杀券信息
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
     * 查询该商铺下的所有优惠券（包含普通券和秒杀券）
     *
     * @param shopId 商铺ID
     * @return 优惠券列表
     */
    @GetMapping("/shop/{shopId}")
    public Result queryVoucherByShopId(@PathVariable("shopId") Long shopId) {
        // 调用服务层根据商铺ID查询优惠券列表
        return voucherService.queryVoucherByShopId(shopId);
    }
}
