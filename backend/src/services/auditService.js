'use strict';

const { knex } = require('../db/knex');
const auditRepo = require('../repositories/auditRepo');
const logger = require('../config/logger');

/**
 * Writes the audit trail — Node port of AuditService. Two modes:
 *  - transactional: pass a `trx` so the state change and its audit row commit atomically.
 *  - fire-and-forget (`record`): own connection; a failure is logged, never thrown,
 *    so it can't break the user's action.
 */

/** Transactional variant — caller owns the trx/commit. */
async function record(trx, entry) {
  return auditRepo.insert(trx, entry);
}

/** Best-effort variant for actions outside a surrounding transaction (e.g. login). */
async function recordSafe({ userId, username, role, action, entity, result, ip }) {
  try {
    await auditRepo.insert(knex, {
      userId,
      username,
      role,
      action,
      entity,
      ipAddress: ip,
      result: result || 'SUCCESS',
    });
  } catch (e) {
    logger.error({ err: e, action, entity, result }, 'Failed to write audit log');
  }
}

/** Convenience: SUCCESS audit for an authenticated actor (req.user). */
async function success(actor, action, entity, ip) {
  return recordSafe({
    userId: actor ? Number(actor.id) : null,
    username: actor ? actor.username : null,
    role: actor ? actor.role : null,
    action,
    entity,
    result: 'SUCCESS',
    ip,
  });
}

module.exports = { record, recordSafe, success };
