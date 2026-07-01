# ICMS (Insurance Claim Management System) — Business Overview

## Executive Summary

ICMS is the internal system an insurance company uses to run its claims
process end to end — from a policyholder reporting a loss, through review,
field survey, financial approval, and final payment. It replaces a set of
paper- and email-driven handoffs with a single system where every claim has
one authoritative status, every decision is recorded, and every person
involved — the customer, the claims agent, the surveyor, the manager, and
the administrator — works from the same live record. The system was
recently rebuilt on a modern web stack after starting life as an older
Java-based application; the rebuild is complete and the old application has
been retired.

## The Problem It Solves

Before a system like this, claims handling typically depends on spreadsheets,
shared inboxes, and phone calls to track where a claim stands, who owns the
next step, and whether documents and approvals are in order. That creates
real business risk: claims can stall with no one accountable, approval
limits can be bypassed, settlement amounts can be miscalculated or
disputed, and there is no reliable record of who approved what and when.
ICMS removes that ambiguity by giving every claim a single defined
lifecycle, enforcing who is allowed to take each action, and keeping a
permanent audit trail of every state change.

## What the System Does

- **Claim intake and self-service tracking.** Policyholders submit a claim
  online against one of their own policies, attach supporting documents,
  message the handling team, follow a visual status timeline, and withdraw
  a claim that is still in progress.
- **Structured claims handling workflow.** Each claim moves through a fixed
  sequence of stages — submitted, under review, survey scheduled, under
  assessment, pending approval, approved, settlement in progress, settled,
  closed — with defined side-states for on-hold, rejected, and withdrawn
  claims, so status always means the same thing to everyone.
- **Field assessment with an auditable payout calculation.** A surveyor
  records a damage/loss breakdown; the system — not the surveyor's own
  arithmetic — calculates the net payable amount from the assessed cost,
  policy deductible, depreciation, and salvage value, removing a common
  source of manual error and dispute.
- **Risk-based, multi-level approval.** Claims above configurable value
  thresholds automatically require a second (and, for larger amounts, a
  third) level of sign-off before money can move, so large payouts always
  get extra scrutiny without slowing down small, routine ones.
- **Controlled settlement and payment tracking.** Once approved, a
  settlement is authorized with payment details and then tracked through a
  defined payment lifecycle (authorized, initiated, with the bank,
  confirmed, claimant notified, closed), and a manager can override the
  settled amount with a recorded justification when needed.
- **Administration and oversight.** Administrators manage user accounts and
  roles, configure service-level targets, approval thresholds, required
  documents per claim type, and notification templates, and can review a
  complete, exportable audit log of every action taken in the system.
- **Reporting for management.** Managers get analytics and exportable
  reports to monitor claim volumes, turnaround, and agent performance,
  supporting oversight without manual data collection.

## Who Uses It

- **Customers (policyholders)** — file and track their own claims, upload
  documents, communicate with the claims team, and withdraw a claim if
  circumstances change.
- **Agents** — the front-line claims handlers: they acknowledge new claims,
  assign a surveyor, review findings, forward claims for approval, process
  settlements, and communicate with customers.
- **Surveyors** — field assessors who inspect the loss, record a
  component-by-component cost breakdown, and produce the assessment that
  drives the payout calculation.
- **Managers** — approve or reject claims at the higher sign-off levels,
  can override a settlement amount, and use reporting/analytics to oversee
  the claims operation.
- **Administrators** — manage who has access to the system, configure the
  business rules (approval thresholds, SLA targets, required documents),
  and audit everything that happens in it.

An "admin" account is, by design, able to act across every one of these
areas — a deliberate super-user capability for operational support and
oversight.

## How It Creates Value

- **Faster, more predictable claims turnaround.** A single defined workflow
  with visible status and configurable service-level targets reduces the
  time claims spend waiting for "whoever has it next."
- **Reduced financial risk.** Payout amounts are calculated by the system
  from surveyor inputs (not self-reported), and claims above set thresholds
  are automatically routed through the required extra approval levels —
  reducing both calculation errors and unauthorized payouts.
- **Full accountability.** Every significant action (claim created,
  acknowledged, forwarded, approved, settled, user changes, configuration
  changes) is written to a permanent audit trail tied to the person who
  performed it, supporting internal controls and regulatory/audit response.
- **Lower operational overhead.** Customers can self-serve status checks
  and document uploads instead of calling in, freeing agent time for
  claims that actually need attention.
- **Management visibility.** Built-in reporting gives managers a live view
  of claim volume, risk, SLA breaches, and settlement activity without
  manual reporting effort.

## Key Integrations & Dependencies

- **Company database of policies and policyholders.** ICMS reads and writes
  against the company's existing claims database (policies, policyholders,
  users), and the recent platform rebuild deliberately kept this database
  unchanged so no data migration was required.
- **Document storage.** Uploaded claim documents (photos, reports, proofs)
  are stored on the server the application runs on, outside the publicly
  served web content, not in a third-party cloud storage service (based on
  current code; see Assumptions).
- **No payment gateway or SMS/email delivery integration found in the code.**
  Settlement "payment methods" (bank transfer, cheque, demand draft,
  direct-to-workshop) and notification "channels" (SMS, email) are recorded
  and tracked as data/status in the system, but the actual transmission of
  money or of SMS/email messages to a bank or telecom/email provider is not
  present in the codebase — see Current Scope & Limitations.

## Current Scope & Limitations

- Fraud/risk scoring fields exist on a claim (a risk level and a numeric
  fraud score), but the code sets these to fixed default values at claim
  creation — there is no live fraud-detection or risk-scoring engine
  currently wired in.
- Notification "templates" (SMS/email) are configurable by administrators,
  but the system does not appear to actually dispatch SMS or email through
  an external provider; notifications observed in the code are in-app
  (recorded for a user or role and shown inside the product).
- Settlement payment methods are recorded, but there is no integration to
  an external banking/payment system — advancing a payment through its
  tracked stages (initiated, bank processing, confirmed) is a manual,
  in-app action taken by a user, not an automated funds transfer.
- The system currently supports six roles/access levels (Customer, Agent,
  Surveyor, Manager, Admin) and six insurance product lines (Motor, Health,
  Property, Life, Travel, Liability) as defined in the data; expanding
  either requires configuration and, in some cases, code changes.
- This is presently a single-organization, internal operational system —
  there is no evidence in the code of multi-company/multi-tenant support.

## Assumptions & Open Questions

- The specific insurance company, market, and regulatory jurisdiction this
  system serves are not stated anywhere in the repository; the above is
  written generically based on the workflow the code implements.
- It is assumed this is used internally by one insurer's own staff plus its
  own policyholders, rather than being resold as a multi-tenant product —
  this should be confirmed with the business owner.
- The commercial or operational reason for the recent platform rebuild
  (from the older technology to the current one) is documented in the
  project's technical migration notes as a technical modernization effort;
  no separate business case document was found in the repository.
- Whether there are plans to integrate real payment execution or real
  SMS/email delivery in the future is not addressed in the code or docs
  and should be confirmed with the product owner.

---
*Generated from source-code analysis on 2026-07-01. Business intent is
partly inferred from the implementation and should be confirmed with the
product owner.*
