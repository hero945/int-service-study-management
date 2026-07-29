package com.huadong.pipeline.service;


import com.huadong.pipeline.api.RoleApi;
import com.huadong.pipeline.audit.BusinessAuditService;
import com.huadong.pipeline.domain.role.Permission;
import com.huadong.pipeline.domain.role.Role;
import com.huadong.pipeline.domain.role.RoleStatus;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.manager.RoleManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleApiService implements RoleApi {
  @Autowired
  private RoleManager manager;
  @Autowired
  private RoleSessionInvalidator sessions;
  @Autowired
  private BusinessAuditService audit;

  @Override
  public RolePageResponse list(int page, int pageSize, String keyword, String status) {
    var roleStatus = status == null || status.isBlank() ? null : RoleStatus.valueOf(status);
    var result = manager.list(page, pageSize, keyword == null ? "" : keyword.trim(), roleStatus);
    long totalPages = (result.totalItems() + pageSize - 1) / pageSize;
    return new RolePageResponse(
        result.data().stream().map(RoleApiService::toResponse).toList(),
        result.page(), result.pageSize(), result.totalItems(), totalPages);
  }

  @Override
  public RoleResponse get(long roleId) {
    return toResponse(manager.get(roleId));
  }

  @Override
  public List<PermissionResponse> listPermissions() {
    return manager.listPermissions().stream().map(RoleApiService::toResponse).toList();
  }

  @Override
  @Transactional
  public RoleResponse create(CreateRoleRequest request, String operator) {
    var created = toResponse(manager.create(
        request.roleCode(), request.roleDescription(),
        DataScope.valueOf(request.dataScopeMode()), request.permissionCodes(), operator));
    audit.success(
        "ROLE", "ROLE", created.id(), created.roleCode(), null,
        "ROLE_CREATE", "hd_plt_role", created.id(), null, created, null, operator);
    return created;
  }

  @Override
  @Transactional
  public UpdateRoleResult update(long roleId, UpdateRoleRequest request, String operator) {
    var before = toResponse(manager.get(roleId));
    var result = manager.update(
        roleId, request.roleDescription(), DataScope.valueOf(request.dataScopeMode()),
        RoleStatus.valueOf(request.status()), request.permissionCodes(), operator);
    int invalidatedUsers = sessions.invalidate(result.affectedUsernames());
    var response = new UpdateRoleResult(
        toResponse(result.role()), invalidatedUsers, result.affectedUsernames().contains(operator));
    audit.success(
        "ROLE", "ROLE", roleId, response.role().roleCode(), null,
        "ROLE_UPDATE", "hd_plt_role", roleId, before, response.role(), null, operator);
    return response;
  }

  @Override
  @Transactional
  public void delete(long roleId, String operator) {
    var before = toResponse(manager.get(roleId));
    manager.delete(roleId, operator);
    audit.success(
        "ROLE", "ROLE", roleId, before.roleCode(), null,
        "ROLE_DELETE", "hd_plt_role", roleId, before, java.util.Map.of("deleted", true),
        null, operator);
  }

  private static RoleResponse toResponse(Role role) {
    return new RoleResponse(
        role.id(), role.code(), role.description(), role.dataScope().name(), role.status().name(),
        role.systemRole(), role.assignedUserCount(), role.permissionCodes(), role.updatedAt());
  }

  private static PermissionResponse toResponse(Permission permission) {
    return new PermissionResponse(
        permission.id(), permission.moduleCode(), permission.code(), permission.name(),
        permission.type(), permission.actionCode(), permission.description(), permission.sortOrder());
  }
}
