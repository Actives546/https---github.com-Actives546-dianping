package com.cclg.dianping.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.constant.ShopConstants;
import com.cclg.dianping.domain.Shop;
import com.cclg.dianping.domain.ShopType;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.mapper.ShopMapper;
import com.cclg.dianping.service.IShopService;
import com.cclg.dianping.service.IShopTypeService;
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

import static com.cclg.dianping.utils.RedisConstants.CACHE_NULL_SHOP_KEY;
import static com.cclg.dianping.utils.RedisConstants.CACHE_NULL_TTL;
import static com.cclg.dianping.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.cclg.dianping.utils.RedisConstants.CACHE_SHOP_TTL;

/**
 * 商铺服务实现类
 * 实现商铺相关的业务逻辑
 *
 * @author system
 */
@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private IShopTypeService shopTypeService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 新增商铺
     * 业务逻辑：
     * 1. 校验商铺信息不能为空
     * 2. 校验商铺名称不能为空
     * 3. 校验商铺名称是否已存在
     * 4. 校验商铺类型是否存在（如果传入了typeId）
     * 5. 设置创建时间和更新时间
     * 6. 保存商铺信息
     *
     * @param shop 商铺信息
     * @return 操作结果，成功返回商铺ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveShop(Shop shop) {
        // ========== 1. 校验商铺信息不能为空 ==========
        if (shop == null) {
            return Result.fail(ShopConstants.SHOP_INFO_NOT_NULL);
        }

        // ========== 2. 校验商铺名称不能为空 ==========
        if (StrUtil.isBlank(shop.getName())) {
            return Result.fail(ShopConstants.SHOP_NAME_NOT_NULL);
        }

        // ========== 3. 校验商铺名称是否已存在 ==========
        if (checkShopNameExist(shop.getName(), null)) {
            return Result.fail(ShopConstants.SHOP_NAME_EXIST);
        }

        // ========== 4. 校验商铺类型是否存在 ==========
        // 如果传入了typeId，则校验该类型是否存在
        if (shop.getTypeId() != null && !checkShopTypeExist(shop.getTypeId())) {
            return Result.fail(ShopConstants.SHOP_TYPE_NOT_EXIST);
        }

        // ========== 5. 设置创建时间和更新时间 ==========
        // 使用统一的时间变量，保证创建时间和更新时间一致
        LocalDateTime now = LocalDateTime.now();
        shop.setCreateTime(now);
        shop.setUpdateTime(now);

        // ========== 6. 保存商铺信息 ==========
        boolean success = save(shop);
        if (success) {
            log.info(ShopConstants.SHOP_CREATE_SUCCESS, shop.getId());
            return Result.ok(shop.getId());
        }

        return Result.fail(ShopConstants.SHOP_CREATE_FAIL);
    }

    /**
     * 更新商铺信息
     * 业务逻辑：
     * 1. 校验商铺ID不能为空
     * 2. 校验商铺是否存在
     * 3. 校验商铺名称不能是空串（如果传入了名称）
     * 4. 校验商铺名称是否已存在（排除自身）
     * 5. 校验商铺类型是否存在（如果传入了typeId）
     * 6. 设置更新时间
     * 7. 更新商铺信息
     *
     * @param shop 商铺信息
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateShop(Shop shop) {
        // ========== 1. 校验商铺ID不能为空 ==========
        if (shop == null || shop.getId() == null) {
            return Result.fail(ShopConstants.SHOP_ID_NOT_NULL);
        }

        // ========== 2. 校验商铺是否存在 ==========
        Shop existShop = getById(shop.getId());
        if (existShop == null) {
            return Result.fail(ShopConstants.SHOP_NOT_EXIST);
        }

        // ========== 3. 校验商铺名称不能是空串 ==========
        // 如果传入了名称参数，校验不能是空串或空白字符串
        if (shop.getName() != null && StrUtil.isBlank(shop.getName())) {
            return Result.fail(ShopConstants.SHOP_NAME_NOT_NULL);
        }

        // ========== 4. 校验商铺名称是否已存在（排除自身） ==========
        // 只有当传入了有效名称时才检查唯一性
        if (StrUtil.isNotBlank(shop.getName()) && checkShopNameExist(shop.getName(), shop.getId())) {
            return Result.fail(ShopConstants.SHOP_NAME_EXIST);
        }

        // ========== 5. 校验商铺类型是否存在 ==========
        // 如果传入了typeId，则校验该类型是否存在
        if (shop.getTypeId() != null && !checkShopTypeExist(shop.getTypeId())) {
            return Result.fail(ShopConstants.SHOP_TYPE_NOT_EXIST);
        }

        // ========== 6. 设置更新时间 ==========
        shop.setUpdateTime(LocalDateTime.now());

        // ========== 7. 更新商铺信息 ==========
        boolean success = updateById(shop);
        if (success) {
            log.info(ShopConstants.SHOP_UPDATE_SUCCESS, shop.getId());
            
            // ========== 8. 删除缓存，保证一致性 ==========
            // 采用"先更新数据库，再删除缓存"的策略保证一致性
            deleteShopCache(shop.getId());
            
            return Result.ok();
        }

        return Result.fail(ShopConstants.SHOP_UPDATE_FAIL);
    }

    /**
     * 根据ID查询商铺信息
     * 业务逻辑：
     * 1. 校验商铺ID不能为空
     * 2. 先查询Redis缓存
     * 3. 缓存命中则直接返回
     * 4. 缓存未命中则查询数据库
     * 5. 数据库不存在则缓存空值（防止缓存穿透）
     * 6. 数据库存在则关联查询商铺类型，写入缓存后返回
     *
     * @param id 商铺ID
     * @return 商铺信息
     */
    @Override
    public Result getShopById(Long id) {
        // ========== 1. 校验商铺ID不能为空 ==========
        if (id == null) {
            return Result.fail(ShopConstants.SHOP_ID_NOT_NULL);
        }

        // ========== 2. 从Redis缓存中查询 ==========
        String key = CACHE_SHOP_KEY + id;
        String nullKey = CACHE_NULL_SHOP_KEY + id;
        
        // 先检查是否为空值缓存（防止缓存穿透）
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(nullKey))) {
            return Result.fail(ShopConstants.SHOP_NOT_EXIST);
        }
        
        // 查询缓存
        String shopJson = stringRedisTemplate.opsForValue().get(key);

        // ========== 3. 缓存命中，直接返回 ==========
        if (StrUtil.isNotBlank(shopJson)) {
            try {
                Shop shop = objectMapper.readValue(shopJson, Shop.class);
                // 关联查询商铺类型
                setShopType(shop);
                return Result.ok(shop);
            } catch (JsonProcessingException e) {
                log.error("解析商铺缓存数据失败，商铺ID：{}", id, e);
                // 解析失败，继续从数据库查询
            }
        }

        // ========== 4. 缓存未命中，查询数据库 ==========
        Shop shop = getById(id);

        // ========== 5. 数据库中不存在，缓存空值防止缓存穿透 ==========
        if (shop == null) {
            // 缓存空值，设置30秒过期时间
            stringRedisTemplate.opsForValue().set(
                    nullKey,
                    "",
                    CACHE_NULL_TTL,
                    TimeUnit.SECONDS
            );
            return Result.fail(ShopConstants.SHOP_NOT_EXIST);
        }

        // ========== 6. 关联查询商铺类型 ==========
        setShopType(shop);

        // ========== 7. 写入Redis缓存 ==========
        try {
            String json = objectMapper.writeValueAsString(shop);
            stringRedisTemplate.opsForValue().set(
                    key,
                    json,
                    CACHE_SHOP_TTL,
                    TimeUnit.MINUTES
            );
        } catch (JsonProcessingException e) {
            log.error("序列化商铺数据到缓存失败，商铺ID：{}", id, e);
            // 序列化失败不影响正常返回
        }

        return Result.ok(shop);
    }

    /**
     * 分页查询商铺信息
     * 支持按商铺名称模糊筛选
     * 业务逻辑：
     * 1. 处理默认分页参数
     * 2. 限制最大分页数
     * 3. 构建查询条件（支持名称模糊搜索）
     * 4. 执行分页查询
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param name    商铺名称（可选，模糊匹配）
     * @return 分页结果，包含数据列表和总记录数
     */
    @Override
    public Result queryShopPage(Integer current, Integer size, String name) {
        // ========== 1. 处理默认分页参数 ==========
        if (current == null || current <= 0) {
            current = ShopConstants.DEFAULT_PAGE_CURRENT;
        }

        // ========== 2. 限制最大分页数 ==========
        if (size == null || size <= 0) {
            size = ShopConstants.DEFAULT_PAGE_SIZE;
        } else if (size > ShopConstants.MAX_PAGE_SIZE) {
            // 超过最大限制时，自动限制为最大分页数
            size = ShopConstants.MAX_PAGE_SIZE;
        }

        // ========== 3. 构建查询条件 ==========
        Page<Shop> page = new Page<>(current, size);
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();

        // 按商铺名称模糊筛选（如果有传入name参数）
        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like(Shop::getName, name);
        }

        // 按更新时间降序排序，确保最新的记录在前
        queryWrapper.orderByDesc(Shop::getUpdateTime);

        // ========== 4. 执行分页查询 ==========
        page(page, queryWrapper);

        // ========== 5. 批量关联查询商铺类型 ==========
        // 使用批量查询替代循环单条查询，减少数据库访问次数
        setShopTypeBatch(page.getRecords());

        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * 根据ID删除商铺
     * 业务逻辑：
     * 1. 校验商铺ID不能为空
     * 2. 查询商铺是否存在
     * 3. 执行删除操作
     *
     * @param id 商铺ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopById(Long id) {
        // ========== 1. 校验商铺ID不能为空 ==========
        if (id == null) {
            return Result.fail(ShopConstants.SHOP_ID_NOT_NULL);
        }

        // ========== 2. 查询商铺是否存在 ==========
        // 删除前必须先查询确认商铺存在
        Shop shop = getById(id);
        if (shop == null) {
            return Result.fail(ShopConstants.SHOP_NOT_EXIST);
        }

        // ========== 3. 执行删除操作 ==========
        boolean success = removeById(id);
        if (success) {
            log.info(ShopConstants.SHOP_DELETE_SUCCESS, id);
            
            // ========== 4. 删除缓存，保证一致性 ==========
            deleteShopCache(id);
            
            return Result.ok();
        }

        return Result.fail(ShopConstants.SHOP_DELETE_FAIL);
    }

    /**
     * 批量删除商铺
     * 业务逻辑：
     * 1. 校验商铺ID列表不能为空
     * 2. 对ID列表进行去重，避免重复操作
     * 3. 批量查询所有商铺，校验是否存在
     * 4. 执行批量删除操作
     *
     * @param ids 商铺ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopByIds(List<Long> ids) {
        // ========== 1. 校验商铺ID列表不能为空 ==========
        if (CollUtil.isEmpty(ids)) {
            return Result.fail(ShopConstants.SHOP_ID_LIST_NOT_NULL);
        }
        
        // ========== 2. 对ID列表进行去重，避免重复操作 ==========
        Set<Long> distinctIds = ids.stream()
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        if (distinctIds.isEmpty()) {
            return Result.fail(ShopConstants.SHOP_ID_LIST_NOT_NULL);
        }

        // ========== 3. 批量查询所有商铺，校验是否存在 ==========
        // 使用listByIds一次性查询所有商铺，替代循环单条查询
        List<Shop> existShops = listByIds(distinctIds);
        Set<Long> existIds = existShops.stream()
                .map(Shop::getId)
                .collect(Collectors.toSet());

        // 找出不存在的ID
        Optional<Long> nonExistId = distinctIds.stream()
                .filter(id -> !existIds.contains(id))
                .findFirst();
        if (nonExistId.isPresent()) {
            return Result.fail(ShopConstants.SHOP_NOT_EXIST + "，商铺ID：" + nonExistId.get());
        }

        // ========== 4. 执行批量删除操作 ==========
        // 使用MyBatis-Plus的removeByIds方法进行批量删除
        boolean success = removeByIds(distinctIds);
        if (success) {
            log.info(ShopConstants.SHOP_BATCH_DELETE_SUCCESS, distinctIds.size());
            
            // ========== 5. 批量删除缓存，保证一致性 ==========
            for (Long id : distinctIds) {
                deleteShopCache(id);
            }
            
            return Result.ok();
        }

        return Result.fail(ShopConstants.SHOP_BATCH_DELETE_FAIL);
    }

    /**
     * 为单个商铺设置关联的商铺类型信息
     * 适用于单条查询的场景
     *
     * @param shop 商铺对象
     */
    private void setShopType(Shop shop) {
        if (shop.getTypeId() != null) {
            ShopType shopType = shopTypeService.getById(shop.getTypeId());
            shop.setShopType(shopType);
        }
    }

    /**
     * 批量为商铺列表设置关联的商铺类型信息
     * 使用批量查询替代循环单条查询，减少数据库访问次数
     *
     * @param shops 商铺列表
     */
    private void setShopTypeBatch(List<Shop> shops) {
        if (CollUtil.isEmpty(shops)) {
            return;
        }

        // ========== 1. 收集所有非空的typeId ==========
        Set<Long> typeIds = shops.stream()
                .map(Shop::getTypeId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        if (typeIds.isEmpty()) {
            return;
        }

        // ========== 2. 批量查询所有商铺类型 ==========
        List<ShopType> shopTypes = shopTypeService.listByIds(typeIds);

        // ========== 3. 转为Map，方便快速查找 ==========
        Map<Long, ShopType> shopTypeMap = shopTypes.stream()
                .collect(Collectors.toMap(ShopType::getId, Function.identity()));

        // ========== 4. 为每个商铺设置关联的类型 ==========
        for (Shop shop : shops) {
            if (shop.getTypeId() != null) {
                shop.setShopType(shopTypeMap.get(shop.getTypeId()));
            }
        }
    }

    /**
     * 检查商铺名称是否已存在
     *
     * @param shopName  商铺名称
     * @param excludeId 需要排除的商铺ID（更新时使用，排除自身）
     * @return true-已存在，false-不存在
     */
    private boolean checkShopNameExist(String shopName, Long excludeId) {
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        // 等值查询商铺名称
        queryWrapper.eq(Shop::getName, shopName);

        // 更新时排除自身ID
        if (excludeId != null) {
            queryWrapper.ne(Shop::getId, excludeId);
        }

        // 统计符合条件的记录数，大于0表示已存在
        return count(queryWrapper) > 0;
    }

    /**
     * 检查商铺类型是否存在
     *
     * @param typeId 商铺类型ID
     * @return true-存在，false-不存在
     */
    private boolean checkShopTypeExist(Long typeId) {
        if (typeId == null) {
            return false;
        }
        ShopType shopType = shopTypeService.getById(typeId);
        return shopType != null;
    }
    
    /**
     * 删除商铺缓存
     * 包含商铺数据缓存和空值缓存
     *
     * @param id 商铺ID
     */
    private void deleteShopCache(Long id) {
        if (id == null) {
            return;
        }
        String key = CACHE_SHOP_KEY + id;
        String nullKey = CACHE_NULL_SHOP_KEY + id;
        try {
            stringRedisTemplate.delete(key);
            stringRedisTemplate.delete(nullKey);
            log.info("商铺缓存已删除，商铺ID：{}", id);
        } catch (Exception e) {
            log.error("删除商铺缓存失败，商铺ID：{}", id, e);
        }
    }
}
