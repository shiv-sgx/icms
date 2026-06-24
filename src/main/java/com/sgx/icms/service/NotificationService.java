package com.sgx.icms.service;

import java.sql.Connection;
import java.util.List;

import com.sgx.icms.dao.NotificationDao;
import com.sgx.icms.db.Db;
import com.sgx.icms.domain.Notification;

/** Creates and reads in-app notifications (per-user or role broadcast). */
public class NotificationService {

    private final NotificationDao dao = new NotificationDao();

    /** Transactional: enrols a role-broadcast notification within the caller's tx. */
    public void notifyRole(Connection conn, String role, String type, String message) {
        Notification n = new Notification();
        n.setTargetRole(role);
        n.setType(type);
        n.setMessage(message);
        dao.insert(conn, n);
    }

    /** Transactional: a notification addressed to a single user. */
    public void notifyUser(Connection conn, long userId, String type, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setMessage(message);
        dao.insert(conn, n);
    }

    public List<Notification> recentForUser(long userId, String role, int limit) {
        return Db.withConnection(conn -> dao.findForUser(conn, userId, role, limit));
    }
}
