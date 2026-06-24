# ICMS MEAN Migration — Progress Tracker

Branch: **MEAN** · Approved plan: `~/.claude/plans/partitioned-enchanting-sunbeam.md`
(also summarized in this repo's history). Stack: **Angular 22 + Node/Express + Knex + same MySQL `icms` DB**.
Auth: **JWT** (BCrypt verified compatible). UI: **reuse `icms.css`**. DB: **mysql2 + Knex, no schema change**.

This file is the crash-safe source of truth for what's done and what's next.
Update it as phases complete.

## How to run (dev)
```bash
# 1. MySQL must be running with the existing `icms` DB (schema.sql + seed.sql).
# 2. Backend
cd backend && cp -n .env.example .env   # set JWT_SECRET + DB creds (local .env already created, gitignored)
npm install && npm run dev              # http://localhost:3000/api/v1/health
# 3. Frontend
cd frontend && npm install
npx ng serve                            # http://localhost:4200 (proxies /api -> :3000)
```
Demo users (all password `Password@123`): admin, manager, agent, sunita, surveyor, vinod, customer, meera.

## Status

| Phase | Scope | Status |
|------|-------|--------|
| 0 | Scaffold backend + frontend | ✅ DONE (committed) |
| 1 | Auth + layout (JWT, shell, login) | ✅ DONE (committed) |
| 2 | Customer portal | ✅ DONE (committed) |
| 3 | Agent + Surveyor portals | ✅ DONE (committed) |
| 4 | Manager + Admin + reports | ⬜ pending |
| 5 | Uploads + CSV exports | ⬜ pending |
| 6 | Parity verification + cutover | ⬜ pending |

### Phase 0 — DONE
- `/backend`: Express+Knex+pino; `env.js` (mirrors AppConfig key convention), `knex.js` (no migrations, tarn pool stats), `tx.js` (= Db.inTransaction), correlation-id + helmet + cors + central error handler, `GET /api/v1/health` (verified against live DB).
- `/frontend`: Angular 22 standalone; reuses `icms.css` verbatim; dev proxy `/api`→:3000; landing verifies design + proxy. Builds clean.
- Root `.gitignore` updated (node_modules/dist/.angular). Verified end-to-end (ng serve → Node → MySQL).

### Phase 1 — DONE
**Backend (DONE + tested):**
- `utils/bcrypt.js` (jbcrypt-compatible — verified `$2a$10$` hashes authenticate), `utils/jwt.js`, `utils/errors.js`, `utils/http.js`.
- `repositories/`: `userRepo.js` (full port of JdbcUserDao), `roleRepo.js`, `auditRepo.js`.
- `services/`: `authService.js` (port of AuthService: ACTIVE check, no enumeration, last_login, audit, JWT), `passwordService.js`, `auditService.js`.
- `middleware/`: `authJwt.js` (= AuthInterceptor), `roleGuard.js` (= RoleInterceptor, ADMIN-any), `validate.js`.
- `controllers/auth.controller.js` (login, logout, me, faq) + `routes/auth.routes.js`, mounted at `/api/v1/auth`.
- **Verified:** wrong pw→401 generic; missing→400; admin login→token+user; /me works; no-token→401; customer role correct; LOGIN/LOGIN_FAIL audit rows written.

**Frontend (DONE):**
- `shared/models/index.ts`; `core/auth/{token-storage,auth.service}.ts` (JWT decode + role signal);
  `core/services/{flash,confirm}.service.ts`; `core/interceptors/{auth,error}.interceptor.ts`;
  `core/guards/{auth,role,home-redirect}.guard.ts`; `core/layout/{app-shell,public-shell}.ts`;
  `features/auth/{login,faq,denied}.ts`; `shared/components/page-placeholder.ts` (used for not-yet-built routes).
- `app.routes.ts`: `/login` under PublicShell; authed area under AppShell (`authGuard`) with per-role children
  (`roleGuard` + `data.role`); `'' → homeRedirectGuard` (role dashboard or /login); `/faq`, `/denied`, `** → ''`.
  Placeholder routes registered for every sidebar link (replaced phase-by-phase).
- Interceptors registered in `app.config.ts`. Phase 0 landing removed.
- **Verified:** `ng build` clean; SPA served; login via proxy → token; `/me`; deep-link SPA fallback 200;
  JWT decodes to correct claims and routes to `/<role>/dashboard`.

> NOTE: `.nav-item.active` has no rule in icms.css (original relied on icms.js adding the class
> with no distinct style); `routerLinkActive="active"` is wired but intentionally not restyled to avoid drift.

### Phase 2 — DONE
**Backend (tested against live DB):**
- domain `claimStatus.js` (port of ClaimStatus + timeline builder); utils `money.js` (decimal.js, DECIMAL as strings), `paging.js`.
- repos: `claimRepo` (full ClaimDao port incl. agent/manager/surveyor queries for later phases), `policyRepo`, `policyholderRepo`, `documentRepo`, `communicationRepo`, `notificationRepo`.
- services: `claimService` (resolveCustomer, policiesForCustomer, listForCustomer, customerCounts, getOwnedClaim, customerClaimBundle, createClaim, withdraw; customer-safe projection strips internalNotes/fraud/risk/ids), `communicationService`, `notificationService`, `documentService` (forClaim + seedRequiredDocuments).
- `controllers/customer.controller.js` + `routes/customer.routes.js` (authJwt + roleGuard('CUSTOMER')); mounted `/api/v1/customer`.
- Endpoints: GET dashboard, claims (paged), claims/:id (bundle, ownership-enforced→404), policies, profile; POST claims (draft/submit), claims/:id/messages, claims/:id/withdraw.
- JWT now also carries `email` (needed to resolve policyholder); `dateStrings:true` on the DB connection (no tz date-shift).
- **Verified:** dashboard counts/recent/notifs; policies displayLabel; list pagination + statusLabel/statusPill; detail bundle (timeline 9, docs, msgs) with internalNotes stripped; cross-customer access→404; create SUBMITTED claim → CLM-2026-0013, 3 docs seeded, system msg, AGENT notification, CLAIM_SUBMITTED audit, decimal 12500.50 preserved; message POST 201; withdraw→WITHDRAWN; re-withdraw→409; validation 400 with field map.

**Frontend (builds clean):**
- shared models (Claim, Policy, Policyholder, ClaimDocument, Communication, Notification, TimelineStage, ClaimBundle, CustomerDashboard).
- reusable components: `status-pill`, `timeline`, `message-thread`, `paginator` (used by later phases too).
- `features/customer`: `customer.api.ts` + pages dashboard/claims/claim-detail/new-claim/profile (port the JSPs 1:1 with same CSS classes); `customer.routes.ts` lazy-loaded.
- `app.routes.ts` customer area now loads real components; `withComponentInputBinding()` enabled (route `:id` → component input).
- **Verified:** ng build clean; dashboard data loads via dev proxy; customer SPA deep-links 200.

> Document upload UI is intentionally stubbed on claim-detail (panel-foot note) until Phase 5.

### Phase 3 — DONE
**Backend (tested vs live DB — full lifecycle):**
- domain: `settlement.js` (TRACKER + next), `approval.js` (decisions).
- repos: `approvalRepo`, `assessmentRepo` (+components), `settlementRepo` (stamp cols), `configRepo` (full ConfigDao port — thresholds/sla/templates/docreqs, used by Phase 4 too).
- services: `agentClaimService` (list/statusCounts/worklist/bundle/acknowledge/updateNotes/forwardForApproval), `assignmentService` (availableSurveyors/assignSurveyor), `approvalService` (forClaim/createForwardChain/decide — decide used by Phase 4), `settlementService` (settlementScreen/authorize/advance), `surveyorService` (assignedClaims/counts/getAssignedClaim/assessScreen/submitAssessment with server-side net-payable).
- controllers/routes: `/api/v1/agent` (dashboard, claims, claim detail+surveyors, acknowledge, assign-surveyor, forward, notes, messages, communications, settlement GET/authorize/advance) + `/api/v1/surveyor` (dashboard, assessment GET/POST). roleGuard AGENT / SURVEYOR.
- **Verified end-to-end:** agent ack→UNDER_REVIEW; assign→SURVEY_SCHEDULED; surveyor submit → **net payable 83000.00 computed server-side** (gross 100000 − ded 5000 − 10% depr 10000 − salvage 2000), persisted; agent forward (~83k) → PENDING_APPROVAL with L1 APPROVED + L2 PENDING (threshold chain); settlement authorize → SETTLEMENT_PROCESSING, advance ×3 → PAYMENT_CONFIRMED → claim SETTLED.

**Frontend (builds clean):**
- models: Approval, Assessment, AssessmentComponent, Settlement, Surveyor, AgentBundle, SettlementScreen, AssessScreen, Agent/SurveyorDashboard.
- `features/agent`: api + dashboard/claims(filter bar)/claim-detail(action bar+assessment+approvals)/settlement(tracker+authorize)/communications, lazy routes.
- `features/surveyor`: api + dashboard + assess (**live net-payable calc** via reactive FormArray + computed signal — ports icms.js recalc; server recomputes authoritatively), lazy routes.
- app.routes loads real agent/surveyor components. Verified via proxy + deep-links.

> Document/report upload still stubbed (Phase 5). `POST /surveyor/claims/:id/report` route deferred to Phase 5.

## Key facts / decisions (don't re-derive)
- BCrypt: existing `$2a$10$...` hashes verify via `bcryptjs` (confirmed). New hashes use cost 10.
- Money `DECIMAL(15,2)`: mysql2 configured `decimalNumbers:false`, `bigNumberStrings:true` → carried as STRINGS. Use `decimal.js` for math (Phase 2+).
- Roles/namespaces: CUSTOMER, AGENT, SURVEYOR, MANAGER, ADMIN. ADMIN passes every role guard.
- API envelope: success `{data, correlationId}`; error `{error:{message,fields?}, correlationId}`; lists `{items,page,size,total}`.
- Layering to mirror: routes → controllers (thin) → services (business logic + tx + audit) → repositories (Knex) → db/tx.
- Reference files: `struts.xml` (routes), `db/Db.java` (tx), `service/ClaimService.java` (representative), `assets/css/icms.css` + `assets/js/icms.js`, `smoke-test.sh`.

## REST endpoint map (target — see plan for full table)
Public: `POST /auth/login`, `POST /auth/logout`, `GET /auth/me`, `GET /auth/faq`.
Then `/customer`, `/agent`, `/surveyor`, `/manager`, `/admin` resources (Phases 2–5).
