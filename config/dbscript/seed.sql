-- ============================================================================
-- ICMS seed data. Re-runnable: clears data tables, then inserts.
-- Every user's password is the same demo password; the hash is injected by the
-- setup script (replaces the __PWHASH__ token with a real BCrypt hash).
-- ============================================================================
USE icms;

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

-- Users (demo password applied to all via __PWHASH__)
INSERT INTO users (id, full_name, email, username, password_hash, role_id, branch, status) VALUES
 (1,'System Administrator','admin@icms.local','admin','__PWHASH__',5,'HQ','ACTIVE'),
 (2,'Anand Sharma','anand.sharma@icms.local','manager','__PWHASH__',4,'Mumbai','ACTIVE'),
 (3,'Priya Kumar','priya.kumar@icms.local','agent','__PWHASH__',2,'Mumbai','ACTIVE'),
 (4,'Sunita Shah','sunita.shah@icms.local','sunita','__PWHASH__',2,'Pune','ACTIVE'),
 (5,'Raj Mehta','raj.mehta@icms.local','surveyor','__PWHASH__',3,'Mumbai','ACTIVE'),
 (6,'Vinod Kulkarni','vinod.kulkarni@icms.local','vinod','__PWHASH__',3,'Pune','ACTIVE'),
 (7,'Ravi Patel','ravi.patel@example.com','customer','__PWHASH__',1,NULL,'ACTIVE'),
 (8,'Meera Iyer','meera.iyer@example.com','meera','__PWHASH__',1,NULL,'ACTIVE');

-- Policyholders
INSERT INTO policyholders (id, first_name, last_name, dob, email, mobile, address, city, state, pin_code) VALUES
 (1,'Ravi','Patel','1985-04-12','ravi.patel@example.com','9820011223','21 Marine Drive','Mumbai','Maharashtra','400002'),
 (2,'Meera','Iyer','1990-09-30','meera.iyer@example.com','9820044556','8 MG Road','Pune','Maharashtra','411001'),
 (3,'Arjun','Nair','1978-01-22','arjun.nair@example.com','9810099887','14 Brigade Road','Bengaluru','Karnataka','560001'),
 (4,'Kavya','Reddy','1995-07-08','kavya.reddy@example.com','9701122334','5 Jubilee Hills','Hyderabad','Telangana','500033'),
 (5,'Suresh','Menon','1982-11-15','suresh.menon@example.com','9847055667','3 Marine Lines','Kochi','Kerala','682001'),
 (6,'Anita','Desai','1988-03-19','anita.desai@example.com','9925077889','9 CG Road','Ahmedabad','Gujarat','380009');

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
 (1,'CLM-2024-0891',1,1,'Ravi Patel','MOTOR','Accident','2024-11-02','14:30','Eastern Express Hwy','Mumbai','Maharashtra','400022',
    'Front-end collision at signal; third party involved.',85000,'MH-01-AB-1234','FIR-2231','Sion PS','PENDING_APPROVAL',3,5,'HIGH',72,'2024-11-23','2024-11-03 09:15:00'),
 (2,'CLM-2024-0879',2,2,'Meera Iyer','HEALTH','Surgery','2024-10-20',NULL,'Ruby Hall Clinic','Pune','Maharashtra','411001',
    'Planned surgery hospitalization for 4 days.',120000,NULL,NULL,NULL,'UNDER_ASSESSMENT',4,6,'MEDIUM',35,'2024-11-10','2024-10-22 11:40:00'),
 (3,'CLM-2024-0874',3,3,'Arjun Nair','PROPERTY','Natural Disaster','2024-10-10',NULL,'Brigade Road','Bengaluru','Karnataka','560001',
    'Fire damage to commercial structure.',340000,NULL,'FIR-1180','Brigade PS','PENDING_APPROVAL',3,5,'LOW',18,'2024-10-31','2024-10-12 16:05:00'),
 (4,'CLM-2024-0871',4,4,'Kavya Reddy','LIFE','Disability','2024-09-28',NULL,'Jubilee Hills','Hyderabad','Telangana','500033',
    'Permanent partial disability claim.',1000000,NULL,NULL,NULL,'PENDING_APPROVAL',4,6,'HIGH',64,'2024-10-19','2024-09-30 10:00:00'),
 (5,'CLM-2024-0880',5,5,'Suresh Menon','MOTOR','Theft','2024-10-05','22:10','Marine Lines parking','Kochi','Kerala','682001',
    'Vehicle stolen from parking lot.',420000,'KL-07-XY-9921','FIR-0998','Ernakulam PS','SETTLED',3,5,'MEDIUM',40,'2024-10-26','2024-10-06 08:30:00'),
 (6,'CLM-2024-0870',1,1,'Ravi Patel','MOTOR','Glass Damage','2024-09-15',NULL,'Marine Drive','Mumbai','Maharashtra','400002',
    'Windshield cracked; insufficient documents.',12000,'MH-01-AB-1234',NULL,NULL,'REJECTED',3,NULL,'LOW',22,'2024-10-06','2024-09-16 13:20:00'),
 (7,'CLM-2024-0862',2,2,'Meera Iyer','HEALTH','Hospitalization','2024-09-01',NULL,'Ruby Hall Clinic','Pune','Maharashtra','411001',
    'Emergency hospitalization 2 days.',60000,NULL,NULL,NULL,'SETTLED',4,6,'LOW',15,'2024-09-22','2024-09-02 19:10:00'),
 (8,'CLM-2024-0858',3,3,'Arjun Nair','PROPERTY','Burglary','2024-08-25',NULL,'Brigade Road','Bengaluru','Karnataka','560001',
    'Office burglary, equipment stolen.',180000,NULL,'FIR-0871','Brigade PS','SURVEY_SCHEDULED',3,5,'MEDIUM',48,'2024-09-15','2024-08-27 09:45:00'),
 (9,'CLM-2024-0850',5,5,'Suresh Menon','MOTOR','Accident','2024-08-10','08:05','NH-66 Bypass','Kochi','Kerala','682001',
    'Rear-ended by truck; moderate damage.',95000,'KL-07-XY-9921','FIR-0810','Ernakulam PS','UNDER_REVIEW',4,NULL,'MEDIUM',38,'2024-08-31','2024-08-11 12:00:00'),
 (10,'CLM-2024-0845',6,6,'Anita Desai','TRAVEL','Trip Cancellation','2024-08-01',NULL,'Ahmedabad Airport','Ahmedabad','Gujarat','380009',
    'International trip cancelled due to medical emergency.',45000,NULL,NULL,NULL,'SUBMITTED',NULL,NULL,'LOW',10,'2024-08-22','2024-08-02 07:30:00'),
 (11,'CLM-2024-0838',1,1,'Ravi Patel','MOTOR','Fire','2024-07-19','17:45','Sion Circle','Mumbai','Maharashtra','400022',
    'Engine fire, total loss suspected.',650000,'MH-01-AB-1234','FIR-0777','Sion PS','APPROVED',3,5,'HIGH',69,'2024-08-09','2024-07-20 10:15:00'),
 (12,'CLM-2024-0830',4,4,'Kavya Reddy','LIFE','Critical Illness','2024-07-05',NULL,'Hyderabad','Telangana','Telangana','500033',
    'Critical illness diagnosis claim.',500000,NULL,NULL,NULL,'ON_HOLD',4,6,'HIGH',81,'2024-07-26','2024-07-06 14:50:00');

