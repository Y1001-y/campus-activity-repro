package com.campus.activity.controller;

import com.campus.activity.common.*;
import com.campus.activity.security.Tokens;
import com.campus.activity.store.Db;
import com.campus.activity.support.ApiSupport;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FileController {
    private final Db db;
    private final Path uploadDir;
    private static final long MAX = 5L * 1024 * 1024;
    private static final List<String> ALLOWED = List.of("jpg", "jpeg", "png", "webp");

    public FileController(Db db, @Value("${campus.upload-dir}") String dir) throws Exception {
        this.db = db;
        this.uploadDir = Paths.get(dir).toAbsolutePath();
        Files.createDirectories(this.uploadDir);
    }

    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file, HttpServletRequest req) {
        if (file.isEmpty() || file.getSize() > MAX) throw new BizException(50001, "文件不能为空且不得超过 5MB");
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED.contains(ext)) throw new BizException(50001, "仅支持 jpg / jpeg / png / webp 格式");
        String name = "upload-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8) + "." + ext;
        try {
            Files.copy(file.getInputStream(), uploadDir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new BizException(ResultCode.SYSTEM_ERROR);
        }
        Tokens.Session s = ApiSupport.session(req);
        if (s != null) db.log(s.userId(), s.username(), "UPLOAD", "上传素材 " + name);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("url", "/uploads/" + name); out.put("name", name); out.put("size", file.getSize());
        return Result.ok(out);
    }

    @GetMapping("/uploads")
    public Result<List<String>> uploads() throws Exception {
        List<String> names = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadDir)) {
            for (Path p : stream) names.add(p.getFileName().toString());
        }
        Collections.sort(names);
        return Result.ok(names);
    }
}
