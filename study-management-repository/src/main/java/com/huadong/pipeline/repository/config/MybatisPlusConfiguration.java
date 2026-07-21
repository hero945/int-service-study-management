package com.huadong.pipeline.repository.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@MapperScan("com.huadong.pipeline.repository.mapper")
public class MybatisPlusConfiguration {
  @Bean
  MybatisPlusInterceptor mybatisPlusInterceptor() {
    var pagination = new PaginationInnerInterceptor(DbType.MYSQL);
    pagination.setMaxLimit(500L);

    var interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(pagination);
    return interceptor;
  }
}
