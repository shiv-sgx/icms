-- ============================================================================
-- TEST-ONLY database reset for the ICMS Cucumber suite.
-- Restores the exact seed baseline (mirrors src/main/resources/db/seed.sql) so
-- every scenario starts from identical, known state (test isolation).
--
-- Data-only (DML): assumes the schema already exists (created by setup.sh).
-- The __PWHASH__ token from seed.sql is pre-substituted with a real jBCrypt
-- hash of the demo password "Password@123" (verified via BCrypt.checkpw), so no
-- runtime substitution is needed. Any valid bcrypt hash of that password works.
-- ============================================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE audit_logs;
TRUNCATE TABLE notifications;
TRUNCATE TABLE communications;
TRUNCATE TABLE settlements;
TRUNCATE TABLE approvals;
TRUNCATE TABLE assessment_components;
TRUNCATE TABLE assessments;
TRUNCATE TABLE claim_documents;
TRUNCATE TABLE claims;
TRUNCATE TABLE policies;
TRUNCATE TABLE policyholders;
TRUNCATE TABLE users;
TRUNCATE TABLE roles;
TRUNCATE TABLE document_requirements;
TRUNCATE TABLE notification_templates;
TRUNCATE TABLE sla_config;
TRUNCATE TABLE approval_thresholds;
SET FOREIGN_KEY_CHECKS = 1;

-- Roles
INSERT INTO roles (id, name, description) VALUES
 (1,'CUSTOMER','Policyholder / claimant portal access'),
 (2,'AGENT','Handles claims, assigns surveyors, processes settlements'),
 (3,'SURVEYOR','Performs damage assessment in the field'),
 (4,'MANAGER','Approves/rejects claims, oversees workload'),
 (5,'ADMIN','Full system administration');

-- Users (all share demo password "Password@123" via the pre-computed hash)
INSERT INTO users (id, full_name, email, username, password_hash, role_id, branch, status) VALUES
 (1,'System Administrator','admin@icms.local','admin','$2a$10$svwhNEJVCWQYiQkcPcew.OXJOVOWMAEXBntM2p11RYd3KCGkBXWHu',5,'HQ','ACTIVE'),
 (2,'Michael Anderson','michael.anderson@icms.local','manager','$2a$10$svwhNEJVCWQYiQkcPcew.OXJOVOWMAEXBntM2p11RYd3KCGkBXWHu',4,'New York','ACTIVE'),
 (3,'Jennifer Martinez','jennifer.martinez@icms.local','agent','$2a$10$svwhNEJVCWQYiQkcPcew.OXJOVOWMAEXBntM2p11RYd3KCGkBXWHu',2,'New York','ACTIVE'),
 (4,'Sarah Thompson','sarah.thompson@icms.local','sunita','$2a$10$svwhNEJVCWQYiQkcPcew.OXJOVOWMAEXBntM2p11RYd3KCGkBXWHu',2,'Chicago','ACTIVE'),
 (5,'David Wilson','david.wilson@icms.local','surveyor','$2a$10$svwhNEJVCWQYiQkcPcew.OXJOVOWMAEXBntM2p11RYd3KCGkBXWHu',3,'New York','ACTIVE'),
 (6,'Robert Garcia','robert.garcia@icms.local','vinod','$2a$10$svwhNEJVCWQYiQkcPcew.OXJOVOWMAEXBntM2p11RYd3KCGkBXWHu',3,'Chicago','ACTIVE'),
 (7,'James Miller','james.miller@example.com','customer','$2a$10$svwhNEJVCWQYiQkcPcew.OXJOVOWMAEXBntM2p11RYd3KCGkBXWHu',1,NULL,'ACTIVE'),
 (8,'Emily Davis','emily.davis@example.com','meera','$2a$10$svwhNEJVCWQYiQkcPcew.OXJOVOWMAEXBntM2p11RYd3KCGkBXWHu',1,NULL,'ACTIVE');

-- Policyholders
INSERT INTO policyholders (id, first_name, last_name, dob, email, mobile, address, city, state, pin_code) VALUES
 (1,'James','Miller','1985-04-12','james.miller@example.com','+1-212-555-0101','21 Broadway','New York','NY','10001'),
 (2,'Emily','Davis','1990-09-30','emily.davis@example.com','+1-312-555-0102','8 Michigan Ave','Chicago','IL','60601'),
 (3,'William','Brown','1978-01-22','william.brown@example.com','+1-213-555-0103','14 Sunset Blvd','Los Angeles','CA','90001'),
 (4,'Olivia','Jones','1995-07-08','olivia.jones@example.com','+1-713-555-0104','5 Main St','Houston','TX','77001'),
 (5,'Christopher','Lee','1982-11-15','christopher.lee@example.com','+1-617-555-0105','3 Beacon St','Boston','MA','02101'),
 (6,'Jessica','Taylor','1988-03-19','jessica.taylor@example.com','+1-206-555-0106','9 Pike St','Seattle','WA','98101');

