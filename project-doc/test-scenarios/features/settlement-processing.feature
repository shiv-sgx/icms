@capability:settlement-processing
Feature: Settlement authorisation and payment tracking
  In order to disburse approved claim payouts
  As an agent
  I want to authorise a settlement and advance the payment tracker step by step
  until the claim is SETTLED and ultimately CLOSED

  # Settlement.TRACKER order (from Settlement.java):
  #   AUTHORIZED → PAYMENT_INITIATED → BANK_PROCESSING → PAYMENT_CONFIRMED
  #   → CLAIMANT_NOTIFIED → CLOSED
  # When status reaches PAYMENT_CONFIRMED, claim is moved to SETTLED.
  # When status reaches CLOSED, claim is moved to CLOSED.
  # SettlementService.authorize requires: claim status APPROVED and no prior settlement.
  # Seeded claim 11 (CLM-2024-0838) has status APPROVED, no settlement yet.
  # Manager can override an existing settlement via /overrideSettlement.

  Background:
    Given the user "agent" with password "Password@123" is logged in

  @critical @smoke
  Scenario: Agent views the settlement screen for an approved claim
    When the agent requests "/agent/settlement?id=11"
    Then the response is successful
    And the response body contains "Settlement"

  @critical
  Scenario: Agent authorises a settlement for an approved claim
    Given claim 11 has status "APPROVED" and no existing settlement
    When the agent posts to "/processSettlement" with
      | id            | 11          |
      | amount        | 620000      |
      | paymentMethod | NEFT        |
      | accountHolder | James Miller|
      | bankName      | Chase Bank  |
      | accountNumber | 1234567890  |
      | ifscCode      | CHAS0001234 |
      | justification | Full loss settlement. |
    Then the response redirects to the settlement page for claim 11
    And the claim 11 now has status "SETTLEMENT_PROCESSING"

  @major @validation
  Scenario: Settlement authorisation is rejected when amount is zero
    Given claim 11 has status "APPROVED" and no existing settlement
    When the agent posts to "/processSettlement" with
      | id     | 11 |
      | amount | 0  |
    Then the agent sees an error about invalid settlement amount

  @major @validation
  Scenario: Settlement authorisation is rejected when amount is negative
    Given claim 11 has status "APPROVED" and no existing settlement
    When the agent posts to "/processSettlement" with
      | id     | 11   |
      | amount | -100 |
    Then the agent sees an error about invalid settlement amount

  @major @validation
  Scenario: Settlement authorisation is rejected for a non-approved claim
    Given claim 9 has status "UNDER_REVIEW"
    When the agent posts to "/processSettlement" with
      | id     | 9      |
      | amount | 50000  |
    Then the agent sees an error about claim status

  @critical
  Scenario: Agent advances the payment tracker from AUTHORIZED to PAYMENT_INITIATED
    Given claim 11 has an existing settlement in status "AUTHORIZED"
    When the agent posts to "/advanceSettlement" with id 11
    Then the settlement for claim 11 has status "PAYMENT_INITIATED"

  @critical
  Scenario: Payment confirmation moves claim status to SETTLED
    Given claim 11 has an existing settlement in status "BANK_PROCESSING"
    When the agent posts to "/advanceSettlement" with id 11
    Then the settlement for claim 11 has status "PAYMENT_CONFIRMED"
    And the claim 11 now has status "SETTLED"

  @major
  Scenario: Advancing a CLOSED settlement has no effect
    Given claim 5 has an existing settlement in status "CLOSED"
    When the agent posts to "/advanceSettlement" with id 5
    Then the settlement for claim 5 remains in status "CLOSED"
