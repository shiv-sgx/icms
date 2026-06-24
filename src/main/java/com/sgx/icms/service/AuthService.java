package com.sgx.icms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sgx.icms.dao.JdbcUserDao;
import com.sgx.icms.dao.UserDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.AuditLog;
import com.sgx.icms.domain.User;

/**
 * Authentication use-case: verify credentials, enforce account status, record the
 * login attempt, and stamp {@code last_login}. Returns the authenticated {@link User}
 * or {@code null} — callers must not be able to distinguish "no such user" from
 * "wrong password" (no user enumeration).
 */
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private final UserDao userDao = new JdbcUserDao();
    private final PasswordService passwords = new PasswordService();
    private final AuditService audit = new AuditService();

    /**
     * @return the authenticated user, or {@code null} if credentials are invalid or
     *         the account is not ACTIVE.
     */
    public User authenticate(String username, String rawPassword, String ip) {
        if (username == null || username.isEmpty() || rawPassword == null || rawPassword.isEmpty()) {
            return null;
        }
        final String uname = username.trim();

        User user = Db.withConnection(conn -> userDao.findByUsername(conn, uname));

        boolean ok = user != null
                && user.isActive()
                && passwords.matches(rawPassword, user.getPasswordHash());

        if (!ok) {
            // Don't reveal which check failed; log internally for ops.
            LOG.info("Failed login for username='{}' from {}", uname, ip);
            audit.record(user == null ? null : user.getId(), uname,
                    user == null ? null : user.getRoleName(),
                    "LOGIN_FAIL", "username:" + uname, AuditLog.RESULT_FAILED, ip);
            return null;
        }

        final long uid = user.getId();
        Db.inTransaction(conn -> {
            userDao.updateLastLogin(conn, uid);
            return null;
        });
        audit.record(user.getId(), user.getUsername(), user.getRoleName(),
                "LOGIN", "user:" + user.getUsername(), AuditLog.RESULT_SUCCESS, ip);
        LOG.info("User '{}' ({}) logged in from {}", user.getUsername(), user.getRoleName(), ip);
        return user;
    }
}
