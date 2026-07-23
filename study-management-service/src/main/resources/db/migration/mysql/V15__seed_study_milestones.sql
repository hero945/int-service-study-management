-- V15 管线总览里程碑演示数据：为 V14 的 19 个 study 生成配套里程碑节点。
-- 规则：每个 study 按其 phase 映射到对应的里程碑 stage，
--   已完成(COMPLETED) study 的当前阶段末节点 actual_end 非空 → currentPhaseCompleted;
--   进行中/计划中 study 的当前阶段末节点之前某一节点 in-progress（actual_start 非空、actual_end 空）→ 显示子状态。
-- 日期：进行中节点锚定在 2026-07 附近，已完成节点回推，保证演示时间线合理。
-- 幂等：INSERT ... ON DUPLICATE KEY UPDATE，重复执行仅更新，不报错。
-- FK 通过子查询按 study_code 引用，避免硬编码自增主键。

-- HDM2001-101  (PHASE_1, ACTIVE)  -> 34 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'IND', 'IND-0', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'IND', 'IND-1', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'IND', 'IND-2', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'IND', 'IND-3', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'IND', 'IND-4', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-0', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-1', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-2', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-3', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-4', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-5', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-8', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-9', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-10', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'SSU', 'SSU-11', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-101'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM2001-201  (PHASE_2, ACTIVE)  -> 36 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-09-22', '2022-10-12', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2022-11-01', '2022-11-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'IND', 'IND-0', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'IND', 'IND-1', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'IND', 'IND-2', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'IND', 'IND-3', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'IND', 'IND-4', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-0', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-1', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-2', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-3', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-4', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-5', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-8', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-9', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-10', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'SSU', 'SSU-11', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'Enrollment', 'Enrollment-2', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-201'),
  'IA', 'IA-0', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM2001-301  (PHASE_3_1, PLANNED)  -> 42 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-01-25', '2022-02-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2022-03-06', '2022-03-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2022-04-15', '2022-05-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2022-05-25', '2022-06-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2022-07-04', '2022-07-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2022-08-13', '2022-09-02', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'IND', 'IND-0', NULL, NULL,
  '2022-09-22', '2022-10-12', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'IND', 'IND-1', NULL, NULL,
  '2022-11-01', '2022-11-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'IND', 'IND-2', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'IND', 'IND-3', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'IND', 'IND-4', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-0', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-1', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-2', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-3', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-4', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-5', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-6', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-8', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-9', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-10', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'SSU', 'SSU-11', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Enrollment', 'Enrollment-2', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'IA', 'IA-0', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'IA', 'IA-1', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Data_Report', 'Data_Report-0', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Data_Report', 'Data_Report-1', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Data_Report', 'Data_Report-2', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Data_Report', 'Data_Report-3', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-301'),
  'Data_Report', 'Data_Report-4', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM2001-302  (PHASE_3_2, PLANNED)  -> 52 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2020-12-21', '2021-01-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2021-01-30', '2021-02-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2021-03-11', '2021-03-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2021-04-20', '2021-05-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2021-05-30', '2021-06-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2021-07-09', '2021-07-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'IND', 'IND-0', NULL, NULL,
  '2021-08-18', '2021-09-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'IND', 'IND-1', NULL, NULL,
  '2021-09-27', '2021-10-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'IND', 'IND-2', NULL, NULL,
  '2021-11-06', '2021-11-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'IND', 'IND-3', NULL, NULL,
  '2021-12-16', '2022-01-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'IND', 'IND-4', NULL, NULL,
  '2022-01-25', '2022-02-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2022-03-06', '2022-03-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2022-04-15', '2022-05-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2022-05-25', '2022-06-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2022-07-04', '2022-07-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2022-08-13', '2022-09-02', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2022-09-22', '2022-10-12', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2022-11-01', '2022-11-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-0', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-1', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-2', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-3', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-4', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-5', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-6', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-7', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-8', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-9', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-10', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'SSU', 'SSU-11', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Enrollment', 'Enrollment-2', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'IA', 'IA-0', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'IA', 'IA-1', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Data_Report', 'Data_Report-0', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Data_Report', 'Data_Report-1', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Data_Report', 'Data_Report-2', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Data_Report', 'Data_Report-3', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Data_Report', 'Data_Report-4', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Data_Report', 'Data_Report-5', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Data_Report', 'Data_Report-6', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'Data_Report', 'Data_Report-7', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreNDA_BLA', 'PreNDA_BLA-0', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreNDA_BLA', 'PreNDA_BLA-1', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreNDA_BLA', 'PreNDA_BLA-2', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreNDA_BLA', 'PreNDA_BLA-3', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreNDA_BLA', 'PreNDA_BLA-4', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'PreNDA_BLA', 'PreNDA_BLA-5', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-302'),
  'NDA_BLA', 'NDA_BLA-0', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM2001-102  (PHASE_1, COMPLETED)  -> 35 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-03-01', '2022-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2022-04-10', '2022-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2022-05-20', '2022-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2022-06-29', '2022-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2022-08-08', '2022-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2022-09-17', '2022-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'IND', 'IND-0', NULL, NULL,
  '2022-10-27', '2022-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'IND', 'IND-1', NULL, NULL,
  '2022-12-06', '2022-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'IND', 'IND-2', NULL, NULL,
  '2023-01-15', '2023-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'IND', 'IND-3', NULL, NULL,
  '2023-02-24', '2023-03-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'IND', 'IND-4', NULL, NULL,
  '2023-04-05', '2023-04-25', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2023-05-15', '2023-06-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2023-06-24', '2023-07-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2023-08-03', '2023-08-23', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2023-09-12', '2023-10-02', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2023-10-22', '2023-11-11', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2023-12-01', '2023-12-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-01-10', '2024-01-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-02-19', '2024-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2024-03-30', '2024-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-0', NULL, NULL,
  '2024-05-09', '2024-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-1', NULL, NULL,
  '2024-06-18', '2024-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-2', NULL, NULL,
  '2024-07-28', '2024-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-3', NULL, NULL,
  '2024-09-06', '2024-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-4', NULL, NULL,
  '2024-10-16', '2024-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-5', NULL, NULL,
  '2024-11-25', '2024-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-01-04', '2025-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-02-13', '2025-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-8', NULL, NULL,
  '2025-03-25', '2025-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-9', NULL, NULL,
  '2025-05-04', '2025-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-10', NULL, NULL,
  '2025-06-13', '2025-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'SSU', 'SSU-11', NULL, NULL,
  '2025-07-23', '2025-08-12', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2025-09-01', '2025-09-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2025-10-11', '2025-10-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-102'),
  'Enrollment', 'Enrollment-2', NULL, NULL,
  '2025-11-20', '2025-12-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM2001-202  (PRE_3, ACTIVE)  -> 14 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2024-09-01', '2024-09-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2024-10-11', '2024-10-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2024-11-20', '2024-12-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2024-12-30', '2025-01-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2025-02-08', '2025-02-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2025-03-20', '2025-04-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'IND', 'IND-0', NULL, NULL,
  '2025-04-29', '2025-05-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'IND', 'IND-1', NULL, NULL,
  '2025-06-08', '2025-06-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'IND', 'IND-2', NULL, NULL,
  '2025-07-18', '2025-08-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'IND', 'IND-3', NULL, NULL,
  '2025-08-27', '2025-09-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'IND', 'IND-4', NULL, NULL,
  '2025-10-06', '2025-10-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2025-11-15', '2025-12-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2025-12-25', '2026-01-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM2001-202'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2026-02-03', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM3002-101  (IND, ACTIVE)  -> 10 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-101'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2025-02-15', '2025-03-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-101'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2025-03-27', '2025-04-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-101'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2025-05-06', '2025-05-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-101'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2025-06-15', '2025-07-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-101'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2025-07-25', '2025-08-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-101'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2025-09-03', '2025-09-23', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-101'),
  'IND', 'IND-0', NULL, NULL,
  '2025-10-13', '2025-11-02', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-101'),
  'IND', 'IND-1', NULL, NULL,
  '2025-11-22', '2025-12-12', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-101'),
  'IND', 'IND-2', NULL, NULL,
  '2026-01-01', '2026-01-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-101'),
  'IND', 'IND-3', NULL, NULL,
  '2026-02-10', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM3002-201  (PHASE_1, ACTIVE)  -> 34 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'IND', 'IND-0', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'IND', 'IND-1', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'IND', 'IND-2', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'IND', 'IND-3', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'IND', 'IND-4', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-0', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-1', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-2', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-3', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-4', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-5', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-8', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-9', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-10', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'SSU', 'SSU-11', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-201'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM3002-301  (PHASE_2, PLANNED)  -> 36 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-09-22', '2022-10-12', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2022-11-01', '2022-11-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'IND', 'IND-0', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'IND', 'IND-1', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'IND', 'IND-2', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'IND', 'IND-3', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'IND', 'IND-4', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-0', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-1', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-2', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-3', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-4', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-5', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-8', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-9', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-10', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'SSU', 'SSU-11', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'Enrollment', 'Enrollment-2', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM3002-301'),
  'IA', 'IA-0', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM1005-101  (PRE_IND, PLANNED)  -> 3 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-101'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2025-09-01', '2025-09-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-101'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2025-10-11', '2025-10-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-101'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2025-11-20', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM1005-201  (PHASE_1, ACTIVE)  -> 34 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'IND', 'IND-0', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'IND', 'IND-1', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'IND', 'IND-2', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'IND', 'IND-3', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'IND', 'IND-4', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-0', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-1', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-2', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-3', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-4', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-5', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-8', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-9', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-10', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'SSU', 'SSU-11', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-201'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM1005-301  (PHASE_2, ACTIVE)  -> 36 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-09-22', '2022-10-12', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2022-11-01', '2022-11-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'IND', 'IND-0', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'IND', 'IND-1', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'IND', 'IND-2', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'IND', 'IND-3', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'IND', 'IND-4', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-0', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-1', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-2', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-3', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-4', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-5', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-8', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-9', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-10', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'SSU', 'SSU-11', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'Enrollment', 'Enrollment-2', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-301'),
  'IA', 'IA-0', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM1005-401  (PHASE_3_1, COMPLETED)  -> 45 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2021-03-01', '2021-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2021-04-10', '2021-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2021-05-20', '2021-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2021-06-29', '2021-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2021-08-08', '2021-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2021-09-17', '2021-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'IND', 'IND-0', NULL, NULL,
  '2021-10-27', '2021-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'IND', 'IND-1', NULL, NULL,
  '2021-12-06', '2021-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'IND', 'IND-2', NULL, NULL,
  '2022-01-15', '2022-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'IND', 'IND-3', NULL, NULL,
  '2022-02-24', '2022-03-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'IND', 'IND-4', NULL, NULL,
  '2022-04-05', '2022-04-25', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2022-05-15', '2022-06-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2022-06-24', '2022-07-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2022-08-03', '2022-08-23', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2022-09-12', '2022-10-02', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2022-10-22', '2022-11-11', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2022-12-01', '2022-12-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2023-01-10', '2023-01-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2023-02-19', '2023-03-11', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2023-03-31', '2023-04-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-0', NULL, NULL,
  '2023-05-10', '2023-05-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-1', NULL, NULL,
  '2023-06-19', '2023-07-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-2', NULL, NULL,
  '2023-07-29', '2023-08-18', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-3', NULL, NULL,
  '2023-09-07', '2023-09-27', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-4', NULL, NULL,
  '2023-10-17', '2023-11-06', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-5', NULL, NULL,
  '2023-11-26', '2023-12-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-6', NULL, NULL,
  '2024-01-05', '2024-01-25', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-7', NULL, NULL,
  '2024-02-14', '2024-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-8', NULL, NULL,
  '2024-03-25', '2024-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-9', NULL, NULL,
  '2024-05-04', '2024-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-10', NULL, NULL,
  '2024-06-13', '2024-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'SSU', 'SSU-11', NULL, NULL,
  '2024-07-23', '2024-08-12', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2024-09-01', '2024-09-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2024-10-11', '2024-10-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Enrollment', 'Enrollment-2', NULL, NULL,
  '2024-11-20', '2024-12-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'IA', 'IA-0', NULL, NULL,
  '2024-12-30', '2025-01-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'IA', 'IA-1', NULL, NULL,
  '2025-02-08', '2025-02-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Data_Report', 'Data_Report-0', NULL, NULL,
  '2025-03-20', '2025-04-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Data_Report', 'Data_Report-1', NULL, NULL,
  '2025-04-29', '2025-05-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Data_Report', 'Data_Report-2', NULL, NULL,
  '2025-06-08', '2025-06-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Data_Report', 'Data_Report-3', NULL, NULL,
  '2025-07-18', '2025-08-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Data_Report', 'Data_Report-4', NULL, NULL,
  '2025-08-27', '2025-09-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Data_Report', 'Data_Report-5', NULL, NULL,
  '2025-10-06', '2025-10-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Data_Report', 'Data_Report-6', NULL, NULL,
  '2025-11-15', '2025-12-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM1005-401'),
  'Data_Report', 'Data_Report-7', NULL, NULL,
  '2025-12-25', '2026-01-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM4003-101  (PHASE_1, ACTIVE)  -> 34 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'IND', 'IND-0', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'IND', 'IND-1', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'IND', 'IND-2', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'IND', 'IND-3', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'IND', 'IND-4', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-0', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-1', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-2', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-3', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-4', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-5', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-8', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-9', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-10', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'SSU', 'SSU-11', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-101'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM4003-201  (PHASE_2, PLANNED)  -> 36 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-09-22', '2022-10-12', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2022-11-01', '2022-11-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'IND', 'IND-0', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'IND', 'IND-1', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'IND', 'IND-2', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'IND', 'IND-3', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'IND', 'IND-4', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-0', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-1', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-2', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-3', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-4', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-5', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-8', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-9', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-10', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'SSU', 'SSU-11', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'Enrollment', 'Enrollment-2', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM4003-201'),
  'IA', 'IA-0', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM5001-101  (PRE_IND, ACTIVE)  -> 3 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-101'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2025-08-10', '2025-08-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-101'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2025-09-19', '2025-10-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-101'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2025-10-29', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM5001-201  (PHASE_1, PLANNED)  -> 34 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'IND', 'IND-0', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'IND', 'IND-1', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'IND', 'IND-2', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'IND', 'IND-3', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'IND', 'IND-4', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-0', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-1', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-2', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-3', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-4', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-5', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-8', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-9', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-10', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'SSU', 'SSU-11', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM5001-201'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM6001-101  (PHASE_1, ACTIVE)  -> 34 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'IND', 'IND-0', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'IND', 'IND-1', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'IND', 'IND-2', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'IND', 'IND-3', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'IND', 'IND-4', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-0', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-1', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-2', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-3', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-4', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-5', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-8', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-9', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-10', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'SSU', 'SSU-11', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-101'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);

