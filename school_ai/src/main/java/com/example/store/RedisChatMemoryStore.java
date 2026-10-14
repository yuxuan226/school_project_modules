package com.example.store;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson;
import static dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson;

/**
 * Redis 聊天记忆存储
 * 将聊天历史保存到 Redis（2号库），支持多轮对话
 */
@Component
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final RedisTemplate<String, Object> redisTemplate;

    // Redis Key 前缀
    private static final String KEY_PREFIX = "chat:memory:";

    // 记忆保留时间（小时）
    private static final long TTL_HOURS = 24;

    /**
     * 构造函数注入
     * 使用 @Qualifier 指定使用 chatAiRedisTemplate（2 号库）
     */
    public RedisChatMemoryStore(
            @Qualifier("chatAiRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取指定用户的聊天历史
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = KEY_PREFIX + memoryId;
        String json = (String) redisTemplate.opsForValue().get(key);

        if (json == null || json.isEmpty()) {
            return List.of(); // 返回空列表，表示没有历史记忆
        }

        return messagesFromJson(json);
    }

    /**
     * 更新聊天历史
     */
    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = KEY_PREFIX + memoryId;
        String json = messagesToJson(messages);

        // 设置值并添加过期时间
        redisTemplate.opsForValue().set(key, json, TTL_HOURS, TimeUnit.HOURS);
    }

    /**
     * 删除指定用户的聊天历史
     */
    @Override
    public void deleteMessages(Object memoryId) {
        redisTemplate.delete(KEY_PREFIX + memoryId);
    }
}
