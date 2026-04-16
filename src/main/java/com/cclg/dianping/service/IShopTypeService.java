package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.ShopType;
import com.cclg.dianping.dto.Result;

import java.util.List;

public interface IShopTypeService extends IService<ShopType> {

    Result saveShopType(ShopType shopType);

    Result updateShopType(ShopType shopType);

    Result getShopTypeById(Long id);

    Result queryShopTypePage(Integer current, Integer size, String name);

    Result deleteShopTypeById(Long id);

    Result deleteShopTypeByIds(List<Long> ids);

    Result queryAllShopTypes();
}
