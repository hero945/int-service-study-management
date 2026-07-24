package com.huadong.pipeline.repository;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huadong.pipeline.domain.config.PipelineConfigRepository;
import com.huadong.pipeline.domain.config.PipelineConfigRow;
import com.huadong.pipeline.domain.config.StudyReferenceCounts;
import com.huadong.pipeline.domain.config.TherapeuticArea;
import com.huadong.pipeline.repository.entity.StudyEntity;
import com.huadong.pipeline.repository.mapper.PipelineConfigMapper;
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
    return mapper.findRows().stream().map(row -> new PipelineConfigRow(
        row.studyId(), row.studyCode(), row.phaseStatusCode(),
        row.projectId(), row.projectCode(), row.indication(),
        row.therapeuticAreaCode(), row.therapeuticAreaName(), row.programId(), row.programCode(),
        row.productName(), row.moa(), row.sourceCode(), row.originCode(),
        row.updatedAt())).toList();
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
      String username) {
    var hierarchy = mapper.findHierarchyByProjectId(projectId);
    if (hierarchy == null) throw new IllegalArgumentException("Project not found");
    var entity = new StudyEntity();
    entity.setId(studyId);
    entity.setPhaseStatusCode(phaseStatusCode);
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
    entity.setSysUpdateBy(username);
    entity.setSysUpdateTime(LocalDateTime.now());
    studyMapper.updateById(entity);
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
