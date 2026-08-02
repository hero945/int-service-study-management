package com.huadong.pipeline.service;

import com.huadong.pipeline.api.AuditLogApi;
import com.huadong.pipeline.domain.audit.AuditLogRepository.AuditLogRecord;
import com.huadong.pipeline.manager.AuditLogManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.springframework.stereotype.Service;

@Service
public class AuditLogApiService implements AuditLogApi {
  private static final Set<String> SENSITIVE_FIELDS = Set.of(
      "password", "passwordhash", "password_hash", "token", "tokenhash", "token_hash",
      "session", "csrf", "credential", "secret");
  private static final Map<String, String> FIELD_LABELS = Map.ofEntries(
      Map.entry("displayName", "显示名称"),
      Map.entry("email", "邮箱"),
      Map.entry("enabled", "启用状态"),
      Map.entry("roleCodes", "角色"),
      Map.entry("roleCode", "角色编码"),
      Map.entry("roleDescription", "角色描述"),
      Map.entry("permissionCodes", "权限"),
      Map.entry("dataScopeMode", "数据范围"),
      Map.entry("status", "状态"),
      Map.entry("statusCode", "状态"),
      Map.entry("studyCode", "Study 编号"),
      Map.entry("programCode", "Program 编号"),
      Map.entry("projectCode", "Project 编号"),
      Map.entry("productName", "产品名称"),
      Map.entry("moa", "作用机制"),
      Map.entry("indication", "适应症"),
      Map.entry("planV1Date", "计划 V1 日期"),
      Map.entry("planV2Date", "计划 V2 日期"),
      Map.entry("actualStartDate", "实际开始日期"),
      Map.entry("actualEndDate", "实际完成日期"),
      Map.entry("deviationNote", "偏差说明"),
      Map.entry("entryDate", "条目日期"),
      Map.entry("content", "内容"),
      Map.entry("functionLine", "功能线"),
      Map.entry("functionCode", "功能线编码"),
      Map.entry("functionName", "功能线"),
      Map.entry("updatedBy", "更新人"),
      Map.entry("description", "描述"),
      Map.entry("registeredDate", "登记日期"),
      Map.entry("ownerName", "责任人"),
      Map.entry("impact", "影响度"),
      Map.entry("probability", "发生可能性"),
      Map.entry("likelihood", "发生可能性"),
      Map.entry("detectability", "可探测性"),
      Map.entry("score", "评分"),
      Map.entry("level", "等级"),
      Map.entry("reason", "说明"),
      Map.entry("closeReason", "关闭/重开原因"),
      Map.entry("plannedDate", "计划日期"),
      Map.entry("completedDate", "完成日期"),
      Map.entry("completionNote", "完成说明"),
      Map.entry("members", "成员"),
      Map.entry("memberUserIds", "成员用户 ID"),
      Map.entry("assignments", "角色分配"),
      Map.entry("assessments", "风险评估"),
      Map.entry("actions", "风险措施"),
      Map.entry("deleted", "删除状态"));

  private final AuditLogManager manager;

  public AuditLogApiService(AuditLogManager manager) {
    this.manager = manager;
  }

  @Override
  public AuditLogPageResponse list(
      AuditLogQuery query, String username, Set<String> authorities) {
    var page = manager.list(
        query.moduleCode(), query.subjectType(), query.subjectId(), query.scopeStudyId(),
        query.groupType(), query.groupId(), query.groupCode(), query.resultCode(),
        query.page(), query.pageSize(), username, authorities);
    long totalPages = (page.totalItems() + page.pageSize() - 1) / page.pageSize();
    return new AuditLogPageResponse(
        page.data().stream().map(this::toResponse).toList(),
        page.page(), page.pageSize(), page.totalItems(), totalPages);
  }

  private AuditLogResponse toResponse(AuditLogRecord row) {
    Map<String, Object> before = sanitizeMap(row.beforeData());
    Map<String, Object> after = sanitizeMap(row.afterData());
    return new AuditLogResponse(
        row.id(), row.moduleCode(), row.subjectType(), row.subjectId(), row.subjectCode(),
        row.actionCode(), actionLabel(row.actionCode()), row.resultCode(), row.operationReason(),
        row.errorCode(), row.operatorUserId(), row.operatorEmail(), row.operatorDisplayName(),
        row.requestId(), row.ipAddress(), row.requestMethod(), row.requestPath(),
        row.targetTable(), row.targetId(), row.payloadVersion(),
        before == null && after == null, before, after,
        resolveChanges(row.actionCode(), before, after), row.occurredTime());
  }

