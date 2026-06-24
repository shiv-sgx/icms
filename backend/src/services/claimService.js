'use strict';

const { knex } = require('../db/knex');
const { withTransaction } = require('../db/tx');
const claimRepo = require('../repositories/claimRepo');
const policyRepo = require('../repositories/policyRepo');
const policyholderRepo = require('../repositories/policyholderRepo');
const documentService = require('./documentService');
const communicationService = require('./communicationService');
const notificationService = require('./notificationService');
const audit = require('./auditService');
const status = require('../domain/claimStatus');
const { paged } = require('../utils/paging');
const { ValidationError, ConflictError } = require('../utils/errors');
const logger = require('../config/logger');

const AGENT = 'AGENT';

const OPEN_STATUSES = [
  status.DRAFT,
  status.SUBMITTED,
  status.UNDER_REVIEW,
  status.SURVEY_SCHEDULED,
  status.UNDER_ASSESSMENT,
  status.PENDING_APPROVAL,
  status.APPROVED,
  status.SETTLEMENT_PROCESSING,
  status.ON_HOLD,
];
const SETTLED_STATUSES = [status.SETTLED, status.CLOSED];

/** Augment a raw claim row with the view-model fields the JSP getters provided. */
function withStatusView(c) {
  if (!c) return c;
  return { ...c, statusLabel: status.label(c.status), statusPill: status.pill(c.status) };
}

/** Customer-safe projection — omits internal-only fields (notes, fraud, risk, ids). */
function toCustomerClaim(c) {
  if (!c) return null;
  const v = withStatusView(c);
  const {
    internalNotes,
    fraudScore,
    riskLevel,
    agentId,
    surveyorId,
    surveyorName,
    slaDueDate,
    ...safe
  } = v;
  return { ...safe, withdrawable: status.isWithdrawable(c.status) };
}

/* ----------------------------- reads ----------------------------- */

async function resolveCustomer(email) {
  return policyholderRepo.findByEmail(knex, email);
}

async function policiesForCustomer(policyholderId) {
  const rows = await policyRepo.findByPolicyholder(knex, policyholderId);
  return rows.map((p) => ({ ...p, displayLabel: `${p.policyNo} — ${p.type}` }));
}

async function listForCustomer(policyholderId, page, size, offset) {
  const [total, rows] = await Promise.all([
    claimRepo.countByPolicyholder(knex, policyholderId),
    claimRepo.findByPolicyholder(knex, policyholderId, size, offset),
  ]);
  return paged(rows.map(toCustomerClaim), page, size, total);
}

/** [total, open, settled] for the customer dashboard. */
async function customerCounts(policyholderId) {
  const [total, open, settled] = await Promise.all([
    claimRepo.countByPolicyholder(knex, policyholderId),
    claimRepo.countByPolicyholderInStatuses(knex, policyholderId, OPEN_STATUSES),
    claimRepo.countByPolicyholderInStatuses(knex, policyholderId, SETTLED_STATUSES),
  ]);
  return { total, open, settled };
}

/** Returns the claim only if it belongs to this policyholder (authorisation). */
async function getOwnedClaim(policyholderId, claimId) {
  const c = await claimRepo.findById(knex, claimId);
  if (!c || Number(c.policyholderId) !== Number(policyholderId)) return null;
  return c;
}

/** Full customer claim detail bundle: claim + timeline + documents + messages. */
async function customerClaimBundle(policyholderId, claimId) {
  const raw = await getOwnedClaim(policyholderId, claimId);
  if (!raw) return null;
  const [documents, messages] = await Promise.all([
    documentService.forClaim(claimId),
    communicationService.forClaim(claimId),
  ]);
  return {
    claim: toCustomerClaim(raw),
    timeline: status.timeline(raw.status),
    documents,
    messages,
  };
}

/* ----------------------------- writes ----------------------------- */

/**
 * Creates a claim (draft or submitted). Seeds required docs and, on submit,
 * notifies agents + posts a system message — all atomically. Port of
 * ClaimService.createClaim.
 * @returns {Promise<{id:number, claimNo:string}>}
 */
async function createClaim(actor, ph, draft, submit, ip) {
  const year = new Date().getFullYear();
  return withTransaction(async (trx) => {
    const policy = await policyRepo.findById(trx, draft.policyId);
    if (!policy || Number(policy.policyholderId) !== Number(ph.id)) {
      throw new ValidationError('Please select one of your own policies.');
    }

    draft.policyholderId = ph.id;
    if (!draft.claimantName || !draft.claimantName.trim()) draft.claimantName = ph.fullName;
    draft.claimType = policy.type; // claim type follows the policy type
    draft.status = submit ? status.SUBMITTED : status.DRAFT;
    draft.riskLevel = 'LOW';
    draft.fraudScore = 0;
    draft.claimNo = await claimRepo.nextClaimNo(trx, year);

    const claimId = await claimRepo.insert(trx, draft);
    await documentService.seedRequiredDocuments(trx, claimId, draft.claimType, draft.claimSubtype);

    const action = submit ? 'CLAIM_SUBMITTED' : 'CLAIM_DRAFT_SAVED';
    if (submit) {
      await notificationService.notifyRole(
        trx,
        AGENT,
        'ACTION',
        `New claim ${draft.claimNo} submitted and awaiting acknowledgement.`
      );
      await communicationService.system(
        trx,
        claimId,
        'ICMS',
        `Your claim ${draft.claimNo} has been received and is awaiting review.`
      );
    }
    await audit.record(trx, {
      userId: Number(actor.id),
      username: actor.username,
      role: actor.role,
      action,
      entity: draft.claimNo,
      ipAddress: ip,
      result: 'SUCCESS',
    });

    logger.info({ user: actor.username, action, claimNo: draft.claimNo, claimId }, 'Claim created');
    return { id: claimId, claimNo: draft.claimNo };
  });
}

/** Customer withdraws their own claim if status permits. Port of ClaimService.withdraw. */
async function withdraw(actor, policyholderId, claimId, ip) {
  return withTransaction(async (trx) => {
    const c = await claimRepo.findById(trx, claimId);
    if (!c || Number(c.policyholderId) !== Number(policyholderId)) return false; // not owner
    if (!status.isWithdrawable(c.status)) {
      throw new ConflictError('This claim can no longer be withdrawn.');
    }
    await claimRepo.updateStatus(trx, claimId, status.WITHDRAWN);
    await notificationService.notifyRole(
      trx,
      AGENT,
      'INFO',
      `Claim ${c.claimNo} was withdrawn by the customer.`
    );
    await audit.record(trx, {
      userId: Number(actor.id),
      username: actor.username,
      role: actor.role,
      action: 'CLAIM_WITHDRAWN',
      entity: c.claimNo,
      ipAddress: ip,
      result: 'SUCCESS',
    });
    return true;
  });
}

module.exports = {
  resolveCustomer,
  policiesForCustomer,
  listForCustomer,
  customerCounts,
  getOwnedClaim,
  customerClaimBundle,
  createClaim,
  withdraw,
  timeline: status.timeline,
  withStatusView,
  toCustomerClaim,
};
