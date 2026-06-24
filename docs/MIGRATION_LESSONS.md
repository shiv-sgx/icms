# Migrating Struts 2 → Angular + Node/Express — Lessons Learned

Practical notes distilled from migrating the ICMS app (Struts 2 + JSP/Tomcat →
Angular 22 + Node/Express + Knex, **same MySQL DB**). Ordered by where the real
effort and the real surprises were. Use it as a pre-flight checklist for similar work.

> TL;DR: the *application* port (logic, layers, UI) is the predictable part — mirror
> the original and it goes smoothly. The time sinks are the **runtime/browser boundary**
> (CSP, CSS build optimization, IPv6 binding, decimal/date serialization) — things
> Tomcat/JSP did for you that Node/Angular make explicit. When "curl works but the
> browser doesn't," suspect a response header or the build, not the network.

## 1. Strategy & sequencing
- **Keep the old app runnable until cutover.** Don't touch the Struts code or DB schema; rollback becomes "repoint the load balancer."
- **Reuse the database as-is.** Biggest risk-reducer — no schema/data migration; run both stacks side by side and diff behavior on the same rows.
- **Migrate phase-by-phase (by role/feature); commit + verify each phase.** We used: 0 scaffold → 1 auth/layout → 2 customer → 3 agent/surveyor → 4 manager/admin → 5 uploads/CSV → 6 parity/cutover.
- **Keep a crash-safe progress tracker in the repo** (`docs/MIGRATION_PROGRESS.md`) — for resuming and for review.

## 2. Preserve behavior, mirror the layering
- Struts **Action → Service → DAO** maps cleanly to **controller (thin) → service (logic + tx + audit) → repository (parameterized SQL)**. Mirroring it makes porting mechanical and reviewable.
- **Port business logic verbatim:** state machines (claim status, settlement tracker), approval thresholds, audit-on-every-change.
- **Keep security-sensitive math on the server.** The surveyor's live net-payable calc is UX only; the server recomputes authoritatively on submit. Never trust client-computed money/totals.

