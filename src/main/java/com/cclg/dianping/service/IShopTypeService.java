package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.ShopType;
import com.cclg.dianping.dto.Result;

import java.util.List;

/**
 * 商铺类型服务接口
 * 定义商铺类型相关的业务操作方法
 *
 * @author system
 */
public interface IShopTypeService extends IService<ShopType> {

    /**
     * 新增商铺类型
     *
     * @param shopType 商铺类型信息
     * @return 操作结果，成功返回商铺类型ID
     */
    Result saveShopType(ShopType shopType);

    /**
     * 更新商铺类型信息
     *
     * @param shopType 商铺类型信息
     * @return 操作结果
     */
    Result updateShopType(ShopType shopType);

    /**
     * 根据ID查询商铺类型信息
     *
     * @param id 商铺类型ID
     * @return 商铺类型信息
     */
    Result getShopTypeById(Long id);

    /**
     * 分页查询商铺类型信息
     * 支持按类型名称模糊筛选
     *
     * @param current 当前页码
     * @param size    每页大小
     * @param name    类型名称（可选，模糊匹配）
     * @return 分页结果，包含数据列表和总记录数
     */
    Result queryShopTypePage(Integer current, Integer size, String name);

    /**
     * 根据ID删除商铺类型
     * 注意：如果该类型下存在关联商铺，则无法删除
     *
     * @param id 商铺类型ID
     * @return 操作结果
     */
    Result deleteShopTypeById(Long id);

    /**
     * 批量删除商铺类型
     * 注意：有一个类型存在关联商铺则整体失败
     *
     * @param ids 商铺类型ID列表
     * @return 操作结果
     */
    Result deleteShopTypeByIds(List<Long> ids);

    /**
     * 查询所有商铺类型
     * 按排序值升序排列
     *
     * @return 所有商铺类型列表
     */
    Result queryAllShopTypes();
}
