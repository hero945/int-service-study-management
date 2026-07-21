package com.huadong.pipeline.domain.user;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository {
  Optional<UserAccount> findByUsername(String username);

  List<UserAccount> findAll();

  boolean rolesExist(List<String> roleCodes);

  void create(String username, String passwordHash, String displayName, List<String> roleCodes);

  default void create(String username, String passwordHash, String displayName, String roleCode) {
    create(username, passwordHash, displayName, List.of(roleCode));
  }
}
