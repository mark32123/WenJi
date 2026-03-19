package com.example.Interceptor;

import com.example.Pojo.User;
import com.example.Common.Utils.JwtUtils;
import com.example.Common.Utils.ThreadLocalUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.Common.Constants.RedisConstants.USER_LOGIN_EXPIRE;
import static com.example.Common.Constants.RedisConstants.USER_LOGIN_KEY;

@Component
//@Component注解，将类注册为Spring组件，用于自动注入依赖
public class RefreshTokenInterceptor implements HandlerInterceptor {
    private final StringRedisTemplate stringRedisTemplate;

    //构造函数
    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 请求处理前拦截器
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器对象
     * @return 是否放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求的令牌,去掉Bearer前缀
        String authorization=request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            response.setStatus(401);
            response.getWriter().write("未登录");
            return false;
        }

        // 解析令牌
        String token=authorization.substring(7).trim();
        try {
            // 解析令牌
            JwtUtils.parseToken(token);
            String key=USER_LOGIN_KEY+token;

            // 从redis中获取用户数据
            Map<Object, Object> userMap=stringRedisTemplate.opsForHash().entries(key);

            // 如果用户数据为空，直接放行
            if(userMap.isEmpty()){
                response.setStatus(401);
                response.getWriter().write("Token已失效，请重新登录");
                return false;
            }

            //使用ObjectMapper的convertValue方法将userMap转为User对象
            ObjectMapper objectMapper=new ObjectMapper();
            User user=objectMapper.convertValue(userMap,User.class);

            //存储到ThreadLocal中
            Map<String,Object> claimsMap=new HashMap<>();
            claimsMap.put("userId",user.getUserId());
            claimsMap.put("username",user.getUsername());
            claimsMap.put("phone",user.getPhone());
            ThreadLocalUtil.set(claimsMap);

            //放行后重新设置令牌有效期
            stringRedisTemplate.expire(key,USER_LOGIN_EXPIRE, TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            //token解析失败，放行让LoginInterceptor进行处理
            return false;
        }
    }
    
    /**
     * 请求处理后拦截器
     * @param request 请求对象
     * @param response 响应对象
     * @param handler 处理器对象
     * @param ex 异常对象
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //请求处理完成后，清空ThreadLocal中的数据,防止内存泄露
        ThreadLocalUtil.remove();
    }
}
