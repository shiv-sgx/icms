'use strict';

const { asyncH, ok } = require('../utils/http');
const { NotFoundError, ValidationError } = require('../utils/errors');
const { parsePageParams, paged } = require('../utils/paging');
const claimService = require('../services/claimService');
const communicationService = require('../services/communicationService');
const notificationService = require('../services/notificationService');
const documentService = require('../services/documentService');
const audit = require('../services/auditService');

/** Resolve the logged-in customer's policyholder (linked by email), or null. */
async function resolvePolicyholder(req) {
  return req.user.email ? claimService.resolveCustomer(req.user.email) : null;
}

const dashboard = asyncH(async (req, res) => {
  const ph = await resolvePolicyholder(req);
  let counts = { total: 0, open: 0, settled: 0 };
  let recentClaims = [];
  if (ph) {
    counts = await claimService.customerCounts(ph.id);
    const recent = await claimService.listForCustomer(ph.id, 1, 5, 0);
    recentClaims = recent.items;
  }
  const notifications = await notificationService.recentForUser(Number(req.user.id), req.user.role, 5);
  return ok(res, req, {
    hasProfile: !!ph,
    totalClaims: counts.total,
    openClaims: counts.open,
    settledClaims: counts.settled,
    recentClaims,
    notifications,
  });
});

const listClaims = asyncH(async (req, res) => {
  const ph = await resolvePolicyholder(req);
  const { page, size, offset } = parsePageParams(req.query);
  if (!ph) return ok(res, req, paged([], page, size, 0));
  const result = await claimService.listForCustomer(ph.id, page, size, offset);
  return ok(res, req, result);
});

const claimDetail = asyncH(async (req, res) => {
  const ph = await resolvePolicyholder(req);
  if (!ph) throw new NotFoundError('Claim not found');
  const bundle = await claimService.customerClaimBundle(ph.id, Number(req.params.id));
  if (!bundle) throw new NotFoundError('Claim not found');
  return ok(res, req, bundle);
});

const policies = asyncH(async (req, res) => {
  const ph = await resolvePolicyholder(req);
  return ok(res, req, ph ? await claimService.policiesForCustomer(ph.id) : []);
});

const profile = asyncH(async (req, res) => {
  const ph = await resolvePolicyholder(req);
  return ok(res, req, {
    account: { fullName: req.user.fullName, username: req.user.username, email: req.user.email },
    hasProfile: !!ph,
    policyholder: ph,
    policies: ph ? await claimService.policiesForCustomer(ph.id) : [],
  });
});

/** Parse + validate the new-claim form (mirrors NewClaimAction parsing/validation). */
function parseClaimDraft(body) {
  const fields = {};
  const draft = {};

  const policyId = Number.parseInt(body.policyId, 10);
  if (!Number.isInteger(policyId) || policyId <= 0) fields.policyId = 'Please select a policy.';
  draft.policyId = policyId;

  if (!body.description || !String(body.description).trim()) {
    fields.description = 'Please describe the incident.';
  }

  if (body.incidentDate && String(body.incidentDate).trim()) {
    const d = String(body.incidentDate).trim();
    if (!/^\d{4}-\d{2}-\d{2}$/.test(d)) fields.incidentDate = 'Use the date picker (YYYY-MM-DD).';
    else draft.incidentDate = d;
  }
  if (body.incidentTime && String(body.incidentTime).trim()) {
    const t = String(body.incidentTime).trim();
    if (!/^\d{2}:\d{2}(:\d{2})?$/.test(t)) fields.incidentTime = 'Invalid time.';
    else draft.incidentTime = t.length === 5 ? `${t}:00` : t;
  }
  let loss = '0';
  if (body.estimatedLoss !== undefined && String(body.estimatedLoss).trim() !== '') {
    const n = Number(body.estimatedLoss);
    if (Number.isNaN(n)) fields.estimatedLoss = 'Enter a valid amount.';
    else if (n < 0) fields.estimatedLoss = 'Estimated loss cannot be negative.';
    else loss = String(body.estimatedLoss).trim();
  }
  draft.estimatedLoss = loss;

  const trimToNull = (v) => (v && String(v).trim() ? String(v).trim() : null);
  draft.claimSubtype = trimToNull(body.claimSubtype);
  draft.incidentLocation = trimToNull(body.incidentLocation);
  draft.city = trimToNull(body.city);
  draft.state = trimToNull(body.state);
  draft.pinCode = trimToNull(body.pinCode);
  draft.description = trimToNull(body.description);
  draft.vehicleRegNo = trimToNull(body.vehicleRegNo);
  draft.firNumber = trimToNull(body.firNumber);
  draft.policeStation = trimToNull(body.policeStation);
  draft.hospitalName = trimToNull(body.hospitalName);
  draft.workshopName = trimToNull(body.workshopName);
  draft.thirdParty = trimToNull(body.thirdParty);

  if (Object.keys(fields).length > 0) throw new ValidationError('Validation failed', fields);
  return draft;
}

const createClaim = asyncH(async (req, res) => {
  const ph = await resolvePolicyholder(req);
  if (!ph) throw new ValidationError('No policyholder profile is linked to your account.');
  const draft = parseClaimDraft(req.body);
  const submit = String(req.body.mode || 'submit').toLowerCase() !== 'draft';
  const result = await claimService.createClaim(req.user, ph, draft, submit, req.ip);
  return ok(
    res,
    req,
    {
      ...result,
      message: submit
        ? 'Claim submitted successfully. Track its progress below.'
        : 'Draft saved. You can complete and submit it any time.',
    },
    201
  );
});

const postMessage = asyncH(async (req, res) => {
  const ph = await resolvePolicyholder(req);
  const claimId = Number(req.params.id);
  if (!ph || !(await claimService.getOwnedClaim(ph.id, claimId))) {
    throw new NotFoundError('Claim not found');
  }
  await communicationService.postMessage(req.user, claimId, req.body.content);
  return ok(res, req, { ok: true }, 201);
});

const uploadDocument = asyncH(async (req, res) => {
  const ph = await resolvePolicyholder(req);
  const claimId = Number(req.params.id);
  if (!ph || !(await claimService.getOwnedClaim(ph.id, claimId))) {
    throw new NotFoundError('Claim not found');
  }
  const result = await documentService.upload(claimId, req.body.docType, req.file);
  await audit.success(req.user, 'DOC_UPLOAD', `claim:${claimId} / ${req.body.docType}`, req.ip);
  return ok(res, req, result, 201);
});

const withdraw = asyncH(async (req, res) => {
  const ph = await resolvePolicyholder(req);
  if (!ph) throw new NotFoundError('Claim not found');
  const done = await claimService.withdraw(req.user, ph.id, Number(req.params.id), req.ip);
  if (!done) throw new NotFoundError('Claim not found');
  return ok(res, req, { ok: true });
});

module.exports = {
  dashboard,
  listClaims,
  claimDetail,
  policies,
  profile,
  createClaim,
  postMessage,
  withdraw,
  uploadDocument,
};
