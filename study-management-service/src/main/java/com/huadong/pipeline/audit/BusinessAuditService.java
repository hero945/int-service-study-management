package com.huadong.pipeline.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huadong.pipeline.domain.audit.AuditContext;
import com.huadong.pipeline.domain.audit.AuditEvent;
import com.huadong.pipeline.manager.AuditCommandManager;
import com.huadong.pipeline.manager.UserManager;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class BusinessAuditService {
  private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE =
      new TypeReference<>() {};
  private static final Set<String> SENSITIVE = Set.of(
      "password", "passwordhash", "token", "session", "csrf", "credential", "secret");

  private final AuditCommandManager commands;
  private final UserManager users;
  private final ObjectMapper mapper;

  public BusinessAuditService(
      AuditCommandManager commands, UserManager users, ObjectMapper mapper) {
    this.commands = commands;
    this.users = users;
    this.mapper = mapper;
  }

  public void success(
      String moduleCode,
      String subjectType,
      Long subjectId,
      String subjectCode,
      Long scopeStudyId,
      String actionCode,
      String targetTable,
      Long targetId,
      Object before,
      Object after,
      String reason,
      String operatorEmail) {
    Map<String, Object> beforeData = snapshot(before);
    Map<String, Object> afterData = snapshot(after);
    if (beforeData != null && Objects.equals(comparable(beforeData), comparable(afterData))) {
      return;
    }
    commands.record(event(
        moduleCode, subjectType, subjectId, subjectCode, scopeStudyId, actionCode,
        targetTable, targetId, null, null, null,
        "SUCCESS", reason, null, beforeData, afterData, operatorEmail));
  }

  public void successGrouped(
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
      Long targetId,
      Object before,
      Object after,
      String reason,
      String operatorEmail) {
    Map<String, Object> beforeData = snapshot(before);
    Map<String, Object> afterData = snapshot(after);
    if (beforeData != null && Objects.equals(comparable(beforeData), comparable(afterData))) {
      return;
    }
    commands.record(event(
        moduleCode, subjectType, subjectId, subjectCode, scopeStudyId, actionCode,
        targetTable, targetId, groupType, groupId, groupCode,
        "SUCCESS", reason, null, beforeData, afterData, operatorEmail));
  }

  public void failed(
      String moduleCode,
      String subjectType,
      Long subjectId,
      String subjectCode,
      Long scopeStudyId,
      String actionCode,
      String targetTable,
      Long targetId,
      String resultCode,
      String reason,
      String errorCode,
      String operatorEmail) {
    commands.recordIndependent(event(
        moduleCode, subjectType, subjectId, subjectCode, scopeStudyId, actionCode,
        targetTable, targetId, null, null, null,
        resultCode, reason, errorCode, null, null, operatorEmail));
  }

  public void failedGrouped(
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
      Long targetId,
      String resultCode,
      String reason,
      String errorCode,
      String operatorEmail) {
    commands.recordIndependent(event(
        moduleCode, subjectType, subjectId, subjectCode, scopeStudyId, actionCode,
        targetTable, targetId, groupType, groupId, groupCode,
        resultCode, reason, errorCode, null, null, operatorEmail));
  }

  private AuditEvent event(
      String moduleCode,
      String subjectType,
      Long subjectId,
      String subjectCode,
      Long scopeStudyId,
      String actionCode,
      String targetTable,
      Long targetId,
      String groupType,
      Long groupId,
      String groupCode,
      String resultCode,
      String reason,
      String errorCode,
      Map<String, Object> before,
      Map<String, Object> after,
      String operatorEmail) {
    var account = operatorEmail == null ? null : users.findByUsername(operatorEmail).orElse(null);
    AuditRequestMetadata request = AuditRequestContext.current();
    var context = new AuditContext(
        account == null ? null : account.id(),
        operatorEmail == null ? "anonymous" : operatorEmail,
        account == null ? null : account.displayName(),
        request == null ? null : request.requestId(),
        request == null ? null : request.ipAddress(),
        request == null ? null : request.requestMethod(),
        request == null ? null : request.requestPath());
    return new AuditEvent(
        moduleCode, subjectType, subjectId, subjectCode, scopeStudyId,
        groupType, groupId, groupCode, actionCode,
        targetTable, targetId, resultCode, reason, errorCode, 1, before, after, context);
  }

  private Map<String, Object> snapshot(Object value) {
    if (value == null) {
      return null;
    }
    Map<String, Object> converted = mapper.convertValue(value, MAP_TYPE);
    return sanitize(converted);
  }

  private static Map<String, Object> sanitize(Map<String, Object> source) {
    var result = new LinkedHashMap<String, Object>();
    source.forEach((name, value) -> {
      if (!isSensitive(name)) {
        result.put(name, sanitizeValue(value));
      }
    });
    return result;
  }

  private static Object sanitizeValue(Object value) {
    if (value instanceof Map<?, ?> map) {
      var converted = new LinkedHashMap<String, Object>();
      map.forEach((name, nested) -> {
        if (!isSensitive(String.valueOf(name))) {
          converted.put(String.valueOf(name), sanitizeValue(nested));
        }
      });
      return converted;
    }
    if (value instanceof Collection<?> collection) {
      return collection.stream().map(BusinessAuditService::sanitizeValue).toList();
    }
    return value;
  }

  private static boolean isSensitive(String field) {
    String normalized = field.replace("_", "").replace("-", "").toLowerCase();
    return SENSITIVE.stream().anyMatch(normalized::contains);
  }

  private static Object comparable(Object value) {
    if (value instanceof Map<?, ?> map) {
      var result = new LinkedHashMap<String, Object>();
      map.forEach((key, nested) -> {
        String name = String.valueOf(key);
        if (!Set.of("updatedAt", "updatedBy", "version", "rowVersion").contains(name)) {
          result.put(name, comparable(nested));
        }
      });
      return result;
    }
    if (value instanceof Collection<?> collection) {
      return collection.stream().map(BusinessAuditService::comparable).toList();
    }
    return value;
  }
}
