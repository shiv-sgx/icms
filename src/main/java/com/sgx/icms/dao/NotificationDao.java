package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.Notification;

public class NotificationDao {

    private static final RowMapper<Notification> MAPPER = NotificationDao::map;

    private static Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        long uid = rs.getLong("user_id");
        n.setUserId(rs.wasNull() ? null : uid);
        n.setTargetRole(rs.getString("target_role"));
        n.setType(rs.getString("type"));
        n.setMessage(rs.getString("message"));
        n.setRead(rs.getBoolean("is_read"));
        n.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        return n;
    }

    /** Notifications addressed to a specific user OR broadcast to their role. */
    public List<Notification> findForUser(Connection conn, long userId, String role, int limit) {
        return Db.query(conn,
            "SELECT id, user_id, target_role, type, message, is_read, created_at FROM notifications "
          + "WHERE user_id = ? OR target_role = ? ORDER BY created_at DESC LIMIT ?",
            MAPPER, userId, role, limit);
    }

    public long insert(Connection conn, Notification n) {
        return Db.insert(conn,
            "INSERT INTO notifications (user_id, target_role, type, message, is_read) VALUES (?,?,?,?,?)",
            n.getUserId(), n.getTargetRole(), n.getType() == null ? "INFO" : n.getType(),
            n.getMessage(), n.isRead());
    }
}
