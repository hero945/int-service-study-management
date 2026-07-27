-- Spring Session appends the case-sensitive suffix "_ATTRIBUTES" to the configured
-- base table name. Linux MySQL defaults to lower_case_table_names=0, so align the
-- Flyway-created lowercase table without changing already-applied migrations.
SET @align_session_attributes_table_case = IF(
    @@lower_case_table_names = 0
    AND EXISTS(
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND BINARY table_name = BINARY 'hd_plt_spring_session_attributes')
    AND NOT EXISTS(
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = DATABASE()
          AND BINARY table_name = BINARY 'hd_plt_spring_session_ATTRIBUTES'),
    'RENAME TABLE `hd_plt_spring_session_attributes` TO `hd_plt_spring_session_ATTRIBUTES`',
    'SELECT 1');

PREPARE align_session_attributes_table_case_stmt
    FROM @align_session_attributes_table_case;
EXECUTE align_session_attributes_table_case_stmt;
DEALLOCATE PREPARE align_session_attributes_table_case_stmt;
