@capability:agent-claim-management
Feature: Agent claim management
  In order to process incoming insurance claims
  As an agent
  I want to view my claim worklist, acknowledge new claims, assign surveyors,
  add internal notes, and forward claims for approval

  # Seeded claims in SUBMITTED status: CLM-2024-0845 (id 10), no agent assigned.
  # Seeded UNDER_REVIEW claim: CLM-2024-0850 (id 9), assigned to agent (id 4 = sunita).
  # Agent login: username=agent (Jennifer Martinez, id 3).

  Background:
    Given the user "agent" with password "Password@123" is logged in

  @critical @smoke
  Scenario: Agent views the claims list
    When the agent requests "/agent/claims"
    Then the response is successful
    And the response body contains "Claims"

  @critical @smoke
  Scenario: Agent views the agent dashboard
    When the agent requests "/agent/dashboard"
    Then the response is successful

  @critical
  Scenario: Agent acknowledges a SUBMITTED claim, moving it to UNDER_REVIEW
    Given claim 10 has status "SUBMITTED"
    When the agent posts to "/acknowledge" with claim id 10
    Then the response redirects to the claim detail
    And the claim 10 now has status "UNDER_REVIEW"

  @major @validation
  Scenario: Agent cannot acknowledge a claim that is not in SUBMITTED status
    Given claim 1 has status "PENDING_APPROVAL"
    When the agent posts to "/acknowledge" with claim id 1
    Then the system returns an error about claim state

  @critical
  Scenario: Agent assigns a surveyor to a claim, moving it to SURVEY_SCHEDULED
    Given claim 9 has status "UNDER_REVIEW"
    When the agent posts to "/assignSurveyor" with claim id 9 and surveyor id 5
    Then the response redirects to the claim detail
    And the claim 9 now has status "SURVEY_SCHEDULED"

  @major @validation
  Scenario: Agent cannot assign a non-existent user as surveyor
    Given claim 9 has status "UNDER_REVIEW"
    When the agent posts to "/assignSurveyor" with claim id 9 and surveyor id 999
    Then the system returns an error about invalid surveyor

  @major @validation
  Scenario: Agent cannot assign a surveyor to a terminal claim
    Given claim 5 has status "SETTLED"
    When the agent posts to "/assignSurveyor" with claim id 5 and surveyor id 5
    Then the system returns an error about claim state

  @major
  Scenario: Agent saves an internal note on a claim
    When the agent posts to "/saveNote" with claim id 9 and note "Follow up with surveyor by Monday."
    Then the response redirects to the claim detail

  @critical
  Scenario: Agent forwards a claim for approval, building the approval chain
    Given claim 9 has status "UNDER_REVIEW"
    When the agent posts to "/forward" with claim id 9
    Then the response redirects to the claim detail
    And the claim 9 now has status "PENDING_APPROVAL" or "APPROVED"

  @major @validation
  Scenario: Agent cannot forward a claim that is already awaiting approval
    Given claim 1 has status "PENDING_APPROVAL"
    When the agent posts to "/forward" with claim id 1
    Then the system returns an error about claim state

  @major
  Scenario: Agent posts a message on a claim
    When the agent posts to "/message" on claim 9 with content "Surveyor to visit on Monday."
    Then the response redirects to the claim detail
