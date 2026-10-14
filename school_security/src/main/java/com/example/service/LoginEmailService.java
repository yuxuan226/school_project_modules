package com.example.service;

import cn.dev33.satoken.util.SaResult;
import com.example.entity.LoginEmail;

public interface LoginEmailService {

    /** 根据手机号发送邮件验证码 */
    SaResult sendEmail(String phone);

    /** 邮箱验证码登录 */
    SaResult loginEmail(LoginEmail loginEmail);
}
