package com.campus.activity.controller;

import com.campus.activity.common.*;
import com.campus.activity.store.Db;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class ActivityController {
    private final Db db;
    public ActivityController(Db db) { this.db = db; }

    @GetMapping("/categories")
    public Result<List<Map<String, Object>>> categories() {
        return Result.ok(db.list("SELECT id, name, sort FROM activity_category ORDER BY sort"));
    }

    @GetMapping("/activities")
    public Result<Map<String, Object>> activities(@RequestParam(required = false) Long categoryId,
                                                  @RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) String status,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        StringBuilder sql = new StringBuilder("SELECT a.*, c.name AS category_name FROM activity a "
                + "LEFT JOIN activity_category c ON a.category_id = c.id WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (categoryId != null && categoryId > 0) { sql.append(" AND a.category_id = ?"); args.add(categoryId); }
        if (keyword != null && !keyword.isBlank()) { sql.append(" AND a.title LIKE ?"); args.add("%" + keyword + "%"); }
        if (status != null && !status.isBlank()) { sql.append(" AND a.status = ?"); args.add(status); }
        else sql.append(" AND a.status = 'PUBLISHED'");
        sql.append(" ORDER BY a.id");
        List<Map<String, Object>> all = db.list(sql.toString(), args.toArray());
        int from = Math.min((page - 1) * size, all.size());
        int to = Math.min(from + size, all.size());
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("list", new ArrayList<>(all.subList(from, to)));
        out.put("total", all.size()); out.put("page", page); out.put("size", size);
        return Result.ok(out);
    }

    @GetMapping("/activities/{id}")
    public Result<Map<String, Object>> detail(@PathVariable long id) {
        Map<String, Object> a = db.one("SELECT a.*, c.name AS category_name FROM activity a "
                + "LEFT JOIN activity_category c ON a.category_id = c.id WHERE a.id = ?", id);
        if (a == null) throw new BizException(ResultCode.ACTIVITY_NOT_FOUND);
        Map<String, Object> out = new LinkedHashMap<>(a);
        out.put("signedUp", db.count("SELECT COUNT(*) FROM activity_signup WHERE activity_id = ?", id));
        return Result.ok(out);
    }
}
