package com.example.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.example.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "登录/退出")
@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 登录：传手机号 + 密码
     */
    @ApiOperation(value = "登录")
    @PostMapping("in")
    public SaResult login(@RequestParam String phone, @RequestParam String password) {
        return userService.login(phone, password);
    }

    /**
     * 退出登录
     */
    @ApiOperation(value = "退出登录")
    @PostMapping("out")
    public SaResult logout() {
        StpUtil.logout();
        return SaResult.ok("退出成功");
    }

    /**
     * 测试是否登录
     */
    @ApiOperation(value = "检查登录状态")
    @GetMapping("check")
    public SaResult check() {
        return SaResult.data(StpUtil.isLogin());
    }
}
