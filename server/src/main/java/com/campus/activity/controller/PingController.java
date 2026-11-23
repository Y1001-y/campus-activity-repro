package com.campus.activity.controller;

import com.campus.activity.common.Result;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PingController {
    @GetMapping("/ping")
    public Result<Map<String, Object>> ping() {
        return Result.ok(Map.of("status", "up", "service", "campus-activity"));
    }
}
