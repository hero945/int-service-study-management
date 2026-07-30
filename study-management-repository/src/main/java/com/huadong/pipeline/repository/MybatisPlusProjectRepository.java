package com.huadong.pipeline.repository;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.config.Project;
import com.huadong.pipeline.domain.config.ProjectRepository;
import com.huadong.pipeline.repository.entity.ProjectEntity;
import com.huadong.pipeline.repository.mapper.PipelineConfigMapper;
import com.huadong.pipeline.repository.mapper.ProjectMapper;
import com.huadong.pipeline.repository.mapper.ProjectSummaryData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusProjectRepository implements ProjectRepository {
  @Autowired
  private ProjectMapper mapper;
  @Autowired
  private PipelineConfigMapper configMapper;

  @Override
  public List<Project> findAll(Long programId, String keyword) {
    return configMapper.findProjects(programId, keyword == null ? "" : keyword.trim()).stream()
        .map(row -> new Project(row.id(), row.version(), row.code(), row.programId(), row.programCode(),
            row.indication(), row.therapeuticAreaId(), row.therapeuticAreaCode(),
            row.therapeuticAreaName(), row.studyCount(), row.updatedAt()))
        .toList();
  }

  @Override
  public Optional<Project> findById(long id) {
    var row = configMapper.findProject(id);
    return Optional.ofNullable(row).map(value -> new Project(
        value.id(), value.version(), value.code(), value.programId(), value.programCode(),
        value.indication(), value.therapeuticAreaId(), value.therapeuticAreaCode(),
        value.therapeuticAreaName(), value.studyCount(), value.updatedAt()));
  }

  @Override
  public Optional<Project> findByCode(String code) {
    return Optional.ofNullable(configMapper.findProjectByCode(code))
        .map(row -> new Project(row.id(), row.version(), row.code(), row.programId(), row.programCode(),
            row.indication(), row.therapeuticAreaId(), row.therapeuticAreaCode(),
            row.therapeuticAreaName(), row.studyCount(), row.updatedAt()));
  }

  @Override
  public Optional<Integer> findMaxVersionByCode(String code) {
    var query = Wrappers.<ProjectEntity>lambdaQuery()
        .eq(ProjectEntity::getProjectCode, code)
        .select(ProjectEntity::getVersion)
        .orderByDesc(ProjectEntity::getVersion)
        .last("LIMIT 1");
    return Optional.ofNullable(mapper.selectOne(query)).map(ProjectEntity::getVersion);
  }

  @Override
  public Project create(String code, long programId, String indication,
      String therapeuticAreaCode, int version, String username) {
    Long areaId = configMapper.findTherapeuticAreaId(therapeuticAreaCode);
    if (areaId == null) throw new IllegalArgumentException("Therapeutic area not found");
    var entity = new ProjectEntity();
    entity.setProjectCode(code);
    entity.setProgramId(programId);
    entity.setIndicationDescription(indication);
    entity.setTherapeuticAreaId(areaId);
    entity.setVersion(version);
    entity.setSortOrder(0);
    entity.setSysCreateBy(username);
    entity.setSysUpdateBy(username);
    try {
      mapper.insert(entity);
    } catch (DuplicateKeyException ex) {
      throw new BusinessException("PROJECT_CODE_EXISTS", "Project 编码已存在", ex);
    }
    return findById(entity.getId()).orElseThrow();
  }

  @Override
  public void update(long id, String indication, String therapeuticAreaCode,
      int expectedVersion, String username) {
    Long areaId = configMapper.findTherapeuticAreaId(therapeuticAreaCode);
    if (areaId == null) throw new IllegalArgumentException("Therapeutic area not found");
    int rows = mapper.update(null,
        Wrappers.<ProjectEntity>lambdaUpdate()
            .set(ProjectEntity::getIndicationDescription, indication)
            .set(ProjectEntity::getTherapeuticAreaId, areaId)
            .set(ProjectEntity::getSysUpdateBy, username)
            .set(ProjectEntity::getSysUpdateTime, LocalDateTime.now())
            .setSql("version = version + 1")
            .eq(ProjectEntity::getId, id)
            .eq(ProjectEntity::getSysDeleted, 0)
            .eq(ProjectEntity::getVersion, expectedVersion));
    if (rows == 0) {
      throw new BusinessException("VERSION_CONFLICT", "数据已被他人修改，请刷新后重试");
    }
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
