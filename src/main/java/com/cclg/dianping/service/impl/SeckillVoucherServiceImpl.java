package com.cclg.dianping.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.constant.VoucherConstants;
import com.cclg.dianping.domain.SeckillVoucher;
import com.cclg.dianping.domain.Voucher;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.mapper.SeckillVoucherMapper;
import com.cclg.dianping.service.ISeckillVoucherService;
import com.cclg.dianping.service.IVoucherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 秒杀券服务实现类
 * 实现秒杀券相关的业务逻辑
 *
 * @author system
 */
@Slf4j
@Service
public class SeckillVoucherServiceImpl extends ServiceImpl<SeckillVoucherMapper, SeckillVoucher> implements ISeckillVoucherService {

    /**
     * 优惠券服务，用于校验优惠券是否存在
     */
    @Resource
    private IVoucherService voucherService;

    /**
     * 新增秒杀券
     * 业务逻辑：
     * 1. 校验秒杀券信息不能为空
     * 2. 校验优惠券ID不能为空
     * 3. 校验优惠券是否存在
     * 4. 校验优惠券类型是否为秒杀券
     * 5. 校验秒杀券库存
     * 6. 校验秒杀开始时间和结束时间
     * 7. 设置创建时间和更新时间
     * 8. 保存秒杀券信息
     *
     * @param seckillVoucher 秒杀券信息
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveSeckillVoucher(SeckillVoucher seckillVoucher) {
        // ========== 1. 校验秒杀券信息不能为空 ==========
        if (seckillVoucher == null) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_INFO_NOT_NULL);
        }

        // ========== 2. 校验优惠券ID不能为空 ==========
        if (seckillVoucher.getVoucherId() == null) {
            return Result.fail(VoucherConstants.VOUCHER_ID_NOT_NULL);
        }

        // ========== 3. 校验优惠券是否存在 ==========
        Voucher voucher = voucherService.getById(seckillVoucher.getVoucherId());
        if (voucher == null) {
            return Result.fail(VoucherConstants.VOUCHER_NOT_EXIST);
        }

        // ========== 4. 校验优惠券类型是否为秒杀券 ==========
        if (!VoucherConstants.VOUCHER_TYPE_SECKILL.equals(voucher.getType())) {
            return Result.fail(VoucherConstants.VOUCHER_TYPE_ERROR);
        }

        // ========== 5. 校验秒杀券库存 ==========
        if (seckillVoucher.getStock() == null) {
            return Result.fail(VoucherConstants.SECKILL_STOCK_NOT_NULL);
        }
        if (seckillVoucher.getStock() < 0) {
            return Result.fail(VoucherConstants.SECKILL_STOCK_MUST_NON_NEGATIVE);
        }

        // ========== 6. 校验秒杀开始时间和结束时间 ==========
        // 校验秒杀开始时间
        if (seckillVoucher.getBeginTime() == null) {
            return Result.fail(VoucherConstants.BEGIN_TIME_NOT_NULL);
        }

        // 校验秒杀结束时间
        if (seckillVoucher.getEndTime() == null) {
            return Result.fail(VoucherConstants.END_TIME_NOT_NULL);
        }

        // 校验开始时间必须早于结束时间
        if (seckillVoucher.getBeginTime().isAfter(seckillVoucher.getEndTime())) {
            return Result.fail(VoucherConstants.BEGIN_TIME_MUST_BEFORE_END_TIME);
        }

        // ========== 7. 设置创建时间和更新时间 ==========
        LocalDateTime now = LocalDateTime.now();
        seckillVoucher.setCreateTime(now);
        seckillVoucher.setUpdateTime(now);

        // ========== 8. 保存秒杀券信息 ==========
        boolean success = save(seckillVoucher);
        if (!success) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_CREATE_FAIL);
        }

        log.info(VoucherConstants.SECKILL_VOUCHER_CREATE_SUCCESS, seckillVoucher.getVoucherId());
        return Result.ok();
    }

    /**
     * 更新秒杀券
     * 业务逻辑：
     * 1. 校验秒杀券信息不能为空
     * 2. 校验优惠券ID不能为空
     * 3. 校验秒杀券是否存在
     * 4. 校验秒杀券库存（如果传入了）
     * 5. 校验秒杀开始时间和结束时间（如果传入了）
     * 6. 设置更新时间
     * 7. 更新秒杀券信息
     *
     * @param seckillVoucher 秒杀券信息
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateSeckillVoucher(SeckillVoucher seckillVoucher) {
        // ========== 1. 校验秒杀券信息不能为空 ==========
        if (seckillVoucher == null) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_INFO_NOT_NULL);
        }

        // ========== 2. 校验优惠券ID不能为空 ==========
        if (seckillVoucher.getVoucherId() == null) {
            return Result.fail(VoucherConstants.VOUCHER_ID_NOT_NULL);
        }

        // ========== 3. 校验秒杀券是否存在 ==========
        SeckillVoucher existSeckillVoucher = getById(seckillVoucher.getVoucherId());
        if (existSeckillVoucher == null) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_NOT_EXIST);
        }

        // ========== 4. 校验秒杀券库存（如果传入了） ==========
        if (seckillVoucher.getStock() != null && seckillVoucher.getStock() < 0) {
            return Result.fail(VoucherConstants.SECKILL_STOCK_MUST_NON_NEGATIVE);
        }

        // ========== 5. 校验秒杀开始时间和结束时间（如果传入了） ==========
        // 校验开始时间必须早于结束时间（如果都传入了）
        LocalDateTime beginTime = seckillVoucher.getBeginTime() != null
                ? seckillVoucher.getBeginTime() : existSeckillVoucher.getBeginTime();
        LocalDateTime endTime = seckillVoucher.getEndTime() != null
                ? seckillVoucher.getEndTime() : existSeckillVoucher.getEndTime();
        if (beginTime.isAfter(endTime)) {
            return Result.fail(VoucherConstants.BEGIN_TIME_MUST_BEFORE_END_TIME);
        }

        // ========== 6. 设置更新时间 ==========
        seckillVoucher.setUpdateTime(LocalDateTime.now());

        // ========== 7. 更新秒杀券信息 ==========
        boolean success = updateById(seckillVoucher);
        if (!success) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_UPDATE_FAIL);
        }

        log.info(VoucherConstants.SECKILL_VOUCHER_UPDATE_SUCCESS, seckillVoucher.getVoucherId());
        return Result.ok();
    }

    /**
     * 根据ID查询秒杀券信息
     * 业务逻辑：
     * 1. 校验优惠券ID不能为空
     * 2. 查询秒杀券信息
     * 3. 返回结果
     *
     * @param voucherId 优惠券ID（秒杀券主键）
     * @return 秒杀券信息
     */
    @Override
    public Result getSeckillVoucherById(Long voucherId) {
        // ========== 1. 校验优惠券ID不能为空 ==========
        if (voucherId == null) {
            return Result.fail(VoucherConstants.VOUCHER_ID_NOT_NULL);
        }

        // ========== 2. 查询秒杀券信息 ==========
        SeckillVoucher seckillVoucher = getById(voucherId);

        // ========== 3. 返回结果 ==========
        if (seckillVoucher == null) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_NOT_EXIST);
        }

        return Result.ok(seckillVoucher);
    }

    /**
     * 分页查询秒杀券信息
     * 业务逻辑：
     * 1. 处理默认分页参数
     * 2. 限制最大分页数
     * 3. 构建查询条件
     * 4. 执行分页查询
     *
     * @param current 当前页码
     * @param size    每页大小
     * @return 分页结果，包含数据列表和总记录数
     */
    @Override
    public Result querySeckillVoucherPage(Integer current, Integer size) {
        // ========== 1. 处理默认分页参数 ==========
        if (current == null || current <= 0) {
            current = VoucherConstants.DEFAULT_PAGE_CURRENT;
        }

        // ========== 2. 限制最大分页数 ==========
        if (size == null || size <= 0) {
            size = VoucherConstants.DEFAULT_PAGE_SIZE;
        } else if (size > VoucherConstants.MAX_PAGE_SIZE) {
            // 超过最大限制时，自动限制为最大分页数
            size = VoucherConstants.MAX_PAGE_SIZE;
        }

        // ========== 3. 构建查询条件 ==========
        Page<SeckillVoucher> page = new Page<>(current, size);
        LambdaQueryWrapper<SeckillVoucher> queryWrapper = new LambdaQueryWrapper<>();

        // 按更新时间降序排序，确保最新的记录在前
        queryWrapper.orderByDesc(SeckillVoucher::getUpdateTime);

        // ========== 4. 执行分页查询 ==========
        page(page, queryWrapper);

        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * 根据ID删除秒杀券
     * 业务逻辑：
     * 1. 校验优惠券ID不能为空
     * 2. 查询秒杀券是否存在
     * 3. 执行删除操作
     *
     * @param voucherId 优惠券ID（秒杀券主键）
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteSeckillVoucherById(Long voucherId) {
        // ========== 1. 校验优惠券ID不能为空 ==========
        if (voucherId == null) {
            return Result.fail(VoucherConstants.VOUCHER_ID_NOT_NULL);
        }

        // ========== 2. 查询秒杀券是否存在 ==========
        // 删除前必须先查询确认秒杀券存在
        SeckillVoucher seckillVoucher = getById(voucherId);
        if (seckillVoucher == null) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_NOT_EXIST);
        }

        // ========== 3. 执行删除操作 ==========
        boolean success = removeById(voucherId);
        if (!success) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_DELETE_FAIL);
        }

        log.info(VoucherConstants.SECKILL_VOUCHER_DELETE_SUCCESS, voucherId);
        return Result.ok();
    }

    /**
     * 批量删除秒杀券
     * 业务逻辑：
     * 1. 校验优惠券ID列表不能为空
     * 2. 对ID列表进行去重，避免重复操作
     * 3. 批量查询所有秒杀券，校验是否存在
     * 4. 执行批量删除操作
     *
     * @param voucherIds 优惠券ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteSeckillVoucherByIds(List<Long> voucherIds) {
        // ========== 1. 校验优惠券ID列表不能为空 ==========
        if (CollUtil.isEmpty(voucherIds)) {
            return Result.fail(VoucherConstants.VOUCHER_ID_LIST_NOT_NULL);
        }

        // ========== 2. 对ID列表进行去重，避免重复操作 ==========
        Set<Long> distinctIds = voucherIds.stream()
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (distinctIds.isEmpty()) {
            return Result.fail(VoucherConstants.VOUCHER_ID_LIST_NOT_NULL);
        }

        // ========== 3. 批量查询所有秒杀券，校验是否存在 ==========
        // 使用listByIds一次性查询所有秒杀券，替代循环单条查询
        List<SeckillVoucher> existSeckillVouchers = listByIds(distinctIds);
        Set<Long> existIds = existSeckillVouchers.stream()
                .map(SeckillVoucher::getVoucherId)
                .collect(Collectors.toSet());

        // 找出不存在的ID
        Optional<Long> nonExistId = distinctIds.stream()
                .filter(id -> !existIds.contains(id))
                .findFirst();
        if (nonExistId.isPresent()) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_NOT_EXIST + "，优惠券ID：" + nonExistId.get());
        }

        // ========== 4. 执行批量删除操作 ==========
        // 使用MyBatis-Plus的removeByIds方法进行批量删除
        boolean success = removeByIds(distinctIds);
        if (!success) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_DELETE_FAIL);
        }

        log.info(VoucherConstants.SECKILL_VOUCHER_DELETE_SUCCESS, distinctIds.size());
        return Result.ok();
    }

    /**
     * 扣减秒杀券库存
     * 用于秒杀下单时扣减库存
     * 业务逻辑：
     * 1. 校验优惠券ID不能为空
     * 2. 校验秒杀券是否存在
     * 3. 校验库存是否充足
     * 4. 使用乐观锁扣减库存
     *
     * @param voucherId 优惠券ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result decreaseStock(Long voucherId) {
        // ========== 1. 校验优惠券ID不能为空 ==========
        if (voucherId == null) {
            return Result.fail(VoucherConstants.VOUCHER_ID_NOT_NULL);
        }

        // ========== 2. 校验秒杀券是否存在 ==========
        SeckillVoucher seckillVoucher = getById(voucherId);
        if (seckillVoucher == null) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_NOT_EXIST);
        }

        // ========== 3. 校验库存是否充足 ==========
        if (seckillVoucher.getStock() <= 0) {
            return Result.fail("秒杀券库存不足");
        }

        // ========== 4. 使用乐观锁扣减库存 ==========
        // 使用LambdaUpdateWrapper构建更新条件
        LambdaUpdateWrapper<SeckillVoucher> updateWrapper = new LambdaUpdateWrapper<>();
        // 条件：voucherId = ? AND stock > 0
        updateWrapper.eq(SeckillVoucher::getVoucherId, voucherId)
                .gt(SeckillVoucher::getStock, 0)
                // 设置：stock = stock - 1
                .setSql("stock = stock - 1")
                // 设置更新时间
                .set(SeckillVoucher::getUpdateTime, LocalDateTime.now());

        // 执行更新
        boolean success = update(updateWrapper);
        if (!success) {
            return Result.fail("扣减库存失败，可能库存已不足");
        }

        log.info("秒杀券库存扣减成功，优惠券ID：{}", voucherId);
        return Result.ok();
    }

    /**
     * 查询秒杀券列表
     * 查询所有可用的秒杀券
     * 业务逻辑：
     * 1. 构建查询条件
     * 2. 执行查询
     *
     * @return 秒杀券列表
     */
    @Override
    public Result querySeckillVoucherList() {
        // ========== 1. 构建查询条件 ==========
        LambdaQueryWrapper<SeckillVoucher> queryWrapper = new LambdaQueryWrapper<>();

        // 查询库存大于0的秒杀券
        queryWrapper.gt(SeckillVoucher::getStock, 0);

        // 按更新时间降序排序
        queryWrapper.orderByDesc(SeckillVoucher::getUpdateTime);

        // ========== 2. 执行查询 ==========
        List<SeckillVoucher> seckillVouchers = list(queryWrapper);

        return Result.ok(seckillVouchers);
    }
}
