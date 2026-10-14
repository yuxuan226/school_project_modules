package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserEntityControllerParent 接口实现
 * 位于 school_user 模块，通过 UserMapper 查询手机号对应的用户ID
 * 不提供 HTTP 端点（无 @RequestMapping），仅作为 Bean 被注入到 BookServiceImpl 中
 */
@RestController
public class UserEntityController implements UserEntityControllerParent {

    @Autowired
    private UserMapper userMapper;

    @Override
    public Long selectByPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return null;
        }
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone).select(User::getId);
        User user = userMapper.selectOne(wrapper);
        return user == null ? null : user.getId();
    }
}
