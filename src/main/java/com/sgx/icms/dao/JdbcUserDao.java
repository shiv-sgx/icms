package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.User;

public class JdbcUserDao implements UserDao {

    private static final String SELECT =
            "SELECT u.id, u.full_name, u.email, u.username, u.password_hash, u.role_id, "
          + "r.name AS role_name, u.branch, u.status, u.last_login, u.created_at "
          + "FROM users u JOIN roles r ON r.id = u.role_id ";

    static final RowMapper<User> MAPPER = JdbcUserDao::mapUser;

    static User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRoleId(rs.getInt("role_id"));
        u.setRoleName(rs.getString("role_name"));
        u.setBranch(rs.getString("branch"));
        u.setStatus(rs.getString("status"));
        u.setLastLogin(rs.getObject("last_login", java.time.LocalDateTime.class));
        u.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        return u;
    }

    @Override
    public User findByUsername(Connection conn, String username) {
        return Db.queryOne(conn, SELECT + "WHERE u.username = ?", MAPPER, username);
    }

    @Override
    public User findById(Connection conn, long id) {
        return Db.queryOne(conn, SELECT + "WHERE u.id = ?", MAPPER, id);
    }

    @Override
    public void updateLastLogin(Connection conn, long userId) {
        Db.update(conn, "UPDATE users SET last_login = NOW() WHERE id = ?", userId);
    }

    @Override
    public java.util.List<User> findActiveByRole(Connection conn, String roleName) {
        return Db.query(conn, SELECT + "WHERE r.name = ? AND u.status = 'ACTIVE' ORDER BY u.full_name",
                MAPPER, roleName);
    }

    @Override
    public java.util.List<User> search(Connection conn, String q, String roleName, int limit, int offset) {
        StringBuilder sql = new StringBuilder(SELECT);
        java.util.List<Object> params = new java.util.ArrayList<>();
        appendUserFilters(sql, params, q, roleName);
        sql.append("ORDER BY u.id LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        return Db.query(conn, sql.toString(), MAPPER, params.toArray());
    }

    @Override
    public long countSearch(Connection conn, String q, String roleName) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM users u JOIN roles r ON r.id = u.role_id ");
        java.util.List<Object> params = new java.util.ArrayList<>();
        appendUserFilters(sql, params, q, roleName);
        return Db.queryLong(conn, sql.toString(), params.toArray());
    }

    @Override
    public long insert(Connection conn, User u) {
        return Db.insert(conn,
            "INSERT INTO users (full_name, email, username, password_hash, role_id, branch, status) "
          + "VALUES (?,?,?,?,?,?,?)",
            u.getFullName(), u.getEmail(), u.getUsername(), u.getPasswordHash(), u.getRoleId(),
            u.getBranch(), u.getStatus() == null ? "ACTIVE" : u.getStatus());
    }

    @Override
    public void updateStatusAndRole(Connection conn, long userId, String status, int roleId) {
        Db.update(conn, "UPDATE users SET status = ?, role_id = ? WHERE id = ?", status, roleId, userId);
    }

    @Override
    public void updatePassword(Connection conn, long userId, String passwordHash) {
        Db.update(conn, "UPDATE users SET password_hash = ? WHERE id = ?", passwordHash, userId);
    }

    @Override
    public boolean existsByUsernameOrEmail(Connection conn, String username, String email) {
        return Db.queryLong(conn, "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?",
                username, email) > 0;
    }

    private static void appendUserFilters(StringBuilder sql, java.util.List<Object> params,
                                          String q, String roleName) {
        java.util.List<String> clauses = new java.util.ArrayList<>();
        if (q != null && !q.trim().isEmpty()) {
            clauses.add("(u.full_name LIKE ? OR u.email LIKE ? OR u.username LIKE ?)");
            String like = "%" + q.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (roleName != null && !roleName.isEmpty()) {
            clauses.add("r.name = ?");
            params.add(roleName);
        }
        if (!clauses.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", clauses)).append(' ');
        }
    }
}
