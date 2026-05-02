package com.hmdp.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private String port;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.database}")
    private String database;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        int dataBase = Integer.parseInt(database);
        config.useSingleServer().setAddress(address)
                .setDatabase(dataBase)
                .setPassword(password);
/*                // 设置连接池大小
                .setConnectionPoolSize(10)
                .setConnectionMinimumIdleSize(5)
                // 设置超时时间
                .setConnectTimeout(10000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);*/
        return Redisson.create(config);
    }

}
