@capability:surveyor-assessment
Feature: Surveyor damage assessment
  In order to provide an independent damage valuation
  As a surveyor assigned to a claim
  I want to submit a component-level damage assessment so that the net payable
  amount is computed server-side and the agent is notified

  # SurveyorService.submitAssessment computes:
  #   deprAmt = gross * depreciationPct / 100
  #   net     = gross - deductible - deprAmt - salvage  (floor 0)
  # Seeded: surveyor (username=surveyor, id 5) is assigned to claim 1 and 8.

  Background:
    Given the user "surveyor" with password "Password@123" is logged in

  @critical @smoke
  Scenario: Surveyor views their dashboard
    When the surveyor requests "/surveyor/dashboard"
    Then the response is successful

  @critical @smoke
  Scenario: Surveyor views the assessment form for an assigned claim
    When the surveyor requests "/assess?id=8"
    Then the response is successful
    And the response body contains "assessment"

  @critical
  Scenario: Surveyor submits a damage assessment with component breakdown
    Given claim 8 is assigned to the logged-in surveyor and has no prior assessment
    When the surveyor posts to "/submitAssessment" with
      | id                | 8                                         |
      | visitDate         | 2026-06-05                                |
      | siteObservations  | Structural damage to east wall observed.  |
      | reportRefNo       | SRV-2026-TEST                             |
      | policyDeductible  | 10000                                     |
      | depreciationPct   | 10                                        |
      | salvageValue      | 5000                                      |
      | compName[0]       | East Wall                                 |
      | compSeverity[0]   | SEVERE                                    |
      | compCost[0]       | 150000                                    |
      | recommendation    | PARTIAL_APPROVE                           |
    Then the response redirects to the assessment page for claim 8
    And the claim 8 now has status "UNDER_ASSESSMENT"

  @major @validation
  Scenario: Assessment is rejected when no component costs or gross amount are provided
    Given claim 8 is assigned to the logged-in surveyor and has no prior assessment
    When the surveyor posts to "/submitAssessment" with
      | id                | 8   |
      | policyDeductible  | 0   |
      | depreciationPct   | 0   |
      | salvageValue      | 0   |
    Then the assessment fails with a message about providing component costs

  @major @security
  Scenario: Surveyor cannot submit an assessment for a claim not assigned to them
    Given claim 2 is not assigned to the logged-in surveyor
    When the surveyor posts to "/submitAssessment" for claim 2 with
      | visitDate         | 2026-06-05   |
      | compName[0]       | Component A  |
      | compCost[0]       | 50000        |
    Then the assessment fails with a message about assignment

  @major @validation
  Scenario: Surveyor cannot submit a second assessment when one is already submitted
    Given claim 1 already has a submitted assessment
    When the surveyor posts to "/submitAssessment" for claim 1 with
      | compName[0]   | Engine Mount |
      | compCost[0]   | 20000        |
    Then the assessment fails with a message about duplicate assessment

  @major
  Scenario: Surveyor uploads a survey report document for an assigned claim
    Given claim 8 is assigned to the logged-in surveyor
    When the surveyor uploads file "survey-report.pdf" of type "Survey Report" for claim 8
    Then the upload redirects to the assessment page for claim 8
