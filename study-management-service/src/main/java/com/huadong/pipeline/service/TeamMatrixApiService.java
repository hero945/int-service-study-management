package com.huadong.pipeline.service;

import com.huadong.pipeline.api.TeamMatrixApi;
import com.huadong.pipeline.manager.TeamMatrixManager;
import org.springframework.stereotype.Service;

@Service
public class TeamMatrixApiService implements TeamMatrixApi {
  private final TeamMatrixManager manager;

  public TeamMatrixApiService(TeamMatrixManager manager) {
    this.manager = manager;
  }

  @Override
  public MatrixResponse list(
      String username, String studyQuery, String roleQuery, int page, int pageSize) {
    var matrix = manager.list(username, studyQuery, roleQuery, page, pageSize);
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
        (int) Math.ceil((double) matrix.totalStudies() / matrix.pageSize()));
    return new MatrixResponse(
        studies, roles, assignments, matrix.totalRoles(),
        new PaginationResponse(
            matrix.page(), matrix.pageSize(), matrix.totalStudies(), totalPages));
  }

  @Override
  public BatchResponse replace(BatchRequest request, String username) {
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
    return new BatchResponse(result.studies().stream()
        .map(study -> new StudyVersionResponse(study.studyId(), study.version()))
        .toList());
  }
}
