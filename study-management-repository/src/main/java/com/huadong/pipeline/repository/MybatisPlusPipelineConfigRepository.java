package com.huadong.pipeline.repository;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.config.PipelineConfigPage;
import com.huadong.pipeline.domain.config.PipelineConfigRepository;
import com.huadong.pipeline.domain.config.PipelineConfigRow;
import com.huadong.pipeline.domain.config.StudyReferenceCounts;
import com.huadong.pipeline.domain.config.TherapeuticArea;
import com.huadong.pipeline.repository.entity.StudyEntity;
import com.huadong.pipeline.repository.mapper.PipelineConfigMapper;
import com.huadong.pipeline.repository.mapper.PipelineConfigRowData;
import com.huadong.pipeline.repository.mapper.StudyMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusPipelineConfigRepository implements PipelineConfigRepository {
  @Autowired
  private PipelineConfigMapper mapper;
  @Autowired
  private StudyMapper studyMapper;

  @Override
  public List<PipelineConfigRow> findAll() {
    return mapper.findRows().stream().map(this::toRow).toList();
  }

  @Override
  public PipelineConfigPage findPage(String keyword, int page, int pageSize) {
    int safePage = Math.max(page, 1);
    int safeSize = Math.min(Math.max(pageSize, 1), 100);
    String term = keyword == null || keyword.isBlank() ? null : keyword.trim();
    long total = mapper.countRows(term);
    if (total == 0) {
      return new PipelineConfigPage(List.of(), safePage, safeSize, 0);
    }
    var rows = mapper.findRowsPage(term, safeSize, (safePage - 1) * safeSize).stream()
        .map(this::toRow)
        .toList();
    return new PipelineConfigPage(rows, safePage, safeSize, total);
  }

  private PipelineConfigRow toRow(PipelineConfigRowData row) {
    return new PipelineConfigRow(
        row.studyId(), row.version(), row.studyCode(), row.phaseStatusCode(),
        row.projectId(), row.projectCode(), row.indication(),
        row.therapeuticAreaCode(), row.therapeuticAreaName(), row.programId(), row.programCode(),
        row.productName(), row.moa(), row.sourceCode(), row.originCode(),
        row.updatedAt());
  }

  @Override
  public List<TherapeuticArea> findTherapeuticAreas() {
    return mapper.findTherapeuticAreas().stream()
        .map(value -> new TherapeuticArea(
            value.id(), value.code(), value.name(), value.englishName()))
        .toList();
  }

  @Override public long countProjects(long id) { return mapper.countProjects(id); }
  @Override public long countStudiesByProgram(long id) { return mapper.countStudiesByProgram(id); }
  @Override public long countStudiesByProject(long id) { return mapper.countStudiesByProject(id); }

  @Override
  public StudyReferenceCounts countStudyReferences(long studyId) {
    return new StudyReferenceCounts(
        mapper.countTeamReferences(studyId), mapper.countMilestoneReferences(studyId),
        mapper.countMonthlyReferences(studyId), mapper.countRiskReferences(studyId));
  }

  @Override
  public void updateStudy(long studyId, long projectId, String phaseStatusCode,
      int expectedVersion, String username) {
    var hierarchy = mapper.findHierarchyByProjectId(projectId);
    if (hierarchy == null) throw new IllegalArgumentException("Project not found");
    int rows = studyMapper.update(null,
        Wrappers.<StudyEntity>lambdaUpdate()
            .set(StudyEntity::getPhaseStatusCode, phaseStatusCode)
            .set(StudyEntity::getProgramId, hierarchy.getProgramId())
            .set(StudyEntity::getProgramCodeSnapshot, hierarchy.getProgramCode())
            .set(StudyEntity::getProductNameSnapshot, hierarchy.getProductName())
            .set(StudyEntity::getMoaSnapshot, hierarchy.getMoa())
            .set(StudyEntity::getSourceCodeSnapshot, hierarchy.getSourceCode())
            .set(StudyEntity::getOriginCodeSnapshot, hierarchy.getOriginCode())
            .set(StudyEntity::getProjectId, hierarchy.getProjectId())
            .set(StudyEntity::getProjectCodeSnapshot, hierarchy.getProjectCode())
            .set(StudyEntity::getTherapeuticAreaId, hierarchy.getTherapeuticAreaId())
            .set(StudyEntity::getTherapeuticAreaCodeSnapshot, hierarchy.getTherapeuticAreaCode())
            .set(StudyEntity::getTherapeuticAreaNameSnapshot, hierarchy.getTherapeuticAreaName())
            .set(StudyEntity::getIndicationDescriptionSnapshot, hierarchy.getIndicationDescription())
            .set(StudyEntity::getSysUpdateBy, username)
            .set(StudyEntity::getSysUpdateTime, LocalDateTime.now())
            .setSql("version = version + 1")
            .eq(StudyEntity::getId, studyId)
            .eq(StudyEntity::getSysDeleted, 0)
            .eq(StudyEntity::getVersion, expectedVersion));
    if (rows == 0) {
      throw new BusinessException("VERSION_CONFLICT", "数据已被他人修改，请刷新后重试");
    }
  }

  @Override
  public void softDeleteStudyReferences(long studyId, String username) {
    mapper.softDeleteRiskActionsByStudy(studyId, username);
    mapper.softDeleteRisksByStudy(studyId, username);
    mapper.softDeleteMonthlyEntriesByStudy(studyId, username);
    mapper.softDeleteMonthlyReportsByStudy(studyId, username);
    mapper.softDeleteMilestonesByStudy(studyId, username);
    mapper.softDeleteTeamAssignmentsByStudy(studyId, username);
  }

  @Override
  public void softDeleteStudy(long studyId, String username) {
    var entity = new StudyEntity();
    entity.setSysUpdateBy(username);
    entity.setSysUpdateTime(LocalDateTime.now());
    entity.setSysDeleted((short) 1);
    studyMapper.update(entity, Wrappers.<StudyEntity>lambdaUpdate()
        .eq(StudyEntity::getId, studyId).eq(StudyEntity::getSysDeleted, 0));
  }
}
