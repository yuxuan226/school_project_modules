package com.example.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 黑名单配置（需要登录且有特殊权限）
 */
@Data
@Component
@ConfigurationProperties(prefix = "blacklist")
public class BlackListConfig {

    /** 禁止直接访问的路径（需要特殊处理） */
    private List<String> paths = new ArrayList<>();
}
