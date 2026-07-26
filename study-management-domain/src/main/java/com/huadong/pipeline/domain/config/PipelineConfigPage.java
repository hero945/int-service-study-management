package com.huadong.pipeline.domain.config;

import java.util.List;

public record PipelineConfigPage(
    List<PipelineConfigRow> data, int page, int pageSize, long totalItems) {
  public PipelineConfigPage {
    data = List.copyOf(data);
  }
}
