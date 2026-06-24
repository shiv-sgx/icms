'use strict';

/** Role data access — Knex port of RoleDao. */

async function findAllSimple(db) {
  return db('roles').select('id', 'name', 'description').orderBy('id');
}

/** Roles with their user counts (admin role-management view). */
async function findAllWithCounts(db) {
  return db('roles as r')
    .leftJoin('users as u', 'u.role_id', 'r.id')
    .select('r.id', 'r.name', 'r.description')
    .count({ userCount: 'u.id' })
    .groupBy('r.id', 'r.name', 'r.description')
    .orderBy('r.id')
    .then((rows) => rows.map((r) => ({ ...r, userCount: Number(r.userCount) })));
}

async function countAll(db) {
  const row = await db('roles').count({ c: '*' }).first();
  return Number(row.c);
}

module.exports = { findAllSimple, findAllWithCounts, countAll };
