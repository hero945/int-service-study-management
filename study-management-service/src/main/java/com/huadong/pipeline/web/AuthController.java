package com.huadong.pipeline.web;

import com.huadong.pipeline.api.UserApi;
import com.huadong.pipeline.service.RoleSessionInvalidator;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform")
public class AuthController {
  private final UserApi userApi;
  private final RoleSessionInvalidator sessions;

  public AuthController(UserApi userApi, RoleSessionInvalidator sessions) {
    this.userApi = userApi;
    this.sessions = sessions;
  }

  @GetMapping("/auth/csrf")
  ResponseEntity<Map<String, String>> csrf(CsrfToken token) {
    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .body(Map.of(
            "headerName", token.getHeaderName(),
            "parameterName", token.getParameterName(),
            "token", token.getToken()));
  }

  @GetMapping("/me")
  UserApi.CurrentUserResponse me(Principal principal) {
    return userApi.getCurrentUser(principal.getName());
  }

  @PostMapping("/me/password")
  ResponseEntity<Void> changePassword(
      @Valid @RequestBody UserApi.ChangePasswordRequest request,
      Principal principal,
      HttpSession session) {
    userApi.changePassword(principal.getName(), request);
    sessions.invalidateOthers(principal.getName(), session.getId());
    return ResponseEntity.noContent().build();
  }
}
