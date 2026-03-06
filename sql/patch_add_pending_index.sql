USE second_class_credit;

SET @idx_exists = (
    SELECT COUNT(1)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'sc_credit_record'
      AND index_name = 'idx_sc_credit_record_status_created'
);

SET @sql = IF(
    @idx_exists = 0,
    'ALTER TABLE sc_credit_record ADD INDEX idx_sc_credit_record_status_created (status, created_at)',
    'SELECT \"idx_sc_credit_record_status_created already exists\"'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
