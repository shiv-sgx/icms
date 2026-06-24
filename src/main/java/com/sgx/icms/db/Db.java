package com.sgx.icms.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.sgx.icms.config.DataSourceProvider;

/**
 * Tiny JDBC helper — the single place raw JDBC plumbing lives, so DAOs stay
 * declarative. Every query is parameterised (injection-safe); every resource is
 * closed via try-with-resources.
 *
 * <p>Two execution styles:
 * <ul>
 *   <li>{@link #withConnection} — one auto-commit unit of work (single reads/writes).</li>
 *   <li>{@link #inTransaction} — explicit transaction (commit on success, rollback on
 *       any exception) for multi-statement writes.</li>
 * </ul>
 * DAO methods take a {@link Connection} so services can compose several DAO calls
 * inside one transaction.
 */
public final class Db {

    /** A unit of work that needs a connection and may fail with a SQL error. */
    @FunctionalInterface
    public interface ConnectionFunction<T> {
        T apply(Connection conn) throws SQLException;
    }

    private Db() {
    }

    /** Borrows a pooled connection in auto-commit mode, runs {@code work}, returns its result. */
    public static <T> T withConnection(ConnectionFunction<T> work) {
        try (Connection conn = DataSourceProvider.dataSource().getConnection()) {
            return work.apply(conn);
        } catch (SQLException e) {
            throw new DaoException("Database operation failed", e);
        }
    }

    /**
     * Runs {@code work} inside a single transaction: autocommit off, commit on normal
     * return, rollback on any exception, original auto-commit restored on close.
     */
    public static <T> T inTransaction(ConnectionFunction<T> work) {
        try (Connection conn = DataSourceProvider.dataSource().getConnection()) {
            boolean prevAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                T result = work.apply(conn);
                conn.commit();
                return result;
            } catch (RuntimeException | SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(prevAutoCommit);
            }
        } catch (SQLException e) {
            throw new DaoException("Transaction failed", e);
        }
    }

    public static <T> List<T> query(Connection conn, String sql, RowMapper<T> mapper, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapper.map(rs));
                }
                return out;
            }
        } catch (SQLException e) {
            throw new DaoException("Query failed: " + sql, e);
        }
    }

    /** Returns the first row mapped, or {@code null} when the result set is empty. */
    public static <T> T queryOne(Connection conn, String sql, RowMapper<T> mapper, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapper.map(rs) : null;
            }
        } catch (SQLException e) {
            throw new DaoException("Query failed: " + sql, e);
        }
    }

    /** Convenience for {@code SELECT COUNT(*)}-style scalar longs. */
    public static long queryLong(Connection conn, String sql, Object... params) {
        Long v = queryOne(conn, sql, rs -> rs.getLong(1), params);
        return v == null ? 0L : v;
    }

    public static int update(Connection conn, String sql, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Update failed: " + sql, e);
        }
    }

    /** Executes an INSERT and returns the generated auto-increment key. */
    public static long insert(Connection conn, String sql, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        } catch (SQLException e) {
            throw new DaoException("Insert failed: " + sql, e);
        }
    }

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            ps.setObject(i + 1, params[i]);
        }
    }
}
