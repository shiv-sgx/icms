@capability:approval-workflow
Feature: Manager approval workflow
  In order to control claim payouts based on amount thresholds
  As a manager
  I want to approve, reject, return, or hold claims in the approval queue
  so that each decision advances the claim status appropriately

  # Approval thresholds (from seed.sql):
  #   L1: Agent Approval      $0 – $25,000
  #   L2: Manager Approval    $25,001 – $100,000
  #   L3: Director Approval   $100,001+
  #
  # Seeded pending-approval claims with L2 pending (manager id 2):
  #   claim 1 (CLM-2024-0891) - $30,080 net payable → L1 approved, L2 pending
  #   claim 3 (CLM-2024-0874) - property claim      → L1 approved, L2 pending
  #   claim 4 (CLM-2024-0871) - $1M life claim      → L1 approved, L2+L3 pending
  # Manager login: username=manager (Michael Anderson, id 2).

  Background:
    Given the user "manager" with password "Password@123" is logged in

  @critical @smoke
  Scenario: Manager views the approval queue
    When the manager requests "/manager/approvals"
    Then the response is successful
    And the response body contains "PENDING_APPROVAL"

  @critical @smoke
  Scenario: Manager views the manager dashboard
    When the manager requests "/manager/dashboard"
    Then the response is successful

  @critical
  Scenario: Manager approves a claim, advancing it to APPROVED when all levels pass
    Given claim 1 has a pending L2 approval and no further levels
    When the manager posts to "/decide" with claim id 1, decision "APPROVED", and remarks "Looks good."
    Then the response redirects to the claim detail
    And the claim 1 now has status "APPROVED"

  @critical
  Scenario: Manager rejects a claim, moving it to REJECTED
    Given claim 3 has a pending L2 approval
    When the manager posts to "/decide" with claim id 3, decision "REJECTED", and remarks "Insufficient evidence."
    Then the response redirects to the claim detail
    And the claim 3 now has status "REJECTED"

  @major
  Scenario: Manager returns a claim to the agent for further review
    Given claim 3 has a pending L2 approval
    When the manager posts to "/decide" with claim id 3, decision "RETURNED", and remarks "Need updated survey."
    Then the response redirects to the claim detail
    And the claim 3 now has status "UNDER_REVIEW"

  @major
  Scenario: Manager puts a claim on hold
    Given claim 3 has a pending L2 approval
    When the manager posts to "/decide" with claim id 3, decision "ON_HOLD", and remarks "Pending legal review."
    Then the response redirects to the claim detail
    And the claim 3 now has status "ON_HOLD"

  @major
  Scenario: Manager approves an L2 level on a multi-level claim, leaving L3 pending
    Given claim 4 has L2 pending and L3 pending
    When the manager posts to "/decide" with claim id 4, decision "APPROVED", and remarks "L2 approved."
    Then the response redirects to the claim detail
    And the claim 4 now has status "PENDING_APPROVAL"

  @major @validation
  Scenario: Decision is rejected when no pending approval exists for the claim
    Given claim 5 has status "SETTLED" and no pending approvals
    When the manager posts to "/decide" with claim id 5, decision "APPROVED", and remarks ""
    Then the manager sees an error about no pending approval

  @major @validation
  Scenario Outline: Decision is rejected for invalid decision codes
    Given claim 1 has a pending L2 approval
    When the manager posts to "/decide" with claim id 1, decision "<bad_decision>", and remarks ""
    Then the manager sees an error about invalid decision

    Examples:
      | bad_decision |
      | APPROVE      |
      | DENY         |
      |              |
