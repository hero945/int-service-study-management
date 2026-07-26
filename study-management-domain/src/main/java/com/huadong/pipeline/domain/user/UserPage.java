package com.huadong.pipeline.domain.user;

import java.util.List;

public record UserPage(List<UserAccount> data, int page, int pageSize, long totalItems) {
  public UserPage {
    data = List.copyOf(data);
  }
}
