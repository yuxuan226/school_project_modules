package com.example.service;

import com.example.utils.EmailCodeRedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class EmailAsyncService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private EmailCodeRedisUtil emailCodeRedisUtil;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("taskExecutor")
    public CompletableFuture<Boolean> sendVerificationCodeAsync(String email, String code) {
        log.info("异步邮件发送开始，线程：{}，邮箱：{}", Thread.currentThread().getName(), email);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(fromEmail, "数字图书管理系统", "UTF-8"));
            helper.setTo(email);
            helper.setSubject("邮箱验证码");
            helper.setText("您的验证码是：" + code + "，有效期为5分钟。如非本人操作，请忽略。");

            mailSender.send(message);

            emailCodeRedisUtil.updatePlaceholderToCode(email, code);

            log.info("异步邮件发送成功，线程：{}", Thread.currentThread().getName());
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("异步邮件发送失败", e);
            emailCodeRedisUtil.deleteEmailCode(email);
            return CompletableFuture.completedFuture(false);
        }
    }
}
