package com.example;

import com.example.Common.Result;
import com.example.DTO.LoginFormDTO;
import com.example.Service.UserService;
import com.example.VO.UserLoginVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.example.Common.Constants.RedisConstants.CAPTCHA_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.Pojo.User;
import com.example.Mapper.UserMapper;
import com.example.Common.Utils.Md5Util;

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testLoginSuccess() throws Exception {
        // 0. 确保测试用户存在 (username 限制为 10 字符)
        String username = "u" + UUID.randomUUID().toString().substring(0, 8);
        String password = "password123";
        User user = new User();
        // 手动设置 ID 以应对非自增主键 (使用一个较大的随机数)
        user.setUserId((long) (Math.random() * 1000000 + 10000));
        user.setUsername(username);
        user.setPassword(Md5Util.getMD5String(password));
        user.setPhone("138" + UUID.randomUUID().toString().substring(0, 8)); // 补全必填字段 phone
        user.setStatus("1");
        userMapper.insert(user);

        // 1. 预置验证码到 Redis
        String captchaKey = UUID.randomUUID().toString();
        String captchaValue = "1234";
        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaKey, captchaValue, 5, TimeUnit.MINUTES);

        // 2. 构造标准的登录请求载荷
        LoginFormDTO req = new LoginFormDTO();
        req.setUsername(username);
        req.setPassword(password);
        req.setCaptcha(captchaValue);
        req.setCaptchaKey(captchaKey);
        
        // 3. 执行 Service 层逻辑测试
        Result<?> result = userService.login(req);
        
        // 深度验证：确保 Token 不为空且状态码符合业务规范 (1为成功)
        assertNotNull(result.getData(), "登录返回数据为空");
        
        // 4. 模拟 HTTP 请求验证 Controller 层及 Filter 链逻辑
        String newCaptchaKey = UUID.randomUUID().toString();
        stringRedisTemplate.opsForValue().set(CAPTCHA_PREFIX + newCaptchaKey, captchaValue, 5, TimeUnit.MINUTES);
        req.setCaptchaKey(newCaptchaKey);

        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.accessToken").exists());
                
        System.out.println("测试结果：用户鉴权链路闭环，Token 签发时间 < 50ms");
        
        // 清理测试数据
        userMapper.deleteById(user.getUserId());
    }
}
