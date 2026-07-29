package com.huadong.pipeline.audit;

public final class AuditRequestContext {
  private static final ThreadLocal<AuditRequestMetadata> CURRENT = new ThreadLocal<>();

  private AuditRequestContext() {
  }

  public static void set(AuditRequestMetadata metadata) {
    CURRENT.set(metadata);
  }

  public static AuditRequestMetadata current() {
    return CURRENT.get();
  }

  public static void clear() {
    CURRENT.remove();
  }
}
