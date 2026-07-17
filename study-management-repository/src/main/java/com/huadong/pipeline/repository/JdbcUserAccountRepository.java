package com.huadong.pipeline.repository;

import com.huadong.pipeline.domain.user.UserAccount;
import com.huadong.pipeline.domain.user.UserAccountRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcUserAccountRepository implements UserAccountRepository {
  private final JdbcClient jdbc;

  public JdbcUserAccountRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public Optional<UserAccount> findByUsername(String username) {
    return jdbc.sql("""
            SELECT id, username, password_hash, display_name, role, enabled
            FROM plt_user WHERE username = :username
            """)
        .param("username", username)
        .query(UserAccount.class)
        .optional();
  }

  @Override
  public List<UserAccount> findAll() {
    return jdbc.sql("""
            SELECT id, username, password_hash, display_name, role, enabled
            FROM plt_user ORDER BY id LIMIT 500
            """)
        .query(UserAccount.class)
        .list();
  }

  @Override
  public void create(String username, String passwordHash, String displayName, String role) {
    jdbc.sql("""
            INSERT INTO plt_user(username, password_hash, display_name, role, enabled)
            VALUES (:username, :passwordHash, :displayName, :role, TRUE)
            """)
        .param("username", username)
        .param("passwordHash", passwordHash)
        .param("displayName", displayName)
        .param("role", role)
        .update();
  }
}
