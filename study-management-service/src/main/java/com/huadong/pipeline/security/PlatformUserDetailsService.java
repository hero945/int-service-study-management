package com.huadong.pipeline.security;


import com.huadong.pipeline.manager.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PlatformUserDetailsService implements UserDetailsService {
  @Autowired
  private UserManager users;

  @Override
  public UserDetails loadUserByUsername(String username) {
    var account = users.findForAuthentication(username)
        .orElseThrow(() -> new UsernameNotFoundException("账号或密码错误"));
    return User.withUsername(account.username())
        .password(account.passwordHash())
        .authorities(account.permissions().toArray(String[]::new))
        .disabled(!account.enabled())
        .build();
  }
}
