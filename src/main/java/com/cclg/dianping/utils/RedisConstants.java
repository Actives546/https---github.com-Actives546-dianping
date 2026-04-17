package com.cclg.dianping.utils;

public class RedisConstants {
    
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;
    
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    public static final Long CACHE_SHOP_TTL = 30L;
    
    public static final String CACHE_NULL_KEY = "cache:null:";
    public static final Long CACHE_NULL_TTL = 2L;
    
    private RedisConstants() {
    }
}
