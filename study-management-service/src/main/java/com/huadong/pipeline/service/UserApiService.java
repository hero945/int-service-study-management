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
    var user = manager.findByUsername(username).orElseThrow();
    return new CurrentUserResponse(
        user.username(),
        user.displayName(),
        user.roles(),
        user.permissions(),
        user.dataScope());
  }

  @Override
  public List<UserResponse> list() {
    return manager.list().stream()
        .map(user -> new UserResponse(
            user.id(),
            user.username(),
            user.displayName(),
            user.roles(),
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
}
