package com.huadong.pipeline.repository;

import com.huadong.pipeline.domain.milestone.ProjectMilestone;
import com.huadong.pipeline.domain.milestone.ProjectMilestonePort;
import com.huadong.pipeline.domain.milestone.ProjectMilestonePort.ProjectMilestoneCommand;
import com.huadong.pipeline.repository.mapper.ProjectMilestoneMapper;
import com.huadong.pipeline.repository.mapper.ProjectMilestoneRow;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusProjectMilestoneRepository implements ProjectMilestonePort {

  @Autowired
  private ProjectMilestoneMapper mapper;

  @Override
  public List<ProjectMilestone> findByProjectId(long projectId) {
    return mapper.findByProjectId(projectId).stream()
        .map(MybatisPlusProjectMilestoneRepository::toDomain)
        .toList();
  }

  @Override
  public List<ProjectMilestone> findByProjectIds(List<Long> projectIds) {
    if (projectIds == null || projectIds.isEmpty()) {
      return List.of();
    }
    return mapper.findByProjectIds(projectIds).stream()
        .map(MybatisPlusProjectMilestoneRepository::toDomain)
        .toList();
  }

  @Override
  public Map<Long, List<ProjectMilestone>> findByProjectIdsGrouped(List<Long> projectIds) {
    return findByProjectIds(projectIds).stream()
        .collect(Collectors.groupingBy(ProjectMilestone::projectId));
  }

  @Override
  public ProjectMilestone save(ProjectMilestoneCommand command) {
    mapper.upsert(new ProjectMilestoneMapper.ProjectMilestoneUpsertParams(
        null,
        command.projectId(),
        command.stageCode(),
        command.milestoneCode(),
        command.planV1Date(),
        command.planV2Date(),
        command.actualStartDate(),
        command.actualEndDate(),
        command.deviationNote(),
        command.operatorEmail()));

    ProjectMilestoneRow row = mapper.findByProjectIdAndMilestoneCode(
        command.projectId(), command.milestoneCode());
    if (row == null) {
      throw new IllegalStateException("Failed to read back saved project milestone: projectId="
          + command.projectId() + ", milestoneCode=" + command.milestoneCode());
    }
    return toDomain(row);
  }

  private static ProjectMilestone toDomain(ProjectMilestoneRow row) {
    return new ProjectMilestone(
        row.id(),
        row.projectId(),
        row.stageCode(),
        row.milestoneCode(),
        row.planV1Date(),
        row.planV2Date(),
        row.actualStartDate(),
        row.actualEndDate(),
        row.deviationNote(),
        row.updatedAt() == null ? LocalDateTime.now() : row.updatedAt());
  }
}
