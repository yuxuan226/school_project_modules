package com.example.controller;

import cn.dev33.satoken.util.SaResult;
import com.example.entity.User;
import com.example.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import validation.Valida;

import java.util.Map;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/h1")
    public String h1() {
        return "h1";
    }

    /**
     * 根据id查询用户
     */
    @ApiOperation(value = "根据id查询用户")
    @GetMapping("{id}")
    public SaResult queryById(@ApiParam("用户ID") @PathVariable Long id) {
        return SaResult.data(userService.getById(id));
    }

    /**
     * 保存用户（注册）
     */
    @ApiOperation(value = "保存用户")
    @PostMapping("save")
    public SaResult save(@Validated(Valida.Create.class) @RequestBody User user) {
        return userService.saveUser(user);
    }

    /**
     * 修改用户
     * 文档要求使用 @PostMapping + @RequestBody
     */
    @ApiOperation(value = "修改用户")
    @PostMapping("update")
    public SaResult update(@Validated(Valida.Update.class) @RequestBody User user) {
        return userService.updateUser(user);
    }

    /**
     * 删除用户
     * 文档要求使用 @GetMapping + @RequestParam
     */
    @ApiOperation(value = "删除用户")
    @GetMapping("delete")
    public SaResult delete(@ApiParam("用户ID") @RequestParam Long id) {
        return userService.deleteUser(id);
    }

    /**
     * 分页条件查询
     */
    @ApiOperation(value = "分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", defaultValue = "1", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "size", value = "每页大小", defaultValue = "10", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "name", value = "用户名", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "string", paramType = "query")
    })
    @GetMapping("page")
    public SaResult selectPage(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer size,
                               @RequestParam Map<String, Object> params) {
        return userService.selectPage(page, size, params);
    }

    /**
     * 初始化密码（重置为 123456 加密后）
     */
    @ApiOperation(value = "初始化密码")
    @GetMapping("initPwd")
    public SaResult initPassword(@ApiParam("用户ID") @RequestParam Long id) {
        return userService.initPassword(id);
    }
}
