package com.huadong.pipeline.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadong.pipeline.domain.user.UserAccount;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import com.huadong.pipeline.domain.user.DataScope;
import com.huadong.pipeline.repository.entity.UserAccountEntity;
import com.huadong.pipeline.repository.mapper.UserAccountMapper;
import com.huadong.pipeline.repository.mapper.UserAuthorizationRow;
import com.huadong.pipeline.repository.mapper.UserStudyCountRow;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusUserAccountRepository implements UserAccountRepository {
  private static final long LIST_LIMIT = 500;

  private final UserAccountMapper mapper;

  public MybatisPlusUserAccountRepository(UserAccountMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Optional<UserAccount> findByUsername(String username) {
    var query = Wrappers.<UserAccountEntity>lambdaQuery()
        .eq(UserAccountEntity::getEmail, username.toLowerCase())
        .eq(UserAccountEntity::getSysDeleted, 0);
    return Optional.ofNullable(mapper.selectOne(query))
        .map(this::toDomain);
  }

  @Override
  public Optional<UserAccount> findById(long id) {
    var query = Wrappers.<UserAccountEntity>lambdaQuery()
        .eq(UserAccountEntity::getId, id)
        .eq(UserAccountEntity::getSysDeleted, 0);
    return Optional.ofNullable(mapper.selectOne(query))
        .map(entity -> toDomainSingle(entity));
  }

  private UserAccount toDomainSingle(UserAccountEntity entity) {
    var authorization = new AuthorizationValues();
    mapper.findRoleCodes(entity.getId()).forEach(authorization.roles::add);
    mapper.findPermissionCodes(entity.getId()).forEach(authorization.permissions::add);
    var scopes = mapper.findDataScopes(entity.getId());
    authorization.hasAllScope = scopes.contains(DataScope.ALL.name());
    return toDomain(entity, authorization);
  }

  @Override
  public List<UserAccount> findAll() {
    return findAll(null, null);
  }

  @Override
  public List<UserAccount> findAll(String keyword, String roleCode) {
    var query = Wrappers.<UserAccountEntity>lambdaQuery()
        .eq(UserAccountEntity::getSysDeleted, 0)
        .orderByAsc(UserAccountEntity::getId);
    if (keyword != null && !keyword.isBlank()) {
      var term = keyword.trim().toLowerCase();
      query.and(w -> w
          .like(UserAccountEntity::getEmail, term)
          .or()
          .like(UserAccountEntity::getDisplayName, term));
    }
    if (roleCode != null && !roleCode.isBlank()) {
      var filteredUserIds = mapper.findUserIdsByRole(roleCode);
      if (filteredUserIds.isEmpty()) {
        return List.of();
      }
      query.in(UserAccountEntity::getId, filteredUserIds);
    }
    var records = mapper.selectPage(Page.of(1, LIST_LIMIT, false), query).getRecords();
    if (records.isEmpty()) {
      return List.of();
    }
    var authorizationByUserId = loadAuthorization(records);
    return records.stream()
        .map(entity -> toDomain(
            entity,
            authorizationByUserId.getOrDefault(entity.getId(), new AuthorizationValues())))
        .toList();
  }

  @Override
  public boolean rolesExist(List<String> roleCodes) {
    return !roleCodes.isEmpty() && mapper.countEnabledRoles(roleCodes) == roleCodes.size();
  }

  @Override
  public void create(
      String username,
      String passwordHash,
      String displayName,
      List<String> roleCodes) {
    var entity = new UserAccountEntity();
    var normalizedUsername = username.trim().toLowerCase();
    entity.setEmail(normalizedUsername);
    entity.setPasswordHash(passwordHash);
    entity.setDisplayName(displayName);
    entity.setStatusCode("ACTIVE");
    entity.setSecurityStamp(UUID.randomUUID().toString());
    entity.setSysCreateBy(normalizedUsername);
    entity.setSysUpdateBy(normalizedUsername);
    mapper.insert(entity);
    roleCodes.forEach(roleCode -> {
      if (mapper.insertUserRole(entity.getId(), roleCode, normalizedUsername) != 1) {
        throw new IllegalArgumentException("Unknown or disabled role: " + roleCode);
      }
    });
  }

  @Override
  public void update(long id, String displayName, boolean enabled, String operator) {
    var statusCode = enabled ? "ACTIVE" : "DISABLED";
    mapper.updateUser(id, displayName, statusCode, operator);
  }

  @Override
  public void softDelete(long id, String operator) {
    mapper.softDeleteUser(id, operator);
    mapper.deleteUserRoles(id, operator);
  }

  @Override
  public void assignRoles(long userId, List<String> roleCodes, String operator) {
    var distinctTarget = List.copyOf(new LinkedHashSet<>(roleCodes));
    var current = mapper.findActiveRoleCodes(userId);
    var currentSet = new LinkedHashSet<>(current);
    var targetSet = new LinkedHashSet<>(distinctTarget);
    current.stream()
        .filter(roleCode -> !targetSet.contains(roleCode))
        .forEach(roleCode -> mapper.softDeleteUserRole(userId, roleCode, operator));
    distinctTarget.stream()
        .filter(roleCode -> !currentSet.contains(roleCode))
        .forEach(roleCode -> {
          if (mapper.insertUserRole(userId, roleCode, operator) != 1) {
            throw new IllegalArgumentException("Unknown or disabled role: " + roleCode);
          }
        });
  }

  @Override
  public void updatePasswordHash(String username, String passwordHash, String operator) {
    int updated = mapper.updatePasswordHash(
        username.trim().toLowerCase(),
        passwordHash,
        UUID.randomUUID().toString(),
        operator);
    if (updated != 1) {
      throw new IllegalArgumentException("User not found: " + username);
    }
  }

  @Override
  public Map<Long, Long> countStudyAssignments(List<Long> userIds) {
    if (userIds.isEmpty()) {
      return Map.of();
    }
    return mapper.countStudyAssignmentsByUserIds(userIds).stream()
        .collect(Collectors.toMap(
            UserStudyCountRow::getUserId,
            UserStudyCountRow::getStudyCount));
  }

  private UserAccount toDomain(UserAccountEntity entity) {
    var scopes = mapper.findDataScopes(entity.getId());
    var authorization = new AuthorizationValues();
    mapper.findRoleCodes(entity.getId()).forEach(authorization.roles::add);
    mapper.findPermissionCodes(entity.getId()).forEach(authorization.permissions::add);
    authorization.hasAllScope = scopes.contains(DataScope.ALL.name());
    return toDomain(entity, authorization);
  }

  private static UserAccount toDomain(
      UserAccountEntity entity,
      AuthorizationValues authorization) {
    return new UserAccount(
        entity.getId(),
        entity.getEmail(),
        entity.getPasswordHash(),
        entity.getDisplayName(),
        List.copyOf(authorization.roles),
        List.copyOf(authorization.roleDescriptions),
        List.copyOf(authorization.permissions),
        authorization.hasAllScope ? DataScope.ALL : DataScope.ASSIGNED_STUDY,
        "ACTIVE".equals(entity.getStatusCode()));
  }

  private Map<Long, AuthorizationValues> loadAuthorization(List<UserAccountEntity> records) {
    var userIds = records.stream().map(UserAccountEntity::getId).toList();
    var authorizationByUserId = new HashMap<Long, AuthorizationValues>();
    for (UserAuthorizationRow row : mapper.findAuthorizationRows(userIds)) {
      var authorization = authorizationByUserId.computeIfAbsent(
          row.getUserId(), ignored -> new AuthorizationValues());
      authorization.roles.add(row.getRoleCode());
      authorization.roleDescriptions.add(row.getRoleDescription());
      if (row.getPermissionCode() != null) {
        authorization.permissions.add(row.getPermissionCode());
      }
      authorization.hasAllScope |= DataScope.ALL.name().equals(row.getDataScope());
    }
    return authorizationByUserId;
  }

  private static final class AuthorizationValues {
    private final TreeSet<String> roles = new TreeSet<>();
    private final LinkedHashSet<String> roleDescriptions = new LinkedHashSet<>();
    private final TreeSet<String> permissions = new TreeSet<>();
    private boolean hasAllScope;
  }
}
