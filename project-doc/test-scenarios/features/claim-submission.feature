@capability:claim-submission
Feature: Claim submission by a customer
  In order to start an insurance claim
  As a policyholder
  I want to submit a new claim against one of my active policies
  so that it enters the review pipeline

  # Seeded data:
  #   customer (James Miller) → policyholder id 1 → policy id 1 (POL-84521, MOTOR)
  #   meera (Emily Davis)     → policyholder id 2 → policy id 2 (POL-84522, HEALTH)

  Background:
    Given the user "customer" with password "Password@123" is logged in

  @critical @smoke
  Scenario: Customer submits a new motor claim
    When the customer posts to "/createClaim" with
      | policyId        | 1                                                |
      | claimSubtype    | Accident                                         |
      | incidentDate    | 2026-06-01                                       |
      | incidentLocation| I-95 Expressway                                  |
      | description     | Front-end collision at traffic signal.           |
      | estimatedLoss   | 85000                                            |
      | vehicleRegNo    | NY-AB-4321                                       |
      | mode            | submit                                           |
    Then the response redirects to a claim detail page
    And accessing that claim detail shows status "SUBMITTED"

  @major
  Scenario: Customer saves a claim as a draft
    When the customer posts to "/createClaim" with
      | policyId        | 1                                    |
      | description     | Minor side-mirror damage.            |
      | mode            | draft                                |
    Then the response redirects to a claim detail page
    And accessing that claim detail shows status "DRAFT"

  @major @validation
  Scenario: Submission is rejected when description is blank
    When the customer posts to "/createClaim" with
      | policyId        | 1  |
      | description     |    |
      | mode            | submit |
    Then the new claim form is re-displayed
    And the response body contains "Please describe the incident"

  @major @validation
  Scenario: Submission is rejected when no policy is selected
    When the customer posts to "/createClaim" with
      | policyId        | 0                                        |
      | description     | Some incident description.               |
      | mode            | submit                                   |
    Then the new claim form is re-displayed
    And the response body contains "Please select a policy"

  @major @validation
  Scenario: Submission is rejected when estimated loss is negative
    When the customer posts to "/createClaim" with
      | policyId        | 1                            |
      | description     | Some incident description.   |
      | estimatedLoss   | -500                         |
      | mode            | submit                       |
    Then the new claim form is re-displayed
    And the response body contains "cannot be negative"

  @major @validation
  Scenario: Submission is rejected when estimated loss is non-numeric
    When the customer posts to "/createClaim" with
      | policyId        | 1                            |
      | description     | Some incident description.   |
      | estimatedLoss   | abc                          |
      | mode            | submit                       |
    Then the new claim form is re-displayed
    And the response body contains "valid amount"

  @major @security
  Scenario: Customer cannot file a claim against another policyholder's policy
    When the customer posts to "/createClaim" with
      | policyId        | 2                                            |
      | description     | Trying to use another customer policy.       |
      | mode            | submit                                       |
    Then the new claim form is re-displayed
    And the response body contains "your own policies"
