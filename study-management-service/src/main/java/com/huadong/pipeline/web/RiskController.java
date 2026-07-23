package com.huadong.pipeline.web;

import com.huadong.pipeline.api.RiskApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/risk-management")
public class RiskController {
  private final RiskApi api;
  public RiskController(RiskApi api) { this.api = api; }

  @GetMapping("/risks")
  @PreAuthorize("hasAuthority('risk.read')")
  RiskApi.PageResponse list(@RequestParam(defaultValue = "") String query,
      @RequestParam(defaultValue = "") String functionCode,
      @RequestParam(defaultValue = "") String status,
      @RequestParam(defaultValue = "") String level,
      @RequestParam(required = false) Long studyId,
      @RequestParam(defaultValue = "updatedAt") String sortBy,
      @RequestParam(defaultValue = "desc") String sortOrder,
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
      Principal principal) {
    return api.list(principal.getName(), query, functionCode, status, level,
        studyId, sortBy, sortOrder, page, pageSize);
  }

  @GetMapping("/risks/{riskCode}")
  @PreAuthorize("hasAuthority('risk.read')")
  RiskApi.DetailResponse detail(@PathVariable String riskCode, Principal principal) {
    return api.detail(principal.getName(), riskCode);
  }

  @GetMapping("/form-options")
  @PreAuthorize("hasAuthority('risk.read')")
  RiskApi.FormOptionsResponse options(@RequestParam(required = false) @Min(1) Long studyId,
                                      Principal principal) {
    return api.formOptions(principal.getName(), studyId);
  }

  @PostMapping("/risks")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAuthority('risk.create')")
  RiskApi.DetailResponse create(@Valid @RequestBody RiskApi.CreateRequest request,
                                Principal principal) {
    return api.create(request, principal.getName());
  }

  @PatchMapping("/risks/{riskCode}")
  @PreAuthorize("hasAuthority('risk.update')")
  RiskApi.DetailResponse update(@PathVariable String riskCode,
      @Valid @RequestBody RiskApi.UpdateRequest request, Principal principal) {
    return api.update(riskCode, request, principal.getName());
  }

  @DeleteMapping("/risks/{riskCode}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAuthority('risk.delete')")
  void delete(@PathVariable String riskCode, @RequestParam @Min(0) long expectedVersion,
              Principal principal) {
    api.delete(riskCode, expectedVersion, principal.getName());
  }

  @PostMapping("/risks/{riskCode}/actions")
  @PreAuthorize("hasAuthority('risk.update')")
  RiskApi.DetailResponse addAction(@PathVariable String riskCode,
      @Valid @RequestBody RiskApi.ActionCreateRequest request, Principal principal) {
    return api.addAction(riskCode, request, principal.getName());
  }

  @PatchMapping("/risks/{riskCode}/actions/{actionId}")
  @PreAuthorize("hasAuthority('risk.update')")
  RiskApi.DetailResponse updateAction(@PathVariable String riskCode, @PathVariable long actionId,
      @Valid @RequestBody RiskApi.ActionUpdateRequest request, Principal principal) {
    return api.updateAction(riskCode, actionId, request, principal.getName());
  }

  @DeleteMapping("/risks/{riskCode}/actions/{actionId}")
  @PreAuthorize("hasAuthority('risk.update')")
  RiskApi.DetailResponse deleteAction(@PathVariable String riskCode, @PathVariable long actionId,
      @RequestParam @Min(0) long expectedVersion, Principal principal) {
    return api.deleteAction(riskCode, actionId, expectedVersion, principal.getName());
  }
}