-- HDM6001-201  (PHASE_2, ACTIVE)  -> 36 milestone rows
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'PreIND', 'PreIND-0', NULL, NULL,
  '2022-09-22', '2022-10-12', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'PreIND', 'PreIND-1', NULL, NULL,
  '2022-11-01', '2022-11-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'PreIND', 'PreIND-2', NULL, NULL,
  '2022-12-11', '2022-12-31', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'PreIND', 'PreIND-3', NULL, NULL,
  '2023-01-20', '2023-02-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'PreIND', 'PreIND-4', NULL, NULL,
  '2023-03-01', '2023-03-21', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'PreIND', 'PreIND-5', NULL, NULL,
  '2023-04-10', '2023-04-30', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'IND', 'IND-0', NULL, NULL,
  '2023-05-20', '2023-06-09', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'IND', 'IND-1', NULL, NULL,
  '2023-06-29', '2023-07-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'IND', 'IND-2', NULL, NULL,
  '2023-08-08', '2023-08-28', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'IND', 'IND-3', NULL, NULL,
  '2023-09-17', '2023-10-07', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'IND', 'IND-4', NULL, NULL,
  '2023-10-27', '2023-11-16', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Pre3', 'Pre3-0', NULL, NULL,
  '2023-12-06', '2023-12-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Pre3', 'Pre3-1', NULL, NULL,
  '2024-01-15', '2024-02-04', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Pre3', 'Pre3-2', NULL, NULL,
  '2024-02-24', '2024-03-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Pre3', 'Pre3-3', NULL, NULL,
  '2024-04-04', '2024-04-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Pre3', 'Pre3-4', NULL, NULL,
  '2024-05-14', '2024-06-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Pre3', 'Pre3-5', NULL, NULL,
  '2024-06-23', '2024-07-13', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Protocol', 'Protocol-0', NULL, NULL,
  '2024-08-02', '2024-08-22', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Protocol', 'Protocol-1', NULL, NULL,
  '2024-09-11', '2024-10-01', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Protocol', 'Protocol-2', NULL, NULL,
  '2024-10-21', '2024-11-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-0', NULL, NULL,
  '2024-11-30', '2024-12-20', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-1', NULL, NULL,
  '2025-01-09', '2025-01-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-2', NULL, NULL,
  '2025-02-18', '2025-03-10', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-3', NULL, NULL,
  '2025-03-30', '2025-04-19', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-4', NULL, NULL,
  '2025-05-09', '2025-05-29', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-5', NULL, NULL,
  '2025-06-18', '2025-07-08', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-6', NULL, NULL,
  '2025-07-28', '2025-08-17', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-7', NULL, NULL,
  '2025-09-06', '2025-09-26', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-8', NULL, NULL,
  '2025-10-16', '2025-11-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-9', NULL, NULL,
  '2025-11-25', '2025-12-15', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-10', NULL, NULL,
  '2026-01-04', '2026-01-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'SSU', 'SSU-11', NULL, NULL,
  '2026-02-13', '2026-03-05', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Enrollment', 'Enrollment-0', NULL, NULL,
  '2026-03-25', '2026-04-14', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Enrollment', 'Enrollment-1', NULL, NULL,
  '2026-05-04', '2026-05-24', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'Enrollment', 'Enrollment-2', NULL, NULL,
  '2026-06-13', '2026-07-03', NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
INSERT INTO hd_plt_study_milestone (
  study_id, stage_code, milestone_code, plan_v1_date, plan_v2_date,
  actual_start_date, actual_end_date, deviation_note, sys_create_by, sys_update_by)
VALUES (
  (SELECT id FROM hd_plt_study WHERE study_code = 'HDM6001-201'),
  'IA', 'IA-0', NULL, NULL,
  '2026-07-23', NULL, NULL, 'seed', 'seed')
ON DUPLICATE KEY UPDATE
  plan_v1_date = VALUES(plan_v1_date),
  plan_v2_date = VALUES(plan_v2_date),
  actual_start_date = VALUES(actual_start_date),
  actual_end_date = VALUES(actual_end_date),
  sys_update_by = VALUES(sys_update_by),
  sys_update_time = CURRENT_TIMESTAMP(6);
