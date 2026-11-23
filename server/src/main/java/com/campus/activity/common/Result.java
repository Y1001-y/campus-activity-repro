package com.campus.activity.common;

/** HTTP 响应统一包装；code=0 才表示业务成功。契约与课程 Result 一致。 */
public record Result<T>(int code, String message, T data) {
    public static <T> Result<T> ok(T data) { return new Result<>(0, "ok", data); }
    public static Result<Void> ok() { return ok(null); }
    public static <T> Result<T> fail(int code, String message) { return new Result<>(code, message, null); }
    public static <T> Result<T> fail(ResultCode code) { return fail(code.getCode(), code.getMessage()); }
}
