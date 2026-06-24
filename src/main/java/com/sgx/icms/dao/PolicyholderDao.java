package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.Policyholder;

/**
 * Policyholder lookups. A logged-in CUSTOMER is linked to their policyholder
 * record by email (the seed aligns user.email with policyholders.email), since the
 * schema has no direct users→policyholders FK.
 */
public class PolicyholderDao {

    private static final String COLS =
            "id, first_name, last_name, dob, email, mobile, address, city, state, pin_code";

    private static final RowMapper<Policyholder> MAPPER = PolicyholderDao::map;

    private static Policyholder map(ResultSet rs) throws SQLException {
        Policyholder p = new Policyholder();
        p.setId(rs.getLong("id"));
        p.setFirstName(rs.getString("first_name"));
        p.setLastName(rs.getString("last_name"));
        p.setDob(rs.getObject("dob", java.time.LocalDate.class));
        p.setEmail(rs.getString("email"));
        p.setMobile(rs.getString("mobile"));
        p.setAddress(rs.getString("address"));
        p.setCity(rs.getString("city"));
        p.setState(rs.getString("state"));
        p.setPinCode(rs.getString("pin_code"));
        return p;
    }

    public Policyholder findByEmail(Connection conn, String email) {
        return Db.queryOne(conn, "SELECT " + COLS + " FROM policyholders WHERE email = ?", MAPPER, email);
    }

    public Policyholder findById(Connection conn, long id) {
        return Db.queryOne(conn, "SELECT " + COLS + " FROM policyholders WHERE id = ?", MAPPER, id);
    }
}
