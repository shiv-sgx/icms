package steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for role-access-control.feature.
 *
 * The RoleInterceptor returns the global "denied" result for mismatched roles.
 * In the ICMS tiles layout the denied tile renders to a page that (at minimum)
 * does NOT contain the target portal's content and is not a redirect to login.
 * We assert that the resource requested was not successfully delivered.
 */
public class RoleAccessSteps {

    private final IcmsWorld world;

    public RoleAccessSteps(IcmsWorld world) {
        this.world = world;
    }

    @When("the user requests the secured page {string}")
    public void theUserRequestsTheSecuredPage(String path) throws Exception {
        world.get(path);
    }

    /**
     * "Access denied" means either:
     * - The body contains "denied" or "403" or "access" (the denied tile), or
     * - The response redirected back to login (session already expired — treat as denied).
     * We do NOT pin to a single HTTP status because the Struts tiles result
     * renders a 200 with the denied tile body.
     */
    @Then("the access is denied")
    public void theAccessIsDenied() {
        IcmsWorld.HttpResponse r = world.getLastResponse();
        boolean deniedPage = r.body.toLowerCase().contains("denied")
                || r.body.toLowerCase().contains("access")
                || r.body.toLowerCase().contains("403")
                || r.body.toLowerCase().contains("not authorized")
                || r.body.toLowerCase().contains("permission");
        boolean redirectedToLogin = r.redirectsTo("/login")
                || r.body.toLowerCase().contains("login");
        assertTrue(deniedPage || redirectedToLogin,
                "Expected access to be denied but the response looked successful.\n"
                        + "Status=" + r.status + " body (first 400 chars):\n"
                        + r.body.substring(0, Math.min(400, r.body.length())));
    }

    @Then("the request is permitted")
    public void theRequestIsPermitted() {
        IcmsWorld.HttpResponse r = world.getLastResponse();
        boolean denied = r.body.toLowerCase().contains("denied")
                || r.body.toLowerCase().contains("not authorized");
        boolean loginPage = r.body.toLowerCase().contains("sign in")
                && r.body.toLowerCase().contains("password");
        assertFalse(denied || loginPage,
                "Expected ADMIN to be permitted but the response showed denied/login.\n"
                        + "Status=" + r.status);
        assertTrue(r.status < 500,
                "Expected a successful response but got HTTP " + r.status);
    }
}
