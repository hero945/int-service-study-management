package com.huadong.pipeline.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadong.pipeline.repository.entity.StudyEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface StudyMapper extends BaseMapper<StudyEntity> {
  @Select("""
      SELECT p.id AS program_id, p.program_code, p.program_name, p.product_name,
             p.moa, p.source_code, p.origin_code,
             pr.id AS project_id, pr.project_code, pr.project_name,
             ta.id AS therapeutic_area_id, ta.area_code AS therapeutic_area_code,
             ta.area_name AS therapeutic_area_name, pr.indication_description
      FROM hd_plt_program p
      JOIN hd_plt_project pr ON pr.program_id = p.id AND pr.sys_deleted = 0
      JOIN hd_plt_therapeutic_area ta
        ON ta.id = pr.therapeutic_area_id AND ta.sys_deleted = 0
      WHERE p.program_code = #{programCode} AND p.sys_deleted = 0
        AND p.status_code = 'ACTIVE'
        AND pr.project_code = #{projectCode}
        AND ta.area_code = #{therapeuticAreaCode} AND ta.status_code = 'ACTIVE'
      """)
  StudyHierarchyRow findHierarchy(
      @Param("programCode") String programCode,
      @Param("projectCode") String projectCode,
      @Param("therapeuticAreaCode") String therapeuticAreaCode);
}
