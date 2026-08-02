package com.huadong.pipeline.service;


import com.huadong.pipeline.api.RiskApi;
import com.huadong.pipeline.domain.risk.RiskRepository;
import com.huadong.pipeline.manager.RiskManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskApiService implements RiskApi {
  @Autowired
  private RiskManager manager;

  @Override
  public PageResponse list(String username, String query, String functionCode, String status,
                           String level, Long studyId, Long ownerUserId, Boolean overdueOnly,
                           String sortBy, String sortOrder, int page, int pageSize) {
    var result = manager.list(username, new RiskRepository.RiskQuery(
        query, functionCode, status, level, studyId, ownerUserId, overdueOnly,
        sortBy, sortOrder, page, pageSize));
    int pages = Math.max(1, (int) Math.ceil((double) result.totalItems() / result.pageSize()));
    return new PageResponse(result.data().stream().map(this::summary).toList(),
        new StatsResponse(result.stats().total(), result.stats().open(),
            result.stats().high(), result.stats().medium()),
        new PaginationResponse(result.page(), result.pageSize(), result.totalItems(), pages));
  }

  @Override public DetailResponse detail(String username, String riskCode) {
    return detail(manager.detail(username, riskCode));
  }
  @Override public FormOptionsResponse formOptions(String username, Long studyId) {
    var options = manager.formOptions(username, studyId);
    var rule = options.scoringRule();
    return new FormOptionsResponse(
        options.studies().stream().map(item -> new StudyOptionResponse(
            item.id(), item.studyCode(), item.programCode(), item.projectCode())).toList(),
        options.functions().stream().map(item -> new FunctionOptionResponse(
            item.id(), item.code(), item.name())).toList(),
        options.owners().stream().map(item -> new MemberOptionResponse(
            item.id(), item.email(), item.displayName())).toList(),
        new ScoringRuleResponse(rule.id(), rule.lowMax(), rule.mediumMax()));
  }
  @Override
  @Transactional
  public DetailResponse create(CreateRequest request, String username) {
    return detail(manager.create(new RiskManager.CreateCommand(request.studyId(),
        request.functionLineId(), request.ownerUserId(), request.description(),
        request.registeredDate(), assessment(request.assessment()), actions(request.actions())), username));
  }
  @Override
  @Transactional
  public DetailResponse update(String riskCode, UpdateRequest request, String username) {
    return detail(manager.update(riskCode, new RiskManager.UpdateCommand(
        request.expectedVersion(), request.studyId(), request.functionLineId(),
        request.ownerUserId(), request.description(), request.registeredDate(), request.status(),
        request.statusReason(), request.assessment() == null ? null : assessment(request.assessment())),
        username));
  }
  @Override
  @Transactional
  public void delete(String riskCode, long expectedVersion, String username) {
    manager.delete(riskCode, expectedVersion, username);
  }
  @Override
  @Transactional
  public DetailResponse addAction(
      String riskCode, ActionCreateRequest request, String username) {
    return detail(manager.addAction(
        riskCode, request.expectedRiskVersion(), action(request.action()), username));
  }
  @Override
  @Transactional
  public DetailResponse updateAction(
      String riskCode, long actionId, ActionUpdateRequest request, String username) {
    return detail(manager.updateAction(
        riskCode, actionId, request.expectedVersion(), action(request.action()), username));
  }
  @Override
  @Transactional
  public DetailResponse deleteAction(
      String riskCode, long actionId, long expectedVersion, String username) {
    return detail(manager.deleteAction(riskCode, actionId, expectedVersion, username));
  }

  private SummaryResponse summary(RiskRepository.RiskSummary item) {
    return new SummaryResponse(item.riskId(), item.riskCode(), item.studyId(), item.studyCode(),
        item.programCode(), item.projectCode(), item.functionCode(), item.functionName(),
        item.description(), item.ownerUserId(), item.ownerName(), item.score(), item.level().name(),
        item.status(), item.actionCount(), item.openActionCount(), item.overdueActionCount(),
        item.nextPlannedDate(), item.version(), item.updatedAt());
  }
  private DetailResponse detail(RiskRepository.RiskDetail item) {
    return new DetailResponse(summary(item.risk()), item.registeredDate(), item.closeReason(),
        item.closedTime(),
        item.assessments().stream().map(a -> new AssessmentResponse(a.id(), a.number(),
            a.impact(), a.likelihood(), a.detectability(), a.score(), a.level().name(),
            a.reason(), a.assessedBy(), a.assessedAt())).toList(),
        item.actions().stream().map(a -> new ActionResponse(a.id(), a.description(),
            a.ownerUserId(), a.ownerName(), a.plannedDate(), a.completedDate(), a.status(),
            a.completionNote(), a.version(), a.overdue())).toList(),
        item.activities().stream().map(a -> new ActivityResponse(
            a.type(), a.title(), a.detail(), a.at(), a.by())).toList());
  }
  private static RiskManager.AssessmentCommand assessment(AssessmentRequest input) {
    return new RiskManager.AssessmentCommand(
        input.impact(), input.likelihood(), input.detectability(), input.reason());
  }
  private static List<RiskManager.ActionCommand> actions(List<ActionInput> input) {
    return input == null ? List.of() : input.stream().map(RiskApiService::action).toList();
  }
  private static RiskManager.ActionCommand action(ActionInput input) {
    return new RiskManager.ActionCommand(input.description(), input.ownerUserId(),
        input.plannedDate(), input.completedDate(), input.status(), input.completionNote(),
        input.changeReason());
  }
}
