package com.example.Config;

import com.example.Interceptor.LoginInterceptor;
import com.example.Interceptor.RefreshTokenInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Autowired
    private RefreshTokenInterceptor refreshTokenInterceptor;

    /**
     * 注册拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        //拦截所有请求，做刷新令牌验证
        registry.addInterceptor(refreshTokenInterceptor)
                .order(0)//优先执行拦截器
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login","/user/captcha","/",
                        "/uploads/**",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html");
        //拦截所有请求，但是要避免拦截登录和注册接口
        registry.addInterceptor(loginInterceptor)//添加拦截器，但是要避免拦截登录和注册接口
                .addPathPatterns("/**")//拦截所有接口
                .excludePathPatterns("/user/login",
                        "/user/captcha",
                        "/",
                        "/uploads/**",
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                );


    }

    /**
     * 静态资源映射
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //映射文件上传路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:D:/develop/Projectes/WenJi/uploads/");
    }

    /**
     * 跨域配置
     * @param  registry 跨域注册
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //配置跨域请求
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Content-Disposition");
    }
}
