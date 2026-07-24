package com.huadong.pipeline.manager;


import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.setting.SettingRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettingManager {
  @Autowired
  private SettingRepository settings;

  public List<SettingView> listPublic() {
    return settings.findPublic().stream().map(SettingView::from).toList();
  }

  public List<SettingView> listAll() {
    return settings.findAll().stream().map(SettingView::from).toList();
  }

  @Transactional
  public SettingView update(String key, String value, String username) {
    var existing = settings.findByKey(key)
        .orElseThrow(() -> new BusinessException("SETTING_NOT_FOUND", "配置项不存在"));
    settings.update(existing.configKey(), value, username);
    return settings.findByKey(key).map(SettingView::from).orElseThrow();
  }

  public record SettingView(
      String configKey,
      String configValue,
      String description,
      boolean publicVisible,
      String updatedBy,
      LocalDateTime updatedAt) {

    private static SettingView from(com.huadong.pipeline.domain.setting.Setting setting) {
      return new SettingView(
          setting.configKey(),
          setting.configValue(),
          setting.description(),
          setting.publicVisible(),
          setting.updatedBy(),
          setting.updatedAt());
    }
  }
}
