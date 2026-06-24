'use strict';

const { asyncH, ok } = require('../utils/http');
const { parsePageParams } = require('../utils/paging');
const adminService = require('../services/adminService');

/* ---- dashboard ---- */
const dashboard = asyncH(async (req, res) => ok(res, req, await adminService.stats()));

/* ---- users ---- */
const listUsers = asyncH(async (req, res) => {
  const { page, size, offset } = parsePageParams(req.query);
  const q = (req.query.q || '').trim() || null;
  const role = (req.query.role || '').trim() || null;
  const [users, roles] = await Promise.all([
    adminService.searchUsers(q, role, page, size, offset),
    adminService.roles(),
  ]);
  return ok(res, req, { users, roles });
});

const createUser = asyncH(async (req, res) => {
  const { fullName, email, username, password, roleId, branch } = req.body;
  const id = await adminService.createUser(
    req.user,
    { fullName, email, username, roleId: Number(roleId), branch },
    password,
    req.ip
  );
  return ok(res, req, { id }, 201);
});

const updateUser = asyncH(async (req, res) => {
  await adminService.updateUser(req.user, Number(req.params.id), req.body.status, Number(req.body.roleId), req.ip);
  return ok(res, req, { ok: true });
});

const resetPassword = asyncH(async (req, res) => {
  await adminService.resetPassword(req.user, Number(req.params.id), req.body.newPassword, req.ip);
  return ok(res, req, { ok: true });
});

const roles = asyncH(async (req, res) => ok(res, req, await adminService.roles()));

/* ---- config ---- */
const sla = asyncH(async (req, res) => ok(res, req, await adminService.slaConfigs()));
const updateSla = asyncH(async (req, res) => {
  await adminService.updateSla(req.user, Number(req.params.id), Number(req.body.hours), req.ip);
  return ok(res, req, { ok: true });
});

const thresholds = asyncH(async (req, res) => ok(res, req, await adminService.thresholds()));
const updateThreshold = asyncH(async (req, res) => {
  const min = req.body.minAmount;
  const max = req.body.maxAmount === '' || req.body.maxAmount == null ? null : req.body.maxAmount;
  await adminService.updateThreshold(req.user, Number(req.params.id), min, max, req.ip);
  return ok(res, req, { ok: true });
});

const templates = asyncH(async (req, res) => ok(res, req, await adminService.templates()));
const updateTemplate = asyncH(async (req, res) => {
  await adminService.updateTemplate(req.user, Number(req.params.id), !!req.body.active, req.body.body, req.ip);
  return ok(res, req, { ok: true });
});

const documents = asyncH(async (req, res) => ok(res, req, await adminService.documentRequirements()));
const addDocument = asyncH(async (req, res) => {
  const { claimType, claimSubtype, docType, required } = req.body;
  await adminService.addDocumentRequirement(req.user, { claimType, claimSubtype, docType, required: !!required }, req.ip);
  return ok(res, req, { ok: true }, 201);
});
const deleteDocument = asyncH(async (req, res) => {
  await adminService.deleteDocumentRequirement(req.user, Number(req.params.id), req.ip);
  return ok(res, req, { ok: true });
});

/* ---- audit ---- */
const audit = asyncH(async (req, res) => {
  const { page, size, offset } = parsePageParams(req.query);
  const action = (req.query.action || '').trim() || null;
  const result = (req.query.result || '').trim() || null;
  return ok(res, req, await adminService.auditLogs(action, result, page, size, offset));
});

module.exports = {
  dashboard,
  listUsers,
  createUser,
  updateUser,
  resetPassword,
  roles,
  sla,
  updateSla,
  thresholds,
  updateThreshold,
  templates,
  updateTemplate,
  documents,
  addDocument,
  deleteDocument,
  audit,
};
