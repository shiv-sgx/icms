@capability:audit-log
Feature: Audit log viewing and CSV export
  In order to maintain a tamper-evident activity trail
  As an admin
  I want to view all system audit events and export them as a CSV file

  # AuditExportAction streams a CSV with headers:
  #   Timestamp, User, Role, Action, Entity, IP, Result
  # exportAudit supports optional ?actionName=&result= filters.
  # Max 5000 rows per export.

  Background:
    Given the user "admin" with password "Password@123" is logged in

  @critical @smoke
  Scenario: Admin views the audit log page
    When the admin requests "/admin/audit"
    Then the response is successful
    And the response body contains "action"

  @critical
  Scenario: Admin exports the full audit log as CSV
    When the admin requests "/admin/exportAudit"
    Then the response content type is "text/csv"
    And the response body contains the CSV headers "Timestamp,User,Role,Action,Entity,IP,Result"

  @major
  Scenario: Admin exports audit log filtered by action
    When the admin requests "/admin/exportAudit?actionName=LOGIN"
    Then the response content type is "text/csv"
    And the response body contains "LOGIN"

  @major
  Scenario: Admin exports audit log filtered by result
    When the admin requests "/admin/exportAudit?result=FAILED"
    Then the response content type is "text/csv"
    And the response body contains "FAILED"

  @major @security
  Scenario: Non-admin user cannot access the audit export endpoint
    Given the user "manager" with password "Password@123" is logged in
    When the manager requests "/admin/exportAudit"
    Then the access is denied
