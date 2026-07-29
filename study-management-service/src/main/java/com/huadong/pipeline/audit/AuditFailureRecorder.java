package com.huadong.pipeline.audit;

import com.huadong.pipeline.domain.milestone.MilestoneDefinition;
import jakarta.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuditFailureRecorder {
  private static final Pattern MILESTONE =
      Pattern.compile("^/api/v1/studies/(\\d+)/milestones/([^/]+)$");
  private static final Pattern MONTHLY_ENTRY =
      Pattern.compile("^/api/v1/monthly-report-entries/(\\d+)$");
  private static final Pattern MONTHLY_ENTRY_CREATE =
      Pattern.compile("^/api/v1/monthly-reports/(\\d+)/entries$");
  private static final Pattern RISK =
      Pattern.compile("^/api/v1/risk-management/risks/([^/]+)(?:/actions(?:/(\\d+))?)?$");
  private static final Pattern USER =
      Pattern.compile("^/api/v1/platform/users(?:/(\\d+)(?:/[^/]+)?)?$");
  private static final Pattern ROLE =
      Pattern.compile("^/api/v1/platform/roles(?:/(\\d+))?$");
  private static final Pattern CONFIG =
      Pattern.compile("^/api/v1/clinical-pipeline/(programs|projects|studies)(?:/(\\d+))?$");

  private final BusinessAuditService audit;

  public AuditFailureRecorder(BusinessAuditService audit) {
    this.audit = audit;
  }

  public void record(HttpServletRequest request, String result, String errorCode, String reason) {
    if ("GET".equalsIgnoreCase(request.getMethod())) {
      return;
    }
    Descriptor descriptor = describe(request);
    if (descriptor == null) {
      return;
    }
    try {
      audit.failedGrouped(
          descriptor.moduleCode(), descriptor.subjectType(), descriptor.subjectId(),
          descriptor.subjectCode(), descriptor.scopeStudyId(),
          descriptor.groupType(), descriptor.groupId(), descriptor.groupCode(),
          descriptor.actionCode(),
          descriptor.targetTable(), descriptor.targetId(), result, reason, errorCode,
          currentUsername());
    } catch (Exception failure) {
      log.error("保存失败或拒绝审计日志失败 path={} code={}",
          request.getRequestURI(), errorCode, failure);
    }
  }

  private static Descriptor describe(HttpServletRequest request) {
    String path = request.getRequestURI();
    Matcher match = MILESTONE.matcher(path);
    if (match.matches()) {
      long studyId = Long.parseLong(match.group(1));
      return new Descriptor(
          "MILESTONE", "MILESTONE", null, match.group(2), studyId,
          "MILESTONE_STAGE", null, milestoneStage(match.group(2)),
          "MILESTONE_UPDATE", "hd_plt_study_milestone", null);
    }
    match = MONTHLY_ENTRY.matcher(path);
    if (match.matches()) {
      long entryId = Long.parseLong(match.group(1));
      return new Descriptor(
          "MONTHLY", "MONTHLY_ENTRY", entryId, String.valueOf(entryId), null,
          null, null, null,
          action("MONTHLY_ENTRY", request.getMethod()), "hd_plt_monthly_report_entry", entryId);
    }
    match = MONTHLY_ENTRY_CREATE.matcher(path);
    if (match.matches()) {
      return new Descriptor(
          "MONTHLY", "MONTHLY_ENTRY", null, null, null,
          "MONTHLY_FUNCTION", Long.valueOf(match.group(1)), null,
          "MONTHLY_ENTRY_CREATE", "hd_plt_monthly_report_entry", null);
    }
    match = RISK.matcher(path);
    if (match.matches()) {
      String riskCode = match.group(1);
      Long actionId = match.group(2) == null ? null : Long.valueOf(match.group(2));
      String prefix = path.contains("/actions") ? "RISK_ACTION" : "RISK";
      return new Descriptor(
          "RISK", "RISK", null, riskCode, null, null, null, null,
          action(prefix, request.getMethod()),
          actionId == null ? "hd_plt_risk" : "hd_plt_risk_action", actionId);
    }
    if ("/api/v1/risk-management/risks".equals(path)) {
      return new Descriptor(
          "RISK", "RISK", null, null, null,
          null, null, null,
          "RISK_CREATE", "hd_plt_risk", null);
    }
    if (path.equals("/api/v1/team-matrix/assignments")) {
      return new Descriptor(
          "TEAM", "STUDY", null, null, null,
          null, null, null,
          "TEAM_ROLE_ASSIGN", "hd_plt_team_assignment", null);
    }
    match = CONFIG.matcher(path);
    if (match.matches()) {
      String type = match.group(1).substring(0, match.group(1).length() - 1).toUpperCase();
      Long id = match.group(2) == null ? null : Long.valueOf(match.group(2));
      return new Descriptor(
          "CONFIG", type, id, null, "STUDY".equals(type) ? id : null,
          null, null, null,
          action(type, request.getMethod()), "hd_plt_" + type.toLowerCase(), id);
    }
    match = USER.matcher(path);
    if (match.matches()) {
      Long id = match.group(1) == null ? null : Long.valueOf(match.group(1));
      String actionCode = path.endsWith("/roles") ? "USER_ROLE_ASSIGN"
          : path.endsWith("/password-reset") ? "PASSWORD_RESET"
          : action("USER", request.getMethod());
      return new Descriptor(
          "ACCOUNT", "USER", id, null, null, null, null, null,
          actionCode, "hd_plt_user", id);
    }
    if ("/api/v1/platform/me/password".equals(path)) {
      return new Descriptor(
          "ACCOUNT", "USER", null, currentUsername(), null,
          null, null, null,
          "PASSWORD_CHANGE", "hd_plt_user", null);
    }
    match = ROLE.matcher(path);
    if (match.matches()) {
      Long id = match.group(1) == null ? null : Long.valueOf(match.group(1));
      return new Descriptor(
          "ROLE", "ROLE", id, null, null,
          null, null, null,
          action("ROLE", request.getMethod()), "hd_plt_role", id);
    }
    return null;
  }

  private static String action(String prefix, String method) {
    return switch (method.toUpperCase()) {
      case "POST" -> prefix + "_CREATE";
      case "DELETE" -> prefix + "_DELETE";
      default -> prefix + "_UPDATE";
    };
  }

  private static String milestoneStage(String milestoneCode) {
    return MilestoneDefinition.ALL.stream()
        .filter(group -> group.nodes().stream()
            .anyMatch(node -> node.code().equals(milestoneCode)))
        .map(MilestoneDefinition.StageGroup::code)
        .findFirst()
        .orElse(null);
  }

  private static String currentUsername() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication == null || !authentication.isAuthenticated()
        ? "anonymous" : authentication.getName();
  }

  private record Descriptor(
      String moduleCode,
      String subjectType,
      Long subjectId,
      String subjectCode,
      Long scopeStudyId,
      String groupType,
      Long groupId,
      String groupCode,
      String actionCode,
      String targetTable,
      Long targetId) {
  }
}
