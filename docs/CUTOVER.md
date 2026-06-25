# ICMS MEAN Cutover & Deployment

The Struts 2 app and the new Angular + Node/Express app both talk to the **same**
MySQL `icms` database (no schema change), so they can run side by side until cutover.

## Run side by side (during migration)
| Component | Port | Notes |
|-----------|------|-------|
| Legacy Struts (Tomcat) | 8080 | `./run.sh` (optional during migration) |
| Node API | 3000 | `cd backend && npm start` |
| Angular dev server | 4200 | `cd frontend && npx ng serve` (proxies `/api` → 3000) |

Both read/write the same rows — useful for visual parity checks.

## Production topology (post-cutover)
Single Node process serves the API **and** the built Angular SPA same-origin (no CORS):

```bash
# 1. Build the SPA
cd frontend && npm ci && npx ng build --configuration production
#    output: frontend/dist/frontend/browser

# 2. Run the API with static serving enabled
cd ../backend && npm ci
cp .env.example .env            # set JWT_SECRET + DB creds (real values)
export STATIC_DIR="$(cd ../frontend/dist/frontend/browser && pwd)"
npm start                       # http://localhost:3000  (SPA + /api/v1)
```

`STATIC_DIR` makes Express serve the SPA at `/` with a client-side-routing fallback,
while `/api/*` continues to route to the API. Alternatively, put the SPA behind a
reverse proxy (nginx) and proxy `/api` to Node — also same-origin.

### Required environment
- `JWT_SECRET` (required, every env), `JWT_TTL` (default 8h)
- DB: `DB_URL` (JDBC-style, same as Struts) **or** `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD`; `DB_POOL_MAX/MIN`
- `ICMS_UPLOAD_DIR` (claim documents, outside web root), `ICMS_PAGE_SIZE`
- `STATIC_DIR` (prod SPA path), `CORS_ORIGINS` (only if SPA is cross-origin), `PORT`, `LOG_LEVEL`

## Parity verification (run before cutover)
```bash
cd backend && npm start &                     # API on :3000
BASE_URL=http://localhost:3000/api/v1 bash test/smoke/smoke-test.sh
```
Drives the full lifecycle (customer submit → agent ack/assign → surveyor assess →
forward → manager approve → settle → SETTLED) and asserts every transition against
the same MySQL DB, including `net_payable = 95000.00`. Expect **13 passed, 0 failed**.

## Cutover steps
1. Deploy Node (API + SPA) pointing at the production `icms` DB; set a real `JWT_SECRET`.
2. Run the smoke test against the new deployment; confirm 13/13.
3. Switch the public hostname/load balancer to the Node service.
4. Stop Tomcat: `./stop.sh` (or `$TOMCAT/bin/catalina.sh stop 10 -force`).
5. Archive the legacy Struts app — keep it in git history; the runtime no longer needs
   `src/main/java`, `src/main/webapp` (JSP), `struts.xml`, `pom.xml`, Tomcat.

> Source of truth for the DB remains `config/dbscript/{schema.sql,seed.sql}` and
> `config/dbscript/icms-dump.sql` — unchanged by the migration. (The legacy Struts
> tree, including its original copy under `src/main/resources/db/`, has since been
> removed from the working tree and lives only in git history.)

## Rollback
Because the schema is unchanged and both stacks share the DB, rollback is just:
re-point the load balancer to Tomcat and restart it (`./run.sh`). No data migration to undo.

## Notes / deferred (non-blocking)
- i18n (en/de) was minimal in the legacy app; English strings are inline in Angular.
  `@angular/localize` can be added later without API changes.
- Stateless JWT logout cannot pre-expire a token before its TTL (acceptable; a token
  deny-list can be added if required).
