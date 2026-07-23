package com.huadong.pipeline.repository.mapper;

/** 管线总览 project 查询行（project JOIN program JOIN TA）。 */
public record OverviewProjectRow(
    long id,
    String code,
    String indication,
    String programCode,
    String productName,
    String moa,
    String sourceCode,
    String originCode,
    String therapeuticAreaCode,
    String therapeuticAreaName) {
}
