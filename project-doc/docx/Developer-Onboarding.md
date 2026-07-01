# ICMS — Developer Onboarding Guide

## 1. What This Project Is (for engineers)

ICMS is a role-based insurance claims management web application: an
**Angular 22 (standalone components)** single-page app talking to a
**Node.js / Express REST API** backed by **MySQL** via **Knex**. It was
migrated from a legacy Struts 2 / JSP application (now removed from the
working tree, see `docs/MIGRATION_LESSONS.md` and `docs/MIGRATION_PROGRESS.md`)
onto the same, unchanged MySQL schema. The backend follows a strict layered
architecture (routes → controllers → services → repositories → db), uses
stateless JWT auth with server-side role checks, and treats money as
string-based decimals throughout to avoid floating-point error.

## 2. Tech Stack at a Glance

| Layer | Technology |
|---|---|
| Frontend | Angular 22 (standalone components), TypeScript, RxJS |
| Backend | Node.js (>=18), Express 4 |
| Database access | Knex 3 (query builder, no ORM), MySQL via `mysql2` |
| Database | MySQL (`icms` schema, `config/dbscript/schema.sql`) |
| Auth | JWT (`jsonwebtoken`), password hashing via `bcryptjs` |
| Validation | `express-validator` |
| Money/math | `decimal.js` (DECIMAL columns carried as strings) |
| File uploads | `multer` (memory storage, type/size checked) |
| Logging | `pino` / `pino-http` (structured, correlation-id aware) |
| Security headers | `helmet` |
| Testing | `jest` + `supertest` (backend), a bash smoke test, and a separate Cucumber.js BDD suite |
| Dev tooling | `nodemon` (backend dev reload), Angular CLI (`ng serve`/`ng build`) |
| Package manager | npm (both `backend/` and `frontend/` have their own `package.json`) |

## 3. Prerequisites

- **Node.js 18 or newer** (`backend/package.json` `engines.node: >=18`).
- **npm** (frontend `package.json` pins `packageManager: npm@11.12.1`).
- **A running MySQL server** with an `icms` database. The backend does not
  create the schema itself — it expects it to already exist.
- **Angular CLI** is used via `npx ng ...` (installed as a frontend
  devDependency: `@angular/cli`); no separate global install is required.
- No cloud accounts, API keys, or third-party service credentials were
  found anywhere in the backend or frontend code — the only required
  secret is a locally-generated JWT signing key (see below).

### Required environment variables (backend)

The backend reads all configuration from environment variables via
`backend/src/config/env.js`, following a `db.url` → `DB_URL` naming
convention (dots become underscores, uppercased). **Note:** the README and
`docs/MIGRATION_PROGRESS.md` both instruct developers to `cp .env.example
.env`, but no `.env.example` file exists in the current `backend/`
directory in this checkout — see Section 13. The variables below are
derived directly from `env.js`.

| Variable | Purpose | Default if unset |
|---|---|---|
| `JWT_SECRET` | Signs/verifies auth tokens. **Required** — startup fails without it (`assertConfig()` in `env.js`). | none (must be set) |
| `JWT_TTL` | Token lifetime | `8h` |
| `DB_URL` | JDBC-style connection string (`jdbc:mysql://host:port/db`), alternative to the discrete `DB_*` vars below | none |
| `DB_HOST` | MySQL host (used if `DB_URL` not set) | `127.0.0.1` |
| `DB_PORT` | MySQL port | `3306` |
| `DB_NAME` | Database name | `icms` |
| `DB_USER` | Database user | `root` |
| `DB_PASSWORD` | Database password | `` (empty) |
| `DB_POOL_MIN` | Knex/tarn pool minimum connections | `2` |
| `DB_POOL_MAX` | Knex/tarn pool maximum connections | `10` |
| `DB_POOL_CONNECTIONTIMEOUTMS` | Pool acquire timeout (ms) | `30000` |
| `NODE_ENV` (or `node.env`) | Environment name | `development` |
| `PORT` | HTTP port | `3000` |
| `HOST` | Bind address (`0.0.0.0` = all interfaces, reachable on LAN) | `0.0.0.0` |
| `LOG_LEVEL` | Pino log level | `info` |
| `ICMS_PAGE_SIZE` | Default page size for list endpoints | `15` |
| `ICMS_UPLOAD_DIR` | Absolute path where uploaded claim documents/reports are stored | `~/icms-uploads` |
| `CORS_ORIGINS` | Comma-separated allowed origins, or `*` to reflect any origin | `` (CORS disabled) |
| `STATIC_DIR` | If set, absolute path to the built Angular app; Express serves it same-origin alongside `/api/v1` | unset |

