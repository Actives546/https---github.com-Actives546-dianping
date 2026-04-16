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
        if (shop == null) {
            return Result.fail(ShopConstants.SHOP_INFO_NOT_NULL);
        }

        if (StrUtil.isBlank(shop.getName())) {
            return Result.fail(ShopConstants.SHOP_NAME_NOT_NULL);
        }

        if (checkShopNameExist(shop.getName(), null)) {
            return Result.fail(ShopConstants.SHOP_NAME_EXIST);
        }

        LocalDateTime now = LocalDateTime.now();
        shop.setCreateTime(now);
        shop.setUpdateTime(now);

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
        if (shop == null || shop.getId() == null) {
            return Result.fail(ShopConstants.SHOP_ID_NOT_NULL);
        }

        Shop existShop = getById(shop.getId());
        if (existShop == null) {
            return Result.fail(ShopConstants.SHOP_NOT_EXIST);
        }

        if (StrUtil.isNotBlank(shop.getName()) && checkShopNameExist(shop.getName(), shop.getId())) {
            return Result.fail(ShopConstants.SHOP_NAME_EXIST);
        }

        shop.setUpdateTime(LocalDateTime.now());

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
        if (id == null) {
            return Result.fail(ShopConstants.SHOP_ID_NOT_NULL);
        }

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
        if (current == null || current <= 0) {
            current = ShopConstants.DEFAULT_PAGE_CURRENT;
        }

        if (size == null || size <= 0) {
            size = ShopConstants.DEFAULT_PAGE_SIZE;
        } else if (size > ShopConstants.MAX_PAGE_SIZE) {
            size = ShopConstants.MAX_PAGE_SIZE;
        }

        Page<Shop> page = new Page<>(current, size);

        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like(Shop::getName, name);
        }

        queryWrapper.orderByDesc(Shop::getUpdateTime);

        page(page, queryWrapper);

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
        if (id == null) {
            return Result.fail(ShopConstants.SHOP_ID_NOT_NULL);
        }

        Shop shop = getById(id);
        if (shop == null) {
            return Result.fail(ShopConstants.SHOP_NOT_EXIST);
        }

        boolean success = removeById(id);
        if (success) {
            log.info(ShopConstants.SHOP_DELETE_SUCCESS, id);
            return Result.ok();
        }

        return Result.fail(ShopConstants.SHOP_DELETE_FAIL);
    }

    /**
     * 批量删除商铺
     * 使用MyBatis-Plus的removeByIds方法进行批量删除
     *
     * @param ids 商铺ID列表
     * @return 操作结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ShopConstants.SHOP_ID_LIST_NOT_NULL);
        }

        boolean success = removeByIds(ids);
        if (success) {
            log.info(ShopConstants.SHOP_BATCH_DELETE_SUCCESS, ids.size());
            return Result.ok();
        }

        return Result.fail(ShopConstants.SHOP_BATCH_DELETE_FAIL);
    }

    /**
     * 检查商铺名称是否已存在
     *
     * @param shopName 商铺名称
     * @param excludeId 需要排除的商铺ID（更新时使用，排除自身）
     * @return true-已存在，false-不存在
     */
    private boolean checkShopNameExist(String shopName, Long excludeId) {
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Shop::getName, shopName);

        if (excludeId != null) {
            queryWrapper.ne(Shop::getId, excludeId);
        }

        return count(queryWrapper) > 0;
    }
}
