package com.huadong.pipeline.domain.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
  List<Project> findAll(Long programId, String keyword);
  Optional<Project> findById(long id);
  Optional<Project> findByCode(String code);
  Project create(String code, String name, long programId, String indication,
      String therapeuticAreaCode, String username);
  boolean update(long id, String name, String indication, String therapeuticAreaCode,
      LocalDateTime expectedUpdatedAt, String username);
  void softDelete(long id, String username);
}
