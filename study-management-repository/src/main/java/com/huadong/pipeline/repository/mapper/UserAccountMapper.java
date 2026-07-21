package com.huadong.pipeline.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadong.pipeline.repository.entity.UserAccountEntity;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserAccountMapper extends BaseMapper<UserAccountEntity> {
  @Select("""
      SELECT r.role_name
      FROM hd_plt_user_role ur
      JOIN hd_plt_role r ON r.id = ur.role_id
      WHERE ur.user_id = #{userId}
        AND ur.sys_deleted = 0 AND r.sys_deleted = 0 AND r.status_code = 'ACTIVE'
      ORDER BY r.role_name
      """)
  List<String> findRoleCodes(long userId);

  @Select("""
      SELECT DISTINCT p.permission_code
      FROM hd_plt_user_role ur
      JOIN hd_plt_role r ON r.id = ur.role_id
      JOIN hd_plt_role_permission rp ON rp.role_id = r.id
      JOIN hd_plt_permission p ON p.id = rp.permission_id
      WHERE ur.user_id = #{userId} AND ur.sys_deleted = 0
        AND r.sys_deleted = 0 AND r.status_code = 'ACTIVE'
        AND rp.sys_deleted = 0 AND p.sys_deleted = 0 AND p.status_code = 'ACTIVE'
      ORDER BY p.permission_code
      """)
  List<String> findPermissionCodes(long userId);

  @Select("""
      SELECT DISTINCT r.data_scope_mode
      FROM hd_plt_user_role ur
      JOIN hd_plt_role r ON r.id = ur.role_id
      WHERE ur.user_id = #{userId} AND ur.sys_deleted = 0
        AND r.sys_deleted = 0 AND r.status_code = 'ACTIVE'
      """)
  List<String> findDataScopes(long userId);

  @Select("""
      <script>
      SELECT ur.user_id, r.role_name AS role_code,
             r.data_scope_mode AS data_scope, p.permission_code
      FROM hd_plt_user_role ur
      JOIN hd_plt_role r ON r.id = ur.role_id
        AND r.sys_deleted = 0 AND r.status_code = 'ACTIVE'
      LEFT JOIN hd_plt_role_permission rp ON rp.role_id = r.id AND rp.sys_deleted = 0
      LEFT JOIN hd_plt_permission p ON p.id = rp.permission_id
        AND p.sys_deleted = 0 AND p.status_code = 'ACTIVE'
      WHERE ur.sys_deleted = 0 AND ur.user_id IN
        <foreach item="userId" collection="userIds" open="(" separator="," close=")">
          #{userId}
        </foreach>
      ORDER BY ur.user_id, r.role_name, p.permission_code
      </script>
      """)
  List<UserAuthorizationRow> findAuthorizationRows(@Param("userIds") List<Long> userIds);

  @Select("""
      <script>
      SELECT COUNT(*)
      FROM hd_plt_role
      WHERE status_code = 'ACTIVE' AND sys_deleted = 0
        AND role_name IN
        <foreach item="roleCode" collection="roleCodes" open="(" separator="," close=")">
          #{roleCode}
        </foreach>
      </script>
      """)
  long countEnabledRoles(@Param("roleCodes") List<String> roleCodes);

  @Insert("""
      INSERT INTO hd_plt_user_role(
          user_id, role_id, sys_create_by, sys_update_by)
      SELECT #{userId}, id, #{operator}, #{operator}
      FROM hd_plt_role
      WHERE role_name = #{roleCode} AND status_code = 'ACTIVE' AND sys_deleted = 0
      """)
  int insertUserRole(
      @Param("userId") long userId,
      @Param("roleCode") String roleCode,
      @Param("operator") String operator);
}
