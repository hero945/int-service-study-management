package com.huadong.pipeline.repository.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("hd_plt_project")
@Getter
@Setter
public class ProjectEntity {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String projectCode;
  private Long programId;
  private String indicationDescription;
  private Long therapeuticAreaId;
  private Integer sortOrder;
  private String projectDescription;
  private String sysCreateBy;
  private String sysUpdateBy;
  private LocalDateTime sysUpdateTime;
  private short sysDeleted;
}
