package com.huadong.pipeline.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huadong.pipeline.domain.setting.Setting;
import com.huadong.pipeline.domain.setting.SettingRepository;
import com.huadong.pipeline.repository.entity.SystemSettingEntity;
import com.huadong.pipeline.repository.mapper.SystemSettingMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusSettingRepository implements SettingRepository {
  private static final long LIST_LIMIT = 100;

  private final SystemSettingMapper mapper;

  public MybatisPlusSettingRepository(SystemSettingMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public List<Setting> findAll() {
    var query = Wrappers.<SystemSettingEntity>lambdaQuery()
        .eq(SystemSettingEntity::getSysDeleted, 0)
        .orderByAsc(SystemSettingEntity::getConfigKey);
    return mapper.selectPage(Page.of(1, LIST_LIMIT, false), query)
        .getRecords()
        .stream()
        .map(MybatisPlusSettingRepository::toDomain)
        .toList();
  }

  @Override
  public List<Setting> findPublic() {
    var query = Wrappers.<SystemSettingEntity>lambdaQuery()
        .eq(SystemSettingEntity::isPublicVisible, true)
        .eq(SystemSettingEntity::getSysDeleted, 0)
        .orderByAsc(SystemSettingEntity::getConfigKey);
    return mapper.selectPage(Page.of(1, LIST_LIMIT, false), query)
        .getRecords()
        .stream()
        .map(MybatisPlusSettingRepository::toDomain)
        .toList();
  }

  @Override
  public Optional<Setting> findByKey(String key) {
    var query = Wrappers.<SystemSettingEntity>lambdaQuery()
        .eq(SystemSettingEntity::getConfigKey, key)
        .eq(SystemSettingEntity::getSysDeleted, 0);
    return Optional.ofNullable(mapper.selectOne(query)).map(MybatisPlusSettingRepository::toDomain);
  }

  @Override
  public void update(String key, String value, String username) {
    mapper.updateValue(key, value, username);
  }

  private static Setting toDomain(SystemSettingEntity entity) {
    return new Setting(
        entity.getConfigKey(),
        entity.getConfigValue(),
        entity.getConfigDescription(),
        entity.isPublicVisible(),
        entity.getSysUpdateBy(),
        entity.getSysUpdateTime());
  }
}
