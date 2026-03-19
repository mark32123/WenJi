package com.example.Common.Utils;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

public class UersUtils {
    /**
     * 获取当前用户ID
     * @return 用户ID
     */
    public static Long getCurrentUserId(){
        Map<String,Object> claims = ThreadLocalUtil.get();
        if(claims==null){
            return null;
        }
        return (Long) claims.get("userId");
    }


    /**
     * 从请求中获取token
     * @param request 请求对象
     * @return token
     **/

    public static String getTokenFromRequest(HttpServletRequest request){
        String token = request.getHeader("Authorization");

        if(token == null){
            token = request.getHeader("authorization");
        }

        //处理Bearer前缀
        if(token != null && token.startsWith("Bearer ")){
            token = token.substring(7);
        }

        return token != null ? token.trim() : null;
    }
}
