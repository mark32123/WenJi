package com.example.Interceptor;

import com.example.Common.Utils.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//在刷新令牌拦截器中配置了拦截所有请求，所以这里只处理其他请求
        //        // 获取请求的 URL
//        String uri = request.getRequestURI();
//
//        // 检查是否为登录或注册请求，如果是则放行
//        if ("/user/login".equals(uri) || "/user/register".equals(uri)) {
//            return true;
//        }
//
//        String token = request.getHeader("Authorization");
//        if (token == null || token.isEmpty()) {
//            // 如果没有token，返回401
//            response.setStatus(401);
//            return false;
//        }
//
//        // 移除"Bearer "前缀（如果存在）
//        if (token.startsWith("Bearer ")) {
//            token = token.substring(7).trim();
//        }
//
//        try{
//            // 直接验证JWT令牌（无需检查Redis，因为RefreshTokenInterceptor会处理）
//            Map<String,Object> claims= JwtUtils.parseToken(token);
//            //把业务数据存储到ThreadLocal中
//            ThreadLocalUtil.set(claims);
//
//            return true;//放行
//        }catch (Exception e){
//            //http状态码为401
//            response.setStatus(401);
//            return false;
//        }
        
        if(ThreadLocalUtil.get()==null){
            response.setStatus(401);
            return false;
        }
        return true;
    }
    
    /**
     * 请求处理完成后，清空ThreadLocal中的数据,防止内存泄露
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //请求处理完成后，清空ThreadLocal中的数据,防止内存泄露
        ThreadLocalUtil.remove();
    }
}
