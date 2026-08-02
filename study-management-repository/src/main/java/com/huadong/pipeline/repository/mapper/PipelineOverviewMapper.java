package com.huadong.pipeline.repository.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 管线总览读模型查询。两次批量查询（project 一次 + study IN 一次），避免 N+1。
 * 数据范围：allStudies=false 时按 hd_plt_team_assignment 限定可见 study 及其所属 project。
 */
public interface PipelineOverviewMapper {

  @Select("""
      <script>
      SELECT pr.id, pr.project_code AS code, pr.indication_description AS indication,
             p.program_code, p.product_name, p.moa, p.source_code, p.origin_code,
             ta.area_code AS therapeutic_area_code, ta.area_name AS therapeutic_area_name
      FROM hd_plt_project pr
      JOIN hd_plt_program p ON p.id = pr.program_id AND p.sys_deleted = 0
      JOIN hd_plt_therapeutic_area ta ON ta.id = pr.therapeutic_area_id AND ta.sys_deleted = 0
      WHERE pr.sys_deleted = 0
      <if test='!allStudies'>
        AND EXISTS (
          SELECT 1 FROM hd_plt_study s
          JOIN hd_plt_team_assignment t ON t.study_id = s.id AND t.sys_deleted = 0
          WHERE s.project_id = pr.id AND s.sys_deleted = 0 AND t.user_id = #{userId})
      </if>
      ORDER BY ta.sort_order, ta.area_code, pr.sort_order, pr.project_code
      </script>
      """)
  List<OverviewProjectRow> findProjects(
      @Param("userId") long userId, @Param("allStudies") boolean allStudies);

  @Select("""
      <script>
      SELECT s.id, s.study_code AS code, s.project_id,
             s.phase_status_code AS phase, s.planned_start_date AS start_date,
             s.actual_start_date, s.actual_end_date, s.sys_update_time AS updated_at
      FROM hd_plt_study s
      WHERE s.sys_deleted = 0
        AND s.project_id IN
        <foreach collection='projectIds' item='pid' open='(' separator=',' close=')'>
          #{pid}
        </foreach>
      <if test='!allStudies'>
        AND EXISTS (
          SELECT 1 FROM hd_plt_team_assignment t
          WHERE t.study_id = s.id AND t.sys_deleted = 0 AND t.user_id = #{userId})
      </if>
      ORDER BY s.sys_update_time DESC, s.study_code
      </script>
      """)
  List<OverviewStudyRow> findStudies(
      @Param("projectIds") List<Long> projectIds,
      @Param("userId") long userId,
      @Param("allStudies") boolean allStudies);
}
