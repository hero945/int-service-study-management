package com.huadong.pipeline.repository.entity;

import lombok.Getter;
import lombok.Setter;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;

@TableName("hd_plt_program")
@Getter
@Setter
public class ProgramEntity {
  @TableId(type = IdType.AUTO)
  private Long id;
  private String programCode;
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
}
