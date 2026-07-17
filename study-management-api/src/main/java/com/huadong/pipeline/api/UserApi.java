package com.huadong.pipeline.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public interface UserApi {
  CurrentUserResponse getCurrentUser(String username);

  List<UserResponse> list();

  void create(@Valid CreateUserRequest request);

  record CreateUserRequest(
      @NotBlank @Pattern(regexp = "[A-Za-z0-9._-]{3,64}") String username,
      @NotBlank @Size(max = 100) String displayName,
      @NotBlank @Size(min = 12, max = 128) String password,
      @NotBlank @Pattern(regexp = "ADMIN|USER") String role) {
  }

  record CurrentUserResponse(String username, String displayName, String role) {
  }

  record UserResponse(long id, String username, String displayName, String role, boolean enabled) {
  }
}
