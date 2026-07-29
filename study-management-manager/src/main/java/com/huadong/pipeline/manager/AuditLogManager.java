package com.huadong.pipeline.manager;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.audit.AuditLogRepository;
import com.huadong.pipeline.domain.audit.AuditLogRepository.AuditQuery;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.team.TeamMatrixRepository;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AuditLogManager {
  private static final Map<String, String> MODULE_READ_PERMISSION = Map.of(
      "MILESTONE", "milestone.read",
      "MONTHLY", "monthly.read",
      "RISK", "risk.read",
      "TEAM", "team.page.view",
      "CONFIG", "config.page.view",
      "ACCOUNT", "account.page.view",
      "ROLE", "role.page.view");
  private static final Set<String> STUDY_SCOPED_MODULES =
      Set.of("MILESTONE", "MONTHLY", "RISK", "TEAM");

  private final AuditLogRepository logs;
  private final UserAccountRepository users;
  private final TeamMatrixRepository teams;

  public AuditLogManager(
      AuditLogRepository logs,
      UserAccountRepository users,
      TeamMatrixRepository teams) {
    this.logs = logs;
    this.users = users;
    this.teams = teams;
  }

  public AuditLogRepository.AuditPage list(
      String moduleCode,
      String subjectType,
      Long subjectId,
      Long scopeStudyId,
      String groupType,
      Long groupId,
      String groupCode,
      String resultCode,
      int page,
      int pageSize,
      String username,
      Set<String> authorities) {
    String readPermission = MODULE_READ_PERMISSION.get(moduleCode);
    if (readPermission == null || !authorities.contains(readPermission)) {
      throw new BusinessException("AUDIT_FORBIDDEN", "没有对应业务模块的查看权限");
    }
    var user = users.findByUsername(username)
        .orElseThrow(() -> new BusinessException("AUDIT_FORBIDDEN", "当前账号不存在或已停用"));
    boolean restrictStudyScope =
        STUDY_SCOPED_MODULES.contains(moduleCode) && user.dataScope() != DataScope.ALL;
    if (restrictStudyScope && scopeStudyId != null
        && teams.findStudyTeam(StudyAccessScope.assignedTo(user.id()), scopeStudyId)
            .studies().isEmpty()) {
      throw new BusinessException("AUDIT_FORBIDDEN", "无权查看该 Study 的操作日志");
    }
    return logs.findPage(new AuditQuery(
        moduleCode, blankToNull(subjectType), subjectId, scopeStudyId,
        blankToNull(groupType), groupId, blankToNull(groupCode), blankToNull(resultCode),
        page, pageSize, restrictStudyScope, user.id()));
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
