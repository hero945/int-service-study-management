package com.huadong.pipeline.web;

import com.huadong.pipeline.api.RoleApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/platform")
public class RoleController {
  private final RoleApi roleApi;

  public RoleController(RoleApi roleApi) {
    this.roleApi = roleApi;
  }

  @GetMapping("/roles")
  @PreAuthorize("hasAuthority('role.page.view')")
  RoleApi.RolePageResponse list(
      @RequestParam(defaultValue = "1") @Min(1) int page,
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int pageSize,
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(required = false) @Pattern(regexp = "ACTIVE|DISABLED") String status) {
    return roleApi.list(page, pageSize, keyword, status);
  }

  @GetMapping("/roles/{roleId}")
  @PreAuthorize("hasAuthority('role.page.view')")
  RoleApi.RoleResponse get(@PathVariable @Min(1) long roleId) {
    return roleApi.get(roleId);
  }

  @GetMapping("/permissions")
  @PreAuthorize("hasAuthority('role.page.view')")
  List<RoleApi.PermissionResponse> permissions() {
    return roleApi.listPermissions();
  }

  @PostMapping("/roles")
  @PreAuthorize("hasAuthority('role.create')")
  @ResponseStatus(HttpStatus.CREATED)
  RoleApi.RoleResponse create(
      @Valid @RequestBody RoleApi.CreateRoleRequest request,
      Principal principal) {
    return roleApi.create(request, principal.getName());
  }

  @PutMapping("/roles/{roleId}")
  @PreAuthorize("hasAuthority('role.update')")
  RoleApi.UpdateRoleResult update(
      @PathVariable @Min(1) long roleId,
      @Valid @RequestBody RoleApi.UpdateRoleRequest request,
      Principal principal) {
    return roleApi.update(roleId, request, principal.getName());
  }

  @DeleteMapping("/roles/{roleId}")
  @PreAuthorize("hasAuthority('role.delete')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable @Min(1) long roleId, Principal principal) {
    roleApi.delete(roleId, principal.getName());
  }
}
