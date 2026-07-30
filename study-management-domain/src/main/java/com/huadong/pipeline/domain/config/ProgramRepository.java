package com.huadong.pipeline.domain.config;

import java.util.List;
import java.util.Optional;

public interface ProgramRepository {
  List<Program> findAll(String keyword);
  Optional<Program> findById(long id);
  Optional<Program> findByCode(String code);
  Optional<Integer> findMaxVersionByCode(String code);
  boolean existsByProductName(String productName, Long excludingId);
  Program create(String code, String productName, String moa,
      String sourceCode, String originCode, int version, String username);
  void update(long id, String productName, String moa,
      String sourceCode, String originCode, int expectedVersion, String username);
  void softDelete(long id, String username);
}
