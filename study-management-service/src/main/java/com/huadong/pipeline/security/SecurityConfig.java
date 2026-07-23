package com.huadong.pipeline.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper mapper) throws Exception {
    http.authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/",
                "/login",
                "/index.html",
                "/assets/**",
                "/app.css",
                "/overrides.css",
                "/app.js",
                "/favicon.ico",
                "/favicon.svg",
                "/api/v1/platform/auth/csrf",
                "/api/v1/platform/settings/public",
                "/actuator/health/**",
                "/actuator/prometheus")
            .permitAll()
            .requestMatchers("/accounts")
            .hasAuthority("account.page.view")
            .requestMatchers("/roles")
            .hasAuthority("role.page.view")
            .requestMatchers("/team")
            .hasAuthority("team.page.view")
            .requestMatchers("/risks")
            .hasAuthority("risk.page.view")
            .anyRequest()
            .authenticated())
        .formLogin(form -> form
            .loginProcessingUrl("/api/v1/platform/auth/login")
            .successHandler((request, response, authentication) -> writeJson(
                response,
                mapper,
                200,
                Map.of("username", authentication.getName(), "message", "登录成功")))
            .failureHandler((request, response, exception) -> writeJson(
                response,
                mapper,
                401,
                Map.of("code", "AUTHENTICATION_FAILED", "message", "账号或密码错误"))))
        .logout(logout -> logout
            .logoutUrl("/api/v1/platform/auth/logout")
            .logoutSuccessHandler((request, response, authentication) -> writeJson(
                response,
                mapper,
                200,
                Map.of("message", "已安全退出")))
            .invalidateHttpSession(true)
            .deleteCookies("SESSION"))
        .exceptionHandling(errors -> errors
            .authenticationEntryPoint((request, response, exception) -> {
              if (isBrowserPageRequest(request)) {
                response.sendRedirect(loginRedirect(request));
                return;
              }
              writeJson(
                  response,
                  mapper,
                  401,
                  Map.of("code", "UNAUTHENTICATED", "message", "请先登录"));
            })
            .accessDeniedHandler((request, response, exception) -> writeJson(
                response,
                mapper,
                403,
                Map.of("code", "ACCESS_DENIED", "message", "无权执行此操作"))))
        .headers(headers -> headers
            .contentSecurityPolicy(csp -> csp.policyDirectives(
                "default-src 'self'; script-src 'self'; "
                    + "style-src 'self' https://fonts.googleapis.com; "
                    + "font-src 'self' https://fonts.gstatic.com data:; "
                    + "img-src 'self' data:; object-src 'none'; "
                    + "frame-ancestors 'none'; base-uri 'self'"))
            .frameOptions(frame -> frame.deny()))
        .requestCache(cache -> cache.disable())
        .csrf(csrf -> csrf.ignoringRequestMatchers("/actuator/**"));
    return http.build();
  }

  private static boolean isBrowserPageRequest(HttpServletRequest request) {
    String accept = request.getHeader("Accept");
    return "GET".equalsIgnoreCase(request.getMethod())
        && !request.getRequestURI().startsWith("/api/")
        && accept != null
        && accept.contains(MediaType.TEXT_HTML_VALUE);
  }

  private static String loginRedirect(HttpServletRequest request) {
    String target = request.getRequestURI();
    if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
      target += "?" + request.getQueryString();
    }
    return "/login?redirect=" + URLEncoder.encode(target, StandardCharsets.UTF_8);
  }

  private static void writeJson(
      HttpServletResponse response,
      ObjectMapper mapper,
      int status,
      Object value) throws java.io.IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    mapper.writeValue(response.getOutputStream(), value);
  }
}
