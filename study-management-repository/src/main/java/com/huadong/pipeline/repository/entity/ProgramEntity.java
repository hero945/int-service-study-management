package com.huadong.pipeline.repository.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("hd_plt_program")
public class ProgramEntity {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String programCode;
  private String programName;
  private String productName;
  private String moa;
  private String sourceCode;
  private String originCode;
  private String statusCode;
  private Integer sortOrder;
  private String sysCreateBy;
  private String sysUpdateBy;
  private LocalDateTime sysUpdateTime;
  private short sysDeleted;

  public Long getId() { return id; }
  public String getProgramCode() { return programCode; }
  public void setProgramCode(String value) { programCode = value; }
  public String getProgramName() { return programName; }
  public void setProgramName(String value) { programName = value; }
  public String getProductName() { return productName; }
  public void setProductName(String value) { productName = value; }
  public String getMoa() { return moa; }
  public void setMoa(String value) { moa = value; }
  public String getSourceCode() { return sourceCode; }
  public void setSourceCode(String value) { sourceCode = value; }
  public String getOriginCode() { return originCode; }
  public void setOriginCode(String value) { originCode = value; }
  public String getStatusCode() { return statusCode; }
  public void setStatusCode(String value) { statusCode = value; }
  public Integer getSortOrder() { return sortOrder; }
  public void setSortOrder(Integer value) { sortOrder = value; }
  public String getSysCreateBy() { return sysCreateBy; }
  public void setSysCreateBy(String value) { sysCreateBy = value; }
  public String getSysUpdateBy() { return sysUpdateBy; }
  public void setSysUpdateBy(String value) { sysUpdateBy = value; }
  public LocalDateTime getSysUpdateTime() { return sysUpdateTime; }
  public void setSysUpdateTime(LocalDateTime value) { sysUpdateTime = value; }
  public short getSysDeleted() { return sysDeleted; }
  public void setSysDeleted(short value) { sysDeleted = value; }
}
