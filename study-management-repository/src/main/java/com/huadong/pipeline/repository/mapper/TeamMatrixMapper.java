package com.huadong.pipeline.repository.mapper;

import java.util.List;
import java.util.Set;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface TeamMatrixMapper {
  @Select("""
      <script>
      SELECT COUNT(*)
      FROM hd_plt_study s
      WHERE s.sys_deleted = 0
        <if test="!allStudies">
          AND EXISTS (
            SELECT 1 FROM hd_plt_team_assignment ta
            WHERE ta.study_id = s.id AND ta.user_id = #{scopeUserId} AND ta.sys_deleted = 0)
        </if>
        <if test="studyQuery != null and studyQuery != ''">
          AND (LOWER(s.study_code) LIKE CONCAT('%', LOWER(#{studyQuery}), '%')
            OR LOWER(s.indication_description_snapshot) LIKE CONCAT('%', LOWER(#{studyQuery}), '%'))
        </if>
      </script>
      """)
  long countStudies(
      @Param("allStudies") boolean allStudies,
      @Param("scopeUserId") long scopeUserId,
      @Param("studyQuery") String studyQuery);

  @Select("""
      <script>
      SELECT s.id AS study_id, s.study_code,
             s.indication_description_snapshot AS indication,
             CASE
               WHEN s.actual_end_date IS NOT NULL THEN 'COMPLETED'
               WHEN s.actual_start_date IS NOT NULL THEN 'ACTIVE'
               ELSE 'PLANNED'
             END AS status_code,
             CASE
               WHEN s.actual_end_date IS NOT NULL THEN '已完成'
               WHEN s.actual_start_date IS NOT NULL THEN '进行中'
               ELSE '计划中'
             END AS status_label,
             s.team_version
      FROM hd_plt_study s
      WHERE s.sys_deleted = 0
        <if test="!allStudies">
          AND EXISTS (
            SELECT 1 FROM hd_plt_team_assignment ta
            WHERE ta.study_id = s.id AND ta.user_id = #{scopeUserId} AND ta.sys_deleted = 0)
        </if>
        <if test="studyQuery != null and studyQuery != ''">
          AND (LOWER(s.study_code) LIKE CONCAT('%', LOWER(#{studyQuery}), '%')
            OR LOWER(s.indication_description_snapshot) LIKE CONCAT('%', LOWER(#{studyQuery}), '%'))
        </if>
      ORDER BY s.id
      LIMIT #{pageSize} OFFSET #{offset}
      </script>
      """)
  List<TeamStudyRow> findStudies(
      @Param("allStudies") boolean allStudies,
      @Param("scopeUserId") long scopeUserId,
      @Param("studyQuery") String studyQuery,
      @Param("offset") int offset,
      @Param("pageSize") int pageSize);

  @Select("""
      <script>
      SELECT s.id AS study_id, s.study_code,
             s.indication_description_snapshot AS indication,
             CASE
               WHEN s.actual_end_date IS NOT NULL THEN 'COMPLETED'
               WHEN s.actual_start_date IS NOT NULL THEN 'ACTIVE'
               ELSE 'PLANNED'
             END AS status_code,
             CASE
               WHEN s.actual_end_date IS NOT NULL THEN '已完成'
               WHEN s.actual_start_date IS NOT NULL THEN '进行中'
               ELSE '计划中'
             END AS status_label,
             s.team_version
      FROM hd_plt_study s
      WHERE s.sys_deleted = 0 AND s.id = #{studyId}
        <if test="!allStudies">
          AND EXISTS (
            SELECT 1 FROM hd_plt_team_assignment ta
            WHERE ta.study_id = s.id AND ta.user_id = #{scopeUserId} AND ta.sys_deleted = 0)
        </if>
      </script>
      """)
  TeamStudyRow findVisibleStudy(
      @Param("studyId") long studyId,
      @Param("allStudies") boolean allStudies,
      @Param("scopeUserId") long scopeUserId);

  @Select("""
      <script>
      SELECT tr.id, tr.role_code, tr.role_name, tr.function_line_id,
             fl.function_code, fl.function_name, tr.sort_order
      FROM hd_plt_team_role tr
      LEFT JOIN hd_plt_function_line fl
        ON fl.id = tr.function_line_id AND fl.sys_deleted = 0
      WHERE tr.sys_deleted = 0 AND tr.status_code = 'ACTIVE'
        <if test="roleQuery != null and roleQuery != ''">
          AND (LOWER(tr.role_code) LIKE CONCAT('%', LOWER(#{roleQuery}), '%')
            OR LOWER(tr.role_name) LIKE CONCAT('%', LOWER(#{roleQuery}), '%')
            OR LOWER(fl.function_name) LIKE CONCAT('%', LOWER(#{roleQuery}), '%'))
        </if>
      ORDER BY tr.sort_order, tr.id
      </script>
      """)
  List<TeamRoleRow> findRoles(@Param("roleQuery") String roleQuery);

  @Select("""
      <script>
      SELECT ta.study_id, tr.role_code, u.id AS user_id, u.email,
             u.display_name, u.status_code
      FROM hd_plt_team_assignment ta
      JOIN hd_plt_team_role tr ON tr.id = ta.team_role_id AND tr.sys_deleted = 0
      JOIN hd_plt_user u ON u.id = ta.user_id AND u.sys_deleted = 0
      WHERE ta.sys_deleted = 0 AND ta.study_id IN
        <foreach collection="studyIds" item="id" open="(" separator="," close=")">
          #{id}
        </foreach>
      ORDER BY ta.study_id, tr.sort_order, u.display_name, u.id
      </script>
      """)
  List<TeamMemberRow> findAssignments(@Param("studyIds") List<Long> studyIds);

  @Select("""
      <script>
      SELECT s.id AS study_id, s.study_code, s.indication_description_snapshot AS indication,
             'PLANNED' AS status_code, '计划中' AS status_label, s.team_version
      FROM hd_plt_study s
      WHERE s.sys_deleted = 0 AND s.id IN
        <foreach collection="studyIds" item="id" open="(" separator="," close=")">
          #{id}
        </foreach>
        <if test="!allStudies">
          AND EXISTS (
            SELECT 1 FROM hd_plt_team_assignment ta
            WHERE ta.study_id = s.id AND ta.user_id = #{scopeUserId} AND ta.sys_deleted = 0)
        </if>
      ORDER BY s.id
      FOR UPDATE
      </script>
      """)
  List<TeamStudyRow> lockStudies(
      @Param("studyIds") Set<Long> studyIds,
      @Param("allStudies") boolean allStudies,
      @Param("scopeUserId") long scopeUserId);

  @Select("""
      <script>
      SELECT tr.id, tr.role_code, tr.role_name, tr.function_line_id,
             fl.function_code, fl.function_name, tr.sort_order
      FROM hd_plt_team_role tr
      LEFT JOIN hd_plt_function_line fl ON fl.id = tr.function_line_id
      WHERE tr.sys_deleted = 0 AND tr.status_code = 'ACTIVE' AND tr.role_code IN
        <foreach collection="roleCodes" item="code" open="(" separator="," close=")">
          #{code}
        </foreach>
      </script>
      """)
  List<TeamRoleRow> findRolesByCodes(@Param("roleCodes") Set<String> roleCodes);

  @Select("""
      <script>
      SELECT 0 AS study_id, '' AS role_code, u.id AS user_id, u.email,
             u.display_name, u.status_code
      FROM hd_plt_user u
      WHERE u.sys_deleted = 0 AND u.id IN
        <foreach collection="userIds" item="id" open="(" separator="," close=")">
          #{id}
        </foreach>
      </script>
      """)
  List<TeamMemberRow> findMembers(@Param("userIds") Set<Long> userIds);

  @Select("""
      SELECT ta.user_id
      FROM hd_plt_team_assignment ta
      JOIN hd_plt_team_role tr ON tr.id = ta.team_role_id
      WHERE ta.study_id = #{studyId} AND tr.role_code = #{roleCode}
        AND ta.sys_deleted = 0
      ORDER BY ta.user_id
      """)
  List<Long> findAssignedUserIds(
      @Param("studyId") long studyId, @Param("roleCode") String roleCode);

  @Select("""
      <script>
      SELECT ta.study_id, '' AS role_code, u.id AS user_id, u.email,
             u.display_name, u.status_code
      FROM hd_plt_team_assignment ta
      JOIN hd_plt_team_role tr ON tr.id = ta.team_role_id AND tr.sys_deleted = 0
      JOIN hd_plt_user u ON u.id = ta.user_id AND u.sys_deleted = 0
      WHERE ta.sys_deleted = 0 AND tr.role_code = #{roleCode}
        AND ta.study_id IN
        <foreach collection="studyIds" item="id" open="(" separator="," close=")">
          #{id}
        </foreach>
      ORDER BY ta.study_id, u.display_name, u.id
      </script>
      """)
  List<TeamMemberRow> findPlPmMembers(
      @Param("studyIds") Set<Long> studyIds, @Param("roleCode") String roleCode);

  @Update("""
      UPDATE hd_plt_team_assignment ta
      SET ta.sys_deleted = 1, ta.sys_update_by = #{operator},
          ta.sys_update_time = CURRENT_TIMESTAMP
      WHERE ta.study_id = #{studyId} AND ta.team_role_id = #{roleId}
        AND ta.sys_deleted = 0
      """)
  void softDeleteRoleAssignments(
      @Param("studyId") long studyId,
      @Param("roleId") long roleId,
      @Param("operator") String operator);

  @Update("""
      UPDATE hd_plt_team_assignment
      SET function_line_id = #{functionLineId},
          team_role_code_snapshot = #{roleCode},
          team_role_name_snapshot = #{roleName},
          function_line_code_snapshot = #{functionCode},
          function_line_name_snapshot = #{functionName},
          user_email_snapshot = #{email},
          user_name_snapshot = #{displayName},
          sys_deleted = 0, sys_update_by = #{operator},
          sys_update_time = CURRENT_TIMESTAMP
      WHERE study_id = #{studyId} AND team_role_id = #{roleId} AND user_id = #{userId}
      """)
  int reviveAssignment(
      @Param("studyId") long studyId,
      @Param("roleId") long roleId,
      @Param("functionLineId") Long functionLineId,
      @Param("roleCode") String roleCode,
      @Param("roleName") String roleName,
      @Param("functionCode") String functionCode,
      @Param("functionName") String functionName,
      @Param("userId") long userId,
      @Param("email") String email,
      @Param("displayName") String displayName,
      @Param("operator") String operator);

  @Insert("""
      INSERT INTO hd_plt_team_assignment(
          study_id, team_role_id, user_id, function_line_id,
          team_role_code_snapshot, team_role_name_snapshot,
          function_line_code_snapshot, function_line_name_snapshot,
          user_email_snapshot, user_name_snapshot, sys_create_by, sys_update_by)
      VALUES(
          #{studyId}, #{roleId}, #{userId}, #{functionLineId},
          #{roleCode}, #{roleName}, #{functionCode}, #{functionName},
          #{email}, #{displayName}, #{operator}, #{operator})
      """)
  void insertAssignment(
      @Param("studyId") long studyId,
      @Param("roleId") long roleId,
      @Param("functionLineId") Long functionLineId,
      @Param("roleCode") String roleCode,
      @Param("roleName") String roleName,
      @Param("functionCode") String functionCode,
      @Param("functionName") String functionName,
      @Param("userId") long userId,
      @Param("email") String email,
      @Param("displayName") String displayName,
      @Param("operator") String operator);

  @Update("""
      UPDATE hd_plt_study
      SET team_version = team_version + 1, sys_update_by = #{operator},
          sys_update_time = CURRENT_TIMESTAMP
      WHERE id = #{studyId} AND team_version = #{expectedVersion} AND sys_deleted = 0
      """)
  int incrementVersion(
      @Param("studyId") long studyId,
      @Param("expectedVersion") long expectedVersion,
      @Param("operator") String operator);

}
