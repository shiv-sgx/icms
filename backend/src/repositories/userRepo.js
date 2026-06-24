'use strict';

const { knex } = require('../db/knex');

/**
 * User data access — Knex port of JdbcUserDao. Every method accepts a query
 * runner `db` (the shared knex for reads, or a `trx` for writes that must join a
 * transaction), mirroring how the Java DAOs took a Connection.
 */

const SELECT_COLS = [
  'u.id',
  'u.full_name as fullName',
  'u.email',
  'u.username',
  'u.password_hash as passwordHash',
  'u.role_id as roleId',
  'r.name as roleName',
  'u.branch',
  'u.status',
  'u.last_login as lastLogin',
  'u.created_at as createdAt',
];

function base(db) {
  return db('users as u').join('roles as r', 'r.id', 'u.role_id').select(SELECT_COLS);
}

async function findByUsername(db, username) {
  return (await base(db).where('u.username', username).first()) || null;
}

async function findById(db, id) {
  return (await base(db).where('u.id', id).first()) || null;
}

async function updateLastLogin(db, userId) {
  return db('users').where({ id: userId }).update({ last_login: knex.fn.now() });
}

async function findActiveByRole(db, roleName) {
  return base(db).where('r.name', roleName).andWhere('u.status', 'ACTIVE').orderBy('u.full_name');
}

function applyFilters(q, search, roleName) {
  if (search && search.trim()) {
    const like = `%${search.trim()}%`;
    q.where((b) =>
      b.where('u.full_name', 'like', like).orWhere('u.email', 'like', like).orWhere('u.username', 'like', like)
    );
  }
  if (roleName) q.andWhere('r.name', roleName);
  return q;
}

async function search(db, searchTerm, roleName, limit, offset) {
  const q = base(db);
  applyFilters(q, searchTerm, roleName);
  return q.orderBy('u.id').limit(limit).offset(offset);
}

async function countSearch(db, searchTerm, roleName) {
  const q = db('users as u').join('roles as r', 'r.id', 'u.role_id');
  applyFilters(q, searchTerm, roleName);
  const row = await q.count({ c: '*' }).first();
  return Number(row.c);
}

async function insert(db, u) {
  const [id] = await db('users').insert({
    full_name: u.fullName,
    email: u.email,
    username: u.username,
    password_hash: u.passwordHash,
    role_id: u.roleId,
    branch: u.branch ?? null,
    status: u.status || 'ACTIVE',
  });
  return id;
}

async function updateStatusAndRole(db, userId, status, roleId) {
  return db('users').where({ id: userId }).update({ status, role_id: roleId });
}

async function updatePassword(db, userId, passwordHash) {
  return db('users').where({ id: userId }).update({ password_hash: passwordHash });
}

async function existsByUsernameOrEmail(db, username, email) {
  const row = await db('users')
    .where({ username })
    .orWhere({ email })
    .count({ c: '*' })
    .first();
  return Number(row.c) > 0;
}

async function countAll(db) {
  const row = await db('users').count({ c: '*' }).first();
  return Number(row.c);
}

module.exports = {
  findByUsername,
  findById,
  updateLastLogin,
  findActiveByRole,
  search,
  countSearch,
  insert,
  updateStatusAndRole,
  updatePassword,
  existsByUsernameOrEmail,
  countAll,
};
