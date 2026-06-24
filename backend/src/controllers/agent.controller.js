'use strict';

const { asyncH, ok } = require('../utils/http');
const { NotFoundError } = require('../utils/errors');
const { parsePageParams } = require('../utils/paging');
const agentClaimService = require('../services/agentClaimService');
const assignmentService = require('../services/assignmentService');
const settlementService = require('../services/settlementService');
const communicationService = require('../services/communicationService');
const communicationRepo = require('../repositories/communicationRepo');
const { knex } = require('../db/knex');
const audit = require('../services/auditService');
const claimStatus = require('../domain/claimStatus');

const c = (counts, k) => counts[k] || 0;

const dashboard = asyncH(async (req, res) => {
  const counts = await agentClaimService.statusCounts();
  const total = Object.values(counts).reduce((a, b) => a + b, 0);
  const terminal =
    c(counts, claimStatus.SETTLED) +
    c(counts, claimStatus.CLOSED) +
    c(counts, claimStatus.REJECTED) +
    c(counts, claimStatus.WITHDRAWN);
  const worklist = await agentClaimService.worklist(10);
  return ok(res, req, {
    openClaims: total - terminal,
    awaitingSurvey: c(counts, claimStatus.SURVEY_SCHEDULED),
    pendingApproval: c(counts, claimStatus.PENDING_APPROVAL),
    settled: c(counts, claimStatus.SETTLED) + c(counts, claimStatus.CLOSED),
    worklist,
  });
});

const listClaims = asyncH(async (req, res) => {
  const { page, size, offset } = parsePageParams(req.query);
  const status = (req.query.status || '').trim() || null;
  const type = (req.query.type || '').trim() || null;
  const q = (req.query.q || '').trim() || null;
  return ok(res, req, await agentClaimService.list(status, type, q, page, size, offset));
});

const claimDetail = asyncH(async (req, res) => {
  const bundle = await agentClaimService.bundle(Number(req.params.id));
  if (!bundle) throw new NotFoundError('Claim not found');
  const surveyors = await assignmentService.availableSurveyors();
  return ok(res, req, { ...bundle, surveyors });
});

const acknowledge = asyncH(async (req, res) => {
  await agentClaimService.acknowledge(req.user, Number(req.params.id), req.ip);
  return ok(res, req, { ok: true });
});

const assignSurveyor = asyncH(async (req, res) => {
  await assignmentService.assignSurveyor(req.user, Number(req.params.id), Number(req.body.surveyorId), req.ip);
  return ok(res, req, { ok: true });
});

const forward = asyncH(async (req, res) => {
  const status = await agentClaimService.forwardForApproval(req.user, Number(req.params.id), req.ip);
  return ok(res, req, { status });
});

const saveNote = asyncH(async (req, res) => {
  await agentClaimService.updateNotes(req.user, Number(req.params.id), req.body.notes, req.ip);
  return ok(res, req, { ok: true });
});

const postMessage = asyncH(async (req, res) => {
  const claimId = Number(req.params.id);
  await communicationService.postMessage(req.user, claimId, req.body.content);
  await audit.success(req.user, 'MESSAGE_SENT', `claim:${claimId}`, req.ip);
  return ok(res, req, { ok: true }, 201);
});

const communications = asyncH(async (req, res) => {
  const messages = await communicationRepo.findRecent(knex, 30);
  return ok(res, req, messages);
});

/* ---- settlement ---- */

const settlement = asyncH(async (req, res) => {
  const screen = await settlementService.settlementScreen(Number(req.params.id));
  if (!screen) throw new NotFoundError('Claim not found');
  return ok(res, req, screen);
});

const processSettlement = asyncH(async (req, res) => {
  const { amount, paymentMethod, accountHolder, bankName, accountNumber, ifscCode, justification } = req.body;
  await settlementService.authorize(
    req.user,
    Number(req.params.id),
    amount,
    { paymentMethod, accountHolder, bankName, accountNumber, ifscCode, justification },
    req.ip
  );
  return ok(res, req, { ok: true });
});

const advanceSettlement = asyncH(async (req, res) => {
  const status = await settlementService.advance(req.user, Number(req.params.id), req.ip);
  return ok(res, req, { status });
});

module.exports = {
  dashboard,
  listClaims,
  claimDetail,
  acknowledge,
  assignSurveyor,
  forward,
  saveNote,
  postMessage,
  communications,
  settlement,
  processSettlement,
  advanceSettlement,
};
