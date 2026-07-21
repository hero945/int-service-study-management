package com.huadong.pipeline.repository.mapper;

public class RolePermissionCodeRow {
  private Long roleId;
  private String permissionCode;

  public Long getRoleId() {
    return roleId;
  }

  public void setRoleId(Long roleId) {
    this.roleId = roleId;
  }

  public String getPermissionCode() {
    return permissionCode;
  }

  public void setPermissionCode(String permissionCode) {
    this.permissionCode = permissionCode;
  }
}
