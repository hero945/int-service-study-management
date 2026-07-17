package com.huadong.pipeline.web;

import com.huadong.pipeline.api.StudyApi;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clinical-pipeline")
public class StudyController {
  private final StudyApi studyApi;

  public StudyController(StudyApi studyApi) {
    this.studyApi = studyApi;
  }

  @GetMapping("/overview")
  StudyApi.PipelineOverviewResponse overview() {
    return studyApi.overview();
  }

  @GetMapping("/studies")
  List<StudyApi.StudyResponse> list() {
    return studyApi.list();
  }

  @PostMapping("/studies")
  @ResponseStatus(HttpStatus.CREATED)
  void create(@Valid @RequestBody StudyApi.CreateStudyRequest request, Principal principal) {
    studyApi.create(request, principal.getName());
  }
}
