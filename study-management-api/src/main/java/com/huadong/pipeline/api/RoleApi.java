package com.huadong.pipeline.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public interface RoleApi {
  RolePageResponse list(int page, int pageSize, String keyword, String status);

  RoleResponse get(long roleId);

  List<PermissionResponse> listPermissions();

  RoleResponse create(@Valid CreateRoleRequest request, String operator);

  UpdateRoleResult update(long roleId, @Valid UpdateRoleRequest request, String operator);

  void delete(long roleId, String operator);

  record CreateRoleRequest(
      @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,63}") String roleCode,
      @Size(max = 500) String roleDescription,
      @NotBlank @Pattern(regexp = "ALL|ASSIGNED_STUDY") String dataScopeMode,
      @NotEmpty @Size(max = 200) List<@NotBlank @Size(max = 128) String> permissionCodes) {
  }

  record UpdateRoleRequest(
      @Size(max = 500) String roleDescription,
      @NotBlank @Pattern(regexp = "ALL|ASSIGNED_STUDY") String dataScopeMode,
      @NotBlank @Pattern(regexp = "ACTIVE|DISABLED") String status,
      @NotEmpty @Size(max = 200) List<@NotBlank @Size(max = 128) String> permissionCodes) {
  }

  record RoleResponse(
      long id,
      String roleCode,
      String roleDescription,
      String dataScopeMode,
      String status,
      boolean systemRole,
      long assignedUserCount,
      List<String> permissionCodes,
      LocalDateTime updatedAt) {
  }

  record PermissionResponse(
      long id,
      String moduleCode,
      String permissionCode,
      String permissionName,
      String permissionType,
      String actionCode,
      String permissionDescription,
      int sortOrder) {
  }

  record RolePageResponse(
      List<RoleResponse> data,
      int page,
      int pageSize,
      long totalItems,
      long totalPages) {
  }

  record UpdateRoleResult(
      RoleResponse role,
      int invalidatedUserCount,
      boolean currentSessionInvalidated) {
  }
}
