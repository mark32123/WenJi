package com.example.Controller;

import com.example.Common.Result;
import com.example.Utils.CaptchaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.Utils.RedisConstants.CAPTCHA_EXPIRE;
import static com.example.Utils.RedisConstants.CAPTCHA_PREFIX;
import static java.util.UUID.randomUUID;

@Slf4j
@RestController
@RequestMapping("/user")
public class CaptchaController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 获取图形验证码
     * @return 验证码
     */
    @GetMapping("/captcha")
    public Result<Map<String,String>>getCaptcha(){
        //生成验证码
        CaptchaUtils.CaptchaResult captchaResult = CaptchaUtils.generateCaptcha();

        //生成唯一标识
        String captchaKey = randomUUID().toString();
        log.info("验证码：{},验证标识{}",captchaResult.getCode(),captchaKey);
        //存到Redis缓存中
        stringRedisTemplate.opsForValue().set(
                CAPTCHA_PREFIX + captchaKey,
                captchaResult.getCode().toLowerCase(),
                CAPTCHA_EXPIRE, TimeUnit.MINUTES);

        //返回结果
        Map<String ,String> result=new HashMap<>();
        result.put("captchaKey",captchaKey);
        result.put("captchaImage","data:image/png;base64,"+captchaResult.getBase64Image());
        return Result.success(result);
    }
}
