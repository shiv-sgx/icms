# ICMS Backend (Node/Express + Knex)

REST API for ICMS, migrated from the legacy Struts 2 app. Talks to the **same**
MySQL `icms` database (no schema changes). Layered to mirror the original:
**routes → controllers (thin) → services (business logic + tx + audit) → repositories (parameterized Knex) → db/tx**.

## Requirements
- Node 18+ (works on newer; LTS recommended for prod)
- The existing MySQL `icms` database (see `../src/main/resources/db/schema.sql` + `seed.sql`)

## Setup
```bash
cp .env.example .env        # then set JWT_SECRET (a real random value) + DB creds
npm install
npm run dev                 # nodemon on :3000  (npm start for prod)
```

## Configuration
Env-driven, mirroring the legacy `AppConfig` key convention (`db.url` → `DB_URL`, etc.).
Either set `DB_URL` (JDBC-style, same as the Struts app) or the discrete
`DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD`. See `.env.example` for all keys.
`JWT_SECRET` is required in every environment.

## Health
```bash
curl http://localhost:3000/api/v1/health
# { "data": { "status":"UP","db":"UP","pool":{...} }, "correlationId":"…" }
```

## Conventions
- Success: `{ data, correlationId }`. Error: `{ error: { message, fields? }, correlationId }`.
- Lists: `{ items, page, size, total }`.
- Money (`DECIMAL(15,2)`) is carried as **strings** end-to-end (see `utils/money.js`).
- Every request carries an `X-Correlation-Id` (generated or honored from a trusted proxy).
