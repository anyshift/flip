package com.flip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 在 RedisTemplate 中，默认是使用 Java 字符串序列化，字符串序列化不能够提供可读性。
 * 最佳的是替换 RedisTemplate 的 key 为默认的字符串序列化，value 采用 Jackson 序列化。
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);

        /* 使用GenericJackson2JsonRedisSerializer替换默认序列化 */
        GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer();

        /* 设置 key 和 value 的序列化规则（key为String，value为JSON） */
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jacksonSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jacksonSerializer);

        /* 序列化完成 */
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
