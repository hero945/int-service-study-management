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
            user.permissions(),
            user.dataScope().name(),
            user.enabled()));
  }

  public List<UserView> list() {
    return users.findAll().stream()
        .map(user -> new UserView(
            user.id(),
            user.username(),
            user.displayName(),
            user.roles(),
            user.permissions(),
            user.dataScope().name(),
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
      List<String> permissions,
      String dataScope,
      boolean enabled) {
  }
}
