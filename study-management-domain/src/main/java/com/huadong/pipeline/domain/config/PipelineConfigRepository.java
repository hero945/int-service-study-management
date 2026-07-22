package com.huadong.pipeline.domain.config;

import java.util.List;

public interface PipelineConfigRepository {
  List<PipelineConfigRow> findAll();
  List<TherapeuticArea> findTherapeuticAreas();
  long countProjects(long programId);
  long countStudiesByProgram(long programId);
  long countStudiesByProject(long projectId);
  StudyReferenceCounts countStudyReferences(long studyId);
  void updateStudy(long studyId, long projectId, String phaseStatusCode,
      String username);
  void softDeleteStudy(long studyId, String username);
}
