package com.huadong.pipeline.repository.mapper;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RoleSummaryRow {
  private Long id;
  private String roleName;
  private String roleDescription;
  private String dataScopeMode;
  private String statusCode;
  private Integer isSystemRole;
  private Long assignedUserCount;
  private LocalDateTime sysUpdateTime;
}
