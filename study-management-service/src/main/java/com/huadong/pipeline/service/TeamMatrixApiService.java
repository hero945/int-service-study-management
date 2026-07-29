package com.huadong.pipeline.service;


import com.huadong.pipeline.api.TeamMatrixApi;
import com.huadong.pipeline.audit.BusinessAuditService;
import com.huadong.pipeline.manager.TeamMatrixManager;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamMatrixApiService implements TeamMatrixApi {
  @Autowired
  private TeamMatrixManager manager;
  @Autowired
  private BusinessAuditService audit;

  @Override
  public MatrixResponse list(
      String username, String studyQuery, String roleQuery, int page, int pageSize) {
    return toResponse(manager.list(username, studyQuery, roleQuery, page, pageSize));
  }

  @Override
  public MatrixResponse getStudyTeam(long studyId, String username) {
    return toResponse(manager.getStudyTeam(studyId, username));
  }

  private MatrixResponse toResponse(
      com.huadong.pipeline.domain.team.TeamMatrixRepository.MatrixPage matrix) {
    var studies = matrix.studies().stream()
        .map(study -> new StudyResponse(
            study.studyId(), study.studyCode(), study.indication(),
            study.statusCode(), study.statusLabel(), study.currentStatus(), study.version()))
        .toList();
    var roles = matrix.roles().stream()
        .map(role -> new RoleResponse(
            role.roleCode(), role.roleName(), role.functionCode(), role.functionName()))
        .toList();
    var assignments = matrix.assignments().stream()
        .map(assignment -> new AssignmentResponse(
            assignment.studyId(),
            assignment.roleCode(),
            assignment.members().stream()
                .map(member -> new MemberResponse(
                    member.userId(), member.email(), member.displayName(), member.enabled()))
                .toList()))
        .toList();
    int totalPages = Math.max(1,
        (int) Math.ceil((double) matrix.totalStudies() / Math.max(1, matrix.pageSize())));
    return new MatrixResponse(
        studies, roles, assignments, matrix.totalRoles(),
        new PaginationResponse(
            matrix.page(), matrix.pageSize(), matrix.totalStudies(), totalPages));
  }

  @Override
  @Transactional
  public BatchResponse replace(BatchRequest request, String username) {
    Map<Long, MatrixResponse> beforeByStudy = new LinkedHashMap<>();
    request.studies().forEach(study ->
        beforeByStudy.put(study.studyId(), toResponse(manager.getStudyTeam(study.studyId(), username))));
    var command = new TeamMatrixManager.BatchCommand(
        request.studies().stream()
            .map(study -> new TeamMatrixManager.StudyChange(
                study.studyId(),
                study.expectedVersion(),
                study.roles().stream()
                    .map(role -> new TeamMatrixManager.RoleChange(
                        role.roleCode(),
                        role.userIds()))
                    .toList()))
            .toList());
    var result = manager.replace(command, username);
    for (var changed : result.studies()) {
      var after = toResponse(manager.getStudyTeamForAudit(changed.studyId(), username));
      String studyCode = after.studies().isEmpty()
          ? String.valueOf(changed.studyId()) : after.studies().getFirst().studyCode();
      audit.success(
          "TEAM", "STUDY", changed.studyId(), studyCode, changed.studyId(),
          "TEAM_ROLE_ASSIGN", "hd_plt_team_assignment", changed.studyId(),
          beforeByStudy.get(changed.studyId()), after, null, username);
    }
    return new BatchResponse(result.studies().stream()
        .map(study -> new StudyVersionResponse(study.studyId(), study.version()))
        .toList());
  }
}
