package com.zentea.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "zentea.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpiration = 900000;
    private long refreshTokenExpiration = 604800000;
}
