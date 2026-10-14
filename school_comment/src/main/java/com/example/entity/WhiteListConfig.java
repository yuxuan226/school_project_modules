package com.example.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 白名单配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "whitelist")
public class WhiteListConfig {

    /** knife4j/swagger 相关路径 */
    private List<String> knife4j = new ArrayList<>();

    /** 登录相关 */
    private List<String> login = new ArrayList<>();

    /** 其他通用白名单 */
    private List<String> common = new ArrayList<>();

    /** 合并所有白名单 */
    public List<String> all() {
        List<String> list = new ArrayList<>();
        list.addAll(knife4j);
        list.addAll(login);
        list.addAll(common);
        return list;
    }
}
