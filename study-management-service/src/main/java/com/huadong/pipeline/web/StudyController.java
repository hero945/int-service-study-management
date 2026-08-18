package com.huadong.pipeline.web;


import com.huadong.pipeline.api.StudyApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/clinical-pipeline")
public class StudyController {
  @Autowired
  private StudyApi studyApi;

  @GetMapping("/overview")
  @PreAuthorize("hasAuthority('pipeline.page.view')")
  StudyApi.PipelineOverviewResponse overview(Principal principal) {
    return studyApi.overview(principal.getName());
  }

  @GetMapping("/studies")
  @PreAuthorize("hasAuthority('study.read')")
  StudyApi.StudyPageResponse list(
      @RequestParam(defaultValue = "") String therapeuticArea,
      @RequestParam(defaultValue = "") String program,
      @RequestParam(defaultValue = "") String product,
      @RequestParam(defaultValue = "") String project,
      @RequestParam(defaultValue = "") String studyCode,
      @RequestParam(defaultValue = "") String milestoneStatus,
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize,
      Principal principal) {
    return studyApi.list(
        principal.getName(),
        therapeuticArea,
        program,
        product,
        project,
        studyCode,
        milestoneStatus,
        page,
        pageSize);
  }

  @PostMapping("/studies")
  @PreAuthorize("hasAuthority('config.create')")
  @ResponseStatus(HttpStatus.CREATED)
  void create(@Valid @RequestBody StudyApi.CreateStudyRequest request, Principal principal) {
    studyApi.create(request, principal.getName());
  }
}
