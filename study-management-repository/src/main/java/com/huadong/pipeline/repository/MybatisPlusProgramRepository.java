package com.huadong.pipeline.repository;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huadong.pipeline.common.BusinessException;
import com.huadong.pipeline.domain.config.Program;
import com.huadong.pipeline.domain.config.ProgramRepository;
import com.huadong.pipeline.repository.entity.ProgramEntity;
import com.huadong.pipeline.repository.mapper.PipelineConfigMapper;
import com.huadong.pipeline.repository.mapper.ProgramMapper;
import com.huadong.pipeline.repository.mapper.ProgramSummaryData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

@Repository
public class MybatisPlusProgramRepository implements ProgramRepository {
  @Autowired
  private ProgramMapper mapper;
  @Autowired
  private PipelineConfigMapper configMapper;

  @Override
  public List<Program> findAll(String keyword) {
    return configMapper.findPrograms(keyword == null ? "" : keyword.trim()).stream()
        .map(MybatisPlusProgramRepository::toDomain).toList();
  }

  @Override
  public Optional<Program> findById(long id) {
    return Optional.ofNullable(configMapper.findProgram(id))
        .map(MybatisPlusProgramRepository::toDomain);
  }

  @Override
  public Optional<Program> findByCode(String code) {
    return Optional.ofNullable(configMapper.findProgramByCode(code))
        .map(MybatisPlusProgramRepository::toDomain);
  }

  @Override
  public Optional<Integer> findMaxVersionByCode(String code) {
    var query = Wrappers.<ProgramEntity>lambdaQuery()
        .eq(ProgramEntity::getProgramCode, code)
        .select(ProgramEntity::getVersion)
        .orderByDesc(ProgramEntity::getVersion)
        .last("LIMIT 1");
    return Optional.ofNullable(mapper.selectOne(query)).map(ProgramEntity::getVersion);
  }

  @Override
  public boolean existsByProductName(String productName, Long excludingId) {
    var query = Wrappers.<ProgramEntity>lambdaQuery()
        .eq(ProgramEntity::getProductName, productName).eq(ProgramEntity::getSysDeleted, 0);
    if (excludingId != null) query.ne(ProgramEntity::getId, excludingId);
    return mapper.selectCount(query) > 0;
  }

  @Override
  public Program create(String code, String productName, String moa,
      String sourceCode, String originCode, int version, String username) {
    var entity = new ProgramEntity();
    entity.setProgramCode(code);
    entity.setProductName(productName);
    entity.setMoa(moa);
    entity.setSourceCode(sourceCode);
    entity.setOriginCode(originCode);
    entity.setVersion(version);
    entity.setStatusCode("ACTIVE");
    entity.setSortOrder(0);
    entity.setSysCreateBy(username);
    entity.setSysUpdateBy(username);
    try {
      mapper.insert(entity);
    } catch (DuplicateKeyException ex) {
      throw new BusinessException("PROGRAM_CODE_EXISTS", "Program 编码已存在", ex);
    }
    return findById(entity.getId()).orElseThrow();
  }

  @Override
  public void update(long id, String productName, String moa,
      String sourceCode, String originCode, int expectedVersion, String username) {
    int rows = mapper.update(null,
        Wrappers.<ProgramEntity>lambdaUpdate()
            .set(ProgramEntity::getProductName, productName)
            .set(ProgramEntity::getMoa, moa)
            .set(ProgramEntity::getSourceCode, sourceCode)
            .set(ProgramEntity::getOriginCode, originCode)
            .set(ProgramEntity::getSysUpdateBy, username)
            .set(ProgramEntity::getSysUpdateTime, LocalDateTime.now())
            .setSql("version = version + 1")
            .eq(ProgramEntity::getId, id)
            .eq(ProgramEntity::getSysDeleted, 0)
            .eq(ProgramEntity::getVersion, expectedVersion));
    if (rows == 0) {
      throw new BusinessException("VERSION_CONFLICT", "数据已被他人修改，请刷新后重试");
    }
  }

  @Override
  public void softDelete(long id, String username) {
    var entity = new ProgramEntity();
    entity.setSysDeleted((short) 1);
    entity.setSysUpdateBy(username);
    entity.setSysUpdateTime(LocalDateTime.now());
    mapper.update(entity, Wrappers.<ProgramEntity>lambdaUpdate().eq(ProgramEntity::getId, id));
  }

  private static Program toDomain(ProgramSummaryData value) {
    return new Program(value.id(), value.version(), value.code(), value.productName(), value.moa(),
        value.sourceCode(), value.originCode(), value.projectCount(), value.studyCount(),
        value.updatedAt());
  }
}
