package com.cclg.dianping.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cclg.dianping.domain.Shop;
import com.cclg.dianping.dto.Result;

import java.util.List;

public interface IShopService extends IService<Shop> {

    Result saveShop(Shop shop);

    Result updateShop(Shop shop);

    Result getShopById(Long id);

    Result queryShopPage(Integer current, Integer size);

    Result deleteShopById(Long id);

    Result deleteShopByIds(List<Long> ids);
}