`env.js` refuses to boot in `production` if `JWT_SECRET` is left at the
literal placeholder string `change-me-in-every-environment` — generate a
real random value for every environment.

## 4. Local Setup — Step by Step

1. **Provision the database** (schema + seed data are committed and
   idempotent):
   ```bash
   mysql -uroot icms < config/dbscript/schema.sql
   mysql -uroot icms < config/dbscript/seed.sql
   # or restore a full snapshot instead:
   ./config/dbscript/restore-db.sh
   ```

2. **Configure and start the backend API:**
   ```bash
   cd backend
   cp .env.example .env   # NOTE: create this file yourself if missing — see Section 13
   # then edit .env: set JWT_SECRET to a real random value, and DB_* credentials
   npm install
   npm run dev             # nodemon, http://localhost:3000
   curl http://localhost:3000/api/v1/health
   ```

3. **Start the frontend SPA** (in a second terminal):
   ```bash
   cd frontend
   npm install
   npx ng serve             # http://localhost:4200
   ```
   `frontend/proxy.conf.json` forwards `/api` calls from the dev server to
   `http://localhost:3000`, so the SPA and API can be developed together
   without a CORS dance.

4. **Log in** with one of the seeded demo accounts (all share the password
   `Password@123`, per the root `README.md`): `admin`, `manager`, `agent`,
   `surveyor`, `customer`.

## 5. How to Run, Test, and Debug

- **Backend dev server:** `cd backend && npm run dev` (auto-reload via
  nodemon). **Production-style run:** `npm start` (plain `node
  src/server.js`).
- **Backend unit/integration tests:** `cd backend && npm test` (runs
  `jest --runInBand`; test files live under `backend/test/`).
- **Backend lint:** `cd backend && npm run lint` (ESLint over `backend/src`,
  config in `backend/.eslintrc.json`).
- **End-to-end smoke test** (drives the real API against a real database
  through the full claim lifecycle — submit → acknowledge → assign →
  assess → forward → approve → settle → SETTLED): `cd backend && npm run
  smoke` (runs `test/smoke/smoke-test.sh`). The README states this should
  report **13 passed, 0 failed**.
- **BDD/acceptance suite:** a separate Cucumber.js project lives at
  `project-doc/test-scenarios/` (its own `package.json`), with feature
  files under `project-doc/test-scenarios/features/` (e.g.
  `claim-submission.feature`, `approval-workflow.feature`,
  `settlement-processing.feature`) and step definitions under
  `project-doc/test-scenarios/steps/`. A pre-generated HTML report is
  checked in at `project-doc/test-result/index.html`. Treat this as a
  documented/reference test suite; confirm the exact run command inside
  `project-doc/test-scenarios/package.json` before relying on it in CI.
- **Frontend dev server:** `cd frontend && npx ng serve`. **Production
  build:** `npx ng build --configuration production` (outputs to
  `frontend/dist/frontend/browser`, matching the `STATIC_DIR` cutover path
  described in `docs/CUTOVER.md`). **Frontend tests:** `npm test` (Angular
  CLI test runner, configured with `vitest`/`jsdom` per
  `frontend/package.json` devDependencies).
- **Common gotchas:**
  - The API refuses to start at all if `JWT_SECRET` is missing
    (`assertConfig()` in `backend/src/config/env.js`), and the DB
    connection is verified at boot (`verifyConnection()` in
    `backend/src/db/knex.js`) — a backend that won't start almost always
    means a bad/missing DB or JWT config, not an application bug.
  - `GET /api/v1/health` checks both process liveness and DB reachability
    (`SELECT 1`) — use it first when debugging "is the backend even up."
  - Money fields (claim amounts, settlement amounts, assessment figures)
    are `DECIMAL(15,2)` columns returned by `mysql2` **as strings**
    (`decimalNumbers: false`, `bigNumberStrings: true` in `env.js`). Do not
    do plain JS arithmetic on them — always go through
    `backend/src/utils/money.js` (`dec`, `toAmount`, `add`, `sub`, `mul`).

