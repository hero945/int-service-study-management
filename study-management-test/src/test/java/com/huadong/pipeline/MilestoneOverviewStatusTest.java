package com.huadong.pipeline;

import static org.assertj.core.api.Assertions.assertThat;

import com.huadong.pipeline.domain.milestone.StudyMilestonePort.PersistedMilestone;
import com.huadong.pipeline.manager.MilestoneManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit test for {@link MilestoneManager#computeOverviewStatus} — no Spring context needed. */
class MilestoneOverviewStatusTest {

  private final MilestoneManager manager = new MilestoneManager();

  private PersistedMilestone m(String stage, String code, LocalDate start, LocalDate end) {
    return new PersistedMilestone(1L, 1L, stage, code, null, null, start, end, null);
  }

  @Test
  void returnsNullWhenNoMilestones() {
    assertThat(manager.computeOverviewStatus(List.of())).isNull();
  }

  @Test
  void preindAndIndCompletedWhenTheirLastNodesHaveActualEnd() {
    // PreIND has 6 nodes (0..5), IND has 5 nodes (0..4) in MilestoneDefinition.
    LocalDate s = LocalDate.of(2026, 1, 1);
    LocalDate e = LocalDate.of(2026, 2, 1);
    var rows = new ArrayList<PersistedMilestone>();
    for (int i = 0; i <= 5; i++) {
      rows.add(m("PreIND", "PreIND-" + i, s, e));
    }
    for (int i = 0; i <= 4; i++) {
      rows.add(m("IND", "IND-" + i, s, e));
    }

    var result = manager.computeOverviewStatus(rows);
    assertThat(result).isNotNull();
    assertThat(result.status().name()).isEqualTo("COMPLETED");
    assertThat(result.preindCompleted()).isTrue();
    assertThat(result.indCompleted()).isTrue();
    assertThat(result.globallyCompleted()).isTrue();
    assertThat(result.mainStageCode()).isEqualTo("IND");
    assertThat(result.mainStageLabel()).isEqualTo("IND");
  }

  @Test
  void preindCompletedButIndNotWhenOnlyPreIndHasActualEndOnLastNode() {
    // PreIND all done, IND started but last node not finished
    LocalDate s = LocalDate.of(2026, 1, 1);
    LocalDate e = LocalDate.of(2026, 2, 1);
    var rows = new ArrayList<PersistedMilestone>();
    for (int i = 0; i <= 5; i++) {
      rows.add(m("PreIND", "PreIND-" + i, s, e));
    }
    rows.add(m("IND", "IND-0", s, null));   // IND in progress

    var result = manager.computeOverviewStatus(rows);
    assertThat(result).isNotNull();
    assertThat(result.preindCompleted()).isTrue();
    assertThat(result.indCompleted()).isFalse();
    assertThat(result.globallyCompleted()).isFalse();
    assertThat(result.status().name()).isEqualTo("ACTIVE");
    assertThat(result.mainStageCode()).isEqualTo("IND");
  }

  @Test
  void activeWhenNodeInProgress() {
    var rows = List.of(m("PreIND", "PreIND-0", LocalDate.of(2026, 1, 1), null));
    var result = manager.computeOverviewStatus(rows);
    assertThat(result.status().name()).isEqualTo("ACTIVE");
    assertThat(result.statusLabel()).isEqualTo("进行中");
    assertThat(result.mainStageCode()).isEqualTo("PreIND");
    assertThat(result.mainStageLabel()).isEqualTo("PreIND");
    assertThat(result.subStatusLabel()).isEqualTo("PreIND 递交");
    assertThat(result.preindCompleted()).isFalse();
    assertThat(result.indCompleted()).isFalse();
    assertThat(result.globallyCompleted()).isFalse();
  }

  @Test
  void plannedWhenNotStarted() {
    var rows = List.of(m("PreIND", "PreIND-0", null, null));
    var result = manager.computeOverviewStatus(rows);
    assertThat(result.status().name()).isEqualTo("PLANNED");
    assertThat(result.statusLabel()).isEqualTo("计划中");
    assertThat(result.mainStageCode()).isEqualTo("PreIND");
    assertThat(result.mainStageLabel()).isEqualTo("PreIND");
    assertThat(result.subStatusLabel()).isEqualTo("未开始");
    assertThat(result.preindCompleted()).isFalse();
    assertThat(result.indCompleted()).isFalse();
    assertThat(result.globallyCompleted()).isFalse();
    assertThat(result.currentPhaseCompleted()).isFalse();
  }

  @Test
  void currentPhaseCompletedWhenFrontierIsLastNodeOfItsStageWithActualEnd() {
    // PreIND 0..5 + IND 0..4 all done → frontier = IND-4 (last IND node, actual_end)
    LocalDate s = LocalDate.of(2026, 1, 1);
    LocalDate e = LocalDate.of(2026, 2, 1);
    var rows = new ArrayList<PersistedMilestone>();
    for (int i = 0; i <= 5; i++) {
      rows.add(m("PreIND", "PreIND-" + i, s, e));
    }
    for (int i = 0; i <= 4; i++) {
      rows.add(m("IND", "IND-" + i, s, e));
    }

    var result = manager.computeOverviewStatus(rows);
    assertThat(result).isNotNull();
    assertThat(result.currentPhaseCompleted()).isTrue();
    assertThat(result.mainStageLabel()).isEqualTo("IND");
    assertThat(result.subStatusLabel()).isEqualTo("IND 获批");
  }

  @Test
  void currentPhaseCompletedFalseWhenFrontierIsMidStage() {
    // PreIND done, IND 0..3 done but last IND node (IND-4) not done → frontier = IND-3
    LocalDate s = LocalDate.of(2026, 1, 1);
    LocalDate e = LocalDate.of(2026, 2, 1);
    var rows = new ArrayList<PersistedMilestone>();
    for (int i = 0; i <= 5; i++) {
      rows.add(m("PreIND", "PreIND-" + i, s, e));
    }
    for (int i = 0; i <= 3; i++) {
      rows.add(m("IND", "IND-" + i, s, e));
    }

    var result = manager.computeOverviewStatus(rows);
    assertThat(result).isNotNull();
    assertThat(result.currentPhaseCompleted()).isFalse();
    assertThat(result.mainStageLabel()).isEqualTo("IND");
    assertThat(result.subStatusLabel()).isEqualTo("IND 受理");
  }

  @Test
  void regulatoryStatusUsesEachStageOwnFrontierWithoutBackfill() {
    LocalDate s = LocalDate.of(2026, 1, 1);
    LocalDate e = LocalDate.of(2026, 2, 1);
    var rows = List.of(
        pm("PreIND", "PreIND-0", s, e),
        pm("Pre3", "Pre3-0", s, null));

    var result = manager.deriveRegulatoryStatus(rows);
    assertThat(result).isNotNull();
    assertThat(result.preindCompleted()).isFalse();
    assertThat(result.preindSubStatusLabel()).isEqualTo("PreIND 递交");
    assertThat(result.indCompleted()).isFalse();
    assertThat(result.indSubStatusLabel()).isNull();
    assertThat(result.pre3Completed()).isFalse();
    assertThat(result.pre3SubStatusLabel()).isEqualTo("Pre3 递交");
  }

  @Test
  void projectionKeepsFrontierInsteadOfNextEmptyNode() {
    var nodes = List.of(
        node("PreIND", "PreIND-0", "PreIND 递交", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 10)),
        node("PreIND", "PreIND-1", "PreIND 反馈-临床医学", null, null));
    var result = MilestoneManager.projectionFromNodes(nodes);
    assertThat(result.currentMilestoneCode()).isEqualTo("PreIND-0");
    assertThat(result.currentMilestoneName()).isEqualTo("PreIND 递交");
    assertThat(result.statusText()).isEqualTo("进行中");
  }

  private com.huadong.pipeline.domain.milestone.ProjectMilestone pm(
      String stage, String code, LocalDate start, LocalDate end) {
    return new com.huadong.pipeline.domain.milestone.ProjectMilestone(
        1L, 1L, stage, code, null, null, start, end, null, null);
  }

  private MilestoneManager.MilestoneNodeState node(
      String stage, String code, String label, LocalDate start, LocalDate end) {
    return new MilestoneManager.MilestoneNodeState(
        1L, stage, stage, 0, code, label, 0, null, null, start, end, null, "PROJECT");
  }
}
