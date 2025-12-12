package com.iven.memo.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 允许不带认证请求的配置类
 */
@Component
@ConfigurationProperties(prefix = "security.allow-request-without-auth")
@Data
public class AllowRequestWithoutAuthProperties {
    private List<String> post = new ArrayList<>();
    private List<String> get = new ArrayList<>();
}
