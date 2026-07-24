package com.huadong.pipeline.repository.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("hd_plt_role")
@Getter
@Setter
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
}
