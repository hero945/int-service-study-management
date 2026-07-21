package com.huadong.pipeline.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huadong.pipeline.repository.entity.SystemSettingEntity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SystemSettingMapper extends BaseMapper<SystemSettingEntity> {
  @Update("""
      UPDATE hd_plt_system_setting
      SET config_value = #{value}, sys_update_by = #{username},
          sys_update_time = CURRENT_TIMESTAMP
      WHERE config_key = #{key} AND sys_deleted = 0
      """)
  int updateValue(
      @Param("key") String key,
      @Param("value") String value,
      @Param("username") String username);
}
