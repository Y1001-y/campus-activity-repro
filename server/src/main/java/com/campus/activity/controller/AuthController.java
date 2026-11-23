package com.campus.activity.controller;

import com.campus.activity.common.*;
import com.campus.activity.security.Tokens;
import com.campus.activity.store.Db;
import com.campus.activity.support.ApiSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final Db db;
    private final Tokens tokens;
    /** 本机真实生成并保存的验证码；未接短信网关，见 README 技术取舍。 */
    private final ConcurrentHashMap<String, String> smsCodes = new ConcurrentHashMap<>();

    public AuthController(Db db, Tokens tokens) { this.db = db; this.tokens = tokens; }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        String username = ApiSupport.str(body, "username");
        String password = ApiSupport.str(body, "password");
        Map<String, Object> u = db.one("SELECT * FROM sys_user WHERE username = ?", username);
        if (u == null || !password.equals(String.valueOf(u.get("password"))))
            throw new BizException(ResultCode.LOGIN_FAILED);
        if (((Number) u.get("status")).intValue() != 1) throw new BizException(10006, "账号已被禁用");
        Tokens.Session s = new Tokens.Session(((Number) u.get("id")).longValue(), username,
                String.valueOf(u.get("role")), String.valueOf(u.get("real_name")));
        String token = tokens.issue(s);
        db.log(s.userId(), username, "LOGIN", "登录成功");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("token", token);
        out.put("user", view(u));
        return Result.ok(out);
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) tokens.revoke(h.substring(7));
        Tokens.Session s = ApiSupport.session(req);
        if (s != null) db.log(s.userId(), s.username(), "LOGOUT", "退出登录");
        return Result.ok();
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me(HttpServletRequest req) {
        Tokens.Session s = ApiSupport.requireLogin(req);
        Map<String, Object> u = db.one("SELECT * FROM sys_user WHERE id = ?", s.userId());
        return Result.ok(view(u));
    }

    @PostMapping("/sms/send")
    public Result<Map<String, Object>> sendSms(@RequestBody Map<String, Object> body) {
        String phone = ApiSupport.str(body, "phone");
        if (!phone.matches("1\\d{10}")) throw new BizException(ResultCode.PARAM_ERROR);
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(0, 1000000));
        smsCodes.put(phone, code);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("phone", phone); out.put("code", code); out.put("expiresIn", 300);
        out.put("note", "本机自签验证码，未接短信网关；仅用于课堂联调");
        return Result.ok(out);
    }

    @PostMapping("/bind")
    public Result<Map<String, Object>> bind(@RequestBody Map<String, Object> body) {
        String studentNo = ApiSupport.str(body, "studentNo");
        String phone = ApiSupport.str(body, "phone");
        String code = ApiSupport.str(body, "code");
        String expected = smsCodes.get(phone);
        if (expected == null || !expected.equals(code))
            throw new BizException(40001, "验证码无效或已过期，请重新获取");
        Map<String, Object> u = db.one("SELECT * FROM sys_user WHERE student_no = ?", studentNo);
        if (u == null) throw new BizException(ResultCode.STUDENT_NO_NOT_FOUND);
        if (u.get("phone") != null && !String.valueOf(u.get("phone")).isBlank()
                && !String.valueOf(u.get("phone")).equals(phone))
            throw new BizException(ResultCode.STUDENT_NO_BOUND);
        db.jdbc.update("UPDATE sys_user SET phone = ? WHERE id = ?", phone, u.get("id"));
        smsCodes.remove(phone);
        return Result.ok(view(db.one("SELECT * FROM sys_user WHERE id = ?", u.get("id"))));
    }

    private Map<String, Object> view(Map<String, Object> u) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (u == null) return m;
        m.put("id", u.get("id")); m.put("username", u.get("username"));
        m.put("realName", u.get("real_name")); m.put("role", u.get("role"));
        m.put("studentNo", u.get("student_no")); m.put("phone", u.get("phone"));
        return m;
    }
}
