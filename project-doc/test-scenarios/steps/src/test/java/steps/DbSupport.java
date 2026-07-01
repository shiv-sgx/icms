package steps;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Small read-only DB helper for assertions where the HTML rendering is an
 * unreliable witness in this environment (e.g. the agent payment tracker behind
 * the IPv4->IPv6 bridge). The workflow itself is still exercised over HTTP; only
 * the final verification reads the persisted system-of-record state.
 *
 * <p>Connection settings mirror {@link Hooks} — env-overridable, defaulting to
 * {@code 127.0.0.1} (not {@code localhost}) to avoid an IPv6 connection attempt.</p>
 */
final class DbSupport {

    private static final String DB_URL = envOrDefault(
            "ICMS_DB_URL",
            "jdbc:mysql://127.0.0.1:3306/icms"
                    + "?useSSL=false&allowPublicKeyRetrieval=true"
                    + "&serverTimezone=UTC&characterEncoding=UTF-8");
    private static final String DB_USER = envOrDefault("ICMS_DB_USER", "root");
    private static final String DB_PASSWORD = envOrDefault("ICMS_DB_PASSWORD", "");

    private DbSupport() {
    }

    /** Returns the settlement status for a claim, or {@code null} if none exists. */
    static String settlementStatus(long claimId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT status FROM settlements WHERE claim_id = ?")) {
            ps.setLong(1, claimId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    private static String envOrDefault(String key, String fallback) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : fallback;
    }
}
