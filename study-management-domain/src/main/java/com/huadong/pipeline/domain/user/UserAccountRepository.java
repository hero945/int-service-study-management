package com.huadong.pipeline.domain.user;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository {
  Optional<UserAccount> findByUsername(String username);

  List<UserAccount> findAll();

  void create(String username, String passwordHash, String displayName, String role);
}
