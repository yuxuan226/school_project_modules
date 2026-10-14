package com.example.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    // ---------- Sa-Token Redis (db=0) ----------
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.redis")
    public RedisProperties saTokenRedisProperties() {
        return new RedisProperties();
    }

    @Bean
    @Primary
    public RedisConnectionFactory saTokenRedisConnectionFactory(
            @Qualifier("saTokenRedisProperties") RedisProperties properties) {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(properties.getHost(), properties.getPort());
        factory.setDatabase(properties.getDatabase()); // db=0
        factory.afterPropertiesSet(); // 必须调用
        return factory;
    }

    @Bean("saTokenRedisTemplate")
    public RedisTemplate<String, Object> saTokenRedisTemplate(
            @Qualifier("saTokenRedisConnectionFactory") RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    // ---------- Spring Cache Redis (db=1) ----------
    @Bean
    @ConfigurationProperties(prefix = "springcache.redis")
    public RedisProperties springCacheRedisProperties() {
        return new RedisProperties();
    }

    @Bean
    public RedisConnectionFactory springCacheRedisConnectionFactory(
            @Qualifier("springCacheRedisProperties") RedisProperties properties) {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(properties.getHost(), properties.getPort());
        factory.setDatabase(properties.getDatabase()); // db=1
        factory.afterPropertiesSet(); // 必须调用
        return factory;
    }

    @Bean("springCacheRedisTemplate")
    public RedisTemplate<String, Object> springCacheRedisTemplate(
            @Qualifier("springCacheRedisConnectionFactory") RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());

        // 使用自定义序列化器
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(mapper));
        return template;
    }

    // ---------- Chat AI Redis (db=2) ----------
    /**
     * Chat AI Redis 配置属性
     */
    @Bean
    @ConfigurationProperties(prefix = "chatai.redis")
    public RedisProperties chatAiRedisProperties() {
        return new RedisProperties();
    }

    /**
     * Chat AI Redis 连接工厂（使用 2 号库）
     */
    @Bean
    public RedisConnectionFactory chatAiRedisConnectionFactory(
            @Qualifier("chatAiRedisProperties") RedisProperties properties) {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                properties.getHost(),
                properties.getPort()
        );
        factory.setDatabase(properties.getDatabase()); // db=2
        factory.afterPropertiesSet(); // 必须调用，否则配置不生效
        return factory;
    }

    /**
     * Chat AI RedisTemplate
     * Key 序列化：String，Value 序列化：JSON
     */
    @Bean("chatAiRedisTemplate")
    public RedisTemplate<String, Object> chatAiRedisTemplate(
            @Qualifier("chatAiRedisConnectionFactory") RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // Key 序列化
        template.setKeySerializer(new StringRedisSerializer());

        // Value 序列化（JSON 格式）
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    // ---------- Embedding Redis (db=3) ----------
    /**
     * Embedding 向量存储 Redis 配置属性
     */
    @Bean
    @ConfigurationProperties(prefix = "embedding.redis")
    public RedisProperties embeddingRedisProperties() {
        return new RedisProperties();
    }

    /**
     * Embedding 向量存储 Redis 连接工厂（使用 3 号库）
     */
    @Bean
    public RedisConnectionFactory embeddingRedisConnectionFactory(
            @Qualifier("embeddingRedisProperties") RedisProperties properties) {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(
                properties.getHost(),
                properties.getPort()
        );
        factory.setDatabase(properties.getDatabase()); // db=3
        factory.afterPropertiesSet();
        return factory;
    }

    /**
     * Embedding 向量存储 RedisTemplate
     * 用于存储 RAG 文档向量和文本片段
     */
    @Bean("embeddingRedisTemplate")
    public RedisTemplate<String, Object> embeddingRedisTemplate(
            @Qualifier("embeddingRedisConnectionFactory") RedisConnectionFactory factory) {

        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    // ---------- Spring Cache Manager ----------
    @Bean
    @Primary // 让 Spring Cache 使用这个 CacheManager
    public RedisCacheManager cacheManager(
            @Qualifier("springCacheRedisConnectionFactory") RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericFastJsonRedisSerializer()))
                .entryTtl(Duration.ofMinutes(5));
        return RedisCacheManager.builder(factory)
                .cacheDefaults(config)
                .build();
    }

    // ---------- KeyGenerator ----------
    @Bean
    public KeyGenerator keyGenerator() {
        return (target, method, params) -> {
            StringBuilder sb = new StringBuilder();
            sb.append(target.getClass().getSimpleName()).append(":");
            sb.append(method.getName()).append(":");
            for (Object param : params) {
                sb.append(param.toString()).append(":");
            }
            return sb.toString();
        };
    }
}
