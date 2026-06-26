package com.sgx.icms.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class DaoExceptionTest {

    @Test
    void messageOnlyConstructorHasNoCause() {
        DaoException e = new DaoException("boom");
        assertEquals("boom", e.getMessage());
        assertNull(e.getCause());
    }

    @Test
    void messageAndCauseConstructorPreservesBoth() {
        Throwable cause = new RuntimeException("root cause");
        DaoException e = new DaoException("wrapped", cause);
        assertEquals("wrapped", e.getMessage());
        assertSame(cause, e.getCause());
    }

    @Test
    void isUncheckedException() {
        DaoException e = new DaoException("x");
        org.junit.jupiter.api.Assertions.assertTrue(e instanceof RuntimeException);
    }
}
