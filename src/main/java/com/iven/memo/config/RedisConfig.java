package com.iven.memo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

@Configuration
class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key 和 Hash Key 推荐用字符串序列化
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value 用新的 JacksonJsonRedisSerializer，替换已弃用的 GenericJackson2JsonRedisSerializer
        JacksonJsonRedisSerializer<?> jacksonJsonRedisSerializer =
                new JacksonJsonRedisSerializer<>(new ObjectMapper(), Object.class);
        template.setValueSerializer(jacksonJsonRedisSerializer);
        template.setHashValueSerializer(jacksonJsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
