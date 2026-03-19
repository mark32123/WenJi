package com.example.Common.Utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Redis 聊天历史操作工具类
 * 提供纯 Redis 操作，不被 Spring 管理
 */
@RequiredArgsConstructor
public class RedisChatHistoryUtils {

    private final StringRedisTemplate redisTemplate;

    private static final String CHAT_HISTORY_KEY_PREFIX = "chat:history:";
    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60; // 7天过期

    /**
     * 保存会话到 Redis
     */
    public void save(String type, String sessionId, Long userId) {
        if (userId == null) {
            return;
        }

        // 按用户维度存储，使用有序集合，分数为当前时间戳
        String key = CHAT_HISTORY_KEY_PREFIX + type + ":" + userId;
        redisTemplate.opsForZSet().add(key, sessionId, System.currentTimeMillis());

        // 设置过期时间
        redisTemplate.expire(key, EXPIRE_TIME, java.util.concurrent.TimeUnit.SECONDS);
    }

    /**
     * 从 Redis 获取会话 ID 列表
     */
    public List<String> getChatIds(String type, Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }

        String key = CHAT_HISTORY_KEY_PREFIX + type + ":" + userId;
        // 按分数倒序获取（最新的会话在前）
        Set<String> chatIds = redisTemplate.opsForZSet().reverseRange(key, 0, -1);
        if (chatIds == null || chatIds.isEmpty()) {
            return Collections.emptyList();
        }
        return chatIds.stream().toList();
    }

    /**
     * 从 Redis 删除会话
     */
    public void delete(String type, String sessionId, Long userId) {
        if (userId == null) {
            return;
        }

        String key = CHAT_HISTORY_KEY_PREFIX + type + ":" + userId;
        redisTemplate.opsForZSet().remove(key, sessionId);
    }
}