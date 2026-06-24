'use strict';

const { asyncH, ok } = require('../utils/http');
const { NotFoundError } = require('../utils/errors');
const { parsePageParams } = require('../utils/paging');
const agentClaimService = require('../services/agentClaimService');
const managerService = require('../services/managerService');
const approvalService = require('../services/approvalService');
const reportService = require('../services/reportService');

const dashboard = asyncH(async (req, res) => {
  const [stats, queue, agentPerformance] = await Promise.all([
    managerService.dashboardStats(),
    agentClaimService.list('PENDING_APPROVAL', null, null, 1, 8, 0),
    reportService.report('agent-performance'),
  ]);
  return ok(res, req, { ...stats, queue: queue.items, agentPerformance });
});

const approvals = asyncH(async (req, res) => {
  const { page, size, offset } = parsePageParams(req.query);
  return ok(res, req, await agentClaimService.list('PENDING_APPROVAL', null, null, page, size, offset));
});

const claimDetail = asyncH(async (req, res) => {
  const bundle = await agentClaimService.bundle(Number(req.params.id));
  if (!bundle) throw new NotFoundError('Claim not found');
  return ok(res, req, bundle);
});

const decide = asyncH(async (req, res) => {
  const status = await approvalService.decide(
    req.user,
    Number(req.params.id),
    req.body.decision,
    req.body.remarks,
    req.ip
  );
  return ok(res, req, { status });
});

const overrideSettlement = asyncH(async (req, res) => {
  await managerService.overrideSettlement(
    req.user,
    Number(req.params.id),
    req.body.amount,
    req.body.justification,
    req.ip
  );
  return ok(res, req, { ok: true });
});

const reports = asyncH(async (req, res) => {
  return ok(res, req, await reportService.allReports());
});

module.exports = { dashboard, approvals, claimDetail, decide, overrideSettlement, reports };
