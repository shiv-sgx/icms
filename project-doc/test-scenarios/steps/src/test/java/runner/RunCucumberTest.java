package runner;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

/**
 * JUnit 5 platform suite runner for the ICMS BDD acceptance tests.
 *
 * Usage:
 *   mvn test -pl project-doc/test-scenarios/steps \
 *     -DICMS_BASE_URL=http://localhost:8080 \
 *     -DICMS_DEMO_PASSWORD=Test@1234
 *
 * The feature files must be on the test classpath. The Maven build copies them
 * from ../../features/ (see the resource filtering in pom.xml), or you can
 * point Cucumber at the absolute path via the cucumber.features system property:
 *
 *   mvn test ... -Dcucumber.features=project-doc/test-scenarios/features
 *       -Dcucumber.glue=steps
 *
 * The JSON plugin output feeds gherkin-runner's HTML report generator.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "steps")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, json:target/cucumber-reports/.cucumber.json, html:target/cucumber-reports/report.html"
)
public class RunCucumberTest {
    // Suite entry point — no code needed here.
}
