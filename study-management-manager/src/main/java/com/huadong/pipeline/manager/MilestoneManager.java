package com.huadong.pipeline.manager;


import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.common.StudyStatus;
import com.huadong.pipeline.domain.milestone.MilestoneDefinition;
import com.huadong.pipeline.domain.milestone.MilestoneDefinition.MilestoneNode;
import com.huadong.pipeline.domain.milestone.MilestoneDefinition.StageGroup;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.MilestoneSaveCommand;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.MilestoneUpdateCommand;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.PersistedMilestone;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.StudyRef;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.domain.user.UserAccount;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MilestoneManager {

  @Autowired
  private StudyMilestonePort milestones;
  @Autowired
  private UserAccountRepository users;

  // ──────────── query ────────────

  public MilestoneResult getMilestones(long studyId, String username) {
    UserAccount user = currentUser(username);
    if (!user.permissions().contains("milestone.read")) {
      throw forbiddenRead();
    }
    return loadMilestones(studyId, user);
  }

  private MilestoneResult loadMilestones(long studyId, UserAccount user) {
    requireStudyExists(studyId);
    StudyRef study = requireStudyInScope(studyId, user);
    List<PersistedMilestone> rows = milestones.findByStudyId(studyId);
    Map<String, PersistedMilestone> byCode = new LinkedHashMap<>();
    for (PersistedMilestone row : rows) {
      byCode.put(row.milestoneCode(), row);
    }
    // Build full ordered list: definitions × persisted data
    List<MilestoneNodeState> nodes = new ArrayList<>();
    for (StageGroup group : MilestoneDefinition.ALL) {
      for (MilestoneNode node : group.nodes()) {
        PersistedMilestone persisted = byCode.get(node.code());
        nodes.add(new MilestoneNodeState(
            persisted == null ? null : persisted.id(),
            group.code(), group.label(), group.sortOrder(),
            node.code(), node.label(), node.sortOrder(),
            persisted == null ? null : persisted.planV1Date(),
            persisted == null ? null : persisted.planV2Date(),
            persisted == null ? null : persisted.actualStartDate(),
            persisted == null ? null : persisted.actualEndDate(),
            persisted == null ? null : persisted.deviationNote()));
      }
    }
    // Derive statuses (must be done after all nodes are assembled for sequential logic)
    deriveStatuses(nodes);
    return new MilestoneResult(study.studyCode(), nodes);
  }

  // ──────────── mutation ────────────

  @Transactional
  public MilestoneResult updateMilestone(long studyId, String milestoneCode,
                                         MilestoneUpdateCommand input, String username) {
    UserAccount user = currentUser(username);
    if (!user.permissions().contains("milestone.update")) {
      throw forbidden();
    }
    requireStudyExists(studyId);
    requireStudyInScope(studyId, user);
    // Resolve stage code and node index from milestone code
    String stageCode = stagePart(milestoneCode);
    int nodeIndex = nodeIndex(milestoneCode);
    MilestoneDefinition.node(stageCode, nodeIndex); // validates existence

    // Validation: actualEnd >= actualStart
    if (input.actualStartDate() != null && input.actualEndDate() != null
        && input.actualEndDate().isBefore(input.actualStartDate())) {
      throw invalid("实际结束日期不能早于实际开始日期");
    }

    // Sequential validation: for completed nodes, ensure subsequent nodes
    // don't have actual dates earlier than the last completed node's actual end
    List<PersistedMilestone> rows = milestones.findByStudyId(studyId);
    LocalDate lastEnd = null;
    for (PersistedMilestone row : rows) {
      if (row.milestoneCode().equals(milestoneCode)) break;
      if (row.actualEndDate() != null) lastEnd = row.actualEndDate();
    }
    if (lastEnd != null && input.actualStartDate() != null
        && input.actualStartDate().isBefore(lastEnd)) {
      throw invalid("该节点实际开始日期不能早于前序已完成节点的实际结束日期 " + lastEnd);
    }
    if (lastEnd != null && input.actualEndDate() != null
        && input.actualEndDate().isBefore(lastEnd)) {
      throw invalid("该节点实际结束日期不能早于前序已完成节点的实际结束日期 " + lastEnd);
    }

    milestones.save(new MilestoneSaveCommand(
        studyId, stageCode, milestoneCode,
        input.planV1Date(), input.planV2Date(),
        input.actualStartDate(), input.actualEndDate(),
        input.deviationNote(), user.username()));

    return loadMilestones(studyId, user);
  }

  // ──────────── stage projection (Section 7.2) ────────────

  public StageProjectionResult getStageProjection(long studyId, String username) {
    MilestoneResult result = getMilestones(studyId, username);
    List<MilestoneNodeState> nodes = result.nodes();
    // Walk through nodes in order to find the active one
    for (MilestoneNodeState node : nodes) {
      if (node.actualStartDate() == null && node.actualEndDate() == null) {
        // First node with neither start nor end → current stage starts here
        return new StageProjectionResult(
            node.stageCode(), node.stageLabel(),
            node.milestoneCode(), node.milestoneLabel(),
            "进行中");
      }
      if (node.actualStartDate() != null && node.actualEndDate() == null) {
        // This node is in progress
        return new StageProjectionResult(
            node.stageCode(), node.stageLabel(),
            node.milestoneCode(), node.milestoneLabel(),
            "进行中");
      }
      // actualEnd exists → node is completed, continue to next
    }
    // All nodes completed → "已完成"
    return new StageProjectionResult("", "", "", "", "已完成");
  }

  // ──────────── status derivation (Section 7.2) ────────────

  private static void deriveStatuses(List<MilestoneNodeState> nodes) {
    // Group nodes by stage
    Map<String, List<Integer>> stageIndices = new LinkedHashMap<>();
    for (int i = 0; i < nodes.size(); i++) {
      stageIndices.computeIfAbsent(nodes.get(i).stageCode(), k -> new ArrayList<>()).add(i);
    }
    // Derive per-node status within each stage group
    for (List<Integer> indices : stageIndices.values()) {
      boolean stageStarted = false;
      boolean stageEnded = false;
      for (int i = 0; i < indices.size(); i++) {
        int idx = indices.get(i);
        MilestoneNodeState node = nodes.get(idx);
        boolean isLast = i == indices.size() - 1;

        String status;
        if (node.actualStartDate() == null && node.actualEndDate() == null) {
          // If the stage has already "ended" (previous node completed), and this
          // is the first node of the next logical flow, it could be "未开始"
          status = "NOT_STARTED";
        } else if (node.actualStartDate() != null && node.actualEndDate() == null) {
          status = "IN_PROGRESS";
          stageStarted = true;
        } else if (node.actualEndDate() != null) {
          status = isLast ? "COMPLETED" : "COMPLETED";
          if (isLast) stageEnded = true;
        } else {
          status = "NOT_STARTED";
        }
        nodes.set(idx, node.withStatus(status));
      }
    }
  }

  // ──────────── overview status (Section: pipeline overview) ────────────

  /**
   * Derive a study's pipeline-overview status from its milestones.
   * 主状态 = current stage (frontier node); 子状态 = node reached within that stage.
   * The three booleans describe per-study milestone completion:
   *   - preindCompleted:  PreIND stage's last node has actual_end_date != null
   *   - indCompleted:     IND stage's last node has actual_end_date != null
   *   - globallyCompleted: study's globally-last milestone node has actual_end_date != null
   *
   * NOTE: the frontend cell rendering no longer keys off these booleans per column.
   * It uses a phase-relative rule against the project's furthest phase
   * (see pipeline-aggregation.getProjectCell): earlier columns → "已完成",
   * the current column → this study's milestone sub-status (主状态作为副文本),
   * later columns → "—". These booleans remain available for per-study detail
   * (e.g. the current column forces "已完成" when currentPhaseCompleted).
   *
   * @return null when the study has no milestone rows (caller falls back to date-based status).
   */
  public MilestoneOverviewStatus computeOverviewStatus(List<PersistedMilestone> rows) {
    if (rows == null || rows.isEmpty()) {
      return null;
    }
    List<MilestoneNodeState> nodes = buildNodeStates(rows);

    // Frontier = last node (definition order) that has any actual date.
    MilestoneNodeState frontier = null;
    for (MilestoneNodeState node : nodes) {
      if (node.actualStartDate() != null || node.actualEndDate() != null) {
        frontier = node;
      }
    }

    String currentStageCode;
    String currentStageLabel;
    if (frontier != null) {
      currentStageCode = frontier.stageCode();
      currentStageLabel = frontier.stageLabel();
    } else {
      PersistedMilestone first = rows.stream().min(Comparator.comparingInt(this::position)).orElse(null);
      if (first != null) {
        currentStageCode = stagePart(first.milestoneCode());
        currentStageLabel = stageLabel(first.milestoneCode());
      } else {
        currentStageCode = MilestoneDefinition.ALL.get(0).code();
        currentStageLabel = MilestoneDefinition.ALL.get(0).label();
      }
    }

    String subStatus = frontier == null ? "NOT_STARTED" : frontier.status();
    String subStatusLabel = frontier == null ? "未开始" : frontier.milestoneLabel();

    // Per-stage completion: check if the LAST node of each target stage has actual_end.
    // Stage codes must match MilestoneDefinition (PreIND / IND), not Study.phase_status_code.
    boolean preindCompleted = isStageLastNodeCompleted(nodes, "PreIND");
    boolean indCompleted = isStageLastNodeCompleted(nodes, "IND");

    // Global completion: the absolute last milestone node in definition order has actual_end
    LocalDate lastMilestoneActualEnd = rows.stream()
        .max(Comparator.comparingInt(this::position))
        .map(PersistedMilestone::actualEndDate)
        .orElse(null);
    boolean globallyCompleted = lastMilestoneActualEnd != null;

    // Current-phase completion (per product owner rule):
    // the frontier node is the LAST node of its stage AND has actual_end set,
    // i.e. the study has finished the milestone stage that represents its current phase.
    boolean currentPhaseCompleted = false;
    if (frontier != null && frontier.actualEndDate() != null) {
      final MilestoneNodeState f = frontier;
      int maxOrderInStage = nodes.stream()
          .filter(n -> n.stageCode().equals(f.stageCode()))
          .mapToInt(MilestoneNodeState::nodeOrder)
          .max().orElse(-1);
      currentPhaseCompleted = f.nodeOrder() == maxOrderInStage;
    }

    // Study-level status: COMPLETED only when globally complete
    StudyStatus status = globallyCompleted ? StudyStatus.COMPLETED : mapStatus(subStatus);

    return new MilestoneOverviewStatus(
        status, status.label(), status.tone(),
        currentStageCode, currentStageLabel, subStatusLabel,
        preindCompleted, indCompleted, globallyCompleted, currentPhaseCompleted);
  }

  /** Check whether the last node of the given stage code has actual_end_date != null. */
  private boolean isStageLastNodeCompleted(List<MilestoneNodeState> nodes, String stageCode) {
    int lastIndex = -1;
    LocalDate lastActualEnd = null;
    for (int i = 0; i < nodes.size(); i++) {
      MilestoneNodeState node = nodes.get(i);
      if (node.stageCode().equals(stageCode)) {
        lastIndex = i;
        lastActualEnd = node.actualEndDate();
      }
    }
    return lastIndex >= 0 && lastActualEnd != null;
  }

  /** Build the full 60-node ordered state list, merging persisted rows by milestone code. */
  private List<MilestoneNodeState> buildNodeStates(List<PersistedMilestone> rows) {
    Map<String, PersistedMilestone> byCode = new LinkedHashMap<>();
    for (PersistedMilestone row : rows) {
      byCode.put(row.milestoneCode(), row);
    }
    List<MilestoneNodeState> nodes = new ArrayList<>();
    for (StageGroup group : MilestoneDefinition.ALL) {
      for (MilestoneNode node : group.nodes()) {
        PersistedMilestone persisted = byCode.get(node.code());
        nodes.add(new MilestoneNodeState(
            persisted == null ? null : persisted.id(),
            group.code(), group.label(), group.sortOrder(),
            node.code(), node.label(), node.sortOrder(),
            persisted == null ? null : persisted.planV1Date(),
            persisted == null ? null : persisted.planV2Date(),
            persisted == null ? null : persisted.actualStartDate(),
            persisted == null ? null : persisted.actualEndDate(),
            persisted == null ? null : persisted.deviationNote()));
      }
    }
    deriveStatuses(nodes);
    return nodes;
  }

  private static String stageLabel(String milestoneCode) {
    String stageCode = stagePart(milestoneCode);
    return MilestoneDefinition.ALL.stream()
        .filter(g -> g.code().equals(stageCode))
        .findFirst()
        .map(StageGroup::label)
        .orElse(stageCode);
  }

  /** Monotonic key for a persisted milestone in MilestoneDefinition order. */
  private int position(PersistedMilestone m) {
    String stageCode = stagePart(m.milestoneCode());
    int nodeIndex = nodeIndex(m.milestoneCode());
    int stageOrder = MilestoneDefinition.ALL.stream()
        .filter(g -> g.code().equals(stageCode))
        .findFirst()
        .map(StageGroup::sortOrder)
        .orElse(Integer.MAX_VALUE);
    return stageOrder * 1000 + nodeIndex;
  }

  private static StudyStatus mapStatus(String milestoneStatus) {
    return switch (milestoneStatus) {
      case "COMPLETED" -> StudyStatus.COMPLETED;
      case "IN_PROGRESS" -> StudyStatus.ACTIVE;
      default -> StudyStatus.PLANNED;
    };
  }

  // ──────────── helpers ────────────

  private StudyRef requireStudyExists(long studyId) {
    return milestones.findStudy(studyId)
        .orElseThrow(MilestoneManager::notFound);
  }

  private StudyRef requireStudyInScope(long studyId, UserAccount user) {
    return milestones.findStudy(scope(user), studyId)
        .orElseThrow(MilestoneManager::outOfScope);
  }

  private UserAccount currentUser(String username) {
    return users.findByUsername(username)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "当前登录账号不存在"));
  }

  private static StudyAccessScope scope(UserAccount user) {
    return user.dataScope() == DataScope.ALL ? StudyAccessScope.all()
        : StudyAccessScope.assignedTo(user.id());
  }

  private static BusinessException notFound() {
    return new BusinessException("STUDY_NOT_FOUND", "目标Study不存在");
  }

  private static BusinessException outOfScope() {
    return new BusinessException("STUDY_OUT_OF_SCOPE", "目标Study不存在或不在当前数据范围");
  }

  private static String stagePart(String milestoneCode) {
    int dash = milestoneCode.lastIndexOf('-');
    if (dash < 0) throw invalid("里程碑编码格式错误: " + milestoneCode);
    return milestoneCode.substring(0, dash);
  }

  private static int nodeIndex(String milestoneCode) {
    int dash = milestoneCode.lastIndexOf('-');
    if (dash < 0) throw invalid("里程碑编码格式错误: " + milestoneCode);
    try {
      return Integer.parseInt(milestoneCode.substring(dash + 1));
    } catch (NumberFormatException e) {
      throw invalid("里程碑编码格式错误: " + milestoneCode);
    }
  }

  private static BusinessException invalid(String message) {
    return new BusinessException("INVALID_MILESTONE", message);
  }

  private static BusinessException forbidden() {
    return new BusinessException("MILESTONE_FORBIDDEN", "需要 milestone.update 权限");
  }

  private static BusinessException forbiddenRead() {
    return new BusinessException("MILESTONE_FORBIDDEN", "需要 milestone.read 权限");
  }

  // ──────────── result types ────────────

  public record MilestoneResult(String studyCode, List<MilestoneNodeState> nodes) {}

  /**
   * Pipeline-overview status derived from a study's milestones.
   * @param status           column status (StudyStatus) for coloring/labeling
   * @param statusLabel      status.label()
   * @param statusTone       status.tone()
   * @param mainStageCode    主状态: current stage code (e.g. "PreIND")
   * @param mainStageLabel   主状态: current stage label (e.g. "PreIND")
   * @param subStatusLabel   子状态: reached node label (e.g. "LPI"), or "未开始"
   * @param preindCompleted  PreIND stage 最后节点 actual_end != null
   * @param indCompleted     IND stage 最后节点 actual_end != null
   * @param globallyCompleted 全局最末里程碑节点 actual_end != null
   * @param currentPhaseCompleted 当前阶段对应里程碑最后节点 actual_end != null（即当前阶段已完成）
   */
  public record MilestoneOverviewStatus(
      StudyStatus status, String statusLabel, String statusTone,
      String mainStageCode, String mainStageLabel, String subStatusLabel,
      boolean preindCompleted, boolean indCompleted, boolean globallyCompleted,
      boolean currentPhaseCompleted) {}

  public record MilestoneNodeState(
      Long milestoneId,
      String stageCode, String stageLabel, int stageOrder,
      String milestoneCode, String milestoneLabel, int nodeOrder,
      LocalDate planV1Date, LocalDate planV2Date,
      LocalDate actualStartDate, LocalDate actualEndDate,
      String deviationNote, String status) {

    public MilestoneNodeState(Long milestoneId, String stageCode, String stageLabel, int stageOrder,
        String milestoneCode, String milestoneLabel, int nodeOrder,
        LocalDate planV1Date, LocalDate planV2Date,
        LocalDate actualStartDate, LocalDate actualEndDate,
        String deviationNote) {
      this(milestoneId, stageCode, stageLabel, stageOrder,
          milestoneCode, milestoneLabel, nodeOrder,
          planV1Date, planV2Date, actualStartDate, actualEndDate,
          deviationNote, "NOT_STARTED");
    }

    public MilestoneNodeState withStatus(String newStatus) {
      return new MilestoneNodeState(milestoneId, stageCode, stageLabel, stageOrder,
          milestoneCode, milestoneLabel, nodeOrder,
          planV1Date, planV2Date, actualStartDate, actualEndDate,
          deviationNote, newStatus);
    }
  }

  public record StageProjectionResult(
      String currentStageCode, String currentStageName,
      String currentMilestoneCode, String currentMilestoneName,
      String statusText) {}
}
