package com.example.app_be.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.vnpay")
@Getter
@Setter
public class VNPayConfig {
    private String tmnCode;
    private String hashSecret;
    private String url;
    private String returnUrl;
    private String ipnUrl;
}
