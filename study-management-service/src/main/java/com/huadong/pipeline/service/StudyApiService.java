package com.huadong.pipeline.service;


import com.huadong.pipeline.api.StudyApi;
import com.huadong.pipeline.audit.BusinessAuditService;
import com.huadong.pipeline.domain.study.StudyRepository;
import com.huadong.pipeline.manager.StudyManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyApiService implements StudyApi {
  @Autowired
  private StudyManager manager;
  @Autowired
  private BusinessAuditService audit;

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
                            study.updatedAt(),
                            study.plName(),
                            study.pmName()))
                        .toList()))
                .toList()))
        .toList();
    return new PipelineOverviewResponse(overview.title(), areas);
  }

  @Override
  public StudyPageResponse list(
      String username,
      String therapeuticArea,
      String program,
      String milestoneStatus,
      int page,
      int pageSize) {
    var result = manager.list(username, new StudyRepository.StudyListQuery(
        therapeuticArea, program, milestoneStatus, page, pageSize));
    int totalPages = Math.max(1, (int) Math.ceil((double) result.totalItems() / result.pageSize()));
    return new StudyPageResponse(
        result.data().stream().map(this::toResponse).toList(),
        result.totalItems(),
        result.page(),
        result.pageSize(),
        totalPages);
  }

  private StudyResponse toResponse(StudyManager.StudyView study) {
    return new StudyResponse(
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
        study.originCode());
  }

  @Override
  @Transactional
  public void create(CreateStudyRequest request, String username) {
    var created = manager.create(
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
    audit.success(
        "CONFIG", "STUDY", created.id(), created.code(), created.id(),
        "STUDY_CREATE", "hd_plt_study", created.id(), null, created, null, username);
  }
}
