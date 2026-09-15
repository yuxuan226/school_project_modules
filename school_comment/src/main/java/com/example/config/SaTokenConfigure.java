package com.example.config;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.serializer.impl.SaSerializerTemplateForJdkUseBase64;
import cn.dev33.satoken.stp.StpUtil;
import com.example.entity.WhiteListConfig;
import com.example.entity.BlackListConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * Sa-Token 拦截器配置
 * 白名单/黑名单通过 @ConfigurationProperties 从 yml 配置文件加载
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfigure implements WebMvcConfigurer {

    private final WhiteListConfig whiteListConfig;
    private final BlackListConfig blackListConfig;

    /** knife4j 是否启用，开发环境 true / 生产环境 false */
    @Value("${knife4j.enable:true}")
    private boolean knife4jEnable;

    @PostConstruct
    public void rewriteComponent() {
        SaManager.setSaSerializerTemplate(new SaSerializerTemplateForJdkUseBase64());
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> excludePaths = new ArrayList<>();

        // 通用白名单 + 登录白名单（始终加入）
        excludePaths.addAll(whiteListConfig.getCommon());
        excludePaths.addAll(whiteListConfig.getLogin());

        // knife4j 开启时才放行 swagger UI 路径
        if (knife4jEnable) {
            excludePaths.addAll(whiteListConfig.getKnife4j());
        }

        registry.addInterceptor(new SaInterceptor(handler -> {
                    SaRouter.match("/**")
                            .notMatch(excludePaths)
                            .check(r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
