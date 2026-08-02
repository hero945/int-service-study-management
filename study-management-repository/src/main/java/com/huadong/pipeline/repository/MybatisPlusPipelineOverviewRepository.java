package com.huadong.pipeline.repository;


import com.huadong.pipeline.common.StudyStatus;
import com.huadong.pipeline.domain.study.OverviewProject;
import com.huadong.pipeline.domain.study.OverviewStudy;
import com.huadong.pipeline.domain.study.PipelineOverviewRepository;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.repository.mapper.OverviewProjectRow;
import com.huadong.pipeline.repository.mapper.OverviewStudyRow;
import com.huadong.pipeline.repository.mapper.PipelineOverviewMapper;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusPipelineOverviewRepository implements PipelineOverviewRepository {
  @Autowired
  private PipelineOverviewMapper mapper;

  @Override
  public List<OverviewProject> findOverviewProjects(StudyAccessScope scope) {
    var rows = mapper.findProjects(scope.userId(), scope.allStudies());
    if (rows.isEmpty()) {
      return List.of();
    }
    var projectIds = rows.stream().map(OverviewProjectRow::id).toList();
    var studyRows = mapper.findStudies(projectIds, scope.userId(), scope.allStudies());
    Map<Long, List<OverviewStudy>> studiesByProject = studyRows.stream()
        .collect(Collectors.groupingBy(
            OverviewStudyRow::projectId,
            Collectors.mapping(MybatisPlusPipelineOverviewRepository::toOverviewStudy,
                Collectors.toList())));
    return rows.stream()
        .map(row -> new OverviewProject(
            row.id(), row.code(), row.indication(), row.programCode(), row.productName(),
            row.moa(), row.sourceCode(), row.originCode(),
            row.therapeuticAreaCode(), row.therapeuticAreaName(),
            studiesByProject.getOrDefault(row.id(), List.of())))
        .toList();
  }

  private static OverviewStudy toOverviewStudy(OverviewStudyRow row) {
    StudyStatus status = row.actualEndDate() != null
        ? StudyStatus.COMPLETED
        : row.actualStartDate() != null ? StudyStatus.ACTIVE : StudyStatus.PLANNED;
    return new OverviewStudy(row.id(), row.code(), row.phase(), status, row.startDate(), row.updatedAt(),
        null, null, null, false, false, false, false, "", "", 0);
  }
}
