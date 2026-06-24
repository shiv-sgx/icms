package com.sgx.icms.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.sgx.icms.db.Db;
import com.sgx.icms.db.RowMapper;
import com.sgx.icms.domain.Communication;

public class CommunicationDao {

    private static final RowMapper<Communication> MAPPER = CommunicationDao::map;

    private static Communication map(ResultSet rs) throws SQLException {
        Communication c = new Communication();
        c.setId(rs.getLong("id"));
        c.setClaimId(rs.getLong("claim_id"));
        long sid = rs.getLong("sender_id");
        c.setSenderId(rs.wasNull() ? null : sid);
        c.setSenderName(rs.getString("sender_name"));
        c.setChannel(rs.getString("channel"));
        c.setContent(rs.getString("content"));
        c.setCreatedAt(rs.getObject("created_at", java.time.LocalDateTime.class));
        return c;
    }

    public List<Communication> findByClaim(Connection conn, long claimId) {
        return Db.query(conn,
            "SELECT id, claim_id, sender_id, sender_name, channel, content, created_at "
          + "FROM communications WHERE claim_id = ? ORDER BY created_at ASC", MAPPER, claimId);
    }

    /** Most recent messages across all claims (with claim number) for the comms feed. */
    public List<Communication> findRecent(Connection conn, int limit) {
        return Db.query(conn,
            "SELECT m.id, m.claim_id, m.sender_id, m.sender_name, m.channel, m.content, m.created_at, "
          + "cl.claim_no AS claim_no FROM communications m "
          + "JOIN claims cl ON cl.id = m.claim_id ORDER BY m.created_at DESC LIMIT ?",
            rs -> {
                Communication c = map(rs);
                c.setClaimNo(rs.getString("claim_no"));
                return c;
            }, limit);
    }

    public long insert(Connection conn, Communication c) {
        return Db.insert(conn,
            "INSERT INTO communications (claim_id, sender_id, sender_name, channel, content) VALUES (?,?,?,?,?)",
            c.getClaimId(), c.getSenderId(), c.getSenderName(),
            c.getChannel() == null ? "MESSAGE" : c.getChannel(), c.getContent());
    }
}
