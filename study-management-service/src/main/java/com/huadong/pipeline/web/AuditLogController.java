package com.huadong.pipeline.web;

import com.huadong.pipeline.api.AuditLogApi;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {
  private final AuditLogApi auditLogApi;

  public AuditLogController(AuditLogApi auditLogApi) {
    this.auditLogApi = auditLogApi;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('audit.read')")
  AuditLogApi.AuditLogPageResponse list(
      @RequestParam
      @Pattern(regexp = "MILESTONE|MONTHLY|RISK|TEAM|CONFIG|ACCOUNT|ROLE")
      String moduleCode,
      @RequestParam(required = false)
      @Pattern(regexp = "MILESTONE|MONTHLY_ENTRY|RISK|STUDY|PROGRAM|PROJECT|USER|ROLE")
      String subjectType,
      @RequestParam(required = false) @Min(1) Long subjectId,
      @RequestParam(required = false) @Min(1) Long scopeStudyId,
      @RequestParam(required = false)
      @Pattern(regexp = "MILESTONE_STAGE|MONTHLY_FUNCTION")
      String groupType,
      @RequestParam(required = false) @Min(1) Long groupId,
      @RequestParam(required = false) @Pattern(regexp = "[A-Za-z0-9_-]{1,128}")
      String groupCode,
      @RequestParam(required = false)
      @Pattern(regexp = "SUCCESS|FAILED|DENIED")
      String resultCode,
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
      Authentication authentication) {
    Set<String> authorities = authentication.getAuthorities().stream()
        .map(authority -> authority.getAuthority())
        .collect(Collectors.toUnmodifiableSet());
    return auditLogApi.list(
        new AuditLogApi.AuditLogQuery(
            moduleCode, subjectType, subjectId, scopeStudyId,
            groupType, groupId, groupCode, resultCode, page, pageSize),
        authentication.getName(), authorities);
  }
}
