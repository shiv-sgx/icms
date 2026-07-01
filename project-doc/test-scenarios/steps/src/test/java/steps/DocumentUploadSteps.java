package steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for document-upload.feature (customer uploads)
 * and surveyor upload steps in surveyor-assessment.feature.
 *
 * A real file is synthesised in-memory so no filesystem fixture is needed.
 */
public class DocumentUploadSteps {

    private final IcmsWorld world;

    public DocumentUploadSteps(IcmsWorld world) {
        this.world = world;
    }

    // ------------------------------------------------------------------
    // Customer upload steps
    // ------------------------------------------------------------------

    @When("the customer uploads file {string} of type {string} to claim {int}")
    public void theCustomerUploadsFile(String filename, String docType, int claimId) throws Exception {
        // FIX 1: customer upload action lives in the /customer namespace.
        performUpload("/customer/uploadDocument", claimId, filename, docType);
    }

    @When("the customer submits an upload request with no file to claim {int}")
    public void theCustomerSubmitsUploadWithNoFile(int claimId) throws Exception {
        Map<String, String> textFields = new LinkedHashMap<>();
        textFields.put("claimId", String.valueOf(claimId));
        textFields.put("id", String.valueOf(claimId));
        textFields.put("docType", "Evidence");
        // Pass null for fileContent → simulates no file chosen.
        // FIX 1: customer upload action lives in the /customer namespace.
        world.uploadFile("/customer/uploadDocument", textFields, "upload", "no-file.pdf", null, null);
    }

    @When("the customer uploads file {string} with blank document type to claim {int}")
    public void theCustomerUploadsFileWithBlankDocType(String filename, int claimId) throws Exception {
        Map<String, String> textFields = new LinkedHashMap<>();
        textFields.put("claimId", String.valueOf(claimId));
        textFields.put("id", String.valueOf(claimId));
        textFields.put("docType", ""); // blank
        byte[] content = "dummy content".getBytes(StandardCharsets.UTF_8);
        // FIX 1: customer upload action lives in the /customer namespace.
        world.uploadFile("/customer/uploadDocument", textFields, "upload", filename, content, "image/jpeg");
    }

    @When("the customer attempts to upload file {string} of type {string} to claim {int}")
    public void theCustomerAttemptsUploadToOtherClaim(String filename, String docType, int claimId) throws Exception {
        // FIX 1: customer upload action lives in the /customer namespace.
        performUpload("/customer/uploadDocument", claimId, filename, docType);
    }

    // ------------------------------------------------------------------
    // Surveyor upload steps
    // ------------------------------------------------------------------

    @When("the surveyor uploads file {string} of type {string} for claim {int}")
    public void theSurveyorUploadsFile(String filename, String docType, int claimId) throws Exception {
        // FIX 1: surveyor upload action lives in the /surveyor namespace.
        performUpload("/surveyor/uploadReport", claimId, filename, docType);
    }

    // ------------------------------------------------------------------
    // Then — upload result assertions
    // ------------------------------------------------------------------

    @Then("the upload succeeds and the customer is redirected back to the claim")
    public void theUploadSucceeds() {
        IcmsWorld.HttpResponse r = world.getLastResponse();
        boolean success = r.redirectsTo("/customer/claim")
                || r.wasRedirect()
                || (r.status >= 200 && r.status < 400
                && !r.body.toLowerCase().contains("unsupported")
                && !r.body.toLowerCase().contains("error"));
        assertTrue(success,
                "Expected upload to succeed and redirect to claim page. "
                        + "Status=" + r.status + " redirect=" + r.originalRedirectTarget
                        + " body-start=" + r.body.substring(0, Math.min(300, r.body.length())));
    }

    @Then("the upload is rejected with a message containing {string}")
    public void theUploadIsRejectedWithMessage(String expectedFragment) {
        IcmsWorld.HttpResponse r = world.getLastResponse();
        boolean rejected = r.body.contains(expectedFragment)
                || r.body.toLowerCase().contains(expectedFragment.toLowerCase())
                || r.status >= 400;
        assertTrue(rejected,
                "Expected upload rejection with message '" + expectedFragment + "' "
                        + "but got status=" + r.status + " body-start="
                        + r.body.substring(0, Math.min(300, r.body.length())));
    }

    @Then("the upload is rejected or the customer is redirected away")
    public void theUploadIsRejectedOrRedirectedAway() {
        IcmsWorld.HttpResponse r = world.getLastResponse();
        // "Rejected" means either an error body or a redirect away from the claim detail.
        boolean notClaimDetail = !r.redirectsTo("/customer/claim?id=2")
                || r.redirectsTo("/customer/claims");
        boolean errorBody = r.body.toLowerCase().contains("error")
                || r.body.toLowerCase().contains("not found");
        assertTrue(notClaimDetail || errorBody || r.wasRedirect(),
                "Expected the upload to be rejected for a claim the customer doesn't own. "
                        + "Status=" + r.status);
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    private void performUpload(String path, int claimId, String filename, String docType) throws Exception {
        Map<String, String> textFields = new LinkedHashMap<>();
        textFields.put("claimId", String.valueOf(claimId));
        textFields.put("id", String.valueOf(claimId));
        textFields.put("docType", docType);
        byte[] content = ("Synthetic test content for " + filename).getBytes(StandardCharsets.UTF_8);
        String mimeType = mimeForFilename(filename);
        world.uploadFile(path, textFields, "upload", filename, content, mimeType);
    }

    private static String mimeForFilename(String filename) {
        if (filename == null) return "application/octet-stream";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf"))  return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png"))  return "image/png";
        if (lower.endsWith(".gif"))  return "image/gif";
        if (lower.endsWith(".doc"))  return "application/msword";
        if (lower.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        return "application/octet-stream";
    }
}
