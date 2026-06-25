# ICMS Database Scripts

Operational scripts and a portable MySQL dump for the ICMS `icms` database.

| File              | Purpose                                                            |
|-------------------|-------------------------------------------------------------------|
| `schema.sql`      | Canonical DDL — tables, indexes, constraints for the `icms` DB.    |
| `seed.sql`        | Canonical seed data (demo users, roles, reference config).         |
| `icms-dump.sql`   | Full `mysqldump` — DROP/CREATE database, schema, data, routines.   |
| `dump-db.sh`      | Regenerate `icms-dump.sql` from a running MySQL server.            |
| `restore-db.sh`   | Restore the dump into a target MySQL server (destructive).        |

> `schema.sql` + `seed.sql` are the source-of-truth DDL/seed for the `icms`
> database (used by the Node/Express backend). `icms-dump.sql` is a point-in-time
> `mysqldump` snapshot for quick provisioning, backups, and sharing a known-good
> dataset.
>
> Provision a fresh DB from the canonical files:
> ```bash
> mysql -uroot -h127.0.0.1 icms < schema.sql && mysql -uroot -h127.0.0.1 icms < seed.sql
> ```

## Configuration (12-factor — no hardcoded credentials)

All scripts read connection settings from the environment:

| Var          | Default     | Notes                          |
|--------------|-------------|--------------------------------|
| `DB_HOST`    | `127.0.0.1` |                                |
| `DB_PORT`    | `3306`      |                                |
| `DB_NAME`    | `icms`      | dump only                      |
| `DB_USER`    | `root`      |                                |
| `DB_PASSWORD`| _(empty)_   | passed via `MYSQL_PWD`, not CLI |
| `OUT_FILE`   | `icms-dump.sql` | dump only                  |
| `IN_FILE`    | `icms-dump.sql` | restore only               |

## Usage

```bash
# Regenerate the dump from local MySQL
DB_PASSWORD=secret ./dump-db.sh

# Timestamped backup from a remote server
DB_HOST=db.prod DB_USER=backup \
  OUT_FILE=/backups/icms-$(date +%F).sql ./dump-db.sh

# Restore into a fresh / staging server (DROPs and recreates `icms`)
DB_HOST=db.staging DB_PASSWORD=secret ./restore-db.sh
```

Or restore manually:

```bash
mysql -uroot -h127.0.0.1 < icms-dump.sql
```
