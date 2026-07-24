package com.huadong.pipeline.service;

import lombok.extern.slf4j.Slf4j;

import com.huadong.pipeline.api.UserApi;
import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.manager.UserManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserApiService implements UserApi {
  static final String DEFAULT_PASSWORD = "Hd123456";

  @Autowired
  private UserManager manager;
  @Autowired
  private PasswordEncoder passwordEncoder;
  @Autowired
  private RoleSessionInvalidator sessions;

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
        passwordEncoder.encode(DEFAULT_PASSWORD),
        request.displayName(),
        request.roleCodes());
    log.info(
        "账号创建 action=create username={} roleCodes={}",
        request.username(),
        request.roleCodes());
  }

  @Override
  public void update(long id, UpdateUserRequest request, String operator) {
    manager.update(id, request.displayName(), request.enabled(), operator);
    log.info(
        "账号更新 operator={} targetUserId={} enabled={}",
        operator,
        id,
        request.enabled());
  }

  @Override
  public void delete(long id, String operator) {
    manager.softDelete(id, operator);
    log.info("账号删除 operator={} targetUserId={}", operator, id);
  }

  @Override
  public void assignRoles(long id, AssignRolesRequest request, String operator) {
    manager.assignRoles(id, request.roleCodes(), operator);
    log.info(
        "账号角色分配 operator={} targetUserId={} roleCodes={}",
        operator,
        id,
        request.roleCodes());
  }

  @Override
  public void changePassword(String username, ChangePasswordRequest request) {
    var user = manager.findForAuthentication(username)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    if (!passwordEncoder.matches(request.currentPassword(), user.passwordHash())) {
      throw new BusinessException("INVALID_CURRENT_PASSWORD", "当前密码不正确");
    }
    if (request.currentPassword().equals(request.newPassword())) {
      throw new BusinessException("PASSWORD_UNCHANGED", "新密码不能与当前密码相同");
    }
    manager.updatePasswordHash(username, passwordEncoder.encode(request.newPassword()));
    log.info("账号修改密码 username={}", username);
  }

  @Override
  public void resetPassword(long id, String operator) {
    var user = manager.findById(id)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    manager.updatePasswordHash(user.username(), passwordEncoder.encode(DEFAULT_PASSWORD));
    sessions.invalidate(List.of(user.username()));
    log.info("账号重置密码 operator={} targetUserId={}", operator, id);
  }
}
