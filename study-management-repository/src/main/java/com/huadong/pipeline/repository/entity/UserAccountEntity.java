package com.huadong.pipeline.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("hd_plt_user")
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
  private short sysDeleted;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(String statusCode) {
    this.statusCode = statusCode;
  }

  public String getSecurityStamp() {
    return securityStamp;
  }

  public void setSecurityStamp(String securityStamp) {
    this.securityStamp = securityStamp;
  }

  public String getSysCreateBy() {
    return sysCreateBy;
  }

  public void setSysCreateBy(String sysCreateBy) {
    this.sysCreateBy = sysCreateBy;
  }

  public String getSysUpdateBy() {
    return sysUpdateBy;
  }

  public void setSysUpdateBy(String sysUpdateBy) {
    this.sysUpdateBy = sysUpdateBy;
  }

  public short getSysDeleted() {
    return sysDeleted;
  }

  public void setSysDeleted(short sysDeleted) {
    this.sysDeleted = sysDeleted;
  }
}
