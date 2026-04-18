package com.cclg.dianping.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson配置类
 * 配置分布式锁客户端
 *
 * @author system
 */
@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private String port;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.database:0}")
    private Integer database;

    /**
     * 创建RedissonClient
     *
     * @return RedissonClient实例
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        
        log.info("初始化Redisson客户端，地址：{}，数据库：{}", address, database);
        
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(10)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(10000)
                .setTimeout(3000);
        
        // 只有当密码不为空时才设置密码
        if (StringUtils.hasText(password)) {
            config.useSingleServer().setPassword(password);
            log.info("Redisson客户端已设置密码");
        }
        
        try {
            RedissonClient client = Redisson.create(config);
            log.info("Redisson客户端初始化成功");
            return client;
        } catch (Exception e) {
            log.error("Redisson客户端初始化失败：{}", e.getMessage(), e);
            throw e;
        }
    }
}