  private static List<FieldChangeResponse> resolveChanges(
      String actionCode, Map<String, Object> before, Map<String, Object> after) {
    if ("TEAM_ROLE_ASSIGN".equals(actionCode)) {
      return teamAssignmentChanges(before, after);
    }
    return changes(before, after);
  }

  private static List<FieldChangeResponse> teamAssignmentChanges(
      Map<String, Object> before, Map<String, Object> after) {
    if (before != null && before.containsKey("memberUserIds")) {
      return legacyMemberChanges(before, after);
    }
    Map<String, String> roleNames = roleNameIndex(before, after);
    Map<String, Set<String>> beforeMembers = assignmentMembers(before);
    Map<String, Set<String>> afterMembers = assignmentMembers(after);
    var roleCodes = new TreeSet<String>();
    roleCodes.addAll(beforeMembers.keySet());
    roleCodes.addAll(afterMembers.keySet());
    var changes = new ArrayList<FieldChangeResponse>();
    for (String roleCode : roleCodes) {
      Set<String> beforeSet = beforeMembers.getOrDefault(roleCode, Set.of());
      Set<String> afterSet = afterMembers.getOrDefault(roleCode, Set.of());
      if (beforeSet.equals(afterSet)) {
        continue;
      }
      String roleLabel = formatRoleLabel(roleCode, roleNames.get(roleCode));
      Set<String> added = new LinkedHashSet<>(afterSet);
      added.removeAll(beforeSet);
      Set<String> removed = new LinkedHashSet<>(beforeSet);
      removed.removeAll(afterSet);
      if (!added.isEmpty()) {
        changes.add(new FieldChangeResponse(
            "assignments." + roleCode + ".added",
            "角色 " + roleLabel + " 增加了成员 " + String.join("、", added),
            memberSummary(beforeSet),
            memberSummary(afterSet)));
      }
      if (!removed.isEmpty()) {
        changes.add(new FieldChangeResponse(
            "assignments." + roleCode + ".removed",
            "角色 " + roleLabel + " 移除了成员 " + String.join("、", removed),
            memberSummary(beforeSet),
            memberSummary(afterSet)));
      }
    }
    return changes;
  }

  private static List<FieldChangeResponse> legacyMemberChanges(
      Map<String, Object> before, Map<String, Object> after) {
    return List.of(new FieldChangeResponse(
        "memberUserIds",
        "成员用户 ID",
        stringifyIds(before == null ? null : before.get("memberUserIds")),
        stringifyIds(after == null ? null : after.get("memberUserIds"))));
  }

  private static Map<String, String> roleNameIndex(
      Map<String, Object> before, Map<String, Object> after) {
    var names = new HashMap<String, String>();
    indexRoleNames(names, before);
    indexRoleNames(names, after);
    return names;
  }

