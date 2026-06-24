'use strict';

/** Persistence for `policies` — Knex port of PolicyDao. */

const COLS = [
  'id',
  'policy_no as policyNo',
  'policyholder_id as policyholderId',
  'type',
  'sum_insured as sumInsured',
  'premium',
  'start_date as startDate',
  'expiry_date as expiryDate',
  'ncb_discount as ncbDiscount',
  'status',
];

async function findById(db, id) {
  return (await db('policies').select(COLS).where({ id }).first()) || null;
}

async function findByPolicyholder(db, policyholderId) {
  return db('policies').select(COLS).where({ policyholder_id: policyholderId }).orderBy('policy_no');
}

module.exports = { findById, findByPolicyholder };
