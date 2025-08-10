package com.itheima.reggie.config;

import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig implements CachingConfigurer {
    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        //默认的Key序列化器为：JdkSerializationRedisSerializer，需要修改，便于通过redis客户端观察Key
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //如果不配置，RedisAutoConfiguration也会创建redisTemplate对象，只不过键序列化器是默认选项
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
}
