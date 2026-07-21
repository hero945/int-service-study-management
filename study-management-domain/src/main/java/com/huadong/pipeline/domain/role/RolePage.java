package com.huadong.pipeline.domain.role;

import java.util.List;

public record RolePage(List<Role> data, int page, int pageSize, long totalItems) {
  public RolePage {
    data = List.copyOf(data);
  }
}
