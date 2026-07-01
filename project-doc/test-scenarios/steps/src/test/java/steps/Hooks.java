package steps;

import io.cucumber.java.Before;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Per-scenario database reset — the cornerstone of test isolation for this
 * suite. The specs are stateful HTTP integration tests that mutate shared rows
 * (claims, users, settlements). Without a reset, one scenario's writes break a
 * later scenario's preconditions (e.g. an admin password-reset invalidates every
 * subsequent customer login). This {@code @Before} hook restores the exact seed
 * baseline before EVERY scenario, so each one starts from identical, known
 * state and the suite is order-independent.
 *
 * <p>Data-only (DML) reset via JDBC: it assumes the schema already exists
 * (created once by {@code setup.sh}). Connection settings come from the
 * environment with safe local defaults; the host defaults to {@code 127.0.0.1}
 * (NOT {@code localhost}) so the driver does not attempt an IPv6 {@code ::1}
 * connection.</p>
 *
 * <p>The reset script is {@code src/test/resources/reset.sql}, loaded from the
 * test classpath. Cucumber runs scenarios sequentially by default, so there is
 * no race between the reset and the application reading the same database.</p>
 */
public class Hooks {

    private static final String RESET_RESOURCE = "/reset.sql";

    private static final String DB_URL = envOrDefault(
            "ICMS_DB_URL",
            "jdbc:mysql://127.0.0.1:3306/icms"
                    + "?useSSL=false&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=UTC&characterEncoding=UTF-8");
    private static final String DB_USER = envOrDefault("ICMS_DB_USER", "root");
    private static final String DB_PASSWORD = envOrDefault("ICMS_DB_PASSWORD", "");

    /** Parsed once; reused for every scenario. */
    private static final List<String> RESET_STATEMENTS = loadStatements();

    @Before(order = 0)
    public void resetDatabaseToSeedBaseline() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {
            for (String sql : RESET_STATEMENTS) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            // Fail loudly: a broken reset would silently corrupt every scenario.
            throw new SQLException("Failed to reset ICMS database to seed baseline via "
                    + DB_URL + " — ensure the schema exists (run setup.sh) and MySQL is "
                    + "reachable on 127.0.0.1:3306. Cause: " + e.getMessage(), e);
        }
    }

    /**
     * Load reset.sql from the classpath and split it into individual statements.
     * Line comments ({@code -- ...}) and blank lines are stripped; statements are
     * delimited by a trailing semicolon. The seed data contains no semicolons
     * inside string literals, so simple semicolon splitting is safe here.
     */
    private static List<String> loadStatements() {
        InputStream in = Hooks.class.getResourceAsStream(RESET_RESOURCE);
        if (in == null) {
            throw new IllegalStateException("Reset script not found on classpath: " + RESET_RESOURCE);
        }
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("--")) {
                    continue;
                }
                current.append(line).append('\n');
                if (trimmed.endsWith(";")) {
                    String sql = current.toString().trim();
                    sql = sql.substring(0, sql.length() - 1).trim(); // drop trailing ';'
                    if (!sql.isEmpty()) {
                        statements.add(sql);
                    }
                    current.setLength(0);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read reset script " + RESET_RESOURCE, e);
        }
        if (statements.isEmpty()) {
            throw new IllegalStateException("Reset script " + RESET_RESOURCE + " produced no statements");
        }
        return statements;
    }

    private static String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : fallback;
    }
}
