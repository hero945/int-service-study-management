package com.huadong.pipeline.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public interface UserApi {
  CurrentUserResponse getCurrentUser(String username);

  List<UserResponse> list();

  void create(@Valid CreateUserRequest request);

  record CreateUserRequest(
      @NotBlank @Email @Size(max = 254) String username,
      @NotBlank @Size(max = 100) String displayName,
      @NotBlank @Size(min = 12, max = 128) String password,
      @NotEmpty @Size(max = 10)
      List<@NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,63}") String> roleCodes) {
  }

  record CurrentUserResponse(
      String username,
      String displayName,
      List<String> roles,
      List<String> permissions,
      String dataScope) {
  }

  record UserResponse(
      long id,
      String username,
      String displayName,
      List<String> roles,
      boolean enabled) {
  }
}