  @SuppressWarnings("unchecked")
  private static void indexRoleNames(Map<String, String> names, Map<String, Object> snapshot) {
    if (snapshot == null) {
      return;
    }
    Object roles = snapshot.get("roles");
    if (!(roles instanceof List<?> roleList)) {
      return;
    }
    for (Object item : roleList) {
      if (!(item instanceof Map<?, ?> role)) {
        continue;
      }
      Object code = role.get("roleCode");
      Object name = role.get("roleName");
      if (code != null) {
        names.put(String.valueOf(code), name == null ? null : String.valueOf(name));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Set<String>> assignmentMembers(Map<String, Object> snapshot) {
    var membersByRole = new HashMap<String, Set<String>>();
    if (snapshot == null) {
      return membersByRole;
    }
    Object assignments = snapshot.get("assignments");
    if (!(assignments instanceof List<?> assignmentList)) {
      return membersByRole;
    }
    for (Object item : assignmentList) {
      if (!(item instanceof Map<?, ?> assignment)) {
        continue;
      }
      Object roleCode = assignment.get("roleCode");
      if (roleCode == null) {
        continue;
      }
      String code = String.valueOf(roleCode);
      Set<String> members = membersByRole.computeIfAbsent(code, key -> new LinkedHashSet<>());
      Object memberItems = assignment.get("members");
      if (!(memberItems instanceof List<?> memberList)) {
        continue;
      }
      for (Object memberItem : memberList) {
        if (!(memberItem instanceof Map<?, ?> member)) {
          continue;
        }
        Object displayName = member.get("displayName");
        if (displayName != null && !String.valueOf(displayName).isBlank()) {
          members.add(String.valueOf(displayName).trim());
        }
      }
    }
    return membersByRole;
  }

  private static String formatRoleLabel(String roleCode, String roleName) {
    if (roleName == null || roleName.isBlank()) {
      return roleCode;
    }
    return "%s（%s）".formatted(roleCode, roleName);
  }

  private static String memberSummary(Set<String> members) {
    if (members == null || members.isEmpty()) {
      return "—";
    }
    return String.join("、", members);
  }

  private static String stringifyIds(Object value) {
    if (value == null) {
      return "—";
    }
    if (value instanceof Collection<?> collection) {
      return collection.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining("、"));
    }
    return String.valueOf(value);
  }

  private static List<FieldChangeResponse> changes(
      Map<String, Object> before, Map<String, Object> after) {
    var changes = new ArrayList<FieldChangeResponse>();
    collectChanges("", before, after, changes);
    return changes;
  }

  private static void collectChanges(
      String prefix,
      Map<String, Object> before,
      Map<String, Object> after,
      List<FieldChangeResponse> changes) {
    var names = new TreeSet<String>();
    if (before != null) {
      names.addAll(before.keySet());
    }
    if (after != null) {
      names.addAll(after.keySet());
    }
    for (String name : names) {
      Object oldValue = before == null ? null : before.get(name);
      Object newValue = after == null ? null : after.get(name);
      String path = prefix.isEmpty() ? name : prefix + "." + name;
      if (oldValue instanceof Map<?, ?> || newValue instanceof Map<?, ?>) {
        collectChanges(path, asMap(oldValue), asMap(newValue), changes);
        continue;
      }
      if (!Objects.equals(normalize(oldValue), normalize(newValue))) {
        changes.add(new FieldChangeResponse(
            path, FIELD_LABELS.getOrDefault(name, name), oldValue, newValue));
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMap(Object value) {
    return value instanceof Map<?, ?> ? (Map<String, Object>) value : null;
  }

  private static Object normalize(Object value) {
    if (value instanceof Collection<?> collection) {
      return collection.stream().map(String::valueOf).sorted().toList();
    }
    return value;
  }

  private static Map<String, Object> sanitizeMap(Map<String, Object> source) {
    if (source == null) {
      return null;
    }
    var result = new LinkedHashMap<String, Object>();
    source.forEach((key, value) -> {
      if (!isSensitive(key)) {
        result.put(key, sanitizeValue(value));
      }
    });
    return result;
  }

  private static Object sanitizeValue(Object value) {
    if (value instanceof Map<?, ?> map) {
      var sanitized = new LinkedHashMap<String, Object>();
      map.forEach((key, nestedValue) -> {
        String name = String.valueOf(key);
        if (!isSensitive(name)) {
          sanitized.put(name, sanitizeValue(nestedValue));
        }
      });
      return sanitized;
    }
    if (value instanceof Collection<?> collection) {
      return collection.stream().map(AuditLogApiService::sanitizeValue).toList();
    }
    return value;
  }

  private static boolean isSensitive(String name) {
    String normalized = name.replace("-", "").replace("_", "").toLowerCase();
    return SENSITIVE_FIELDS.stream()
        .map(field -> field.replace("_", ""))
        .anyMatch(normalized::contains);
  }

  private static String actionLabel(String actionCode) {
    if (actionCode == null) {
      return "未知操作";
    }
    String known = switch (actionCode) {
      case "monthly_save" -> "保存月报条目";
      case "monthly_delete" -> "删除月报条目";
      case "RISK_ASSESS" -> "风险评估";
      case "TEAM_ROLE_ASSIGN" -> "分配 Study 角色";
      default -> null;
    };
    if (known != null) {
      return known;
    }
    if (actionCode.endsWith("_CREATE")) {
      return "新增";
    }
    if (actionCode.endsWith("_UPDATE")) {
      return "修改";
    }
    if (actionCode.endsWith("_DELETE")) {
      return "删除";
    }
    if (actionCode.contains("ASSIGN")) {
      return "分配";
    }
    if (actionCode.contains("PASSWORD")) {
      return "密码操作";
    }
    return actionCode;
  }
}
