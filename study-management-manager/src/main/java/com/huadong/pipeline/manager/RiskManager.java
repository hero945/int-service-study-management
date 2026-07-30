package com.huadong.pipeline.manager;


import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.risk.RiskLevel;
import com.huadong.pipeline.domain.risk.RiskRepository;
import com.huadong.pipeline.domain.risk.RiskRepository.ActionView;
import com.huadong.pipeline.domain.risk.RiskRepository.CreateAction;
import com.huadong.pipeline.domain.risk.RiskRepository.FormOptions;
import com.huadong.pipeline.domain.risk.RiskRepository.RiskDetail;
import com.huadong.pipeline.domain.risk.RiskRepository.RiskPage;
import com.huadong.pipeline.domain.risk.RiskRepository.RiskQuery;
import com.huadong.pipeline.domain.risk.RiskRepository.UpdateAction;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.domain.user.UserAccount;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiskManager {
  private static final Set<String> RISK_STATUSES = Set.of("OPEN", "CLOSED");
  private static final Set<String> ACTION_STATUSES =
      Set.of("OPEN", "IN_PROGRESS", "COMPLETED", "CANCELLED");
  private static final Set<String> CREATE_ACTION_STATUSES =
      Set.of("OPEN", "IN_PROGRESS", "COMPLETED");
  private static final Map<String, Set<String>> ACTION_TRANSITIONS = Map.of(
      "OPEN", Set.of("IN_PROGRESS", "COMPLETED", "CANCELLED"),
      "IN_PROGRESS", Set.of("COMPLETED", "CANCELLED"),
      "COMPLETED", Set.of("IN_PROGRESS"),
      "CANCELLED", Set.of("IN_PROGRESS"));

  @Autowired
  private RiskRepository risks;
  @Autowired
  private UserAccountRepository users;

  public RiskPage list(String username, RiskQuery query) {
    return risks.findPage(scope(currentUser(username)), normalized(query));
  }

  public RiskDetail detail(String username, String riskCode) {
    return risks.findDetail(scope(currentUser(username)), normalizedCode(riskCode))
        .orElseThrow(() -> notFound());
  }

  public FormOptions formOptions(String username, Long studyId) {
    UserAccount user = currentUser(username);
    if (studyId != null && risks.findStudy(scope(user), studyId).isEmpty()) throw outOfScope();
    return risks.findFormOptions(scope(user), studyId);
  }

  @Transactional
  public RiskDetail create(CreateCommand command, String username) {
    UserAccount user = currentUser(username);
    var study = risks.findStudy(scope(user), command.studyId()).orElseThrow(RiskManager::outOfScope);
    var function = risks.findFunction(
        study.id(), user.id(), user.dataScope() == DataScope.ALL, command.functionLineId())
        .orElseThrow(() -> invalid("功能线不在当前用户对该Study的授权范围"));
    var owner = member(study.id(), command.ownerUserId());
    String description = required(command.description(), "风险描述", 4000);
    LocalDate registeredDate = command.registeredDate() == null
        ? LocalDate.now() : command.registeredDate();
    var assessment = assessment(command.assessment(), true);
    List<CreateAction> actions = command.actions() == null ? List.of()
        : command.actions().stream()
            .map(action -> createAction(study.id(), action, null)).toList();
    return risks.create(new RiskRepository.CreateRisk(
            study, function, owner, description, registeredDate),
        assessment, actions, new RiskRepository.Operator(user.id(), user.username()));
  }

  @Transactional
  public RiskDetail update(String riskCode, UpdateCommand command, String username) {
    UserAccount user = currentUser(username);
    StudyAccessScope scope = scope(user);
    RiskDetail before = risks.findDetail(scope, normalizedCode(riskCode))
        .orElseThrow(RiskManager::notFound);
    String status = enumValue(command.status(), RISK_STATUSES, "风险状态");
    String reason = trim(command.statusReason());
    boolean statusChanged = !before.risk().status().equals(status);
    boolean closing = statusChanged && "CLOSED".equals(status);
    boolean reopening = statusChanged && "OPEN".equals(status)
        && "CLOSED".equals(before.risk().status());

    if ("CLOSED".equals(before.risk().status()) && !reopening) {
      throw invalid("已关闭的风险不可编辑，请先重新打开");
    }
    if (statusChanged && reason.isBlank()) {
      throw invalid("关闭或重新打开风险时必须填写原因");
    }
    if (closing) {
      boolean hasActive = before.actions().stream()
          .anyMatch(item -> "OPEN".equals(item.status()) || "IN_PROGRESS".equals(item.status()));
      if (hasActive) {
        throw invalid("存在未完成的控制措施，请先完成或取消后再关闭风险");
      }
    }

    var study = risks.findStudy(scope, command.studyId()).orElseThrow(RiskManager::outOfScope);
    var function = risks.findFunction(
        study.id(), user.id(), user.dataScope() == DataScope.ALL, command.functionLineId())
        .orElseThrow(() -> invalid("功能线不在当前用户对该Study的授权范围"));
    var owner = member(study.id(), command.ownerUserId());
    var assessment = command.assessment() == null ? null : assessment(command.assessment(), true);
    return risks.update(before.risk().riskCode(), command.expectedVersion(),
        new RiskRepository.UpdateRisk(study, function, owner,
            required(command.description(), "风险描述", 4000),
            command.registeredDate() == null ? before.registeredDate() : command.registeredDate(),
            before.risk().status(), status, statusChanged, closing, reason), assessment,
        new RiskRepository.Operator(user.id(), user.username()));
  }

  @Transactional
  public void delete(String riskCode, long expectedVersion, String username) {
    UserAccount user = currentUser(username);
    risks.softDelete(normalizedCode(riskCode), expectedVersion, scope(user),
        new RiskRepository.Operator(user.id(), user.username()));
  }

  @Transactional
  public RiskDetail addAction(String riskCode, long expectedRiskVersion,
                              ActionCommand command, String username) {
    UserAccount user = currentUser(username);
    RiskDetail risk = detail(username, riskCode);
    assertRiskOpen(risk);
    return risks.addAction(risk.risk().riskCode(), expectedRiskVersion,
        createAction(risk.risk().studyId(), command, null), scope(user),
        new RiskRepository.Operator(user.id(), user.username()));
  }

  @Transactional
  public RiskDetail updateAction(String riskCode, long actionId, long expectedActionVersion,
                                 ActionCommand command, String username) {
    UserAccount user = currentUser(username);
    RiskDetail risk = detail(username, riskCode);
    assertRiskOpen(risk);
    ActionView existing = risk.actions().stream().filter(item -> item.id() == actionId).findFirst()
        .orElseThrow(() -> new BusinessException("RISK_ACTION_NOT_FOUND", "风险措施不存在"));
    CreateAction validated = createAction(risk.risk().studyId(), command, existing.status());
    boolean reopen = isTerminal(existing.status()) && "IN_PROGRESS".equals(validated.status());
    String changeReason = trim(command.changeReason());
    if (reopen && changeReason.isBlank()) {
      throw invalid("重新打开措施时必须填写原因");
    }
    return risks.updateAction(risk.risk().riskCode(), existing.id(), expectedActionVersion,
        new UpdateAction(validated.description(), validated.owner(), validated.plannedDate(),
            validated.completedDate(), validated.status(), validated.completionNote(),
            changeReason, reopen),
        scope(user), new RiskRepository.Operator(user.id(), user.username()));
  }

  @Transactional
  public RiskDetail deleteAction(String riskCode, long actionId, long expectedActionVersion,
                                 String username) {
    UserAccount user = currentUser(username);
    RiskDetail risk = detail(username, riskCode);
    assertRiskOpen(risk);
    return risks.deleteAction(normalizedCode(riskCode), actionId, expectedActionVersion,
        scope(user), new RiskRepository.Operator(user.id(), user.username()));
  }

  private RiskRepository.Assessment assessment(AssessmentCommand input, boolean required) {
    if (input == null) {
      if (required) throw invalid("风险评估不能为空");
      return null;
    }
    if (input.impact() < 1 || input.impact() > 5 || input.likelihood() < 1
        || input.likelihood() > 5 || input.detectability() < 1 || input.detectability() > 5) {
      throw invalid("影响、可能性和可探测性必须为1至5");
    }
    var rule = risks.activeRule();
    int score = input.impact() * input.likelihood() * (6 - input.detectability());
    return new RiskRepository.Assessment(input.impact(), input.likelihood(),
        input.detectability(), score,
        RiskLevel.fromScore(score, rule.lowMax(), rule.mediumMax()),
        trim(input.reason()), rule.id());
  }

  private CreateAction createAction(long studyId, ActionCommand command, String fromStatus) {
    var owner = member(studyId, command.ownerUserId());
    String status = command.status() == null || command.status().isBlank()
        ? "OPEN" : enumValue(command.status(), ACTION_STATUSES, "措施状态");
    if (fromStatus == null) {
      if (!CREATE_ACTION_STATUSES.contains(status)) {
        throw invalid("新建措施状态只能是未开始、进行中或已完成");
      }
    } else if (!fromStatus.equals(status)) {
      Set<String> allowed = ACTION_TRANSITIONS.getOrDefault(fromStatus, Set.of());
      if (!allowed.contains(status)) {
        throw invalid("措施状态不允许从 %s 变更为 %s".formatted(fromStatus, status));
      }
    }
    if (command.plannedDate() == null) {
      throw invalid("控制措施必须填写计划完成日期");
    }
    String note = trim(command.completionNote());
    if ("COMPLETED".equals(status)) {
      if (command.completedDate() == null) {
        throw invalid("措施完成时必须填写实际完成日期");
      }
      if (note.isBlank()) {
        throw invalid("措施完成时必须填写完成说明");
      }
    }
    if ("CANCELLED".equals(status) && note.isBlank()) {
      throw invalid("取消措施时必须填写取消说明");
    }
    return new CreateAction(required(command.description(), "措施内容", 4000), owner,
        command.plannedDate(), command.completedDate(), status, note);
  }

  private static void assertRiskOpen(RiskDetail risk) {
    if ("CLOSED".equals(risk.risk().status())) {
      throw invalid("已关闭的风险不可再维护控制措施");
    }
  }

  private static boolean isTerminal(String status) {
    return "COMPLETED".equals(status) || "CANCELLED".equals(status);
  }

  private RiskRepository.MemberOption member(long studyId, long userId) {
    return risks.findStudyMember(studyId, userId)
        .orElseThrow(() -> invalid("责任人必须是目标Study的有效团队成员"));
  }

  private RiskQuery normalized(RiskQuery query) {
    int page = Math.max(1, query.page());
    int pageSize = Math.min(100, Math.max(1, query.pageSize()));
    String status = optionalEnum(query.status(), RISK_STATUSES, "风险状态");
    String level = optionalEnum(query.level(), Set.of("LOW", "MEDIUM", "HIGH"), "风险等级");
    return new RiskQuery(trim(query.query()), trim(query.functionCode()), status, level,
        query.studyId(), query.ownerUserId(), query.overdueOnly(),
        trim(query.sortBy()), trim(query.sortOrder()), page, pageSize);
  }

  private UserAccount currentUser(String username) {
    return users.findByUsername(username)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "当前登录账号不存在"));
  }
  private static StudyAccessScope scope(UserAccount user) {
    return user.dataScope() == DataScope.ALL ? StudyAccessScope.all()
        : StudyAccessScope.assignedTo(user.id());
  }
  private static String required(String value, String label, int max) {
    String result = trim(value);
    if (result.isBlank() || result.length() > max) throw invalid(label + "不能为空且不能超过" + max + "字");
    return result;
  }
  private static String optionalEnum(String value, Set<String> allowed, String label) {
    if (value == null || value.isBlank()) return "";
    return enumValue(value, allowed, label);
  }
  private static String enumValue(String value, Set<String> allowed, String label) {
    String normalized = trim(value).toUpperCase();
    if (!allowed.contains(normalized)) throw invalid(label + "不合法");
    return normalized;
  }
  private static String normalizedCode(String value) {
    String code = trim(value);
    if (!code.matches("RSK-[0-9]{4}-[0-9]{6}")) throw notFound();
    return code;
  }
  private static String trim(String value) { return value == null ? "" : value.trim(); }
  private static BusinessException invalid(String message) {
    return new BusinessException("INVALID_RISK", message);
  }
  private static BusinessException notFound() {
    return new BusinessException("RISK_NOT_FOUND", "风险不存在或不在当前数据范围");
  }
  private static BusinessException outOfScope() {
    return new BusinessException("STUDY_OUT_OF_SCOPE", "目标Study不存在或不在当前数据范围");
  }

  public record AssessmentCommand(int impact, int likelihood, int detectability, String reason) {}
  public record ActionCommand(String description, long ownerUserId, LocalDate plannedDate,
                              LocalDate completedDate, String status, String completionNote,
                              String changeReason) {}
  public record CreateCommand(long studyId, long functionLineId, long ownerUserId,
                              String description, LocalDate registeredDate,
                              AssessmentCommand assessment, List<ActionCommand> actions) {}
  public record UpdateCommand(long expectedVersion, long studyId, long functionLineId,
                              long ownerUserId, String description, LocalDate registeredDate,
                              String status, String statusReason, AssessmentCommand assessment) {}
}
