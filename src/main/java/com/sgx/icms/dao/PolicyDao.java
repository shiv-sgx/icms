package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.Policy;

public class PolicyDao {

    private static final String COLS =
            "id, policy_no, policyholder_id, type, sum_insured, premium, start_date, expiry_date, ncb_discount, status";

    static final RowMapper<Policy> MAPPER = PolicyDao::map;

    private static Policy map(ResultSet rs) throws SQLException {
        Policy p = new Policy();
        p.setId(rs.getLong("id"));
        p.setPolicyNo(rs.getString("policy_no"));
        p.setPolicyholderId(rs.getLong("policyholder_id"));
        p.setType(rs.getString("type"));
        p.setSumInsured(rs.getBigDecimal("sum_insured"));
        p.setPremium(rs.getBigDecimal("premium"));
        p.setStartDate(rs.getObject("start_date", java.time.LocalDate.class));
        p.setExpiryDate(rs.getObject("expiry_date", java.time.LocalDate.class));
        p.setNcbDiscount(rs.getBigDecimal("ncb_discount"));
        p.setStatus(rs.getString("status"));
        return p;
    }

    public Policy findById(Connection conn, long id) {
        return Db.queryOne(conn, "SELECT " + COLS + " FROM policies WHERE id = ?", MAPPER, id);
    }

    public List<Policy> findByPolicyholder(Connection conn, long policyholderId) {
        return Db.query(conn,
                "SELECT " + COLS + " FROM policies WHERE policyholder_id = ? ORDER BY policy_no",
                MAPPER, policyholderId);
    }
}
