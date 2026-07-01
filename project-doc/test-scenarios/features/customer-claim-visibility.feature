@capability:customer-claim-visibility
Feature: Customer claim list and detail visibility
  In order to track their insurance claims
  As a policyholder
  I want to view my own claims list and each claim's detail
  but never see claims belonging to other policyholders

  # ClaimService.getOwnedClaim enforces ownership:
  #   returns null (redirected to /customer/claims) if claim doesn't belong to the caller.
  # Seeded: customer (James Miller, policyholder 1) owns claims 1, 6, 11.

  Background:
    Given the user "customer" with password "Password@123" is logged in

  @critical @smoke
  Scenario: Customer sees the claims list page
    When the customer requests "/customer/claims"
    Then the response is successful

  @critical
  Scenario: Customer sees the detail of one of their own claims
    When the customer requests "/customer/claim?id=1"
    Then the response is successful
    And the response body contains "CLM-2024-0891"

  @critical @security
  Scenario: Customer is redirected when requesting a claim belonging to another policyholder
    When the customer requests "/customer/claim?id=2"
    Then the response redirects to "/customer/claims"

  @major @smoke
  Scenario: Customer views the customer dashboard with summary counts
    When the customer requests "/customer/dashboard"
    Then the response is successful

  @major @smoke
  Scenario: Customer views their profile page
    When the customer requests "/customer/profile"
    Then the response is successful
    And the response body contains "Ravi Patel"
