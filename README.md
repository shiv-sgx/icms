# ICMS — Insurance Claim Management System

A role-based insurance claim management web application built on **Struts 2.2.1**
(JSP + Apache Tiles views, Struts2 tag library), **JDBC + HikariCP + MySQL**, and
**BCrypt** authentication. Runs on Servlet 2.5 / Java 8 / Tomcat 9 and is served as
the ROOT context.

It implements the full claim lifecycle across five role portals, matching the
reference wireframe.

## Roles & portals

| Role | Capabilities |
|------|--------------|
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

Layered: **Action → Service → DAO**, thin actions, transactions owned by services.

```
config/      AppConfig (env+properties), DataSourceProvider (HikariCP), AppContextListener
db/          Db (query/update/tx helper), RowMapper, DaoException
domain/      POJOs + status/role constants
dao/         JDBC DAOs (parameterised SQL only)
service/     business logic, transactions, auditing
web/action/  Struts2 actions (per-role subpackages)
web/interceptor/  AuthInterceptor, RoleInterceptor (server-side authorization)
web/filter/  CorrelationIdFilter (request tracing)
web/support/ SessionUser, Paged, ClaimBundle, ReportTable, CsvWriter
tools/       PasswordHashTool (used by setup.sh)
```

Views: Apache Tiles (`/WEB-INF/tiles.xml`) + Struts2 tags under `/WEB-INF/jsp/`.

## Configuration (12-Factor)

All config is externalised — defaults in `src/main/resources/db.properties`, every key
overridable by an environment variable (e.g. `db.url` → `DB_URL`). **No secrets are
committed.**

| Key | Env | Default |
|-----|-----|---------|
| `db.url` | `DB_URL` | `jdbc:mysql://localhost:3306/icms?...` |
| `db.user` / `db.password` | `DB_USER` / `DB_PASSWORD` | `root` / *(empty)* |
| `db.pool.max` / `db.pool.min` | `DB_POOL_MAX` / `DB_POOL_MIN` | `10` / `2` |
| `icms.upload.dir` | `ICMS_UPLOAD_DIR` | `${user.home}/icms-uploads` |
| `icms.page.size` | `ICMS_PAGE_SIZE` | `15` |

## Setup & run

Prerequisites: MySQL running locally, and the toolchain under `../tools/`
(Maven 3.9.x, Tomcat 9, JDK 11) — or set `JAVA_HOME`/`MVN`/`TOMCAT` env vars.

```bash
./setup.sh     # creates schema, BCrypt-hashes the demo password, loads seed data
./run.sh       # builds the WAR and deploys to Tomcat → http://localhost:8080/
./stop.sh      # stops Tomcat
```

`setup.sh` honours `DB_HOST`, `DB_PORT`, `DB_USER`, `DB_PASSWORD`, and
`DEMO_PASSWORD` (default `Password@123`).

### Seeded logins (all share the demo password)

| Username | Role |
|----------|------|
| `admin` | ADMIN |
| `manager` | MANAGER |
| `agent` | AGENT |
| `surveyor` | SURVEYOR |
| `customer` | CUSTOMER |

## Security

- Session-based auth; **role authorization enforced server-side** on every secured
  namespace (not just hidden nav). Namespaces are prefix-matched so sub-paths can't
  fall through to "unrestricted".
- All SQL is parameterised (injection-safe). BCrypt password hashing.
- Inputs validated server-side; uploads are type/size-checked and stored outside the
  WAR; the client filename is never trusted for the stored path.
- Dynamic method invocation is disabled; exceptions are mapped to a generic error page
  (no stack traces leak to the browser). Every state change is written to `audit_logs`.

## Verification

`./smoke-test.sh` drives the complete lifecycle across all five roles against the
running app and asserts each transition (submit → acknowledge → assign → assess →
forward → approve → settle → SETTLED).
