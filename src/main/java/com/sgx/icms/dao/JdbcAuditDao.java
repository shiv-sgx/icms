package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.AuditLog;

public class JdbcAuditDao implements AuditDao {

    private static final RowMapper<AuditLog> MAPPER = JdbcAuditDao::map;

    private static AuditLog map(ResultSet rs) throws SQLException {
        AuditLog a = new AuditLog();
        a.setId(rs.getLong("id"));
        a.setTs(rs.getObject("ts", java.time.LocalDateTime.class));
        long uid = rs.getLong("user_id");
        a.setUserId(rs.wasNull() ? null : uid);
        a.setUsername(rs.getString("username"));
        a.setRole(rs.getString("role"));
        a.setAction(rs.getString("action"));
        a.setEntity(rs.getString("entity"));
        a.setIpAddress(rs.getString("ip_address"));
        a.setResult(rs.getString("result"));
        return a;
    }

    @Override
    public void insert(Connection conn, AuditLog log) {
        Db.update(conn,
            "INSERT INTO audit_logs (user_id, username, role, action, entity, ip_address, result) "
          + "VALUES (?,?,?,?,?,?,?)",
            log.getUserId(), log.getUsername(), log.getRole(), log.getAction(),
            log.getEntity(), log.getIpAddress(),
            log.getResult() == null ? AuditLog.RESULT_SUCCESS : log.getResult());
    }

    @Override
    public List<AuditLog> find(Connection conn, String action, String result, int limit, int offset) {
        StringBuilder sql = new StringBuilder(
            "SELECT id, ts, user_id, username, role, action, entity, ip_address, result FROM audit_logs ");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, action, result);
        sql.append("ORDER BY ts DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return Db.query(conn, sql.toString(), MAPPER, params.toArray());
    }

    @Override
    public long count(Connection conn, String action, String result) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_logs ");
        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, action, result);
        return Db.queryLong(conn, sql.toString(), params.toArray());
    }

    /** Builds an injection-safe, parameterised WHERE clause from optional filters. */
    private static void appendFilters(StringBuilder sql, List<Object> params, String action, String result) {
        List<String> clauses = new ArrayList<>();
        if (action != null && !action.isEmpty()) {
            clauses.add("action = ?");
            params.add(action);
        }
        if (result != null && !result.isEmpty()) {
            clauses.add("result = ?");
            params.add(result);
        }
        if (!clauses.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", clauses)).append(' ');
        }
    }
}