-- Policies
INSERT INTO policies (id, policy_no, policyholder_id, type, sum_insured, premium, start_date, expiry_date, ncb_discount, status) VALUES
 (1,'POL-84521',1,'MOTOR',800000,18500,'2024-04-01','2025-03-31',20.00,'ACTIVE'),
 (2,'POL-84522',2,'HEALTH',1000000,24000,'2024-06-01','2025-05-31',0.00,'ACTIVE'),
 (3,'POL-84523',3,'PROPERTY',5000000,42000,'2024-01-15','2025-01-14',0.00,'ACTIVE'),
 (4,'POL-84524',4,'LIFE',10000000,55000,'2023-09-01','2043-08-31',0.00,'ACTIVE'),
 (5,'POL-84525',5,'MOTOR',600000,14200,'2024-07-10','2025-07-09',35.00,'ACTIVE'),
 (6,'POL-84526',6,'TRAVEL',300000,3500,'2024-10-01','2025-09-30',0.00,'ACTIVE');

-- Claims (mix of statuses for realistic dashboards/lists)
INSERT INTO claims (id, claim_no, policy_id, policyholder_id, claimant_name, claim_type, claim_subtype,
        incident_date, incident_time, incident_location, city, state, pin_code, description, estimated_loss,
        vehicle_reg_no, fir_number, police_station, status, agent_id, surveyor_id, risk_level, fraud_score,
        sla_due_date, filed_at) VALUES
 (1,'CLM-2024-0891',1,1,'James Miller','MOTOR','Accident','2024-11-02','14:30','I-95 Expressway','New York','NY','10002',
    'Front-end collision at signal; third party involved.',85000,'NY-AB-1234','PR-2231','Midtown PD','PENDING_APPROVAL',3,5,'HIGH',72,'2024-11-23','2024-11-03 09:15:00'),
 (2,'CLM-2024-0879',2,2,'Emily Davis','HEALTH','Surgery','2024-10-20',NULL,'Mercy Hospital','Chicago','IL','60601',
    'Planned surgery hospitalization for 4 days.',120000,NULL,NULL,NULL,'UNDER_ASSESSMENT',4,6,'MEDIUM',35,'2024-11-10','2024-10-22 11:40:00'),
 (3,'CLM-2024-0874',3,3,'William Brown','PROPERTY','Natural Disaster','2024-10-10',NULL,'Sunset Blvd','Los Angeles','CA','90001',
    'Fire damage to commercial structure.',340000,NULL,'PR-1180','Central PD','PENDING_APPROVAL',3,5,'LOW',18,'2024-10-31','2024-10-12 16:05:00'),
 (4,'CLM-2024-0871',4,4,'Olivia Jones','LIFE','Disability','2024-09-28',NULL,'Memorial Dr','Houston','TX','77001',
    'Permanent partial disability claim.',1000000,NULL,NULL,NULL,'PENDING_APPROVAL',4,6,'HIGH',64,'2024-10-19','2024-09-30 10:00:00'),
 (5,'CLM-2024-0880',5,5,'Christopher Lee','MOTOR','Theft','2024-10-05','22:10','Beacon St parking','Boston','MA','02101',
    'Vehicle stolen from parking lot.',420000,'MA-XY-9921','PR-0998','Downtown PD','SETTLED',3,5,'MEDIUM',40,'2024-10-26','2024-10-06 08:30:00'),
 (6,'CLM-2024-0870',1,1,'James Miller','MOTOR','Glass Damage','2024-09-15',NULL,'Broadway','New York','NY','10001',
    'Windshield cracked; insufficient documents.',12000,'NY-AB-1234',NULL,NULL,'REJECTED',3,NULL,'LOW',22,'2024-10-06','2024-09-16 13:20:00'),
 (7,'CLM-2024-0862',2,2,'Emily Davis','HEALTH','Hospitalization','2024-09-01',NULL,'Mercy Hospital','Chicago','IL','60601',
    'Emergency hospitalization 2 days.',60000,NULL,NULL,NULL,'SETTLED',4,6,'LOW',15,'2024-09-22','2024-09-02 19:10:00'),
 (8,'CLM-2024-0858',3,3,'William Brown','PROPERTY','Burglary','2024-08-25',NULL,'Sunset Blvd','Los Angeles','CA','90001',
    'Office burglary, equipment stolen.',180000,NULL,'PR-0871','Central PD','SURVEY_SCHEDULED',3,5,'MEDIUM',48,'2024-09-15','2024-08-27 09:45:00'),
 (9,'CLM-2024-0850',5,5,'Christopher Lee','MOTOR','Accident','2024-08-10','08:05','I-90 Bypass','Boston','MA','02101',
    'Rear-ended by truck; moderate damage.',95000,'MA-XY-9921','PR-0810','Downtown PD','UNDER_REVIEW',4,NULL,'MEDIUM',38,'2024-08-31','2024-08-11 12:00:00'),
 (10,'CLM-2024-0845',6,6,'Jessica Taylor','TRAVEL','Trip Cancellation','2024-08-01',NULL,'Seattle-Tacoma Airport','Seattle','WA','98101',
    'International trip cancelled due to medical emergency.',45000,NULL,NULL,NULL,'SUBMITTED',NULL,NULL,'LOW',10,'2024-08-22','2024-08-02 07:30:00'),
 (11,'CLM-2024-0838',1,1,'James Miller','MOTOR','Fire','2024-07-19','17:45','Times Square','New York','NY','10002',
    'Engine fire, total loss suspected.',650000,'NY-AB-1234','PR-0777','Midtown PD','APPROVED',3,5,'HIGH',69,'2024-08-09','2024-07-20 10:15:00'),
 (12,'CLM-2024-0830',4,4,'Olivia Jones','LIFE','Critical Illness','2024-07-05',NULL,'Houston','Houston','TX','77001',
    'Critical illness diagnosis claim.',500000,NULL,NULL,NULL,'ON_HOLD',4,6,'HIGH',81,'2024-07-26','2024-07-06 14:50:00');

