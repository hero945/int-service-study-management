package com.huadong.pipeline.domain.milestone;

import com.huadong.pipeline.domain.milestone.StudyMilestonePort.PersistedMilestone;
import java.util.List;

/**
 * Derives the current milestone phase and sub-status labels from persisted actual dates.
 * Shared by Study list and Team matrix so both pages show the same status field.
 */
public final class CurrentMilestoneStatus {
  private CurrentMilestoneStatus() {
  }

  public static PhaseStatus derive(List<PersistedMilestone> persisted) {
    if (persisted == null || persisted.isEmpty()) {
      return PhaseStatus.EMPTY;
    }
    MilestoneDefinition.StageGroup currentGroup = MilestoneDefinition.ALL.stream()
        .sorted((a, b) -> Integer.compare(b.sortOrder(), a.sortOrder()))
        .filter(group -> groupHasActualData(persisted, group))
        .findFirst()
        .orElse(null);
    if (currentGroup == null) {
      return PhaseStatus.EMPTY;
    }
    String status = currentGroup.nodes().stream()
        .sorted((a, b) -> Integer.compare(b.sortOrder(), a.sortOrder()))
        .filter(node -> hasMilestoneData(persisted, node.code()))
        .map(MilestoneDefinition.MilestoneNode::label)
        .findFirst()
        .orElse("");
    return new PhaseStatus(currentGroup.label(), status);
  }

  private static boolean groupHasActualData(
      List<PersistedMilestone> persisted,
      MilestoneDefinition.StageGroup group) {
    return group.nodes().stream()
        .anyMatch(node -> hasMilestoneData(persisted, node.code()));
  }

  private static boolean hasMilestoneData(List<PersistedMilestone> persisted, String milestoneCode) {
    return persisted.stream()
        .filter(m -> m.milestoneCode().equals(milestoneCode))
        .anyMatch(m -> m.actualStartDate() != null || m.actualEndDate() != null);
  }

  public record PhaseStatus(String phase, String status) {
    public static final PhaseStatus EMPTY = new PhaseStatus("", "");
  }
}
