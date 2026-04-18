package com.cclg.dianping.utils;

public class RedisConstants {
    
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL_MINUTES = 2L;
    
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL_MINUTES = 30L;
    
    /**
     * 商铺缓存键前缀
     */
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    /**
     * 商铺缓存过期时间（分钟）
     */
    public static final Long CACHE_SHOP_TTL_MINUTES = 30L;

    /**
     * 商铺空值缓存键前缀（防止缓存穿透）
     */
    public static final String CACHE_NULL_SHOP_KEY = "cache:null:shop:";

    /**
     * 空值缓存过期时间（秒）
     */
    public static final Long CACHE_NULL_TTL_SECONDS = 30L;

    /**
     * 优惠券缓存键前缀
     */
    public static final String CACHE_VOUCHER_KEY = "cache:voucher:";

    /**
     * 优惠券缓存过期时间（分钟）
     */
    public static final Long CACHE_VOUCHER_TTL_MINUTES = 30L;

    /**
     * 优惠券空值缓存键前缀（防止缓存穿透）
     */
    public static final String CACHE_NULL_VOUCHER_KEY = "cache:null:voucher:";

    private RedisConstants() {
    }
}
