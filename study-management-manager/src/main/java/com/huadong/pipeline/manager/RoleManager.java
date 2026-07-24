package com.huadong.pipeline.manager;


import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.role.Permission;
import com.huadong.pipeline.domain.role.Role;
import com.huadong.pipeline.domain.role.RolePage;
import com.huadong.pipeline.domain.role.RoleRepository;
import com.huadong.pipeline.domain.role.RoleStatus;
import com.huadong.pipeline.domain.user.DataScope;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleManager {
  private static final Set<String> ROLE_ADMIN_PERMISSIONS = Set.of(
      "role.page.view", "role.create", "role.update", "role.delete");

  @Autowired
  private RoleRepository roles;

  public RolePage list(int page, int pageSize, String keyword, RoleStatus status) {
    return roles.findPage(page, pageSize, keyword, status);
  }

  public Role get(long roleId) {
    return roles.findById(roleId)
        .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND", "角色不存在"));
  }

  public List<Permission> listPermissions() {
    return roles.findEnabledPermissions();
  }

  @Transactional
  public Role create(
      String roleCode,
      String description,
      DataScope dataScope,
      List<String> permissionCodes,
      String operator) {
    var distinctPermissions = distinctPermissions(permissionCodes);
    if (roles.codeExists(roleCode)) {
      throw new BusinessException("ROLE_CODE_EXISTS", "角色编码已存在且不可复用");
    }
    validatePermissions(distinctPermissions);
    return roles.create(roleCode, description, dataScope, distinctPermissions, operator);
  }

  @Transactional
  public UpdateResult update(
      long roleId,
      String description,
      DataScope dataScope,
      RoleStatus status,
      List<String> permissionCodes,
      String operator) {
    var existing = get(roleId);
    if (existing.systemRole() && status != RoleStatus.ACTIVE) {
      throw new BusinessException("SYSTEM_ROLE_PROTECTED", "系统角色不可停用");
    }
    var distinctPermissions = distinctPermissions(permissionCodes);
    validatePermissions(distinctPermissions);
    if (!retainsRoleAdministrator(roleId, status, distinctPermissions)) {
      throw new BusinessException("LAST_ROLE_ADMIN_PROTECTED", "必须保留至少一个有效角色管理员");
    }
    boolean authorizationChanged = existing.dataScope() != dataScope
        || existing.status() != status
        || !Set.copyOf(existing.permissionCodes()).equals(Set.copyOf(distinctPermissions));
    var affectedUsers = authorizationChanged
        ? roles.findAssignedUsernames(roleId)
        : List.<String>of();
    var updated = roles.update(
        roleId, description, dataScope, status, distinctPermissions, operator);
    return new UpdateResult(updated, affectedUsers);
  }

  @Transactional
  public void delete(long roleId, String operator) {
    var role = get(roleId);
    if (role.systemRole()) {
      throw new BusinessException("SYSTEM_ROLE_PROTECTED", "系统角色不可删除");
    }
    if (role.assignedUserCount() > 0) {
      throw new BusinessException("ROLE_IN_USE", "角色仍关联用户，不能删除");
    }
    roles.delete(roleId, operator);
  }

  private void validatePermissions(List<String> permissionCodes) {
    if (permissionCodes.isEmpty() || !roles.permissionsExist(permissionCodes)) {
      throw new BusinessException("INVALID_PERMISSION", "权限不存在、已停用或未选择权限");
    }
  }

  private boolean retainsRoleAdministrator(
      long updatedRoleId,
      RoleStatus updatedStatus,
      List<String> updatedPermissionCodes) {
    Map<Long, Set<String>> permissionsByUser = new HashMap<>();
    Set<Long> usersAssignedToUpdatedRole = new HashSet<>();
    for (var row : roles.findActiveUserRolePermissions()) {
      if (row.roleId() == updatedRoleId) {
        usersAssignedToUpdatedRole.add(row.userId());
      } else if (row.permissionCode() != null) {
        permissionsByUser.computeIfAbsent(row.userId(), ignored -> new HashSet<>())
            .add(row.permissionCode());
      }
    }
    if (updatedStatus == RoleStatus.ACTIVE) {
      for (long userId : usersAssignedToUpdatedRole) {
        permissionsByUser.computeIfAbsent(userId, ignored -> new HashSet<>())
            .addAll(updatedPermissionCodes);
      }
    }
    return permissionsByUser.values().stream()
        .anyMatch(permissions -> permissions.containsAll(ROLE_ADMIN_PERMISSIONS));
  }

  private static List<String> distinctPermissions(List<String> permissionCodes) {
    return List.copyOf(new LinkedHashSet<>(permissionCodes));
  }

  public record UpdateResult(Role role, List<String> affectedUsernames) {
    public UpdateResult {
      affectedUsernames = List.copyOf(affectedUsernames);
    }
  }
}