## 6. Architecture Overview

The backend is a strict four-layer stack; every request flows the same
direction, and each layer only talks to the one directly below it.

```
                 Angular 22 SPA (frontend/)
                 lazy-loaded per-role feature
                 modules (customer/agent/...)
                          |
                          | HTTP (JSON), Bearer JWT
                          v
        +-----------------------------------------+
        |  Express app  (backend/src/app.js)       |
        |  helmet, cors, correlationId, pino-http  |
        +-----------------------------------------+
                          |
                          v
        routes/*.routes.js  (authJwt + roleGuard)
                          |
                          v
        controllers/*.controller.js   (thin; HTTP<->service glue)
                          |
                          v
        services/*Service.js   (business rules, transactions, audit)
                          |
                          v
        repositories/*Repo.js   (parameterized Knex queries only)
                          |
                          v
                 db/knex.js, db/tx.js
                          |
                          v
                     MySQL "icms" DB
```

Cross-cutting pieces that sit beside this stack:

```
domain/         pure business rules & constants, no I/O:
                claimStatus.js, approval.js, settlement.js
middleware/     authJwt, roleGuard, validate, correlationId,
                errorHandler (all wired in app.js / route files)
utils/          money.js, jwt.js, bcrypt.js, csv.js, fileUpload.js,
                paging.js, http.js (asyncH wrapper + ok() envelope)
```

Each of the five role portals (customer, agent, surveyor, manager, admin)
has its own route file, controller, and (mostly) its own service — they
share the `claims`, `approvals`, `settlements`, etc. repositories rather
than duplicating data access.

## 7. Codebase Map (directory tour)

| Path | Purpose |
|---|---|
| `backend/src/app.js` | Builds the Express app: security headers, CORS, correlation-id, logging, mounts `/api/v1`, optional SPA static serving |
| `backend/src/server.js` | Process entry point: validates config, verifies DB, starts HTTP server, graceful shutdown |
| `backend/src/config/env.js` | Centralized, environment-driven configuration (12-factor); the source of truth for every setting |
| `backend/src/routes/` | One file per role (`customer.routes.js`, `agent.routes.js`, `surveyor.routes.js`, `manager.routes.js`, `admin.routes.js`) plus `auth.routes.js` and `index.js` (mounts everything + `/health`) |
| `backend/src/controllers/` | Thin HTTP handlers; parse request, call a service, shape the response via `utils/http.js`'s `ok()` |
| `backend/src/services/` | Business logic, transactions (`withTransaction`), and audit-log writes; one service per bounded concern (claims, approvals, settlement, surveyor assessment, admin, manager, notifications, communications, documents, reports) |
| `backend/src/repositories/` | All raw Knex query building; the only layer allowed to touch table names/columns directly |
| `backend/src/domain/` | Pure, side-effect-free business rules: `claimStatus.js` (lifecycle/timeline), `approval.js` (decision constants), `settlement.js` (payment tracker) |
| `backend/src/middleware/` | `authJwt.js` (JWT verification), `roleGuard.js` (role authorization), `validate.js` (express-validator error mapping), `correlationId.js`, `errorHandler.js` |
| `backend/src/utils/` | `money.js` (decimal.js helpers), `jwt.js`, `bcrypt.js`, `csv.js`, `fileUpload.js` (multer config), `paging.js`, `errors.js` (typed `AppError` subclasses), `http.js` |
| `backend/src/db/knex.js` | Knex instance, connection pool, `verifyConnection()`, `poolStats()` |
| `backend/src/db/tx.js` | `withTransaction()` helper used by every service that writes data |
| `backend/test/smoke/smoke-test.sh` | End-to-end lifecycle smoke test against a live API + DB |
| `frontend/src/app/core/` | Auth (`core/auth/`), route guards (`core/guards/`), HTTP interceptors (`core/interceptors/`), shell layout (`core/layout/`) |
| `frontend/src/app/features/` | Lazy-loaded per-role feature modules: `customer/`, `agent/`, `surveyor/`, `manager/`, `admin/`, `auth/` |
| `frontend/src/app/shared/` | Shared models (`shared/models/`) and reusable UI components (`shared/components/`) |
| `frontend/proxy.conf.json` | Dev-server proxy that forwards `/api` to the backend on port 3000 |
| `config/dbscript/schema.sql` | Canonical DDL for the `icms` MySQL database |
| `config/dbscript/seed.sql` | Seed data (demo users, policies, config) |
| `config/dbscript/icms-dump.sql`, `dump-db.sh`, `restore-db.sh` | Full DB snapshot + dump/restore scripts |
| `docs/MIGRATION_PROGRESS.md` | Phase-by-phase record of the Struts→MEAN migration; useful history of *why* things are shaped this way |
| `docs/MIGRATION_LESSONS.md`, `docs/IMPLEMENTATION_PLAN.md`, `docs/CUTOVER.md` | Migration lessons learned, original implementation plan, and production cutover/deployment guide |
| `project-doc/test-scenarios/` | Standalone Cucumber.js BDD suite (`features/`, `steps/`) exercising the system's behavior scenario-by-scenario |
| `project-doc/test-result/index.html` | A pre-generated HTML report from the BDD suite |

