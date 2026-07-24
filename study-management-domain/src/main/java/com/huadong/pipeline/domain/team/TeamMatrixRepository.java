package com.huadong.pipeline.domain.team;

import com.huadong.pipeline.domain.study.StudyAccessScope;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TeamMatrixRepository {
  MatrixPage findMatrix(
      StudyAccessScope scope, String studyQuery, String roleQuery, int page, int pageSize);

  /** Single-study team read model within the caller's Study data scope. */
  MatrixPage findStudyTeam(StudyAccessScope scope, long studyId);

  Map<Long, Long> findStudyVersions(Set<Long> studyIds, StudyAccessScope scope);

  Map<String, TeamRole> findRoles(Set<String> roleCodes);

  Map<Long, TeamMember> findMembers(Set<Long> userIds);

  List<Long> findAssignedUserIds(long studyId, String roleCode);

  /** Study id -> concatenated member display names for the given role code across the study set. */
  Map<Long, String> findRoleMemberNames(Set<Long> studyIds, String roleCode);

  void replaceAssignments(
      long studyId, TeamRole role, List<TeamMember> members, String operator);

  boolean incrementVersion(long studyId, long expectedVersion, String operator);

  void appendAudit(
      long studyId, String roleCode, List<Long> beforeUserIds, List<Long> afterUserIds,
      long operatorUserId, String operator);

  record TeamStudy(
      long studyId,
      String studyCode,
      String indication,
      String statusCode,
      String statusLabel,
      String currentStatus,
      long version) {
  }

  record TeamRole(
      long id,
      String roleCode,
      String roleName,
      Long functionLineId,
      String functionCode,
      String functionName,
      int sortOrder) {
  }

  record TeamMember(
      long userId,
      String email,
      String displayName,
      boolean enabled) {
  }

  record Assignment(long studyId, String roleCode, List<TeamMember> members) {
    public Assignment {
      members = List.copyOf(members);
    }
  }

  record MatrixPage(
      List<TeamStudy> studies,
      List<TeamRole> roles,
      List<Assignment> assignments,
      long totalStudies,
      int totalRoles,
      int page,
      int pageSize) {
    public MatrixPage {
      studies = List.copyOf(studies);
      roles = List.copyOf(roles);
      assignments = List.copyOf(assignments);
    }
  }
}
