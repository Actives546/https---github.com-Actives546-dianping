package com.cclg.dianping.handler;

import com.cclg.dianping.constant.VoucherConstants;
import com.cclg.dianping.domain.SeckillVoucher;
import com.cclg.dianping.domain.Voucher;
import com.cclg.dianping.service.ISeckillVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 秒杀券处理器
 * 处理秒杀券的新增、更新等逻辑
 *
 * @author system
 */
@Slf4j
@Component
public class SeckillVoucherHandler implements VoucherHandler {

    /**
     * 秒杀券服务
     */
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    /**
     * 获取处理器支持的优惠券类型
     *
     * @return 秒杀券类型
     */
    @Override
    public Integer getType() {
        return VoucherConstants.VOUCHER_TYPE_SECKILL;
    }

    /**
     * 新增秒杀券时的校验逻辑
     * 校验库存、开始时间、结束时间等
     *
     * @param voucher 优惠券信息
     * @return 校验结果，成功返回null，失败返回错误信息
     */
    @Override
    public String validateSave(Voucher voucher) {
        // 校验秒杀券库存
        if (voucher.getStock() == null) {
            return VoucherConstants.SECKILL_STOCK_NOT_NULL;
        }
        if (voucher.getStock() < 0) {
            return VoucherConstants.SECKILL_STOCK_MUST_NON_NEGATIVE;
        }

        // 校验秒杀开始时间
        if (voucher.getBeginTime() == null) {
            return VoucherConstants.BEGIN_TIME_NOT_NULL;
        }

        // 校验秒杀结束时间
        if (voucher.getEndTime() == null) {
            return VoucherConstants.END_TIME_NOT_NULL;
        }

        // 校验开始时间必须早于结束时间
        if (voucher.getBeginTime().isAfter(voucher.getEndTime())) {
            return VoucherConstants.BEGIN_TIME_MUST_BEFORE_END_TIME;
        }

        return null;
    }

    /**
     * 新增秒杀券时的额外处理逻辑
     * 保存秒杀券信息到SeckillVoucher表
     *
     * @param voucher 优惠券信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void afterSave(Voucher voucher) {
        // 构建秒杀券对象
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());

        // 设置时间
        LocalDateTime now = LocalDateTime.now();
        seckillVoucher.setCreateTime(now);
        seckillVoucher.setUpdateTime(now);

        // 保存秒杀券信息
        boolean success = seckillVoucherService.save(seckillVoucher);
        if (!success) {
            log.error("保存秒杀券信息失败，优惠券ID：{}", voucher.getId());
            throw new RuntimeException(VoucherConstants.SECKILL_VOUCHER_CREATE_FAIL);
        }

        log.info("秒杀券新增完成，优惠券ID：{}", voucher.getId());
    }

    /**
     * 更新秒杀券时的校验逻辑
     * 校验库存不能为负数、时间逻辑等
     *
     * @param voucher       待更新的优惠券信息
     * @param existVoucher  已存在的优惠券信息
     * @return 校验结果，成功返回null，失败返回错误信息
     */
    @Override
    public String validateUpdate(Voucher voucher, Voucher existVoucher) {
        // 校验库存不能为负数（如果传入了stock）
        if (voucher.getStock() != null && voucher.getStock() < 0) {
            return VoucherConstants.SECKILL_STOCK_MUST_NON_NEGATIVE;
        }

        // 校验时间逻辑（如果传入了开始时间或结束时间）
        LocalDateTime beginTime = voucher.getBeginTime();
        LocalDateTime endTime = voucher.getEndTime();

        // 如果只传入了一个时间，需要结合已存在的时间校验
        if (beginTime != null || endTime != null) {
            // 查询现有的秒杀券信息（如果存在）
            SeckillVoucher existSeckillVoucher = seckillVoucherService.getById(existVoucher.getId());

            // 确定要使用的开始时间和结束时间
            LocalDateTime effectiveBeginTime = beginTime;
            LocalDateTime effectiveEndTime = endTime;

            if (effectiveBeginTime == null && existSeckillVoucher != null) {
                effectiveBeginTime = existSeckillVoucher.getBeginTime();
            }
            if (effectiveEndTime == null && existSeckillVoucher != null) {
                effectiveEndTime = existSeckillVoucher.getEndTime();
            }

            // 如果两个时间都有值，校验开始时间必须早于结束时间
            if (effectiveBeginTime != null && effectiveEndTime != null) {
                if (effectiveBeginTime.isAfter(effectiveEndTime)) {
                    return VoucherConstants.BEGIN_TIME_MUST_BEFORE_END_TIME;
                }
            }
        }

        return null;
    }

    /**
     * 更新秒杀券时的额外处理逻辑
     * 更新或创建SeckillVoucher记录
     *
     * @param voucher       待更新的优惠券信息
     * @param existVoucher  已存在的优惠券信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void afterUpdate(Voucher voucher, Voucher existVoucher) {
        // 检查是否传入了秒杀券相关字段
        if (voucher.getStock() == null && voucher.getBeginTime() == null
                && voucher.getEndTime() == null) {
            // 没有传入秒杀券相关字段，不需要处理
            return;
        }

        // 查询现有的秒杀券信息
        SeckillVoucher existSeckillVoucher = seckillVoucherService.getById(voucher.getId());
        LocalDateTime now = LocalDateTime.now();

        if (existSeckillVoucher == null) {
            // 如果不存在秒杀券信息，需要创建
            SeckillVoucher newSeckillVoucher = new SeckillVoucher();
            newSeckillVoucher.setVoucherId(voucher.getId());
            // 使用传入的值，如果没有则使用默认值
            newSeckillVoucher.setStock(voucher.getStock() != null ? voucher.getStock() : 0);
            newSeckillVoucher.setBeginTime(voucher.getBeginTime() != null ? voucher.getBeginTime() : now);
            newSeckillVoucher.setEndTime(voucher.getEndTime() != null ? voucher.getEndTime() : now.plusDays(7));
            newSeckillVoucher.setCreateTime(now);
            newSeckillVoucher.setUpdateTime(now);

            boolean success = seckillVoucherService.save(newSeckillVoucher);
            if (!success) {
                log.error("创建秒杀券信息失败，优惠券ID：{}", voucher.getId());
                throw new RuntimeException(VoucherConstants.SECKILL_VOUCHER_CREATE_FAIL);
            }
        } else {
            // 如果已存在，更新秒杀券信息
            SeckillVoucher updateSeckillVoucher = new SeckillVoucher();
            updateSeckillVoucher.setVoucherId(voucher.getId());
            // 只更新传入的字段
            if (voucher.getStock() != null) {
                updateSeckillVoucher.setStock(voucher.getStock());
            }
            if (voucher.getBeginTime() != null) {
                updateSeckillVoucher.setBeginTime(voucher.getBeginTime());
            }
            if (voucher.getEndTime() != null) {
                updateSeckillVoucher.setEndTime(voucher.getEndTime());
            }
            updateSeckillVoucher.setUpdateTime(now);

            boolean success = seckillVoucherService.updateById(updateSeckillVoucher);
            if (!success) {
                log.error("更新秒杀券信息失败，优惠券ID：{}", voucher.getId());
                throw new RuntimeException(VoucherConstants.SECKILL_VOUCHER_UPDATE_FAIL);
            }
        }

        log.info("秒杀券更新完成，优惠券ID：{}", voucher.getId());
    }
}
