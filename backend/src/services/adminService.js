'use strict';

const { knex, poolStats } = require('../db/knex');
const { withTransaction } = require('../db/tx');
const userRepo = require('../repositories/userRepo');
const roleRepo = require('../repositories/roleRepo');
const configRepo = require('../repositories/configRepo');
const auditRepo = require('../repositories/auditRepo');
const claimRepo = require('../repositories/claimRepo');
const passwords = require('./passwordService');
const audit = require('./auditService');
const { paged } = require('../utils/paging');
const { ValidationError, ConflictError, NotFoundError } = require('../utils/errors');
const logger = require('../config/logger');

/* ---- dashboard ---- */
async function stats() {
  const [users, claims, roles, auditEvents] = await Promise.all([
    userRepo.countAll(knex),
    claimRepo.countAll(knex),
    roleRepo.countAll(knex),
    auditRepo.countAll(knex),
  ]);
  const p = poolStats();
  return {
    users,
    claims,
    roles,
    auditEvents,
    poolActive: p.used,
    poolIdle: p.free,
    poolTotal: p.used + p.free,
  };
}

/* ---- users ---- */
function toUserView(u) {
  // Never expose the password hash.
  const { passwordHash, ...safe } = u;
  return safe;
}

async function searchUsers(q, role, page, size, offset) {
  const [total, rows] = await Promise.all([
    userRepo.countSearch(knex, q, role),
    userRepo.search(knex, q, role, size, offset),
  ]);
  return paged(rows.map(toUserView), page, size, total);
}

async function roles() {
  return roleRepo.findAllWithCounts(knex);
}

async function createUser(admin, draft, rawPassword, ip) {
  if (!draft.username?.trim() || !draft.email?.trim() || !draft.fullName?.trim()) {
    throw new ValidationError('Name, email, and username are required.');
  }
  if (!rawPassword || rawPassword.length < 6) {
    throw new ValidationError('Password must be at least 6 characters.');
  }
  const passwordHash = await passwords.hash(rawPassword);
  return withTransaction(async (trx) => {
    if (await userRepo.existsByUsernameOrEmail(trx, draft.username.trim(), draft.email.trim())) {
      throw new ConflictError('A user with that username or email already exists.');
    }
    const id = await userRepo.insert(trx, {
      fullName: draft.fullName.trim(),
      email: draft.email.trim(),
      username: draft.username.trim(),
      passwordHash,
      roleId: draft.roleId,
      branch: draft.branch ?? null,
      status: 'ACTIVE',
    });
    await writeAudit(trx, admin, 'USER_CREATED', `${draft.username} (id ${id})`, ip);
    logger.info({ admin: admin.username, created: draft.username }, 'User created');
    return id;
  });
}

async function updateUser(admin, userId, status, roleId, ip) {
  return withTransaction(async (trx) => {
    const u = await userRepo.findById(trx, userId);
    if (!u) throw new NotFoundError('User not found.');
    await userRepo.updateStatusAndRole(trx, userId, status, roleId);
    await writeAudit(trx, admin, 'USER_UPDATED', `${u.username} -> ${status}/role:${roleId}`, ip);
  });
}

async function resetPassword(admin, userId, newPassword, ip) {
  if (!newPassword || newPassword.length < 6) {
    throw new ValidationError('Password must be at least 6 characters.');
  }
  const hash = await passwords.hash(newPassword);
  return withTransaction(async (trx) => {
    const u = await userRepo.findById(trx, userId);
    if (!u) throw new NotFoundError('User not found.');
    await userRepo.updatePassword(trx, userId, hash);
    await writeAudit(trx, admin, 'PASSWORD_RESET', u.username, ip);
  });
}

/* ---- config reads ---- */
const slaConfigs = () => configRepo.slaConfigs(knex);
const thresholds = () => configRepo.approvalThresholds(knex);
const templates = () => configRepo.templates(knex);
const documentRequirements = () => configRepo.documentRequirements(knex);

/* ---- config writes ---- */
async function updateSla(admin, id, hours, ip) {
  if (!Number.isInteger(hours) || hours <= 0) {
    throw new ValidationError('SLA hours must be a positive integer.');
  }
  return withTransaction(async (trx) => {
    await configRepo.updateSla(trx, id, hours);
    await writeAudit(trx, admin, 'SLA_UPDATED', `sla:${id} -> ${hours}h`, ip);
  });
}

async function updateThreshold(admin, id, min, max, ip) {
  return withTransaction(async (trx) => {
    await configRepo.updateThreshold(trx, id, min, max);
    await writeAudit(trx, admin, 'THRESHOLD_UPDATED', `threshold:${id}`, ip);
  });
}

async function updateTemplate(admin, id, active, body, ip) {
  return withTransaction(async (trx) => {
    await configRepo.updateTemplate(trx, id, active, body);
    await writeAudit(trx, admin, 'TEMPLATE_UPDATED', `template:${id}`, ip);
  });
}

async function addDocumentRequirement(admin, d, ip) {
  if (!d.claimType?.trim() || !d.docType?.trim()) {
    throw new ValidationError('Claim type and document type are required.');
  }
  return withTransaction(async (trx) => {
    await configRepo.insertDocumentRequirement(trx, {
      claimType: d.claimType.trim(),
      claimSubtype: d.claimSubtype?.trim() || null,
      docType: d.docType.trim(),
      required: d.required,
    });
    await writeAudit(trx, admin, 'DOCREQ_ADDED', `${d.claimType}/${d.docType}`, ip);
  });
}

async function deleteDocumentRequirement(admin, id, ip) {
  return withTransaction(async (trx) => {
    await configRepo.deleteDocumentRequirement(trx, id);
    await writeAudit(trx, admin, 'DOCREQ_DELETED', `docreq:${id}`, ip);
  });
}

/* ---- audit ---- */
async function auditLogs(action, result, page, size, offset) {
  const [total, items] = await Promise.all([
    auditRepo.count(knex, action, result),
    auditRepo.find(knex, action, result, size, offset),
  ]);
  return paged(items, page, size, total);
}

async function auditLogsForExport(action, result) {
  return auditRepo.find(knex, action, result, 5000, 0);
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

module.exports = {
  stats,
  searchUsers,
  roles,
  createUser,
  updateUser,
  resetPassword,
  slaConfigs,
  thresholds,
  templates,
  documentRequirements,
  updateSla,
  updateThreshold,
  updateTemplate,
  addDocumentRequirement,
  deleteDocumentRequirement,
  auditLogs,
  auditLogsForExport,
};
