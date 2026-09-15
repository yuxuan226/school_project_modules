package com.example.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.util.SaResult;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

    // Sa-Token 未登录异常
    @ExceptionHandler(NotLoginException.class)
    public SaResult handlerNotLoginException(NotLoginException e) {
        String message;
        switch (e.getType()) {
            case NotLoginException.NOT_TOKEN:      message = "未提供Token"; break;
            case NotLoginException.INVALID_TOKEN:   message = "Token无效"; break;
            case NotLoginException.TOKEN_TIMEOUT:   message = "Token已过期"; break;
            case NotLoginException.BE_REPLACED:     message = "账号已被顶下线"; break;
            case NotLoginException.KICK_OUT:        message = "账号已被踢下线"; break;
            default:                                message = "当前会话未登录"; break;
        }
        return SaResult.error(message).setCode(401);
    }

    // 参数校验异常（@Validated @RequestBody）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public SaResult handleValidException(MethodArgumentNotValidException e) {
        String errorMsg = e.getBindingResult().getFieldError().getDefaultMessage();
        return SaResult.error(errorMsg);
    }

    // 参数校验异常（@Validated 表单）
    @ExceptionHandler(BindException.class)
    public SaResult handleBindException(BindException e) {
        String errorMsg = e.getBindingResult().getFieldError().getDefaultMessage();
        return SaResult.error(errorMsg);
    }

    // 其他异常
    @ExceptionHandler(value = Exception.class)
    public SaResult handleException(Exception e) {
        e.printStackTrace();
        return SaResult.error("服务器内部错误: " + e.getMessage()).setCode(500);
    }
}
