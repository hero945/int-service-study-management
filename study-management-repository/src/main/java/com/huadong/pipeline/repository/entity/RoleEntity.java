package com.huadong.pipeline.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("hd_plt_role")
public class RoleEntity {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String roleName;
  private String roleDescription;
  private String dataScopeMode;
  private String statusCode;
  private Integer isSystemRole;
  private String sysCreateBy;
  private String sysUpdateBy;
  private LocalDateTime sysCreateTime;
  private LocalDateTime sysUpdateTime;
  private Integer sysDeleted;

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
  public String getSysCreateBy() { return sysCreateBy; }
  public void setSysCreateBy(String sysCreateBy) { this.sysCreateBy = sysCreateBy; }
  public String getSysUpdateBy() { return sysUpdateBy; }
  public void setSysUpdateBy(String sysUpdateBy) { this.sysUpdateBy = sysUpdateBy; }
  public LocalDateTime getSysCreateTime() { return sysCreateTime; }
  public void setSysCreateTime(LocalDateTime sysCreateTime) { this.sysCreateTime = sysCreateTime; }
  public LocalDateTime getSysUpdateTime() { return sysUpdateTime; }
  public void setSysUpdateTime(LocalDateTime sysUpdateTime) { this.sysUpdateTime = sysUpdateTime; }
  public Integer getSysDeleted() { return sysDeleted; }
  public void setSysDeleted(Integer sysDeleted) { this.sysDeleted = sysDeleted; }
}
