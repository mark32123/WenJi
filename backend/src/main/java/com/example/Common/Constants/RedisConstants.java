package com.example.Common.Constants;

import java.security.PublicKey;

public class RedisConstants {
    public static final String CAPTCHA_PREFIX= "captcha:"; //验证码前缀
    public static final long CAPTCHA_EXPIRE =300;          //验证码过期时间
    public static final String USER_LOGIN_KEY = "user:";   //用户登录前缀
    public static final long USER_LOGIN_EXPIRE = 30;       //用户登录过期时间


    public static final String AI_CHAT_PREFIX="chat:history:";//ai对话前缀

    //RedisChatHistory.java
    public static final long CHAT_HISTORY_EXPIRE=30;        //用户对话历史过期时间

    public static final  String CHAT_HISTORY_PREFIX = "chat:";  //用户存储对话前缀
}
