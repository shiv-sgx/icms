@capability:authentication
Feature: Authentication — login and logout
  In order to access the Insurance Claims Management System
  As a registered user with any role
  I want to authenticate with my credentials and be routed to my role portal

  # ------------------------------------------------------------------
  # Seed data: demo users from seed.sql.
  #   admin/admin@icms.local  → /admin/dashboard
  #   manager               → /manager/dashboard
  #   agent                 → /agent/dashboard
  #   surveyor              → /surveyor/dashboard
  #   customer (username: customer, email james.miller@example.com) → /customer/dashboard
  # All share the demo password injected by setup.sh.
  # ------------------------------------------------------------------

  @critical @smoke
  Scenario: Customer logs in with valid credentials and is redirected to the customer dashboard
    Given the application is running
    When a user submits username "customer" and password "Password@123"
    Then the response redirects to "/customer/dashboard"

  @critical @smoke
  Scenario: Agent logs in with valid credentials and is redirected to the agent dashboard
    Given the application is running
    When a user submits username "agent" and password "Password@123"
    Then the response redirects to "/agent/dashboard"

  @critical @smoke
  Scenario: Manager logs in with valid credentials and is redirected to the manager dashboard
    Given the application is running
    When a user submits username "manager" and password "Password@123"
    Then the response redirects to "/manager/dashboard"

  @critical @smoke
  Scenario: Surveyor logs in with valid credentials and is redirected to the surveyor dashboard
    Given the application is running
    When a user submits username "surveyor" and password "Password@123"
    Then the response redirects to "/surveyor/dashboard"

  @critical @smoke
  Scenario: Admin logs in with valid credentials and is redirected to the admin dashboard
    Given the application is running
    When a user submits username "admin" and password "Password@123"
    Then the response redirects to "/admin/dashboard"

  @critical @security
  Scenario: Login fails for wrong password and does not enumerate users
    Given the application is running
    When a user submits username "customer" and password "wrong-password"
    Then the login form is re-displayed
    And the response body contains "Invalid username or password"

  @critical @security
  Scenario: Login fails for non-existent username with the same message as a wrong password
    Given the application is running
    When a user submits username "no_such_user" and password "anything"
    Then the login form is re-displayed
    And the response body contains "Invalid username or password"

  @major @validation
  Scenario Outline: Login is rejected when required fields are blank
    Given the application is running
    When a user submits username "<username>" and password "<password>"
    Then the login form is re-displayed

    Examples:
      | username | password |
      |          | Password@123 |
      | customer |           |
      |          |            |

  @critical @security
  Scenario: Authenticated user can log out and session is invalidated
    Given the user "customer" with password "Password@123" is logged in
    When the user requests the logout URL "/logout"
    Then the response redirects to "/login"
    And accessing a secured page "/customer/dashboard" redirects to "/login"

  @critical @security
  Scenario: Accessing a secured page without a session redirects to login
    Given the application is running
    When an unauthenticated request is made to "/customer/dashboard"
    Then the response redirects to "/login"

  @critical @security
  Scenario: Inactive user account cannot log in
    Given the application is running
    When a user submits username "inactive_user" and password "Password@123"
    Then the login form is re-displayed
    And the response body contains "Invalid username or password"
