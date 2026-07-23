package com.huadong.pipeline.web;

import com.huadong.pipeline.api.UserApi;
import java.security.Principal;
import java.util.Map;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform")
public class AuthController {
  private final UserApi userApi;
  private final UserDetailsService userDetailsService;

  public AuthController(UserApi userApi, UserDetailsService userDetailsService) {
    this.userApi = userApi;
    this.userDetailsService = userDetailsService;
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
    // /me 从库重读权限；同步刷新 Session 内 SecurityContext，
    // 避免前端已有新权限、@PreAuthorize 仍用登录时旧权限。
    refreshAuthorities(principal.getName());
    return userApi.getCurrentUser(principal.getName());
  }

  private void refreshAuthorities(String username) {
    var current = SecurityContextHolder.getContext().getAuthentication();
    if (current == null || !current.isAuthenticated()) {
      return;
    }
    var fresh = userDetailsService.loadUserByUsername(username);
    var updated = new UsernamePasswordAuthenticationToken(
        fresh, current.getCredentials(), fresh.getAuthorities());
    updated.setDetails(current.getDetails());
    SecurityContextHolder.getContext().setAuthentication(updated);
  }
}
