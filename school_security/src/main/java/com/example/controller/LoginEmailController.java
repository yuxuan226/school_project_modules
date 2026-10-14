package com.example.controller;

import cn.dev33.satoken.util.SaResult;
import com.example.entity.LoginEmail;
import com.example.service.LoginEmailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import validation.Valida;

@Api(tags = "邮箱验证码登录")
@RestController
@RequestMapping("/login/email")
public class LoginEmailController {

    @Autowired
    private LoginEmailService loginEmailService;

    /**
     * 发送邮件验证码
     */
    @ApiOperation(value = "发送邮件验证码")
    @GetMapping("sendCode")
    public SaResult sendCode(@RequestParam String phone) {
        return loginEmailService.sendEmail(phone);
    }

    /**
     * 邮箱验证码登录
     */
    @ApiOperation(value = "邮箱验证码登录")
    @PostMapping("login")
    public SaResult login(@Validated(Valida.Create.class) @RequestBody LoginEmail loginEmail) {
        return loginEmailService.loginEmail(loginEmail);
    }
}
