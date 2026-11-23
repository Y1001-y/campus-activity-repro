package com.campus.activity.controller;

import com.campus.activity.common.*;
import com.campus.activity.security.Tokens;
import com.campus.activity.store.Db;
import com.campus.activity.support.ApiSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class StudentController {
    private final Db db;
    public StudentController(Db db) { this.db = db; }

    @PostMapping("/activities/{id}/signup")
    public Result<Map<String, Object>> signup(@PathVariable long id, HttpServletRequest req) {
        Tokens.Session s = ApiSupport.requireLogin(req);
        Map<String, Object> a = db.one("SELECT * FROM activity WHERE id = ?", id);
        if (a == null) throw new BizException(ResultCode.ACTIVITY_NOT_FOUND);
        if (!"PUBLISHED".equals(String.valueOf(a.get("status"))))
            throw new BizException(ResultCode.ACTIVITY_NOT_OPEN);
        if (db.count("SELECT COUNT(*) FROM activity_signup WHERE activity_id = ? AND user_id = ?", id, s.userId()) > 0)
            throw new BizException(ResultCode.SIGNUP_REPEATED);
        int capacity = ((Number) a.get("capacity")).intValue();
        int signed = db.count("SELECT COUNT(*) FROM activity_signup WHERE activity_id = ?", id);
        if (signed >= capacity) throw new BizException(ResultCode.SIGNUP_FULL);
        long sid = db.nextId("activity_signup");
        db.jdbc.update("INSERT INTO activity_signup (id,activity_id,user_id,status,created_at) VALUES (?,?,?,?,?)",
                sid, id, s.userId(), "APPROVED", Db.now());
        db.jdbc.update("UPDATE activity SET signed_up = ? WHERE id = ?", signed + 1, id);
        db.log(s.userId(), s.username(), "SIGNUP", "报名活动 " + id);
        return Result.ok(Map.of("signupId", sid, "activityId", id, "status", "APPROVED"));
    }

    @GetMapping("/me/signups")
    public Result<List<Map<String, Object>>> mySignups(HttpServletRequest req) {
        Tokens.Session s = ApiSupport.requireLogin(req);
        return Result.ok(db.list("SELECT s.id, s.status, s.created_at, a.id AS activity_id, a.title, "
                + "a.place, a.start_time, a.checkin_code FROM activity_signup s "
                + "JOIN activity a ON a.id = s.activity_id WHERE s.user_id = ? ORDER BY s.id DESC", s.userId()));
    }

    @DeleteMapping("/signups/{id}")
    public Result<Void> cancel(@PathVariable long id, HttpServletRequest req) {
        Tokens.Session s = ApiSupport.requireLogin(req);
        Map<String, Object> row = db.one("SELECT * FROM activity_signup WHERE id = ? AND user_id = ?", id, s.userId());
        if (row == null) throw new BizException(ResultCode.SIGNUP_NOT_FOUND);
        if (!"APPROVED".equals(String.valueOf(row.get("status"))))
            throw new BizException(ResultCode.SIGNUP_STATUS_INVALID);
        db.jdbc.update("DELETE FROM activity_signup WHERE id = ?", id);
        db.jdbc.update("UPDATE activity SET signed_up = (SELECT COUNT(*) FROM activity_signup WHERE activity_id = ?) "
                + "WHERE id = ?", row.get("activity_id"), row.get("activity_id"));
        db.log(s.userId(), s.username(), "CANCEL_SIGNUP", "取消报名 " + id);
        return Result.ok();
    }

    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        Tokens.Session s = ApiSupport.requireLogin(req);
        long activityId = ApiSupport.lng(body.get("activityId"));
        String code = ApiSupport.str(body, "code");
        Map<String, Object> a = db.one("SELECT * FROM activity WHERE id = ?", activityId);
        if (a == null) throw new BizException(ResultCode.ACTIVITY_NOT_FOUND);
        if (code.isBlank() || !code.equals(String.valueOf(a.get("checkin_code"))))
            throw new BizException(ResultCode.CHECKIN_TOKEN_INVALID);
        if (!"PUBLISHED".equals(String.valueOf(a.get("status"))))
            throw new BizException(ResultCode.CHECKIN_CLOSED);
        if (db.count("SELECT COUNT(*) FROM activity_signup WHERE activity_id = ? AND user_id = ? AND status = 'APPROVED'",
                activityId, s.userId()) == 0)
            throw new BizException(ResultCode.CHECKIN_NOT_SIGNED_UP);
        if (db.count("SELECT COUNT(*) FROM activity_checkin WHERE activity_id = ? AND user_id = ?", activityId, s.userId()) > 0)
            throw new BizException(ResultCode.CHECKIN_REPEATED);
        db.jdbc.update("INSERT INTO activity_checkin (id,activity_id,user_id,checked_in_at) VALUES (?,?,?,?)",
                db.nextId("activity_checkin"), activityId, s.userId(), Db.now());
        db.log(s.userId(), s.username(), "CHECKIN", "签到活动 " + activityId);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("activityId", activityId); out.put("title", a.get("title"));
        out.put("checkedInAt", Db.now()); out.put("message", "签到成功");
        return Result.ok(out);
    }

    @GetMapping("/notices")
    public Result<List<Map<String, Object>>> notices(HttpServletRequest req) {
        Tokens.Session s = ApiSupport.requireLogin(req);
        return Result.ok(db.list("SELECT * FROM sys_notice WHERE user_id = ? ORDER BY id DESC", s.userId()));
    }
}
