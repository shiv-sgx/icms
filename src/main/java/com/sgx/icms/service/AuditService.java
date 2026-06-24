package com.sgx.icms.service;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.dao.AuditDao;
import com.sgx.icms.dao.JdbcAuditDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.web.support.SessionUser;

/**
 * Writes the audit trail. Two modes:
 * <ul>
 *   <li>Fire-and-forget ({@link #record}) — own connection; an audit failure is
 *       logged but never propagated, so it can't break the user's action.</li>
 *   <li>Transactional ({@link #record(Connection, AuditLog)}) — participates in the
 *       caller's transaction so the state change and its audit row commit atomically.</li>
 * </ul>
 */
public class AuditService {

    private static final Logger LOG = LoggerFactory.getLogger(AuditService.class);

    private final AuditDao auditDao = new JdbcAuditDao();

    /** Transactional variant — caller owns the connection/commit. */
    public void record(Connection conn, AuditLog entry) {
        auditDao.insert(conn, entry);
    }

    /** Best-effort variant for actions outside a surrounding transaction (e.g. login). */
    public void record(Long userId, String username, String role,
                       String action, String entity, String result, String ip) {
        AuditLog a = new AuditLog();
        a.setUserId(userId);
        a.setUsername(username);
        a.setRole(role);
        a.setAction(action);
        a.setEntity(entity);
        a.setResult(result);
        a.setIpAddress(ip);
        try {
            Db.withConnection(conn -> {
                auditDao.insert(conn, a);
                return null;
            });
        } catch (RuntimeException e) {
            LOG.error("Failed to write audit log action={} entity={} result={}", action, entity, result, e);
        }
    }

    public void success(SessionUser actor, String action, String entity, String ip) {
        record(actor == null ? null : actor.getId(),
               actor == null ? null : actor.getUsername(),
               actor == null ? null : actor.getRole(),
               action, entity, AuditLog.RESULT_SUCCESS, ip);
    }
}
