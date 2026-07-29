package com.huadong.pipeline.manager;

import com.huadong.pipeline.domain.audit.AuditEvent;
import com.huadong.pipeline.domain.audit.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditCommandManager {
  private final AuditLogRepository logs;

  public AuditCommandManager(AuditLogRepository logs) {
    this.logs = logs;
  }

  @Transactional
  public void record(AuditEvent event) {
    logs.insert(event);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void recordIndependent(AuditEvent event) {
    logs.insert(event);
  }
}
