@capability:role-access-control
Feature: Role-based access control
  In order to protect each portal from unauthorised access
  As the ICMS security policy
  Role namespaces must only be accessible to users whose role matches that namespace;
  ADMIN is permitted everywhere; any other role receives the "denied" response.

  # The RoleInterceptor enforces this server-side on every request by matching the
  # URL namespace prefix (/customer, /agent, /surveyor, /manager, /admin) to the
  # authenticated user's role (Roles.forNamespace).

  @critical @security
  Scenario: Customer cannot access the agent portal
    Given the user "customer" with password "Password@123" is logged in
    When the user requests the secured page "/agent/claims"
    Then the access is denied

  @critical @security
  Scenario: Customer cannot access the manager portal
    Given the user "customer" with password "Password@123" is logged in
    When the user requests the secured page "/manager/approvals"
    Then the access is denied

  @critical @security
  Scenario: Customer cannot access the admin portal
    Given the user "customer" with password "Password@123" is logged in
    When the user requests the secured page "/admin/dashboard"
    Then the access is denied

  @critical @security
  Scenario: Agent cannot access the customer portal
    Given the user "agent" with password "Password@123" is logged in
    When the user requests the secured page "/customer/dashboard"
    Then the access is denied

  @critical @security
  Scenario: Surveyor cannot access the manager portal
    Given the user "surveyor" with password "Password@123" is logged in
    When the user requests the secured page "/manager/approvals"
    Then the access is denied

  @critical @security
  Scenario: Manager cannot access the admin portal
    Given the user "manager" with password "Password@123" is logged in
    When the user requests the secured page "/admin/dashboard"
    Then the access is denied

  @critical @security
  Scenario: Admin can access the agent portal
    Given the user "admin" with password "Password@123" is logged in
    When the user requests the secured page "/agent/claims"
    Then the request is permitted

  @critical @security
  Scenario: Admin can access the manager portal
    Given the user "admin" with password "Password@123" is logged in
    When the user requests the secured page "/manager/approvals"
    Then the request is permitted

  @critical @security
  Scenario: Admin can access the customer portal
    Given the user "admin" with password "Password@123" is logged in
    When the user requests the secured page "/customer/dashboard"
    Then the request is permitted
