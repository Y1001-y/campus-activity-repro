package com.campus.activity.security;

import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 真实服务端签发的令牌；进程内有效，登出即撤销。不使用前端伪造值。 */
@Component
public class Tokens {
    public record Session(long userId, String username, String role, String realName) {}
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public String issue(Session s) {
        String token = UUID.randomUUID().toString().replace("-", "");
        sessions.put(token, s);
        return token;
    }
    public Session get(String token) { return token == null ? null : sessions.get(token); }
    public void revoke(String token) { if (token != null) sessions.remove(token); }
    public int size() { return sessions.size(); }
}
