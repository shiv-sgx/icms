# ICMS — Insurance Claim Management System: Struts2 Migration Plan

> **STATUS / RESUME POINTER (update this block after every work session):**
> - Phase 0 (Infra & auth): ☑ DONE & verified (2026-06-24) — all 5 roles log in to correct dashboards, role-guard denies cross-portal, logout works, logins audited. App boots clean (HikariCP + Tiles).
> - Phase 1 (Customer portal): ☑ DONE & verified (2026-06-24) — dashboard KPIs, new-claim submit/draft (type from policy, validation), required-doc seeding, agent notification, status timeline, document upload (stored outside WAR + required-slot match), messaging thread, withdraw (status guard), ownership authorization all verified against live DB.
> - Phase 2 (Agent portal): ☑ DONE & verified (2026-06-24) — dashboard KPIs+worklist, claims list (filter/search/paginate), claim detail bundle, acknowledge, assign surveyor (notify+audit), forward-for-approval (threshold chain L1✓+L2 pending, manager notified), internal notes, messaging, settlement authorize + payment-tracker advance→SETTLED, communications feed, role isolation. NOTE: removed Tomcat's bundled /manager (+host-manager/examples/docs) webapps in run.sh — they shadowed our /manager namespace.
> - Phase 3 (Surveyor portal): ☑ DONE & verified (2026-06-24) — assigned-claims dashboard+KPIs, assessment form with dynamic component breakdown + live net-payable JS, server-side gross→depreciation→net computation (verified 20000−5000−2000−1000=12000), component persistence, claim→UNDER_ASSESSMENT, agent notified, re-submit guard, survey report upload, ownership enforced. Cross-phase: agent forward reads net_payable for the approval-threshold decision.
> - Phase 4 (Manager portal + reports): ☑ DONE & verified (2026-06-24) — dashboard KPIs+queue+agent-perf, paginated approval queue, approval decision (approve/reject/return/hold) with multi-level chain (L2 approve leaves L3 pending → stays PENDING_APPROVAL; final approve → APPROVED), settlement override, 6 analytics reports, CSV export (text/csv + Content-Disposition, RFC-4180), role isolation. All verified against live DB.
> - Phase 5 (Admin portal + audit): ☑ DONE & verified (2026-06-24) — dashboard (counts + live HikariCP pool stats), user mgmt (search, create w/ BCrypt → new user logs in, update status/role → inactive blocked, reset password), role mgmt, SLA config, approval thresholds, notification templates, document-requirement add/delete, audit viewer (filter+paginate) + CSV export, role isolation. Hardened Roles.forNamespace to prefix-match (sub-namespaces can't fall through to unguarded). NOTE: never name an action field "action" (OGNL ValueStack collision) — used actionName.
> - Phase 6 (Hardening & verification): ☑ DONE & verified (2026-06-24) — struts.devMode off (config-driven), generic error page (no stack-trace leak to browser), quieter log4j (framework WARN, app INFO) with per-request correlation id (CorrelationIdFilter → %X{cid} + X-Correlation-Id header), removed dead DashboardAction, namespace prefix-match authorization, README.md, smoke-test.sh. **Full end-to-end lifecycle smoke test: 12/12 PASS** (customer submit → agent ack/assign → surveyor assess → agent forward → manager approve → agent settle → SETTLED).
>
> **PROJECT COMPLETE** — all 6 phases done. App runs at http://localhost:8080/ (./setup.sh then ./run.sh). Verify anytime with ./smoke-test.sh.

---

## Context

The `icms` repo started as a bare Struts2 2.2.1 "Hello World" skeleton. It has already been pre-wired for an **Insurance Claim Management System (ICMS)**: `pom.xml` carries the full dependency set (MySQL, HikariCP, slf4j/log4j, jBCrypt, JSTL) and `src/main/resources/db/{schema.sql,seed.sql}` define an 18-table schema with realistic demo data. **No application code exists yet** beyond `HelloWorldAction` + two demo JSPs.

The goal is to **build the ICMS application on this skeleton to match the Figma wireframe** at `https://santa-evoke-68994092.figma.site/` using the **same tech stack** (Struts2 2.2.1 + JSP/JSTL + JDBC/HikariCP/MySQL + BCrypt, Servlet 2.5, Java 8, Tomcat 9). The wireframe is a 5-role, login-gated claims platform whose screens map almost 1:1 onto the existing DB schema.

A prior attempt was lost when the IDE crashed mid-execution **with no saved plan**. This plan is the durable artifact: phased, checkpointed, resumable.

### User-confirmed scope decisions
1. **Full app** — all 5 role portals and the complete claim lifecycle.
2. **CSV-only** report export (no new dependency; stream via `HttpServletResponse`).
3. **`setup.sh`** creates schema, generates a real BCrypt hash for the demo password, injects it for the `__PWHASH__` token, and loads seed data. DB config externalized via env vars / `db.properties`.
4. **Struts-only UI** — views built with the **Struts2 tag library** (`<s:form>`, `<s:textfield>`, `<s:select>`, `<s:iterator>`, `<s:if>`, `<s:url>`, `<s:property>`, etc.) and templated with **Apache Tiles** (`struts2-tiles-plugin`, Tiles 2). **No plain `jsp:include` layout and no SPA framework**; JSTL used only where a Struts2 tag has no equivalent.

---

## Stack & non-negotiable constraints

- Struts2 **2.2.1.1** (old API: filter `org.apache.struts2.dispatcher.ng.filter.StrutsPrepareAndExecuteFilter`, package `extends="struts-default"`). Servlet **2.5**, JSP **2.1**, JSTL **1.2**, Java **8** (`maven.compiler.release=8`), packaged as `ROOT.war`, served at `/`.
- **View layer = Struts2 tags + Apache Tiles.** Add to `pom.xml`: `org.apache.struts:struts2-tiles-plugin:2.2.1.1` (pulls in Tiles 2.0.x — pin `tiles-core`/`tiles-jsp`/`tiles-template` to the version the plugin expects). Register `org.apache.struts2.tiles.StrutsTilesListener` in `web.xml`; secured packages `extend="tiles-default"`; results use `type="tiles"` pointing at definitions in `/WEB-INF/tiles.xml`. Views use the Struts2 UI tag library; the simple/xhtml theme is fine (no extra theme deps).
- **No hardcoded** secrets/URLs/credentials/ports — all DB + path config from env vars with `db.properties` defaults (12-Factor).
- **All SQL parameterized** (PreparedStatement) — no string concatenation of user input (injection-safe).
- **No swallowed exceptions** — custom exception types, contextual logging via slf4j, standardized user-facing error result; never leak stack traces/internal detail to the browser.
- Session-based auth; **role authorization enforced server-side** on every secured action (not just hidden nav).
- Layered architecture (Action → Service → DAO), thin actions, transactions owned by services, resources closed with try-with-resources, connection pooling via HikariCP.
- Lists paginated; audit trail on state changes; inputs validated server-side.

---

## Target architecture

### Package layout (root `com.sgx.icms`, matching existing convention)

```
config/      AppConfig (env+properties loader), DataSourceProvider (HikariCP singleton),
             AppContextListener (ServletContextListener: init pool on boot, close on shutdown)
domain/      POJOs 1:1 with tables: User, Role, Policyholder, Policy, Claim, ClaimDocument,
             Assessment, AssessmentComponent, Approval, Settlement, Communication,
             Notification, AuditLog, ApprovalThreshold, SlaConfig, DocumentRequirement,
             NotificationTemplate; status values as String constants/enums
db/          Db (query/update/tx helper over DataSource), RowMapper<T>, Tx (inTransaction),
             DaoException (custom unchecked)
dao/         Interface + JdbcXxxDao impl per aggregate: UserDao, PolicyDao, PolicyholderDao,
             ClaimDao, DocumentDao, AssessmentDao, ApprovalDao, SettlementDao,
             CommunicationDao, NotificationDao, AuditDao, ConfigDao. Methods take a
             Connection (for tx composition) + convenience overloads.
service/     AuthService, ClaimService, AssignmentService, AssessmentService,
             ApprovalService, SettlementService, CommunicationService,
             NotificationService, ReportService, AdminService, AuditService,
             PasswordService (jBCrypt wrapper)
web/action/  BaseAction (SessionAware/ServletRequestAware, common helpers, paging),
             + per-role subpackages: auth/, customer/, agent/, surveyor/, manager/, admin/
web/interceptor/  AuthInterceptor (session-present + last_login + audit),
                  RoleInterceptor (namespace→allowed-role guard)
web/support/ SessionUser (id, username, fullName, role, branch), Paged<T>, CsvWriter
tools/       PasswordHashTool (main: prints BCrypt hash; used by setup.sh)
```

### Cross-cutting infrastructure (Phase 0 foundation)

- **DataSourceProvider**: builds one `HikariDataSource` from `AppConfig`. Keys (env var → `db.properties` default): `DB_URL` (`jdbc:mysql://localhost:3306/icms?...`), `DB_USER`, `DB_PASSWORD`, `DB_POOL_MAX` (10), `DB_POOL_MIN` (2), `ICMS_UPLOAD_DIR`, `ICMS_PAGE_SIZE` (15), `DEMO_PASSWORD`. Initialized by `AppContextListener.contextInitialized`, closed on `contextDestroyed`. No connections opened per request outside the pool.
- **Db helper**: `query(conn, sql, mapper, params...)`, `queryOne(...)`, `update(conn, sql, params...)`, `Tx.inTransaction(fn)` (autocommit off, commit/rollback, close). All via try-with-resources. This is the only place JDBC plumbing lives — DAOs stay declarative.
- **Auth flow**: `LoginAction` → `AuthService.authenticate(username, rawPw)` → `UserDao.findByUsername` → `PasswordService.matches` (jBCrypt `checkpw`). On success store `SessionUser`; audit `LOGIN`/`LOGIN_FAIL`. `AuthInterceptor` guards secured namespaces (redirect to `/login` when absent). `RoleInterceptor` maps namespace → role: `/customer`→CUSTOMER, `/agent`→AGENT, `/surveyor`→SURVEYOR, `/manager`→MANAGER, `/admin`→ADMIN (ADMIN may be allowed cross-portal where the wireframe implies). `LogoutAction` invalidates session.
- **struts.xml**: replace the single demo package with a `public` package (login/logout/index/faq) + one secured package per namespace, each defining an interceptor stack `icmsStack = exception, servletConfig, prepare, ... , auth, role`. Keep dev `struts.devMode` off for prod via constant from config. Result JSPs under `/WEB-INF/jsp/<role>/...`; convert URLs to extensionless (existing `struts.action.extension= ,`).
- **Layout (Apache Tiles + Struts2 tags)**: `/WEB-INF/tiles.xml` defines a base `.layout` (attributes: `title`, `header`, `sidebar`, `body`, `footer`) backed by `/WEB-INF/jsp/layout/layout.jsp` (renders top bar with ICMS brand + logged-in user + logout, role-specific left sidebar, content body, footer using `<tiles:insertAttribute>`). Sidebar JSP switches nav by `SessionUser.role`. Each page is a Tiles definition `extends=".layout"` overriding `title`+`body`; actions return result `type="tiles"`. **All page markup uses Struts2 tags** (`<s:form>/<s:textfield>/<s:select>/<s:iterator>/<s:if>/<s:url>/<s:a>/<s:property>/<s:actionerror>`). One `webapp/assets/css/icms.css` approximating the wireframe's clean enterprise look (status pills, risk badges, cards, tables, timeline). Minimal vanilla `icms.js` for tabs, multi-step claim form, confirm dialogs.
- **AuditService.log(user, action, entity, result, ip)** called from every state-changing service method.
- **Error handling**: global `exception` interceptor → `/WEB-INF/jsp/error.jsp` (generic message + correlation id in logs); `web.xml` error-page entries for 404/500.

---

## Wireframe → implementation map (condensed)

Each role gets a dashboard + the screens below. Routes are namespaced; every secured action passes through auth+role interceptors.

| Role | Screens (Figma) | Key actions / routes | Primary tables |
|------|-----------------|----------------------|----------------|
| **Customer** | My Claims, New Claim Submission (type select → details → docs → review), Claim Details + **Status Timeline**, Upload Docs, **Communication Center**, Withdraw/Close, My Profile, FAQs | `/customer/dashboard`, `/customer/claims/new` (multi-step, Save Draft/Submit), `/customer/claims/{id}`, `/customer/claims/{id}/documents`, `/customer/claims/{id}/messages`, `/customer/claims/{id}/withdraw`, `/customer/profile`, `/faq` | claims, claim_documents, communications, policies, policyholders, document_requirements |
| **Agent** | Dashboard (Open Claims / Avg-in-queue / worklist), Claims List (search/filter/paginate), Claim Details (edit, acknowledge), **Assign Surveyor**, Review Assessment, Forward for Approval, **Settlement Screen** (calc + payment tracker), Communications | `/agent/dashboard`, `/agent/claims`, `/agent/claims/{id}`, `/agent/claims/{id}/assign`, `/agent/claims/{id}/forward`, `/agent/settlements/{claimId}`, `/agent/claims/{id}/message` | claims, assessments, approvals, settlements, communications, users |
| **Surveyor** | Assigned Claims, **Claim Assessment** (visit date/time, site observations, **component breakdown** w/ severity+cost, gross→deductible→depreciation→salvage→**net payable**, recommendation, confidence), Upload Survey Report + site photos, Submit | `/surveyor/dashboard`, `/surveyor/claims/{id}/assess`, `/surveyor/claims/{id}/report`, submit assessment | assessments, assessment_components, claim_documents, claims |
| **Manager** | Dashboard (approval queue, agent performance), **Approval Workflow** (Approve/Reject/Return/Hold + remarks, multi-level L1/L2/L3 vs thresholds), Review assessment, Override settlement, **Reports & Analytics** (Claims Volume, SLA Compliance, Settlement TAT, Fraud Detection, Agent Performance) + **CSV export** | `/manager/dashboard`, `/manager/approvals`, `/manager/approvals/{claimId}/decide`, `/manager/settlements/{claimId}/override`, `/manager/reports`, `/manager/reports/{type}/export.csv` | approvals, claims, settlements, approval_thresholds, sla_config, users |
| **Admin** | Dashboard / system monitoring, **User Management** (search by name/email/role, status, last login, CRUD), Role & Permission Mgmt, Claim Config (document_requirements), **SLA Config**, **Approval Thresholds**, **Notification Templates**, **Audit Logs** (+ CSV export) | `/admin/dashboard`, `/admin/users`, `/admin/users/{id}`, `/admin/roles`, `/admin/config/documents`, `/admin/config/sla`, `/admin/config/thresholds`, `/admin/templates`, `/admin/audit`, `/admin/audit/export.csv` | users, roles, document_requirements, sla_config, approval_thresholds, notification_templates, audit_logs |

**Claim status lifecycle** (drives timeline + allowed transitions, enforced in `ClaimService`):
`DRAFT → SUBMITTED → UNDER_REVIEW → SURVEY_SCHEDULED → UNDER_ASSESSMENT → PENDING_APPROVAL → APPROVED → SETTLEMENT_PROCESSING → SETTLED → CLOSED`; side states `REJECTED`, `WITHDRAWN`, `ON_HOLD`. Settlement payment tracker: `AUTHORIZED → PAYMENT_INITIATED → BANK_PROCESSING → PAYMENT_CONFIRMED → CLAIMANT_NOTIFIED → CLOSED`.

---

## Phased execution (each phase is a resumable checkpoint)

### Phase 0 — Infrastructure, auth, layout *(foundation; everything depends on it)*
- **FIRST: persist this plan into the repo** — copy it to `docs/IMPLEMENTATION_PLAN.md` so it lives in the project and survives any IDE crash. Keep the STATUS pointer at the top of that copy in sync after every phase.
- `config/`: `AppConfig`, `DataSourceProvider`, `AppContextListener` (register in `web.xml`).
- `db/`: `Db`, `RowMapper`, `Tx`, `DaoException`. `resources/db.properties` (defaults, no secrets) + document env overrides.
- `domain/`: all POJOs + status constants.
- `dao/UserDao` + `JdbcUserDao` (findByUsername, updateLastLogin); `AuditDao`.
- `service/`: `PasswordService`, `AuthService`, `AuditService`.
- `web/support/SessionUser`; `web/interceptor/{AuthInterceptor,RoleInterceptor}`; `web/action/BaseAction`, `auth/{LoginAction,LogoutAction}`.
- Add `struts2-tiles-plugin` (+ Tiles 2 deps) to `pom.xml`. Rewrite `struts.xml` (public + 5 secured packages `extend="tiles-default"` + interceptor stacks, `type="tiles"` results); update `web.xml` (AppContextListener, **StrutsTilesListener**, welcome=login, error pages, session-timeout).
- Layout via **Tiles**: `/WEB-INF/tiles.xml` (base `.layout` + login/error definitions), `layout.jsp` + role sidebar JSPs (Struts2 + `<tiles:insertAttribute>`), `icms.css`, `icms.js`, `error.jsp`, `login.jsp` (Struts2 `<s:form>`). Repurpose/remove `HelloWorldAction`+demo JSPs.
- `setup.sh` + `tools/PasswordHashTool`; update `run.sh`/`stop.sh` notes for DB env vars; README/run notes.
- **Exit criteria**: build WAR, run `setup.sh`, deploy, log in as each seeded role (admin/manager/agent/surveyor/customer) → land on a role-appropriate empty dashboard shell; wrong-role URL is blocked; logout works.

### Phase 1 — Customer portal
- DAOs/services: `ClaimDao`, `PolicyDao`, `PolicyholderDao`, `DocumentDao`, `CommunicationDao`, `NotificationDao`; `ClaimService` (create/draft/submit/withdraw, status guard), `CommunicationService`, `NotificationService`.
- Actions + JSPs: dashboard (my claims + notifications), multi-step **new claim** (type-specific fields per schema: motor/health/property/etc., Save Draft/Submit, server validation, doc-requirement seeding from `document_requirements`), claim detail + **status timeline**, document upload (Struts2 fileUpload → `ICMS_UPLOAD_DIR/claims/{id}/`, persist row, type/size limits), messaging, withdraw, profile, FAQ.
- **Exit criteria**: customer logs in, submits a claim end-to-end, sees it listed with correct status + timeline, uploads a document, sends a message.

### Phase 2 — Agent portal
- `AssignmentService`, `SettlementService` (calc + tracker), extend `ClaimService` transitions.
- Dashboard (worklist KPIs from SQL aggregates), claims list (search/filter/paginate via `ClaimDao` with limit/offset), claim detail/edit/acknowledge, **assign surveyor** (sets `surveyor_id`, status→SURVEY_SCHEDULED, notify), review assessment, forward for approval (→PENDING_APPROVAL, create `approvals` rows by threshold), **settlement screen** (amount calc, payment method/bank fields, payment status tracker), communications.
- **Exit criteria**: agent picks a submitted claim, assigns a surveyor, later forwards an assessed claim for approval, and processes a settlement.

### Phase 3 — Surveyor portal
- `AssessmentService` (+ `AssessmentDao`): create/update assessment, manage `assessment_components`, compute gross→deductible→depreciation→salvage→**net payable**, recommendation + confidence, submit (status→PENDING_APPROVAL/UNDER_ASSESSMENT per flow).
- Screens: assigned-claims dashboard, **assessment form** (component table add/remove, live totals via `icms.js`), upload survey report + site photos.
- **Exit criteria**: surveyor opens an assigned claim, records components, system computes net payable, submits report; agent/manager can see it.

### Phase 4 — Manager portal + reports
- `ApprovalService` (multi-level L1/L2/L3 vs `approval_thresholds`, approve/reject/return/hold with remarks + audit), settlement override, `ReportService` (SQL aggregates).
- Screens: dashboard (approval queue + agent performance), **approval workflow** screen, settlement override, **Reports & Analytics** (5 reports) + **CSV export** via `CsvWriter` + `HttpServletResponse` (`text/csv`, `Content-Disposition`).
- **Exit criteria**: manager approves/rejects from the queue (claim status transitions + audit), views each report, downloads a CSV.

### Phase 5 — Admin portal + audit
- `AdminService` + `ConfigDao` CRUD. Screens: dashboard/monitoring (pool stats, counts), **user management** (search/filter, create/edit, status, role assign, reset password via `PasswordService`), role mgmt, claim config (document_requirements), SLA config, approval thresholds, notification templates, **audit logs** viewer (filter/paginate) + CSV export.
- **Exit criteria**: admin creates a user (BCrypt-hashed pw), edits a threshold/SLA/template, browses + exports audit logs.

### Phase 6 — Hardening & end-to-end verification
- Server-side validation coverage, consistent error handling, input sanitization review, authorization re-check on every namespace, pagination defaults, resource-leak pass, structured logging + correlation id, remove dead demo code, `struts.devMode` off by default (config-driven).
- Full lifecycle smoke test (below). Note any remaining bottlenecks/optimizations.

---

## Setup & verification

**One-time setup** (`setup.sh`, config from env, defaults in `db.properties`):
1. `mysql < src/main/resources/db/schema.sql`
2. `HASH=$(java ... tools.PasswordHashTool "$DEMO_PASSWORD")` (default `DEMO_PASSWORD=Password@123`), classpath resolved from the Maven build/`~/.m2` jBCrypt jar.
3. `sed "s/__PWHASH__/$HASH/g" seed.sql | mysql` (escape `$`/`/` safely).
4. Print seeded logins (admin/manager/agent/surveyor/customer, all = `DEMO_PASSWORD`).

**Build & run**: `./run.sh` (Maven `clean package` → deploy `target/ROOT.war` to Tomcat 9) → `http://localhost:8080/`.

**End-to-end smoke test** (final acceptance):
1. Login each of the 5 roles → correct role dashboard; cross-role URL access denied.
2. Customer submits a new motor claim with a document → appears in agent worklist.
3. Agent assigns surveyor → status `SURVEY_SCHEDULED`, customer timeline updates.
4. Surveyor completes assessment (component breakdown, net payable) → submits.
5. Agent forwards → Manager approval queue → Manager approves → settlement processed by agent → status `SETTLED`, customer notified.
6. Manager opens each report + downloads a CSV; Admin edits a config item + exports audit logs.
7. Confirm `audit_logs` captured the state changes; no stack traces surfaced to the browser; logout invalidates session.

---

## Key risks / watch-items
- **Old Struts 2.2.1 API**: interceptor class names, `fileUpload`/`json` plugin availability, DTD — verify against the bundled jars; avoid newer-version-only features.
- **Tiles 2 + Struts 2.2.1**: `struts2-tiles-plugin:2.2.1.1` targets **Tiles 2.0.x** (not 2.2/3.x) — pin matching `tiles-core`/`tiles-jsp`/`tiles-template` and use `StrutsTilesListener` (the plugin's listener, not raw `org.apache.tiles.web.startup.TilesListener`). Validate one Tiles page renders before fanning the pattern out across all screens (de-risk the templating early in Phase 0).
- **MySQL connector 9.x vs Servlet 2.5 era**: ensure JDBC URL params (`serverTimezone`, `allowPublicKeyRetrieval`, `useSSL=false` for local) are config-driven, not hardcoded.
- **jBCrypt 0.4 hash prefix**: generate hashes with `PasswordHashTool` (same lib) so `checkpw` accepts them; don't mix `htpasswd` `$2y$` output unverified.
- **`__PWHASH__` injection**: BCrypt hashes contain `/` and `$` — use a sed-safe substitution (or load via a temp file) to avoid corrupting the SQL.
- **File uploads**: enforce type/size limits and store outside the WAR (`ICMS_UPLOAD_DIR`); never trust client filename for the stored path.
- **Transactions**: multi-table writes (forward-for-approval, settlement, assessment+components) must be single `Tx.inTransaction` units.
- Scope is large — strictly honor the phase checkpoints and update the STATUS pointer after each so a crash resumes cleanly.
