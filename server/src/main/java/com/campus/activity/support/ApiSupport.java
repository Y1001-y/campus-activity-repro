package com.campus.activity.support;

import com.campus.activity.common.BizException;
import com.campus.activity.common.ResultCode;
import com.campus.activity.security.Tokens;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

public final class ApiSupport {
    private ApiSupport() {}
    public static Tokens.Session session(HttpServletRequest req) {
        return (Tokens.Session) req.getAttribute("session");
    }
    public static Tokens.Session requireLogin(HttpServletRequest req) {
        Tokens.Session s = session(req);
        if (s == null) throw new BizException(ResultCode.UNAUTHORIZED);
        return s;
    }
    public static Tokens.Session requireManager(HttpServletRequest req) {
        Tokens.Session s = requireLogin(req);
        if (!("ADMIN".equals(s.role()) || "ORGANIZER".equals(s.role())))
            throw new BizException(ResultCode.FORBIDDEN);
        return s;
    }
    public static String str(Map<?, ?> body, String key) {
        Object v = body == null ? null : body.get(key);
        return v == null ? "" : String.valueOf(v);
    }
    public static long lng(Object v) {
        try { return Long.parseLong(String.valueOf(v)); } catch (Exception e) { return -1; }
    }
}
