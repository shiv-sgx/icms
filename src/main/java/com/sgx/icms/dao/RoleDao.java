package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.Role;

public class RoleDao {

    private static final RowMapper<Role> MAPPER = RoleDao::map;

    private static Role map(ResultSet rs) throws SQLException {
        Role r = new Role();
        r.setId(rs.getInt("id"));
        r.setName(rs.getString("name"));
        r.setDescription(rs.getString("description"));
        return r;
    }

    public List<Role> findAll(Connection conn) {
        return Db.query(conn,
            "SELECT r.id, r.name, r.description, "
          + "(SELECT COUNT(*) FROM users u WHERE u.role_id = r.id) AS user_count "
          + "FROM roles r ORDER BY r.id", rs -> {
                Role r = map(rs);
                r.setUserCount(rs.getLong("user_count"));
                return r;
            });
    }

    public List<Role> findAllSimple(Connection conn) {
        return Db.query(conn, "SELECT id, name, description FROM roles ORDER BY id", MAPPER);
    }
}