-- Documents for a couple of claims
INSERT INTO claim_documents (claim_id, doc_type, file_name, upload_status, verification_status, uploaded_at) VALUES
 (1,'Policy Document','policy_84521.pdf','UPLOADED','VERIFIED','2024-11-03 09:20:00'),
 (1,'Police Report Copy','pr_2231.pdf','UPLOADED','VERIFIED','2024-11-03 09:22:00'),
 (1,'Photographs of Damage','damage_front.jpg','UPLOADED','UNDER_REVIEW','2024-11-03 09:25:00'),
 (1,'Repair Estimate','estimate_garage.pdf','PENDING','PENDING',NULL),
 (1,'Driver License','dl_james.pdf','UPLOADED','VERIFIED','2024-11-03 09:27:00'),
 (3,'Policy Document','policy_84523.pdf','UPLOADED','VERIFIED','2024-10-12 16:10:00'),
 (3,'Survey Report','survey_1180.pdf','UPLOADED','VERIFIED','2024-10-15 11:00:00');

-- Assessment for claim 1 (motor accident) with component breakdown
INSERT INTO assessments (id, claim_id, surveyor_id, visit_date, visit_time, site_observations, report_ref_no,
        gross_assessed, policy_deductible, depreciation_pct, depreciation_amt, salvage_value, net_payable,
        recommendation, remarks, status) VALUES
 (1,1,5,'2024-11-08','11:00','Front structural damage consistent with reported collision. No pre-existing damage observed.',
    'SRV-2024-0891',44800,5000,15.00,6720,3000,30080,'PARTIAL_APPROVE','Recommend partial approval after standard deductions.','SUBMITTED');

INSERT INTO assessment_components (assessment_id, component, severity, repair_cost, replace_flag) VALUES
 (1,'Front Bumper','SEVERE',12000,TRUE),
 (1,'Bonnet','MODERATE',8500,FALSE),
 (1,'Headlight Assembly','SEVERE',9800,TRUE),
 (1,'Radiator','MODERATE',7500,FALSE),
 (1,'Front Grille','MINOR',7000,FALSE);

-- Approvals (multi-level) for pending-approval claims
INSERT INTO approvals (claim_id, level, approver_id, approver_role, decision, remarks, decided_at) VALUES
 (1,'L1',3,'AGENT','APPROVED','Initial review complete; docs largely in order.','2024-11-09 10:00:00'),
 (1,'L2',2,'MANAGER','PENDING',NULL,NULL),
 (3,'L1',3,'AGENT','APPROVED','Verified survey report.','2024-10-16 12:00:00'),
 (3,'L2',2,'MANAGER','PENDING',NULL,NULL),
 (4,'L1',4,'AGENT','APPROVED','Disability certificate verified.','2024-10-02 09:30:00'),
 (4,'L2',2,'MANAGER','PENDING',NULL,NULL),
 (4,'L3',NULL,'DIRECTOR','PENDING',NULL,NULL),
 (11,'L1',3,'AGENT','APPROVED','Fire claim, survey supports total loss.','2024-07-28 10:00:00'),
 (11,'L2',2,'MANAGER','APPROVED','Approved for settlement.','2024-08-01 15:30:00');

