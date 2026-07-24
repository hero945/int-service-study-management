package com.huadong.pipeline.repository.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("hd_plt_system_setting")
@Getter
@Setter
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
}
