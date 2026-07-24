package com.huadong.pipeline.repository.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAuthorizationRow {
  private long userId;
  private String roleCode;
  private String roleDescription;
  private String permissionCode;
  private String dataScope;
}
