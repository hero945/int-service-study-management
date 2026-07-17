package com.huadong.pipeline.domain.setting;

import java.util.List;
import java.util.Optional;

public interface SettingRepository {
  List<Setting> findAll();

  List<Setting> findPublic();

  Optional<Setting> findByKey(String key);

  void update(String key, String value, String username);
}
