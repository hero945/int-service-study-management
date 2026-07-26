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

  UserPageResponse list(int page, int pageSize, String keyword, String roleCode);

  void create(@Valid CreateUserRequest request);

  void update(long id, @Valid UpdateUserRequest request, String operator);

  void delete(long id, String operator);

  void assignRoles(long id, @Valid AssignRolesRequest request, String operator);

  void changePassword(String username, @Valid ChangePasswordRequest request);

  void resetPassword(long id, String operator);

  record CreateUserRequest(
      @NotBlank @Email @Size(max = 254) String username,
      @NotBlank @Size(max = 100) String displayName,
      @NotEmpty @Size(max = 10)
      List<@NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,63}") String> roleCodes) {
  }

  record UpdateUserRequest(
      @NotBlank @Size(max = 100) String displayName,
      boolean enabled) {
  }

  record AssignRolesRequest(
      @NotEmpty @Size(max = 10)
      List<@NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]{1,63}") String> roleCodes) {
  }

  record ChangePasswordRequest(
      @NotBlank @Size(max = 128) String currentPassword,
      @NotBlank
      @Size(min = 8, max = 128)
      @Pattern(
          regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,128}$",
          message = "新密码至少 8 位，且须包含大写字母、小写字母和数字")
      String newPassword) {
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
      List<String> roleDescriptions,
      String dataScope,
      long visibleStudyCount,
      boolean enabled) {
  }

  record UserPageResponse(
      List<UserResponse> data,
      int page,
      int pageSize,
      long totalItems,
      long totalPages) {
  }
}
