package com.huadong.pipeline.config;

import io.sentry.SentryOptions;
import io.sentry.protocol.Request;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentryConfig {

  private static final Set<String> REDACTED_HEADERS =
      Set.of(
          "authorization",
          "cookie",
          "set-cookie",
          "x-csrf-token",
          "x-xsrf-token");

  /**
   * Scrub auth/session headers before events leave the process.
   *
   * @see <a href="https://docs.sentry.io/platforms/java/guides/spring-boot/">Sentry Spring Boot</a>
   */
  @Bean
  SentryOptions.BeforeSendCallback sentryBeforeSendCallback() {
    return (event, hint) -> {
      Request request = event.getRequest();
      if (request == null) {
        return event;
      }
      Map<String, String> headers = request.getHeaders();
      if (headers == null || headers.isEmpty()) {
        return event;
      }
      headers
          .entrySet()
          .removeIf(entry -> REDACTED_HEADERS.contains(entry.getKey().toLowerCase(Locale.ROOT)));
      return event;
    };
  }
}
