package com.huadong.pipeline.support;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestFaultController {

  @GetMapping("/api/v1/__test__/fault")
  void fault() {
    throw new IllegalStateException("deterministic integration test fault");
  }
}
