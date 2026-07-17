package com.huadong.pipeline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class StudyManagementApplication {
  public static void main(String[] args) {
    SpringApplication.run(StudyManagementApplication.class, args);
  }
}
