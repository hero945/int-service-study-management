package com.huadong.pipeline.security;

import jakarta.validation.constraints.Email;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("app.bootstrap")
@Validated
public record BootstrapProperties(
    @Email String adminUsername,
    String adminPassword,
    String adminDisplayName) {
}
