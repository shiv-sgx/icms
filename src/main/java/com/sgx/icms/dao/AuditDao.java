package com.sgx.icms.dao;

import java.sql.Connection;
import java.util.List;

import com.sgx.icms.domain.AuditLog;

/** Persistence for {@code audit_logs}. */
public interface AuditDao {

    void insert(Connection conn, AuditLog log);

    /** Paged + optional filtered read for the admin audit viewer. */
    List<AuditLog> find(Connection conn, String action, String result, int limit, int offset);

    long count(Connection conn, String action, String result);
}
