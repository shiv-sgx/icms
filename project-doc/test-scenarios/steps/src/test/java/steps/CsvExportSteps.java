package steps;

import io.cucumber.java.en.Then;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for audit-log.feature and reports-and-analytics.feature
 * covering the CSV export endpoints:
 *   /admin/exportAudit  (AuditExportAction)
 *   /manager/exportReport?key=<key>  (ReportExportAction)
 *
 * Note: the When steps ("the admin/manager/customer/agent requests ...") are
 * already registered in ClaimSteps to avoid duplicate step expressions.
 * Only unique Then assertions belong here.
 */
public class CsvExportSteps {

    private final IcmsWorld world;

    public CsvExportSteps(IcmsWorld world) {
        this.world = world;
    }

    @Then("the response body contains the CSV headers {string}")
    public void theResponseBodyContainsCsvHeaders(String expectedHeaders) {
        IcmsWorld.HttpResponse r = world.getLastResponse();
        // CSV header row appears in the body, possibly with or without quotes.
        String body = r.body;
        // Strip quotes and check each column name.
        String unquoted = body.replaceAll("\"", "");
        for (String header : expectedHeaders.split(",")) {
            String h = header.trim();
            assertTrue(unquoted.contains(h),
                    "Expected CSV header '" + h + "' in response. Body-start="
                            + body.substring(0, Math.min(300, body.length())));
        }
    }
}
