package com.huadong.pipeline.domain.milestone;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Java-fixed milestone stage groups and nodes for clinical development.
 * Not database-driven — adding or reordering nodes requires a code change and redeploy.
 */
public final class MilestoneDefinition {

  private MilestoneDefinition() {}

  /**
   * Predefined milestone node within a stage group.
   * @param code       Stable identifier (e.g. "PREIND_SUBMIT")
   * @param label      Display name (e.g. "PreIND 递交")
   * @param sortOrder  Zero-based order within the parent stage group
   */
  public record MilestoneNode(String code, String label, int sortOrder) {}

  /**
   * A stage group (e.g. "PreIND") containing an ordered list of milestone nodes.
   * @param code        Stable stage identifier
   * @param label       Display name
   * @param sortOrder   Zero-based order among all stage groups
   * @param nodes       Ordered milestone nodes belonging to this stage
   */
  public record StageGroup(String code, String label, int sortOrder, List<MilestoneNode> nodes) {}

  // ────────────────────────────────────────────────────────────
  // 10 stage groups — 60 milestone nodes — source: PRD §4.9 & Coverpage MS_GROUPS
  // ────────────────────────────────────────────────────────────

  public static final List<StageGroup> ALL = List.of(
    stage(0, "PreIND", "PreIND", List.of(
      "PreIND 递交", "PreIND 反馈-临床医学", "PreIND 反馈-数统",
      "PreIND 反馈-临床药理", "PreIND 反馈-非临床", "PreIND 反馈-药学")),
    stage(1, "IND", "IND", List.of(
      "IND 递交", "IND 形审发补", "IND 形审补正", "IND 受理", "IND 获批")),
    stage(2, "Pre3", "Pre3", List.of(
      "Pre3 递交", "Pre3 反馈-临床医学", "Pre3 反馈-数统",
      "Pre3 反馈-临床药理", "Pre3 反馈-非临床", "Pre3 反馈-药学")),
    stage(3, "Protocol", "Protocol", List.of(
      "方案摘要定稿", "方案讨论会", "方案定稿")),
    stage(4, "SSU", "SSU", List.of(
      "组长单位立项递交", "组长单位立项获批", "组长单位伦理递交",
      "组长单位伦理获批", "组长单位合同签署", "首家中心启动",
      "组长单位启动", "所有中心启动", "人遗递交", "人遗批准",
      "CDE 平台登记", "ClinicalTrial 登记")),
    stage(5, "Enrollment", "Enrollment", List.of(
      "FPI", "LPI", "LPO")),
    stage(6, "IA", "IA", List.of(
      "IA 数据冻结", "IA 数据分析")),
    stage(7, "Data_Report", "Data & Report", List.of(
      "DBL", "TLR初稿", "TLR定稿", "TFL初稿", "TFL定稿",
      "CSR初稿", "CSR定稿", "中心关闭")),
    stage(8, "PreNDA_BLA", "PreNDA/BLA", List.of(
      "PreNDA 递交", "PreNDA 反馈-临床医学", "PreNDA 反馈-数统",
      "PreNDA 反馈-临床药理", "PreNDA 反馈-非临床", "PreNDA 反馈-药学")),
    stage(9, "NDA_BLA", "NDA/BLA", List.of(
      "NDA/BLA 递交", "NDA/BLA 形审发补", "NDA/BLA 形审补正",
      "NDA/BLA 受理", "临床核查", "药学核查",
      "NDA/BLA 发补", "NDA/BLA 补正", "NDA/BLA 获批"))
  );

  /** Study 完成节点：中心关闭（Data & Report 最后一节点）。缺行或 Actual End 为空均视为未完成。 */
  public static final String STUDY_COMPLETED_CODE = "Data_Report-7";

  /**
   * All 60 nodes flattened in definition order, each keyed by {@code stageCode + "-" + nodeIndex}.
   */
  public static List<MilestoneNode> orderedNodes() {
    return ALL.stream()
        .flatMap(group -> group.nodes.stream())
        .toList();
  }

  /**
   * Resolve a node by stage code and its zero-based index within that stage.
   */
  public static MilestoneNode node(String stageCode, int nodeIndex) {
    StageGroup group = ALL.stream()
        .filter(g -> g.code.equals(stageCode))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown stage: " + stageCode));
    if (nodeIndex < 0 || nodeIndex >= group.nodes.size()) {
      throw new IllegalArgumentException(
          "Node index " + nodeIndex + " out of range for stage " + stageCode);
    }
    return group.nodes.get(nodeIndex);
  }

  public static String milestoneCode(String stageCode, int nodeIndex) {
    return stageCode + "-" + nodeIndex;
  }

  // ─────────────── helpers ───────────────

  private static StageGroup stage(int sort, String code, String label,
                                  List<String> nodeLabels) {
    List<MilestoneNode> nodes = IntStream.range(0, nodeLabels.size())
        .mapToObj(i -> new MilestoneNode(
            milestoneCode(code, i), nodeLabels.get(i), i))
        .toList();
    return new StageGroup(code, label, sort, nodes);
  }
}
