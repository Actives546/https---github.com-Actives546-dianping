package com.cclg.dianping.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.constant.ShopConstants;
import com.cclg.dianping.domain.Shop;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.mapper.ShopMapper;
import com.cclg.dianping.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 新增商铺
     *
     * @param shop 商铺信息
     * @return 操作结果，成功返回商铺ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveShop(Shop shop) {
        // 校验商铺信息不能为空
        if (shop == null) {
            return Result.fail(ShopConstants.SHOP_INFO_NOT_NULL);
        }

        // 校验商铺名称不能为空
        if (StrUtil.isBlank(shop.getName())) {
            return Result.fail(ShopConstants.SHOP_NAME_NOT_NULL);
        }

        // 使用统一的时间变量设置创建时间和更新时间，保证时间一致性
        LocalDateTime now = LocalDateTime.now();
        shop.setCreateTime(now);
        shop.setUpdateTime(now);

        // 保存商铺信息
        boolean success = save(shop);
        if (success) {
            log.info(ShopConstants.SHOP_CREATE_SUCCESS, shop.getId());
            return Result.ok(shop.getId());
        }

        return Result.fail(ShopConstants.SHOP_CREATE_FAIL);
    }

    /**
     * 更新商铺信息
     *
     * @param shop 商铺信息
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateShop(Shop shop) {
        // 校验商铺ID不能为空
        if (shop == null || shop.getId() == null) {
            return Result.fail(ShopConstants.SHOP_ID_NOT_NULL);
        }

        // 校验商铺是否存在
        Shop existShop = getById(shop.getId());
        if (existShop == null) {
            return Result.fail(ShopConstants.SHOP_NOT_EXIST);
        }

        // 设置更新时间
        shop.setUpdateTime(LocalDateTime.now());

        // 更新商铺信息
        boolean success = updateById(shop);
        if (success) {
            log.info(ShopConstants.SHOP_UPDATE_SUCCESS, shop.getId());
            return Result.ok();
        }

        return Result.fail(ShopConstants.SHOP_UPDATE_FAIL);
    }

    /**
     * 根据ID查询商铺信息
     *
     * @param id 商铺ID
     * @return 商铺信息
     */
    @Override
    public Result getShopById(Long id) {
        // 校验商铺ID不能为空
        if (id == null) {
            return Result.fail(ShopConstants.SHOP_ID_NOT_NULL);
        }

        // 根据ID查询商铺
        Shop shop = getById(id);
        if (shop == null) {
            return Result.fail(ShopConstants.SHOP_NOT_EXIST);
        }

        return Result.ok(shop);
    }

    /**
     * 分页查询商铺信息
     * 支持按商铺名称模糊筛选
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param name    商铺名称（可选，模糊匹配）
     * @return 分页结果，包含数据列表和总记录数
     */
    @Override
    public Result queryShopPage(Integer current, Integer size, String name) {
        // 处理默认分页参数
        if (current == null || current <= 0) {
            current = ShopConstants.DEFAULT_PAGE_CURRENT;
        }
        if (size == null || size <= 0) {
            size = ShopConstants.DEFAULT_PAGE_SIZE;
        }

        // 创建分页对象
        Page<Shop> page = new Page<>(current, size);

        // 构建查询条件
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();

        // 按商铺名称模糊筛选（如果有传入name参数）
        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like(Shop::getName, name);
        }

        // 按更新时间降序排序，确保最新的记录在前
        queryWrapper.orderByDesc(Shop::getUpdateTime);

        // 执行分页查询
        page(page, queryWrapper);

        // 返回分页结果，包含数据列表和总记录数
        return Result.ok(page.getRecords(), page.getTotal());
    }

    /**
     * 根据ID删除商铺
     *
     * @param id 商铺ID
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopById(Long id) {
        // 校验商铺ID不能为空
        if (id == null) {
            return Result.fail(ShopConstants.SHOP_ID_NOT_NULL);
        }

        // 校验商铺是否存在
        Shop shop = getById(id);
        if (shop == null) {
            return Result.fail(ShopConstants.SHOP_NOT_EXIST);
        }

        // 执行删除操作
        boolean success = removeById(id);
        if (success) {
            log.info(ShopConstants.SHOP_DELETE_SUCCESS, id);
            return Result.ok();
        }

        return Result.fail(ShopConstants.SHOP_DELETE_FAIL);
    }

    /**
     * 批量删除商铺
     * 注意：有一个删除失败则整体失败（事务回滚）
     * 实现逻辑：逐个校验并删除，确保每个商铺都存在且删除成功
     *
     * @param ids 商铺ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopByIds(List<Long> ids) {
        // 校验商铺ID列表不能为空
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ShopConstants.SHOP_ID_LIST_NOT_NULL);
        }

        // 逐个校验商铺是否存在，并执行删除
        // 注意：使用事务注解，任何一个删除失败都会触发回滚
        for (Long id : ids) {
            // 校验商铺是否存在
            Shop shop = getById(id);
            if (shop == null) {
                // 商铺不存在，直接返回失败（事务会回滚）
                return Result.fail(ShopConstants.SHOP_NOT_EXIST + "，商铺ID：" + id);
            }

            // 执行删除
            boolean success = removeById(id);
            if (!success) {
                // 删除失败，返回失败（事务会回滚）
                return Result.fail(ShopConstants.SHOP_DELETE_FAIL + "，商铺ID：" + id);
            }
        }

        log.info(ShopConstants.SHOP_BATCH_DELETE_SUCCESS, ids.size());
        return Result.ok();
    }
}
