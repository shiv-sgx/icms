@capability:admin-user-management
Feature: Admin user management
  In order to manage system access
  As an admin
  I want to create, update, and reset passwords for system users

  # AdminService.createUser enforces:
  #   - fullName, email, username: required
  #   - rawPassword: minimum 6 characters
  #   - username/email must be unique
  # AdminService.resetPassword enforces: minimum 6 characters.

  Background:
    Given the user "admin" with password "Password@123" is logged in

  @critical @smoke
  Scenario: Admin views the user management page
    When the admin requests "/admin/users"
    Then the response is successful
    And the response body contains "username"

  @critical
  Scenario: Admin creates a new user with valid details
    When the admin posts to "/createUser" with
      | full_name  | Test New Agent         |
      | email      | testagent@icms.test    |
      | username   | testagent              |
      | password   | Secret99               |
      | role_id    | 2                      |
      | status     | ACTIVE                 |
    Then the response redirects to the users list
    And the user "testagent" appears in the users list

  @major @validation
  Scenario: User creation is rejected when username is blank
    When the admin posts to "/createUser" with
      | full_name  | Test User           |
      | email      | nouser@icms.test    |
      | username   |                     |
      | password   | Secret99            |
      | role_id    | 2                   |
      | status     | ACTIVE              |
    Then the user creation fails with a message about required fields

  @major @validation
  Scenario: User creation is rejected when email is blank
    When the admin posts to "/createUser" with
      | full_name  | Test User  |
      | email      |            |
      | username   | nouser2    |
      | password   | Secret99   |
      | role_id    | 2          |
      | status     | ACTIVE     |
    Then the user creation fails with a message about required fields

  @major @validation
  Scenario: User creation is rejected when password is fewer than 6 characters
    When the admin posts to "/createUser" with
      | full_name  | Test User3          |
      | email      | user3@icms.test     |
      | username   | user3               |
      | password   | abc                 |
      | role_id    | 2                   |
      | status     | ACTIVE              |
    Then the user creation fails with a message about password length

  @major @validation
  Scenario: User creation is rejected when username or email already exists
    When the admin posts to "/createUser" with
      | full_name  | Duplicate Agent       |
      | email      | jennifer.martinez@icms.local |
      | username   | agent                 |
      | password   | Secret99              |
      | role_id    | 2                     |
      | status     | ACTIVE                |
    Then the user creation fails with a message about duplicate user

  @major
  Scenario: Admin updates a user's status and role
    When the admin posts to "/updateUser" with user id 8, status "INACTIVE", and role_id 1
    Then the response redirects to the users list

  @major @mutates-password
  Scenario: Admin resets a user's password
    When the admin posts to "/resetPassword" with user id 7 and new password "NewPass99"
    Then the response redirects to the users list
    And the user "customer" can log in with password "NewPass99"

  @major @validation
  Scenario: Password reset is rejected when new password is fewer than 6 characters
    When the admin posts to "/resetPassword" with user id 7 and new password "abc"
    Then the password reset fails with a message about password length
