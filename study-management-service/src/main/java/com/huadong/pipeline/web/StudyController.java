package com.huadong.pipeline.web;


import com.huadong.pipeline.api.StudyApi;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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
  List<StudyApi.StudyResponse> list(Principal principal) {
    return studyApi.list(principal.getName());
  }

  @PostMapping("/studies")
  @PreAuthorize("hasAuthority('config.create')")
  @ResponseStatus(HttpStatus.CREATED)
  void create(@Valid @RequestBody StudyApi.CreateStudyRequest request, Principal principal) {
    studyApi.create(request, principal.getName());
  }
}
