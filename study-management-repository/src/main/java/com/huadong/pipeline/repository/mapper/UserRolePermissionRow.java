package com.huadong.pipeline.repository.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRolePermissionRow {
  private Long userId;
  private Long roleId;
  private String permissionCode;
}
