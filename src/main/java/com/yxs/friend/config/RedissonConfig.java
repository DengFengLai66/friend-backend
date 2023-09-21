package com.yxs.friend.config;

import lombok.Data;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description: redis
 * @author: yxs
 * @create: 2023-08-06 16:26
 **/
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private Integer port;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public Redisson getClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        if (StringUtils.isNotEmpty(password)) {
            config.useSingleServer().setPassword(password);
        }
        return (Redisson) Redisson.create(config);
    }
}

