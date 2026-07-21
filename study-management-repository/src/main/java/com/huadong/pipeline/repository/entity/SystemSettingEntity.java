package com.huadong.pipeline.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("hd_plt_system_setting")
public class SystemSettingEntity {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String configKey;
  private String configValue;
  private String configDescription;
  private boolean publicVisible;
  private String sysUpdateBy;
  private LocalDateTime sysUpdateTime;
  private short sysDeleted;

  public Long getId() {
    return id;
  }

  public String getConfigKey() {
    return configKey;
  }

  public void setConfigKey(String configKey) {
    this.configKey = configKey;
  }

  public String getConfigValue() {
    return configValue;
  }

  public void setConfigValue(String configValue) {
    this.configValue = configValue;
  }

  public String getConfigDescription() {
    return configDescription;
  }

  public void setConfigDescription(String configDescription) {
    this.configDescription = configDescription;
  }

  public boolean isPublicVisible() {
    return publicVisible;
  }

  public void setPublicVisible(boolean publicVisible) {
    this.publicVisible = publicVisible;
  }

  public String getSysUpdateBy() {
    return sysUpdateBy;
  }

  public void setSysUpdateBy(String sysUpdateBy) {
    this.sysUpdateBy = sysUpdateBy;
  }

  public LocalDateTime getSysUpdateTime() {
    return sysUpdateTime;
  }

  public void setSysUpdateTime(LocalDateTime sysUpdateTime) {
    this.sysUpdateTime = sysUpdateTime;
  }

  public short getSysDeleted() {
    return sysDeleted;
  }

  public void setSysDeleted(short sysDeleted) {
    this.sysDeleted = sysDeleted;
  }
}
