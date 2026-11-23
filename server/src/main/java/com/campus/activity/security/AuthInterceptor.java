package com.campus.activity.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 只解析 Bearer 令牌并把会话挂到请求上；是否要求登录/角色由各接口显式声明。 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final Tokens tokens;
    public AuthInterceptor(Tokens tokens) { this.tokens = tokens; }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            Tokens.Session s = tokens.get(header.substring(7));
            if (s != null) req.setAttribute("session", s);
        }
        return true;
    }
}
