package com.cclg.dianping.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cclg.dianping.domain.Shop;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.mapper.ShopMapper;
import com.cclg.dianping.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result saveShop(Shop shop) {
        if (shop == null) {
            return Result.fail("商铺信息不能为空");
        }
        if (StrUtil.isBlank(shop.getName())) {
            return Result.fail("商铺名称不能为空");
        }
        shop.setCreateTime(LocalDateTime.now());
        shop.setUpdateTime(LocalDateTime.now());
        boolean success = save(shop);
        if (success) {
            log.info("商铺创建成功，商铺ID：{}", shop.getId());
            return Result.ok(shop.getId());
        }
        return Result.fail("商铺创建失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateShop(Shop shop) {
        if (shop == null || shop.getId() == null) {
            return Result.fail("商铺ID不能为空");
        }
        Shop existShop = getById(shop.getId());
        if (existShop == null) {
            return Result.fail("商铺不存在");
        }
        shop.setUpdateTime(LocalDateTime.now());
        boolean success = updateById(shop);
        if (success) {
            log.info("商铺更新成功，商铺ID：{}", shop.getId());
            return Result.ok();
        }
        return Result.fail("商铺更新失败");
    }

    @Override
    public Result getShopById(Long id) {
        if (id == null) {
            return Result.fail("商铺ID不能为空");
        }
        Shop shop = getById(id);
        if (shop == null) {
            return Result.fail("商铺不存在");
        }
        return Result.ok(shop);
    }

    @Override
    public Result queryShopPage(Integer current, Integer size) {
        if (current == null || current <= 0) {
            current = 1;
        }
        if (size == null || size <= 0) {
            size = 10;
        }
        Page<Shop> page = new Page<>(current, size);
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Shop::getUpdateTime);
        page(page, queryWrapper);
        return Result.ok(page.getRecords(), page.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopById(Long id) {
        if (id == null) {
            return Result.fail("商铺ID不能为空");
        }
        Shop shop = getById(id);
        if (shop == null) {
            return Result.fail("商铺不存在");
        }
        boolean success = removeById(id);
        if (success) {
            log.info("商铺删除成功，商铺ID：{}", id);
            return Result.ok();
        }
        return Result.fail("商铺删除失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result deleteShopByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Result.fail("商铺ID列表不能为空");
        }
        boolean success = removeByIds(ids);
        if (success) {
            log.info("批量删除商铺成功，商铺数量：{}", ids.size());
            return Result.ok();
        }
        return Result.fail("批量删除商铺失败");
    }
}
