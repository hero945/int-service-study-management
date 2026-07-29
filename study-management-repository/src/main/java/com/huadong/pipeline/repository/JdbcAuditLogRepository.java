package com.huadong.pipeline.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huadong.pipeline.domain.audit.AuditEvent;
import com.huadong.pipeline.domain.audit.AuditLogRepository;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcAuditLogRepository implements AuditLogRepository {
  private static final TypeReference<LinkedHashMap<String, Object>> MAP_TYPE =
      new TypeReference<>() {};

  private final JdbcTemplate jdbc;
  private final ObjectMapper mapper;

  public JdbcAuditLogRepository(JdbcTemplate jdbc, ObjectMapper mapper) {
    this.jdbc = jdbc;
    this.mapper = mapper;
  }

  @Override
  public AuditPage findPage(AuditQuery query) {
    var where = new StringBuilder(" WHERE module_code = ?");
    var args = new ArrayList<Object>();
    args.add(query.moduleCode());
    if (query.subjectType() != null) {
      where.append(" AND subject_type = ?");
      args.add(query.subjectType());
    }
    if (query.subjectId() != null) {
      where.append(" AND subject_id = ?");
      args.add(query.subjectId());
    }
    if (query.scopeStudyId() != null) {
      where.append(" AND scope_study_id = ?");
      args.add(query.scopeStudyId());
    }
    if (query.groupType() != null) {
      where.append(" AND group_type = ?");
      args.add(query.groupType());
    }
    if (query.groupId() != null) {
      where.append(" AND group_id = ?");
      args.add(query.groupId());
    }
    if (query.groupCode() != null) {
      where.append(" AND group_code = ?");
      args.add(query.groupCode());
    }
    if (query.resultCode() != null) {
      where.append(" AND result_code = ?");
      args.add(query.resultCode());
    }
    if (query.restrictStudyScope()) {
      where.append("""
           AND (
             (scope_study_id IS NOT NULL AND EXISTS (
               SELECT 1 FROM hd_plt_team_assignment ta
               WHERE ta.study_id = scope_study_id AND ta.user_id = ? AND ta.sys_deleted = 0
             ))
             OR (scope_study_id IS NULL AND operator_user_id = ?)
           )
          """);
      args.add(query.userId());
      args.add(query.userId());
    }
    long total = jdbc.queryForObject(
        "SELECT COUNT(*) FROM hd_plt_audit_log" + where, Long.class, args.toArray());
    args.add(query.pageSize());
    args.add((query.page() - 1) * query.pageSize());
    var data = jdbc.query("""
        SELECT id, module_code, subject_type, subject_id, subject_code, scope_study_id,
               action_code, target_table, target_id, result_code, operation_reason, error_code,
               operator_user_id, operator_email, operator_display_name, request_id, ip_address,
               request_method, request_path, payload_version, before_data, after_data, occurred_time
        FROM hd_plt_audit_log
        """ + where + " ORDER BY occurred_time DESC, id DESC LIMIT ? OFFSET ?",
        this::mapRow, args.toArray());
    return new AuditPage(data, query.page(), query.pageSize(), total);
  }

  @Override
  public void insert(AuditEvent event) {
    var context = event.context();
    jdbc.update("""
        INSERT INTO hd_plt_audit_log(
          operator_user_id, operator_email, operator_display_name, action_code, module_code,
          subject_type, subject_id, subject_code, scope_study_id, target_table, target_id,
          group_type, group_id, group_code,
          request_id, ip_address, request_method, request_path, operation_reason, error_code,
          payload_version, before_data, after_data, result_code)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        context.operatorUserId(), context.operatorEmail(), context.operatorDisplayName(),
        event.actionCode(), event.moduleCode(), event.subjectType(), event.subjectId(),
        event.subjectCode(), event.scopeStudyId(), event.targetTable(), event.targetId(),
        event.groupType(), event.groupId(), event.groupCode(),
        context.requestId(), context.ipAddress(), context.requestMethod(), context.requestPath(),
        event.operationReason(), event.errorCode(), event.payloadVersion(),
        json(event.beforeData()), json(event.afterData()), event.resultCode());
  }

  private AuditLogRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
    return new AuditLogRecord(
        rs.getLong("id"), rs.getString("module_code"), rs.getString("subject_type"),
        nullableLong(rs, "subject_id"), rs.getString("subject_code"),
        nullableLong(rs, "scope_study_id"), rs.getString("action_code"),
        rs.getString("target_table"), nullableLong(rs, "target_id"), rs.getString("result_code"),
        rs.getString("operation_reason"), rs.getString("error_code"),
        nullableLong(rs, "operator_user_id"), rs.getString("operator_email"),
        rs.getString("operator_display_name"), rs.getString("request_id"),
        rs.getString("ip_address"), rs.getString("request_method"), rs.getString("request_path"),
        rs.getInt("payload_version"), parse(rs.getString("before_data")),
        parse(rs.getString("after_data")), rs.getTimestamp("occurred_time").toLocalDateTime());
  }

  private Map<String, Object> parse(String json) {
    if (json == null || json.isBlank()) {
      return null;
    }
    try {
      JsonNode root = mapper.readTree(json);
      // 兼容双重编码：字符串里再包了一层 JSON
      if (root.isTextual()) {
        root = mapper.readTree(root.textValue());
      }
      if (root.isObject()) {
        return mapper.convertValue(root, MAP_TYPE);
      }
      // 历史数据：旧团队矩阵审计把成员用户 ID 列表直接存成 JSON 数组（如 [1, 2, 3]），
      // 包一层对象避免整页查询失败，差异展示时按 memberUserIds 字段呈现
      var wrapped = new LinkedHashMap<String, Object>();
      wrapped.put("memberUserIds", mapper.convertValue(root, Object.class));
      return wrapped;
    } catch (JsonProcessingException error) {
      throw new IllegalStateException("Invalid audit snapshot JSON", error);
    }
  }

  private String json(Map<String, Object> value) {
    if (value == null) {
      return null;
    }
    try {
      return mapper.writeValueAsString(value);
    } catch (JsonProcessingException error) {
      throw new IllegalArgumentException("Audit snapshot cannot be serialized", error);
    }
  }

  private static Long nullableLong(ResultSet rs, String column) throws SQLException {
    long value = rs.getLong(column);
    return rs.wasNull() ? null : value;
  }
}
