package com.cclg.dianping.service.impl;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 新增商铺
     * 业务逻辑：
     * 1. 校验商铺信息不能为空
     * 2. 校验商铺名称不能为空
     * 3. 校验商铺名称是否已存在
     * 4. 设置创建时间和更新时间
     * 5. 保存商铺信息
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

        // ========== 4. 设置创建时间和更新时间 ==========
        // 使用统一的时间变量，保证创建时间和更新时间一致
        LocalDateTime now = LocalDateTime.now();
        shop.setCreateTime(now);
        shop.setUpdateTime(now);

        // ========== 5. 保存商铺信息 ==========
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
     * 5. 设置更新时间
     * 6. 更新商铺信息
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

        // ========== 5. 设置更新时间 ==========
        shop.setUpdateTime(LocalDateTime.now());

        // ========== 6. 更新商铺信息 ==========
        boolean success = updateById(shop);
        if (success) {
            log.info(ShopConstants.SHOP_UPDATE_SUCCESS, shop.getId());
            return Result.ok();
        }

        return Result.fail(ShopConstants.SHOP_UPDATE_FAIL);
    }

    /**
     * 根据ID查询商铺信息
     * 业务逻辑：
     * 1. 校验商铺ID不能为空
     * 2. 根据ID查询商铺
     * 3. 校验商铺是否存在
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

        // ========== 2. 根据ID查询商铺 ==========
        Shop shop = getById(id);

        // ========== 3. 校验商铺是否存在 ==========
        if (shop == null) {
            return Result.fail(ShopConstants.SHOP_NOT_EXIST);
        }

        // ========== 4. 关联查询商铺类型 ==========
        setShopType(shop);

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

        // ========== 5. 关联查询商铺类型 ==========
        for (Shop shop : page.getRecords()) {
            setShopType(shop);
        }

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
            return Result.ok();
        }

        return Result.fail(ShopConstants.SHOP_DELETE_FAIL);
    }

    /**
     * 批量删除商铺
     * 业务逻辑：
     * 1. 校验商铺ID列表不能为空
     * 2. 逐个查询商铺是否存在（有一个不存在则整体失败）
     * 3. 执行批量删除操作
     *
     * @param ids 商铺ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopByIds(List<Long> ids) {
        // ========== 1. 校验商铺ID列表不能为空 ==========
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ShopConstants.SHOP_ID_LIST_NOT_NULL);
        }

        // ========== 2. 逐个查询商铺是否存在 ==========
        // 批量删除前必须先查询确认每个商铺都存在
        // 有一个不存在则整体失败，事务回滚
        for (Long id : ids) {
            Shop shop = getById(id);
            if (shop == null) {
                return Result.fail(ShopConstants.SHOP_NOT_EXIST + "，商铺ID：" + id);
            }
        }

        // ========== 3. 执行批量删除操作 ==========
        // 使用MyBatis-Plus的removeByIds方法进行批量删除
        boolean success = removeByIds(ids);
        if (success) {
            log.info(ShopConstants.SHOP_BATCH_DELETE_SUCCESS, ids.size());
            return Result.ok();
        }

        return Result.fail(ShopConstants.SHOP_BATCH_DELETE_FAIL);
    }

    /**
     * 为商铺设置关联的商铺类型信息
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
}
