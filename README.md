# ICMS — Insurance Claim Management System

A role-based insurance claim management web application built on the **MEAN-style**
stack: **Angular 22 (standalone)** SPA, **Node.js / Express** REST API, **Knex**
over **MySQL**, with **JWT** authentication and **BCrypt** password hashing.

> Migrated from a legacy Struts 2 / JSP application. The old stack has been removed
> from the working tree (it remains in git history); the database schema is unchanged.
> See `docs/MIGRATION_PROGRESS.md`, `docs/MIGRATION_LESSONS.md`, and `docs/CUTOVER.md`.

It implements the full claim lifecycle across five role portals.

## Roles & portals

| Role | Capabilities |
|------|-------------|
| **Customer** | Submit/track claims (multi-type form, draft/submit), status timeline, document upload, messaging, withdraw, profile, FAQ |
| **Agent** | Worklist dashboard, claims list (filter/search/paginate), acknowledge, assign surveyor, review assessment, forward for approval, settlement processing, communications |
| **Surveyor** | Assigned claims, assessment (component breakdown → net-payable calculation), survey report/photo upload |
| **Manager** | Approval queue, multi-level approval decisions (approve/reject/return/hold), settlement override, Reports & Analytics with CSV export |
| **Admin** | User & role management, claim/SLA/threshold configuration, notification templates, audit log viewer + CSV export, system monitoring |

## Claim lifecycle

```
DRAFT → SUBMITTED → UNDER_REVIEW → SURVEY_SCHEDULED → UNDER_ASSESSMENT
      → PENDING_APPROVAL → APPROVED → SETTLEMENT_PROCESSING → SETTLED → CLOSED
(side states: REJECTED, WITHDRAWN, ON_HOLD)
```

## Architecture

```
backend/          Node/Express REST API
  routes → controllers (thin) → services (business logic + tx + audit)
         → repositories (parameterized Knex) → db/tx
frontend/         Angular 22 standalone SPA (lazy per-role feature modules)
config/dbscript/  Canonical DB schema.sql + seed.sql, mysqldump snapshot, dump/restore scripts
docs/             Migration progress, lessons, implementation plan, cutover guide
```

- **API envelope** — success `{ data, correlationId }`; error `{ error: { message, fields? }, correlationId }`; lists `{ items, page, size, total }`.
- **Money** (`DECIMAL(15,2)`) is carried as **strings** end-to-end (`decimal.js` for math).
- **Auth** — stateless JWT; `ADMIN` passes every role guard. Server-side role authorization on every secured route.

## Setup & run (dev)

Prerequisites: **Node 18+** and a running **MySQL** with the `icms` database.

```bash
# 1. Provision the database (canonical DDL + seed)
mysql -uroot icms < config/dbscript/schema.sql
mysql -uroot icms < config/dbscript/seed.sql
#    …or restore the snapshot:  ./config/dbscript/restore-db.sh

# 2. Backend API (http://localhost:3000)
cd backend
cp .env.example .env        # set JWT_SECRET (real random value) + DB creds
npm install && npm run dev  # http://localhost:3000/api/v1/health

# 3. Frontend SPA (http://localhost:4200, proxies /api → :3000)
cd ../frontend
npm install && npx ng serve
```

### Seeded logins (all share the demo password `Password@123`)

| Username | Role |
|----------|------|
| `admin` | ADMIN |
| `manager` | MANAGER |
| `agent` | AGENT |
| `surveyor` | SURVEYOR |
| `customer` | CUSTOMER |

## Production (single same-origin process)

Build the SPA and let Express serve it alongside the API — see **`docs/CUTOVER.md`**
for the full topology, environment variables, and deployment steps.

```bash
cd frontend && npm ci && npx ng build --configuration production
cd ../backend && npm ci
export STATIC_DIR="$(cd ../frontend/dist/frontend/browser && pwd)"
npm start                   # SPA + /api/v1 on http://localhost:3000
```

## Verification

```bash
cd backend && npm run smoke   # drives the full lifecycle against the live DB
```

Customer submit → agent ack/assign → surveyor assess → forward → manager approve →
settle → **SETTLED**, asserting every transition. Expect **13 passed, 0 failed**.

## Security

- Stateless JWT auth; **role authorization enforced server-side** on every secured route (`ADMIN` is a superset).
- All SQL is parameterized via Knex (injection-safe). BCrypt password hashing (cost 10).
- Inputs validated server-side (`express-validator`); uploads are type/size-checked and stored outside the web root; the client filename is never trusted for the stored path.
- Helmet security headers; per-request `X-Correlation-Id` for tracing; every state change is written to `audit_logs`. Internal details are never leaked in error responses.
</content>
</invoke>
