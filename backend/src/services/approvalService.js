'use strict';

const { knex } = require('../db/knex');
const { withTransaction } = require('../db/tx');
const approvalRepo = require('../repositories/approvalRepo');
const claimRepo = require('../repositories/claimRepo');
const configRepo = require('../repositories/configRepo');
const notifications = require('./notificationService');
const audit = require('./auditService');
const approval = require('../domain/approval');
const claimStatus = require('../domain/claimStatus');
const { dec } = require('../utils/money');
const { ValidationError, ConflictError, NotFoundError } = require('../utils/errors');
const logger = require('../config/logger');

async function forClaim(claimId) {
  return approvalRepo.findByClaim(knex, claimId);
}

function levelIndex(level) {
  if (level === 'L3') return 3;
  if (level === 'L2') return 2;
  return 1;
}

/** Highest required level (1..3) for an amount, per configured thresholds. */
async function requiredLevelIndex(trx, amount) {
  const amt = dec(amount);
  let idx = 1;
  const thresholds = await configRepo.approvalThresholds(trx);
  for (const t of thresholds) {
    const aboveMin = t.minAmount === null || amt.greaterThanOrEqualTo(dec(t.minAmount));
    if (aboveMin) idx = levelIndex(t.level);
  }
  return idx;
}

/**
 * Builds the approval chain for a forwarded claim within the caller's transaction.
 * L1 (the forwarding agent) is recorded approved; L2/L3 added pending as the amount
 * requires. Returns the number of pending approvals created.
 */
async function createForwardChain(trx, claimId, agent, amount) {
  const requiredIdx = await requiredLevelIndex(trx, amount);

  await approvalRepo.insert(trx, {
    claimId,
    level: 'L1',
    approverId: Number(agent.id),
    approverRole: 'AGENT',
    decision: approval.APPROVED,
    remarks: 'Initial review complete; forwarded for approval.',
    decidedAt: knex.fn.now(),
  });

  let pending = 0;
  if (requiredIdx >= 2) {
    await approvalRepo.insert(trx, { claimId, level: 'L2', approverRole: 'MANAGER', decision: approval.PENDING });
    pending++;
  }
  if (requiredIdx >= 3) {
    await approvalRepo.insert(trx, { claimId, level: 'L3', approverRole: 'DIRECTOR', decision: approval.PENDING });
    pending++;
  }
  return pending;
}

/**
 * Records a manager's decision on the next pending level and advances the claim.
 * Port of ApprovalService.decide (used by the manager portal, Phase 4).
 */
async function decide(manager, claimId, decision, remarks, ip) {
  const dec_ = (decision || '').trim().toUpperCase();
  if (!approval.isValidDecision(dec_)) {
    throw new ValidationError('Invalid approval decision.');
  }
  return withTransaction(async (trx) => {
    const claim = await claimRepo.findById(trx, claimId);
    if (!claim) throw new NotFoundError('Claim not found.');
    const next = await approvalRepo.findNextPending(trx, claimId);
    if (!next) throw new ConflictError('This claim has no pending approval to decide.');

    await approvalRepo.decide(trx, next.id, Number(manager.id), dec_, remarks);

    let newStatus;
    switch (dec_) {
      case approval.APPROVED:
      case approval.CONDITIONAL:
        newStatus =
          (await approvalRepo.countPending(trx, claimId)) > 0
            ? claimStatus.PENDING_APPROVAL
            : claimStatus.APPROVED;
        break;
      case approval.REJECTED:
        newStatus = claimStatus.REJECTED;
        break;
      case approval.RETURNED:
        newStatus = claimStatus.UNDER_REVIEW;
        break;
      default:
        newStatus = claimStatus.ON_HOLD;
        break;
    }
    await claimRepo.updateStatus(trx, claimId, newStatus);

    if (claim.agentId != null) {
      await notifications.notifyUser(
        trx,
        Number(claim.agentId),
        'ACTION',
        `Claim ${claim.claimNo} was ${claimStatus.label(newStatus)} by ${manager.fullName}.`
      );
    }
    await audit.record(trx, {
      userId: Number(manager.id),
      username: manager.username,
      role: manager.role,
      action: `APPROVAL_${dec_}`,
      entity: `${claim.claimNo} (${next.level})`,
      ipAddress: ip,
      result: 'SUCCESS',
    });
    logger.info({ manager: manager.username, decision: dec_, claimNo: claim.claimNo, newStatus }, 'Approval decided');
    return newStatus;
  });
}

module.exports = { forClaim, createForwardChain, decide };
