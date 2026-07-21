package com.huadong.pipeline.web;

import com.huadong.pipeline.api.UserApi;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
  List<UserApi.UserResponse> list() {
    return userApi.list();
  }

  @PostMapping
  @PreAuthorize("hasAuthority('account.create')")
  @ResponseStatus(HttpStatus.CREATED)
  void create(@Valid @RequestBody UserApi.CreateUserRequest request) {
    userApi.create(request);
  }
}
