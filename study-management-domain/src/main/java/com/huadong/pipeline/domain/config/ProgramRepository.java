package com.huadong.pipeline.domain.config;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository {
  List<Program> findAll(String keyword);
  Optional<Program> findById(long id);
  Optional<Program> findByCode(String code);
  boolean existsByProductName(String productName, Long excludingId);
  Program create(String code, String productName, String moa,
      String sourceCode, String originCode, String username);
  void update(long id, String productName, String moa,
      String sourceCode, String originCode, String username);
  void softDelete(long id, String username);
}
