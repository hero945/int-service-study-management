package com.huadong.pipeline.web;

import com.huadong.pipeline.api.TeamMatrixApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.security.Principal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1")
public class TeamMatrixController {
  private final TeamMatrixApi teamMatrixApi;

  public TeamMatrixController(TeamMatrixApi teamMatrixApi) {
    this.teamMatrixApi = teamMatrixApi;
  }

  @GetMapping("/team-matrix")
  @PreAuthorize("hasAuthority('team.page.view')")
  TeamMatrixApi.MatrixResponse list(
      @RequestParam(defaultValue = "") String studyQuery,
      @RequestParam(defaultValue = "") String roleQuery,
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "100") @Min(1) @Max(100) int pageSize,
      Principal principal) {
    return teamMatrixApi.list(
        principal.getName(), studyQuery, roleQuery, page, pageSize);
  }

  @GetMapping("/studies/{studyId}/team")
  @PreAuthorize("hasAuthority('study.read')")
  TeamMatrixApi.MatrixResponse getStudyTeam(@PathVariable long studyId, Principal principal) {
    return teamMatrixApi.getStudyTeam(studyId, principal.getName());
  }

  @PutMapping("/team-matrix/assignments")
  @PreAuthorize("hasAuthority('team.edit_mode') and hasAuthority('team.update')")
  TeamMatrixApi.BatchResponse replace(
      @Valid @RequestBody TeamMatrixApi.BatchRequest request,
      Principal principal) {
    return teamMatrixApi.replace(request, principal.getName());
  }
}
