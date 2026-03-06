USE second_class_credit;

CREATE TABLE IF NOT EXISTS sc_credit_review_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT NOT NULL,
    action VARCHAR(16) NOT NULL,
    operator_role VARCHAR(32) NOT NULL,
    success TINYINT(1) NOT NULL,
    remark VARCHAR(255),
    detail VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_sc_credit_review_log_record_created (record_id, created_at),
    INDEX idx_sc_credit_review_log_action_created (action, created_at)
);
