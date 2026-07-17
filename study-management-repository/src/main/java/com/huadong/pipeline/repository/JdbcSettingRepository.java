package com.huadong.pipeline.repository;

import com.huadong.pipeline.domain.setting.Setting;
import com.huadong.pipeline.domain.setting.SettingRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcSettingRepository implements SettingRepository {
  private final JdbcClient jdbc;

  public JdbcSettingRepository(JdbcClient jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<Setting> findAll() {
    return jdbc.sql("""
            SELECT config_key, config_value, description, public_visible, updated_by, updated_at
            FROM plt_system_setting ORDER BY config_key LIMIT 100
            """)
        .query(Setting.class)
        .list();
  }

  @Override
  public List<Setting> findPublic() {
    return jdbc.sql("""
            SELECT config_key, config_value, description, public_visible, updated_by, updated_at
            FROM plt_system_setting
            WHERE public_visible = TRUE
            ORDER BY config_key LIMIT 100
            """)
        .query(Setting.class)
        .list();
  }

  @Override
  public Optional<Setting> findByKey(String key) {
    return jdbc.sql("""
            SELECT config_key, config_value, description, public_visible, updated_by, updated_at
            FROM plt_system_setting WHERE config_key = :key
            """)
        .param("key", key)
        .query(Setting.class)
        .optional();
  }

  @Override
  public void update(String key, String value, String username) {
    jdbc.sql("""
            UPDATE plt_system_setting
            SET config_value = :value, updated_by = :username, updated_at = CURRENT_TIMESTAMP
            WHERE config_key = :key
            """)
        .param("key", key)
        .param("value", value)
        .param("username", username)
        .update();
  }
}