-- Settlements for settled claims
INSERT INTO settlements (claim_id, final_amount, justification, payment_method, account_holder, bank_name,
        account_number, ifsc_code, status, approved_by, approval_date, payment_confirmed_at, closed_at) VALUES
 (5,380000,'Theft claim settled per policy terms.','NEFT','Christopher Lee','Bank of America','50100123456789','026009593','CLOSED',2,'2024-10-18 10:00:00','2024-10-20 14:00:00','2024-10-21 09:00:00'),
 (7,52000,'Hospitalization within sub-limits.','NEFT','Emily Davis','Wells Fargo','60200987654321','121000248','CLOSED',2,'2024-09-18 11:00:00','2024-09-20 13:00:00','2024-09-21 10:00:00');

-- Communications on claim 1
INSERT INTO communications (claim_id, sender_id, sender_name, channel, content, created_at) VALUES
 (1,3,'Jennifer Martinez','SMS','Your claim CLM-2024-0891 has been acknowledged.','2024-11-03 09:30:00'),
 (1,7,'James Miller','MESSAGE','When will the surveyor visit?','2024-11-05 10:00:00'),
 (1,5,'David Wilson','MESSAGE','Site visit scheduled for 8 Nov, 11 AM.','2024-11-06 12:00:00');

-- Notifications
INSERT INTO notifications (user_id, target_role, type, message, is_read) VALUES
 (2,'MANAGER','ACTION','3 claims awaiting L2 approval.',FALSE),
 (NULL,'MANAGER','URGENT','SLA breach: CLM-2024-0870 overdue 2 days.',FALSE),
 (NULL,'AGENT','INFO','Surveyor report uploaded: CLM-2024-0858.',FALSE);

-- Audit log samples
INSERT INTO audit_logs (ts, user_id, username, role, action, entity, ip_address, result) VALUES
 ('2024-11-09 10:00:00',3,'agent','AGENT','CLAIM_APPROVED','CLM-2024-0891 (L1)','127.0.0.1','SUCCESS'),
 ('2024-11-06 12:00:00',5,'surveyor','SURVEYOR','REPORT_UPLOAD','CLM-2024-0858','127.0.0.1','SUCCESS'),
 ('2024-11-05 09:00:00',1,'admin','ADMIN','ROLE_CHANGE','user:sunita','127.0.0.1','SUCCESS'),
 ('2024-11-04 08:00:00',NULL,'unknown',NULL,'LOGIN_FAIL','username:agent','127.0.0.1','FAILED');

-- Approval thresholds
INSERT INTO approval_thresholds (level, label, min_amount, max_amount) VALUES
 ('L1','Agent Approval',0,25000),
 ('L2','Manager Approval',25001,100000),
 ('L3','Director Approval',100001,NULL);

-- SLA configuration (hours)
INSERT INTO sla_config (stage, hours) VALUES
 ('Claim Acknowledgement',24),
 ('Surveyor Assignment',48),
 ('Assessment Completion',120),
 ('Approval Decision',72),
 ('Settlement Processing',168),
 ('Total End-to-End',504);

-- Document requirements
INSERT INTO document_requirements (claim_type, claim_subtype, doc_type, required) VALUES
 ('MOTOR','Accident','Police Report',TRUE),
 ('MOTOR','Accident','Photographs of Damage',TRUE),
 ('MOTOR','Accident','Repair Estimate from Garage',TRUE),
 ('MOTOR','Theft','Police Report',TRUE),
 ('MOTOR','Theft','Vehicle Title',TRUE),
 ('MOTOR','Theft','Driver License',TRUE),
 ('HEALTH','Hospitalization','Medical Bills & Receipts',TRUE),
 ('HEALTH','Hospitalization','Hospital Discharge Summary',TRUE),
 ('PROPERTY','Fire','Police Report',TRUE),
 ('PROPERTY','Fire','Survey Report',TRUE);

-- Notification templates
INSERT INTO notification_templates (name, channel, active, body) VALUES
 ('Claim Acknowledgement','SMS',TRUE,'Your claim {claimNo} has been acknowledged.'),
 ('Survey Scheduled','SMS',TRUE,'Surveyor visit for {claimNo} scheduled on {date}.'),
 ('Settlement Confirmation','EMAIL',TRUE,'Your claim {claimNo} has been settled for {amount}.'),
 ('Rejection Notice','EMAIL',TRUE,'Your claim {claimNo} could not be approved. Reason: {reason}.');