## 8. Core Flows (sequence walk-throughs)

### 8.1 Authentication (login)

1. Client `POST /api/v1/auth/login` with `{ username, password }` →
   `backend/src/routes/auth.routes.js` → `controllers/auth.controller.js`.
2. Controller calls `authService.authenticate(username, password, ip)`
   (`backend/src/services/authService.js`).
3. `authenticate()` looks up the user via
   `userRepo.findByUsername(knex, username)`, requires `status === 'ACTIVE'`,
   and verifies the password with `passwordService.matches()` (bcrypt).
   On any failure it logs a generic `LOGIN_FAIL` audit row via
   `audit.recordSafe()` and returns `null` — the API never reveals whether
   the username or the password was wrong.
4. On success, it stamps `last_login` (`userRepo.updateLastLogin`, inside
   `withTransaction`), writes a `LOGIN` audit row, and signs a JWT with
   `jwtUtil.sign()` (`backend/src/utils/jwt.js`) carrying `id`, `username`,
   `role`, `fullName`, `email`.
5. Controller returns `{ data: { token, user }, correlationId }`
   (envelope built by `utils/http.js`'s `ok()`).
6. Every subsequent request carries `Authorization: Bearer <token>`;
   `middleware/authJwt.js` verifies it and sets `req.user`; each role
   router additionally applies `middleware/roleGuard.js` (e.g.
   `roleGuard('AGENT')`), which also always admits `role === 'ADMIN'`.

### 8.2 Claim submission → settlement (the full claims lifecycle)

This is the flow the backend's own smoke test
(`backend/test/smoke/smoke-test.sh`) exercises end to end.

1. **Customer submits a claim.** `POST /api/v1/customer/claims` →
   `customer.controller.js` → `claimService.createClaim()`
   (`backend/src/services/claimService.js`). Inside one transaction it
   validates the policy belongs to the caller, sets `status = SUBMITTED`,
   generates the claim number via `claimRepo.nextClaimNo()`, seeds required
   documents (`documentService.seedRequiredDocuments`), notifies the AGENT
   role (`notificationService.notifyRole`), posts a system message
   (`communicationService.system`), and writes a `CLAIM_SUBMITTED` audit
   row (`auditService.record`).
2. **Agent acknowledges.** `POST /api/v1/agent/claims/:id/acknowledge` →
   `agent.controller.js` → `agentClaimService.acknowledge()` — requires the
   claim to be `SUBMITTED`, moves it to `UNDER_REVIEW`
   (`claimRepo.acknowledge`), audits `CLAIM_ACKNOWLEDGED`.
3. **Agent assigns a surveyor.** `POST
   /api/v1/agent/claims/:id/assign-surveyor` →
   `assignmentService.assignSurveyor()` — validates the target user is an
   active `SURVEYOR`, moves the claim to `SURVEY_SCHEDULED`, notifies the
   surveyor.
4. **Surveyor submits an assessment.** `POST
   /api/v1/surveyor/claims/:id/assessment` →
   `surveyorService.submitAssessment()`
   (`backend/src/services/surveyorService.js`). This is the one place the
   payout is computed, and it is computed **server-side only**: gross cost
   is summed from component repair costs, then `net = gross − deductible −
   (gross × depreciation% ) − salvage`, floored at zero, all via
   `decimal.js` (`utils/money.js`). The claim moves to `UNDER_ASSESSMENT`
   and the handling agent is notified.
5. **Agent forwards for approval.** `POST /api/v1/agent/claims/:id/forward`
   → `agentClaimService.forwardForApproval()` — takes the assessed net
   payable (or estimated loss if none), calls
   `approvalService.createForwardChain()`
   (`backend/src/services/approvalService.js`), which looks up
   configured approval thresholds (`configRepo.approvalThresholds`) to
   decide whether an L2 and/or L3 approval is required, records L1 as
   already approved (the forwarding agent), and inserts pending L2/L3 rows
   as needed. The claim becomes `PENDING_APPROVAL` (or `APPROVED` directly
   if no higher level is required) and the MANAGER role is notified.
6. **Manager decides.** `POST /api/v1/manager/claims/:id/decision` →
   `approvalService.decide()` — records the decision on the next pending
   approval level and advances the claim status
   (`APPROVED`/`REJECTED`/`UNDER_REVIEW` for a return/`ON_HOLD`), notifying
   the agent and writing an `APPROVAL_<DECISION>` audit row.
7. **Settlement is authorized.** `POST
   /api/v1/agent/claims/:id/settlement` → `settlementService.authorize()`
   (`backend/src/services/settlementService.js`) — only allowed once the
   claim is `APPROVED`; inserts a `settlements` row with status
   `AUTHORIZED` and moves the claim to `SETTLEMENT_PROCESSING`.
8. **Settlement is advanced through its payment tracker.** `POST
   /api/v1/agent/claims/:id/settlement/advance` →
   `settlementService.advance()` steps the settlement through
   `AUTHORIZED → PAYMENT_INITIATED → BANK_PROCESSING → PAYMENT_CONFIRMED →
   CLAIMANT_NOTIFIED → CLOSED` (order defined in
   `backend/src/domain/settlement.js`). Reaching `PAYMENT_CONFIRMED` sets
   the claim to `SETTLED`; reaching `CLOSED` sets the claim to `CLOSED`.
   A manager can also call `managerService.overrideSettlement()`
   (`POST /api/v1/manager/claims/:id/settlement/override`) to change the
   final amount with a recorded justification.

Throughout this whole flow, every state-changing call runs inside
`withTransaction()` (`backend/src/db/tx.js`) and ends with an
`audit.record()` call, so the `audit_logs` table is a complete, ordered
history of the claim's life.

## 9. Data Model

Schema source of truth: `config/dbscript/schema.sql` (MySQL, InnoDB). Key
tables and relationships:

- **roles** (`id`, `name`) ← **users** (`id`, `full_name`, `email`,
  `username`, `password_hash`, `role_id` FK, `branch`, `status`,
  `last_login`). Roles: `CUSTOMER`, `AGENT`, `SURVEYOR`, `MANAGER`,
  `ADMIN`.
- **policyholders** (`id`, name/contact/address fields) ← **policies**
  (`id`, `policy_no`, `policyholder_id` FK, `type` enum
  MOTOR/HEALTH/PROPERTY/LIFE/TRAVEL/LIABILITY, `sum_insured`, `premium`,
  dates, `status`).
- **claims** (`id`, `claim_no`, `policy_id` FK, `policyholder_id` FK,
  `claimant_name`, `claim_type`, `claim_subtype`, incident details,
  `estimated_loss`, type-specific fields (`vehicle_reg_no`, `fir_number`,
  `hospital_name`, etc.), `status` enum (13 values — the lifecycle states
  plus `ON_HOLD`/`REJECTED`/`WITHDRAWN`), `agent_id` FK, `surveyor_id` FK,
  `risk_level`, `fraud_score`, `internal_notes`, `sla_due_date`). This is
  the central entity almost everything else hangs off of.
- **claim_documents** (per-claim required/uploaded documents, `upload_status`,
  `verification_status`) — FK to `claims`, cascade delete.
- **assessments** (one per claim, FK `claims`, cascade delete) holds the
  surveyor's visit details and the computed `gross_assessed`,
  `policy_deductible`, `depreciation_pct`/`depreciation_amt`,
  `salvage_value`, `net_payable`. **assessment_components** (FK
  `assessments`, cascade delete) holds the line-item cost breakdown used
  to compute `gross_assessed`.
- **approvals** (FK `claims`, cascade delete): one row per required
  approval level (`L1`/`L2`/`L3`), `approver_id` FK `users`, `decision`
  enum (`PENDING`/`APPROVED`/`CONDITIONAL`/`REJECTED`/`RETURNED`/`ON_HOLD`/`NA`).
- **settlements** (one per claim, unique FK `claims`, cascade delete):
  `final_amount`, payment details, `payment_method` enum, `status` enum
  matching the payment tracker in `domain/settlement.js`, `approved_by` FK
  `users`, and stage timestamps.
- **communications** (FK `claims`, cascade delete): message thread per
  claim, `sender_id` FK `users` (nullable — system messages have no
  sender), `channel` enum.
- **notifications**: in-app notifications, either to a specific `user_id`
  or broadcast to a `target_role`.
- **audit_logs**: append-only action log — `user_id`, `username`, `role`,
  `action`, `entity`, `ip_address`, `result` (`SUCCESS`/`FAILED`), `ts`.
- **Configuration tables** (admin-managed, no direct FK to claims):
  `approval_thresholds` (amount bands → required level),
  `sla_config` (stage → target hours), `document_requirements`
  (claim type/subtype → required document types), `notification_templates`
  (SMS/email template bodies, active flag).

## 10. Configuration Reference

See Section 3 for the full environment variable table (all consumed via
`backend/src/config/env.js`). In addition to environment variables, the
following are runtime, database-driven configuration managed through the
admin portal rather than env vars or files:

| Setting | Table | Managed via |
|---|---|---|
| Approval thresholds (which claim amount requires L2/L3 sign-off) | `approval_thresholds` | `adminService.updateThreshold` / `PUT /api/v1/admin/config/thresholds/:id` |
| SLA targets per workflow stage | `sla_config` | `adminService.updateSla` / `PUT /api/v1/admin/config/sla/:id` |
| Required documents per claim type/subtype | `document_requirements` | `adminService.addDocumentRequirement` / `deleteDocumentRequirement` |
| Notification template bodies (SMS/email) | `notification_templates` | `adminService.updateTemplate` / `PUT /api/v1/admin/config/templates/:id` |

## 11. Conventions & Gotchas

- **API response envelope is fixed and consistent everywhere:** success
  responses are `{ data, correlationId }` (built by `ok()` in
  `utils/http.js`); errors are `{ error: { message, fields? },
  correlationId }` (built by `errorHandler` in
  `middleware/errorHandler.js`); paginated lists are `{ items, page, size,
  total }` (built by `paged()` in `utils/paging.js`).
- **Controllers stay thin.** All controllers in `backend/src/controllers/`
  follow the same shape: parse `req`, call exactly one service function,
  wrap the result with `ok()`. Business rules belong in `services/`, not
  controllers — follow this pattern for new endpoints.
- **Repositories are the only layer that writes raw Knex/SQL.** Every
  query is parameterized through Knex's query builder (no string-concatenated
  SQL was found), which is the project's SQL-injection defense.
  `managerService.dashboardStats()` is a documented exception — it uses
  `knex.raw()` for four aggregate COUNT queries; keep any future raw SQL
  parameterized the same defensive way.
