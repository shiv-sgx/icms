'use strict';

const { knex } = require('../db/knex');
const { withTransaction } = require('../db/tx');
const settlementRepo = require('../repositories/settlementRepo');
const assessmentRepo = require('../repositories/assessmentRepo');
const claimRepo = require('../repositories/claimRepo');
const communications = require('./communicationService');
const audit = require('./auditService');
const claimStatus = require('../domain/claimStatus');
const settlement = require('../domain/settlement');
const { withStatusView } = require('./claimService');
const { dec, toAmount } = require('../utils/money');
const { NotFoundError, ValidationError, ConflictError } = require('../utils/errors');
const logger = require('../config/logger');

async function forClaim(claimId) {
  return settlementRepo.findByClaim(knex, claimId);
}

/** Settlement screen model: claim + settlement + payment tracker + suggested amount. */
async function settlementScreen(claimId) {
  const claim = await claimRepo.findById(knex, claimId);
  if (!claim) return null;
  const [s, assessment] = await Promise.all([
    settlementRepo.findByClaim(knex, claimId),
    assessmentRepo.findByClaim(knex, claimId),
  ]);
  let suggested;
  if (s && s.finalAmount != null) suggested = s.finalAmount;
  else if (assessment && assessment.netPayable != null) suggested = assessment.netPayable;
  else suggested = claim.estimatedLoss;
  return {
    claim: withStatusView(claim),
    settlement: s,
    tracker: settlement.TRACKER,
    suggestedAmount: toAmount(suggested),
    paymentMethods: settlement.PAYMENT_METHODS,
  };
}

/** Authorise (or re-amount) a settlement. Requires an approved/processing claim. */
async function authorize(actor, claimId, amount, fields, ip) {
  if (amount == null || dec(amount).lessThanOrEqualTo(0)) {
    throw new ValidationError('Enter a valid settlement amount.');
  }
  const amt = toAmount(amount);
  return withTransaction(async (trx) => {
    const claim = await claimRepo.findById(trx, claimId);
    if (!claim) throw new NotFoundError('Claim not found.');
    const existing = await settlementRepo.findByClaim(trx, claimId);
    if (!existing) {
      if (claim.status !== claimStatus.APPROVED) {
        throw new ConflictError('Only approved claims can be settled.');
      }
      await settlementRepo.insert(trx, {
        claimId,
        finalAmount: amt,
        justification: fields.justification,
        paymentMethod: fields.paymentMethod,
        accountHolder: fields.accountHolder,
        bankName: fields.bankName,
        accountNumber: fields.accountNumber,
        ifscCode: fields.ifscCode,
        status: settlement.AUTHORIZED,
        approvedBy: Number(actor.id),
      });
      await claimRepo.updateStatus(trx, claimId, claimStatus.SETTLEMENT_PROCESSING);
      await communications.system(
        trx,
        claimId,
        'ICMS',
        `Settlement of ₹ ${amt} has been authorised for your claim.`
      );
    } else {
      await settlementRepo.updateAmount(trx, claimId, amt, fields.justification);
    }
    await writeAudit(trx, actor, 'SETTLEMENT_AUTHORIZED', `${claim.claimNo} (₹${amt})`, ip);
    logger.info({ actor: actor.username, amount: amt, claimNo: claim.claimNo }, 'Settlement authorised');
  });
}

/** Advance the payment tracker one step; returns the new status (or current if at end). */
async function advance(actor, claimId, ip) {
  return withTransaction(async (trx) => {
    const claim = await claimRepo.findById(trx, claimId);
    const s = await settlementRepo.findByClaim(trx, claimId);
    if (!claim || !s) throw new NotFoundError('No settlement to advance.');
    const nextStatus = settlement.next(s.status);
    if (!nextStatus) return s.status; // already closed / unknown

    await settlementRepo.advance(trx, claimId, nextStatus);

    if (nextStatus === settlement.PAYMENT_CONFIRMED) {
      await claimRepo.updateStatus(trx, claimId, claimStatus.SETTLED);
      await communications.system(
        trx,
        claimId,
        'ICMS',
        `Your claim has been settled. Payment of ₹ ${s.finalAmount} is confirmed.`
      );
    } else if (nextStatus === settlement.CLOSED) {
      await claimRepo.updateStatus(trx, claimId, claimStatus.CLOSED);
    }
    await writeAudit(trx, actor, `SETTLEMENT_${nextStatus}`, claim.claimNo, ip);
    return nextStatus;
  });
}

async function writeAudit(trx, actor, action, entity, ip) {
  return audit.record(trx, {
    userId: Number(actor.id),
    username: actor.username,
    role: actor.role,
    action,
    entity,
    ipAddress: ip,
    result: 'SUCCESS',
  });
}

module.exports = { forClaim, settlementScreen, authorize, advance };
