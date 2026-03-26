package com.example.Repository;


import com.example.Pojo.Msg;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.example.Common.Constants.RedisConstants.AI_CHAT_HISTORY_EXPIRE;
import static com.example.Common.Constants.RedisConstants.AI_CHAT_HISTORY_PREFIX;

@Slf4j
@RequiredArgsConstructor
@Component
//这个类的功能是存储会话记忆的类
public class RedisChatMemory implements ChatMemory {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;



    /**
     * 添加会话记忆消息
     * @param conversationId 会话 ID
     * @param messages 消息列表
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        String key=AI_CHAT_HISTORY_PREFIX+conversationId;

        try {
            //进行序列化 TODO：搞明白这是什么
            List<String> jsonList = messages.stream()
                    .map(Msg::new) //将消息转换为Msg对象
                    //将Msg对象转换为JSON字符串
                    .map(msg -> {
                        try {
                            return objectMapper.writeValueAsString(msg);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("JSON 序列化失败", e);
                        }
                    })
                    .toList();

            //rightPushAll将jsonList根据key添加到Redis列表的末尾
            redisTemplate.opsForList().rightPushAll(key, jsonList);

            // 设置过期时间（每次添加都刷新 TTL）
            redisTemplate.expire(key, AI_CHAT_HISTORY_EXPIRE, TimeUnit.MINUTES);

            log.debug("添加消息到 Redis，conversationId: {}, 消息数：{}", conversationId, messages.size());
        } catch (Exception e) {
            log.error("添加消息到 Redis 失败，conversationId: {}", conversationId, e);
        }
    }

    /**
     * 获取指定会话的记忆消息
     * @param conversationId 会话 ID
     * @param lastN 最多返回的记忆消息数量
     * @return 会话的记忆消息列表
     */
    @Override
    public List<Message> get(String conversationId, int lastN) {
        String key = AI_CHAT_HISTORY_PREFIX + conversationId;

        try {
            //在redis缓存中查找关于这个key的前N条信息
            List<String> jsonList = redisTemplate.opsForList().range(key, -lastN, -1);

            //为空，Redis中没有找到信息，返回空列表
            if (jsonList == null || jsonList.isEmpty()) {
                log.debug("Redis 中未找到会话记忆，conversationId: {}", conversationId);
                return Collections.emptyList();
            }
            //进行反序列化
            List<Message> messages = jsonList.stream()
                    .map(s -> {
                        try {
                            Msg msg = objectMapper.readValue(s, Msg.class);
                            return msg.toMessage();
                        } catch (JsonProcessingException e) {
                            log.error("JSON 反序列化失败", e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)//过滤掉null值
                    .toList();

            // 刷新 TTL
            redisTemplate.expire(key, AI_CHAT_HISTORY_EXPIRE, TimeUnit.MINUTES);

            log.debug("从 Redis 获取会话记忆，conversationId: {}, 消息数：{}", conversationId, messages.size());
            return messages;
        } catch (Exception e) {
            log.error("从 Redis 获取会话记忆失败，conversationId: {}", conversationId, e);
            return Collections.emptyList();
        }
    }


    /**
     * 清空指定会话的记忆消息
     * @param conversationId 会话 ID
     */
    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(AI_CHAT_HISTORY_PREFIX + conversationId);
    }
}
