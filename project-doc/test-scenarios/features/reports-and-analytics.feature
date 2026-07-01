@capability:reports-and-analytics
Feature: Manager reports and analytics
  In order to oversee the claims pipeline and financial exposure
  As a manager
  I want to view analytics reports and export individual reports as CSV files

  # ReportService builds six reports (keys from ReportService.allReports):
  #   claims-volume, claims-type, sla-compliance, settlement-tat,
  #   fraud-detection, agent-performance
  # ReportExportAction returns 404 for an unknown key and CSV for a known key.

  Background:
    Given the user "manager" with password "Password@123" is logged in

  @major @smoke
  Scenario: Manager views the reports page
    When the manager requests "/manager/reports"
    Then the response is successful
    And the response body contains "Reports"

  @major
  Scenario Outline: Manager exports a known report as CSV
    When the manager requests "/manager/exportReport?key=<key>"
    Then the response content type is "text/csv"
    And the response body is not empty

    Examples:
      | key               |
      | claims-volume     |
      | claims-type       |
      | sla-compliance    |
      | settlement-tat    |
      | fraud-detection   |
      | agent-performance |

  @major @validation
  Scenario: Exporting an unknown report key returns 404
    When the manager requests "/manager/exportReport?key=nonexistent-report"
    Then the response status is 404

  @major @security
  Scenario: Customer cannot access the reports page
    Given the user "customer" with password "Password@123" is logged in
    When the customer requests "/manager/reports"
    Then the access is denied

  @major @security
  Scenario: Agent cannot export a report
    Given the user "agent" with password "Password@123" is logged in
    When the agent requests "/manager/exportReport?key=claims-volume"
    Then the access is denied
