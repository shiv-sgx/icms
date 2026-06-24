'use strict';

const express = require('express');
const { asyncH, ok } = require('../utils/http');
const { knex, poolStats } = require('../db/knex');

const router = express.Router();

/**
 * Liveness/readiness — verifies the DB round-trips and reports pool stats.
 * Returns 200 only when the database is reachable (readiness gate for k8s).
 */
router.get(
  '/health',
  asyncH(async (req, res) => {
    await knex.raw('SELECT 1');
    return ok(res, req, { status: 'UP', db: 'UP', pool: poolStats() });
  })
);

// Feature routers
router.use('/auth', require('./auth.routes'));
// Phase 2+ mount points:
//   router.use('/customer', require('./customer.routes'));
//   router.use('/agent', require('./agent.routes'));
//   ...

module.exports = router;
