package com.huadong.pipeline.domain.user;

import java.util.List;

public record UserAccount(
    long id,
    String username,
    String passwordHash,
    String displayName,
    List<String> roles,
    List<String> roleDescriptions,
    List<String> permissions,
    DataScope dataScope,
    boolean enabled) {

  public UserAccount {
    roles = List.copyOf(roles);
    roleDescriptions = List.copyOf(roleDescriptions);
    permissions = List.copyOf(permissions);
  }
}
