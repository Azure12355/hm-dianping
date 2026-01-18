package com.hmdp.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

public class RefreshTokenInterceptor implements HandlerInterceptor {


    private final StringRedisTemplate stringRedisTemplate;

    // 因为 LoginInterceptor 本身并不是通过Bean 注入的所以 stringRedisTemplate 也必须使用构造器而不能使用 @Resource 注解
    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.从 Header 中取出 token
        String token = request.getHeader("authorization");
        if (StringUtils.isBlank(token)) {
            // 1.1 直接放行
            return true;
        }

        // 拼装 tokenKey
        String tokenKey = LOGIN_USER_KEY + token;
        // 2. 从 Redis 中获取 userMap
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash().entries(tokenKey);
        if (userMap.isEmpty()) {
            // 2.1 如果 userMap 为空，说明过期了，直接放行
            return true;
        }

        // 3. 将 userMap 转换为 UserDTO 并保存到 ThreadLocal 中
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        UserHolder.saveUser(userDTO);

        // 4. 更新时间（每次获取到了 user，都需要更新对应的 expire 时间，只有超过 30min 未访问，对应的数据才会消失）
        stringRedisTemplate.expire(tokenKey, LOGIN_USER_TTL, TimeUnit.MINUTES);

        // 5.放行
        return true;
    }
}
