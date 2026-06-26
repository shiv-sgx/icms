package com.sgx.icms.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * AppConfig is a classpath-loaded singleton (db.properties ships icms.page.size=15
 * among other defaults); these tests exercise the env/system-property/default
 * resolution order without needing to set real environment variables (the JVM
 * doesn't allow that from within a running process, so the "environment variable"
 * tier of the precedence chain is exercised indirectly via system properties only).
 */
class AppConfigTest {

    private static final String KNOWN_KEY = "icms.page.size";
    private static final String UNKNOWN_KEY = "icms.nonexistent.test.key";

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty(KNOWN_KEY);
        System.clearProperty(UNKNOWN_KEY);
    }

    @Test
    void getReturnsDefaultFromClasspathProperties() {
        assertEquals("15", AppConfig.get().get(KNOWN_KEY));
    }

    @Test
    void getReturnsNullForUnknownKey() {
        assertNull(AppConfig.get().get(UNKNOWN_KEY));
    }

    @Test
    void getWithFallbackReturnsFallbackForUnknownKey() {
        assertEquals("fallback-value", AppConfig.get().get(UNKNOWN_KEY, "fallback-value"));
    }

    @Test
    void getWithFallbackReturnsActualValueWhenPresent() {
        assertEquals("15", AppConfig.get().get(KNOWN_KEY, "999"));
    }

    @Test
    void systemPropertyTakesPrecedenceOverClasspathDefault() {
        System.setProperty(KNOWN_KEY, "777");
        assertEquals("777", AppConfig.get().get(KNOWN_KEY));
    }

    @Test
    void getIntParsesConfiguredValue() {
        assertEquals(15, AppConfig.get().getInt(KNOWN_KEY, 99));
    }

    @Test
    void getIntFallsBackWhenKeyMissing() {
        assertEquals(42, AppConfig.get().getInt(UNKNOWN_KEY, 42));
    }

    @Test
    void getIntFallsBackOnMalformedNumber() {
        System.setProperty(KNOWN_KEY, "not-a-number");
        assertEquals(7, AppConfig.get().getInt(KNOWN_KEY, 7));
    }

    @Test
    void requireReturnsValueWhenPresent() {
        assertEquals("15", AppConfig.get().require(KNOWN_KEY));
    }

    @Test
    void requireThrowsWhenKeyMissing() {
        assertThrows(IllegalStateException.class, () -> AppConfig.get().require(UNKNOWN_KEY));
    }
}
