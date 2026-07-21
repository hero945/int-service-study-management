package com.huadong.pipeline.web;

import com.huadong.pipeline.api.SettingApi;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/settings")
public class SettingsController {
  private final SettingApi settingApi;

  public SettingsController(SettingApi settingApi) {
    this.settingApi = settingApi;
  }

  @GetMapping("/public")
  List<SettingApi.SettingResponse> publicSettings() {
    return settingApi.listPublic();
  }

  @GetMapping
  @PreAuthorize("hasAuthority('platform.setting.read')")
  List<SettingApi.SettingResponse> all() {
    return settingApi.listAll();
  }

  @PutMapping
  @PreAuthorize("hasAuthority('platform.setting.update')")
  SettingApi.SettingResponse update(
      @RequestParam String key,
      @Valid @RequestBody SettingApi.UpdateSettingRequest request,
      Principal principal) {
    return settingApi.update(key, request, principal.getName());
  }
}
