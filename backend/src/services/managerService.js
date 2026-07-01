'use strict';

const { knex } = require('../db/knex');
const { withTransaction } = require('../db/tx');
const settlementRepo = require('../repositories/settlementRepo');
const claimRepo = require('../repositories/claimRepo');
const audit = require('./auditService');
const { toAmount } = require('../utils/money');
const { ValidationError, ConflictError } = require('../utils/errors');

/** Manager dashboard stats + settlement override — Node port of ManagerService. */

async function dashboardStats() {
  const q = async (sql) => {
    const [rows] = await knex.raw(sql);
    return Number(Object.values(rows[0])[0]);
  };
  const [pendingApproval, highRisk, slaBreaches, settled] = await Promise.all([
    q("SELECT COUNT(*) FROM claims WHERE status = 'PENDING_APPROVAL'"),
    q("SELECT COUNT(*) FROM claims WHERE risk_level = 'HIGH' AND status NOT IN ('SETTLED','CLOSED','REJECTED','WITHDRAWN')"),
    q("SELECT COUNT(*) FROM claims WHERE sla_due_date < CURDATE() AND status NOT IN ('SETTLED','CLOSED','REJECTED','WITHDRAWN')"),
    q("SELECT COUNT(*) FROM claims WHERE status IN ('SETTLED','CLOSED')"),
  ]);
  return { pendingApproval, highRisk, slaBreaches, settled };
}

async function overrideSettlement(manager, claimId, amount, justification, ip) {
  const amountNum = Number(amount);
  if (amount == null || amount === '' || !Number.isFinite(amountNum) || amountNum <= 0) {
    throw new ValidationError('Enter a valid override amount.');
  }
  const amt = toAmount(amount);
  return withTransaction(async (trx) => {
    const s = await settlementRepo.findByClaim(trx, claimId);
    if (!s) throw new ConflictError('No settlement exists yet to override.');
    await settlementRepo.updateAmount(trx, claimId, amt, justification);
    const claim = await claimRepo.findById(trx, claimId);
    await audit.record(trx, {
      userId: Number(manager.id),
      username: manager.username,
      role: manager.role,
      action: 'SETTLEMENT_OVERRIDE',
      entity: `${claim ? claim.claimNo : 'claim:' + claimId} (₹${amt})`,
      ipAddress: ip,
      result: 'SUCCESS',
    });
  });
}

module.exports = { dashboardStats, overrideSettlement };
