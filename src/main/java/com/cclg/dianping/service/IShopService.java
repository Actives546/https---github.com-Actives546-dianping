package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.Shop;
import com.cclg.dianping.dto.Result;

import java.util.List;

/**
 * 商铺服务接口
 * 定义商铺相关的业务操作方法
 *
 * @author system
 */
public interface IShopService extends IService<Shop> {

    /**
     * 新增商铺
     *
     * @param shop 商铺信息
     * @return 操作结果
     */
    Result saveShop(Shop shop);

    /**
     * 更新商铺信息
     *
     * @param shop 商铺信息
     * @return 操作结果
     */
    Result updateShop(Shop shop);

    /**
     * 根据ID查询商铺信息
     *
     * @param id 商铺ID
     * @return 商铺信息
     */
    Result getShopById(Long id);

    /**
     * 分页查询商铺信息
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param name    商铺名称（可选，模糊匹配）
     * @return 分页结果
     */
    Result queryShopPage(Integer current, Integer size, String name);

    /**
     * 根据ID删除商铺
     *
     * @param id 商铺ID
     * @return 操作结果
     */
    Result deleteShopById(Long id);

    /**
     * 批量删除商铺
     * 注意：有一个删除失败则整体失败
     *
     * @param ids 商铺ID列表
     * @return 操作结果
     */
    Result deleteShopByIds(List<Long> ids);
}
