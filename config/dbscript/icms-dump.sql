-- MySQL dump 10.13  Distrib 9.6.0, for macos26.4 (arm64)
--
-- Host: 127.0.0.1    Database: icms
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `icms`
--

/*!40000 DROP DATABASE IF EXISTS `icms`*/;

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `icms` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `icms`;

--
-- Table structure for table `approval_thresholds`
--

DROP TABLE IF EXISTS `approval_thresholds`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `approval_thresholds` (
  `id` int NOT NULL AUTO_INCREMENT,
  `level` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL,
  `label` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `min_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `max_amount` decimal(15,2) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `approval_thresholds`
--

LOCK TABLES `approval_thresholds` WRITE;
/*!40000 ALTER TABLE `approval_thresholds` DISABLE KEYS */;
INSERT INTO `approval_thresholds` VALUES (1,'L1','Agent Approval',0.00,25000.00),(2,'L2','Manager Approval',25001.00,100000.00),(3,'L3','Director Approval',100001.00,NULL);
/*!40000 ALTER TABLE `approval_thresholds` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `approvals`
--

DROP TABLE IF EXISTS `approvals`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `approvals` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `claim_id` bigint NOT NULL,
  `level` enum('L1','L2','L3') COLLATE utf8mb4_unicode_ci NOT NULL,
  `approver_id` bigint DEFAULT NULL,
  `approver_role` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `decision` enum('PENDING','APPROVED','CONDITIONAL','REJECTED','RETURNED','ON_HOLD','NA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `remarks` text COLLATE utf8mb4_unicode_ci,
  `decided_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_appr_user` (`approver_id`),
  KEY `idx_appr_claim` (`claim_id`),
  KEY `idx_appr_decision` (`decision`),
  CONSTRAINT `fk_appr_claim` FOREIGN KEY (`claim_id`) REFERENCES `claims` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_appr_user` FOREIGN KEY (`approver_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `approvals`
--

LOCK TABLES `approvals` WRITE;
/*!40000 ALTER TABLE `approvals` DISABLE KEYS */;
INSERT INTO `approvals` VALUES (1,1,'L1',3,'AGENT','APPROVED','Initial review complete; docs largely in order.','2024-11-09 10:00:00','2026-06-24 14:03:29'),(2,1,'L2',2,'MANAGER','PENDING',NULL,NULL,'2026-06-24 14:03:29'),(3,3,'L1',3,'AGENT','APPROVED','Verified survey report.','2024-10-16 12:00:00','2026-06-24 14:03:29'),(4,3,'L2',2,'MANAGER','PENDING',NULL,NULL,'2026-06-24 14:03:29'),(5,4,'L1',4,'AGENT','APPROVED','Disability certificate verified.','2024-10-02 09:30:00','2026-06-24 14:03:29'),(6,4,'L2',2,'MANAGER','PENDING',NULL,NULL,'2026-06-24 14:03:29'),(7,4,'L3',NULL,'DIRECTOR','PENDING',NULL,NULL,'2026-06-24 14:03:29'),(8,11,'L1',3,'AGENT','APPROVED','Fire claim, survey supports total loss.','2024-07-28 10:00:00','2026-06-24 14:03:29'),(9,11,'L2',2,'MANAGER','APPROVED','Approved for settlement.','2024-08-01 15:30:00','2026-06-24 14:03:29');
/*!40000 ALTER TABLE `approvals` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assessment_components`
--

DROP TABLE IF EXISTS `assessment_components`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assessment_components` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `assessment_id` bigint NOT NULL,
  `component` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `severity` enum('MINOR','MODERATE','SEVERE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MODERATE',
  `repair_cost` decimal(15,2) NOT NULL DEFAULT '0.00',
  `replace_flag` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_comp_asm` (`assessment_id`),
  CONSTRAINT `fk_comp_asm` FOREIGN KEY (`assessment_id`) REFERENCES `assessments` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assessment_components`
--

LOCK TABLES `assessment_components` WRITE;
/*!40000 ALTER TABLE `assessment_components` DISABLE KEYS */;
INSERT INTO `assessment_components` VALUES (1,1,'Front Bumper','SEVERE',12000.00,1),(2,1,'Bonnet','MODERATE',8500.00,0),(3,1,'Headlight Assembly','SEVERE',9800.00,1),(4,1,'Radiator','MODERATE',7500.00,0),(5,1,'Front Grille','MINOR',7000.00,0);
/*!40000 ALTER TABLE `assessment_components` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `assessments`
--

DROP TABLE IF EXISTS `assessments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `assessments` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `claim_id` bigint NOT NULL,
  `surveyor_id` bigint DEFAULT NULL,
  `visit_date` date DEFAULT NULL,
  `visit_time` time DEFAULT NULL,
  `site_observations` text COLLATE utf8mb4_unicode_ci,
  `report_ref_no` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `gross_assessed` decimal(15,2) NOT NULL DEFAULT '0.00',
  `policy_deductible` decimal(15,2) NOT NULL DEFAULT '0.00',
  `depreciation_pct` decimal(5,2) NOT NULL DEFAULT '0.00',
  `depreciation_amt` decimal(15,2) NOT NULL DEFAULT '0.00',
  `salvage_value` decimal(15,2) NOT NULL DEFAULT '0.00',
  `net_payable` decimal(15,2) NOT NULL DEFAULT '0.00',
  `recommendation` enum('APPROVE_FULL','PARTIAL_APPROVE','REJECT') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `remarks` text COLLATE utf8mb4_unicode_ci,
  `status` enum('ASSIGNED','IN_PROGRESS','SUBMITTED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ASSIGNED',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_asm_surveyor` (`surveyor_id`),
  KEY `idx_asm_claim` (`claim_id`),
  CONSTRAINT `fk_asm_claim` FOREIGN KEY (`claim_id`) REFERENCES `claims` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_asm_surveyor` FOREIGN KEY (`surveyor_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `assessments`
--

LOCK TABLES `assessments` WRITE;
/*!40000 ALTER TABLE `assessments` DISABLE KEYS */;
INSERT INTO `assessments` VALUES (1,1,5,'2024-11-08','11:00:00','Front structural damage consistent with reported collision. No pre-existing damage observed.','SRV-2024-0891',44800.00,5000.00,15.00,6720.00,3000.00,30080.00,'PARTIAL_APPROVE','Recommend partial approval after standard deductions.','SUBMITTED','2026-06-24 14:03:29');
/*!40000 ALTER TABLE `assessments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audit_logs`
--

DROP TABLE IF EXISTS `audit_logs`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `audit_logs` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ts` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` bigint DEFAULT NULL,
  `username` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `role` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `action` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `entity` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ip_address` varchar(45) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `result` enum('SUCCESS','FAILED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SUCCESS',
  PRIMARY KEY (`id`),
  KEY `idx_audit_ts` (`ts`),
  KEY `idx_audit_user` (`user_id`),
  KEY `idx_audit_action` (`action`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audit_logs`
--

LOCK TABLES `audit_logs` WRITE;
/*!40000 ALTER TABLE `audit_logs` DISABLE KEYS */;
INSERT INTO `audit_logs` VALUES (1,'2024-11-09 10:00:00',3,'agent','AGENT','CLAIM_APPROVED','CLM-2024-0891 (L1)','127.0.0.1','SUCCESS'),(2,'2024-11-06 12:00:00',5,'surveyor','SURVEYOR','REPORT_UPLOAD','CLM-2024-0858','127.0.0.1','SUCCESS'),(3,'2024-11-05 09:00:00',1,'admin','ADMIN','ROLE_CHANGE','user:sunita','127.0.0.1','SUCCESS'),(4,'2024-11-04 08:00:00',NULL,'unknown',NULL,'LOGIN_FAIL','username:agent','127.0.0.1','FAILED'),(5,'2026-06-24 14:54:23',3,'agent','AGENT','LOGIN','user:agent','0:0:0:0:0:0:0:1','SUCCESS'),(6,'2026-06-24 14:54:30',3,'agent','AGENT','LOGOUT','user:agent','0:0:0:0:0:0:0:1','SUCCESS'),(7,'2026-06-24 14:54:33',1,'admin','ADMIN','LOGIN','user:admin','0:0:0:0:0:0:0:1','SUCCESS'),(8,'2026-06-24 14:54:55',1,'admin','ADMIN','LOGOUT','user:admin','0:0:0:0:0:0:0:1','SUCCESS'),(9,'2026-06-24 14:54:57',3,'agent','AGENT','LOGIN','user:agent','0:0:0:0:0:0:0:1','SUCCESS'),(10,'2026-06-24 14:55:02',3,'agent','AGENT','LOGOUT','user:agent','0:0:0:0:0:0:0:1','SUCCESS'),(11,'2026-06-24 14:55:04',7,'customer','CUSTOMER','LOGIN','user:customer','0:0:0:0:0:0:0:1','SUCCESS'),(12,'2026-06-24 14:55:09',7,'customer','CUSTOMER','LOGOUT','user:customer','0:0:0:0:0:0:0:1','SUCCESS'),(13,'2026-06-24 14:55:12',2,'manager','MANAGER','LOGIN','user:manager','0:0:0:0:0:0:0:1','SUCCESS'),(14,'2026-06-24 14:55:18',2,'manager','MANAGER','LOGOUT','user:manager','0:0:0:0:0:0:0:1','SUCCESS'),(15,'2026-06-24 14:55:38',1,'admin','ADMIN','LOGIN','user:admin','192.168.152.252','SUCCESS'),(16,'2026-06-24 15:04:50',1,'admin','ADMIN','LOGOUT','user:admin','192.168.152.252','SUCCESS'),(17,'2026-06-24 15:04:53',1,'admin','ADMIN','LOGIN','user:admin','192.168.152.252','SUCCESS');
/*!40000 ALTER TABLE `audit_logs` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `claim_documents`
--

DROP TABLE IF EXISTS `claim_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `claim_documents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `claim_id` bigint NOT NULL,
  `doc_type` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `file_path` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `upload_status` enum('REQUIRED','CONDITIONAL','PENDING','UPLOADED','MISSING','NA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `verification_status` enum('PENDING','UNDER_REVIEW','VERIFIED','FLAGGED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING',
  `uploaded_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_doc_claim` (`claim_id`),
  CONSTRAINT `fk_doc_claim` FOREIGN KEY (`claim_id`) REFERENCES `claims` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `claim_documents`
--

LOCK TABLES `claim_documents` WRITE;
/*!40000 ALTER TABLE `claim_documents` DISABLE KEYS */;
INSERT INTO `claim_documents` VALUES (1,1,'Policy Document','policy_84521.pdf',NULL,'UPLOADED','VERIFIED','2024-11-03 09:20:00'),(2,1,'FIR Copy','fir_2231.pdf',NULL,'UPLOADED','VERIFIED','2024-11-03 09:22:00'),(3,1,'Photographs of Damage','damage_front.jpg',NULL,'UPLOADED','UNDER_REVIEW','2024-11-03 09:25:00'),(4,1,'Repair Estimate','estimate_garage.pdf',NULL,'PENDING','PENDING',NULL),(5,1,'Driver License','dl_ravi.pdf',NULL,'UPLOADED','VERIFIED','2024-11-03 09:27:00'),(6,3,'Policy Document','policy_84523.pdf',NULL,'UPLOADED','VERIFIED','2024-10-12 16:10:00'),(7,3,'Survey Report','survey_1180.pdf',NULL,'UPLOADED','VERIFIED','2024-10-15 11:00:00');
/*!40000 ALTER TABLE `claim_documents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `claims`
--

DROP TABLE IF EXISTS `claims`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `claims` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `claim_no` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `policy_id` bigint NOT NULL,
  `policyholder_id` bigint NOT NULL,
  `claimant_name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `claim_type` enum('MOTOR','HEALTH','PROPERTY','LIFE','TRAVEL','LIABILITY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `claim_subtype` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `incident_date` date DEFAULT NULL,
  `incident_time` time DEFAULT NULL,
  `incident_location` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `state` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pin_code` varchar(12) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `estimated_loss` decimal(15,2) NOT NULL DEFAULT '0.00',
  `vehicle_reg_no` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `fir_number` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `police_station` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `hospital_name` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `workshop_name` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `third_party` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('DRAFT','SUBMITTED','UNDER_REVIEW','SURVEY_SCHEDULED','UNDER_ASSESSMENT','PENDING_APPROVAL','APPROVED','SETTLEMENT_PROCESSING','SETTLED','CLOSED','REJECTED','WITHDRAWN','ON_HOLD') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SUBMITTED',
  `agent_id` bigint DEFAULT NULL,
  `surveyor_id` bigint DEFAULT NULL,
  `risk_level` enum('LOW','MEDIUM','HIGH') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LOW',
  `fraud_score` int NOT NULL DEFAULT '0',
  `internal_notes` text COLLATE utf8mb4_unicode_ci,
  `sla_due_date` date DEFAULT NULL,
  `filed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `claim_no` (`claim_no`),
  KEY `fk_claim_policy` (`policy_id`),
  KEY `fk_claim_holder` (`policyholder_id`),
  KEY `idx_claim_status` (`status`),
  KEY `idx_claim_type` (`claim_type`),
  KEY `idx_claim_agent` (`agent_id`),
  KEY `idx_claim_surveyor` (`surveyor_id`),
  KEY `idx_claim_filed` (`filed_at`),
  KEY `idx_claim_risk` (`risk_level`),
  CONSTRAINT `fk_claim_agent` FOREIGN KEY (`agent_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_claim_holder` FOREIGN KEY (`policyholder_id`) REFERENCES `policyholders` (`id`),
  CONSTRAINT `fk_claim_policy` FOREIGN KEY (`policy_id`) REFERENCES `policies` (`id`),
  CONSTRAINT `fk_claim_surveyor` FOREIGN KEY (`surveyor_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `claims`
--

LOCK TABLES `claims` WRITE;
/*!40000 ALTER TABLE `claims` DISABLE KEYS */;
INSERT INTO `claims` VALUES (1,'CLM-2024-0891',1,1,'Ravi Patel','MOTOR','Accident','2024-11-02','14:30:00','Eastern Express Hwy','Mumbai','Maharashtra','400022','Front-end collision at signal; third party involved.',85000.00,'MH-01-AB-1234','FIR-2231','Sion PS',NULL,NULL,NULL,'PENDING_APPROVAL',3,5,'HIGH',72,NULL,'2024-11-23','2024-11-03 09:15:00','2026-06-24 14:03:29'),(2,'CLM-2024-0879',2,2,'Meera Iyer','HEALTH','Surgery','2024-10-20',NULL,'Ruby Hall Clinic','Pune','Maharashtra','411001','Planned surgery hospitalization for 4 days.',120000.00,NULL,NULL,NULL,NULL,NULL,NULL,'UNDER_ASSESSMENT',4,6,'MEDIUM',35,NULL,'2024-11-10','2024-10-22 11:40:00','2026-06-24 14:03:29'),(3,'CLM-2024-0874',3,3,'Arjun Nair','PROPERTY','Natural Disaster','2024-10-10',NULL,'Brigade Road','Bengaluru','Karnataka','560001','Fire damage to commercial structure.',340000.00,NULL,'FIR-1180','Brigade PS',NULL,NULL,NULL,'PENDING_APPROVAL',3,5,'LOW',18,NULL,'2024-10-31','2024-10-12 16:05:00','2026-06-24 14:03:29'),(4,'CLM-2024-0871',4,4,'Kavya Reddy','LIFE','Disability','2024-09-28',NULL,'Jubilee Hills','Hyderabad','Telangana','500033','Permanent partial disability claim.',1000000.00,NULL,NULL,NULL,NULL,NULL,NULL,'PENDING_APPROVAL',4,6,'HIGH',64,NULL,'2024-10-19','2024-09-30 10:00:00','2026-06-24 14:03:29'),(5,'CLM-2024-0880',5,5,'Suresh Menon','MOTOR','Theft','2024-10-05','22:10:00','Marine Lines parking','Kochi','Kerala','682001','Vehicle stolen from parking lot.',420000.00,'KL-07-XY-9921','FIR-0998','Ernakulam PS',NULL,NULL,NULL,'SETTLED',3,5,'MEDIUM',40,NULL,'2024-10-26','2024-10-06 08:30:00','2026-06-24 14:03:29'),(6,'CLM-2024-0870',1,1,'Ravi Patel','MOTOR','Glass Damage','2024-09-15',NULL,'Marine Drive','Mumbai','Maharashtra','400002','Windshield cracked; insufficient documents.',12000.00,'MH-01-AB-1234',NULL,NULL,NULL,NULL,NULL,'REJECTED',3,NULL,'LOW',22,NULL,'2024-10-06','2024-09-16 13:20:00','2026-06-24 14:03:29'),(7,'CLM-2024-0862',2,2,'Meera Iyer','HEALTH','Hospitalization','2024-09-01',NULL,'Ruby Hall Clinic','Pune','Maharashtra','411001','Emergency hospitalization 2 days.',60000.00,NULL,NULL,NULL,NULL,NULL,NULL,'SETTLED',4,6,'LOW',15,NULL,'2024-09-22','2024-09-02 19:10:00','2026-06-24 14:03:29'),(8,'CLM-2024-0858',3,3,'Arjun Nair','PROPERTY','Burglary','2024-08-25',NULL,'Brigade Road','Bengaluru','Karnataka','560001','Office burglary, equipment stolen.',180000.00,NULL,'FIR-0871','Brigade PS',NULL,NULL,NULL,'SURVEY_SCHEDULED',3,5,'MEDIUM',48,NULL,'2024-09-15','2024-08-27 09:45:00','2026-06-24 14:03:29'),(9,'CLM-2024-0850',5,5,'Suresh Menon','MOTOR','Accident','2024-08-10','08:05:00','NH-66 Bypass','Kochi','Kerala','682001','Rear-ended by truck; moderate damage.',95000.00,'KL-07-XY-9921','FIR-0810','Ernakulam PS',NULL,NULL,NULL,'UNDER_REVIEW',4,NULL,'MEDIUM',38,NULL,'2024-08-31','2024-08-11 12:00:00','2026-06-24 14:03:29'),(10,'CLM-2024-0845',6,6,'Anita Desai','TRAVEL','Trip Cancellation','2024-08-01',NULL,'Ahmedabad Airport','Ahmedabad','Gujarat','380009','International trip cancelled due to medical emergency.',45000.00,NULL,NULL,NULL,NULL,NULL,NULL,'SUBMITTED',NULL,NULL,'LOW',10,NULL,'2024-08-22','2024-08-02 07:30:00','2026-06-24 14:03:29'),(11,'CLM-2024-0838',1,1,'Ravi Patel','MOTOR','Fire','2024-07-19','17:45:00','Sion Circle','Mumbai','Maharashtra','400022','Engine fire, total loss suspected.',650000.00,'MH-01-AB-1234','FIR-0777','Sion PS',NULL,NULL,NULL,'APPROVED',3,5,'HIGH',69,NULL,'2024-08-09','2024-07-20 10:15:00','2026-06-24 14:03:29'),(12,'CLM-2024-0830',4,4,'Kavya Reddy','LIFE','Critical Illness','2024-07-05',NULL,'Hyderabad','Telangana','Telangana','500033','Critical illness diagnosis claim.',500000.00,NULL,NULL,NULL,NULL,NULL,NULL,'ON_HOLD',4,6,'HIGH',81,NULL,'2024-07-26','2024-07-06 14:50:00','2026-06-24 14:03:29');
/*!40000 ALTER TABLE `claims` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `communications`
--

DROP TABLE IF EXISTS `communications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `communications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `claim_id` bigint NOT NULL,
  `sender_id` bigint DEFAULT NULL,
  `sender_name` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `channel` enum('SMS','EMAIL','CALL','MESSAGE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'MESSAGE',
  `content` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_comm_user` (`sender_id`),
  KEY `idx_comm_claim` (`claim_id`),
  CONSTRAINT `fk_comm_claim` FOREIGN KEY (`claim_id`) REFERENCES `claims` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_comm_user` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `communications`
--

LOCK TABLES `communications` WRITE;
/*!40000 ALTER TABLE `communications` DISABLE KEYS */;
INSERT INTO `communications` VALUES (1,1,3,'Priya Kumar','SMS','Your claim CLM-2024-0891 has been acknowledged.','2024-11-03 09:30:00'),(2,1,7,'Ravi Patel','MESSAGE','When will the surveyor visit?','2024-11-05 10:00:00'),(3,1,5,'Raj Mehta','MESSAGE','Site visit scheduled for 8 Nov, 11 AM.','2024-11-06 12:00:00');
/*!40000 ALTER TABLE `communications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `document_requirements`
--

DROP TABLE IF EXISTS `document_requirements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `document_requirements` (
  `id` int NOT NULL AUTO_INCREMENT,
  `claim_type` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `claim_subtype` varchar(60) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `doc_type` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `required` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `document_requirements`
--

LOCK TABLES `document_requirements` WRITE;
/*!40000 ALTER TABLE `document_requirements` DISABLE KEYS */;
INSERT INTO `document_requirements` VALUES (1,'MOTOR','Accident','FIR Copy / Police Report',1),(2,'MOTOR','Accident','Photographs of Damage',1),(3,'MOTOR','Accident','Repair Estimate from Garage',1),(4,'MOTOR','Theft','FIR Copy / Police Report',1),(5,'MOTOR','Theft','Vehicle RC Book',1),(6,'MOTOR','Theft','Driver License',1),(7,'HEALTH','Hospitalization','Medical Bills & Receipts',1),(8,'HEALTH','Hospitalization','Hospital Discharge Summary',1),(9,'PROPERTY','Fire','FIR Copy / Police Report',1),(10,'PROPERTY','Fire','Survey Report',1);
/*!40000 ALTER TABLE `document_requirements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification_templates`
--

DROP TABLE IF EXISTS `notification_templates`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification_templates` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `channel` enum('SMS','EMAIL') COLLATE utf8mb4_unicode_ci NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  `body` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification_templates`
--

LOCK TABLES `notification_templates` WRITE;
/*!40000 ALTER TABLE `notification_templates` DISABLE KEYS */;
INSERT INTO `notification_templates` VALUES (1,'Claim Acknowledgement','SMS',1,'Your claim {claimNo} has been acknowledged.'),(2,'Survey Scheduled','SMS',1,'Surveyor visit for {claimNo} scheduled on {date}.'),(3,'Settlement Confirmation','EMAIL',1,'Your claim {claimNo} has been settled for {amount}.'),(4,'Rejection Notice','EMAIL',1,'Your claim {claimNo} could not be approved. Reason: {reason}.');
/*!40000 ALTER TABLE `notification_templates` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint DEFAULT NULL,
  `target_role` varchar(30) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `type` enum('ACTION','URGENT','INFO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'INFO',
  `message` varchar(500) COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_notif_user` (`user_id`),
  KEY `idx_notif_role` (`target_role`),
  CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,2,'MANAGER','ACTION','3 claims awaiting L2 approval.',0,'2026-06-24 14:03:29'),(2,NULL,'MANAGER','URGENT','SLA breach: CLM-2024-0870 overdue 2 days.',0,'2026-06-24 14:03:29'),(3,NULL,'AGENT','INFO','Surveyor report uploaded: CLM-2024-0858.',0,'2026-06-24 14:03:29');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `policies`
--

DROP TABLE IF EXISTS `policies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `policies` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `policy_no` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `policyholder_id` bigint NOT NULL,
  `type` enum('MOTOR','HEALTH','PROPERTY','LIFE','TRAVEL','LIABILITY') COLLATE utf8mb4_unicode_ci NOT NULL,
  `sum_insured` decimal(15,2) NOT NULL DEFAULT '0.00',
  `premium` decimal(15,2) NOT NULL DEFAULT '0.00',
  `start_date` date DEFAULT NULL,
  `expiry_date` date DEFAULT NULL,
  `ncb_discount` decimal(5,2) NOT NULL DEFAULT '0.00',
  `status` enum('ACTIVE','EXPIRED','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `policy_no` (`policy_no`),
  KEY `idx_pol_holder` (`policyholder_id`),
  KEY `idx_pol_status` (`status`),
  KEY `idx_pol_type` (`type`),
  CONSTRAINT `fk_pol_holder` FOREIGN KEY (`policyholder_id`) REFERENCES `policyholders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `policies`
--

LOCK TABLES `policies` WRITE;
/*!40000 ALTER TABLE `policies` DISABLE KEYS */;
INSERT INTO `policies` VALUES (1,'POL-84521',1,'MOTOR',800000.00,18500.00,'2024-04-01','2025-03-31',20.00,'ACTIVE','2026-06-24 14:03:29'),(2,'POL-84522',2,'HEALTH',1000000.00,24000.00,'2024-06-01','2025-05-31',0.00,'ACTIVE','2026-06-24 14:03:29'),(3,'POL-84523',3,'PROPERTY',5000000.00,42000.00,'2024-01-15','2025-01-14',0.00,'ACTIVE','2026-06-24 14:03:29'),(4,'POL-84524',4,'LIFE',10000000.00,55000.00,'2023-09-01','2043-08-31',0.00,'ACTIVE','2026-06-24 14:03:29'),(5,'POL-84525',5,'MOTOR',600000.00,14200.00,'2024-07-10','2025-07-09',35.00,'ACTIVE','2026-06-24 14:03:29'),(6,'POL-84526',6,'TRAVEL',300000.00,3500.00,'2024-10-01','2025-09-30',0.00,'ACTIVE','2026-06-24 14:03:29');
/*!40000 ALTER TABLE `policies` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `policyholders`
--

DROP TABLE IF EXISTS `policyholders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `policyholders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `first_name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `last_name` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `dob` date DEFAULT NULL,
  `email` varchar(160) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `mobile` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `city` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `state` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `pin_code` varchar(12) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ph_name` (`last_name`,`first_name`),
  KEY `idx_ph_mobile` (`mobile`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `policyholders`
--

LOCK TABLES `policyholders` WRITE;
/*!40000 ALTER TABLE `policyholders` DISABLE KEYS */;
INSERT INTO `policyholders` VALUES (1,'Ravi','Patel','1985-04-12','ravi.patel@example.com','9820011223','21 Marine Drive','Mumbai','Maharashtra','400002','2026-06-24 14:03:29'),(2,'Meera','Iyer','1990-09-30','meera.iyer@example.com','9820044556','8 MG Road','Pune','Maharashtra','411001','2026-06-24 14:03:29'),(3,'Arjun','Nair','1978-01-22','arjun.nair@example.com','9810099887','14 Brigade Road','Bengaluru','Karnataka','560001','2026-06-24 14:03:29'),(4,'Kavya','Reddy','1995-07-08','kavya.reddy@example.com','9701122334','5 Jubilee Hills','Hyderabad','Telangana','500033','2026-06-24 14:03:29'),(5,'Suresh','Menon','1982-11-15','suresh.menon@example.com','9847055667','3 Marine Lines','Kochi','Kerala','682001','2026-06-24 14:03:29'),(6,'Anita','Desai','1988-03-19','anita.desai@example.com','9925077889','9 CG Road','Ahmedabad','Gujarat','380009','2026-06-24 14:03:29');
/*!40000 ALTER TABLE `policyholders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `roles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'CUSTOMER','Policyholder / claimant portal access'),(2,'AGENT','Handles claims, assigns surveyors, processes settlements'),(3,'SURVEYOR','Performs damage assessment in the field'),(4,'MANAGER','Approves/rejects claims, oversees workload'),(5,'ADMIN','Full system administration');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `settlements`
--

DROP TABLE IF EXISTS `settlements`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `settlements` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `claim_id` bigint NOT NULL,
  `final_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `justification` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `payment_method` enum('NEFT','CHEQUE','DEMAND_DRAFT','DIRECT_TO_WORKSHOP') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'NEFT',
  `account_holder` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `bank_name` varchar(120) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `account_number` varchar(40) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `ifsc_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('AUTHORIZED','PAYMENT_INITIATED','BANK_PROCESSING','PAYMENT_CONFIRMED','CLAIMANT_NOTIFIED','CLOSED') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'AUTHORIZED',
  `approved_by` bigint DEFAULT NULL,
  `approval_date` datetime DEFAULT NULL,
  `payment_initiated_at` datetime DEFAULT NULL,
  `payment_confirmed_at` datetime DEFAULT NULL,
  `closed_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `claim_id` (`claim_id`),
  KEY `fk_settle_user` (`approved_by`),
  KEY `idx_settle_status` (`status`),
  CONSTRAINT `fk_settle_claim` FOREIGN KEY (`claim_id`) REFERENCES `claims` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_settle_user` FOREIGN KEY (`approved_by`) REFERENCES `users` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `settlements`
--

LOCK TABLES `settlements` WRITE;
/*!40000 ALTER TABLE `settlements` DISABLE KEYS */;
INSERT INTO `settlements` VALUES (1,5,380000.00,'Theft claim settled per policy terms.','NEFT','Suresh Menon','HDFC Bank','50100123456789','HDFC0001234','CLOSED',2,'2024-10-18 10:00:00',NULL,'2024-10-20 14:00:00','2024-10-21 09:00:00','2026-06-24 14:03:29'),(2,7,52000.00,'Hospitalization within sub-limits.','NEFT','Meera Iyer','ICICI Bank','60200987654321','ICIC0006020','CLOSED',2,'2024-09-18 11:00:00',NULL,'2024-09-20 13:00:00','2024-09-21 10:00:00','2026-06-24 14:03:29');
/*!40000 ALTER TABLE `settlements` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sla_config`
--

DROP TABLE IF EXISTS `sla_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sla_config` (
  `id` int NOT NULL AUTO_INCREMENT,
  `stage` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `hours` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sla_config`
--

LOCK TABLES `sla_config` WRITE;
/*!40000 ALTER TABLE `sla_config` DISABLE KEYS */;
INSERT INTO `sla_config` VALUES (1,'Claim Acknowledgement',24),(2,'Surveyor Assignment',48),(3,'Assessment Completion',120),(4,'Approval Decision',72),(5,'Settlement Processing',168),(6,'Total End-to-End',504);
/*!40000 ALTER TABLE `sla_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `full_name` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(160) COLLATE utf8mb4_unicode_ci NOT NULL,
  `username` varchar(80) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password_hash` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `role_id` int NOT NULL,
  `branch` varchar(80) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ACTIVE',
  `last_login` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_users_role` (`role_id`),
  KEY `idx_users_status` (`status`),
  CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'System Administrator','admin@icms.local','admin','$2a$10$l98i2wasnZoYt37EaksT9eV/LqWkvbEWNeCFKMeUs9iIkFVpt5cF.',5,'HQ','ACTIVE','2026-06-24 15:04:53','2026-06-24 14:03:29'),(2,'Anand Sharma','anand.sharma@icms.local','manager','$2a$10$l98i2wasnZoYt37EaksT9eV/LqWkvbEWNeCFKMeUs9iIkFVpt5cF.',4,'Mumbai','ACTIVE','2026-06-24 14:55:12','2026-06-24 14:03:29'),(3,'Priya Kumar','priya.kumar@icms.local','agent','$2a$10$l98i2wasnZoYt37EaksT9eV/LqWkvbEWNeCFKMeUs9iIkFVpt5cF.',2,'Mumbai','ACTIVE','2026-06-24 14:54:57','2026-06-24 14:03:29'),(4,'Sunita Shah','sunita.shah@icms.local','sunita','$2a$10$l98i2wasnZoYt37EaksT9eV/LqWkvbEWNeCFKMeUs9iIkFVpt5cF.',2,'Pune','ACTIVE',NULL,'2026-06-24 14:03:29'),(5,'Raj Mehta','raj.mehta@icms.local','surveyor','$2a$10$l98i2wasnZoYt37EaksT9eV/LqWkvbEWNeCFKMeUs9iIkFVpt5cF.',3,'Mumbai','ACTIVE',NULL,'2026-06-24 14:03:29'),(6,'Vinod Kulkarni','vinod.kulkarni@icms.local','vinod','$2a$10$l98i2wasnZoYt37EaksT9eV/LqWkvbEWNeCFKMeUs9iIkFVpt5cF.',3,'Pune','ACTIVE',NULL,'2026-06-24 14:03:29'),(7,'Ravi Patel','ravi.patel@example.com','customer','$2a$10$l98i2wasnZoYt37EaksT9eV/LqWkvbEWNeCFKMeUs9iIkFVpt5cF.',1,NULL,'ACTIVE','2026-06-24 14:55:04','2026-06-24 14:03:29'),(8,'Meera Iyer','meera.iyer@example.com','meera','$2a$10$l98i2wasnZoYt37EaksT9eV/LqWkvbEWNeCFKMeUs9iIkFVpt5cF.',1,NULL,'ACTIVE',NULL,'2026-06-24 14:03:29');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'icms'
--

--
-- Dumping routines for database 'icms'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-24 15:08:43
