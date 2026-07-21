package com.huadong.pipeline.domain.role;

import com.huadong.pipeline.domain.user.DataScope;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {
  RolePage findPage(int page, int pageSize, String keyword, RoleStatus status);

  Optional<Role> findById(long roleId);

  List<Permission> findEnabledPermissions();

  boolean codeExists(String roleCode);

  boolean permissionsExist(List<String> permissionCodes);

  Role create(
      String roleCode,
      String description,
      DataScope dataScope,
      List<String> permissionCodes,
      String operator);

  Role update(
      long roleId,
      String description,
      DataScope dataScope,
      RoleStatus status,
      List<String> permissionCodes,
      String operator);

  void delete(long roleId, String operator);

  List<String> findAssignedUsernames(long roleId);

  List<UserRolePermission> findActiveUserRolePermissions();
}
