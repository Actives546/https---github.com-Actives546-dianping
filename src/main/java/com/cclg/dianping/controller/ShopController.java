package com.cclg.dianping.controller;

import com.cclg.dianping.domain.Shop;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商铺管理控制器
 * 提供商铺的增删改查等接口
 *
 * @author system
 */
@Slf4j
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    private IShopService shopService;

    /**
     * 新增商铺
     *
     * @param shop 商铺信息
     * @return 操作结果，成功返回商铺ID
     */
    @PostMapping
    public Result saveShop(@RequestBody Shop shop) {
        log.info("新增商铺，商铺名称：{}", shop.getName());
        return shopService.saveShop(shop);
    }

    /**
     * 更新商铺信息
     *
     * @param shop 商铺信息
     * @return 操作结果
     */
    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        log.info("更新商铺，商铺ID：{}", shop.getId());
        return shopService.updateShop(shop);
    }

    /**
     * 根据ID查询商铺信息
     *
     * @param id 商铺ID
     * @return 商铺信息
     */
    @GetMapping("/{id}")
    public Result getShopById(@PathVariable("id") Long id) {
        log.info("根据ID查询商铺，商铺ID：{}", id);
        return shopService.getShopById(id);
    }

    /**
     * 分页查询商铺信息
     * 支持按商铺名称模糊筛选
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10
     * @param name    商铺名称（可选，模糊匹配）
     * @return 分页结果，包含数据列表和总记录数
     */
    @GetMapping("/page")
    public Result queryShopPage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "name", required = false) String name) {
        log.info("分页查询商铺，当前页：{}，每页大小：{}，商铺名称：{}", current, size, name);
        return shopService.queryShopPage(current, size, name);
    }

    /**
     * 根据ID删除商铺
     *
     * @param id 商铺ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result deleteShopById(@PathVariable("id") Long id) {
        log.info("删除商铺，商铺ID：{}", id);
        return shopService.deleteShopById(id);
    }

    /**
     * 批量删除商铺
     * 注意：有一个删除失败则整体失败（事务回滚）
     *
     * @param ids 商铺ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result deleteShopByIds(@RequestBody List<Long> ids) {
        log.info("批量删除商铺，商铺数量：{}", ids.size());
        return shopService.deleteShopByIds(ids);
    }
}
