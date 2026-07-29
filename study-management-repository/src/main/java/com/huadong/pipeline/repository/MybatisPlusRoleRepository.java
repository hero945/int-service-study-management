package com.huadong.pipeline.repository;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huadong.pipeline.domain.role.Permission;
import com.huadong.pipeline.domain.role.Role;
import com.huadong.pipeline.domain.role.RolePage;
import com.huadong.pipeline.domain.role.RoleRepository;
import com.huadong.pipeline.domain.role.RoleStatus;
import com.huadong.pipeline.domain.role.UserRolePermission;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.repository.entity.PermissionEntity;
import com.huadong.pipeline.repository.entity.RoleEntity;
import com.huadong.pipeline.repository.mapper.PermissionMapper;
import com.huadong.pipeline.repository.mapper.RoleMapper;
import com.huadong.pipeline.repository.mapper.RoleSummaryRow;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusRoleRepository implements RoleRepository {
  @Autowired
  private RoleMapper roleMapper;
  @Autowired
  private PermissionMapper permissionMapper;

  @Override
  public RolePage findPage(int page, int pageSize, String keyword, RoleStatus status) {
    var statusCode = status == null ? null : status.name();
    var rows = roleMapper.findPage(keyword, statusCode, pageSize, (page - 1) * pageSize);
    Map<Long, List<String>> permissionCodesByRole = rows.isEmpty()
        ? Map.of()
        : roleMapper.findPermissionCodesByRoleIds(
                rows.stream().map(RoleSummaryRow::getId).toList()).stream()
            .collect(Collectors.groupingBy(
                row -> row.getRoleId(),
                Collectors.mapping(row -> row.getPermissionCode(), Collectors.toList())));
    var roles = rows.stream()
        .map(row -> toDomain(row, permissionCodesByRole.getOrDefault(row.getId(), List.of())))
        .toList();
    return new RolePage(roles, page, pageSize, roleMapper.countPage(keyword, statusCode));
  }

  @Override
  public Optional<Role> findById(long roleId) {
    var query = Wrappers.<RoleEntity>lambdaQuery()
        .eq(RoleEntity::getId, roleId)
        .eq(RoleEntity::getSysDeleted, 0);
    return Optional.ofNullable(roleMapper.selectOne(query)).map(this::toDomain);
  }

  @Override
  public List<Permission> findEnabledPermissions() {
    var query = Wrappers.<PermissionEntity>lambdaQuery()
        .eq(PermissionEntity::getStatusCode, "ACTIVE")
        .eq(PermissionEntity::getSysDeleted, 0)
        .orderByAsc(PermissionEntity::getModuleCode)
        .orderByAsc(PermissionEntity::getSortOrder)
        .orderByAsc(PermissionEntity::getPermissionCode);
    return permissionMapper.selectList(query).stream().map(MybatisPlusRoleRepository::toDomain).toList();
  }

  @Override
  public boolean codeExists(String roleCode) {
    return roleMapper.selectCount(Wrappers.<RoleEntity>lambdaQuery()
        .eq(RoleEntity::getRoleName, roleCode)) > 0;
  }

  @Override
  public boolean permissionsExist(List<String> permissionCodes) {
    return permissionMapper.selectCount(Wrappers.<PermissionEntity>lambdaQuery()
        .in(PermissionEntity::getPermissionCode, permissionCodes)
        .eq(PermissionEntity::getStatusCode, "ACTIVE")
        .eq(PermissionEntity::getSysDeleted, 0)) == permissionCodes.size();
  }

  @Override
  public Role create(
      String roleCode,
      String description,
      DataScope dataScope,
      List<String> permissionCodes,
      String operator) {
    var entity = new RoleEntity();
    entity.setRoleName(roleCode);
    entity.setRoleDescription(description);
    entity.setDataScopeMode(dataScope.name());
    entity.setStatusCode(RoleStatus.ACTIVE.name());
    entity.setIsSystemRole(0);
    entity.setSysCreateBy(operator);
    entity.setSysUpdateBy(operator);
    roleMapper.insert(entity);
    replacePermissions(entity.getId(), permissionCodes, operator);
    var created = findById(entity.getId()).orElseThrow();
    return created;
  }

  @Override
  public Role update(
      long roleId,
      String description,
      DataScope dataScope,
      RoleStatus status,
      List<String> permissionCodes,
      String operator) {
    var before = findById(roleId).orElseThrow();
    var entity = roleMapper.selectById(roleId);
    entity.setRoleDescription(description);
    entity.setDataScopeMode(dataScope.name());
    entity.setStatusCode(status.name());
    entity.setSysUpdateBy(operator);
    roleMapper.updateById(entity);
    replacePermissions(roleId, permissionCodes, operator);
    var updated = findById(roleId).orElseThrow();
    return updated;
  }

  @Override
  public void delete(long roleId, String operator) {
    var before = findById(roleId).orElseThrow();
    var entity = roleMapper.selectById(roleId);
    entity.setSysDeleted(1);
    entity.setSysUpdateBy(operator);
    roleMapper.updateById(entity);
    roleMapper.deletePermissions(roleId, operator);
  }

  @Override
  public List<String> findAssignedUsernames(long roleId) {
    return roleMapper.findAssignedUsernames(roleId);
  }

  @Override
  public List<UserRolePermission> findActiveUserRolePermissions() {
    return roleMapper.findActiveUserRolePermissions().stream()
        .map(row -> new UserRolePermission(
            row.getUserId(), row.getRoleId(), row.getPermissionCode()))
        .toList();
  }

  private void replacePermissions(long roleId, List<String> permissionCodes, String operator) {
    roleMapper.removeUnselectedPermissions(roleId, permissionCodes, operator);
    for (String permissionCode : permissionCodes) {
      if (roleMapper.restorePermission(roleId, permissionCode, operator) == 0) {
        roleMapper.insertPermission(roleId, permissionCode, operator);
      }
    }
  }

  private Role toDomain(RoleEntity entity) {
    long assignedUsers = roleMapper.findAssignedUsernames(entity.getId()).size();
    return new Role(
        entity.getId(), entity.getRoleName(), entity.getRoleDescription(),
        DataScope.valueOf(entity.getDataScopeMode()), RoleStatus.valueOf(entity.getStatusCode()),
        entity.getIsSystemRole() == 1, assignedUsers,
        roleMapper.findPermissionCodes(entity.getId()), entity.getSysUpdateTime());
  }

  private Role toDomain(RoleSummaryRow row, List<String> permissionCodes) {
    return new Role(
        row.getId(), row.getRoleName(), row.getRoleDescription(),
        DataScope.valueOf(row.getDataScopeMode()), RoleStatus.valueOf(row.getStatusCode()),
        row.getIsSystemRole() == 1, row.getAssignedUserCount(),
        permissionCodes, row.getSysUpdateTime());
  }

  private static Permission toDomain(PermissionEntity entity) {
    return new Permission(
        entity.getId(), entity.getModuleCode(), entity.getPermissionCode(),
        entity.getPermissionName(), entity.getPermissionType(), entity.getActionCode(),
        entity.getPermissionDescription(), entity.getSortOrder());
  }

}
