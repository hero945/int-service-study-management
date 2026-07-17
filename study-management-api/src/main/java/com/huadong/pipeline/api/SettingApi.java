package com.huadong.pipeline.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

public interface SettingApi {
  List<SettingResponse> listPublic();

  List<SettingResponse> listAll();

  SettingResponse update(String key, @Valid UpdateSettingRequest request, String username);

  record UpdateSettingRequest(@NotBlank @Size(max = 1000) String value) {
  }

  record SettingResponse(
      String configKey,
      String configValue,
      String description,
      boolean publicVisible,
      String updatedBy,
      LocalDateTime updatedAt) {
  }
}
