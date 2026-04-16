package com.cclg.dianping.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.constant.ShopTypeConstants;
import com.cclg.dianping.domain.ShopType;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.mapper.ShopTypeMapper;
import com.cclg.dianping.service.IShopTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveShopType(ShopType shopType) {
        if (shopType == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_INFO_NOT_NULL);
        }

        if (StrUtil.isBlank(shopType.getName())) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NAME_NOT_NULL);
        }

        if (checkShopTypeNameExist(shopType.getName(), null)) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NAME_EXIST);
        }

        LocalDateTime now = LocalDateTime.now();
        shopType.setCreateTime(now);
        shopType.setUpdateTime(now);

        if (shopType.getSort() == null) {
            shopType.setSort(0);
        }

        boolean success = save(shopType);
        if (success) {
            log.info(ShopTypeConstants.SHOP_TYPE_CREATE_SUCCESS, shopType.getId());
            return Result.ok(shopType.getId());
        }

        return Result.fail(ShopTypeConstants.SHOP_TYPE_CREATE_FAIL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateShopType(ShopType shopType) {
        if (shopType == null || shopType.getId() == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_ID_NOT_NULL);
        }

        ShopType existShopType = getById(shopType.getId());
        if (existShopType == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NOT_EXIST);
        }

        if (shopType.getName() != null && StrUtil.isBlank(shopType.getName())) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NAME_NOT_NULL);
        }

        if (StrUtil.isNotBlank(shopType.getName()) && checkShopTypeNameExist(shopType.getName(), shopType.getId())) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NAME_EXIST);
        }

        shopType.setUpdateTime(LocalDateTime.now());

        boolean success = updateById(shopType);
        if (success) {
            log.info(ShopTypeConstants.SHOP_TYPE_UPDATE_SUCCESS, shopType.getId());
            return Result.ok();
        }

        return Result.fail(ShopTypeConstants.SHOP_TYPE_UPDATE_FAIL);
    }

    @Override
    public Result getShopTypeById(Long id) {
        if (id == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_ID_NOT_NULL);
        }

        ShopType shopType = getById(id);

        if (shopType == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NOT_EXIST);
        }

        return Result.ok(shopType);
    }

    @Override
    public Result queryShopTypePage(Integer current, Integer size, String name) {
        if (current == null || current <= 0) {
            current = com.cclg.dianping.constant.ShopConstants.DEFAULT_PAGE_CURRENT;
        }

        if (size == null || size <= 0) {
            size = com.cclg.dianping.constant.ShopConstants.DEFAULT_PAGE_SIZE;
        } else if (size > com.cclg.dianping.constant.ShopConstants.MAX_PAGE_SIZE) {
            size = com.cclg.dianping.constant.ShopConstants.MAX_PAGE_SIZE;
        }

        Page<ShopType> page = new Page<>(current, size);
        LambdaQueryWrapper<ShopType> queryWrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(name)) {
            queryWrapper.like(ShopType::getName, name);
        }

        queryWrapper.orderByAsc(ShopType::getSort);
        queryWrapper.orderByDesc(ShopType::getUpdateTime);

        page(page, queryWrapper);

        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopTypeById(Long id) {
        if (id == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_ID_NOT_NULL);
        }

        ShopType shopType = getById(id);
        if (shopType == null) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_NOT_EXIST);
        }

        boolean success = removeById(id);
        if (success) {
            log.info(ShopTypeConstants.SHOP_TYPE_DELETE_SUCCESS, id);
            return Result.ok();
        }

        return Result.fail(ShopTypeConstants.SHOP_TYPE_DELETE_FAIL);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopTypeByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail(ShopTypeConstants.SHOP_TYPE_ID_LIST_NOT_NULL);
        }

        for (Long id : ids) {
            ShopType shopType = getById(id);
            if (shopType == null) {
                return Result.fail(ShopTypeConstants.SHOP_TYPE_NOT_EXIST + "，商铺类型ID：" + id);
            }
        }

        boolean success = removeByIds(ids);
        if (success) {
            log.info(ShopTypeConstants.SHOP_TYPE_BATCH_DELETE_SUCCESS, ids.size());
            return Result.ok();
        }

        return Result.fail(ShopTypeConstants.SHOP_TYPE_BATCH_DELETE_FAIL);
    }

    @Override
    public Result queryAllShopTypes() {
        LambdaQueryWrapper<ShopType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(ShopType::getSort);
        List<ShopType> shopTypes = list(queryWrapper);
        return Result.ok(shopTypes);
    }

    private boolean checkShopTypeNameExist(String shopTypeName, Long excludeId) {
        LambdaQueryWrapper<ShopType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShopType::getName, shopTypeName);

        if (excludeId != null) {
            queryWrapper.ne(ShopType::getId, excludeId);
        }

        return count(queryWrapper) > 0;
    }
}
