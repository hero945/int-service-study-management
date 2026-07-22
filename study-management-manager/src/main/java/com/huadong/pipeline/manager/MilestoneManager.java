package com.huadong.pipeline.manager;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.milestone.MilestoneDefinition;
import com.huadong.pipeline.domain.milestone.MilestoneDefinition.MilestoneNode;
import com.huadong.pipeline.domain.milestone.MilestoneDefinition.StageGroup;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.MilestoneSaveCommand;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.MilestoneUpdateCommand;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.PersistedMilestone;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.StudyRef;
import com.huadong.pipeline.domain.user.UserAccount;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MilestoneManager {

  private final StudyMilestonePort milestones;
  private final UserAccountRepository users;

  public MilestoneManager(StudyMilestonePort milestones, UserAccountRepository users) {
    this.milestones = milestones;
    this.users = users;
  }

  // ──────────── query ────────────

  public MilestoneResult getMilestones(long studyId, String username) {
    UserAccount user = currentUser(username);
    StudyRef study = requireStudy(studyId);
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
    requireStudy(studyId);
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

    return getMilestones(studyId, username);
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

  // ──────────── helpers ────────────

  private StudyRef requireStudy(long studyId) {
    return milestones.findStudy(studyId)
        .orElseThrow(() -> new BusinessException("STUDY_NOT_FOUND",
            "Study " + studyId + " 不存在"));
  }

  private UserAccount currentUser(String username) {
    return users.findByUsername(username)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "当前登录账号不存在"));
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

  // ──────────── result types ────────────

  public record MilestoneResult(String studyCode, List<MilestoneNodeState> nodes) {}

  public record MilestoneNodeState(
      String stageCode, String stageLabel, int stageOrder,
      String milestoneCode, String milestoneLabel, int nodeOrder,
      LocalDate planV1Date, LocalDate planV2Date,
      LocalDate actualStartDate, LocalDate actualEndDate,
      String deviationNote, String status) {

    public MilestoneNodeState(String stageCode, String stageLabel, int stageOrder,
        String milestoneCode, String milestoneLabel, int nodeOrder,
        LocalDate planV1Date, LocalDate planV2Date,
        LocalDate actualStartDate, LocalDate actualEndDate,
        String deviationNote) {
      this(stageCode, stageLabel, stageOrder,
          milestoneCode, milestoneLabel, nodeOrder,
          planV1Date, planV2Date, actualStartDate, actualEndDate,
          deviationNote, "NOT_STARTED");
    }

    public MilestoneNodeState withStatus(String newStatus) {
      return new MilestoneNodeState(stageCode, stageLabel, stageOrder,
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
