package com.huadong.pipeline.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@Slf4j
public class AuditRequestContextFilter extends OncePerRequestFilter {
  public static final String REQUEST_ID_HEADER = "X-Request-ID";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String requestId = UUID.randomUUID().toString();
    AuditRequestContext.set(new AuditRequestMetadata(
        requestId, clientIp(request), request.getMethod(), request.getRequestURI()));
    response.setHeader(REQUEST_ID_HEADER, requestId);
    try {
      filterChain.doFilter(request, response);
    } finally {
      AuditRequestContext.clear();
    }
  }

  private static String clientIp(HttpServletRequest request) {
    String remote = request.getRemoteAddr();
    if (isLoopback(remote)) {
      String forwarded = request.getHeader("X-Forwarded-For");
      if (forwarded != null && !forwarded.isBlank()) {
        return forwarded.split(",", 2)[0].trim();
      }
    }
    return remote;
  }

  private static boolean isLoopback(String address) {
    try {
      return InetAddress.getByName(address).isLoopbackAddress();
    } catch (Exception ex) {
      log.debug("loopback 地址检测失败 address={}", address, ex);
      return false;
    }
  }
}
