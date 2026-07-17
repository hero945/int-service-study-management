package com.huadong.pipeline.service;

import com.huadong.pipeline.api.StudyApi;
import com.huadong.pipeline.manager.StudyManager;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StudyApiService implements StudyApi {
  private final StudyManager manager;

  public StudyApiService(StudyManager manager) {
    this.manager = manager;
  }

  @Override
  public PipelineOverviewResponse overview() {
    var overview = manager.overview();
    var metrics = overview.statuses().stream()
        .map(metric -> new StatusMetricResponse(
            metric.status().name(),
            metric.status().label(),
            metric.status().tone(),
            metric.count()))
        .toList();
    return new PipelineOverviewResponse(overview.title(), overview.total(), metrics);
  }

  @Override
  public List<StudyResponse> list() {
    return manager.list().stream()
        .map(study -> new StudyResponse(
            study.id(),
            study.code(),
            study.name(),
            study.indication(),
            study.phase(),
            study.status().name(),
            study.status().label(),
            study.status().tone(),
            study.ownerName(),
            study.startDate(),
            study.updatedAt()))
        .toList();
  }

  @Override
  public void create(CreateStudyRequest request, String username) {
    manager.create(
        new StudyManager.CreateStudyCommand(
            request.code(),
            request.name(),
            request.indication(),
            request.phase(),
            request.status(),
            request.ownerName(),
            request.startDate()),
        username);
  }
}