## 3. Auth is the highest-risk swap
- **Sessions → JWT** changes the model: no server session, logout is client-side, every request carries a Bearer token.
- **Verify password-hash compatibility FIRST.** Existing BCrypt `$2a$` hashes worked unchanged via `bcryptjs` — confirmed day one. If they hadn't, the plan changes (forced resets / dual-hash).
- **Re-implement authz, don't assume it.** Struts interceptors (auth + role-by-namespace, ADMIN-any) → JWT middleware + role guards. **Server-side guards are the real enforcement; SPA route guards are UX only.**
- **Enforce ownership on every endpoint.** JSP relied on session scoping; a REST API must check "does this principal own this resource?" explicitly (we returned 404 — not 403 — to avoid leaking existence).
- Decide token lifetime + refresh strategy up front (we used short-lived tokens + re-login; stateless logout can't pre-expire a token before its TTL).

## 4. Data-type landmines (silent, costly)
- **Money / `DECIMAL`:** DECIMAL → JS `Number` loses precision. Configure the driver to return decimals as **strings**, carry them as strings end-to-end, do arithmetic with `decimal.js`. (mysql2: `decimalNumbers:false`, `bigNumberStrings:true`.)
- **Dates:** set `dateStrings: true` so `DATE`/`DATETIME`/`TIME` come back as stored strings — otherwise JS `Date` + timezone shifts dates by a day in JSON.
- **Enums:** model DB enums as TS string-unions; validate against the allowed set before writing (fail fast with 400, not a DB error).
- **BIGINT ids:** return as strings or be careful past 2^53; we coerced with `Number()` where safe.

## 5. UI parity
- **Reuse the existing CSS verbatim.** Importing the original `icms.css` unchanged gave identical look & feel with ~zero styling work. Original `<s:...>` markup maps 1:1 to HTML with the same classes.
- **Port JSP per-page** into components; extract genuinely reused pieces (status pill, timeline, paginator, message thread, report table).
- **Translate JSP idioms:**
  - Tiles layout → app-shell component with `<router-outlet>`
  - `<s:iterator>` → `@for`, `<s:if>/<s:else>` → `@if/@else`
  - role-based sidebar (`<s:if test="#role==...">`) → a nav config keyed by role
  - session flash (POST-redirect-GET) → a flash service banner
  - `data-confirm` JS → a confirm service
  - redirect results → call API then router-navigate
  - active-nav JS → `routerLinkActive`

## 6. Deployment / browser gotchas (these cost the most time at the end)
All runtime/browser, not application logic — and all things Tomcat handled implicitly:
- **Angular `inlineCritical` (prod build):** inlines only critical CSS and lazy-loads the rest via `media="print" onload`. Left components unstyled in our case. Fix: `optimization.styles.inlineCritical: false` (or verify the deferred load actually applies).
- **Helmet CSP `upgrade-insecure-requests`:** forces assets to HTTPS. Works on `localhost` (secure context) but **blanks the page over plain HTTP on a LAN IP** (JS/CSS upgraded to `https://<ip>` which has no TLS). Drop the directive unless you're behind TLS; re-enable in production behind HTTPS.
- **Node binds IPv6-only by default** with `app.listen(port)` — unreachable for some IPv4 clients (Tomcat was IPv4, so it "just worked"). Bind explicitly to `0.0.0.0`.
- **CORS is a non-issue when the SPA is served same-origin** (relative `/api` base). It only matters if frontend and API are on different origins (e.g., `ng serve` cross-host). Don't add CORS you don't need.
- **`curl` ignores CSP, system proxies, and same-origin rules.** Great for proving the server works — but it *masks* browser-only failures. "curl 200 but browser blank" ⇒ suspect a **response header (CSP), the build, or a browser proxy**, not the network. A headless-Chrome screenshot is the fastest way to see the real rendered result.

## 7. Replace framework freebies Struts gave you for free
- **File upload:** `commons-fileupload` → `multer` (size cap, extension whitelist, filename sanitization, store **outside web root**, ownership-checked). Use a maintained major version (we moved to multer 2.x for the security fixes).
- **CSV export:** hand-rolled writer → keep RFC-4180 **and add a CSV-injection guard** (prefix cells starting with `= + - @`). An improvement over the original.
- **Cross-cutting:** correlation IDs (`CorrelationIdFilter` → middleware), structured logging, connection pool (HikariCP → knex/tarn + expose pool stats), config resolution (`AppConfig` env convention → reuse the same env keys so both stacks share config), transaction helper (`Db.inTransaction` → `knex.transaction`). Keep **audit as fire-and-forget outside the user's transaction** so an audit failure never breaks the action.

## 8. Prove parity objectively
- **Adapt the existing smoke/E2E test** to the new API but keep the **same DB assertions**. We rewrote the Struts cookie-based smoke test for JWT/REST yet asserted the same values (e.g., `net_payable = 95000.00`, claim → `SETTLED`, settlement → `PAYMENT_CONFIRMED`). Same DB + same outcomes = real proof. Get it green **before** cutover (we required 13/13).
- Spot-check visual parity against the old JSP pages (same CSS makes this easy).

## 9. Pre-flight checklist (copy/paste)
- [ ] DB schema frozen; both stacks point at the same DB
- [ ] Existing password hashes verified against the new hashing lib
- [ ] DECIMAL→string + `dateStrings` confirmed in the DB driver config
- [ ] Authz re-implemented server-side; ownership checks on every resource endpoint
- [ ] CSP reviewed for HTTP vs HTTPS deployment (`upgrade-insecure-requests`)
- [ ] Server bound to `0.0.0.0`; reachable via LAN IP (not just localhost)
- [ ] Prod build verified in a real browser / headless screenshot (not just curl)
- [ ] File upload limits + whitelist + out-of-webroot storage
- [ ] CSV export injection-guarded
- [ ] Adapted E2E/smoke test green with same DB assertions
- [ ] Old app still runnable for instant rollback; cutover + rollback documented
