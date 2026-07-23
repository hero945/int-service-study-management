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
  public PipelineOverviewResponse overview(String username) {
    var overview = manager.overview(username);
    var areas = overview.areas().stream()
        .map(area -> new OverviewAreaResponse(
            area.therapeuticAreaCode(),
            area.therapeuticAreaName(),
            area.projects().stream()
                .map(project -> new OverviewProjectResponse(
                    project.id(),
                    project.code(),
                    project.indication(),
                    project.programCode(),
                    project.productName(),
                    project.moa(),
                    project.sourceCode(),
                    project.originCode(),
                    project.studies().stream()
                        .map(study -> new OverviewStudyResponse(
                            study.id(),
                            study.code(),
                            study.phase(),
                            study.status().name(),
                            study.status().label(),
                            study.status().tone(),
                            study.mainStageCode(),
                            study.mainStageLabel(),
                            study.subStatusLabel(),
                            study.preindCompleted(),
                            study.indCompleted(),
                            study.globallyCompleted(),
                            study.currentPhaseCompleted(),
                            study.startDate(),
                            study.updatedAt()))
                        .toList()))
                .toList()))
        .toList();
    return new PipelineOverviewResponse(overview.title(), areas);
  }

  @Override
  public List<StudyResponse> list(String username) {
    return manager.list(username).stream()
        .map(study -> new StudyResponse(
            study.id(),
            study.code(),
            study.indication(),
            study.phase(),
            study.status().name(),
            study.status().label(),
            study.status().tone(),
            study.ownerName(),
            study.startDate(),
            study.plName(),
            study.pmName(),
            study.currentPhase(),
            study.currentStatus(),
            study.updatedAt(),
            study.therapeuticAreaCode(),
            study.therapeuticAreaName(),
            study.programCode(),
            study.projectCode(),
            study.productName(),
            study.moa(),
            study.sourceCode(),
            study.originCode()))
        .toList();
  }

  @Override
  public void create(CreateStudyRequest request, String username) {
    manager.create(
        new StudyManager.CreateStudyCommand(
            request.code(),
            request.projectId(),
            request.programCode(),
            request.projectCode(),
            request.therapeuticAreaCode(),
            request.phase(),
            request.plannedStartDate(),
            request.plannedEndDate(),
            request.actualStartDate(),
            request.actualEndDate(),
            request.description()),
        username);
  }
}
