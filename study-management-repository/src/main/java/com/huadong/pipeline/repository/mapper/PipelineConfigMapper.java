package com.huadong.pipeline.repository.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface PipelineConfigMapper {
  @Select("""
      SELECT p.id, p.program_code AS code, p.product_name,
             p.moa, p.source_code, p.origin_code,
             (SELECT COUNT(*) FROM hd_plt_project pr
              WHERE pr.program_id = p.id AND pr.sys_deleted = 0) AS project_count,
             (SELECT COUNT(*) FROM hd_plt_study s
              WHERE s.program_id = p.id AND s.sys_deleted = 0) AS study_count,
             p.sys_update_time AS updated_at
      FROM hd_plt_program p
      WHERE p.sys_deleted = 0
        AND (#{keyword} = '' OR LOWER(p.program_code) LIKE LOWER(CONCAT('%', #{keyword}, '%'))
          OR LOWER(p.product_name) LIKE LOWER(CONCAT('%', #{keyword}, '%')))
      ORDER BY p.sort_order, p.program_code
      LIMIT 500
      """)
  List<ProgramSummaryData> findPrograms(@Param("keyword") String keyword);

  @Select("""
      SELECT p.id, p.program_code AS code, p.product_name,
             p.moa, p.source_code, p.origin_code,
             (SELECT COUNT(*) FROM hd_plt_project pr
              WHERE pr.program_id = p.id AND pr.sys_deleted = 0) AS project_count,
             (SELECT COUNT(*) FROM hd_plt_study s
              WHERE s.program_id = p.id AND s.sys_deleted = 0) AS study_count,
             p.sys_update_time AS updated_at
      FROM hd_plt_program p
      WHERE p.id = #{id} AND p.sys_deleted = 0
      """)
  ProgramSummaryData findProgram(@Param("id") long id);

  @Select("""
      SELECT p.id, p.program_code AS code, p.product_name,
             p.moa, p.source_code, p.origin_code,
             (SELECT COUNT(*) FROM hd_plt_project pr
              WHERE pr.program_id = p.id AND pr.sys_deleted = 0) AS project_count,
             (SELECT COUNT(*) FROM hd_plt_study s
              WHERE s.program_id = p.id AND s.sys_deleted = 0) AS study_count,
             p.sys_update_time AS updated_at
      FROM hd_plt_program p
      WHERE p.program_code = #{code} AND p.sys_deleted = 0
      """)
  ProgramSummaryData findProgramByCode(@Param("code") String code);

  @Select("""
      SELECT s.id AS study_id, s.study_code, s.phase_status_code,
             pr.id AS project_id, pr.project_code,
             pr.indication_description AS indication,
             ta.area_code AS therapeutic_area_code, ta.area_name AS therapeutic_area_name,
             p.id AS program_id, p.program_code, p.product_name,
             p.moa, p.source_code, p.origin_code, s.sys_update_time AS updated_at
      FROM hd_plt_study s
      JOIN hd_plt_project pr ON pr.id = s.project_id AND pr.sys_deleted = 0
      JOIN hd_plt_program p ON p.id = pr.program_id AND p.sys_deleted = 0
      JOIN hd_plt_therapeutic_area ta
        ON ta.id = pr.therapeutic_area_id AND ta.sys_deleted = 0
      WHERE s.sys_deleted = 0
      ORDER BY p.program_code, pr.project_code, s.study_code
      """)
  List<PipelineConfigRowData> findRows();

  @Select("""
      <script>
      SELECT s.id AS study_id, s.study_code, s.phase_status_code,
             pr.id AS project_id, pr.project_code,
             pr.indication_description AS indication,
             ta.area_code AS therapeutic_area_code, ta.area_name AS therapeutic_area_name,
             p.id AS program_id, p.program_code, p.product_name,
             p.moa, p.source_code, p.origin_code, s.sys_update_time AS updated_at
      FROM hd_plt_study s
      JOIN hd_plt_project pr ON pr.id = s.project_id AND pr.sys_deleted = 0
      JOIN hd_plt_program p ON p.id = pr.program_id AND p.sys_deleted = 0
      JOIN hd_plt_therapeutic_area ta
        ON ta.id = pr.therapeutic_area_id AND ta.sys_deleted = 0
      WHERE s.sys_deleted = 0
      <if test="keyword != null and keyword != ''">
        AND (
          LOWER(s.study_code) LIKE CONCAT('%', LOWER(#{keyword}), '%')
          OR LOWER(ta.area_code) LIKE CONCAT('%', LOWER(#{keyword}), '%')
          OR LOWER(ta.area_name) LIKE CONCAT('%', LOWER(#{keyword}), '%')
          OR LOWER(p.program_code) LIKE CONCAT('%', LOWER(#{keyword}), '%')
        )
      </if>
      ORDER BY p.program_code, pr.project_code, s.study_code
      LIMIT #{limit} OFFSET #{offset}
      </script>
      """)
  List<PipelineConfigRowData> findRowsPage(
      @Param("keyword") String keyword,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Select("""
      <script>
      SELECT COUNT(*)
      FROM hd_plt_study s
      JOIN hd_plt_project pr ON pr.id = s.project_id AND pr.sys_deleted = 0
      JOIN hd_plt_program p ON p.id = pr.program_id AND p.sys_deleted = 0
      JOIN hd_plt_therapeutic_area ta
        ON ta.id = pr.therapeutic_area_id AND ta.sys_deleted = 0
      WHERE s.sys_deleted = 0
      <if test="keyword != null and keyword != ''">
        AND (
          LOWER(s.study_code) LIKE CONCAT('%', LOWER(#{keyword}), '%')
          OR LOWER(ta.area_code) LIKE CONCAT('%', LOWER(#{keyword}), '%')
          OR LOWER(ta.area_name) LIKE CONCAT('%', LOWER(#{keyword}), '%')
          OR LOWER(p.program_code) LIKE CONCAT('%', LOWER(#{keyword}), '%')
        )
      </if>
      </script>
      """)
  long countRows(@Param("keyword") String keyword);

  @Select("""
      SELECT pr.id, pr.project_code AS code,
             pr.program_id, p.program_code, pr.indication_description AS indication,
             ta.id AS therapeutic_area_id, ta.area_code AS therapeutic_area_code,
             ta.area_name AS therapeutic_area_name,
             (SELECT COUNT(*) FROM hd_plt_study s
              WHERE s.project_id = pr.id AND s.sys_deleted = 0) AS study_count,
             pr.sys_update_time AS updated_at
      FROM hd_plt_project pr
      JOIN hd_plt_program p ON p.id = pr.program_id AND p.sys_deleted = 0
      JOIN hd_plt_therapeutic_area ta
        ON ta.id = pr.therapeutic_area_id AND ta.sys_deleted = 0
      WHERE pr.sys_deleted = 0
        AND (#{programId} IS NULL OR pr.program_id = #{programId})
        AND (#{keyword} = '' OR LOWER(pr.project_code) LIKE LOWER(CONCAT('%', #{keyword}, '%')))
      ORDER BY pr.sort_order, pr.project_code
      """)
  List<ProjectSummaryData> findProjects(
      @Param("programId") Long programId, @Param("keyword") String keyword);

  @Select("""
      SELECT pr.id, pr.project_code AS code,
             pr.program_id, p.program_code, pr.indication_description AS indication,
             ta.id AS therapeutic_area_id, ta.area_code AS therapeutic_area_code,
             ta.area_name AS therapeutic_area_name,
             (SELECT COUNT(*) FROM hd_plt_study s
              WHERE s.project_id = pr.id AND s.sys_deleted = 0) AS study_count,
             pr.sys_update_time AS updated_at
      FROM hd_plt_project pr
      JOIN hd_plt_program p ON p.id = pr.program_id AND p.sys_deleted = 0
      JOIN hd_plt_therapeutic_area ta
        ON ta.id = pr.therapeutic_area_id AND ta.sys_deleted = 0
      WHERE pr.id = #{id} AND pr.sys_deleted = 0
      """)
  ProjectSummaryData findProject(@Param("id") long id);

  @Select("SELECT id FROM hd_plt_therapeutic_area WHERE area_code = #{code} AND status_code = 'ACTIVE' AND sys_deleted = 0")
  Long findTherapeuticAreaId(@Param("code") String code);

  @Select("""
      SELECT id, area_code AS code, area_name AS name, english_name
      FROM hd_plt_therapeutic_area
      WHERE status_code = 'ACTIVE' AND sys_deleted = 0
      ORDER BY sort_order, area_code
      """)
  List<TherapeuticAreaData> findTherapeuticAreas();

  @Select("""
      SELECT p.id AS program_id, p.program_code, p.product_name,
             p.moa, p.source_code, p.origin_code,
             pr.id AS project_id, pr.project_code,
             ta.id AS therapeutic_area_id, ta.area_code AS therapeutic_area_code,
             ta.area_name AS therapeutic_area_name, pr.indication_description
      FROM hd_plt_program p
      JOIN hd_plt_project pr ON pr.program_id = p.id AND pr.sys_deleted = 0
      JOIN hd_plt_therapeutic_area ta
        ON ta.id = pr.therapeutic_area_id AND ta.sys_deleted = 0
      WHERE pr.id = #{projectId} AND p.sys_deleted = 0
      """)
  StudyHierarchyRow findHierarchyByProjectId(@Param("projectId") long projectId);

  @Select("SELECT COUNT(*) FROM hd_plt_project WHERE program_id = #{id} AND sys_deleted = 0")
  long countProjects(@Param("id") long programId);

  @Select("SELECT COUNT(*) FROM hd_plt_study WHERE program_id = #{id} AND sys_deleted = 0")
  long countStudiesByProgram(@Param("id") long programId);

  @Select("SELECT COUNT(*) FROM hd_plt_study WHERE project_id = #{id} AND sys_deleted = 0")
  long countStudiesByProject(@Param("id") long projectId);

  @Select("SELECT COUNT(*) FROM hd_plt_team_assignment WHERE study_id = #{id} AND sys_deleted = 0")
  long countTeamReferences(@Param("id") long studyId);

  @Select("SELECT COUNT(*) FROM hd_plt_study_milestone WHERE study_id = #{id} AND sys_deleted = 0")
  long countMilestoneReferences(@Param("id") long studyId);

  @Select("SELECT COUNT(*) FROM hd_plt_monthly_report WHERE study_id = #{id} AND sys_deleted = 0")
  long countMonthlyReferences(@Param("id") long studyId);

  @Select("SELECT COUNT(*) FROM hd_plt_risk WHERE study_id = #{id} AND sys_deleted = 0")
  long countRiskReferences(@Param("id") long studyId);
}
