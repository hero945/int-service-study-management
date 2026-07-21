package com.huadong.pipeline.service;

import com.huadong.pipeline.api.UserApi;
import com.huadong.pipeline.manager.UserManager;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserApiService implements UserApi {
  private final UserManager manager;
  private final PasswordEncoder passwordEncoder;

  public UserApiService(UserManager manager, PasswordEncoder passwordEncoder) {
    this.manager = manager;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public CurrentUserResponse getCurrentUser(String username) {
    var user = manager.findForAuthentication(username).orElseThrow();
    return new CurrentUserResponse(
        user.username(),
        user.displayName(),
        user.roles(),
        user.permissions(),
        user.dataScope());
  }

  @Override
  public List<UserResponse> list(String keyword, String roleCode) {
    return manager.list(keyword, roleCode).stream()
        .map(user -> new UserResponse(
            user.id(),
            user.username(),
            user.displayName(),
            user.roles(),
            user.roleDescriptions(),
            user.dataScope(),
            user.visibleStudyCount(),
            user.enabled()))
        .toList();
  }

  @Override
  public void create(CreateUserRequest request) {
    manager.create(
        request.username(),
        passwordEncoder.encode(request.password()),
        request.displayName(),
        request.roleCodes());
  }

  @Override
  public void update(long id, UpdateUserRequest request, String operator) {
    manager.update(id, request.displayName(), request.enabled(), operator);
  }

  @Override
  public void delete(long id, String operator) {
    manager.softDelete(id, operator);
  }

  @Override
  public void assignRoles(long id, AssignRolesRequest request, String operator) {
    manager.assignRoles(id, request.roleCodes(), operator);
  }
}
