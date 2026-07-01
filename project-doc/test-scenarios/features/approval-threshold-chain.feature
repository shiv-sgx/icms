@capability:approval-threshold-chain
Feature: Approval chain creation based on amount thresholds
  In order to ensure financial controls on claim payouts
  As the ICMS approval system
  When an agent forwards a claim the correct multi-level approval chain must be built
  based on the configured amount thresholds

  # Thresholds (seed.sql):
  #   L1: $0 – $25,000    (agent authority alone)
  #   L2: $25,001 – $100,000  (manager required)
  #   L3: $100,001+           (director also required)
  # ApprovalService.createForwardChain records L1 as APPROVED,
  # creates L2 PENDING if amount > 25000,
  # creates L3 PENDING if amount > 100000.
  # The forwardForApproval path moves claim to APPROVED (pending=0) or PENDING_APPROVAL.

  @critical
  Scenario: Forwarding a claim with amount ≤ 25000 results in immediate APPROVED status
    Given a claim with estimated loss 20000 is in status "UNDER_REVIEW"
    And the claim has no prior assessment
    When the agent forwards that claim for approval
    Then the approval chain has only L1 as APPROVED
    And the claim status is "APPROVED"

  @critical
  Scenario: Forwarding a claim with amount between 25001 and 100000 creates L2 pending
    Given a claim with estimated loss 60000 is in status "UNDER_REVIEW"
    And the claim has no prior assessment
    When the agent forwards that claim for approval
    Then the approval chain has L1 APPROVED and L2 PENDING
    And the claim status is "PENDING_APPROVAL"

  @critical
  Scenario: Forwarding a claim with amount above 100000 creates L2 and L3 pending
    Given a claim with estimated loss 500000 is in status "UNDER_REVIEW"
    And the claim has no prior assessment
    When the agent forwards that claim for approval
    Then the approval chain has L1 APPROVED, L2 PENDING, and L3 PENDING
    And the claim status is "PENDING_APPROVAL"

  @major
  Scenario: Assessment net payable is used over estimated loss when assessment is present
    Given a claim has estimated loss 200000 but assessment net payable 20000
    When the agent forwards that claim for approval
    Then the approval chain has only L1 as APPROVED
    And the claim status is "APPROVED"