-- Documents for a couple of claims
INSERT INTO claim_documents (claim_id, doc_type, file_name, upload_status, verification_status, uploaded_at) VALUES
 (1,'Policy Document','policy_84521.pdf','UPLOADED','VERIFIED','2024-11-03 09:20:00'),
 (1,'FIR Copy','fir_2231.pdf','UPLOADED','VERIFIED','2024-11-03 09:22:00'),
 (1,'Photographs of Damage','damage_front.jpg','UPLOADED','UNDER_REVIEW','2024-11-03 09:25:00'),
 (1,'Repair Estimate','estimate_garage.pdf','PENDING','PENDING',NULL),
 (1,'Driver License','dl_ravi.pdf','UPLOADED','VERIFIED','2024-11-03 09:27:00'),
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
 (5,380000,'Theft claim settled per policy terms.','NEFT','Suresh Menon','HDFC Bank','50100123456789','HDFC0001234','CLOSED',2,'2024-10-18 10:00:00','2024-10-20 14:00:00','2024-10-21 09:00:00'),
 (7,52000,'Hospitalization within sub-limits.','NEFT','Meera Iyer','ICICI Bank','60200987654321','ICIC0006020','CLOSED',2,'2024-09-18 11:00:00','2024-09-20 13:00:00','2024-09-21 10:00:00');

-- Communications on claim 1
INSERT INTO communications (claim_id, sender_id, sender_name, channel, content, created_at) VALUES
 (1,3,'Priya Kumar','SMS','Your claim CLM-2024-0891 has been acknowledged.','2024-11-03 09:30:00'),
 (1,7,'Ravi Patel','MESSAGE','When will the surveyor visit?','2024-11-05 10:00:00'),
 (1,5,'Raj Mehta','MESSAGE','Site visit scheduled for 8 Nov, 11 AM.','2024-11-06 12:00:00');

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
 ('MOTOR','Accident','FIR Copy / Police Report',TRUE),
 ('MOTOR','Accident','Photographs of Damage',TRUE),
 ('MOTOR','Accident','Repair Estimate from Garage',TRUE),
 ('MOTOR','Theft','FIR Copy / Police Report',TRUE),
 ('MOTOR','Theft','Vehicle RC Book',TRUE),
 ('MOTOR','Theft','Driver License',TRUE),
 ('HEALTH','Hospitalization','Medical Bills & Receipts',TRUE),
 ('HEALTH','Hospitalization','Hospital Discharge Summary',TRUE),
 ('PROPERTY','Fire','FIR Copy / Police Report',TRUE),
 ('PROPERTY','Fire','Survey Report',TRUE);

-- Notification templates
INSERT INTO notification_templates (name, channel, active, body) VALUES
 ('Claim Acknowledgement','SMS',TRUE,'Your claim {claimNo} has been acknowledged.'),
 ('Survey Scheduled','SMS',TRUE,'Surveyor visit for {claimNo} scheduled on {date}.'),
 ('Settlement Confirmation','EMAIL',TRUE,'Your claim {claimNo} has been settled for {amount}.'),
 ('Rejection Notice','EMAIL',TRUE,'Your claim {claimNo} could not be approved. Reason: {reason}.');
