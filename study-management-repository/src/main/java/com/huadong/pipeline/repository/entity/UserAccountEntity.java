package com.huadong.pipeline.repository.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("hd_plt_user")
@Getter
@Setter
public class UserAccountEntity {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String email;
  private String passwordHash;
  private String displayName;
  private String statusCode;
  private String securityStamp;
  private String sysCreateBy;
  private String sysUpdateBy;
  private LocalDateTime sysCreateTime;
  private LocalDateTime sysUpdateTime;
  private short sysDeleted;
}
