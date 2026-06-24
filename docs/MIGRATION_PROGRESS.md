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
| 1 | Auth + layout (JWT, shell, login) | 🔄 IN PROGRESS |
| 2 | Customer portal | ⬜ pending |
| 3 | Agent + Surveyor portals | ⬜ pending |
| 4 | Manager + Admin + reports | ⬜ pending |
| 5 | Uploads + CSV exports | ⬜ pending |
| 6 | Parity verification + cutover | ⬜ pending |

### Phase 0 — DONE
- `/backend`: Express+Knex+pino; `env.js` (mirrors AppConfig key convention), `knex.js` (no migrations, tarn pool stats), `tx.js` (= Db.inTransaction), correlation-id + helmet + cors + central error handler, `GET /api/v1/health` (verified against live DB).
- `/frontend`: Angular 22 standalone; reuses `icms.css` verbatim; dev proxy `/api`→:3000; landing verifies design + proxy. Builds clean.
- Root `.gitignore` updated (node_modules/dist/.angular). Verified end-to-end (ng serve → Node → MySQL).

### Phase 1 — IN PROGRESS
**Backend (DONE + tested):**
- `utils/bcrypt.js` (jbcrypt-compatible — verified `$2a$10$` hashes authenticate), `utils/jwt.js`, `utils/errors.js`, `utils/http.js`.
- `repositories/`: `userRepo.js` (full port of JdbcUserDao), `roleRepo.js`, `auditRepo.js`.
- `services/`: `authService.js` (port of AuthService: ACTIVE check, no enumeration, last_login, audit, JWT), `passwordService.js`, `auditService.js`.
- `middleware/`: `authJwt.js` (= AuthInterceptor), `roleGuard.js` (= RoleInterceptor, ADMIN-any), `validate.js`.
- `controllers/auth.controller.js` (login, logout, me, faq) + `routes/auth.routes.js`, mounted at `/api/v1/auth`.
- **Verified:** wrong pw→401 generic; missing→400; admin login→token+user; /me works; no-token→401; customer role correct; LOGIN/LOGIN_FAIL audit rows written.

**Frontend (PARTIAL — remaining to finish Phase 1):**
- DONE: `shared/models/index.ts`; `core/auth/{token-storage,auth.service}.ts`; `core/services/{flash,confirm}.service.ts`;
  `core/interceptors/{auth,error}.interceptor.ts`; `core/guards/{auth,role}.guard.ts`;
  `core/layout/{app-shell,public-shell}.ts`; `features/auth/login.ts`.
- TODO to finish Phase 1:
  1. `features/auth/faq.ts` (render FAQ from `GET /auth/faq`), `features/auth/denied.ts`, `features/auth/error.ts` (404/500).
  2. Per-role dashboard placeholder components (so login redirect + sidebar nav resolve): customer/agent/surveyor/manager/admin `dashboard` + a generic "coming soon" page for not-yet-built routes (claims, profile, etc.).
  3. `app.routes.ts`: public shell (`/login`, `/faq` optionally public), authed AppShell with `authGuard` + per-role children using `roleGuard` (data.role). `'' → redirect to role dashboard or /login`. `/denied`, `**`.
  4. Register interceptors in `app.config.ts`: `provideHttpClient(withInterceptors([authInterceptor, errorInterceptor]))`.
  5. Remove the Phase 0 `features/landing` placeholder + its route.
  6. Build (`ng build`) and verify login→dashboard→logout against running backend.

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
