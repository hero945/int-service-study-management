package com.huadong.pipeline.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadong.pipeline.repository.entity.RoleEntity;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface RoleMapper extends BaseMapper<RoleEntity> {
  @Select("""
      <script>
      SELECT r.id, r.role_name, r.role_description, r.data_scope_mode, r.status_code,
             r.is_system_role, r.sys_update_time,
             (SELECT COUNT(*) FROM hd_plt_user_role ur
              WHERE ur.role_id = r.id AND ur.sys_deleted = 0) AS assigned_user_count
      FROM hd_plt_role r
      WHERE r.sys_deleted = 0
      <if test="keyword != null and keyword != ''">
        AND (LOWER(r.role_name) LIKE CONCAT('%', LOWER(#{keyword}), '%')
             OR LOWER(COALESCE(r.role_description, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%'))
      </if>
      <if test="status != null">AND r.status_code = #{status}</if>
      ORDER BY r.is_system_role DESC, r.role_name ASC
      LIMIT #{limit} OFFSET #{offset}
      </script>
      """)
  List<RoleSummaryRow> findPage(
      @Param("keyword") String keyword,
      @Param("status") String status,
      @Param("limit") int limit,
      @Param("offset") int offset);

  @Select("""
      <script>
      SELECT COUNT(*) FROM hd_plt_role r WHERE r.sys_deleted = 0
      <if test="keyword != null and keyword != ''">
        AND (LOWER(r.role_name) LIKE CONCAT('%', LOWER(#{keyword}), '%')
             OR LOWER(COALESCE(r.role_description, '')) LIKE CONCAT('%', LOWER(#{keyword}), '%'))
      </if>
      <if test="status != null">AND r.status_code = #{status}</if>
      </script>
      """)
  long countPage(@Param("keyword") String keyword, @Param("status") String status);

  @Select("""
      SELECT p.permission_code
      FROM hd_plt_role_permission rp
      JOIN hd_plt_permission p ON p.id = rp.permission_id
      WHERE rp.role_id = #{roleId} AND rp.sys_deleted = 0
        AND p.sys_deleted = 0 AND p.status_code = 'ACTIVE'
      ORDER BY p.sort_order, p.permission_code
      """)
  List<String> findPermissionCodes(long roleId);

  @Select("""
      <script>
      SELECT rp.role_id, p.permission_code
      FROM hd_plt_role_permission rp
      JOIN hd_plt_permission p ON p.id = rp.permission_id
      WHERE rp.sys_deleted = 0 AND p.sys_deleted = 0 AND p.status_code = 'ACTIVE'
        AND rp.role_id IN
        <foreach item="roleId" collection="roleIds" open="(" separator="," close=")">
          #{roleId}
        </foreach>
      ORDER BY rp.role_id, p.sort_order, p.permission_code
      </script>
      """)
  List<RolePermissionCodeRow> findPermissionCodesByRoleIds(
      @Param("roleIds") List<Long> roleIds);

  @Select("""
      SELECT u.email
      FROM hd_plt_user_role ur
      JOIN hd_plt_user u ON u.id = ur.user_id
      WHERE ur.role_id = #{roleId} AND ur.sys_deleted = 0
        AND u.sys_deleted = 0 AND u.status_code = 'ACTIVE'
      ORDER BY u.email
      """)
  List<String> findAssignedUsernames(long roleId);

  @Select("""
      SELECT u.id AS user_id, r.id AS role_id, p.permission_code
      FROM hd_plt_user u
      JOIN hd_plt_user_role ur ON ur.user_id = u.id AND ur.sys_deleted = 0
      JOIN hd_plt_role r ON r.id = ur.role_id
        AND r.sys_deleted = 0 AND r.status_code = 'ACTIVE'
      LEFT JOIN hd_plt_role_permission rp ON rp.role_id = r.id AND rp.sys_deleted = 0
      LEFT JOIN hd_plt_permission p ON p.id = rp.permission_id
        AND p.sys_deleted = 0 AND p.status_code = 'ACTIVE'
      WHERE u.sys_deleted = 0 AND u.status_code = 'ACTIVE'
      ORDER BY u.id, r.id, p.permission_code
      """)
  List<UserRolePermissionRow> findActiveUserRolePermissions();

  @Update("""
      <script>
      UPDATE hd_plt_role_permission
      SET sys_deleted = 1, sys_update_by = #{operator}, sys_update_time = CURRENT_TIMESTAMP
      WHERE role_id = #{roleId} AND sys_deleted = 0
        AND permission_id NOT IN (
          SELECT id FROM hd_plt_permission WHERE permission_code IN
          <foreach item="code" collection="permissionCodes" open="(" separator="," close=")">
            #{code}
          </foreach>
        )
      </script>
      """)
  int removeUnselectedPermissions(
      @Param("roleId") long roleId,
      @Param("permissionCodes") List<String> permissionCodes,
      @Param("operator") String operator);

  @Update("""
      UPDATE hd_plt_role_permission
      SET sys_deleted = 0, sys_update_by = #{operator}, sys_update_time = CURRENT_TIMESTAMP
      WHERE role_id = #{roleId}
        AND permission_id = (SELECT id FROM hd_plt_permission WHERE permission_code = #{code})
      """)
  int restorePermission(
      @Param("roleId") long roleId,
      @Param("code") String code,
      @Param("operator") String operator);

  @Insert("""
      INSERT INTO hd_plt_role_permission(
          role_id, permission_id, sys_create_by, sys_update_by)
      SELECT #{roleId}, id, #{operator}, #{operator}
      FROM hd_plt_permission
      WHERE permission_code = #{code} AND status_code = 'ACTIVE' AND sys_deleted = 0
      """)
  int insertPermission(
      @Param("roleId") long roleId,
      @Param("code") String code,
      @Param("operator") String operator);

  @Update("""
      UPDATE hd_plt_role_permission
      SET sys_deleted = 1, sys_update_by = #{operator}, sys_update_time = CURRENT_TIMESTAMP
      WHERE role_id = #{roleId} AND sys_deleted = 0
      """)
  int deletePermissions(@Param("roleId") long roleId, @Param("operator") String operator);

}
