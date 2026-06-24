package com.sgx.icms.service;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.config.DataSourceProvider;
import com.sgx.icms.dao.AuditDao;
import com.sgx.icms.dao.ConfigDao;
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.dao.JdbcUserDao;
import com.sgx.icms.dao.RoleDao;
import com.sgx.icms.dao.UserDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.ApprovalThreshold;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.domain.DocumentRequirement;
import com.sgx.icms.domain.NotificationTemplate;
import com.sgx.icms.domain.Role;
import com.sgx.icms.domain.SlaConfig;
import com.sgx.icms.domain.User;
import com.sgx.icms.web.support.AdminStats;
import com.sgx.icms.web.support.Paged;
import com.sgx.icms.web.support.SessionUser;

import com.zaxxer.hikari.HikariDataSource;

/**
 * Admin use-cases: system stats, user management (BCrypt-hashed credentials),
 * configuration (SLA / thresholds / templates / document requirements), and audit
 * reads. All state changes are audited.
 */
public class AdminService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminService.class);

    private final UserDao userDao = new JdbcUserDao();
    private final RoleDao roleDao = new RoleDao();
    private final ConfigDao configDao = new ConfigDao();
    private final AuditDao auditDao = new JdbcAuditDao();
    private final PasswordService passwords = new PasswordService();
    private final AuditService audit = new AuditService();

    /* ----------------------------- dashboard ----------------------------- */

    public AdminStats stats() {
        AdminStats s = Db.withConnection(conn -> {
            AdminStats a = new AdminStats();
            a.setUsers(Db.queryLong(conn, "SELECT COUNT(*) FROM users"));
            a.setClaims(Db.queryLong(conn, "SELECT COUNT(*) FROM claims"));
            a.setRoles(Db.queryLong(conn, "SELECT COUNT(*) FROM roles"));
            a.setAuditEvents(Db.queryLong(conn, "SELECT COUNT(*) FROM audit_logs"));
            return a;
        });
        HikariDataSource ds = DataSourceProvider.hikari();
        if (ds != null && ds.getHikariPoolMXBean() != null) {
            s.setPoolActive(ds.getHikariPoolMXBean().getActiveConnections());
            s.setPoolIdle(ds.getHikariPoolMXBean().getIdleConnections());
            s.setPoolTotal(ds.getHikariPoolMXBean().getTotalConnections());
        }
        return s;
    }

    /* ----------------------------- users ----------------------------- */

    public Paged<User> searchUsers(String q, String role, int page, int size) {
        int offset = (page - 1) * size;
        return Db.withConnection(conn -> {
            long total = userDao.countSearch(conn, q, role);
            List<User> items = userDao.search(conn, q, role, size, offset);
            return new Paged<>(items, page, size, total);
        });
    }

    public List<Role> roles() {
        return Db.withConnection(roleDao::findAll);
    }

    public User user(long id) {
        return Db.withConnection(conn -> userDao.findById(conn, id));
    }

    public void createUser(SessionUser admin, User draft, String rawPassword, String ip) {
        if (draft.getUsername() == null || draft.getUsername().trim().isEmpty()
                || draft.getEmail() == null || draft.getEmail().trim().isEmpty()
                || draft.getFullName() == null || draft.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name, email, and username are required.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        draft.setPasswordHash(passwords.hash(rawPassword));
        Db.inTransaction(conn -> {
            if (userDao.existsByUsernameOrEmail(conn, draft.getUsername().trim(), draft.getEmail().trim())) {
                throw new IllegalStateException("A user with that username or email already exists.");
            }
            long id = userDao.insert(conn, draft);
            writeAudit(conn, admin, "USER_CREATED", draft.getUsername() + " (id " + id + ")", ip);
            LOG.info("Admin {} created user {}", admin.getUsername(), draft.getUsername());
            return null;
        });
    }

    public void updateUser(SessionUser admin, long userId, String status, int roleId, String ip) {
        Db.inTransaction(conn -> {
            User u = userDao.findById(conn, userId);
            if (u == null) {
                throw new IllegalStateException("User not found.");
            }
            userDao.updateStatusAndRole(conn, userId, status, roleId);
            writeAudit(conn, admin, "USER_UPDATED", u.getUsername() + " -> " + status + "/role:" + roleId, ip);
            return null;
        });
    }

    public void resetPassword(SessionUser admin, long userId, String newPassword, String ip) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        String hash = passwords.hash(newPassword);
        Db.inTransaction(conn -> {
            User u = userDao.findById(conn, userId);
            if (u == null) {
                throw new IllegalStateException("User not found.");
            }
            userDao.updatePassword(conn, userId, hash);
            writeAudit(conn, admin, "PASSWORD_RESET", u.getUsername(), ip);
            return null;
        });
    }

    /* ----------------------------- config reads ----------------------------- */

    public List<SlaConfig> slaConfigs() { return Db.withConnection(configDao::slaConfigs); }
    public List<ApprovalThreshold> thresholds() { return Db.withConnection(configDao::approvalThresholds); }
    public List<NotificationTemplate> templates() { return Db.withConnection(configDao::templates); }
    public List<DocumentRequirement> documentRequirements() { return Db.withConnection(configDao::documentRequirements); }

    /* ----------------------------- config writes ----------------------------- */

    public void updateSla(SessionUser admin, int id, int hours, String ip) {
        Db.inTransaction(conn -> {
            configDao.updateSla(conn, id, hours);
            writeAudit(conn, admin, "SLA_UPDATED", "sla:" + id + " -> " + hours + "h", ip);
            return null;
        });
    }

    public void updateThreshold(SessionUser admin, int id, BigDecimal min, BigDecimal max, String ip) {
        Db.inTransaction(conn -> {
            configDao.updateThreshold(conn, id, min, max);
            writeAudit(conn, admin, "THRESHOLD_UPDATED", "threshold:" + id, ip);
            return null;
        });
    }

    public void updateTemplate(SessionUser admin, int id, boolean active, String body, String ip) {
        Db.inTransaction(conn -> {
            configDao.updateTemplate(conn, id, active, body);
            writeAudit(conn, admin, "TEMPLATE_UPDATED", "template:" + id, ip);
            return null;
        });
    }

    public void addDocumentRequirement(SessionUser admin, DocumentRequirement d, String ip) {
        if (d.getClaimType() == null || d.getClaimType().trim().isEmpty()
                || d.getDocType() == null || d.getDocType().trim().isEmpty()) {
            throw new IllegalArgumentException("Claim type and document type are required.");
        }
        Db.inTransaction(conn -> {
            configDao.insertDocumentRequirement(conn, d);
            writeAudit(conn, admin, "DOCREQ_ADDED", d.getClaimType() + "/" + d.getDocType(), ip);
            return null;
        });
    }

    public void deleteDocumentRequirement(SessionUser admin, int id, String ip) {
        Db.inTransaction(conn -> {
            configDao.deleteDocumentRequirement(conn, id);
            writeAudit(conn, admin, "DOCREQ_DELETED", "docreq:" + id, ip);
            return null;
        });
    }

    /* ----------------------------- audit ----------------------------- */

    public Paged<AuditLog> auditLogs(String action, String result, int page, int size) {
        int offset = (page - 1) * size;
        return Db.withConnection(conn -> {
            long total = auditDao.count(conn, action, result);
            List<AuditLog> items = auditDao.find(conn, action, result, size, offset);
            return new Paged<>(items, page, size, total);
        });
    }

    public List<AuditLog> auditLogsForExport(String action, String result) {
        return Db.withConnection(conn -> auditDao.find(conn, action, result, 5000, 0));
    }

    private void writeAudit(java.sql.Connection conn, SessionUser actor, String action, String entity, String ip) {
        AuditLog a = new AuditLog();
        a.setUserId(actor.getId());
        a.setUsername(actor.getUsername());
        a.setRole(actor.getRole());
        a.setAction(action);
        a.setEntity(entity);
        a.setIpAddress(ip);
        a.setResult(AuditLog.RESULT_SUCCESS);
        audit.record(conn, a);
    }
}
