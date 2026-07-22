package com.huadong.pipeline.domain.milestone;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository port for {@code hd_plt_study_milestone}.
 * The definition of stages/nodes comes from {@link MilestoneDefinition}, not the database.
 */
public interface StudyMilestonePort {

  // ──────────── query ────────────

  /** Load persisted milestone rows for a study. */
  List<PersistedMilestone> findByStudyId(long studyId);

  /** Look up study metadata for access validation. */
  Optional<StudyRef> findStudy(long studyId);

  // ──────────── mutation ────────────

  /** Upsert (INSERT … ON DUPLICATE KEY UPDATE) one milestone row. */
  PersistedMilestone save(MilestoneSaveCommand command);

  // ──────────── records ────────────

  record PersistedMilestone(
      long id, long studyId, String stageCode, String milestoneCode,
      LocalDate planV1Date, LocalDate planV2Date,
      LocalDate actualStartDate, LocalDate actualEndDate,
      String deviationNote) {}

  record StudyRef(long id, String studyCode, long programId, String programCode,
                  long projectId, String projectCode) {}

  record MilestoneSaveCommand(
      long studyId, String stageCode, String milestoneCode,
      LocalDate planV1Date, LocalDate planV2Date,
      LocalDate actualStartDate, LocalDate actualEndDate,
      String deviationNote, String operatorEmail) {}

  record MilestoneUpdateCommand(
      LocalDate planV1Date, LocalDate planV2Date,
      LocalDate actualStartDate, LocalDate actualEndDate,
      String deviationNote) {}
}
