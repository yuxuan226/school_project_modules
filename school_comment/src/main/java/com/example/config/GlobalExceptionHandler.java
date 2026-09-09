//package com.example.config;
//
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//@ControllerAdvice
//@ResponseBody
//public class GlobalExceptionHandler {
//
//    // Sa-Token 异常处理
//    @ExceptionHandler(NotLoginException.class)
//    public SaResult handlerNotLoginException(NotLoginException e) {
//        // 判断场景值，定制化提示
//        String message;
//        switch (e.getType()) {
//            case NotLoginException.NOT_TOKEN: message = "未提供Token"; break;
//            case NotLoginException.INVALID_TOKEN: message = "Token无效"; break;
//            case NotLoginException.TOKEN_TIMEOUT: message = "Token已过期"; break;
//            case NotLoginException.BE_REPLACED: message = "账号已被顶下线"; break;
//            case NotLoginException.KICK_OUT: message = "账号已被踢下线"; break;
//            default: message = "当前会话未登录"; break;
//        }
//        return SaResult.error(message).setCode(401);
//    }
//    // 处理其他异常（可选）
//    @ExceptionHandler(value = Exception.class)
//    public SaResult handleException(Exception e) {
//        // 处理其他未捕获的异常
//        return SaResult.error("服务器内部错误"+e.getMessage()).setCode(500);
//    }
//}
