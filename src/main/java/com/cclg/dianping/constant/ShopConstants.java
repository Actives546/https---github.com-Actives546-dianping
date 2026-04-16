package com.cclg.dianping.constant;

public final class ShopConstants {

    private ShopConstants() {
        throw new AssertionError("常量类不能被实例化");
    }

    public static final String SHOP_INFO_NOT_NULL = "商铺信息不能为空";
    public static final String SHOP_NAME_NOT_NULL = "商铺名称不能为空";
    public static final String SHOP_ID_NOT_NULL = "商铺ID不能为空";
    public static final String SHOP_NOT_EXIST = "商铺不存在";
    public static final String SHOP_ID_LIST_NOT_NULL = "商铺ID列表不能为空";
    public static final String SHOP_NAME_EXIST = "商铺名称已存在";
    public static final String SHOP_TYPE_NOT_EXIST = "商铺类型不存在";

    public static final String SHOP_CREATE_SUCCESS = "商铺创建成功，商铺ID：{}";
    public static final String SHOP_CREATE_FAIL = "商铺创建失败";
    public static final String SHOP_UPDATE_SUCCESS = "商铺更新成功，商铺ID：{}";
    public static final String SHOP_UPDATE_FAIL = "商铺更新失败";
    public static final String SHOP_DELETE_SUCCESS = "商铺删除成功，商铺ID：{}";
    public static final String SHOP_DELETE_FAIL = "商铺删除失败";
    public static final String SHOP_BATCH_DELETE_SUCCESS = "批量删除商铺成功，商铺数量：{}";
    public static final String SHOP_BATCH_DELETE_FAIL = "批量删除商铺失败";

    public static final String LOG_PAGE_QUERY = "分页查询商铺，当前页：{}，每页大小：{}，商铺名称：{}";
    public static final String LOG_SAVE_SHOP = "新增商铺，商铺名称：{}";
    public static final String LOG_UPDATE_SHOP = "更新商铺，商铺ID：{}";
    public static final String LOG_GET_SHOP = "根据ID查询商铺，商铺ID：{}";
    public static final String LOG_DELETE_SHOP = "删除商铺，商铺ID：{}";
    public static final String LOG_BATCH_DELETE_SHOP = "批量删除商铺，商铺数量：{}";

    public static final Integer DEFAULT_PAGE_CURRENT = 1;
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    public static final Integer MAX_PAGE_SIZE = 100;
}
