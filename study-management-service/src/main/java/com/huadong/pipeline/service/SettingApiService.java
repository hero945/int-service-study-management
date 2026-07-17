package com.huadong.pipeline.service;

import com.huadong.pipeline.api.SettingApi;
import com.huadong.pipeline.manager.SettingManager;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SettingApiService implements SettingApi {
  private final SettingManager manager;

  public SettingApiService(SettingManager manager) {
    this.manager = manager;
  }

  @Override
  public List<SettingResponse> listPublic() {
    return manager.listPublic().stream().map(SettingApiService::toResponse).toList();
  }

  @Override
  public List<SettingResponse> listAll() {
    return manager.listAll().stream().map(SettingApiService::toResponse).toList();
  }

  @Override
  public SettingResponse update(String key, UpdateSettingRequest request, String username) {
    return toResponse(manager.update(key, request.value(), username));
  }

  private static SettingResponse toResponse(SettingManager.SettingView setting) {
    return new SettingResponse(
        setting.configKey(),
        setting.configValue(),
        setting.description(),
        setting.publicVisible(),
        setting.updatedBy(),
        setting.updatedAt());
  }
}
