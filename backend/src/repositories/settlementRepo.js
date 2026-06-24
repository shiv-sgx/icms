'use strict';

const { knex } = require('../db/knex');
const settlement = require('../domain/settlement');

/** Persistence for `settlements` — Knex port of SettlementDao. */

const COLS = [
  'id',
  'claim_id as claimId',
  'final_amount as finalAmount',
  'justification',
  'payment_method as paymentMethod',
  'account_holder as accountHolder',
  'bank_name as bankName',
  'account_number as accountNumber',
  'ifsc_code as ifscCode',
  'status',
  'approved_by as approvedBy',
  'approval_date as approvalDate',
  'payment_initiated_at as paymentInitiatedAt',
  'payment_confirmed_at as paymentConfirmedAt',
  'closed_at as closedAt',
  'created_at as createdAt',
];

async function findByClaim(db, claimId) {
  return (await db('settlements').select(COLS).where({ claim_id: claimId }).first()) || null;
}

async function insert(db, s) {
  const [id] = await db('settlements').insert({
    claim_id: s.claimId,
    final_amount: s.finalAmount,
    justification: s.justification ?? null,
    payment_method: s.paymentMethod || 'NEFT',
    account_holder: s.accountHolder ?? null,
    bank_name: s.bankName ?? null,
    account_number: s.accountNumber ?? null,
    ifsc_code: s.ifscCode ?? null,
    status: s.status || 'AUTHORIZED',
    approved_by: s.approvedBy ?? null,
    approval_date: knex.fn.now(),
  });
  return id;
}

async function updateAmount(db, claimId, amount, justification) {
  return db('settlements').where({ claim_id: claimId }).update({ final_amount: amount, justification: justification ?? null });
}

/** Advances the payment status and stamps the matching timestamp column. */
async function advance(db, claimId, status) {
  const stampCol = {
    [settlement.PAYMENT_INITIATED]: 'payment_initiated_at',
    [settlement.PAYMENT_CONFIRMED]: 'payment_confirmed_at',
    [settlement.CLOSED]: 'closed_at',
  }[status];
  const patch = { status };
  if (stampCol) patch[stampCol] = knex.fn.now();
  return db('settlements').where({ claim_id: claimId }).update(patch);
}

module.exports = { findByClaim, insert, updateAmount, advance };
