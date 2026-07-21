package com.huadong.pipeline.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("hd_plt_permission")
public class PermissionEntity {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String moduleCode;
  private String permissionCode;
  private String permissionName;
  private String permissionType;
  private String actionCode;
  private String permissionDescription;
  private String statusCode;
  private Integer sortOrder;
  private Integer sysDeleted;

  public Long getId() { return id; }
  public String getModuleCode() { return moduleCode; }
  public String getPermissionCode() { return permissionCode; }
  public String getPermissionName() { return permissionName; }
  public String getPermissionType() { return permissionType; }
  public String getActionCode() { return actionCode; }
  public String getPermissionDescription() { return permissionDescription; }
  public String getStatusCode() { return statusCode; }
  public Integer getSortOrder() { return sortOrder; }
  public Integer getSysDeleted() { return sysDeleted; }
}
