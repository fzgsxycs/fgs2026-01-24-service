package com.example.computerassociation.exception;

/*
全局异常处理器
 */
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.example.computerassociation.common.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalException {

    private static final Logger log = LoggerFactory.getLogger(GlobalException.class);


    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e, HttpServletRequest request) {
        log.error("[{}] 系统未知异常: 请求URI: {}, 异常信息: {}",
                TraceIdUtil.getTraceId(),
                request.getRequestURI(),
                e.getMessage(),
                e);

        return Result.error(500, "服务器内部错误，请稍后重试");
    }
}
