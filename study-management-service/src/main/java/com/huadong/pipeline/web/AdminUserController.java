package com.huadong.pipeline.web;

import com.huadong.pipeline.api.UserApi;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/platform/users")
public class AdminUserController {
  private final UserApi userApi;

  public AdminUserController(UserApi userApi) {
    this.userApi = userApi;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('account.page.view')")
  List<UserApi.UserResponse> list(
      @RequestParam(defaultValue = "") String keyword,
      @RequestParam(defaultValue = "") String roleCode) {
    return userApi.list(keyword, roleCode);
  }

  @PostMapping
  @PreAuthorize("hasAuthority('account.create')")
  @ResponseStatus(HttpStatus.CREATED)
  void create(@Valid @RequestBody UserApi.CreateUserRequest request) {
    userApi.create(request);
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAuthority('account.update')")
  void update(
      @PathVariable @Min(1) long id,
      @Valid @RequestBody UserApi.UpdateUserRequest request,
      Principal principal) {
    userApi.update(id, request, principal.getName());
  }

  @PutMapping("/{id}/roles")
  @PreAuthorize("hasAuthority('account.assignRole')")
  void assignRoles(
      @PathVariable @Min(1) long id,
      @Valid @RequestBody UserApi.AssignRolesRequest request,
      Principal principal) {
    userApi.assignRoles(id, request, principal.getName());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('account.delete')")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  void delete(@PathVariable @Min(1) long id, Principal principal) {
    userApi.delete(id, principal.getName());
  }
}
