package com.huadong.pipeline.repository.mapper;

import java.time.LocalDateTime;

public class RoleSummaryRow {
  private Long id;
  private String roleName;
  private String roleDescription;
  private String dataScopeMode;
  private String statusCode;
  private Integer isSystemRole;
  private Long assignedUserCount;
  private LocalDateTime sysUpdateTime;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getRoleName() { return roleName; }
  public void setRoleName(String roleName) { this.roleName = roleName; }
  public String getRoleDescription() { return roleDescription; }
  public void setRoleDescription(String roleDescription) { this.roleDescription = roleDescription; }
  public String getDataScopeMode() { return dataScopeMode; }
  public void setDataScopeMode(String dataScopeMode) { this.dataScopeMode = dataScopeMode; }
  public String getStatusCode() { return statusCode; }
  public void setStatusCode(String statusCode) { this.statusCode = statusCode; }
  public Integer getIsSystemRole() { return isSystemRole; }
  public void setIsSystemRole(Integer isSystemRole) { this.isSystemRole = isSystemRole; }
  public Long getAssignedUserCount() { return assignedUserCount; }
  public void setAssignedUserCount(Long assignedUserCount) { this.assignedUserCount = assignedUserCount; }
  public LocalDateTime getSysUpdateTime() { return sysUpdateTime; }
  public void setSysUpdateTime(LocalDateTime sysUpdateTime) { this.sysUpdateTime = sysUpdateTime; }
}
