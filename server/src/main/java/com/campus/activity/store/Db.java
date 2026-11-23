package com.campus.activity.store;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/** 直接读写真实 H2 数据库；所有业务数据来自 schema.sql / data.sql，不是内存假数据。 */
@Component
public class Db {
    public final JdbcTemplate jdbc;
    public Db(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> list(String sql, Object... args) { return jdbc.queryForList(sql, args); }
    public Map<String, Object> one(String sql, Object... args) {
        List<Map<String, Object>> rows = list(sql, args);
        return rows.isEmpty() ? null : rows.get(0);
    }
    public int count(String sql, Object... args) {
        Integer n = jdbc.queryForObject(sql, Integer.class, args);
        return n == null ? 0 : n;
    }
    public long nextId(String table) {
        return count("SELECT COALESCE(MAX(id), 0) + 1 FROM " + table);
    }
    public void log(Long userId, String username, String action, String detail) {
        jdbc.update("INSERT INTO op_log (id,user_id,username,action,detail,created_at) VALUES (?,?,?,?,?,?)",
                nextId("op_log"), userId, username, action, detail, now());
    }
    public static String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
