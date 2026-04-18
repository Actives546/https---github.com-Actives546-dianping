package com.cclg.dianping.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.constant.VoucherConstants;
import com.cclg.dianping.domain.SeckillVoucher;
import com.cclg.dianping.domain.Voucher;
import com.cclg.dianping.domain.VoucherOrder;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.mapper.VoucherOrderMapper;
import com.cclg.dianping.service.ISeckillVoucherService;
import com.cclg.dianping.service.IVoucherOrderService;
import com.cclg.dianping.service.IVoucherService;
import com.cclg.dianping.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.aop.framework.AopContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static com.cclg.dianping.utils.RedisConstants.LOCK_ORDER_KEY;

/**
 * 优惠券订单服务实现类
 * 实现秒杀券下单等核心业务功能
 * 采用分布式锁保证一人一单、库存一致性
 *
 * @author system
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    /**
     * 秒杀券服务
     */
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    /**
     * 优惠券服务
     */
    @Resource
    private IVoucherService voucherService;

    /**
     * Redisson客户端，用于分布式锁
     */
    @Resource
    private RedissonClient redissonClient;

    /**
     * Redis模板
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 秒杀库存扣减Lua脚本
     */
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    /**
     * 锁等待时间（秒）
     */
    private static final long LOCK_WAIT_TIME = 0L;

    /**
     * 锁自动释放时间（秒）
     */
    private static final long LOCK_LEASE_TIME = 10L;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    /**
     * 秒杀券下单
     * 业务流程：
     * 1. 校验秒杀券是否存在
     * 2. 校验秒杀是否在有效期内
     * 3. 使用Lua脚本在Redis中预扣减库存和判断一人一单
     * 4. 获取分布式锁（在事务外部，确保锁范围包含整个事务生命周期）
     * 5. 如果Redis校验通过，创建订单（带事务）
     * 6. 事务提交后释放锁
     *
     * @param voucherId 秒杀券ID
     * @return 操作结果，成功返回订单ID
     */
    @Override
    public Result seckillVoucher(Long voucherId) {
        // ========== 1. 校验参数 ==========
        if (voucherId == null) {
            return Result.fail(VoucherConstants.VOUCHER_ID_NOT_NULL);
        }

        // 获取当前用户ID
        com.cclg.dianping.dto.UserDTO user = UserHolder.getUser();
        if (user == null || user.getId() == null) {
            return Result.fail("用户未登录");
        }
        Long userId = user.getId();

        // ========== 2. 校验秒杀券基本信息 ==========
        Voucher voucher = voucherService.getById(voucherId);
        if (voucher == null) {
            return Result.fail(VoucherConstants.VOUCHER_NOT_EXIST);
        }

        // 校验是否为秒杀券
        if (!VoucherConstants.VOUCHER_TYPE_SECKILL.equals(voucher.getType())) {
            return Result.fail("该优惠券不是秒杀券");
        }

        // ========== 3. 校验秒杀时间 ==========
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        if (seckillVoucher == null) {
            return Result.fail(VoucherConstants.SECKILL_VOUCHER_NOT_EXIST);
        }

        LocalDateTime now = LocalDateTime.now();

        // 校验秒杀是否开始
        if (seckillVoucher.getBeginTime().isAfter(now)) {
            return Result.fail("秒杀尚未开始");
        }

        // 校验秒杀是否结束
        if (seckillVoucher.getEndTime().isBefore(now)) {
            return Result.fail("秒杀已经结束");
        }

        // ========== 4. 使用Lua脚本执行Redis预校验和预扣减 ==========
        // 执行Lua脚本，返回结果：
        // 0-成功，1-库存不足，2-重复下单
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString()
        );

        if (result == null) {
            return Result.fail("秒杀失败");
        }

        int r = result.intValue();
        if (r == 1) {
            return Result.fail("库存不足");
        }
        if (r == 2) {
            return Result.fail("您已经抢购过该优惠券");
        }

        // ========== 5. 获取分布式锁（在事务外部获取） ==========
        // 锁的key：order:lock:{userId}:{voucherId}
        String lockKey = LOCK_ORDER_KEY + userId + ":" + voucherId;
        RLock lock = redissonClient.getLock(lockKey);
        Long orderId = null;

        try {
            // 尝试获取锁，设置超时时间防止死锁
            // waitTime: 获取锁的等待时间（0表示不等待）
            // leaseTime: 锁自动释放时间（防止死锁）
            boolean isLocked = lock.tryLock(LOCK_WAIT_TIME, LOCK_LEASE_TIME, TimeUnit.SECONDS);
            if (!isLocked) {
                log.error("获取分布式锁失败，用户ID：{}，优惠券ID：{}", userId, voucherId);
                return Result.fail("系统繁忙，请稍后再试");
            }

            log.info("获取分布式锁成功，用户ID：{}，优惠券ID：{}", userId, voucherId);

            // ========== 6. 创建订单（带事务） ==========
            // 获取代理对象，确保事务生效
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            orderId = proxy.createVoucherOrder(voucherId, userId);

        } catch (InterruptedException e) {
            log.error("获取分布式锁被中断，用户ID：{}，优惠券ID：{}", userId, voucherId, e);
            Thread.currentThread().interrupt();
            return Result.fail("系统异常");
        } finally {
            // ========== 7. 释放锁（事务已经提交后才释放） ==========
            // 检查当前线程是否持有该锁，避免误释放其他线程的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("释放分布式锁成功，用户ID：{}，优惠券ID：{}", userId, voucherId);
            }
        }

        log.info("秒杀成功，订单ID：{}，用户ID：{}，优惠券ID：{}", orderId, userId, voucherId);
        return Result.ok(orderId);
    }

    /**
     * 创建优惠券订单
     * 在事务保护下执行，保证数据一致性
     * 注意：分布式锁在事务外部获取和释放，确保锁范围包含整个事务生命周期
     * 业务流程：
     * 1. 双重校验：查询用户是否已经下单
     * 2. 扣减库存（使用乐观锁）
     * 3. 创建订单
     *
     * @param voucherId 秒杀券ID
     * @param userId    用户ID
     * @return 订单ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucherOrder(Long voucherId, Long userId) {
        log.info("开始创建订单，用户ID：{}，优惠券ID：{}", userId, voucherId);

        // ========== 1. 双重校验：查询用户是否已经下单 ==========
        // 虽然Redis已经预校验，但数据库层面再次校验防止数据不一致
        LambdaQueryWrapper<VoucherOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VoucherOrder::getUserId, userId);
        queryWrapper.eq(VoucherOrder::getVoucherId, voucherId);
        VoucherOrder existOrder = getOne(queryWrapper);

        if (existOrder != null) {
            log.warn("用户已经抢购过该优惠券，用户ID：{}，优惠券ID：{}", userId, voucherId);
            throw new RuntimeException("您已经抢购过该优惠券");
        }

        // ========== 2. 扣减库存（使用乐观锁） ==========
        // 使用乐观锁：stock > 0 保证不会超卖
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();

        if (!success) {
            log.error("扣减库存失败，优惠券ID：{}", voucherId);
            throw new RuntimeException("库存不足");
        }

        log.info("扣减库存成功，优惠券ID：{}", voucherId);

        // ========== 3. 创建订单 ==========
        VoucherOrder order = new VoucherOrder();
        // 使用雪花算法生成订单ID
        long orderId = IdUtil.getSnowflake(1, 1).nextId();
        order.setId(orderId);
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        // 订单状态：1-未支付
        order.setStatus(1);
        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);

        save(order);

        log.info("创建订单成功，订单ID：{}", orderId);

        return orderId;
    }
}
