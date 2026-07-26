package com.huadong.pipeline.domain.risk;

import com.huadong.pipeline.domain.study.StudyAccessScope;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RiskRepository {
  RiskPage findPage(StudyAccessScope scope, RiskQuery query);

  /** Open risks for the given study ids (already scope-filtered caller-side). */
  List<RiskSummary> findOpenByStudyIds(StudyAccessScope scope, List<Long> studyIds);

  Optional<RiskDetail> findDetail(StudyAccessScope scope, String riskCode);
  FormOptions findFormOptions(StudyAccessScope scope, Long studyId);
  Optional<StudyContext> findStudy(StudyAccessScope scope, long studyId);
  Optional<MemberOption> findStudyMember(long studyId, long userId);
  Optional<FunctionOption> findFunction(long studyId, long userId, boolean allStudies,
                                        long functionLineId);
  Rule activeRule();
  RiskDetail create(CreateRisk data, Assessment assessment, List<CreateAction> actions,
                    Operator operator);
  RiskDetail update(String riskCode, long expectedVersion, UpdateRisk data,
                    Assessment assessment, Operator operator);
  void softDelete(String riskCode, long expectedVersion, StudyAccessScope scope,
                  Operator operator);
  RiskDetail addAction(String riskCode, long expectedRiskVersion, CreateAction action,
                       StudyAccessScope scope, Operator operator);
  RiskDetail updateAction(String riskCode, long actionId, long expectedActionVersion,
                          UpdateAction action, StudyAccessScope scope, Operator operator);
  RiskDetail deleteAction(String riskCode, long actionId, long expectedActionVersion,
                          StudyAccessScope scope, Operator operator);

  record RiskQuery(String query, String functionCode, String status, String level,
                   Long studyId, String sortBy, String sortOrder, int page, int pageSize) {}
  record RiskPage(List<RiskSummary> data, Stats stats, int page, int pageSize,
                  long totalItems) {}
  record Stats(long total, long open, long high, long medium) {}
  record RiskSummary(String riskCode, long studyId, String studyCode, String programCode,
                     String projectCode, String functionCode, String functionName,
                     String description, long ownerUserId, String ownerName,
                     int score, RiskLevel level, String status, int actionCount,
                     long version, Instant updatedAt) {}
  record RiskDetail(RiskSummary risk, LocalDate registeredDate, String closeReason,
                    List<AssessmentView> assessments, List<ActionView> actions) {}
  record AssessmentView(long id, int number, int impact, int likelihood, int detectability,
                        int score, RiskLevel level, String reason, String assessedBy,
                        Instant assessedAt) {}
  record ActionView(long id, String description, long ownerUserId, String ownerName,
                    LocalDate plannedDate, LocalDate completedDate, String status,
                    String completionNote, long version) {}
  record StudyOption(long id, String studyCode, String programCode, String projectCode) {}
  record FunctionOption(long id, String code, String name) {}
  record MemberOption(long id, String email, String displayName) {}
  record FormOptions(List<StudyOption> studies, List<FunctionOption> functions,
                     List<MemberOption> owners, Rule scoringRule) {}
  record StudyContext(long id, String studyCode, long programId, String programCode,
                      long projectId, String projectCode) {}
  record Rule(long id, int lowMax, int mediumMax) {}
  record Operator(long id, String username) {}
  record Assessment(int impact, int likelihood, int detectability, int score,
                    RiskLevel level, String reason, long ruleId) {}
  record CreateRisk(StudyContext study, FunctionOption function, MemberOption owner,
                    String description, LocalDate registeredDate) {}
  record UpdateRisk(StudyContext study, FunctionOption function, MemberOption owner,
                    String description, LocalDate registeredDate, String status,
                    boolean closing, String statusReason) {}
  record CreateAction(String description, MemberOption owner, LocalDate plannedDate,
                      LocalDate completedDate, String status, String completionNote) {}
  record UpdateAction(String description, MemberOption owner, LocalDate plannedDate,
                      LocalDate completedDate, String status, String completionNote) {}
}
