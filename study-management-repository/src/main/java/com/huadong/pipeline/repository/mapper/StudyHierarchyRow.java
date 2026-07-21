package com.huadong.pipeline.repository.mapper;

public class StudyHierarchyRow {
  private Long programId;
  private String programCode;
  private String programName;
  private String productName;
  private String moa;
  private String sourceCode;
  private String originCode;
  private Long projectId;
  private String projectCode;
  private String projectName;
  private Long therapeuticAreaId;
  private String therapeuticAreaCode;
  private String therapeuticAreaName;
  private String indicationDescription;

  public Long getProgramId() { return programId; }
  public String getProgramCode() { return programCode; }
  public String getProgramName() { return programName; }
  public String getProductName() { return productName; }
  public String getMoa() { return moa; }
  public String getSourceCode() { return sourceCode; }
  public String getOriginCode() { return originCode; }
  public Long getProjectId() { return projectId; }
  public String getProjectCode() { return projectCode; }
  public String getProjectName() { return projectName; }
  public Long getTherapeuticAreaId() { return therapeuticAreaId; }
  public String getTherapeuticAreaCode() { return therapeuticAreaCode; }
  public String getTherapeuticAreaName() { return therapeuticAreaName; }
  public String getIndicationDescription() { return indicationDescription; }
}
