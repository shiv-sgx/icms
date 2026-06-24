package com.sgx.icms.config;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralised, externalised configuration (12-Factor).
 *
 * <p>Resolution order for any key, highest priority first:
 * <ol>
 *   <li>Environment variable ({@code DB_URL} for key {@code db.url})</li>
 *   <li>JVM system property ({@code -Ddb.url=...})</li>
 *   <li>{@code db.properties} on the classpath (committed defaults, no secrets)</li>
 * </ol>
 *
 * <p>Secrets (DB password) must come from the environment in real deployments;
 * the committed {@code db.properties} only holds safe local defaults.
 */
public final class AppConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);
    private static final String RESOURCE = "db.properties";

    private static final AppConfig INSTANCE = new AppConfig();

    private final Properties defaults = new Properties();

    private AppConfig() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(RESOURCE)) {
            if (in != null) {
                defaults.load(in);
                LOG.info("Loaded {} default configuration keys from {}", defaults.size(), RESOURCE);
            } else {
                LOG.warn("{} not found on classpath; relying solely on env/system properties", RESOURCE);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load " + RESOURCE, e);
        }
    }

    public static AppConfig get() {
        return INSTANCE;
    }

    /** Returns the configured value or {@code null} if unset everywhere. */
    public String get(String key) {
        String envKey = key.toUpperCase().replace('.', '_');
        String v = System.getenv(envKey);
        if (v == null || v.isEmpty()) {
            v = System.getProperty(key);
        }
        if (v == null || v.isEmpty()) {
            v = defaults.getProperty(key);
        }
        return v;
    }

    public String get(String key, String fallback) {
        String v = get(key);
        return (v == null || v.isEmpty()) ? fallback : v;
    }

    public String require(String key) {
        String v = get(key);
        if (v == null || v.isEmpty()) {
            throw new IllegalStateException("Required configuration '" + key
                    + "' is missing (set env " + key.toUpperCase().replace('.', '_')
                    + " or add it to " + RESOURCE + ")");
        }
        return v;
    }

    public int getInt(String key, int fallback) {
        String v = get(key);
        if (v == null || v.isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            LOG.warn("Config '{}'='{}' is not an int; using default {}", key, v, fallback);
            return fallback;
        }
    }
}
