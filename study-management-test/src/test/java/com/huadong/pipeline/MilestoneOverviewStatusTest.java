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

  private final MilestoneManager manager = new MilestoneManager(null, null);

  private PersistedMilestone m(String stage, String code, LocalDate start, LocalDate end) {
    return new PersistedMilestone(1L, 1L, stage, code, null, null, start, end, null);
  }

  @Test
  void returnsNullWhenNoMilestones() {
    assertThat(manager.computeOverviewStatus(List.of())).isNull();
  }

  @Test
  void preindAndIndCompletedWhenTheirLastNodesHaveActualEnd() {
    // PreIND: 3 nodes (0,1,2), all completed → preindCompleted=true
    // IND: 3 nodes (0,1,2), node-2 has actual_end → indCompleted=true
    // Global last = IND-2 with actual_end → globallyCompleted=true
    LocalDate s = LocalDate.of(2026, 1, 1);
    LocalDate e = LocalDate.of(2026, 2, 1);
    var rows = new ArrayList<PersistedMilestone>();
    for (int i = 0; i <= 2; i++) rows.add(m("PRE_IND", "PRE_IND-" + i, s, e));
    for (int i = 0; i <= 2; i++) rows.add(m("IND", "IND-" + i, s, e));

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
    for (int i = 0; i <= 2; i++) rows.add(m("PRE_IND", "PRE_IND-" + i, s, e));
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
    var rows = List.of(m("PRE_IND", "PRE_IND-0", LocalDate.of(2026, 1, 1), null));
    var result = manager.computeOverviewStatus(rows);
    assertThat(result.status().name()).isEqualTo("ACTIVE");
    assertThat(result.statusLabel()).isEqualTo("进行中");
    assertThat(result.mainStageCode()).isEqualTo("PRE_IND");
    assertThat(result.mainStageLabel()).isEqualTo("PreIND");
    assertThat(result.subStatusLabel()).isEqualTo("PreIND 递交");
    assertThat(result.preindCompleted()).isFalse();
    assertThat(result.indCompleted()).isFalse();
    assertThat(result.globallyCompleted()).isFalse();
  }

  @Test
  void plannedWhenNotStarted() {
    var rows = List.of(m("PRE_IND", "PRE_IND-0", null, null));
    var result = manager.computeOverviewStatus(rows);
    assertThat(result.status().name()).isEqualTo("PLANNED");
    assertThat(result.statusLabel()).isEqualTo("计划中");
    assertThat(result.mainStageCode()).isEqualTo("PRE_IND");
    assertThat(result.mainStageLabel()).isEqualTo("PreIND");
    assertThat(result.subStatusLabel()).isEqualTo("未开始");
    assertThat(result.preindCompleted()).isFalse();
    assertThat(result.indCompleted()).isFalse();
    assertThat(result.globallyCompleted()).isFalse();
    assertThat(result.currentPhaseCompleted()).isFalse();
  }

  @Test
  void currentPhaseCompletedWhenFrontierIsLastNodeOfItsStageWithActualEnd() {
    // PreIND 0..2 + IND 0..4 all done → frontier = IND-4 (last IND node, actual_end)
    LocalDate s = LocalDate.of(2026, 1, 1);
    LocalDate e = LocalDate.of(2026, 2, 1);
    var rows = new ArrayList<PersistedMilestone>();
    for (int i = 0; i <= 2; i++) rows.add(m("PRE_IND", "PRE_IND-" + i, s, e));
    for (int i = 0; i <= 4; i++) rows.add(m("IND", "IND-" + i, s, e));

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
    for (int i = 0; i <= 2; i++) rows.add(m("PRE_IND", "PRE_IND-" + i, s, e));
    for (int i = 0; i <= 3; i++) rows.add(m("IND", "IND-" + i, s, e));

    var result = manager.computeOverviewStatus(rows);
    assertThat(result).isNotNull();
    assertThat(result.currentPhaseCompleted()).isFalse();
    assertThat(result.mainStageLabel()).isEqualTo("IND");
    assertThat(result.subStatusLabel()).isEqualTo("IND 受理");
  }
}
