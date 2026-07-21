package com.huadong.pipeline.repository.mapper;

public class UserRolePermissionRow {
  private Long userId;
  private Long roleId;
  private String permissionCode;

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public Long getRoleId() { return roleId; }
  public void setRoleId(Long roleId) { this.roleId = roleId; }
  public String getPermissionCode() { return permissionCode; }
  public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
}
