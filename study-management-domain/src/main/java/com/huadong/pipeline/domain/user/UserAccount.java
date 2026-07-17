package com.huadong.pipeline.domain.user;

public record UserAccount(
    long id,
    String username,
    String passwordHash,
    String displayName,
    String role,
    boolean enabled) {
}
