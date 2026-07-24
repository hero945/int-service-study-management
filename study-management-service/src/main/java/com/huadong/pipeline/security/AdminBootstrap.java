package com.huadong.pipeline.security;

import lombok.extern.slf4j.Slf4j;

import com.huadong.pipeline.manager.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.List;

@Component
@Slf4j
public class AdminBootstrap implements ApplicationRunner {

  @Autowired
  private BootstrapProperties properties;
  @Autowired
  private UserManager users;
  @Autowired
  private PasswordEncoder encoder;

  @Override
  public void run(ApplicationArguments args) {
    if (!StringUtils.hasText(properties.adminUsername())
        || !StringUtils.hasText(properties.adminPassword())) {
      log.warn("未配置首个管理员；新环境需设置 BOOTSTRAP_ADMIN_USERNAME 和 BOOTSTRAP_ADMIN_PASSWORD");
      return;
    }
    if (properties.adminPassword().length() < 12) {
      throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD 至少需要 12 位");
    }
    if (users.findByUsername(properties.adminUsername()).isEmpty()) {
      users.create(
          properties.adminUsername(),
          encoder.encode(properties.adminPassword()),
          properties.adminDisplayName(),
          List.of("ADMIN"));
      log.info("已创建首个管理员账号：{}；请在首次登录后移除引导密码", properties.adminUsername());
    }
  }
}