- **Custom typed errors drive HTTP status codes.** `utils/errors.js`
  defines an `AppError` base class and subclasses
  (`ValidationError`, `ConflictError`, `NotFoundError`, `UnauthorizedError`,
  `ForbiddenError`); throw these from services and `errorHandler` maps them
  to the right status automatically. Never leak a raw internal error
  message to the client — anything not an `AppError` becomes a generic 500.
- **Every state-changing service call is wrapped in `withTransaction()`**
  (`backend/src/db/tx.js`) and ends with an `audit.record()`/`writeAudit()`
  call. Follow this pattern for any new claim-mutating endpoint so the
  audit trail stays complete.
- **ADMIN is a superset role everywhere.** `middleware/roleGuard.js`
  always admits `role === 'ADMIN'` regardless of which role(s) a route
  requires — this is intentional, not a bug, per the README.
- **Money is never a JS `number` in backend logic.** DECIMAL columns come
  back from `mysql2` as strings (`decimalNumbers: false`,
  `bigNumberStrings: true`); always route arithmetic through
  `utils/money.js`'s `Decimal`/`dec`/`toAmount` helpers.
- **Ownership checks matter as much as role checks.** e.g.
  `claimService.getOwnedClaim()` and `surveyorService.getAssignedClaim()`
  return `null`/404 rather than another customer's or surveyor's claim data
  — role alone does not imply access to a specific record.
