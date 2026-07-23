package com.huadong.pipeline.manager;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.milestone.CurrentMilestoneStatus;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort;
import com.huadong.pipeline.domain.milestone.StudyMilestonePort.PersistedMilestone;
import com.huadong.pipeline.domain.study.StudyAccessScope;
import com.huadong.pipeline.domain.team.TeamMatrixRepository;
import com.huadong.pipeline.domain.team.TeamMatrixRepository.MatrixPage;
import com.huadong.pipeline.domain.team.TeamMatrixRepository.TeamMember;
import com.huadong.pipeline.domain.team.TeamMatrixRepository.TeamStudy;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.domain.user.UserAccount;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamMatrixManager {
  private final TeamMatrixRepository teams;
  private final UserAccountRepository users;
  private final StudyMilestonePort studyMilestones;

  public TeamMatrixManager(
      TeamMatrixRepository teams,
      UserAccountRepository users,
      StudyMilestonePort studyMilestones) {
    this.teams = teams;
    this.users = users;
    this.studyMilestones = studyMilestones;
  }

  public MatrixPage list(
      String username, String studyQuery, String roleQuery, int page, int pageSize) {
    UserAccount user = currentUser(username);
    MatrixPage matrix = teams.findMatrix(
        accessScope(user), normalizeQuery(studyQuery), normalizeQuery(roleQuery), page, pageSize);
    return withCurrentStatus(matrix);
  }

  private MatrixPage withCurrentStatus(MatrixPage matrix) {
    List<Long> studyIds = matrix.studies().stream().map(TeamStudy::studyId).toList();
    Map<Long, List<PersistedMilestone>> milestonesByStudy =
        studyMilestones.findByStudyIds(studyIds).stream()
            .collect(Collectors.groupingBy(PersistedMilestone::studyId));
    List<TeamStudy> studies = matrix.studies().stream()
        .map(study -> {
          String currentStatus = CurrentMilestoneStatus.derive(
              milestonesByStudy.getOrDefault(study.studyId(), List.of())).status();
          return new TeamStudy(
              study.studyId(),
              study.studyCode(),
              study.indication(),
              study.statusCode(),
              study.statusLabel(),
              currentStatus,
              study.version());
        })
        .toList();
    return new MatrixPage(
        studies,
        matrix.roles(),
        matrix.assignments(),
        matrix.totalStudies(),
        matrix.totalRoles(),
        matrix.page(),
        matrix.pageSize());
  }

  @Transactional
  public BatchResult replace(BatchCommand command, String username) {
    if (command.studies().isEmpty() || command.studies().size() > 20) {
      throw invalid("一次必须提交1至20个Study");
    }
    ensureUnique(
        command.studies().stream().map(StudyChange::studyId).toList(),
        "同一Study不能重复提交");

    UserAccount operator = currentUser(username);
    Set<Long> studyIds = new LinkedHashSet<>();
    Set<String> roleCodes = new LinkedHashSet<>();
    Set<Long> userIds = new LinkedHashSet<>();
    for (StudyChange study : command.studies()) {
      if (study.studyId() <= 0 || study.expectedVersion() < 0
          || study.roles().isEmpty() || study.roles().size() > 44) {
        throw invalid("Study变更内容不完整");
      }
      ensureUnique(
          study.roles().stream().map(RoleChange::roleCode).toList(),
          "同一Study中的角色不能重复提交");
      studyIds.add(study.studyId());
      for (RoleChange role : study.roles()) {
        if (role.roleCode() == null || role.roleCode().isBlank()
            || role.userIds().size() > 100) {
          throw invalid("角色编码或成员数量不合法");
        }
        ensureUnique(role.userIds(), "同一角色不能重复添加成员");
        if (role.userIds().stream().anyMatch(id -> id == null || id <= 0)) {
          throw invalid("成员ID必须为正整数");
        }
        roleCodes.add(role.roleCode());
        userIds.addAll(role.userIds());
      }
    }

    Map<Long, Long> versions = teams.findStudyVersions(studyIds, accessScope(operator));
    if (versions.size() != studyIds.size()) {
      throw new BusinessException("STUDY_OUT_OF_SCOPE", "目标Study不存在或不在当前数据范围");
    }
    for (StudyChange study : command.studies()) {
      if (!versions.get(study.studyId()).equals(study.expectedVersion())) {
        throw new BusinessException("TEAM_VERSION_CONFLICT", "团队矩阵已被其他用户修改，请刷新后重试");
      }
    }

    var roles = teams.findRoles(roleCodes);
    if (roles.size() != roleCodes.size()) {
      throw new BusinessException("INVALID_TEAM_ROLE", "包含不存在或已停用的团队角色");
    }
    var members = teams.findMembers(userIds);
    if (members.size() != userIds.size()) {
      throw new BusinessException("INVALID_TEAM_MEMBER", "包含不存在的团队成员");
    }

    List<StudyVersion> result = new ArrayList<>();
    for (StudyChange study : command.studies()) {
      boolean changed = false;
      for (RoleChange roleChange : study.roles()) {
        List<Long> before = teams.findAssignedUserIds(study.studyId(), roleChange.roleCode());
        Set<Long> beforeSet = new HashSet<>(before);
        List<TeamMember> afterMembers = roleChange.userIds().stream()
            .map(members::get)
            .toList();
        boolean newlyAddsDisabled = afterMembers.stream()
            .anyMatch(member -> !member.enabled() && !beforeSet.contains(member.userId()));
        if (newlyAddsDisabled) {
          throw new BusinessException("INVALID_TEAM_MEMBER", "不能新增已停用账号为团队成员");
        }
        List<Long> after = afterMembers.stream().map(TeamMember::userId).sorted().toList();
        List<Long> sortedBefore = before.stream().sorted().toList();
        if (!sortedBefore.equals(after)) {
          teams.replaceAssignments(study.studyId(), roles.get(roleChange.roleCode()),
              afterMembers, operator.username());
          teams.appendAudit(
              study.studyId(), roleChange.roleCode(), sortedBefore, after,
              operator.id(), operator.username());
          changed = true;
        }
      }
      long nextVersion = study.expectedVersion();
      if (changed) {
        if (!teams.incrementVersion(
            study.studyId(), study.expectedVersion(), operator.username())) {
          throw new BusinessException("TEAM_VERSION_CONFLICT", "团队矩阵已被其他用户修改，请刷新后重试");
        }
        nextVersion++;
      }
      result.add(new StudyVersion(study.studyId(), nextVersion));
    }
    return new BatchResult(result);
  }

  private UserAccount currentUser(String username) {
    return users.findByUsername(username)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "当前登录账号不存在"));
  }

  private StudyAccessScope accessScope(UserAccount user) {
    return user.dataScope() == DataScope.ALL
        ? StudyAccessScope.all()
        : StudyAccessScope.assignedTo(user.id());
  }

  private static String normalizeQuery(String value) {
    if (value == null || value.isBlank()) {
      return "";
    }
    return value.trim();
  }

  private static <T> void ensureUnique(List<T> values, String message) {
    if (new HashSet<>(values).size() != values.size()) {
      throw invalid(message);
    }
  }

  private static BusinessException invalid(String message) {
    return new BusinessException("INVALID_TEAM_CHANGE", message);
  }

  public record BatchCommand(List<StudyChange> studies) {
    public BatchCommand {
      studies = List.copyOf(studies);
    }
  }

  public record StudyChange(long studyId, long expectedVersion, List<RoleChange> roles) {
    public StudyChange {
      roles = List.copyOf(roles);
    }
  }

  public record RoleChange(String roleCode, List<Long> userIds) {
    public RoleChange {
      userIds = List.copyOf(userIds);
    }
  }

  public record StudyVersion(long studyId, long version) {
  }

  public record BatchResult(List<StudyVersion> studies) {
    public BatchResult {
      studies = List.copyOf(studies);
    }
  }
}
