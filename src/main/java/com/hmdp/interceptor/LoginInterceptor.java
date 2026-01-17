package com.hmdp.interceptor;

import com.hmdp.entity.User;
import com.hmdp.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 当前请求执行完毕后移除 user 避免内存泄露
        UserHolder.removeUser();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1.从 request 中取出 session
        HttpSession session = request.getSession();
        // 2.从 session 中取出对应的 user
        User user =(User) session.getAttribute("user");
        // 3.判断 user 是否为空
        if (user == null) {
            // 3.1 user 不存在，直接拦截报错并返回 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        // 4.保存用户到 ThreadLocal 中
        UserHolder.saveUser(user.getUserDTO());

        // 5.放行
        return true;
    }
}
