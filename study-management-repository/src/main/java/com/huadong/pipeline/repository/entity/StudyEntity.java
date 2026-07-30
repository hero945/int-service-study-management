package com.huadong.pipeline.repository.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("hd_plt_study")
@Getter
@Setter
public class StudyEntity {
  @TableId(type = IdType.AUTO)
  private Long id;
  private Integer version;
  private String studyCode;
  private String phaseStatusCode;
  private LocalDate plannedStartDate;
  private LocalDate plannedEndDate;
  private LocalDate actualStartDate;
  private LocalDate actualEndDate;
  private String studyDescription;
  private Long programId;
  private String programCodeSnapshot;
  private String productNameSnapshot;
  private String moaSnapshot;
  private String sourceCodeSnapshot;
  private String originCodeSnapshot;
  private Long projectId;
  private String projectCodeSnapshot;
  private Long therapeuticAreaId;
  private String therapeuticAreaCodeSnapshot;
  private String therapeuticAreaNameSnapshot;
  private String indicationDescriptionSnapshot;
  private String sysCreateBy;
  private String sysUpdateBy;
  private LocalDateTime sysUpdateTime;
  private short sysDeleted;
}
