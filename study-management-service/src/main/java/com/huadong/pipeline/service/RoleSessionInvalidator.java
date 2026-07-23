package com.huadong.pipeline.service;

import java.util.Collection;
import java.util.List;
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

  /** Revoke all sessions for the user except one currently active session id. */
  public int invalidateOthers(String username, String keepSessionId) {
    var userSessions = sessions.findByPrincipalName(username);
    int deleted = 0;
    for (String sessionId : userSessions.keySet()) {
      if (keepSessionId != null && keepSessionId.equals(sessionId)) {
        continue;
      }
      sessions.deleteById(sessionId);
      deleted++;
    }
    return deleted;
  }

  /** Revoke every session for the user (used when current request session is unknown). */
  public int invalidateOthers(String username) {
    return invalidate(List.of(username));
  }
}
