package com.huadong.pipeline.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.bootstrap")
public record BootstrapProperties(
    String adminUsername,
    String adminPassword,
    String adminDisplayName) {
}
