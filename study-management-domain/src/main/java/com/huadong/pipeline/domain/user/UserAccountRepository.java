package com.huadong.pipeline.domain.user;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserAccountRepository {
  Optional<UserAccount> findByUsername(String username);

  Optional<UserAccount> findById(long id);

  List<UserAccount> findAll();

  List<UserAccount> findAll(String keyword, String roleCode);

  boolean rolesExist(List<String> roleCodes);

  void create(String username, String passwordHash, String displayName, List<String> roleCodes);

  default void create(String username, String passwordHash, String displayName, String roleCode) {
    create(username, passwordHash, displayName, List.of(roleCode));
  }

  void update(long id, String displayName, boolean enabled, String operator);

  void softDelete(long id, String operator);

  void assignRoles(long userId, List<String> roleCodes, String operator);

  Map<Long, Long> countStudyAssignments(List<Long> userIds);
}