- **Customer-facing claim data is filtered.** `claimService.toCustomerClaim()`
  strips `internalNotes`, `fraudScore`, `riskLevel`, `agentId`,
  `surveyorId`, `slaDueDate` before a claim is ever sent to a customer.
- **File uploads are hardened.** `utils/fileUpload.js` uses multer
  memory storage with a size cap and file-extension allowlist, and stores
  files under `ICMS_UPLOAD_DIR` outside the served static root, using a
  server-generated filename rather than trusting the client's filename.
- **No database migration tool is used.** `db/knex.js` runs schema-less
  (per `docs/MIGRATION_PROGRESS.md`, Phase 0 notes: "no migrations, tarn
  pool stats"). Schema changes are made directly to
  `config/dbscript/schema.sql`, which is idempotent (drops and recreates).
  Coordinate schema changes carefully since there is no versioned migration
  history.

## 12. Glossary

- **Claim** — a policyholder's request for compensation for a covered
  loss; the central entity in the system (`claims` table).
- **Policy** — an insurance contract a policyholder holds; a claim is
  always filed against one specific policy.
- **Assessment** — the surveyor's recorded inspection findings and cost
  breakdown for a claim, from which `net_payable` is calculated.
- **Net payable** — the final calculated compensation amount for an
  assessed claim: gross assessed cost minus deductible, depreciation, and
  salvage value (never below zero).
- **Approval chain / L1, L2, L3** — the sequence of sign-offs a claim
  needs before it can be settled. L1 is the forwarding agent (auto-approved
  on forward); L2 (Manager) and/or L3 (Director) become required based on
  configured amount thresholds.
- **Settlement** — the financial payout record for an approved claim,
  tracked through a payment lifecycle from `AUTHORIZED` to `CLOSED`.
- **SLA (Service Level Agreement) config** — admin-configured target
  turnaround hours per workflow stage, used to flag SLA breaches on the
  manager dashboard.
- **Correlation ID** — a per-request identifier
  (`middleware/correlationId.js`) attached to every log line and returned
  to the client, used to trace a single request across logs.
- **Worklist** — the agent dashboard's prioritized list of claims needing
  attention (`agentClaimService.worklist()`).
- **Audit log** — the permanent, append-only record of every significant
  action taken in the system (`audit_logs` table, written via
  `services/auditService.js`).

## 13. Assumptions & Open Questions

- **`.env.example` is referenced but not present in this checkout.** Both
  the root `README.md` and `backend/README.md` instruct developers to run
  `cp .env.example .env`, and `docs/MIGRATION_PROGRESS.md` implies one was
  created locally and gitignored — but no such file exists in
  `backend/` as currently checked out. A new developer must construct
  `backend/.env` manually from the environment variable table in Section 3
  of this guide (at minimum `JWT_SECRET` and DB credentials) until this
  file is restored or recreated.
- **The exact command to run the Cucumber BDD suite** under
  `project-doc/test-scenarios/` was not independently verified here beyond
  reading its `package.json`/directory layout — confirm the run script
  there before relying on it for CI.
- **Production deployment target** (containerization, orchestration,
  hosting) is not defined anywhere in the repository — no Dockerfile,
  compose file, or CI/CD pipeline was found. `docs/CUTOVER.md` describes a
  same-origin single-process deployment (Express serving the built SPA via
  `STATIC_DIR`), but the actual hosting environment (VM, container
  platform, cloud provider) is not specified in the code.
- **No automated CI pipeline was found** in the repository (no
  `.github/workflows`, no other CI config detected) — test and lint
  commands above must currently be run manually or wired into CI
  separately.
- The demo/seed users and password (`Password@123`) are for local
  development only, per the README; confirm this is never reused for a
  real environment.

---
*Generated from source-code analysis on 2026-07-01. Verify version
numbers, secrets, and run commands against your actual environment before
relying on them.*
