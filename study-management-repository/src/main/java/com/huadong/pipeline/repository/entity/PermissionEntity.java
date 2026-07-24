package com.huadong.pipeline.repository.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("hd_plt_permission")
@Getter
@Setter
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
}
