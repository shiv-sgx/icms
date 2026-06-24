'use strict';

/**
 * Persistence for `policyholders` — Knex port of PolicyholderDao. A CUSTOMER user
 * is linked to their policyholder by email (no direct FK in the schema).
 */

const COLS = [
  'id',
  'first_name as firstName',
  'last_name as lastName',
  'dob',
  'email',
  'mobile',
  'address',
  'city',
  'state',
  'pin_code as pinCode',
];

function withFullName(row) {
  if (!row) return null;
  return { ...row, fullName: `${row.firstName} ${row.lastName}` };
}

async function findByEmail(db, email) {
  return withFullName(await db('policyholders').select(COLS).where({ email }).first());
}

async function findById(db, id) {
  return withFullName(await db('policyholders').select(COLS).where({ id }).first());
}

module.exports = { findByEmail, findById };
