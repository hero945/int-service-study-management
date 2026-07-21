package com.huadong.pipeline.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("hd_plt_study")
public class StudyEntity {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String studyCode;
  private String studyName;
  private String phaseStatusCode;
  private LocalDate plannedStartDate;
  private LocalDate plannedEndDate;
  private LocalDate actualStartDate;
  private LocalDate actualEndDate;
  private String studyDescription;
  private Long programId;
  private String programCodeSnapshot;
  private String programNameSnapshot;
  private String productNameSnapshot;
  private String moaSnapshot;
  private String sourceCodeSnapshot;
  private String originCodeSnapshot;
  private Long projectId;
  private String projectCodeSnapshot;
  private String projectNameSnapshot;
  private Long therapeuticAreaId;
  private String therapeuticAreaCodeSnapshot;
  private String therapeuticAreaNameSnapshot;
  private String indicationDescriptionSnapshot;
  private String sysCreateBy;
  private String sysUpdateBy;
  private LocalDateTime sysUpdateTime;
  private short sysDeleted;

  public Long getId() { return id; }
  public String getStudyCode() { return studyCode; }
  public void setStudyCode(String value) { studyCode = value; }
  public String getStudyName() { return studyName; }
  public void setStudyName(String value) { studyName = value; }
  public String getPhaseStatusCode() { return phaseStatusCode; }
  public void setPhaseStatusCode(String value) { phaseStatusCode = value; }
  public LocalDate getPlannedStartDate() { return plannedStartDate; }
  public void setPlannedStartDate(LocalDate value) { plannedStartDate = value; }
  public LocalDate getPlannedEndDate() { return plannedEndDate; }
  public void setPlannedEndDate(LocalDate value) { plannedEndDate = value; }
  public LocalDate getActualStartDate() { return actualStartDate; }
  public void setActualStartDate(LocalDate value) { actualStartDate = value; }
  public LocalDate getActualEndDate() { return actualEndDate; }
  public void setActualEndDate(LocalDate value) { actualEndDate = value; }
  public String getStudyDescription() { return studyDescription; }
  public void setStudyDescription(String value) { studyDescription = value; }
  public Long getProgramId() { return programId; }
  public void setProgramId(Long value) { programId = value; }
  public String getProgramCodeSnapshot() { return programCodeSnapshot; }
  public void setProgramCodeSnapshot(String value) { programCodeSnapshot = value; }
  public String getProgramNameSnapshot() { return programNameSnapshot; }
  public void setProgramNameSnapshot(String value) { programNameSnapshot = value; }
  public String getProductNameSnapshot() { return productNameSnapshot; }
  public void setProductNameSnapshot(String value) { productNameSnapshot = value; }
  public String getMoaSnapshot() { return moaSnapshot; }
  public void setMoaSnapshot(String value) { moaSnapshot = value; }
  public String getSourceCodeSnapshot() { return sourceCodeSnapshot; }
  public void setSourceCodeSnapshot(String value) { sourceCodeSnapshot = value; }
  public String getOriginCodeSnapshot() { return originCodeSnapshot; }
  public void setOriginCodeSnapshot(String value) { originCodeSnapshot = value; }
  public Long getProjectId() { return projectId; }
  public void setProjectId(Long value) { projectId = value; }
  public String getProjectCodeSnapshot() { return projectCodeSnapshot; }
  public void setProjectCodeSnapshot(String value) { projectCodeSnapshot = value; }
  public String getProjectNameSnapshot() { return projectNameSnapshot; }
  public void setProjectNameSnapshot(String value) { projectNameSnapshot = value; }
  public Long getTherapeuticAreaId() { return therapeuticAreaId; }
  public void setTherapeuticAreaId(Long value) { therapeuticAreaId = value; }
  public String getTherapeuticAreaCodeSnapshot() { return therapeuticAreaCodeSnapshot; }
  public void setTherapeuticAreaCodeSnapshot(String value) { therapeuticAreaCodeSnapshot = value; }
  public String getTherapeuticAreaNameSnapshot() { return therapeuticAreaNameSnapshot; }
  public void setTherapeuticAreaNameSnapshot(String value) { therapeuticAreaNameSnapshot = value; }
  public String getIndicationDescriptionSnapshot() { return indicationDescriptionSnapshot; }
  public void setIndicationDescriptionSnapshot(String value) { indicationDescriptionSnapshot = value; }
  public String getSysCreateBy() { return sysCreateBy; }
  public void setSysCreateBy(String value) { sysCreateBy = value; }
  public String getSysUpdateBy() { return sysUpdateBy; }
  public void setSysUpdateBy(String value) { sysUpdateBy = value; }
  public LocalDateTime getSysUpdateTime() { return sysUpdateTime; }
  public short getSysDeleted() { return sysDeleted; }
}
