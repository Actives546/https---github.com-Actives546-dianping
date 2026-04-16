package com.cclg.dianping.constant;

public final class ShopTypeConstants {

    private ShopTypeConstants() {
        throw new AssertionError("常量类不能被实例化");
    }

    public static final String SHOP_TYPE_INFO_NOT_NULL = "商铺类型信息不能为空";
    public static final String SHOP_TYPE_NAME_NOT_NULL = "商铺类型名称不能为空";
    public static final String SHOP_TYPE_ID_NOT_NULL = "商铺类型ID不能为空";
    public static final String SHOP_TYPE_NOT_EXIST = "商铺类型不存在";
    public static final String SHOP_TYPE_ID_LIST_NOT_NULL = "商铺类型ID列表不能为空";
    public static final String SHOP_TYPE_NAME_EXIST = "商铺类型名称已存在";
    public static final String SHOP_TYPE_HAS_ASSOCIATED_SHOP = "该类型下存在关联商铺，无法删除";

    public static final String SHOP_TYPE_CREATE_SUCCESS = "商铺类型创建成功，类型ID：{}";
    public static final String SHOP_TYPE_CREATE_FAIL = "商铺类型创建失败";
    public static final String SHOP_TYPE_UPDATE_SUCCESS = "商铺类型更新成功，类型ID：{}";
    public static final String SHOP_TYPE_UPDATE_FAIL = "商铺类型更新失败";
    public static final String SHOP_TYPE_DELETE_SUCCESS = "商铺类型删除成功，类型ID：{}";
    public static final String SHOP_TYPE_DELETE_FAIL = "商铺类型删除失败";
    public static final String SHOP_TYPE_BATCH_DELETE_SUCCESS = "批量删除商铺类型成功，类型数量：{}";
    public static final String SHOP_TYPE_BATCH_DELETE_FAIL = "批量删除商铺类型失败";

    public static final String LOG_PAGE_QUERY = "分页查询商铺类型，当前页：{}，每页大小：{}，类型名称：{}";
    public static final String LOG_SAVE_SHOP_TYPE = "新增商铺类型，类型名称：{}";
    public static final String LOG_UPDATE_SHOP_TYPE = "更新商铺类型，类型ID：{}";
    public static final String LOG_GET_SHOP_TYPE = "根据ID查询商铺类型，类型ID：{}";
    public static final String LOG_DELETE_SHOP_TYPE = "删除商铺类型，类型ID：{}";
    public static final String LOG_BATCH_DELETE_SHOP_TYPE = "批量删除商铺类型，类型数量：{}";
}
