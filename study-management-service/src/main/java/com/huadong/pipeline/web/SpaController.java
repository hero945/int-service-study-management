package com.huadong.pipeline.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
  @GetMapping({
      "/login",
      "/pipeline",
      "/studies",
      "/monthly",
      "/risks",
      "/team",
      "/config",
      "/reports",
      "/accounts"
  })
  String forwardToVue() {
    return "forward:/index.html";
  }
}
