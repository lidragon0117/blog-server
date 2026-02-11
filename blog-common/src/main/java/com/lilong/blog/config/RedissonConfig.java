package com.lilong.blog.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : lilong
 * @date : 2025-11-25 22:51
 * @description :
 */
@Configuration
public class RedissonConfig {
    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private String port;

    /**
     * 对 Redisson 的使用都是通过 RedissonClient 对象
     * @return
     */
    @Bean(name = "redissonClient", destroyMethod = "shutdown") // 服务停止后调用 shutdown 方法
    public RedissonClient redissonClient() {
        // 1、创建配置
        Config config = new Config();

        // 2、集群模式
        // config.useClusterServers().addNodeAddress("127.0.0.1:7004", "127.0.0.1:7001");
        // 根据 Config 创建出 RedissonClient 示例
        config.useSingleServer()
                .setAddress(String.format("redis://%s:%s", host, port))
                .setDatabase(0);
        return Redisson.create(config);
    }
}
