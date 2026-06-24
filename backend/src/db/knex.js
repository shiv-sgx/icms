'use strict';

const knexFactory = require('knex');
const { config } = require('../config/env');
const logger = require('../config/logger');

/**
 * Single application-wide Knex instance (the counterpart of DataSourceProvider +
 * HikariCP). Knex is used purely as a parameterised query builder over the
 * EXISTING schema — no migrations are run against this database.
 */
const knex = knexFactory({
  client: 'mysql2',
  connection: config.db.connection,
  pool: {
    min: config.db.pool.min,
    max: config.db.pool.max,
    acquireTimeoutMillis: config.db.pool.acquireTimeoutMillis,
  },
  acquireConnectionTimeout: config.db.pool.acquireTimeoutMillis,
});

/** Verify connectivity at boot (fail-fast, like Hikari initFailTimeout). */
async function verifyConnection() {
  await knex.raw('SELECT 1');
  logger.info(
    { db: config.db.connection.database, host: config.db.connection.host },
    'MySQL connection pool ready'
  );
}

/** tarn pool stats — replaces the HikariCP stats shown on the admin dashboard. */
function poolStats() {
  const p = knex.client.pool;
  return {
    used: p.numUsed(),
    free: p.numFree(),
    pendingAcquires: p.numPendingAcquires(),
    pendingCreates: p.numPendingCreates(),
    min: config.db.pool.min,
    max: config.db.pool.max,
  };
}

async function close() {
  await knex.destroy();
}

module.exports = { knex, verifyConnection, poolStats, close };
