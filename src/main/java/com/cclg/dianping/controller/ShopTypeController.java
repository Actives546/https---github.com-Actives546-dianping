package com.cclg.dianping.controller;

import com.cclg.dianping.constant.ShopTypeConstants;
import com.cclg.dianping.domain.ShopType;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.service.IShopTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private IShopTypeService shopTypeService;

    @PostMapping
    public Result saveShopType(@RequestBody ShopType shopType) {
        log.info(ShopTypeConstants.LOG_SAVE_SHOP_TYPE, shopType.getName());
        return shopTypeService.saveShopType(shopType);
    }

    @PutMapping
    public Result updateShopType(@RequestBody ShopType shopType) {
        log.info(ShopTypeConstants.LOG_UPDATE_SHOP_TYPE, shopType.getId());
        return shopTypeService.updateShopType(shopType);
    }

    @GetMapping("/{id}")
    public Result getShopTypeById(@PathVariable("id") Long id) {
        log.info(ShopTypeConstants.LOG_GET_SHOP_TYPE, id);
        return shopTypeService.getShopTypeById(id);
    }

    @GetMapping("/list")
    public Result queryAllShopTypes() {
        return shopTypeService.queryAllShopTypes();
    }

    @GetMapping("/page")
    public Result queryShopTypePage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "name", required = false) String name) {
        log.info(ShopTypeConstants.LOG_PAGE_QUERY, current, size, name);
        return shopTypeService.queryShopTypePage(current, size, name);
    }

    @DeleteMapping("/{id}")
    public Result deleteShopTypeById(@PathVariable("id") Long id) {
        log.info(ShopTypeConstants.LOG_DELETE_SHOP_TYPE, id);
        return shopTypeService.deleteShopTypeById(id);
    }

    @DeleteMapping("/batch")
    public Result deleteShopTypeByIds(@RequestBody List<Long> ids) {
        log.info(ShopTypeConstants.LOG_BATCH_DELETE_SHOP_TYPE, ids.size());
        return shopTypeService.deleteShopTypeByIds(ids);
    }
}
