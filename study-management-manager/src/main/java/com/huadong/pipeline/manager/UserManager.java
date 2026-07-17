package com.huadong.pipeline.manager;

import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.user.UserAccountRepository;
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
            user.role(),
            user.enabled()));
  }

  public Optional<UserView> findByUsername(String username) {
    return users.findByUsername(username)
        .map(user -> new UserView(
            user.id(),
            user.username(),
            user.displayName(),
            user.role(),
            user.enabled()));
  }

  public List<UserView> list() {
    return users.findAll().stream()
        .map(user -> new UserView(
            user.id(),
            user.username(),
            user.displayName(),
            user.role(),
            user.enabled()))
        .toList();
  }

  @Transactional
  public void create(String username, String passwordHash, String displayName, String role) {
    if (users.findByUsername(username).isPresent()) {
      throw new BusinessException("USERNAME_EXISTS", "用户名已存在");
    }
    users.create(username, passwordHash, displayName, role);
  }

  public record AuthenticationUser(
      String username,
      String passwordHash,
      String displayName,
      String role,
      boolean enabled) {
  }

  public record UserView(
      long id,
      String username,
      String displayName,
      String role,
      boolean enabled) {
  }
}
