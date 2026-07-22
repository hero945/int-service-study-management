package com.huadong.pipeline.domain.config;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
  List<Project> findAll(Long programId, String keyword);
  Optional<Project> findById(long id);
  Optional<Project> findByCode(String code);
  Project create(String code, long programId, String indication,
      String therapeuticAreaCode, String username);
  void update(long id, String indication, String therapeuticAreaCode, String username);
  void softDelete(long id, String username);
}
