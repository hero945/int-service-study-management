package com.huadong.pipeline.service;

import java.util.Collection;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

@Component
public class RoleSessionInvalidator {
  private final FindByIndexNameSessionRepository<? extends Session> sessions;

  public RoleSessionInvalidator(
      FindByIndexNameSessionRepository<? extends Session> sessions) {
    this.sessions = sessions;
  }

  public int invalidate(Collection<String> usernames) {
    int invalidatedUsers = 0;
    for (String username : usernames) {
      var userSessions = sessions.findByPrincipalName(username);
      if (!userSessions.isEmpty()) {
        invalidatedUsers++;
        userSessions.keySet().forEach(sessions::deleteById);
      }
    }
    return invalidatedUsers;
  }
}
