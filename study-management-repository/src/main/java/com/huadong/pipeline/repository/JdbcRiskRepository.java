package com.huadong.pipeline.repository;


import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.risk.RiskLevel;
import com.huadong.pipeline.domain.risk.RiskRepository;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
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
    long total = jdbc.queryForObject(
        "SELECT COUNT(DISTINCT r.id) " + filtered, Long.class, args.toArray());
    String sort = switch (query.sortBy()) {
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
    List<RiskSummary> data = jdbc.query("""
        SELECT r.risk_code, r.study_id, r.study_code_snapshot,
          r.program_code_snapshot, r.project_code_snapshot,
          r.function_line_code_snapshot, r.function_line_name_snapshot,
          r.risk_description, r.owner_user_id, r.owner_name_snapshot,
          r.current_score, r.current_level_code, r.status_code,
          (SELECT COUNT(*) FROM hd_plt_risk_action a
             WHERE a.risk_id = r.id AND a.sys_deleted = 0) action_count,
          r.row_version, r.sys_update_time
        """ + filtered + " ORDER BY " + sort + " " + order + ", r.id DESC LIMIT ? OFFSET ?",
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
    StringBuilder sql = new StringBuilder("""
        SELECT r.risk_code, r.study_id, r.study_code_snapshot,
          r.program_code_snapshot, r.project_code_snapshot,
          r.function_line_code_snapshot, r.function_line_name_snapshot,
          r.risk_description, r.owner_user_id, r.owner_name_snapshot,
          r.current_score, r.current_level_code, r.status_code,
          (SELECT COUNT(*) FROM hd_plt_risk_action a
             WHERE a.risk_id = r.id AND a.sys_deleted = 0) action_count,
          r.row_version, r.sys_update_time
        FROM hd_plt_risk r
        WHERE r.sys_deleted = 0 AND r.status_code = 'OPEN'
          AND r.study_id IN (""");
    sql.append(placeholders).append(")");
    sql.append(scopeClause(scope, args, "r.study_id"));
    sql.append(" ORDER BY r.current_score DESC, r.risk_code");
    return jdbc.query(sql.toString(), (rs, row) -> summary(rs), args.toArray());
  }

  @Override
  public Optional<RiskDetail> findDetail(StudyAccessScope scope, String riskCode) {
    var args = new ArrayList<Object>();
    args.add(riskCode);
    String scopeSql = scopeClause(scope, args, "r.study_id");
    List<RiskDetail> results = jdbc.query("""
        SELECT r.id, r.risk_code, r.study_id, r.study_code_snapshot,
          r.program_code_snapshot, r.project_code_snapshot,
          r.function_line_code_snapshot, r.function_line_name_snapshot,
          r.risk_description, r.owner_user_id, r.owner_name_snapshot,
          r.current_score, r.current_level_code, r.status_code,
          (SELECT COUNT(*) FROM hd_plt_risk_action a
             WHERE a.risk_id = r.id AND a.sys_deleted = 0) action_count,
          r.row_version, r.sys_update_time, r.registered_date, r.close_reason
        FROM hd_plt_risk r WHERE r.risk_code = ? AND r.sys_deleted = 0
        """ + scopeSql, (rs, row) -> {
          long id = rs.getLong("id");
          return new RiskDetail(summary(rs), rs.getDate("registered_date").toLocalDate(),
              rs.getString("close_reason"), assessments(id), actions(id));
        }, args.toArray());
    return results.stream().findFirst();
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
    if (studyId == null) return new FormOptions(studies, functions, List.of());
    List<MemberOption> owners = jdbc.query("""
        SELECT DISTINCT u.id, u.email, u.display_name
        FROM hd_plt_team_assignment ta JOIN hd_plt_user u ON u.id = ta.user_id
        WHERE ta.study_id = ? AND ta.sys_deleted = 0 AND u.sys_deleted = 0
          AND u.status_code = 'ACTIVE' ORDER BY u.display_name, u.id
        """, (rs, row) -> new MemberOption(rs.getLong(1), rs.getString(2), rs.getString(3)),
        studyId);
    return new FormOptions(studies, functions, owners);
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
    for (CreateAction action : actions) insertAction(id, action, operator.username());
    audit(operator, "RISK_CREATE", "hd_plt_risk", id, code);
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
    if (assessment != null) {
      long assessmentId = insertAssessment(id, assessment, operator.username());
      jdbc.update("""
          UPDATE hd_plt_risk SET latest_assessment_id=?, current_score=?, current_level_code=?
          WHERE id=?
          """, assessmentId, assessment.score(), assessment.level().name(), id);
    }
    audit(operator, "RISK_UPDATE", "hd_plt_risk", id,
        present(data.statusReason()) ? data.statusReason() : riskCode);
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
    audit(operator, "RISK_DELETE", "hd_plt_risk", id, riskCode);
  }

  @Override
  public RiskDetail addAction(String riskCode, long expectedRiskVersion, CreateAction action,
                              StudyAccessScope scope, Operator operator) {
    long id = riskId(riskCode, scope);
    bumpRisk(id, expectedRiskVersion, operator.username());
    insertAction(id, action, operator.username());
    audit(operator, "RISK_ACTION_CREATE", "hd_plt_risk_action", id, riskCode);
    return findDetail(scope, riskCode).orElseThrow();
  }

  @Override
  public RiskDetail updateAction(String riskCode, long actionId, long expectedActionVersion,
                                 UpdateAction action, StudyAccessScope scope, Operator operator) {
    long riskId = riskId(riskCode, scope);
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
    audit(operator, "RISK_ACTION_UPDATE", "hd_plt_risk_action", actionId, riskCode);
    return findDetail(scope, riskCode).orElseThrow();
  }

  @Override
  public RiskDetail deleteAction(String riskCode, long actionId, long expectedActionVersion,
                                 StudyAccessScope scope, Operator operator) {
    long riskId = riskId(riskCode, scope);
    int changed = jdbc.update("""
        UPDATE hd_plt_risk_action SET sys_deleted=1, row_version=row_version+1,
          sys_update_by=?, sys_update_time=CURRENT_TIMESTAMP
        WHERE id=? AND risk_id=? AND row_version=? AND sys_deleted=0
        """, operator.username(), actionId, riskId, expectedActionVersion);
    if (changed == 0) throw conflict();
    jdbc.update("UPDATE hd_plt_risk SET row_version=row_version+1, sys_update_by=? WHERE id=?",
        operator.username(), riskId);
    audit(operator, "RISK_ACTION_DELETE", "hd_plt_risk_action", actionId, riskCode);
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
             OR LOWER(r.program_code_snapshot) LIKE LOWER(?))
          """);
      String like = "%" + query.trim() + "%";
      args.add(like); args.add(like); args.add(like); args.add(like);
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
    return new RiskSummary(rs.getString("risk_code"), rs.getLong("study_id"),
        rs.getString("study_code_snapshot"), rs.getString("program_code_snapshot"),
        rs.getString("project_code_snapshot"), rs.getString("function_line_code_snapshot"),
        rs.getString("function_line_name_snapshot"), rs.getString("risk_description"),
        rs.getLong("owner_user_id"), rs.getString("owner_name_snapshot"),
        rs.getInt("current_score"), RiskLevel.valueOf(rs.getString("current_level_code")),
        rs.getString("status_code"), rs.getInt("action_count"), rs.getLong("row_version"),
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
    return jdbc.query("""
        SELECT id, action_description, owner_user_id, owner_name_snapshot,
          planned_date, completed_date, status_code, completion_note, row_version
        FROM hd_plt_risk_action WHERE risk_id=? AND sys_deleted=0 ORDER BY id
        """, (rs, row) -> new ActionView(rs.getLong(1), rs.getString(2), rs.getLong(3),
            rs.getString(4), localDate(rs.getDate(5)), localDate(rs.getDate(6)),
            rs.getString(7), rs.getString(8), rs.getLong(9)), riskId);
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

  private void insertAction(long riskId, CreateAction action, String operator) {
    jdbc.update("""
        INSERT INTO hd_plt_risk_action(
          risk_id, action_description, owner_user_id, owner_email_snapshot,
          owner_name_snapshot, planned_date, completed_date, status_code,
          completion_note, sys_create_by, sys_update_by)
        VALUES (?,?,?,?,?,?,?,?,?,?,?)
        """, riskId, action.description(), action.owner().id(), action.owner().email(),
        action.owner().displayName(), date(action.plannedDate()), date(action.completedDate()),
        action.status(), action.completionNote(), operator, operator);
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

  private void audit(Operator operator, String action, String table, long id, String code) {
    jdbc.update("""
        INSERT INTO hd_plt_audit_log(
          operator_user_id, operator_email, action_code, target_table, target_id,
          operation_reason, result_code)
        VALUES (?,?,?,?,?,?,'SUCCESS')
        """, operator.id(), operator.username(), action, table, id, code);
  }

  private static BusinessException conflict() {
    return new BusinessException("RISK_VERSION_CONFLICT", "风险已被其他用户修改，请刷新后重试");
  }
  private static boolean present(String value) { return value != null && !value.isBlank(); }
  private static Date date(LocalDate value) { return value == null ? null : Date.valueOf(value); }
  private static LocalDate localDate(Date value) { return value == null ? null : value.toLocalDate(); }
  private record Filter(String sql, Object[] args) {}
}
