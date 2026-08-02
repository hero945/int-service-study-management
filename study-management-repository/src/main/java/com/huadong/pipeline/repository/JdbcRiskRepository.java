package com.huadong.pipeline.repository;


import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.risk.RiskLevel;
import com.huadong.pipeline.domain.risk.RiskRepository;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcRiskRepository implements RiskRepository {
  private static final String SUMMARY_SELECT = """
      SELECT r.id AS risk_id, r.risk_code, r.study_id, r.study_code_snapshot,
        r.program_code_snapshot, r.project_code_snapshot,
        r.function_line_code_snapshot, r.function_line_name_snapshot,
        r.risk_description, r.owner_user_id, r.owner_name_snapshot,
        r.current_score, r.current_level_code, r.status_code,
        (SELECT COUNT(*) FROM hd_plt_risk_action a
           WHERE a.risk_id = r.id AND a.sys_deleted = 0) action_count,
        (SELECT COUNT(*) FROM hd_plt_risk_action a
           WHERE a.risk_id = r.id AND a.sys_deleted = 0
             AND a.status_code IN ('OPEN','IN_PROGRESS')) open_action_count,
        (SELECT COUNT(*) FROM hd_plt_risk_action a
           WHERE a.risk_id = r.id AND a.sys_deleted = 0
             AND a.status_code IN ('OPEN','IN_PROGRESS')
             AND a.planned_date IS NOT NULL AND a.planned_date < CURRENT_DATE) overdue_action_count,
        (SELECT MIN(a.planned_date) FROM hd_plt_risk_action a
           WHERE a.risk_id = r.id AND a.sys_deleted = 0
             AND a.status_code IN ('OPEN','IN_PROGRESS')
             AND a.planned_date IS NOT NULL) next_planned_date,
        r.row_version, r.sys_update_time
      """;

  @Autowired
  private JdbcTemplate jdbc;

  @Override
  public RiskPage findPage(StudyAccessScope scope, RiskQuery query) {
    var base = baseFilter(scope, query.query(), query.functionCode());
    Stats stats = jdbc.queryForObject("""
        SELECT COUNT(DISTINCT r.id),
          COUNT(DISTINCT CASE WHEN r.status_code = 'OPEN' THEN r.id END),
          COUNT(DISTINCT CASE WHEN r.current_level_code = 'HIGH' THEN r.id END),
          COUNT(DISTINCT CASE WHEN r.current_level_code = 'MEDIUM' THEN r.id END)
        """ + base.sql(), (rs, row) -> new Stats(
            rs.getLong(1), rs.getLong(2), rs.getLong(3), rs.getLong(4)), base.args());

    StringBuilder filtered = new StringBuilder(base.sql());
    List<Object> args = new ArrayList<>();
    java.util.Collections.addAll(args, base.args());
    if (present(query.status())) {
      filtered.append(" AND r.status_code = ?");
      args.add(query.status());
    }
    if (present(query.level())) {
      filtered.append(" AND r.current_level_code = ?");
      args.add(query.level());
    }
    if (query.studyId() != null) {
      filtered.append(" AND r.study_id = ?");
      args.add(query.studyId());
    }
    if (query.ownerUserId() != null) {
      filtered.append(" AND r.owner_user_id = ?");
      args.add(query.ownerUserId());
    }
    if (Boolean.TRUE.equals(query.overdueOnly())) {
      filtered.append("""
           AND EXISTS (
             SELECT 1 FROM hd_plt_risk_action oa
             WHERE oa.risk_id = r.id AND oa.sys_deleted = 0
               AND oa.status_code IN ('OPEN','IN_PROGRESS')
               AND oa.planned_date IS NOT NULL AND oa.planned_date < CURRENT_DATE)
          """);
    }
    long total = jdbc.queryForObject(
        "SELECT COUNT(DISTINCT r.id) " + filtered, Long.class, args.toArray());
    String sort = switch (query.sortBy() == null ? "" : query.sortBy()) {
      case "riskCode" -> "r.risk_code";
      case "studyCode" -> "r.study_code_snapshot";
      case "score" -> "r.current_score";
      case "level" -> "r.current_level_code";
      case "registeredDate" -> "r.registered_date";
      default -> "r.sys_update_time";
    };
    String order = "asc".equalsIgnoreCase(query.sortOrder()) ? "ASC" : "DESC";
    args.add(query.pageSize());
    args.add((query.page() - 1) * query.pageSize());
    List<RiskSummary> data = jdbc.query(
        SUMMARY_SELECT + filtered + " ORDER BY " + sort + " " + order + ", r.id DESC LIMIT ? OFFSET ?",
        (rs, row) -> summary(rs), args.toArray());
    return new RiskPage(data, stats, query.page(), query.pageSize(), total);
  }

  @Override
  public List<RiskSummary> findOpenByStudyIds(StudyAccessScope scope, List<Long> studyIds) {
    if (studyIds == null || studyIds.isEmpty()) {
      return List.of();
    }
    List<Object> args = new ArrayList<>();
    StringJoiner placeholders = new StringJoiner(", ");
    for (Long studyId : studyIds) {
      placeholders.add("?");
      args.add(studyId);
    }
    StringBuilder sql = new StringBuilder(SUMMARY_SELECT);
    sql.append("""
        FROM hd_plt_risk r
        WHERE r.sys_deleted = 0 AND r.status_code = 'OPEN'
          AND r.study_id IN (""");
    sql.append(placeholders).append(")");
    sql.append(scopeClause(scope, args, "r.study_id"));
    sql.append(" ORDER BY r.current_score DESC, r.risk_code");
    return jdbc.query(sql.toString(), (rs, row) -> summary(rs), args.toArray());
  }

  @Override
  public Map<Long, Integer> countOpenByStudyIds(StudyAccessScope scope, List<Long> studyIds) {
    if (studyIds == null || studyIds.isEmpty()) {
      return Map.of();
    }
    List<Object> args = new ArrayList<>();
    StringJoiner placeholders = new StringJoiner(", ");
    for (Long studyId : studyIds) {
      placeholders.add("?");
      args.add(studyId);
    }
    StringBuilder sql = new StringBuilder("""
        SELECT r.study_id, COUNT(*) AS cnt
        FROM hd_plt_risk r
        WHERE r.sys_deleted = 0 AND r.status_code = 'OPEN'
          AND r.study_id IN (""");
    sql.append(placeholders).append(")");
    sql.append(scopeClause(scope, args, "r.study_id"));
    sql.append(" GROUP BY r.study_id");
    Map<Long, Integer> counts = new HashMap<>();
    jdbc.query(sql.toString(), rs -> {
      counts.put(rs.getLong("study_id"), rs.getInt("cnt"));
    }, args.toArray());
    return counts;
  }

  @Override
  public Optional<RiskDetail> findDetail(StudyAccessScope scope, String riskCode) {
    var args = new ArrayList<Object>();
    args.add(riskCode);
    String scopeSql = scopeClause(scope, args, "r.study_id");
    List<long[]> ids = new ArrayList<>();
    List<RiskDetail> headers = jdbc.query(SUMMARY_SELECT
        + ", r.id, r.registered_date, r.close_reason, r.closed_time"
        + " FROM hd_plt_risk r WHERE r.risk_code = ? AND r.sys_deleted = 0"
        + scopeSql, (rs, row) -> {
          ids.add(new long[]{rs.getLong("id")});
          Timestamp closed = rs.getTimestamp("closed_time");
          return new RiskDetail(summary(rs), rs.getDate("registered_date").toLocalDate(),
              nullToEmpty(rs.getString("close_reason")),
              closed == null ? null : closed.toInstant(),
              List.of(), List.of(), List.of());
        }, args.toArray());
    if (headers.isEmpty()) return Optional.empty();
    RiskDetail header = headers.get(0);
    long id = ids.get(0)[0];
    return Optional.of(new RiskDetail(header.risk(), header.registeredDate(), header.closeReason(),
        header.closedTime(), assessments(id), actions(id), activities(id)));
  }

  @Override
  public FormOptions findFormOptions(StudyAccessScope scope, Long studyId) {
    List<Object> studyArgs = new ArrayList<>();
    String studyScope = scopeClause(scope, studyArgs, "s.id");
    List<StudyOption> studies = jdbc.query("""
        SELECT s.id, s.study_code, s.program_code_snapshot, s.project_code_snapshot
        FROM hd_plt_study s WHERE s.sys_deleted = 0
        """ + studyScope + " ORDER BY s.study_code", (rs, row) -> new StudyOption(
            rs.getLong(1), rs.getString(2), rs.getString(3), rs.getString(4)),
        studyArgs.toArray());
    List<FunctionOption> functions;
    if (scope.allStudies()) {
      functions = jdbc.query("""
          SELECT id, function_code, function_name FROM hd_plt_function_line
          WHERE status_code = 'ACTIVE' AND sys_deleted = 0 ORDER BY sort_order, id
          """, (rs, row) -> new FunctionOption(rs.getLong(1), rs.getString(2), rs.getString(3)));
    } else if (studyId != null) {
      functions = jdbc.query("""
          SELECT DISTINCT fl.id, fl.function_code, fl.function_name
          FROM hd_plt_team_assignment ta
          JOIN hd_plt_function_line fl ON fl.id = ta.function_line_id
          WHERE ta.study_id = ? AND ta.user_id = ? AND ta.sys_deleted = 0
            AND fl.status_code = 'ACTIVE' AND fl.sys_deleted = 0
          ORDER BY fl.function_name
          """, (rs, row) -> new FunctionOption(rs.getLong(1), rs.getString(2), rs.getString(3)),
          studyId, scope.userId());
    } else {
      functions = jdbc.query("""
          SELECT DISTINCT fl.id, fl.function_code, fl.function_name
          FROM hd_plt_team_assignment ta
          JOIN hd_plt_function_line fl ON fl.id = ta.function_line_id
          WHERE ta.user_id = ? AND ta.sys_deleted = 0
            AND fl.status_code = 'ACTIVE' AND fl.sys_deleted = 0
          ORDER BY fl.function_name
          """, (rs, row) -> new FunctionOption(rs.getLong(1), rs.getString(2), rs.getString(3)),
          scope.userId());
    }
    Rule scoringRule = activeRule();
    if (studyId == null) return new FormOptions(studies, functions, List.of(), scoringRule);
    List<MemberOption> owners = jdbc.query("""
        SELECT DISTINCT u.id, u.email, u.display_name
        FROM hd_plt_team_assignment ta JOIN hd_plt_user u ON u.id = ta.user_id
        WHERE ta.study_id = ? AND ta.sys_deleted = 0 AND u.sys_deleted = 0
          AND u.status_code = 'ACTIVE' ORDER BY u.display_name, u.id
        """, (rs, row) -> new MemberOption(rs.getLong(1), rs.getString(2), rs.getString(3)),
        studyId);
    return new FormOptions(studies, functions, owners, scoringRule);
  }

  @Override
  public Optional<StudyContext> findStudy(StudyAccessScope scope, long studyId) {
    List<Object> args = new ArrayList<>();
    args.add(studyId);
    String scopeSql = scopeClause(scope, args, "s.id");
    return jdbc.query("""
        SELECT s.id, s.study_code, s.program_id, s.program_code_snapshot,
          s.project_id, s.project_code_snapshot FROM hd_plt_study s
        WHERE s.id = ? AND s.sys_deleted = 0
        """ + scopeSql, (rs, row) -> new StudyContext(
            rs.getLong(1), rs.getString(2), rs.getLong(3), rs.getString(4),
            rs.getLong(5), rs.getString(6)), args.toArray()).stream().findFirst();
  }

  @Override
  public Optional<MemberOption> findStudyMember(long studyId, long userId) {
    return jdbc.query("""
        SELECT DISTINCT u.id, u.email, u.display_name
        FROM hd_plt_team_assignment ta JOIN hd_plt_user u ON u.id = ta.user_id
        WHERE ta.study_id = ? AND ta.user_id = ? AND ta.sys_deleted = 0
          AND u.sys_deleted = 0 AND u.status_code = 'ACTIVE'
        """, (rs, row) -> new MemberOption(rs.getLong(1), rs.getString(2), rs.getString(3)),
        studyId, userId).stream().findFirst();
  }

  @Override
  public Optional<FunctionOption> findFunction(
      long studyId, long userId, boolean allStudies, long functionLineId) {
    String sql = """
        SELECT fl.id, fl.function_code, fl.function_name FROM hd_plt_function_line fl
        WHERE fl.id = ? AND fl.status_code = 'ACTIVE' AND fl.sys_deleted = 0
        """ + (allStudies ? "" : """
        AND EXISTS (SELECT 1 FROM hd_plt_team_assignment ta
          WHERE ta.study_id = ? AND ta.user_id = ? AND ta.function_line_id = fl.id
            AND ta.sys_deleted = 0)
        """);
    Object[] args = allStudies ? new Object[]{functionLineId}
        : new Object[]{functionLineId, studyId, userId};
    return jdbc.query(sql, (rs, row) -> new FunctionOption(
        rs.getLong(1), rs.getString(2), rs.getString(3)), args).stream().findFirst();
  }

  @Override
  public Rule activeRule() {
    return jdbc.query("""
        SELECT id, low_risk_max_score, medium_risk_max_score
        FROM hd_plt_risk_rule_version WHERE status_code = 'ACTIVE' AND sys_deleted = 0
          AND (effective_from IS NULL OR effective_from <= CURRENT_TIMESTAMP)
          AND (effective_to IS NULL OR effective_to > CURRENT_TIMESTAMP)
        ORDER BY version_no DESC LIMIT 1
        """, (rs, row) -> new Rule(rs.getLong(1), rs.getInt(2), rs.getInt(3)))
        .stream().findFirst().orElseThrow(() ->
            new BusinessException("RISK_RULE_MISSING", "当前没有生效的风险评分规则"));
  }

  @Override
  public RiskDetail create(CreateRisk data, Assessment assessment, List<CreateAction> actions,
                           Operator operator) {
    String temporaryCode = "TMP-" + java.util.UUID.randomUUID();
    var key = new GeneratedKeyHolder();
    jdbc.update(connection -> {
      PreparedStatement ps = connection.prepareStatement("""
          INSERT INTO hd_plt_risk(
            risk_code, study_id, study_code_snapshot, program_id, program_code_snapshot,
            project_id, project_code_snapshot, function_line_id,
            function_line_code_snapshot, function_line_name_snapshot,
            owner_user_id, owner_email_snapshot, owner_name_snapshot,
            risk_description, registered_date, status_code,
            current_score, current_level_code, sys_create_by, sys_update_by)
          VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,'OPEN',?,?,?,?)
          """, new String[]{"id"});
      int i = 1;
      ps.setString(i++, temporaryCode);
      ps.setLong(i++, data.study().id()); ps.setString(i++, data.study().studyCode());
      ps.setLong(i++, data.study().programId()); ps.setString(i++, data.study().programCode());
      ps.setLong(i++, data.study().projectId()); ps.setString(i++, data.study().projectCode());
      ps.setLong(i++, data.function().id()); ps.setString(i++, data.function().code());
      ps.setString(i++, data.function().name()); ps.setLong(i++, data.owner().id());
      ps.setString(i++, data.owner().email()); ps.setString(i++, data.owner().displayName());
      ps.setString(i++, data.description()); ps.setDate(i++, Date.valueOf(data.registeredDate()));
      ps.setInt(i++, assessment.score()); ps.setString(i++, assessment.level().name());
      ps.setString(i++, operator.username()); ps.setString(i, operator.username());
      return ps;
    }, key);
    long id = key.getKey().longValue();
    String code = "RSK-%d-%06d".formatted(LocalDate.now().getYear(), id);
    jdbc.update("UPDATE hd_plt_risk SET risk_code = ? WHERE id = ?", code, id);
    long assessmentId = insertAssessment(id, assessment, operator.username());
    jdbc.update("UPDATE hd_plt_risk SET latest_assessment_id = ? WHERE id = ?", assessmentId, id);
    for (CreateAction action : actions) {
      long actionId = insertAction(id, action, operator.username());
      insertActionHistory(actionId, id, "CREATE", null, action.status(),
          actionSnapshot(action), null, operator.username());
    }
    return findDetail(StudyAccessScope.all(), code).orElseThrow();
  }

  @Override
  public RiskDetail update(String riskCode, long expectedVersion, UpdateRisk data,
                           Assessment assessment, Operator operator) {
    long id = riskId(riskCode, StudyAccessScope.all());
    int changed = jdbc.update("""
        UPDATE hd_plt_risk SET study_id=?, study_code_snapshot=?, program_id=?,
          program_code_snapshot=?, project_id=?, project_code_snapshot=?,
          function_line_id=?, function_line_code_snapshot=?, function_line_name_snapshot=?,
          owner_user_id=?, owner_email_snapshot=?, owner_name_snapshot=?, risk_description=?,
          registered_date=?, closed_time=CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE closed_time END,
          close_reason=CASE WHEN ? THEN ? ELSE close_reason END, status_code=?,
          row_version=row_version+1, sys_update_by=?, sys_update_time=CURRENT_TIMESTAMP
        WHERE id=? AND row_version=? AND sys_deleted=0
        """, data.study().id(), data.study().studyCode(), data.study().programId(),
        data.study().programCode(), data.study().projectId(), data.study().projectCode(),
        data.function().id(), data.function().code(), data.function().name(),
        data.owner().id(), data.owner().email(), data.owner().displayName(),
        data.description(), Date.valueOf(data.registeredDate()), data.closing(), data.closing(),
        data.statusReason(), data.status(), operator.username(), id, expectedVersion);
    if (changed == 0) throw conflict();
    if (data.statusChanged()) {
      insertStatusHistory(id, data.fromStatus(), data.status(), data.statusReason(),
          operator.username());
    }
    if (assessment != null) {
      long assessmentId = insertAssessment(id, assessment, operator.username());
      jdbc.update("""
          UPDATE hd_plt_risk SET latest_assessment_id=?, current_score=?, current_level_code=?
          WHERE id=?
          """, assessmentId, assessment.score(), assessment.level().name(), id);
    }
    return findDetail(StudyAccessScope.all(), riskCode).orElseThrow();
  }

  @Override
  public void softDelete(String riskCode, long expectedVersion, StudyAccessScope scope,
                         Operator operator) {
    long id = riskId(riskCode, scope);
    int changed = jdbc.update("""
        UPDATE hd_plt_risk SET sys_deleted=1, row_version=row_version+1,
          sys_update_by=?, sys_update_time=CURRENT_TIMESTAMP
        WHERE id=? AND row_version=? AND sys_deleted=0
        """, operator.username(), id, expectedVersion);
    if (changed == 0) throw conflict();
    jdbc.update("UPDATE hd_plt_risk_action SET sys_deleted=1, sys_update_by=? WHERE risk_id=?",
        operator.username(), id);
  }

  @Override
  public RiskDetail addAction(String riskCode, long expectedRiskVersion, CreateAction action,
                              StudyAccessScope scope, Operator operator) {
    long id = riskId(riskCode, scope);
    bumpRisk(id, expectedRiskVersion, operator.username());
    long actionId = insertAction(id, action, operator.username());
    insertActionHistory(actionId, id, "CREATE", null, action.status(),
        actionSnapshot(action), null, operator.username());
    return findDetail(scope, riskCode).orElseThrow();
  }

  @Override
  public RiskDetail updateAction(String riskCode, long actionId, long expectedActionVersion,
                                 UpdateAction action, StudyAccessScope scope, Operator operator) {
    long riskId = riskId(riskCode, scope);
    String fromStatus = jdbc.query("""
        SELECT status_code FROM hd_plt_risk_action
        WHERE id=? AND risk_id=? AND sys_deleted=0
        """, (rs, row) -> rs.getString(1), actionId, riskId).stream().findFirst()
        .orElseThrow(() -> new BusinessException("RISK_ACTION_NOT_FOUND", "风险措施不存在"));
    int changed = jdbc.update("""
        UPDATE hd_plt_risk_action SET action_description=?, owner_user_id=?,
          owner_email_snapshot=?, owner_name_snapshot=?, planned_date=?, completed_date=?,
          status_code=?, completion_note=?, row_version=row_version+1,
          sys_update_by=?, sys_update_time=CURRENT_TIMESTAMP
        WHERE id=? AND risk_id=? AND row_version=? AND sys_deleted=0
        """, action.description(), action.owner().id(), action.owner().email(),
        action.owner().displayName(), date(action.plannedDate()), date(action.completedDate()),
        action.status(), action.completionNote(), operator.username(), actionId, riskId,
        expectedActionVersion);
    if (changed == 0) throw conflict();
    jdbc.update("UPDATE hd_plt_risk SET row_version=row_version+1, sys_update_by=? WHERE id=?",
        operator.username(), riskId);
    String changeType = action.reopen() ? "REOPEN" : "UPDATE";
    insertActionHistory(actionId, riskId, changeType, fromStatus, action.status(),
        updateSnapshot(action), action.changeReason(), operator.username());
    return findDetail(scope, riskCode).orElseThrow();
  }

  @Override
  public RiskDetail deleteAction(String riskCode, long actionId, long expectedActionVersion,
                                 StudyAccessScope scope, Operator operator) {
    long riskId = riskId(riskCode, scope);
    String fromStatus = jdbc.query("""
        SELECT status_code FROM hd_plt_risk_action
        WHERE id=? AND risk_id=? AND sys_deleted=0
        """, (rs, row) -> rs.getString(1), actionId, riskId).stream().findFirst()
        .orElseThrow(() -> new BusinessException("RISK_ACTION_NOT_FOUND", "风险措施不存在"));
    int changed = jdbc.update("""
        UPDATE hd_plt_risk_action SET sys_deleted=1, row_version=row_version+1,
          sys_update_by=?, sys_update_time=CURRENT_TIMESTAMP
        WHERE id=? AND risk_id=? AND row_version=? AND sys_deleted=0
        """, operator.username(), actionId, riskId, expectedActionVersion);
    if (changed == 0) throw conflict();
    jdbc.update("UPDATE hd_plt_risk SET row_version=row_version+1, sys_update_by=? WHERE id=?",
        operator.username(), riskId);
    insertActionHistory(actionId, riskId, "DELETE", fromStatus, fromStatus, null, null,
        operator.username());
    return findDetail(scope, riskCode).orElseThrow();
  }

  private Filter baseFilter(StudyAccessScope scope, String query, String functionCode) {
    StringBuilder sql = new StringBuilder("FROM hd_plt_risk r WHERE r.sys_deleted = 0");
    List<Object> args = new ArrayList<>();
    sql.append(scopeClause(scope, args, "r.study_id"));
    if (present(query)) {
      sql.append("""
           AND (LOWER(r.risk_code) LIKE LOWER(?) OR LOWER(r.risk_description) LIKE LOWER(?)
             OR LOWER(r.owner_name_snapshot) LIKE LOWER(?)
             OR LOWER(r.program_code_snapshot) LIKE LOWER(?)
             OR LOWER(r.study_code_snapshot) LIKE LOWER(?)
             OR LOWER(r.project_code_snapshot) LIKE LOWER(?))
          """);
      String like = "%" + query.trim() + "%";
      args.add(like); args.add(like); args.add(like); args.add(like); args.add(like); args.add(like);
    }
    if (present(functionCode)) {
      sql.append(" AND r.function_line_code_snapshot = ?"); args.add(functionCode);
    }
    return new Filter(sql.toString(), args.toArray());
  }

  private String scopeClause(StudyAccessScope scope, List<Object> args, String studyColumn) {
    if (scope.allStudies()) return "";
    args.add(scope.userId());
    return " AND EXISTS (SELECT 1 FROM hd_plt_team_assignment scope_ta WHERE scope_ta.study_id = "
        + studyColumn + " AND scope_ta.user_id = ? AND scope_ta.sys_deleted = 0)";
  }

  private RiskSummary summary(java.sql.ResultSet rs) throws java.sql.SQLException {
    Date next = rs.getDate("next_planned_date");
    return new RiskSummary(rs.getLong("risk_id"), rs.getString("risk_code"), rs.getLong("study_id"),
        rs.getString("study_code_snapshot"), rs.getString("program_code_snapshot"),
        rs.getString("project_code_snapshot"), rs.getString("function_line_code_snapshot"),
        rs.getString("function_line_name_snapshot"), rs.getString("risk_description"),
        rs.getLong("owner_user_id"), rs.getString("owner_name_snapshot"),
        rs.getInt("current_score"), RiskLevel.valueOf(rs.getString("current_level_code")),
        rs.getString("status_code"), rs.getInt("action_count"),
        rs.getInt("open_action_count"), rs.getInt("overdue_action_count"),
        next == null ? null : next.toLocalDate(),
        rs.getLong("row_version"),
        rs.getTimestamp("sys_update_time").toInstant());
  }

  private List<AssessmentView> assessments(long riskId) {
    return jdbc.query("""
        SELECT id, assessment_no, impact_score, likelihood_score, detectability_score,
          total_score, risk_level_code, assessment_reason, assessed_by, assessed_time
        FROM hd_plt_risk_assessment WHERE risk_id=? AND sys_deleted=0
        ORDER BY assessment_no DESC
        """, (rs, row) -> new AssessmentView(rs.getLong(1), rs.getInt(2), rs.getInt(3),
            rs.getInt(4), rs.getInt(5), rs.getInt(6), RiskLevel.valueOf(rs.getString(7)),
            rs.getString(8), rs.getString(9), rs.getTimestamp(10).toInstant()), riskId);
  }

  private List<ActionView> actions(long riskId) {
    LocalDate today = LocalDate.now();
    return jdbc.query("""
        SELECT id, action_description, owner_user_id, owner_name_snapshot,
          planned_date, completed_date, status_code, completion_note, row_version
        FROM hd_plt_risk_action WHERE risk_id=? AND sys_deleted=0 ORDER BY id
        """, (rs, row) -> {
          LocalDate planned = localDate(rs.getDate(5));
          String status = rs.getString(7);
          boolean overdue = planned != null && planned.isBefore(today)
              && ("OPEN".equals(status) || "IN_PROGRESS".equals(status));
          return new ActionView(rs.getLong(1), rs.getString(2), rs.getLong(3),
              rs.getString(4), planned, localDate(rs.getDate(6)),
              status, nullToEmpty(rs.getString(8)), rs.getLong(9), overdue);
        }, riskId);
  }

  private List<ActivityView> activities(long riskId) {
    List<ActivityView> items = new ArrayList<>();
    for (AssessmentView assessment : assessments(riskId)) {
      items.add(new ActivityView("ASSESSMENT",
          assessment.number() <= 1 ? "首次评估 · %d 分 · %s".formatted(
              assessment.score(), assessment.level().name())
              : "重新评估 · 第 %d 次 · %d 分 · %s".formatted(
                  assessment.number(), assessment.score(), assessment.level().name()),
          "影响 %d × 可能性 %d × 可探测性 %d = %d 分%s".formatted(
              assessment.impact(), assessment.likelihood(), assessment.detectability(),
              assessment.score(),
              present(assessment.reason()) ? " · " + assessment.reason() : " · 未填写评估原因"),
          assessment.assessedAt(), assessment.assessedBy()));
    }
    List<ActivityView> statusItems = jdbc.query("""
        SELECT from_status, to_status, reason, changed_by, changed_time
        FROM hd_plt_risk_status_history WHERE risk_id=? ORDER BY changed_time DESC, id DESC
        """, (rs, row) -> {
          Timestamp changed = rs.getTimestamp("changed_time");
          return new ActivityView("STATUS",
              "风险状态（%s → %s）".formatted(
                  statusLabel(rs.getString("from_status")),
                  statusLabel(rs.getString("to_status"))),
              nullToEmpty(rs.getString("reason")),
              changed == null ? Instant.EPOCH : changed.toInstant(),
              nullToEmpty(rs.getString("changed_by")));
        }, riskId);
    items.addAll(statusItems);
    List<ActivityView> actionItems = jdbc.query("""
        SELECT change_type, from_status, to_status, snapshot_json, reason, changed_by, changed_time
        FROM hd_plt_risk_action_history WHERE risk_id=? ORDER BY changed_time DESC, id DESC
        """, (rs, row) -> {
          String type = nullToEmpty(rs.getString("change_type"));
          String from = rs.getString("from_status");
          String to = rs.getString("to_status");
          String title = switch (type) {
            case "CREATE" -> "新增控制措施";
            case "DELETE" -> "删除控制措施";
            case "REOPEN" -> "重新打开措施（%s → %s）".formatted(
                statusLabel(from), statusLabel(to));
            default -> (from != null && from.equals(to))
                ? "更新措施内容"
                : "更新措施状态（%s → %s）".formatted(statusLabel(from), statusLabel(to));
          };
          String detail = actionHistoryDetail(
              rs.getString("snapshot_json"), rs.getString("reason"));
          Timestamp changed = rs.getTimestamp("changed_time");
          return new ActivityView("ACTION", title, detail,
              changed == null ? Instant.EPOCH : changed.toInstant(),
              nullToEmpty(rs.getString("changed_by")));
        }, riskId);
    items.addAll(actionItems);
    items.sort(Comparator.comparing(ActivityView::at).reversed());
    return items;
  }

  private static String actionHistoryDetail(String snapshot, String reason) {
    String formatted = formatActionSnapshot(snapshot);
    if (present(formatted)) {
      return formatted;
    }
    if (present(reason)) {
      return reason.trim();
    }
    return "";
  }

  private static String statusLabel(String status) {
    if (status == null || status.isBlank()) return "-";
    return switch (status) {
      case "OPEN" -> "未开始";
      case "IN_PROGRESS" -> "进行中";
      case "COMPLETED" -> "已完成";
      case "CANCELLED" -> "已取消";
      case "CLOSED" -> "已关闭";
      default -> status;
    };
  }

  private static String formatActionSnapshot(String snapshot) {
    if (!present(snapshot)) return "";
    String description = extractJson(snapshot, "description");
    String owner = extractJson(snapshot, "owner");
    String planned = extractJson(snapshot, "plannedDate");
    String status = extractJson(snapshot, "status");
    String note = extractJson(snapshot, "note");
    StringJoiner parts = new StringJoiner("；");
    if (present(description)) parts.add(description);
    if (present(owner)) parts.add("责任人 " + owner);
    if (present(planned)) parts.add("计划 " + planned);
    if (present(status)) parts.add(statusLabel(status));
    if (present(note)) parts.add("说明 " + note);
    String formatted = parts.toString();
    return formatted.isBlank() ? snapshot.trim() : formatted;
  }

  private static String extractJson(String json, String key) {
    String marker = "\"" + key + "\":\"";
    int start = json.indexOf(marker);
    if (start < 0) return "";
    start += marker.length();
    StringBuilder value = new StringBuilder();
    for (int i = start; i < json.length(); i++) {
      char ch = json.charAt(i);
      if (ch == '\\' && i + 1 < json.length()) {
        value.append(json.charAt(++i));
        continue;
      }
      if (ch == '"') break;
      value.append(ch);
    }
    return value.toString();
  }

  private long insertAssessment(long riskId, Assessment assessment, String operator) {
    Integer number = jdbc.queryForObject("""
        SELECT COALESCE(MAX(assessment_no),0)+1 FROM hd_plt_risk_assessment WHERE risk_id=?
        """, Integer.class, riskId);
    var key = new GeneratedKeyHolder();
    jdbc.update(connection -> {
      PreparedStatement ps = connection.prepareStatement("""
          INSERT INTO hd_plt_risk_assessment(
            risk_id, assessment_no, rule_version_id, impact_score, likelihood_score,
            detectability_score, total_score, risk_level_code, assessment_reason,
            assessed_by, sys_create_by, sys_update_by)
          VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
          """, new String[]{"id"});
      ps.setLong(1, riskId); ps.setInt(2, number); ps.setLong(3, assessment.ruleId());
      ps.setInt(4, assessment.impact()); ps.setInt(5, assessment.likelihood());
      ps.setInt(6, assessment.detectability()); ps.setInt(7, assessment.score());
      ps.setString(8, assessment.level().name()); ps.setString(9, assessment.reason());
      ps.setString(10, operator); ps.setString(11, operator); ps.setString(12, operator);
      return ps;
    }, key);
    return key.getKey().longValue();
  }

  private long insertAction(long riskId, CreateAction action, String operator) {
    var key = new GeneratedKeyHolder();
    jdbc.update(connection -> {
      PreparedStatement ps = connection.prepareStatement("""
          INSERT INTO hd_plt_risk_action(
            risk_id, action_description, owner_user_id, owner_email_snapshot,
            owner_name_snapshot, planned_date, completed_date, status_code,
            completion_note, sys_create_by, sys_update_by)
          VALUES (?,?,?,?,?,?,?,?,?,?,?)
          """, new String[]{"id"});
      ps.setLong(1, riskId);
      ps.setString(2, action.description());
      ps.setLong(3, action.owner().id());
      ps.setString(4, action.owner().email());
      ps.setString(5, action.owner().displayName());
      Date planned = date(action.plannedDate());
      if (planned == null) ps.setNull(6, java.sql.Types.DATE); else ps.setDate(6, planned);
      Date completed = date(action.completedDate());
      if (completed == null) ps.setNull(7, java.sql.Types.DATE); else ps.setDate(7, completed);
      ps.setString(8, action.status());
      ps.setString(9, action.completionNote());
      ps.setString(10, operator);
      ps.setString(11, operator);
      return ps;
    }, key);
    return key.getKey().longValue();
  }

  private void insertStatusHistory(long riskId, String fromStatus, String toStatus,
                                   String reason, String operator) {
    jdbc.update("""
        INSERT INTO hd_plt_risk_status_history(
          risk_id, from_status, to_status, reason, changed_by)
        VALUES (?,?,?,?,?)
        """, riskId, fromStatus, toStatus, reason, operator);
  }

  private void insertActionHistory(long actionId, long riskId, String changeType,
                                   String fromStatus, String toStatus, String snapshot,
                                   String reason, String operator) {
    jdbc.update("""
        INSERT INTO hd_plt_risk_action_history(
          action_id, risk_id, change_type, from_status, to_status,
          snapshot_json, reason, changed_by)
        VALUES (?,?,?,?,?,?,?,?)
        """, actionId, riskId, changeType, fromStatus, toStatus, snapshot, reason, operator);
  }

  private long riskId(String riskCode, StudyAccessScope scope) {
    List<Object> args = new ArrayList<>(); args.add(riskCode);
    String scopeSql = scopeClause(scope, args, "r.study_id");
    return jdbc.query("SELECT r.id FROM hd_plt_risk r WHERE r.risk_code=? AND r.sys_deleted=0"
        + scopeSql, (rs, row) -> rs.getLong(1), args.toArray()).stream().findFirst()
        .orElseThrow(() -> new BusinessException("RISK_NOT_FOUND", "风险不存在或不在当前数据范围"));
  }

  private void bumpRisk(long id, long version, String operator) {
    int changed = jdbc.update("""
        UPDATE hd_plt_risk SET row_version=row_version+1, sys_update_by=?,
          sys_update_time=CURRENT_TIMESTAMP WHERE id=? AND row_version=? AND sys_deleted=0
        """, operator, id, version);
    if (changed == 0) throw conflict();
  }

  private static String actionSnapshot(CreateAction action) {
    return actionSnapshot(action.description(), action.owner().displayName(),
        action.plannedDate(), action.status(), action.completionNote());
  }

  private static String updateSnapshot(UpdateAction action) {
    return actionSnapshot(action.description(), action.owner().displayName(),
        action.plannedDate(), action.status(), action.completionNote());
  }

  private static String actionSnapshot(String description, String owner, LocalDate plannedDate,
                                       String status, String completionNote) {
    StringJoiner parts = new StringJoiner(",");
    parts.add("\"description\":\"" + escape(description) + "\"");
    parts.add("\"owner\":\"" + escape(owner) + "\"");
    parts.add("\"plannedDate\":\"" + (plannedDate == null ? "" : plannedDate) + "\"");
    parts.add("\"status\":\"" + escape(status) + "\"");
    if (present(completionNote)) {
      parts.add("\"note\":\"" + escape(completionNote) + "\"");
    }
    return "{" + parts + "}";
  }

  private static String escape(String value) {
    return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static BusinessException conflict() {
    return new BusinessException("RISK_VERSION_CONFLICT", "风险已被其他用户修改，请刷新后重试");
  }
  private static boolean present(String value) { return value != null && !value.isBlank(); }
  private static String nullToEmpty(String value) { return value == null ? "" : value; }
  private static Date date(LocalDate value) { return value == null ? null : Date.valueOf(value); }
  private static LocalDate localDate(Date value) { return value == null ? null : value.toLocalDate(); }
  private record Filter(String sql, Object[] args) {}
}
