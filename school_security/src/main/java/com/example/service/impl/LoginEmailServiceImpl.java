package com.example.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.entity.LoginEmail;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.EmailAsyncService;
import com.example.service.LoginEmailService;
import com.example.utils.EmailCodeRedisUtil;
import lombok.extern.slf4j.Slf4j;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class LoginEmailServiceImpl implements LoginEmailService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailAsyncService emailAsyncService;

    @Autowired
    private EmailCodeRedisUtil emailCodeRedisUtil;

    @Override
    public SaResult sendEmail(String phone) {
        try {
            // 1. 查询用户
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, phone);
            User user = userMapper.selectOne(wrapper);
            if (user == null) {
                return SaResult.error("没有该用户");
            }
            if (user.getEmail() == null || "".equals(user.getEmail())) {
                return SaResult.error("该用户未绑定邮箱");
            }
            String email = user.getEmail();

            // 2. 检查Redis中是否已有有效验证码
            if (emailCodeRedisUtil.hasValidCode(email)) {
                Long expireSeconds = emailCodeRedisUtil.getExpireTime(email);
                if (expireSeconds > 0) {
                    return SaResult.ok("验证码已发送，请查收邮件。剩余有效期：" + (expireSeconds / 60) + "分钟");
                }
            }

            // 3. 检查是否正在发送中（占位符状态）
            if (emailCodeRedisUtil.isSendingPlaceholder(email)) {
                return SaResult.ok("邮件正在发送中，请稍后查看邮箱。若长时间未收到，请2分钟后重试");
            }

            // 4. 生成验证码
            String code = generateCode();

            // 5. 先创建占位符（防止重复提交）
            emailCodeRedisUtil.createSendingPlaceholder(email);

            // 6. 异步发送邮件
            CompletableFuture<Boolean> future = emailAsyncService.sendVerificationCodeAsync(email, code);
            future.whenComplete((result, throwable) -> {
                if (throwable != null || !result) {
                    log.error("邮件发送失败，删除占位符：{}", email);
                    emailCodeRedisUtil.deleteEmailCode(email);
                }
            });

            return SaResult.ok("验证码发送请求已接收，请30秒后查收邮件");
        } catch (Exception e) {
            e.printStackTrace();
            return SaResult.error("发送失败，请稍后重试");
        }
    }

    @Override
    public SaResult loginEmail(LoginEmail loginEmail) {
        // 1. 判断是否有该用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, loginEmail.getPhone());
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            return SaResult.error("没有该账号");
        }

        // 2. 获取redis中的验证码
        String emailCode = emailCodeRedisUtil.getEmailCode(user.getEmail());
        if (StrUtil.isBlank(emailCode)) {
            return SaResult.error("该用户未发送验证码");
        }

        // 3. 对比验证码
        if (loginEmail.getCode().equals(emailCode)) {
            // 4. 正确则删除redis中的验证码
            emailCodeRedisUtil.deleteEmailCode(user.getEmail());
            // 5. 登录（存储 phone，便于业务层通过 phone 反查 user id）
            StpUtil.login(user.getPhone());
            return SaResult.data(StpUtil.getTokenInfo());
        } else {
            return SaResult.error("验证码不正确");
        }
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
