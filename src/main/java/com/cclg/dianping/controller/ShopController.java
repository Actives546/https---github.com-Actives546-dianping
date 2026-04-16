package com.cclg.dianping.controller;

import com.cclg.dianping.dto.Result;
import com.cclg.dianping.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    private IShopService shopService;

    @PostMapping
    public Result saveShop(@RequestBody com.cclg.dianping.domain.Shop shop) {
        log.info("新增商铺，商铺名称：{}", shop.getName());
        return shopService.saveShop(shop);
    }

    @PutMapping
    public Result updateShop(@RequestBody com.cclg.dianping.domain.Shop shop) {
        log.info("更新商铺，商铺ID：{}", shop.getId());
        return shopService.updateShop(shop);
    }

    @GetMapping("/{id}")
    public Result getShopById(@PathVariable("id") Long id) {
        log.info("根据ID查询商铺，商铺ID：{}", id);
        return shopService.getShopById(id);
    }

    @GetMapping("/page")
    public Result queryShopPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.info("分页查询商铺，当前页：{}，每页大小：{}", current, size);
        return shopService.queryShopPage(current, size);
    }

    @DeleteMapping("/{id}")
    public Result deleteShopById(@PathVariable("id") Long id) {
        log.info("删除商铺，商铺ID：{}", id);
        return shopService.deleteShopById(id);
    }

    @DeleteMapping("/batch")
    public Result deleteShopByIds(@RequestBody List<Long> ids) {
        log.info("批量删除商铺，商铺数量：{}", ids.size());
        return shopService.deleteShopByIds(ids);
    }
}
