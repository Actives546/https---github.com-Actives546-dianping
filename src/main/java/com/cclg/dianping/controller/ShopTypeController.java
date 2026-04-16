package com.cclg.dianping.controller;

import com.cclg.dianping.constant.ShopTypeConstants;
import com.cclg.dianping.domain.ShopType;
import com.cclg.dianping.dto.Result;
import com.cclg.dianping.service.IShopTypeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商铺类型管理控制器
 * 提供商铺类型的增删改查等接口
 *
 * @author system
 */
@Slf4j
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private IShopTypeService shopTypeService;

    /**
     * 新增商铺类型
     *
     * @param shopType 商铺类型信息
     * @return 操作结果，成功返回商铺类型ID
     */
    @PostMapping
    public Result saveShopType(@RequestBody ShopType shopType) {
        log.info(ShopTypeConstants.LOG_SAVE_SHOP_TYPE, shopType.getName());
        return shopTypeService.saveShopType(shopType);
    }

    /**
     * 更新商铺类型信息
     *
     * @param shopType 商铺类型信息
     * @return 操作结果
     */
    @PutMapping
    public Result updateShopType(@RequestBody ShopType shopType) {
        log.info(ShopTypeConstants.LOG_UPDATE_SHOP_TYPE, shopType.getId());
        return shopTypeService.updateShopType(shopType);
    }

    /**
     * 根据ID查询商铺类型信息
     *
     * @param id 商铺类型ID
     * @return 商铺类型信息
     */
    @GetMapping("/{id}")
    public Result getShopTypeById(@PathVariable("id") Long id) {
        log.info(ShopTypeConstants.LOG_GET_SHOP_TYPE, id);
        return shopTypeService.getShopTypeById(id);
    }

    /**
     * 查询所有商铺类型
     * 按排序值升序排列
     *
     * @return 所有商铺类型列表
     */
    @GetMapping("/list")
    public Result queryAllShopTypes() {
        return shopTypeService.queryAllShopTypes();
    }

    /**
     * 分页查询商铺类型信息
     * 支持按类型名称模糊筛选
     *
     * @param current 当前页码，默认1
     * @param size    每页大小，默认10，最大100
     * @param name    类型名称（可选，模糊匹配）
     * @return 分页结果，包含数据列表和总记录数
     */
    @GetMapping("/page")
    public Result queryShopTypePage(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "name", required = false) String name) {
        log.info(ShopTypeConstants.LOG_PAGE_QUERY, current, size, name);
        return shopTypeService.queryShopTypePage(current, size, name);
    }

    /**
     * 根据ID删除商铺类型
     * 注意：如果该类型下存在关联商铺，则无法删除
     *
     * @param id 商铺类型ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result deleteShopTypeById(@PathVariable("id") Long id) {
        log.info(ShopTypeConstants.LOG_DELETE_SHOP_TYPE, id);
        return shopTypeService.deleteShopTypeById(id);
    }

    /**
     * 批量删除商铺类型
     * 使用MyBatis-Plus的批量删除方法
     * 注意：有一个类型存在关联商铺则整体失败
     *
     * @param ids 商铺类型ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public Result deleteShopTypeByIds(@RequestBody List<Long> ids) {
        log.info(ShopTypeConstants.LOG_BATCH_DELETE_SHOP_TYPE, ids.size());
        return shopTypeService.deleteShopTypeByIds(ids);
    }
}
