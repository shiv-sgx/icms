'use strict';

const { knex } = require('./knex');

/**
 * Transaction helper — the counterpart of {@code Db.inTransaction}. Runs `work`
 * inside a single transaction, committing on normal return and rolling back on
 * any thrown error. Repositories accept a query runner (`trx` or `knex`) so
 * services can compose several repo calls atomically:
 *
 *   await withTransaction(async (trx) => {
 *     const id = await claimRepo.insert(trx, claim);
 *     await documentRepo.seed(trx, id, reqs);
 *     return id;
 *   });
 */
function withTransaction(work) {
  return knex.transaction((trx) => work(trx));
}

module.exports = { withTransaction };
