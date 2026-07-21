package com.huadong.pipeline.domain.user;

import java.util.List;

public record UserAccount(
    long id,
    String username,
    String passwordHash,
    String displayName,
    List<String> roles,
    List<String> permissions,
    DataScope dataScope,
    boolean enabled) {

  public UserAccount {
    roles = List.copyOf(roles);
    permissions = List.copyOf(permissions);
  }
}
