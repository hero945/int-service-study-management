package com.huadong.pipeline.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huadong.pipeline.domain.config.Project;
import com.huadong.pipeline.domain.config.ProjectRepository;
import com.huadong.pipeline.repository.entity.ProjectEntity;
import com.huadong.pipeline.repository.mapper.PipelineConfigMapper;
import com.huadong.pipeline.repository.mapper.ProjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusProjectRepository implements ProjectRepository {
  private final ProjectMapper mapper;
  private final PipelineConfigMapper configMapper;

  public MybatisPlusProjectRepository(ProjectMapper mapper, PipelineConfigMapper configMapper) {
    this.mapper = mapper;
    this.configMapper = configMapper;
  }

  @Override
  public List<Project> findAll(Long programId, String keyword) {
    return configMapper.findProjects(programId, keyword == null ? "" : keyword.trim()).stream()
        .map(row -> new Project(row.id(), row.code(), row.name(), row.programId(), row.programCode(),
            row.indication(), row.therapeuticAreaId(), row.therapeuticAreaCode(),
            row.therapeuticAreaName(), row.studyCount(), row.updatedAt()))
        .toList();
  }

  @Override
  public Optional<Project> findById(long id) {
    var row = configMapper.findProject(id);
    return Optional.ofNullable(row).map(value -> new Project(
        value.id(), value.code(), value.name(), value.programId(), value.programCode(),
        value.indication(), value.therapeuticAreaId(), value.therapeuticAreaCode(),
        value.therapeuticAreaName(), value.studyCount(), value.updatedAt()));
  }

  @Override
  public Optional<Project> findByCode(String code) {
    return findAll(null, code).stream().filter(project -> project.code().equals(code)).findFirst();
  }

  @Override
  public Project create(String code, String name, long programId, String indication,
      String therapeuticAreaCode, String username) {
    Long areaId = configMapper.findTherapeuticAreaId(therapeuticAreaCode);
    if (areaId == null) throw new IllegalArgumentException("Therapeutic area not found");
    var entity = new ProjectEntity();
    entity.setProjectCode(code);
    entity.setProjectName(name);
    entity.setProgramId(programId);
    entity.setIndicationDescription(indication);
    entity.setTherapeuticAreaId(areaId);
    entity.setSortOrder(0);
    entity.setSysCreateBy(username);
    entity.setSysUpdateBy(username);
    mapper.insert(entity);
    return findById(entity.getId()).orElseThrow();
  }

  @Override
  public boolean update(long id, String name, String indication, String therapeuticAreaCode,
      LocalDateTime expectedUpdatedAt, String username) {
    Long areaId = configMapper.findTherapeuticAreaId(therapeuticAreaCode);
    if (areaId == null) throw new IllegalArgumentException("Therapeutic area not found");
    var entity = new ProjectEntity();
    entity.setProjectName(name);
    entity.setIndicationDescription(indication);
    entity.setTherapeuticAreaId(areaId);
    entity.setSysUpdateBy(username);
    entity.setSysUpdateTime(LocalDateTime.now());
    var update = Wrappers.<ProjectEntity>lambdaUpdate()
        .eq(ProjectEntity::getId, id).eq(ProjectEntity::getSysDeleted, 0);
    if (expectedUpdatedAt != null) update.eq(ProjectEntity::getSysUpdateTime, expectedUpdatedAt);
    return mapper.update(entity, update) == 1;
  }

  @Override
  public void softDelete(long id, String username) {
    var entity = new ProjectEntity();
    entity.setSysDeleted((short) 1);
    entity.setSysUpdateBy(username);
    entity.setSysUpdateTime(LocalDateTime.now());
    mapper.update(entity, Wrappers.<ProjectEntity>lambdaUpdate().eq(ProjectEntity::getId, id));
  }
}
