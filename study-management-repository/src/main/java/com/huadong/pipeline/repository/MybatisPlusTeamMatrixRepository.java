package com.huadong.pipeline.repository;

import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.team.TeamMatrixRepository;
import com.huadong.pipeline.repository.mapper.TeamMatrixMapper;
import com.huadong.pipeline.repository.mapper.TeamMemberRow;
import com.huadong.pipeline.repository.mapper.TeamRoleRow;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusTeamMatrixRepository implements TeamMatrixRepository {
  private final TeamMatrixMapper mapper;

  public MybatisPlusTeamMatrixRepository(TeamMatrixMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public MatrixPage findMatrix(
      StudyAccessScope scope, String studyQuery, String roleQuery, int page, int pageSize) {
    long total = mapper.countStudies(
        scope.allStudies(), scope.userId(), studyQuery);
    var studyRows = mapper.findStudies(
        scope.allStudies(), scope.userId(), studyQuery, (page - 1) * pageSize, pageSize);
    var studies = studyRows.stream()
        .map(row -> new TeamStudy(
            row.studyId(), row.studyCode(), row.indication(),
            row.statusCode(), row.statusLabel(), row.teamVersion()))
        .toList();
    var roles = mapper.findRoles(roleQuery).stream().map(this::role).toList();
    if (studies.isEmpty()) {
      return new MatrixPage(studies, roles, List.of(), total, roles.size(), page, pageSize);
    }

    var visibleRoleCodes = roles.stream().map(TeamRole::roleCode).collect(Collectors.toSet());
    Map<String, List<TeamMember>> grouped = new LinkedHashMap<>();
    for (TeamMemberRow row : mapper.findAssignments(
        studies.stream().map(TeamStudy::studyId).toList())) {
      if (!visibleRoleCodes.contains(row.roleCode())) {
        continue;
      }
      grouped.computeIfAbsent(row.studyId() + "|" + row.roleCode(), ignored -> new ArrayList<>())
          .add(member(row));
    }
    var assignments = grouped.entrySet().stream()
        .map(entry -> {
          int separator = entry.getKey().indexOf('|');
          return new Assignment(
              Long.parseLong(entry.getKey().substring(0, separator)),
              entry.getKey().substring(separator + 1),
              entry.getValue());
        })
        .toList();
    return new MatrixPage(studies, roles, assignments, total, roles.size(), page, pageSize);
  }

  @Override
  public Map<Long, Long> findStudyVersions(Set<Long> studyIds, StudyAccessScope scope) {
    if (studyIds.isEmpty()) {
      return Map.of();
    }
    return mapper.lockStudies(studyIds, scope.allStudies(), scope.userId()).stream()
        .collect(Collectors.toMap(row -> row.studyId(), row -> row.teamVersion()));
  }

  @Override
  public Map<String, TeamRole> findRoles(Set<String> roleCodes) {
    if (roleCodes.isEmpty()) {
      return Map.of();
    }
    return mapper.findRolesByCodes(roleCodes).stream()
        .map(this::role)
        .collect(Collectors.toMap(TeamRole::roleCode, value -> value));
  }

  @Override
  public Map<Long, TeamMember> findMembers(Set<Long> userIds) {
    if (userIds.isEmpty()) {
      return Map.of();
    }
    return mapper.findMembers(userIds).stream()
        .map(this::member)
        .collect(Collectors.toMap(TeamMember::userId, value -> value));
  }

  @Override
  public List<Long> findAssignedUserIds(long studyId, String roleCode) {
    return mapper.findAssignedUserIds(studyId, roleCode);
  }

  @Override
  public void replaceAssignments(
      long studyId, TeamRole role, List<TeamMember> members, String operator) {
    mapper.softDeleteRoleAssignments(studyId, role.id(), operator);
    for (TeamMember member : members) {
      int updated = mapper.reviveAssignment(
          studyId, role.id(), role.functionLineId(), role.roleCode(), role.roleName(),
          role.functionCode(), role.functionName(), member.userId(), member.email(),
          member.displayName(), operator);
      if (updated == 0) {
        mapper.insertAssignment(
            studyId, role.id(), role.functionLineId(), role.roleCode(), role.roleName(),
            role.functionCode(), role.functionName(), member.userId(), member.email(),
            member.displayName(), operator);
      }
    }
  }

  @Override
  public boolean incrementVersion(long studyId, long expectedVersion, String operator) {
    return mapper.incrementVersion(studyId, expectedVersion, operator) == 1;
  }

  @Override
  public void appendAudit(
      long studyId, String roleCode, List<Long> beforeUserIds, List<Long> afterUserIds,
      long operatorUserId, String operator) {
    mapper.insertAudit(
        studyId, roleCode, beforeUserIds.toString(), afterUserIds.toString(),
        operatorUserId, operator);
  }

  private TeamRole role(TeamRoleRow row) {
    return new TeamRole(
        row.id(), row.roleCode(), row.roleName(), row.functionLineId(),
        row.functionCode(), row.functionName(), row.sortOrder());
  }

  private TeamMember member(TeamMemberRow row) {
    return new TeamMember(
        row.userId(), row.email(), row.displayName(), "ACTIVE".equals(row.statusCode()));
  }
}
