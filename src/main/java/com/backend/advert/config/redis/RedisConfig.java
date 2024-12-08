package com.backend.advert.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * RedisTemplate을 설정합니다. 객체를 JSON 형태로 직렬화/역직렬화하여 Redis에 저장합니다.
     *
     * @param redisConnectionFactory Redis 연결 팩토리
     * @return RedisTemplate 설정 객체
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        // Jackson2JsonRedisSerializer 설정
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // ObjectMapper 설정 (LocalDateTime 처리 위해)
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // LocalDateTime 처리
        objectMapper.findAndRegisterModules(); // 모든 jackson 모듈 자동 등록
        serializer.setObjectMapper(objectMapper);

        template.setDefaultSerializer(serializer);  // Redis에 저장되는 모든 객체에 적용
        return template;
    }

    /**
     * StringRedisTemplate을 설정합니다. Redis에 문자열 값을 저장하는 템플릿입니다.
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return StringRedisTemplate 설정 객체
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    /**
     * CacheManager 설정. Redis를 캐시 저장소로 사용하도록 설정합니다.
     *
     * @param redisConnectionFactory Redis 연결 팩토리
     * @return CacheManager 설정 객체
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // Jackson2JsonRedisSerializer로 객체 직렬화
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // ObjectMapper를 설정
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());  // LocalDateTime 지원
        objectMapper.findAndRegisterModules();  // 모든 jackson 모듈 자동 등록
        serializer.setObjectMapper(objectMapper);

        // 기본 캐시 구성 설정
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();  // 캐시된 값이 null이면 저장되지 않도록 설정

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
    }
}

