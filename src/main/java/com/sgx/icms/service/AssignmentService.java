package com.sgx.icms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.dao.ClaimDao;
import com.sgx.icms.dao.JdbcUserDao;
import com.sgx.icms.dao.UserDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.domain.Claim;
import com.sgx.icms.domain.ClaimStatus;
import com.sgx.icms.domain.User;
import com.sgx.icms.web.support.SessionUser;

/** Agent assigns a surveyor to a claim and schedules the survey. */
public class AssignmentService {

    private static final Logger LOG = LoggerFactory.getLogger(AssignmentService.class);

    private final ClaimDao claimDao = new ClaimDao();
    private final UserDao userDao = new JdbcUserDao();
    private final NotificationService notifications = new NotificationService();
    private final AuditService audit = new AuditService();

    public java.util.List<User> availableSurveyors() {
        return Db.withConnection(conn -> userDao.findActiveByRole(conn, "SURVEYOR"));
    }

    /**
     * Assigns the surveyor, moves the claim to SURVEY_SCHEDULED, notifies the surveyor.
     * @throws IllegalStateException if the claim is in a state that can't be assigned
     */
    public void assignSurveyor(SessionUser agent, long claimId, long surveyorId, String ip) {
        Db.inTransaction(conn -> {
            Claim claim = claimDao.findById(conn, claimId);
            if (claim == null) {
                throw new IllegalStateException("Claim not found.");
            }
            if (ClaimStatus.isTerminal(claim.getStatus())) {
                throw new IllegalStateException("Cannot assign a surveyor to a closed claim.");
            }
            User surveyor = userDao.findById(conn, surveyorId);
            if (surveyor == null || !"SURVEYOR".equalsIgnoreCase(surveyor.getRoleName())) {
                throw new IllegalArgumentException("Please choose a valid surveyor.");
            }

            claimDao.assignSurveyor(conn, claimId, surveyorId, agent.getId(), ClaimStatus.SURVEY_SCHEDULED);
            notifications.notifyUser(conn, surveyorId, "ACTION",
                    "You have been assigned to survey claim " + claim.getClaimNo() + ".");

            AuditLog a = new AuditLog();
            a.setUserId(agent.getId());
            a.setUsername(agent.getUsername());
            a.setRole(agent.getRole());
            a.setAction("SURVEYOR_ASSIGNED");
            a.setEntity(claim.getClaimNo() + " -> " + surveyor.getFullName());
            a.setIpAddress(ip);
            a.setResult(AuditLog.RESULT_SUCCESS);
            audit.record(conn, a);

            LOG.info("Agent {} assigned surveyor {} to claim {}", agent.getUsername(),
                    surveyor.getUsername(), claim.getClaimNo());
            return null;
        });
    }
}
