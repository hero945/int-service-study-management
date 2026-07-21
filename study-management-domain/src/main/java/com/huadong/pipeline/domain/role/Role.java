package com.huadong.pipeline.domain.role;

import com.huadong.pipeline.domain.user.DataScope;
import java.time.LocalDateTime;
import java.util.List;

public record Role(
    long id,
    String code,
    String description,
    DataScope dataScope,
    RoleStatus status,
    boolean systemRole,
    long assignedUserCount,
    List<String> permissionCodes,
    LocalDateTime updatedAt) {
  public Role {
    permissionCodes = List.copyOf(permissionCodes);
  }
}
