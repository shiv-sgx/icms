@capability:manager-settlement-override
Feature: Manager settlement amount override
  In order to correct an incorrectly authorised settlement amount
  As a manager
  I want to override the final settlement amount on an existing settlement

  # ManagerService.overrideSettlement requires:
  #   - a settlement row already exists for the claim
  #   - amount must be > 0
  # Seeded settled claims: 5 (settlement CLOSED), 7 (settlement CLOSED).

  Background:
    Given the user "manager" with password "Password@123" is logged in

  @critical
  Scenario: Manager overrides the settlement amount for an existing settlement
    Given claim 5 has an existing settlement
    When the manager posts to "/overrideSettlement" with
      | claimId       | 5       |
      | amount        | 395000  |
      | justification | Corrected per final inspection. |
    Then the response redirects to the claim detail

  @major @validation
  Scenario: Override is rejected when the amount is zero
    Given claim 5 has an existing settlement
    When the manager posts to "/overrideSettlement" with
      | claimId       | 5 |
      | amount        | 0 |
      | justification | Override test. |
    Then the manager sees an error about invalid override amount

  @major @validation
  Scenario: Override is rejected when no settlement exists for the claim
    Given claim 9 has no settlement
    When the manager posts to "/overrideSettlement" with
      | claimId       | 9      |
      | amount        | 50000  |
      | justification | Override test. |
    Then the manager sees an error about no settlement to override
