package com.cclg.dianping.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.constant.VoucherConstants;
import com.cclg.dianping.domain.SeckillVoucher;
import com.cclg.dianping.domain.Shop;
import com.cclg.dianping.domain.Voucher;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.handler.VoucherHandler;
import com.cclg.dianping.handler.VoucherHandlerFactory;
import com.cclg.dianping.mapper.VoucherMapper;
import com.cclg.dianping.service.ISeckillVoucherService;
import com.cclg.dianping.service.IShopService;
import com.cclg.dianping.service.IVoucherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.function.Function;

import static com.cclg.dianping.utils.RedisConstants.CACHE_NULL_VOUCHER_KEY;
import static com.cclg.dianping.utils.RedisConstants.CACHE_NULL_TTL_SECONDS;
import static com.cclg.dianping.utils.RedisConstants.CACHE_VOUCHER_KEY;
import static com.cclg.dianping.utils.RedisConstants.CACHE_VOUCHER_TTL_MINUTES;

/**
 * 优惠券服务实现类
 * 实现优惠券相关的业务逻辑
 * 使用策略模式处理不同类型的优惠券
 *
 * @author system
 */
@Slf4j
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    /**
     * 商铺服务，用于校验商铺是否存在
     */
    @Resource
    private IShopService shopService;

    /**
     * 秒杀券服务，用于操作秒杀券信息
     */
    @Resource
    private ISeckillVoucherService seckillVoucherService;

    /**
     * Redis模板，用于缓存操作
     */
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * JSON序列化工具
     */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 优惠券处理器工厂
     * 用于根据类型获取对应的处理器
     */
    @Resource
    private VoucherHandlerFactory voucherHandlerFactory;

    /**
     * 新增优惠券
     * 业务逻辑：
     * 1. 校验通用参数（标题、商铺、金额等）
     * 2. 校验优惠券类型
     * 3. 获取对应类型的处理器
     * 4. 使用处理器进行类型特定的校验
     * 5. 保存优惠券基本信息
     * 6. 使用处理器执行类型特定的后续操作
     *
     * @param voucher 优惠券信息
     * @return 操作结果，成功返回优惠券ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveVoucher(Voucher voucher) {
        // ========== 1. 校验通用参数 ==========
        String validateResult = validateCommonSaveParams(voucher);
        if (validateResult != null) {
            return Result.fail(validateResult);
        }

        // ========== 2. 处理优惠券类型 ==========
        Integer type = voucher.getType();
        if (type == null) {
            // 默认为普通券
            type = VoucherConstants.VOUCHER_TYPE_NORMAL;
            voucher.setType(type);
        }

        // ========== 3. 获取对应类型的处理器 ==========
        VoucherHandler handler = voucherHandlerFactory.getHandler(type);
        if (handler == null) {
            return Result.fail(VoucherConstants.VOUCHER_TYPE_ERROR);
        }

        // ========== 4. 使用处理器进行类型特定的校验 ==========
        String typeValidateResult = handler.validateSave(voucher);
        if (typeValidateResult != null) {
            return Result.fail(typeValidateResult);
        }

        // ========== 5. 保存优惠券基本信息 ==========
        LocalDateTime now = LocalDateTime.now();
        voucher.setCreateTime(now);
        voucher.setUpdateTime(now);

        boolean success = save(voucher);
        if (!success) {
            log.error("保存优惠券基本信息失败，优惠券标题：{}", voucher.getTitle());
            throw new RuntimeException(VoucherConstants.VOUCHER_CREATE_FAIL);
        }

        // ========== 6. 使用处理器执行类型特定的后续操作 ==========
        handler.afterSave(voucher);

        log.info(VoucherConstants.VOUCHER_CREATE_SUCCESS, voucher.getId());
        return Result.ok(voucher.getId());
    }

    /**
     * 校验新增优惠券的通用参数
     *
     * @param voucher 优惠券信息
     * @return 校验结果，成功返回null，失败返回错误信息
     */
    private String validateCommonSaveParams(Voucher voucher) {
        // 校验优惠券信息不能为空
        if (voucher == null) {
            return VoucherConstants.VOUCHER_INFO_NOT_NULL;
        }

        // 校验优惠券标题不能为空
        if (StrUtil.isBlank(voucher.getTitle())) {
            return VoucherConstants.VOUCHER_TITLE_NOT_NULL;
        }

        // 校验商铺ID不能为空
        if (voucher.getShopId() == null) {
            return VoucherConstants.SHOP_ID_NOT_NULL;
        }

        // 校验商铺是否存在
        if (!checkShopExist(voucher.getShopId())) {
            return VoucherConstants.SHOP_NOT_EXIST;
        }

        // 校验支付金额
        if (voucher.getPayValue() == null) {
            return VoucherConstants.PAY_VALUE_NOT_NULL;
        }
        if (voucher.getPayValue() <= 0) {
            return VoucherConstants.PAY_VALUE_MUST_POSITIVE;
        }

        // 校验抵扣金额
        if (voucher.getActualValue() == null) {
            return VoucherConstants.ACTUAL_VALUE_NOT_NULL;
        }
        if (voucher.getActualValue() <= 0) {
            return VoucherConstants.ACTUAL_VALUE_MUST_POSITIVE;
        }

        return null;
    }

    /**
     * 更新优惠券
     * 业务逻辑（修复事务回滚问题：先校验所有参数，再执行数据库操作）：
     * 1. 校验通用参数（ID是否存在等）
     * 2. 确定要使用的优惠券类型
     * 3. 获取对应类型的处理器
     * 4. 使用处理器进行类型特定的校验（关键：在校验阶段就处理库存、时间等校验）
     * 5. 更新优惠券基本信息
     * 6. 使用处理器执行类型特定的后续操作
     *
     * @param voucher 优惠券信息
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateVoucher(Voucher voucher) {
        // ========== 1. 校验通用参数 ==========
        String validateResult = validateCommonUpdateParams(voucher);
        if (validateResult != null) {
            return Result.fail(validateResult);
        }

        // 查询已存在的优惠券信息
        Voucher existVoucher = getById(voucher.getId());
        if (existVoucher == null) {
            return Result.fail(VoucherConstants.VOUCHER_NOT_EXIST);
        }

        // ========== 2. 确定要使用的优惠券类型 ==========
        // 如果传入了type，使用传入的type；否则使用已存在的type
        Integer targetType = voucher.getType() != null ? voucher.getType() : existVoucher.getType();

        // ========== 3. 获取对应类型的处理器 ==========
        VoucherHandler handler = voucherHandlerFactory.getHandler(targetType);
        if (handler == null) {
            return Result.fail(VoucherConstants.VOUCHER_TYPE_ERROR);
        }

        // ========== 4. 使用处理器进行类型特定的校验 ==========
        // 关键修复：在执行数据库操作之前，先进行所有类型特定的校验
        // 包括：库存不能为负数、时间逻辑等
        String typeValidateResult = handler.validateUpdate(voucher, existVoucher);
        if (typeValidateResult != null) {
            return Result.fail(typeValidateResult);
        }

        // ========== 5. 更新优惠券基本信息 ==========
        voucher.setUpdateTime(LocalDateTime.now());

        boolean success = updateById(voucher);
        if (!success) {
            log.error("更新优惠券基本信息失败，优惠券ID：{}", voucher.getId());
            throw new RuntimeException(VoucherConstants.VOUCHER_UPDATE_FAIL);
        }

        // ========== 6. 使用处理器执行类型特定的后续操作 ==========
        handler.afterUpdate(voucher, existVoucher);

        log.info(VoucherConstants.VOUCHER_UPDATE_SUCCESS, voucher.getId());

        // ========== 7. 删除缓存，保证一致性 ==========
        deleteVoucherCache(voucher.getId());

        return Result.ok();
    }

    /**
     * 校验更新优惠券的通用参数
     *
     * @param voucher 优惠券信息
     * @return 校验结果，成功返回null，失败返回错误信息
     */
    private String validateCommonUpdateParams(Voucher voucher) {
        // 校验优惠券ID不能为空
        if (voucher == null || voucher.getId() == null) {
            return VoucherConstants.VOUCHER_ID_NOT_NULL;
        }

        // 校验商铺是否存在（如果传入了shopId）
        if (voucher.getShopId() != null && !checkShopExist(voucher.getShopId())) {
            return VoucherConstants.SHOP_NOT_EXIST;
        }

        // 校验支付金额（如果传入了）
        if (voucher.getPayValue() != null && voucher.getPayValue() <= 0) {
            return VoucherConstants.PAY_VALUE_MUST_POSITIVE;
        }

        // 校验抵扣金额（如果传入了）
        if (voucher.getActualValue() != null && voucher.getActualValue() <= 0) {
            return VoucherConstants.ACTUAL_VALUE_MUST_POSITIVE;
        }

        // 校验优惠券类型（如果传入了）
        if (voucher.getType() != null && !voucherHandlerFactory.supports(voucher.getType())) {
            return VoucherConstants.VOUCHER_TYPE_ERROR;
        }

        return null;
    }

    /**
     * 根据ID查询优惠券信息
     * 业务逻辑：
     * 1. 校验优惠券ID不能为空
     * 2. 先查询Redis缓存
     * 3. 缓存命中则直接返回
     * 4. 缓存未命中则查询数据库
     * 5. 数据库不存在则缓存空值（防止缓存穿透）
     * 6. 数据库存在则关联查询秒杀券信息和商铺信息
     * 7. 写入缓存后返回
     *
     * @param id 优惠券ID
     * @return 优惠券信息
     */
    @Override
    public Result getVoucherById(Long id) {
        // ========== 1. 校验优惠券ID不能为空 ==========
        if (id == null) {
            return Result.fail(VoucherConstants.VOUCHER_ID_NOT_NULL);
        }

        // ========== 2. 从Redis缓存中查询 ==========
        String key = CACHE_VOUCHER_KEY + id;
        String nullKey = CACHE_NULL_VOUCHER_KEY + id;

        // 先检查是否为空值缓存（防止缓存穿透）
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(nullKey))) {
            return Result.fail(VoucherConstants.VOUCHER_NOT_EXIST);
        }

        // 查询缓存
        String voucherJson = stringRedisTemplate.opsForValue().get(key);

        // ========== 3. 缓存命中，直接返回 ==========
        if (StrUtil.isNotBlank(voucherJson)) {
            try {
                Voucher voucher = objectMapper.readValue(voucherJson, Voucher.class);
                // 关联查询秒杀券信息和商铺信息
                setSeckillInfo(voucher);
                setShopInfo(voucher);
                return Result.ok(voucher);
            } catch (JsonProcessingException e) {
                log.error("解析优惠券缓存数据失败，优惠券ID：{}", id, e);
                // 解析失败，继续从数据库查询
            }
        }

        // ========== 4. 缓存未命中，查询数据库 ==========
        Voucher voucher = getById(id);

        // ========== 5. 数据库中不存在，缓存空值防止缓存穿透 ==========
        if (voucher == null) {
            // 缓存空值，设置30秒过期时间
            stringRedisTemplate.opsForValue().set(
                    nullKey,
                    "",
                    CACHE_NULL_TTL_SECONDS,
                    TimeUnit.SECONDS
            );
            return Result.fail(VoucherConstants.VOUCHER_NOT_EXIST);
        }

        // ========== 6. 关联查询秒杀券信息和商铺信息 ==========
        setSeckillInfo(voucher);
        setShopInfo(voucher);

        // ========== 7. 写入Redis缓存 ==========
        try {
            String json = objectMapper.writeValueAsString(voucher);
            stringRedisTemplate.opsForValue().set(
                    key,
                    json,
                    CACHE_VOUCHER_TTL_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (JsonProcessingException e) {
            log.error("序列化优惠券数据到缓存失败，优惠券ID：{}", id, e);
            // 序列化失败不影响正常返回
        }

        return Result.ok(voucher);
    }

    /**
     * 分页查询优惠券信息
     * 支持按商铺ID和优惠券类型筛选
     * 业务逻辑：
     * 1. 处理默认分页参数
     * 2. 限制最大分页数
     * 3. 构建查询条件（支持商铺ID和类型筛选）
     * 4. 执行分页查询
     * 5. 批量关联查询秒杀券信息和商铺信息
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param shopId  商铺ID（可选，按商铺筛选）
     * @param type    优惠券类型（可选，0-普通券，1-秒杀券）
     * @return 分页结果，包含数据列表和总记录数
     */
    @Override
    public Result queryVoucherPage(Integer current, Integer size, Long shopId, Integer type) {
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
        Page<Voucher> page = new Page<>(current, size);
        LambdaQueryWrapper<Voucher> queryWrapper = new LambdaQueryWrapper<>();

        // 按商铺ID筛选（如果有传入shopId参数）
        if (shopId != null) {
            queryWrapper.eq(Voucher::getShopId, shopId);
        }

        // 按优惠券类型筛选（如果有传入type参数）
        if (type != null) {
            queryWrapper.eq(Voucher::getType, type);
        }

        // 按更新时间降序排序，确保最新的记录在前
        queryWrapper.orderByDesc(Voucher::getUpdateTime);

        // ========== 4. 执行分页查询 ==========
        page(page, queryWrapper);

        // ========== 5. 批量关联查询秒杀券信息和商铺信息 ==========
        // 使用批量查询替代循环单条查询，减少数据库访问次数
        setSeckillInfoBatch(page.getRecords());
        setShopInfoBatch(page.getRecords());

        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * 根据ID删除优惠券
     * 业务逻辑：
     * 1. 校验优惠券ID不能为空
     * 2. 查询优惠券是否存在
     * 3. 删除关联的秒杀券信息
     * 4. 执行删除操作
     *
     * @param id 优惠券ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteVoucherById(Long id) {
        // ========== 1. 校验优惠券ID不能为空 ==========
        if (id == null) {
            return Result.fail(VoucherConstants.VOUCHER_ID_NOT_NULL);
        }

        // ========== 2. 查询优惠券是否存在 ==========
        // 删除前必须先查询确认优惠券存在
        Voucher voucher = getById(id);
        if (voucher == null) {
            return Result.fail(VoucherConstants.VOUCHER_NOT_EXIST);
        }

        // ========== 3. 删除关联的秒杀券信息 ==========
        // 先尝试删除秒杀券信息（如果存在）
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);
        if (seckillVoucher != null) {
            boolean seckillSuccess = seckillVoucherService.removeById(id);
            if (!seckillSuccess) {
                log.error("删除秒杀券信息失败，优惠券ID：{}", id);
                throw new RuntimeException(VoucherConstants.SECKILL_VOUCHER_DELETE_FAIL);
            }
        }

        // ========== 4. 执行删除操作 ==========
        boolean success = removeById(id);
        if (!success) {
            log.error("删除优惠券基本信息失败，优惠券ID：{}", id);
            throw new RuntimeException(VoucherConstants.VOUCHER_DELETE_FAIL);
        }

        log.info(VoucherConstants.VOUCHER_DELETE_SUCCESS, id);

        // ========== 5. 删除缓存，保证一致性 ==========
        deleteVoucherCache(id);

        return Result.ok();
    }

    /**
     * 批量删除优惠券
     * 业务逻辑：
     * 1. 校验优惠券ID列表不能为空
     * 2. 对ID列表进行去重，避免重复操作
     * 3. 批量查询所有优惠券，校验是否存在
     * 4. 批量删除关联的秒杀券信息
     * 5. 执行批量删除操作
     *
     * @param ids 优惠券ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteVoucherByIds(List<Long> ids) {
        // ========== 1. 校验优惠券ID列表不能为空 ==========
        if (CollUtil.isEmpty(ids)) {
            return Result.fail(VoucherConstants.VOUCHER_ID_LIST_NOT_NULL);
        }

        // ========== 2. 对ID列表进行去重，避免重复操作 ==========
        Set<Long> distinctIds = ids.stream()
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (distinctIds.isEmpty()) {
            return Result.fail(VoucherConstants.VOUCHER_ID_LIST_NOT_NULL);
        }

        // ========== 3. 批量查询所有优惠券，校验是否存在 ==========
        // 使用listByIds一次性查询所有优惠券，替代循环单条查询
        List<Voucher> existVouchers = listByIds(distinctIds);
        Set<Long> existIds = existVouchers.stream()
                .map(Voucher::getId)
                .collect(Collectors.toSet());

        // 找出不存在的ID
        Optional<Long> nonExistId = distinctIds.stream()
                .filter(id -> !existIds.contains(id))
                .findFirst();
        if (nonExistId.isPresent()) {
            return Result.fail(VoucherConstants.VOUCHER_NOT_EXIST + "，优惠券ID：" + nonExistId.get());
        }

        // ========== 4. 批量删除关联的秒杀券信息 ==========
        // 先查询所有存在的秒杀券
        List<SeckillVoucher> existSeckillVouchers = seckillVoucherService.listByIds(distinctIds);
        if (CollUtil.isNotEmpty(existSeckillVouchers)) {
            // 提取秒杀券ID列表
            List<Long> seckillIds = existSeckillVouchers.stream()
                    .map(SeckillVoucher::getVoucherId)
                    .collect(Collectors.toList());

            // 批量删除秒杀券
            boolean seckillSuccess = seckillVoucherService.removeByIds(seckillIds);
            if (!seckillSuccess) {
                log.error("批量删除秒杀券信息失败");
                throw new RuntimeException(VoucherConstants.SECKILL_VOUCHER_DELETE_FAIL);
            }
        }

        // ========== 5. 执行批量删除操作 ==========
        // 使用MyBatis-Plus的removeByIds方法进行批量删除
        boolean success = removeByIds(distinctIds);
        if (!success) {
            log.error("批量删除优惠券基本信息失败，优惠券数量：{}", distinctIds.size());
            throw new RuntimeException(VoucherConstants.VOUCHER_BATCH_DELETE_FAIL);
        }

        log.info(VoucherConstants.VOUCHER_BATCH_DELETE_SUCCESS, distinctIds.size());

        // ========== 6. 批量删除缓存，保证一致性 ==========
        for (Long id : distinctIds) {
            deleteVoucherCache(id);
        }

        return Result.ok();
    }

    /**
     * 根据商铺ID查询优惠券列表
     * 查询该商铺下的所有优惠券
     * 业务逻辑：
     * 1. 校验商铺ID不能为空
     * 2. 构建查询条件
     * 3. 执行查询
     * 4. 批量关联查询秒杀券信息
     *
     * @param shopId 商铺ID
     * @return 优惠券列表
     */
    @Override
    public Result queryVoucherByShopId(Long shopId) {
        // ========== 1. 校验商铺ID不能为空 ==========
        if (shopId == null) {
            return Result.fail(VoucherConstants.SHOP_ID_NOT_NULL);
        }

        // ========== 2. 构建查询条件 ==========
        LambdaQueryWrapper<Voucher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Voucher::getShopId, shopId);
        queryWrapper.orderByDesc(Voucher::getUpdateTime);

        // ========== 3. 执行查询 ==========
        List<Voucher> vouchers = list(queryWrapper);

        // ========== 4. 批量关联查询秒杀券信息 ==========
        setSeckillInfoBatch(vouchers);

        return Result.ok(vouchers);
    }

    /**
     * 检查商铺是否存在
     *
     * @param shopId 商铺ID
     * @return true-存在，false-不存在
     */
    private boolean checkShopExist(Long shopId) {
        if (shopId == null) {
            return false;
        }
        Shop shop = shopService.getById(shopId);
        return shop != null;
    }

    /**
     * 为单个优惠券设置关联的秒杀券信息
     * 适用于单条查询的场景
     *
     * @param voucher 优惠券对象
     */
    private void setSeckillInfo(Voucher voucher) {
        if (voucher == null) {
            return;
        }
        // 只有秒杀券才需要关联查询秒杀券信息
        if (VoucherConstants.VOUCHER_TYPE_SECKILL.equals(voucher.getType())) {
            SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucher.getId());
            if (seckillVoucher != null) {
                voucher.setStock(seckillVoucher.getStock());
                voucher.setBeginTime(seckillVoucher.getBeginTime());
                voucher.setEndTime(seckillVoucher.getEndTime());
            }
        }
    }

    /**
     * 为单个优惠券设置关联的商铺信息
     * 适用于单条查询的场景
     *
     * @param voucher 优惠券对象
     */
    private void setShopInfo(Voucher voucher) {
        if (voucher == null || voucher.getShopId() == null) {
            return;
        }
        // 直接设置shopId，不做额外查询
        // 如果需要返回完整的商铺信息，可以在这里查询并设置
    }

    /**
     * 批量为优惠券列表设置关联的秒杀券信息
     * 使用批量查询替代循环单条查询，减少数据库访问次数
     *
     * @param vouchers 优惠券列表
     */
    private void setSeckillInfoBatch(List<Voucher> vouchers) {
        if (CollUtil.isEmpty(vouchers)) {
            return;
        }

        // ========== 1. 收集所有秒杀券的ID ==========
        Set<Long> seckillIds = vouchers.stream()
                .filter(v -> VoucherConstants.VOUCHER_TYPE_SECKILL.equals(v.getType()))
                .map(Voucher::getId)
                .collect(Collectors.toSet());

        if (seckillIds.isEmpty()) {
            return;
        }

        // ========== 2. 批量查询所有秒杀券信息 ==========
        List<SeckillVoucher> seckillVouchers = seckillVoucherService.listByIds(seckillIds);

        // ========== 3. 转为Map，方便快速查找 ==========
        Map<Long, SeckillVoucher> seckillVoucherMap = seckillVouchers.stream()
                .collect(Collectors.toMap(SeckillVoucher::getVoucherId, Function.identity()));

        // ========== 4. 为每个秒杀券设置关联的秒杀信息 ==========
        for (Voucher voucher : vouchers) {
            if (VoucherConstants.VOUCHER_TYPE_SECKILL.equals(voucher.getType())) {
                SeckillVoucher seckillVoucher = seckillVoucherMap.get(voucher.getId());
                if (seckillVoucher != null) {
                    voucher.setStock(seckillVoucher.getStock());
                    voucher.setBeginTime(seckillVoucher.getBeginTime());
                    voucher.setEndTime(seckillVoucher.getEndTime());
                }
            }
        }
    }

    /**
     * 批量为优惠券列表设置关联的商铺信息
     * 使用批量查询替代循环单条查询，减少数据库访问次数
     *
     * @param vouchers 优惠券列表
     */
    private void setShopInfoBatch(List<Voucher> vouchers) {
        // 目前只需要shopId，不需要额外查询商铺信息
        // 如果需要返回完整的商铺信息，可以在这里批量查询并设置
    }

    /**
     * 删除优惠券缓存
     * 包含优惠券数据缓存和空值缓存
     *
     * @param id 优惠券ID
     */
    private void deleteVoucherCache(Long id) {
        if (id == null) {
            return;
        }
        String key = CACHE_VOUCHER_KEY + id;
        String nullKey = CACHE_NULL_VOUCHER_KEY + id;
        try {
            stringRedisTemplate.delete(key);
            stringRedisTemplate.delete(nullKey);
            log.info("优惠券缓存已删除，优惠券ID：{}", id);
        } catch (Exception e) {
            log.error("删除优惠券缓存失败，优惠券ID：{}", id, e);
        }
    }
}
