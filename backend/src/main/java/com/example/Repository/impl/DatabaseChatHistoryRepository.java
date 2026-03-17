package com.example.Repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.Common.Utils.RedisChatHistoryUtils;
import com.example.Mapper.AIChatSessionMapper;
import com.example.Pojo.Entity.AI.AIChatSession;
import com.example.Repository.ChatHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基于数据库的会话历史记录存储
 * 功能：仅存储会话索引（会话 ID 列表），不存储具体对话内容
 * 对话内容由 ChatMemory 在 Redis 中管理
 */

@Slf4j
@Component

public class DatabaseChatHistoryRepository implements ChatHistoryRepository {

    @Autowired
    private  StringRedisTemplate redisTemplate;

    private final AIChatSessionMapper chatSessionMapper;
    
    // Redis 操作工具类（在构造函数中初始化）
    private RedisChatHistoryUtils redisUtils;
    
    // 构造函数，在注入依赖后初始化 redisUtils
   public DatabaseChatHistoryRepository(StringRedisTemplate redisTemplate, 
                                        AIChatSessionMapper chatSessionMapper) {
        this.redisTemplate = redisTemplate;
        this.chatSessionMapper = chatSessionMapper;
        this.redisUtils = new RedisChatHistoryUtils(redisTemplate);
    }


    @Override
    public void save(String type, String sessionId,Long userId) {

        // 1.保存到 Redis：用于快速获取会话列表，通过 redisUtils 进行
        redisUtils.save(type, sessionId, userId);

        // 2.保存到数据库：创建或更新会话元数据
        saveOrUpdateSession(sessionId, userId);
        log.info("保存会话记录成功，userId: {}, sessionId: {}", userId, sessionId);
    }

    /**
     * 保存或更新会话到数据库（仅元数据）
     * @param sessionId 会话 ID (String类型,与数据库表结构匹配)
     * @param userId 用户 ID
     */
    private void saveOrUpdateSession(String sessionId, Long userId) {
        AIChatSession existingSession = getChatSessionBySessionId(sessionId);

        //判断该对话是否属于该用户
        if (existingSession != null && !existingSession.getUserId().equals(userId)) {
            log.error("会话归属权不匹配，sessionId: {}, 期望 userId: {}, 实际 userId: {}",
                    sessionId, existingSession.getUserId(), userId);
            throw new RuntimeException("无权操作该会话");
        }

        if (existingSession == null) {
            // 第一次创建会话
            AIChatSession newSession = AIChatSession.builder()
                    .sessionId(sessionId)
                    .userId(userId)
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
                    .userId(existingSession.getUserId())  // 保持原有的 userId
                    .lastActiveTime(LocalDateTime.now())
                    .messageCount(existingSession.getMessageCount() + 1)
                    .status(existingSession.getStatus())
                    .currentLocation(existingSession.getCurrentLocation())
                    .sessionContext(existingSession.getSessionContext())
                    .build();

            chatSessionMapper.updateById(updateSession);
            log.debug("更新会话活跃时间，sessionId: {}", sessionId);
        }
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
        // 委托给 RedisChatHistoryUtils 进行操作
       return redisUtils.getChatIds(type);
    }

    /**
     * 删除会话
     * @param type 业务类型，如：chat、service、pdf
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String type, String sessionId, Long userId) {
        // 1. 先操作 Redis
        redisUtils.delete(type, sessionId, userId);

        // 2. 验证权限并从数据库中删除
        AIChatSession session= getChatSessionBySessionId(sessionId);
        
        // 判断是否是该对话的拥有者操作，验证权限
        if (session == null || !session.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除会话");
        }

        //然后对数据库进行操作
        AIChatSession updateSession=AIChatSession.builder()
                .sessionId(session.getSessionId())
                .userId(session.getUserId())
                .status("deleted")
                .lastActiveTime(LocalDateTime.now())
                .build();

        int delete =chatSessionMapper.deleteById(updateSession);
        if(delete ==0){
            throw new RuntimeException("数据删除异常");
        }

        log.info("删除会话成功：sessionId:{}",sessionId);
    }
}
