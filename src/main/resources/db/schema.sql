-- ============================================================================
-- Insurance Claim Management System (ICMS) - MySQL schema
-- Idempotent: safe to re-run. Drops in FK-safe order, then recreates.
-- ============================================================================
CREATE DATABASE IF NOT EXISTS icms
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE icms;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS communications;
DROP TABLE IF EXISTS settlements;
DROP TABLE IF EXISTS approvals;
DROP TABLE IF EXISTS assessment_components;
DROP TABLE IF EXISTS assessments;
DROP TABLE IF EXISTS claim_documents;
DROP TABLE IF EXISTS claims;
DROP TABLE IF EXISTS policies;
DROP TABLE IF EXISTS policyholders;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS document_requirements;
DROP TABLE IF EXISTS notification_templates;
DROP TABLE IF EXISTS sla_config;
DROP TABLE IF EXISTS approval_thresholds;
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------------
-- Identity & access
-- ---------------------------------------------------------------------------
CREATE TABLE roles (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(30)  NOT NULL UNIQUE,   -- CUSTOMER, AGENT, SURVEYOR, MANAGER, ADMIN
    description VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(120) NOT NULL,
    email         VARCHAR(160) NOT NULL UNIQUE,
    username      VARCHAR(80)  NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,        -- BCrypt
    role_id       INT          NOT NULL,
    branch        VARCHAR(80),
    status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    last_login    DATETIME NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id),
    INDEX idx_users_role (role_id),
    INDEX idx_users_status (status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Policyholders & policies
-- ---------------------------------------------------------------------------
CREATE TABLE policyholders (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(80) NOT NULL,
    last_name  VARCHAR(80) NOT NULL,
    dob        DATE,
    email      VARCHAR(160),
    mobile     VARCHAR(20),
    address    VARCHAR(255),
    city       VARCHAR(80),
    state      VARCHAR(80),
    pin_code   VARCHAR(12),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ph_name (last_name, first_name),
    INDEX idx_ph_mobile (mobile)
) ENGINE=InnoDB;

CREATE TABLE policies (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    policy_no       VARCHAR(30) NOT NULL UNIQUE,            -- POL-#####
    policyholder_id BIGINT NOT NULL,
    type            ENUM('MOTOR','HEALTH','PROPERTY','LIFE','TRAVEL','LIABILITY') NOT NULL,
    sum_insured     DECIMAL(15,2) NOT NULL DEFAULT 0,
    premium         DECIMAL(15,2) NOT NULL DEFAULT 0,
    start_date      DATE,
    expiry_date     DATE,
    ncb_discount    DECIMAL(5,2)  NOT NULL DEFAULT 0,       -- percent
    status          ENUM('ACTIVE','EXPIRED','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pol_holder FOREIGN KEY (policyholder_id) REFERENCES policyholders(id),
    INDEX idx_pol_holder (policyholder_id),
    INDEX idx_pol_status (status),
    INDEX idx_pol_type (type)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Claims (core entity) + supporting documents
-- ---------------------------------------------------------------------------
CREATE TABLE claims (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_no         VARCHAR(30) NOT NULL UNIQUE,           -- CLM-YYYY-####
    policy_id        BIGINT NOT NULL,
    policyholder_id  BIGINT NOT NULL,
    claimant_name    VARCHAR(120) NOT NULL,
    claim_type       ENUM('MOTOR','HEALTH','PROPERTY','LIFE','TRAVEL','LIABILITY') NOT NULL,
    claim_subtype    VARCHAR(60),
    incident_date    DATE,
    incident_time    TIME NULL,
    incident_location VARCHAR(255),
    city             VARCHAR(80),
    state            VARCHAR(80),
    pin_code         VARCHAR(12),
    description      TEXT,
    estimated_loss   DECIMAL(15,2) NOT NULL DEFAULT 0,
    -- claim-type specific optional fields
    vehicle_reg_no   VARCHAR(30),
    fir_number       VARCHAR(60),
    police_station   VARCHAR(120),
    hospital_name    VARCHAR(120),
    workshop_name    VARCHAR(120),
    third_party      VARCHAR(255),
    -- workflow
    status           ENUM('DRAFT','SUBMITTED','UNDER_REVIEW','SURVEY_SCHEDULED',
                          'UNDER_ASSESSMENT','PENDING_APPROVAL','APPROVED',
                          'SETTLEMENT_PROCESSING','SETTLED','CLOSED','REJECTED',
                          'WITHDRAWN','ON_HOLD') NOT NULL DEFAULT 'SUBMITTED',
    agent_id         BIGINT NULL,
    surveyor_id      BIGINT NULL,
    risk_level       ENUM('LOW','MEDIUM','HIGH') NOT NULL DEFAULT 'LOW',
    fraud_score      INT NOT NULL DEFAULT 0,                -- 0-100 (seeded; no live ML)
    internal_notes   TEXT,
    sla_due_date     DATE NULL,
    filed_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_claim_policy FOREIGN KEY (policy_id) REFERENCES policies(id),
    CONSTRAINT fk_claim_holder FOREIGN KEY (policyholder_id) REFERENCES policyholders(id),
    CONSTRAINT fk_claim_agent FOREIGN KEY (agent_id) REFERENCES users(id),
    CONSTRAINT fk_claim_surveyor FOREIGN KEY (surveyor_id) REFERENCES users(id),
    INDEX idx_claim_status (status),
    INDEX idx_claim_type (claim_type),
    INDEX idx_claim_agent (agent_id),
    INDEX idx_claim_surveyor (surveyor_id),
    INDEX idx_claim_filed (filed_at),
    INDEX idx_claim_risk (risk_level)
) ENGINE=InnoDB;

CREATE TABLE claim_documents (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id            BIGINT NOT NULL,
    doc_type            VARCHAR(120) NOT NULL,
    file_name           VARCHAR(255),
    file_path           VARCHAR(500),
    upload_status       ENUM('REQUIRED','CONDITIONAL','PENDING','UPLOADED','MISSING','NA')
                          NOT NULL DEFAULT 'PENDING',
    verification_status ENUM('PENDING','UNDER_REVIEW','VERIFIED','FLAGGED')
                          NOT NULL DEFAULT 'PENDING',
    uploaded_at         DATETIME NULL,
    CONSTRAINT fk_doc_claim FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE,
    INDEX idx_doc_claim (claim_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Assessment (surveyor) + component breakdown
-- ---------------------------------------------------------------------------
CREATE TABLE assessments (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id           BIGINT NOT NULL,
    surveyor_id        BIGINT NULL,
    visit_date         DATE NULL,
    visit_time         TIME NULL,
    site_observations  TEXT,
    report_ref_no      VARCHAR(60),
    gross_assessed     DECIMAL(15,2) NOT NULL DEFAULT 0,
    policy_deductible  DECIMAL(15,2) NOT NULL DEFAULT 0,
    depreciation_pct   DECIMAL(5,2)  NOT NULL DEFAULT 0,
    depreciation_amt   DECIMAL(15,2) NOT NULL DEFAULT 0,
    salvage_value      DECIMAL(15,2) NOT NULL DEFAULT 0,
    net_payable        DECIMAL(15,2) NOT NULL DEFAULT 0,
    recommendation     ENUM('APPROVE_FULL','PARTIAL_APPROVE','REJECT') NULL,
    remarks            TEXT,
    status             ENUM('ASSIGNED','IN_PROGRESS','SUBMITTED') NOT NULL DEFAULT 'ASSIGNED',
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_asm_claim FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE,
    CONSTRAINT fk_asm_surveyor FOREIGN KEY (surveyor_id) REFERENCES users(id),
    INDEX idx_asm_claim (claim_id)
) ENGINE=InnoDB;

CREATE TABLE assessment_components (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    assessment_id BIGINT NOT NULL,
    component     VARCHAR(120) NOT NULL,
    severity      ENUM('MINOR','MODERATE','SEVERE') NOT NULL DEFAULT 'MODERATE',
    repair_cost   DECIMAL(15,2) NOT NULL DEFAULT 0,
    replace_flag  BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_comp_asm FOREIGN KEY (assessment_id) REFERENCES assessments(id) ON DELETE CASCADE,
    INDEX idx_comp_asm (assessment_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Multi-level approvals
-- ---------------------------------------------------------------------------
CREATE TABLE approvals (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id      BIGINT NOT NULL,
    level         ENUM('L1','L2','L3') NOT NULL,
    approver_id   BIGINT NULL,
    approver_role VARCHAR(30),
    decision      ENUM('PENDING','APPROVED','CONDITIONAL','REJECTED','RETURNED','ON_HOLD','NA')
                    NOT NULL DEFAULT 'PENDING',
    remarks       TEXT,
    decided_at    DATETIME NULL,
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appr_claim FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE,
    CONSTRAINT fk_appr_user FOREIGN KEY (approver_id) REFERENCES users(id),
    INDEX idx_appr_claim (claim_id),
    INDEX idx_appr_decision (decision)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Settlement + payment tracking
-- ---------------------------------------------------------------------------
CREATE TABLE settlements (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id            BIGINT NOT NULL UNIQUE,
    final_amount        DECIMAL(15,2) NOT NULL DEFAULT 0,
    justification       VARCHAR(500),
    payment_method      ENUM('NEFT','CHEQUE','DEMAND_DRAFT','DIRECT_TO_WORKSHOP') NOT NULL DEFAULT 'NEFT',
    account_holder      VARCHAR(120),
    bank_name           VARCHAR(120),
    account_number      VARCHAR(40),
    ifsc_code           VARCHAR(20),
    status              ENUM('AUTHORIZED','PAYMENT_INITIATED','BANK_PROCESSING',
                            'PAYMENT_CONFIRMED','CLAIMANT_NOTIFIED','CLOSED')
                          NOT NULL DEFAULT 'AUTHORIZED',
    approved_by         BIGINT NULL,
    approval_date       DATETIME NULL,
    payment_initiated_at DATETIME NULL,
    payment_confirmed_at DATETIME NULL,
    closed_at           DATETIME NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_settle_claim FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE,
    CONSTRAINT fk_settle_user FOREIGN KEY (approved_by) REFERENCES users(id),
    INDEX idx_settle_status (status)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Communications, notifications, audit
-- ---------------------------------------------------------------------------
CREATE TABLE communications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    claim_id    BIGINT NOT NULL,
    sender_id   BIGINT NULL,
    sender_name VARCHAR(120),
    channel     ENUM('SMS','EMAIL','CALL','MESSAGE') NOT NULL DEFAULT 'MESSAGE',
    content     TEXT,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comm_claim FOREIGN KEY (claim_id) REFERENCES claims(id) ON DELETE CASCADE,
    CONSTRAINT fk_comm_user FOREIGN KEY (sender_id) REFERENCES users(id),
    INDEX idx_comm_claim (claim_id)
) ENGINE=InnoDB;

CREATE TABLE notifications (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT NULL,
    target_role VARCHAR(30) NULL,
    type        ENUM('ACTION','URGENT','INFO') NOT NULL DEFAULT 'INFO',
    message     VARCHAR(500) NOT NULL,
    is_read     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_notif_user (user_id),
    INDEX idx_notif_role (target_role)
) ENGINE=InnoDB;

CREATE TABLE audit_logs (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    ts         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id    BIGINT NULL,
    username   VARCHAR(80),
    role       VARCHAR(30),
    action     VARCHAR(80) NOT NULL,
    entity     VARCHAR(120),
    ip_address VARCHAR(45),
    result     ENUM('SUCCESS','FAILED') NOT NULL DEFAULT 'SUCCESS',
    INDEX idx_audit_ts (ts),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_action (action)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Configuration (admin-managed)
-- ---------------------------------------------------------------------------
CREATE TABLE approval_thresholds (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    level      VARCHAR(10) NOT NULL,        -- L1/L2/L3
    label      VARCHAR(80) NOT NULL,
    min_amount DECIMAL(15,2) NOT NULL DEFAULT 0,
    max_amount DECIMAL(15,2) NULL           -- NULL = no upper bound
) ENGINE=InnoDB;

CREATE TABLE sla_config (
    id    INT AUTO_INCREMENT PRIMARY KEY,
    stage VARCHAR(80) NOT NULL,
    hours INT NOT NULL
) ENGINE=InnoDB;

CREATE TABLE document_requirements (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    claim_type    VARCHAR(30) NOT NULL,
    claim_subtype VARCHAR(60),
    doc_type      VARCHAR(120) NOT NULL,
    required      BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE notification_templates (
    id      INT AUTO_INCREMENT PRIMARY KEY,
    name    VARCHAR(120) NOT NULL,
    channel ENUM('SMS','EMAIL') NOT NULL,
    active  BOOLEAN NOT NULL DEFAULT TRUE,
    body    TEXT
) ENGINE=InnoDB;
