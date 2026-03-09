package com.example.Common.Utils;

import java.util.Map;

public class GetUserIdUtils {
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
}
