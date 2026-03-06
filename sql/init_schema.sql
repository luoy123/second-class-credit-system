USE second_class_credit;

CREATE TABLE IF NOT EXISTS sc_student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(32) NOT NULL,
    name VARCHAR(64) NOT NULL,
    major VARCHAR(64),
    grade VARCHAR(16),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sc_student_no UNIQUE (student_no)
);

CREATE TABLE IF NOT EXISTS sc_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(128) NOT NULL,
    category VARCHAR(64) NOT NULL,
    organizer VARCHAR(128),
    activity_date DATE NOT NULL,
    max_credit DECIMAL(8, 2) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sc_credit_rule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    category VARCHAR(64) NOT NULL,
    base_credit DECIMAL(8, 2) NOT NULL,
    description VARCHAR(255),
    enabled TINYINT(1) NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_sc_credit_rule_category UNIQUE (category)
);

CREATE TABLE IF NOT EXISTS sc_credit_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    category VARCHAR(64) NOT NULL,
    credit DECIMAL(8, 2) NOT NULL,
    status VARCHAR(16) NOT NULL,
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sc_credit_record_student FOREIGN KEY (student_id) REFERENCES sc_student(id),
    CONSTRAINT fk_sc_credit_record_activity FOREIGN KEY (activity_id) REFERENCES sc_activity(id),
    INDEX idx_sc_credit_record_student_status (student_id, status),
    INDEX idx_sc_credit_record_status_created (status, created_at),
    INDEX idx_sc_credit_record_created_at (created_at)
);

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
