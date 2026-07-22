package com.huadong.pipeline.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("hd_plt_project")
public class ProjectEntity {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String projectCode;
  private String projectName;
  private Long programId;
  private String indicationDescription;
  private Long therapeuticAreaId;
  private Integer sortOrder;
  private String projectDescription;
  private String sysCreateBy;
  private String sysUpdateBy;
  private LocalDateTime sysUpdateTime;
  private short sysDeleted;

  public Long getId() { return id; }
  public String getProjectCode() { return projectCode; }
  public void setProjectCode(String value) { projectCode = value; }
  public String getProjectName() { return projectName; }
  public void setProjectName(String value) { projectName = value; }
  public Long getProgramId() { return programId; }
  public void setProgramId(Long value) { programId = value; }
  public String getIndicationDescription() { return indicationDescription; }
  public void setIndicationDescription(String value) { indicationDescription = value; }
  public Long getTherapeuticAreaId() { return therapeuticAreaId; }
  public void setTherapeuticAreaId(Long value) { therapeuticAreaId = value; }
  public Integer getSortOrder() { return sortOrder; }
  public void setSortOrder(Integer value) { sortOrder = value; }
  public String getProjectDescription() { return projectDescription; }
  public void setProjectDescription(String value) { projectDescription = value; }
  public String getSysCreateBy() { return sysCreateBy; }
  public void setSysCreateBy(String value) { sysCreateBy = value; }
  public String getSysUpdateBy() { return sysUpdateBy; }
  public void setSysUpdateBy(String value) { sysUpdateBy = value; }
  public LocalDateTime getSysUpdateTime() { return sysUpdateTime; }
  public void setSysUpdateTime(LocalDateTime value) { sysUpdateTime = value; }
  public short getSysDeleted() { return sysDeleted; }
  public void setSysDeleted(short value) { sysDeleted = value; }
}
