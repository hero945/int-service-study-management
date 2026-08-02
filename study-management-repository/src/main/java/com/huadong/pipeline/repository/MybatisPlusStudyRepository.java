package com.huadong.pipeline.repository;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.common.StudyStatus;
import com.huadong.pipeline.domain.study.Study;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.study.StudyRepository;
import com.huadong.pipeline.domain.study.StudyRepository.StudyListQuery;
import com.huadong.pipeline.domain.study.StudyRepository.StudyPage;
import com.huadong.pipeline.repository.entity.StudyEntity;
import com.huadong.pipeline.repository.mapper.StudyMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusStudyRepository implements StudyRepository {
  private static final long LIST_LIMIT = 500;

  @Autowired
  private StudyMapper mapper;

  @Override
  public List<Study> findAll(StudyAccessScope accessScope) {
    var query = Wrappers.<StudyEntity>lambdaQuery()
        .eq(StudyEntity::getSysDeleted, 0)
        .orderByDesc(StudyEntity::getSysUpdateTime)
        .orderByDesc(StudyEntity::getId);
    applyAccessScope(query, accessScope);
    return mapper.selectPage(Page.of(1, LIST_LIMIT, false), query)
        .getRecords().stream().map(MybatisPlusStudyRepository::toDomain).toList();
  }

  @Override
  public StudyPage findPage(StudyAccessScope accessScope, StudyListQuery query) {
    StudyListQuery q = query.normalized();
    var wrapper = Wrappers.<StudyEntity>lambdaQuery()
        .eq(StudyEntity::getSysDeleted, 0)
        .orderByDesc(StudyEntity::getSysUpdateTime)
        .orderByDesc(StudyEntity::getId);
    applyAccessScope(wrapper, accessScope);
    if (!q.therapeuticArea().isBlank()) {
      wrapper.and(w -> w.eq(StudyEntity::getTherapeuticAreaNameSnapshot, q.therapeuticArea())
          .or()
          .eq(StudyEntity::getTherapeuticAreaCodeSnapshot, q.therapeuticArea()));
    }
    if (!q.program().isBlank()) {
      wrapper.like(StudyEntity::getProgramCodeSnapshot, q.program());
    }
    if (!q.product().isBlank()) {
      wrapper.like(StudyEntity::getProductNameSnapshot, q.product());
    }
    if (!q.studyCode().isBlank()) {
      wrapper.like(StudyEntity::getStudyCode, q.studyCode());
    }
    var result = mapper.selectPage(Page.of(q.page(), q.pageSize(), true), wrapper);
    return new StudyPage(
        result.getRecords().stream().map(MybatisPlusStudyRepository::toDomain).toList(),
        result.getTotal(),
        q.page(),
        q.pageSize());
  }

  @Override
  public long count(StudyAccessScope accessScope) {
    var query = Wrappers.<StudyEntity>lambdaQuery()
        .eq(StudyEntity::getSysDeleted, 0);
    applyAccessScope(query, accessScope);
    return mapper.selectCount(query);
  }

  @Override
  public long countByStatus(StudyStatus status, StudyAccessScope accessScope) {
    var query = Wrappers.<StudyEntity>lambdaQuery()
        .eq(StudyEntity::getSysDeleted, 0);
    applyAccessScope(query, accessScope);
    switch (status) {
      case PLANNED -> query.isNull(StudyEntity::getActualStartDate)
          .isNull(StudyEntity::getActualEndDate);
      case ACTIVE -> query.isNotNull(StudyEntity::getActualStartDate)
          .isNull(StudyEntity::getActualEndDate);
      case COMPLETED -> query.isNotNull(StudyEntity::getActualEndDate);
      case ON_HOLD -> query.apply("1 = 0");
    }
    return mapper.selectCount(query);
  }

  private static void applyAccessScope(
      LambdaQueryWrapper<StudyEntity> query,
      StudyAccessScope accessScope) {
    if (!accessScope.allStudies()) {
      query.apply("""
          EXISTS (
            SELECT 1 FROM hd_plt_team_assignment ta
            WHERE ta.study_id = hd_plt_study.id
              AND ta.user_id = {0} AND ta.sys_deleted = 0
          )
          """, accessScope.userId());
    }
  }

  @Override
  public Optional<Integer> findMaxVersionByCode(String code) {
    var query = Wrappers.<StudyEntity>lambdaQuery()
        .eq(StudyEntity::getStudyCode, code)
        .select(StudyEntity::getVersion)
        .orderByDesc(StudyEntity::getVersion)
        .last("LIMIT 1");
    return Optional.ofNullable(mapper.selectOne(query)).map(StudyEntity::getVersion);
  }

  @Override
  public Optional<Study> findByCode(String code) {
    return Optional.ofNullable(mapper.findByCode(code)).map(MybatisPlusStudyRepository::toDomain);
  }

  @Override
  public void save(Study study, int version, String createdBy) {
    var hierarchy = mapper.findHierarchy(
        study.programCode(), study.projectCode(), study.therapeuticAreaCode());
    if (hierarchy == null) {
      throw new BusinessException(
          "INVALID_STUDY_HIERARCHY", "Program、Project 或治疗领域不存在，或三者关系不匹配");
    }

    var entity = new StudyEntity();
    entity.setStudyCode(study.code());
    entity.setVersion(version);
    entity.setPhaseStatusCode(study.phase());
    entity.setPlannedStartDate(study.startDate());
    entity.setPlannedEndDate(study.plannedEndDate());
    entity.setActualStartDate(study.actualStartDate());
    entity.setActualEndDate(study.actualEndDate());
    entity.setStudyDescription(study.description());
    entity.setProgramId(hierarchy.getProgramId());
    entity.setProgramCodeSnapshot(hierarchy.getProgramCode());
    entity.setProductNameSnapshot(hierarchy.getProductName());
    entity.setMoaSnapshot(hierarchy.getMoa());
    entity.setSourceCodeSnapshot(hierarchy.getSourceCode());
    entity.setOriginCodeSnapshot(hierarchy.getOriginCode());
    entity.setProjectId(hierarchy.getProjectId());
    entity.setProjectCodeSnapshot(hierarchy.getProjectCode());
    entity.setTherapeuticAreaId(hierarchy.getTherapeuticAreaId());
    entity.setTherapeuticAreaCodeSnapshot(hierarchy.getTherapeuticAreaCode());
    entity.setTherapeuticAreaNameSnapshot(hierarchy.getTherapeuticAreaName());
    entity.setIndicationDescriptionSnapshot(hierarchy.getIndicationDescription());
    entity.setSysCreateBy(createdBy);
    entity.setSysUpdateBy(createdBy);
    try {
      mapper.insert(entity);
    } catch (DuplicateKeyException ex) {
      throw new BusinessException("STUDY_CODE_EXISTS", "项目编号已存在", ex);
    }
  }

  private static Study toDomain(StudyEntity entity) {
    StudyStatus status = entity.getActualEndDate() != null
        ? StudyStatus.COMPLETED
        : entity.getActualStartDate() != null ? StudyStatus.ACTIVE : StudyStatus.PLANNED;
    return new Study(
        entity.getId(),
        entity.getVersion(),
        entity.getStudyCode(),
        entity.getIndicationDescriptionSnapshot(),
        entity.getPhaseStatusCode(),
        status,
        entity.getSysCreateBy(),
        entity.getPlannedStartDate(),
        entity.getSysUpdateTime(),
        entity.getProgramCodeSnapshot(),
        entity.getProjectCodeSnapshot(),
        entity.getTherapeuticAreaCodeSnapshot(),
        entity.getTherapeuticAreaNameSnapshot(),
        entity.getProductNameSnapshot(),
        entity.getMoaSnapshot(),
        entity.getSourceCodeSnapshot(),
        entity.getOriginCodeSnapshot(),
        entity.getPlannedEndDate(),
        entity.getActualStartDate(),
        entity.getActualEndDate(),
        entity.getStudyDescription());
  }
}
