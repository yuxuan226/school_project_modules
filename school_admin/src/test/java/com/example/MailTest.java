package com.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

@SpringBootTest
@Slf4j
public class MailTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    public void sendMailTwe() {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress("2379186530@qq.com", "数字图书管理系统", "UTF-8"));
            helper.setTo("2379186530@qq.com");
            helper.setSubject("关于您的问题反馈");
            helper.setText("您好，感谢您的反馈，我们会尽快处理。", false);

            mailSender.send(message);

            System.out.println("邮件发送成功，发件人显示为：客服小助手");

        } catch (Exception e) {
            System.err.println("邮件发送失败：" + e.getMessage());
        }
    }
}
