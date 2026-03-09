package com.example.Repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.Mapper.AIChatSessionMapper;
import com.example.Pojo.Entity.AI.AIChatSession;
import com.example.Repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.example.Common.Constants.RedisConstants.AI_CHAT_PREFIX;
import static com.example.Common.Utils.GetUserIdUtils.getCurrentUserId;

/**
 * 基于数据库的会话历史记录存储
 * 功能：仅存储会话索引（会话 ID 列表），不存储具体对话内容
 * 对话内容由 ChatMemory 在 Redis 中管理
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseChatHistoryRepository implements ChatHistoryRepository {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AIChatSessionMapper chatSessionMapper;

    @Override
    public void save(String type, String sessionId) {
        // 1.通过线程获取用户 Id
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.error("用户未登录，无法保存会话记录");
            return;
        }

        // 2.保存到数据库：创建或更新会话元数据
        saveOrUpdateSession(sessionId, userId);

        // 3.保存到 Redis：用于快速获取会话列表
        saveToRedis(userId, type, sessionId);

        log.info("保存会话记录成功，userId: {}, sessionId: {}", userId, sessionId);
    }

    /**
     * 保存或更新会话到数据库（仅元数据）
     * @param sessionId 会话 ID (String类型,与数据库表结构匹配)
     * @param userId 用户 ID
     */
    private void saveOrUpdateSession(String sessionId, Long userId) {
        AIChatSession existingSession = getChatSessionBySessionId(sessionId);

        if (existingSession == null) {
            // 第一次创建会话
            AIChatSession newSession = AIChatSession.builder()
                    .sessionId(sessionId)
                    .userId(userId.intValue())
                    .startTime(LocalDateTime.now())
                    .lastActiveTime(LocalDateTime.now())
                    .messageCount(0)
                    .status("active")
                    .build();

            chatSessionMapper.insert(newSession);
            log.info("创建新会话，sessionId: {}", sessionId);
        } else {
            // 更新已有会话（只更新活跃时间和消息数）
            AIChatSession updateSession = AIChatSession.builder()
                    .sessionId(existingSession.getSessionId())
                    .userId(userId.intValue())
                    .lastActiveTime(LocalDateTime.now())
                    .messageCount(existingSession.getMessageCount() + 1)
                    .status(existingSession.getStatus())
                    .build();

            chatSessionMapper.updateById(updateSession);
            log.debug("更新会话活跃时间，sessionId: {}", sessionId);
        }
    }

    /**
     * 保存会话 ID 到 Redis（用于快速查询列表）
     * @param userId 用户 ID
     *
     */
    private void saveToRedis(Long userId, String type, String sessionId) {
        String key = AI_CHAT_PREFIX + userId + ":" + type;
        redisTemplate.opsForSet().add(key, sessionId);
        // 设置 7 天过期时间
        redisTemplate.expire(key, 7, java.util.concurrent.TimeUnit.DAYS);
    }

    /**
     * 根据 sessionId 查询会话信息
     * @param sessionId 会话 ID
     * @return 会话信息
     */
    private AIChatSession getChatSessionBySessionId(String sessionId) {
        QueryWrapper<AIChatSession> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("session_id", sessionId);
        return chatSessionMapper.selectOne(queryWrapper);
    }

    /**
     * 获取会话列表
     * @param type 会话类型
     * @return 会话列表
     */
    @Override
    public List<String> getChatIds(String type) {
        log.error("=== [DEBUG] getChatIds 被调用，type: {} ===", type);
        Long userId = getCurrentUserId();
        log.error("=== [DEBUG] 从线程上下文获取 userId: {} ===", userId);
        if (userId == null) {
            log.error("=== [DEBUG] 用户未登录，userId 为 null ===");
            return List.of();
        }

        // 从 Redis 中获取会话 ID 列表
        String redisKey = AI_CHAT_PREFIX + userId + ":" + type;
        log.error("=== [DEBUG] Redis key: {} ===", redisKey);
        Set<String> sessionIds = redisTemplate.opsForSet().members(redisKey);
        log.error("=== [DEBUG] 从 Redis 获取到 sessionIds: {} ===", sessionIds);

        if (sessionIds == null || sessionIds.isEmpty()) {
            log.error("=== [DEBUG] Redis 中没有数据，返回空列表 ===");
            return List.of();
        }

        List<String> sortedList = sessionIds.stream().sorted(String::compareTo).toList();
        log.error("=== [DEBUG] 排序后的列表：{} ===", sortedList);
        return sortedList;
    }
}
