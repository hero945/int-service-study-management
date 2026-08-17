package com.huadong.pipeline.domain.study;

import java.util.List;

/** 管线总览的一行：一个 project 聚合其下所有 study，附 TA 与 program 展示信息。 */
public record OverviewProject(
    long id,
    String code,
    String indication,
    String programCode,
    String productName,
    String moa,
    String sourceCode,
    String originCode,
    String therapeuticAreaCode,
    String therapeuticAreaName,
    List<OverviewStudy> studies,
    RegulatoryOverviewStatus regulatoryStatus) {
}
