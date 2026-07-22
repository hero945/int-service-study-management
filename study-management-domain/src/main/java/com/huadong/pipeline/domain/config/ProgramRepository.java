package com.huadong.pipeline.domain.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProgramRepository {
  List<Program> findAll(String keyword);
  Optional<Program> findById(long id);
  Optional<Program> findByCode(String code);
  boolean existsByProductName(String productName, Long excludingId);
  Program create(String code, String name, String productName, String moa,
      String sourceCode, String originCode, String username);
  boolean update(long id, String name, String productName, String moa,
      String sourceCode, String originCode, LocalDateTime expectedUpdatedAt, String username);
  void softDelete(long id, String username);
}
