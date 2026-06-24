'use strict';

const { knex } = require('../db/knex');

/** Persistence for the `approvals` chain — Knex port of ApprovalDao. */

const COLS = [
  'a.id',
  'a.claim_id as claimId',
  'a.level',
  'a.approver_id as approverId',
  'a.approver_role as approverRole',
  'a.decision',
  'a.remarks',
  'a.decided_at as decidedAt',
  'a.created_at as createdAt',
  'u.full_name as approverName',
];

function base(db) {
  return db('approvals as a').leftJoin('users as u', 'u.id', 'a.approver_id').select(COLS);
}

async function findByClaim(db, claimId) {
  return base(db).where('a.claim_id', claimId).orderBy('a.level').orderBy('a.id');
}

/** Lowest-level pending approval (the next decision needed), or null. */
async function findNextPending(db, claimId) {
  return (
    (await base(db)
      .where('a.claim_id', claimId)
      .andWhere('a.decision', 'PENDING')
      .orderBy('a.level')
      .orderBy('a.id')
      .first()) || null
  );
}

async function insert(db, a) {
  const [id] = await db('approvals').insert({
    claim_id: a.claimId,
    level: a.level,
    approver_id: a.approverId ?? null,
    approver_role: a.approverRole ?? null,
    decision: a.decision || 'PENDING',
    remarks: a.remarks ?? null,
    decided_at: a.decidedAt ?? null,
  });
  return id;
}

async function decide(db, approvalId, approverId, decision, remarks) {
  return db('approvals')
    .where({ id: approvalId })
    .update({ approver_id: approverId, decision, remarks: remarks ?? null, decided_at: knex.fn.now() });
}

async function countPending(db, claimId) {
  const row = await db('approvals').where({ claim_id: claimId, decision: 'PENDING' }).count({ c: '*' }).first();
  return Number(row.c);
}

module.exports = { findByClaim, findNextPending, insert, decide, countPending };
