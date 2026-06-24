'use strict';

/** Audit-trail data access — Knex port of JdbcAuditDao (insert-only log). */

async function insert(db, entry) {
  return db('audit_logs').insert({
    user_id: entry.userId ?? null,
    username: entry.username ?? null,
    role: entry.role ?? null,
    action: entry.action,
    entity: entry.entity ?? null,
    ip_address: entry.ipAddress ?? null,
    result: entry.result || 'SUCCESS',
  });
}

const SELECT_COLS = [
  'id',
  'ts',
  'user_id as userId',
  'username',
  'role',
  'action',
  'entity',
  'ip_address as ipAddress',
  'result',
];

function applyFilters(q, action, result) {
  if (action && action.trim()) q.where('action', action.trim());
  if (result && result.trim()) q.andWhere('result', result.trim());
  return q;
}

async function find(db, action, result, limit, offset) {
  const q = db('audit_logs').select(SELECT_COLS);
  applyFilters(q, action, result);
  return q.orderBy('ts', 'desc').orderBy('id', 'desc').limit(limit).offset(offset);
}

async function count(db, action, result) {
  const q = db('audit_logs');
  applyFilters(q, action, result);
  const row = await q.count({ c: '*' }).first();
  return Number(row.c);
}

async function countAll(db) {
  const row = await db('audit_logs').count({ c: '*' }).first();
  return Number(row.c);
}

module.exports = { insert, find, count, countAll };
