package com.sgx.icms.db;

/**
 * Unchecked wrapper for {@link java.sql.SQLException} and other persistence failures,
 * so DAO callers are not forced to handle checked SQL exceptions everywhere. The
 * original cause is always preserved for logging.
 */
public class DaoException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }

    public DaoException(String message) {
        super(message);
    }
}
