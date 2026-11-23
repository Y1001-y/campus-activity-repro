package com.campus.activity.controller;

import com.campus.activity.common.*;
import com.campus.activity.security.Tokens;
import com.campus.activity.store.Db;
import com.campus.activity.support.ApiSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 所有接口在进入业务逻辑前先校验管理员/组织者角色，低权限令牌返回 10002。 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final Db db;
    public AdminController(Db db) { this.db = db; }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard(HttpServletRequest req) {
        Tokens.Session s = ApiSupport.requireManager(req);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("operator", s.username());
        out.put("activityCount", db.count("SELECT COUNT(*) FROM activity"));
        out.put("publishedCount", db.count("SELECT COUNT(*) FROM activity WHERE status = 'PUBLISHED'"));
        out.put("pendingAudit", db.count("SELECT COUNT(*) FROM activity WHERE audit_status = 'PENDING'"));
        out.put("signupCount", db.count("SELECT COUNT(*) FROM activity_signup"));
        out.put("checkinCount", db.count("SELECT COUNT(*) FROM activity_checkin"));
        out.put("userCount", db.count("SELECT COUNT(*) FROM sys_user"));
        return Result.ok(out);
    }

    @GetMapping("/activities")
    public Result<List<Map<String, Object>>> activities(HttpServletRequest req) {
        ApiSupport.requireManager(req);
        return Result.ok(db.list("SELECT a.*, c.name AS category_name FROM activity a "
                + "LEFT JOIN activity_category c ON a.category_id = c.id ORDER BY a.id DESC"));
    }

    @PostMapping("/activities")
    public Result<Map<String, Object>> publish(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        Tokens.Session s = ApiSupport.requireManager(req);
        String title = ApiSupport.str(body, "title");
        long categoryId = ApiSupport.lng(body.get("categoryId"));
        int capacity = Integer.parseInt(ApiSupport.str(body, "capacity").isBlank() ? "0" : ApiSupport.str(body, "capacity"));
        if (title.isBlank() || categoryId <= 0 || capacity <= 0) throw new BizException(ResultCode.PARAM_ERROR);
        long id = db.nextId("activity");
        String code = "CK-2026-" + id;
        db.jdbc.update("INSERT INTO activity (id,title,category_id,cover,place,start_time,end_time,signup_deadline,"
                + "capacity,signed_up,status,audit_status,organizer_id,description,checkin_code) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", id, title, categoryId,
                ApiSupport.str(body, "cover"), ApiSupport.str(body, "place"),
                ApiSupport.str(body, "startTime"), ApiSupport.str(body, "endTime"),
                ApiSupport.str(body, "signupDeadline"), capacity, 0, "DRAFT", "PENDING", s.userId(),
                ApiSupport.str(body, "description"), code);
        db.log(s.userId(), s.username(), "PUBLISH_ACTIVITY", "发布活动 " + id + " " + title);
        return Result.ok(Map.of("id", id, "checkinCode", code, "auditStatus", "PENDING"));
    }

    @PostMapping("/activities/{id}/audit")
    public Result<Map<String, Object>> audit(@PathVariable long id,
                                             @RequestBody Map<String, Object> body, HttpServletRequest req) {
        Tokens.Session s = ApiSupport.requireManager(req);
        Map<String, Object> a = db.one("SELECT * FROM activity WHERE id = ?", id);
        if (a == null) throw new BizException(ResultCode.ACTIVITY_NOT_FOUND);
        boolean approve = Boolean.parseBoolean(ApiSupport.str(body, "approve"));
        db.jdbc.update("UPDATE activity SET audit_status = ?, status = ? WHERE id = ?",
                approve ? "APPROVED" : "REJECTED", approve ? "PUBLISHED" : "DRAFT", id);
        db.log(s.userId(), s.username(), "AUDIT_ACTIVITY", "审核活动 " + id + " -> " + (approve ? "APPROVED" : "REJECTED"));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("id", id); out.put("auditStatus", approve ? "APPROVED" : "REJECTED");
        out.put("status", approve ? "PUBLISHED" : "DRAFT");
        return Result.ok(out);
    }

    @GetMapping("/users")
    public Result<List<Map<String, Object>>> users(HttpServletRequest req) {
        ApiSupport.requireManager(req);
        return Result.ok(db.list("SELECT id, username, real_name, role, student_no, phone, status FROM sys_user ORDER BY id"));
    }

    @GetMapping("/signups")
    public Result<List<Map<String, Object>>> signups(@RequestParam(required = false) Long activityId,
                                                     HttpServletRequest req) {
        ApiSupport.requireManager(req);
        String sql = "SELECT s.*, u.real_name, u.student_no, a.title FROM activity_signup s "
                + "JOIN sys_user u ON u.id = s.user_id JOIN activity a ON a.id = s.activity_id";
        List<Map<String, Object>> rows = activityId == null ? db.list(sql + " ORDER BY s.id")
                : db.list(sql + " WHERE s.activity_id = ? ORDER BY s.id", activityId);
        return Result.ok(rows);
    }

    @PostMapping("/checkin/verify")
    public Result<Map<String, Object>> verify(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        Tokens.Session s = ApiSupport.requireManager(req);
        long activityId = ApiSupport.lng(body.get("activityId"));
        long userId = ApiSupport.lng(body.get("userId"));
        if (db.count("SELECT COUNT(*) FROM activity_checkin WHERE activity_id = ? AND user_id = ?", activityId, userId) == 0)
            throw new BizException(40002, "该用户尚未签到，无法核销");
        db.log(s.userId(), s.username(), "VERIFY_CHECKIN", "核销活动 " + activityId + " 用户 " + userId);
        return Result.ok(Map.of("activityId", activityId, "userId", userId, "verified", true));
    }

    @GetMapping("/logs")
    public Result<List<Map<String, Object>>> logs(HttpServletRequest req) {
        ApiSupport.requireManager(req);
        return Result.ok(db.list("SELECT * FROM op_log ORDER BY id DESC LIMIT 100"));
    }
}
