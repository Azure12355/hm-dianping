package com.hmdp.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.Map;

@Slf4j
public class RequestInterceptor implements HandlerInterceptor {
    private static final String REQUEST_LOG_PREFIX = "========== 请求详情 ==========";
    private static final String REQUEST_LOG_SUFFIX = "========== 请求结束 ==========";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 打印请求开始标记
        log.info(REQUEST_LOG_PREFIX);
        
        // 基本信息
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String protocol = request.getProtocol();
        String remoteAddr = request.getRemoteAddr();
        HttpSession session = request.getSession(false);
        String sessionId = session != null ? session.getId() : "无会话";
        
        log.info("请求方法: {}", method);
        log.info("请求URI: {}", uri);
        log.info("协议版本: {}", protocol);
        log.info("客户端IP: {}", remoteAddr);
        log.info("会话ID: {}", sessionId);
        
        // 请求头信息
//        log.info("请求头信息:");
//        Enumeration<String> headerNames = request.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String headerName = headerNames.nextElement();
//            String headerValue = request.getHeader(headerName);
//            log.info("  {}: {}", headerName, headerValue);
//        }
        
        // 请求参数
        log.info("请求参数:");
        Map<String, String[]> parameterMap = request.getParameterMap();
        if (parameterMap.isEmpty()) {
            log.info("  无请求参数");
        } else {
            parameterMap.forEach((key, values) -> {
                if (values.length == 1) {
                    log.info("  {}: {}", key, values[0]);
                } else {
                    log.info("  {}: {}", key, String.join(", ", values));
                }
            });
        }
        
        // 注意：请求体不能在拦截器中读取，因为 InputStream 只能读取一次
        // 读取后会消耗掉请求体，导致后续 @RequestBody 无法获取内容
        
        // 打印请求结束标记
        log.info(REQUEST_LOG_SUFFIX);
        
        return true;
    }
}