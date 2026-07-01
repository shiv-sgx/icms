@capability:claim-withdrawal
Feature: Customer claim withdrawal
  In order to cancel an in-progress claim
  As a policyholder
  I want to withdraw my claim before it has been approved or settled

  # ClaimStatus.isWithdrawable allows: DRAFT, SUBMITTED, UNDER_REVIEW,
  # SURVEY_SCHEDULED, UNDER_ASSESSMENT, PENDING_APPROVAL, ON_HOLD.
  # Not allowed once: APPROVED, SETTLEMENT_PROCESSING, SETTLED, CLOSED, REJECTED, WITHDRAWN.

  Background:
    Given the user "customer" with password "Password@123" is logged in

  @critical
  Scenario: Customer withdraws a SUBMITTED claim
    Given claim "CLM-2024-0891" belongs to the logged-in customer and has status "SUBMITTED"
    When the customer posts to "/withdraw" with claim id for "CLM-2024-0891"
    Then the response redirects to the claims list
    And the claim "CLM-2024-0891" now has status "WITHDRAWN"

  @major
  Scenario Outline: Customer can withdraw claims in withdrawable statuses
    Given claim "CLM-TEST-WD01" belongs to the logged-in customer and has status "<status>"
    When the customer posts to "/withdraw" with claim id for "CLM-TEST-WD01"
    Then the response redirects to the claims list

    Examples:
      | status              |
      | DRAFT               |
      | SUBMITTED           |
      | UNDER_REVIEW        |
      | PENDING_APPROVAL    |
      | ON_HOLD             |

  @major @validation
  Scenario: Customer cannot withdraw an already approved claim
    Given claim id 11 belongs to the logged-in customer and has status "APPROVED"
    When the customer posts to "/withdraw" with claim id 11
    Then the system refuses the withdrawal

  @major @security
  Scenario: Customer cannot withdraw a claim belonging to another policyholder
    Given the user "customer" with password "Password@123" is logged in
    When the customer posts to "/withdraw" with claim id 2
    Then the system refuses the withdrawal
