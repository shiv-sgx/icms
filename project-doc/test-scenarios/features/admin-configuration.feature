@capability:admin-configuration
Feature: Admin system configuration
  In order to tune ICMS business rules
  As an admin
  I want to manage SLA targets, approval thresholds, notification templates,
  and document requirements

  # AdminService covers:
  #   updateSla(id, hours)
  #   updateThreshold(id, min, max)
  #   updateTemplate(id, active, body)
  #   addDocumentRequirement(DocumentRequirement)
  #   deleteDocumentRequirement(id)
  # Seeded SLA ids match the six sla_config rows.

  Background:
    Given the user "admin" with password "Password@123" is logged in

  @major @smoke
  Scenario: Admin views the SLA configuration page
    When the admin requests "/admin/sla"
    Then the response is successful
    And the response body contains "stage"

  @major
  Scenario: Admin updates a SLA target
    When the admin posts to "/updateSla" with sla id 1 and hours 36
    Then the response redirects to the SLA page

  @major @smoke
  Scenario: Admin views the approval thresholds page
    When the admin requests "/admin/thresholds"
    Then the response is successful
    And the response body contains "minAmount"

  @major
  Scenario: Admin updates an approval threshold
    When the admin posts to "/updateThreshold" with threshold id 2, min 30000, and max 120000
    Then the response redirects to the thresholds page

  @major @smoke
  Scenario: Admin views the notification templates page
    When the admin requests "/admin/templates"
    Then the response is successful

  @major
  Scenario: Admin updates a notification template body
    When the admin posts to "/updateTemplate" with template id 1, active true, and body "Claim {claimNo} acknowledged by ICMS."
    Then the response redirects to the templates page

  @major @smoke
  Scenario: Admin views the document requirements page
    When the admin requests "/admin/documents"
    Then the response is successful

  @major
  Scenario: Admin adds a new document requirement
    When the admin posts to "/addDocument" with claim type "TRAVEL" and doc type "Travel Itinerary"
    Then the response redirects to the documents page

  @major @validation
  Scenario: Adding a document requirement fails when claim type is blank
    When the admin posts to "/addDocument" with claim type "" and doc type "Some Doc"
    Then the response body contains an error about required fields

  @major
  Scenario: Admin deletes a document requirement
    When the admin posts to "/deleteDocument" with document requirement id 1
    Then the response redirects to the documents page
