package com.huadong.pipeline.service;

import lombok.extern.slf4j.Slf4j;

import com.huadong.pipeline.api.UserApi;
import com.huadong.pipeline.audit.BusinessAuditService;
import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.manager.UserManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  @Autowired
  private BusinessAuditService audit;

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
  public UserPageResponse list(int page, int pageSize, String keyword, String roleCode) {
    var result = manager.list(page, pageSize, keyword, roleCode);
    long totalPages = result.totalItems() == 0
        ? 1
        : (result.totalItems() + result.pageSize() - 1) / result.pageSize();
    return new UserPageResponse(
        result.data().stream()
            .map(user -> new UserResponse(
                user.id(),
                user.username(),
                user.displayName(),
                user.roles(),
                user.roleDescriptions(),
                user.dataScope(),
                user.visibleStudyCount(),
                user.enabled()))
            .toList(),
        result.page(),
        result.pageSize(),
        result.totalItems(),
        totalPages);
  }

  @Override
  @Transactional
  public void create(CreateUserRequest request, String operator) {
    manager.create(
        request.username(),
        passwordEncoder.encode(DEFAULT_PASSWORD),
        request.displayName(),
        request.roleCodes());
    var created = manager.findByUsername(request.username()).orElseThrow();
    audit.success(
        "ACCOUNT", "USER", created.id(), created.username(), null,
        "USER_CREATE", "hd_plt_user", created.id(), null, created, null, operator);
    log.info(
        "账号创建 action=create username={} roleCodes={}",
        request.username(),
        request.roleCodes());
  }

  @Override
  @Transactional
  public void update(long id, UpdateUserRequest request, String operator) {
    var before = manager.findById(id)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    manager.update(id, request.displayName(), request.enabled(), operator);
    var after = manager.findById(id).orElseThrow();
    audit.success(
        "ACCOUNT", "USER", id, after.username(), null,
        "USER_UPDATE", "hd_plt_user", id, before, after, null, operator);
    log.info(
        "账号更新 operator={} targetUserId={} enabled={}",
        operator,
        id,
        request.enabled());
  }

  @Override
  @Transactional
  public void delete(long id, String operator) {
    var before = manager.findById(id)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    manager.softDelete(id, operator);
    audit.success(
        "ACCOUNT", "USER", id, before.username(), null,
        "USER_DELETE", "hd_plt_user", id, before, java.util.Map.of("deleted", true),
        null, operator);
    log.info("账号删除 operator={} targetUserId={}", operator, id);
  }

  @Override
  @Transactional
  public void assignRoles(long id, AssignRolesRequest request, String operator) {
    var before = manager.findById(id)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    manager.assignRoles(id, request.roleCodes(), operator);
    var after = manager.findById(id).orElseThrow();
    audit.success(
        "ACCOUNT", "USER", id, after.username(), null,
        "USER_ROLE_ASSIGN", "hd_plt_user_role", id, before, after, null, operator);
    log.info(
        "账号角色分配 operator={} targetUserId={} roleCodes={}",
        operator,
        id,
        request.roleCodes());
  }

  @Override
  @Transactional
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
    var changedUser = manager.findByUsername(username).orElseThrow();
    audit.success(
        "ACCOUNT", "USER", changedUser.id(), user.username(), null,
        "PASSWORD_CHANGE", "hd_plt_user", changedUser.id(), null,
        java.util.Map.of("passwordChanged", true), null, username);
    log.info("账号修改密码 username={}", username);
  }

  @Override
  @Transactional
  public void resetPassword(long id, String operator) {
    var user = manager.findById(id)
        .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "用户不存在"));
    manager.updatePasswordHash(user.username(), passwordEncoder.encode(DEFAULT_PASSWORD));
    sessions.invalidate(List.of(user.username()));
    audit.success(
        "ACCOUNT", "USER", id, user.username(), null,
        "PASSWORD_RESET", "hd_plt_user", id, null,
        java.util.Map.of("passwordReset", true), null, operator);
    log.info("账号重置密码 operator={} targetUserId={}", operator, id);
  }
}
