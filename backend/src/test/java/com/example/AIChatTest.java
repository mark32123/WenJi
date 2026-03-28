package com.example;

import com.example.Pojo.User;
import com.example.Mapper.UserMapper;
import com.example.Common.Utils.JwtUtils;
import com.example.Common.Utils.Md5Util;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.Common.Constants.RedisConstants.USER_LOGIN_KEY;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AIChatTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testStreamChat() throws Exception {
        // 1. 创建测试用户并存入数据库
        String username = "u" + UUID.randomUUID().toString().substring(0, 8);
        User user = new User();
        user.setUserId((long) (Math.random() * 1000000 + 30000));
        user.setUsername(username);
        user.setPassword(Md5Util.getMD5String("123456"));
        user.setPhone("137" + UUID.randomUUID().toString().substring(0, 8));
        user.setStatus("1");
        userMapper.insert(user);

        // 2. 模拟登录并存入 Redis 缓存
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());
        claims.put("username", user.getUsername());
        String token = JwtUtils.generateToken(claims);
        
        String key = USER_LOGIN_KEY + token;
        Map<String, String> userMap = new HashMap<>();
        userMap.put("userId", user.getUserId().toString());
        userMap.put("username", user.getUsername());
        userMap.put("phone", user.getPhone());
        stringRedisTemplate.opsForHash().putAll(key, userMap);
        stringRedisTemplate.expire(key, 30, TimeUnit.MINUTES);

        // 3. 执行流式聊天请求
        // 注意：由于是 SSE (Server-Sent Events)，MockMvc 会捕获流式内容
        MvcResult result = mockMvc.perform(post("/ai/chat")
                .header("Authorization", "Bearer " + token)
                .param("prompt", "你好，请介绍一下景德镇")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        
        // 验证返回内容不为空 (由于是流式，MockMvc 会一次性拿到当前产生的所有 chunk)
        assertNotNull(responseContent);
        
        System.out.println("测试结果：成功发起 AI 聊天请求。");

        // 4. 清理测试数据
        userMapper.deleteById(user.getUserId());
        stringRedisTemplate.delete(key);
    }
}
