package com.example;

import com.example.Config.WebConfig;
import com.example.Interceptor.LoginInterceptor;
import com.example.Interceptor.RefreshTokenInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebConfigTest {

    @Mock
    private LoginInterceptor loginInterceptor;

    @Mock
    private RefreshTokenInterceptor refreshTokenInterceptor;

    @Mock
    private InterceptorRegistry interceptorRegistry;

    @Mock
    private InterceptorRegistration interceptorRegistration;

    @Mock
    private CorsRegistry corsRegistry;

    @Mock
    private CorsRegistration corsRegistration;

    @InjectMocks
    private WebConfig webConfig;

    @BeforeEach
    void setUp() {
        // 设置跨域注册的mock行为
        when(corsRegistry.addMapping(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedOrigins(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.allowedMethods(any(String[].class))).thenReturn(corsRegistration);
        when(corsRegistration.allowedHeaders(anyString())).thenReturn(corsRegistration);
        when(corsRegistration.exposedHeaders(anyString())).thenReturn(corsRegistration);
    }

    @Test
    void testAddInterceptors() {
        // 执行方法
        webConfig.addInterceptors(interceptorRegistry);

        // 验证refreshTokenInterceptor的注册
        verify(interceptorRegistry, times(1)).addInterceptor(refreshTokenInterceptor);
        verify(interceptorRegistration, times(1)).order(0);
        verify(interceptorRegistration, times(1)).addPathPatterns("/**");
        verify(interceptorRegistration, times(1)).excludePathPatterns("/user/login", "/user/captcha", "/");

    }

    @Test
    void testAddCorsMappings() {
        // 执行方法
        webConfig.addCorsMappings(corsRegistry);

        // 验证跨域配置
        verify(corsRegistry, times(1)).addMapping("/**");
        verify(corsRegistration, times(1)).allowedOrigins("*");
        verify(corsRegistration, times(1)).allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
        verify(corsRegistration, times(1)).allowedHeaders("*");
        verify(corsRegistration, times(1)).exposedHeaders("Content-Disposition");
    }
}
