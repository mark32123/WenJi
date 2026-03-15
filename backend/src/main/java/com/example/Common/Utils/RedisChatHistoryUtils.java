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

    /**
     * 保存会话到 Redis
     */
   public void save(String type, String chatId, Long userId) {
        // 只存储纯 chatId，不添加 userId 前缀（与数据库保持一致）
        redisTemplate.opsForSet().add(CHAT_HISTORY_KEY_PREFIX + type, chatId);
    }

    /**
     * 从 Redis 获取会话 ID 列表
     */
   public List<String> getChatIds(String type) {
        Set<String> chatIds = redisTemplate.opsForSet().members(CHAT_HISTORY_KEY_PREFIX + type);
        if (chatIds == null || chatIds.isEmpty()) {
            return Collections.emptyList();
        }
        return chatIds.stream().sorted(String::compareTo).toList();
    }

    /**
     * 从 Redis 删除会话
     */
   public void delete(String type, String sessionId, Long userId) {
        String key = CHAT_HISTORY_KEY_PREFIX + type;
        // 删除时使用纯 sessionId
        redisTemplate.opsForSet().remove(key, sessionId);
    }
}
