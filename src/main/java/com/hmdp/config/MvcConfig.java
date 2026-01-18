package com.hmdp.config;

import com.hmdp.interceptor.LoginInterceptor;
import com.hmdp.interceptor.RefreshTokenInterceptor;
import com.hmdp.interceptor.RequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // 在这里使用 @Resource 注解注入模板，因为这是一个配置的 Bean，会级联将这些模板作为 Bean 进行注入
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/shop/**",
                        "/shop-type/**",
                        "/upload/**",
                        "/blog/hot"
                ).order(2);

        registry.addInterceptor(new RequestInterceptor()).addPathPatterns("/**").order(1);
        registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate)).order(0).addPathPatterns("/**");

    }
}
