package com.huadong.pipeline.domain.study;

import java.util.List;

/** 管线总览按治疗领域（TA）分组的一组 project。 */
public record OverviewArea(
    String therapeuticAreaCode,
    String therapeuticAreaName,
    List<OverviewProject> projects) {
}
