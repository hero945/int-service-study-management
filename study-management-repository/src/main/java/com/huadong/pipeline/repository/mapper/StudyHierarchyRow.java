package com.huadong.pipeline.repository.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StudyHierarchyRow {
  private Long programId;
  private String programCode;
  private String productName;
  private String moa;
  private String sourceCode;
  private String originCode;
  private Long projectId;
  private String projectCode;
  private Long therapeuticAreaId;
  private String therapeuticAreaCode;
  private String therapeuticAreaName;
  private String indicationDescription;
}
