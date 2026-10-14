package com.example.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import cn.dev33.satoken.secure.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public SaResult selectPage(Integer page, Integer size, Map<String, Object> params) {
        Page<User> page1 = new Page<>(page, size);
        String name = params.get("name") != null ? params.get("name").toString() : null;
        String startTime = params.get("startTime") != null ? params.get("startTime").toString() : null;
        String endTime = params.get("endTime") != null ? params.get("endTime").toString() : null;

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null && !name.isEmpty(), User::getName, name)
                .ge(startTime != null && !startTime.isEmpty(), User::getSubmitTime, parseTime(startTime))
                .le(endTime != null && !endTime.isEmpty(), User::getSubmitTime, parseTime(endTime));

        Page<User> userPage = baseMapper.selectPage(page1, queryWrapper);
        return SaResult.data(userPage);
    }

    @Override
    public SaResult saveUser(User user) {
        // 1. 电话号码是否传递（validation 已处理）
        // 2. 校验电话号码是否正常（validation 正则已处理）
        // 3. 判断数据库中电话号码是否存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, user.getPhone());
        if (count(wrapper) > 0) {
            return SaResult.error("该手机号已存在，无法注册");
        }
        // 4. 密码加密（Sa-Token BCrypt）
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setSubmitTime(LocalDateTime.now());
        // 5. 注册
        boolean ok = this.save(user);
        return ok ? SaResult.ok("注册成功") : SaResult.error("注册失败");
    }

    @Override
    public SaResult updateUser(User user) {
        // 1. 校验是否传递用户id
        if (user.getId() == null) {
            return SaResult.error("用户ID不能为空");
        }
        // 2. 判断数据库中是否有该用户
        User dbUser = this.getById(user.getId());
        if (dbUser == null) {
            return SaResult.error("该用户不存在");
        }
        // 3. 判断是否修改了密码
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // 4. 修改了密码 → 加密
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        } else {
            // 没改密码 → 用原来的
            user.setPassword(dbUser.getPassword());
        }
        boolean ok = this.updateById(user);
        return ok ? SaResult.ok("修改成功") : SaResult.error("修改失败");
    }

    @Override
    public SaResult deleteUser(Long id) {
        // 1. 校验id
        if (id == null) {
            return SaResult.error("用户ID不能为空");
        }
        // 2. 判断用户是否存在
        User dbUser = this.getById(id);
        if (dbUser == null) {
            return SaResult.error("该用户不存在");
        }
        // 3. 删除
        boolean ok = this.removeById(id);
        return ok ? SaResult.ok("删除成功") : SaResult.error("删除失败");
    }

    @Override
    public SaResult initPassword(Long id) {
        if (id == null) {
            return SaResult.error("用户ID不能为空");
        }
        User dbUser = this.getById(id);
        if (dbUser == null) {
            return SaResult.error("该用户不存在");
        }
        dbUser.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt()));
        boolean ok = this.updateById(dbUser);
        return ok ? SaResult.ok("密码初始化成功") : SaResult.error("密码初始化失败");
    }

    @Override
    public SaResult login(String phone, String password) {
        if (phone == null || phone.isEmpty()) {
            return SaResult.error("手机号不能为空");
        }
        if (password == null || password.isEmpty()) {
            return SaResult.error("密码不能为空");
        }
        User user = getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (user == null) {
            return SaResult.error("用户不存在");
        }
        if (!BCrypt.checkpw(password, user.getPassword())) {
            return SaResult.error("密码错误");
        }
        // 登录（存储 phone，便于业务层通过 phone 反查 user id）
        StpUtil.login(user.getPhone());
        return SaResult.data(StpUtil.getTokenValue()).setMsg("登录成功");
    }

    private LocalDateTime parseTime(String time) {
        try {
            return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
