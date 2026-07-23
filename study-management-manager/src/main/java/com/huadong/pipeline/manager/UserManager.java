package com.huadong.pipeline.manager;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserManager {
  private final UserAccountRepository users;

  public UserManager(UserAccountRepository users) {
    this.users = users;
  }

  public Optional<AuthenticationUser> findForAuthentication(String username) {
    return users.findByUsername(username)
        .map(user -> new AuthenticationUser(
            user.username(),
            user.passwordHash(),
            user.displayName(),
            user.roles(),
            user.permissions(),
            user.dataScope().name(),
            user.enabled()));
  }

  public Optional<UserView> findByUsername(String username) {
    return users.findByUsername(username)
        .map(user -> new UserView(
            user.id(),
            user.username(),
            user.displayName(),
            user.roles(),
            user.roleDescriptions(),
            user.dataScope().name(),
            0,
            user.enabled()));
  }

  public Optional<UserView> findById(long id) {
    return users.findById(id)
        .map(user -> new UserView(
            user.id(),
            user.username(),
            user.displayName(),
            user.roles(),
            user.roleDescriptions(),
            user.dataScope().name(),
            0,
            user.enabled()));
  }

  public List<UserView> list() {
    return list(null, null);
  }

  public List<UserView> list(String keyword, String roleCode) {
    var accounts = users.findAll(keyword, roleCode);
    if (accounts.isEmpty()) {
      return List.of();
    }
    var userIds = accounts.stream().map(u -> u.id()).toList();
    var studyCounts = users.countStudyAssignments(userIds);
    return accounts.stream()
        .map(user -> new UserView(
            user.id(),
            user.username(),
            user.displayName(),
            user.roles(),
            user.roleDescriptions(),
            user.dataScope().name(),
            studyCounts.getOrDefault(user.id(), 0L),
            user.enabled()))
        .toList();
  }

  @Transactional
  public void create(
      String username,
      String passwordHash,
      String displayName,
      List<String> roleCodes) {
    if (users.findByUsername(username).isPresent()) {
      throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
    }
    var distinctRoleCodes = List.copyOf(new LinkedHashSet<>(roleCodes));
    if (distinctRoleCodes.isEmpty() || !users.rolesExist(distinctRoleCodes)) {
      throw new BusinessException("INVALID_ROLE", "Role does not exist or is disabled");
    }
    users.create(username, passwordHash, displayName, distinctRoleCodes);
  }

  @Transactional
  public void update(long id, String displayName, boolean enabled, String operator) {
    var existing = users.findById(id)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    users.update(id, displayName, enabled, operator);
  }

  @Transactional
  public void softDelete(long id, String operator) {
    var existing = users.findById(id)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    users.softDelete(id, operator);
  }

  @Transactional
  public void assignRoles(long userId, List<String> roleCodes, String operator) {
    var existing = users.findById(userId)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    var distinctRoleCodes = List.copyOf(new LinkedHashSet<>(roleCodes));
    if (distinctRoleCodes.isEmpty() || !users.rolesExist(distinctRoleCodes)) {
      throw new BusinessException("INVALID_ROLE", "Role does not exist or is disabled");
    }
    users.assignRoles(userId, distinctRoleCodes, operator);
  }

  @Transactional
  public void updatePasswordHash(String username, String passwordHash) {
    var existing = users.findByUsername(username)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    users.updatePasswordHash(existing.username(), passwordHash, existing.username());
  }

  public record AuthenticationUser(
      String username,
      String passwordHash,
      String displayName,
      List<String> roles,
      List<String> permissions,
      String dataScope,
      boolean enabled) {
  }

  public record UserView(
      long id,
      String username,
      String displayName,
      List<String> roles,
      List<String> roleDescriptions,
      String dataScope,
      long visibleStudyCount,
      boolean enabled) {
  }

}
