package com.huadong.pipeline.repository.mapper;

import java.time.LocalDate;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ProjectMilestoneMapper {

  @Select("""
      SELECT id, project_id, stage_code, milestone_code,
             plan_v1_date, plan_v2_date, actual_start_date, actual_end_date,
             deviation_note, sys_update_time AS updated_at
      FROM hd_plt_project_milestone
      WHERE sys_deleted = 0 AND project_id = #{projectId}
      ORDER BY stage_code, milestone_code
      """)
  List<ProjectMilestoneRow> findByProjectId(@Param("projectId") long projectId);

  @Select("""
      <script>
      SELECT id, project_id, stage_code, milestone_code,
             plan_v1_date, plan_v2_date, actual_start_date, actual_end_date,
             deviation_note, sys_update_time AS updated_at
      FROM hd_plt_project_milestone
      WHERE sys_deleted = 0
        AND project_id IN
        <foreach collection='projectIds' item='pid' open='(' separator=',' close=')'>
          #{pid}
        </foreach>
      ORDER BY project_id, stage_code, milestone_code
      </script>
      """)
  List<ProjectMilestoneRow> findByProjectIds(@Param("projectIds") List<Long> projectIds);

  @Insert("""
      INSERT INTO hd_plt_project_milestone(
        project_id, stage_code, milestone_code,
        plan_v1_date, plan_v2_date, actual_start_date, actual_end_date,
        deviation_note, sys_create_by, sys_update_by)
      VALUES (
        #{projectId}, #{stageCode}, #{milestoneCode},
        #{planV1Date}, #{planV2Date}, #{actualStartDate}, #{actualEndDate},
        #{deviationNote}, #{operatorEmail}, #{operatorEmail})
      ON DUPLICATE KEY UPDATE
        plan_v1_date = VALUES(plan_v1_date),
        plan_v2_date = VALUES(plan_v2_date),
        actual_start_date = VALUES(actual_start_date),
        actual_end_date = VALUES(actual_end_date),
        deviation_note = VALUES(deviation_note),
        sys_update_by = VALUES(sys_update_by),
        sys_update_time = CURRENT_TIMESTAMP(6)
      """)
  int upsert(ProjectMilestoneUpsertParams params);

  @Select("""
      SELECT id, project_id, stage_code, milestone_code,
             plan_v1_date, plan_v2_date, actual_start_date, actual_end_date,
             deviation_note, sys_update_time AS updated_at
      FROM hd_plt_project_milestone
      WHERE sys_deleted = 0 AND project_id = #{projectId} AND milestone_code = #{milestoneCode}
      """)
  ProjectMilestoneRow findByProjectIdAndMilestoneCode(
      @Param("projectId") long projectId,
      @Param("milestoneCode") String milestoneCode);

  record ProjectMilestoneUpsertParams(
      Long id,
      long projectId,
      String stageCode,
      String milestoneCode,
      LocalDate planV1Date,
      LocalDate planV2Date,
      LocalDate actualStartDate,
      LocalDate actualEndDate,
      String deviationNote,
      String operatorEmail) {}
}
