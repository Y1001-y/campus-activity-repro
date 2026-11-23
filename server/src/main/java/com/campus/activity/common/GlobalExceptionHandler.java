package com.campus.activity.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 所有异常都收敛成统一响应体，HTTP 恒为 200，由前端按 code 分支处理。 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) { return Result.fail(e.getCode(), e.getMessage()); }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        return Result.fail(ResultCode.SYSTEM_ERROR.getCode(),
                ResultCode.SYSTEM_ERROR.getMessage() + " [" + e.getClass().getSimpleName() + "]");
    }
}
