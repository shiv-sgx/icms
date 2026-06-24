'use strict';

const { knex } = require('../db/knex');
const { withTransaction } = require('../db/tx');
const claimRepo = require('../repositories/claimRepo');
const documentRepo = require('../repositories/documentRepo');
const communicationRepo = require('../repositories/communicationRepo');
const approvalRepo = require('../repositories/approvalRepo');
const assessmentRepo = require('../repositories/assessmentRepo');
const settlementRepo = require('../repositories/settlementRepo');
const approvalService = require('./approvalService');
const notifications = require('./notificationService');
const audit = require('./auditService');
const claimStatus = require('../domain/claimStatus');
const { withStatusView } = require('./claimService');
const { paged } = require('../utils/paging');
const { NotFoundError, ConflictError } = require('../utils/errors');
const logger = require('../config/logger');

const MANAGER = 'MANAGER';

/** Filtered + paginated claim list (agent/manager). */
async function list(status, type, search, page, size, offset) {
  const [total, rows] = await Promise.all([
    claimRepo.countFiltered(knex, status, type, search),
    claimRepo.findFiltered(knex, status, type, search, size, offset),
  ]);
  return paged(rows.map(withStatusView), page, size, total);
}

async function statusCounts() {
  return claimRepo.countByStatus(knex);
}

async function worklist(limit) {
  const rows = await claimRepo.worklist(knex, limit);
  return rows.map(withStatusView);
}

/** Full detail aggregation, or null if the claim doesn't exist. */
async function bundle(claimId) {
  const claim = await claimRepo.findById(knex, claimId);
  if (!claim) return null;
  const [documents, messages, approvals, settlement, assessment] = await Promise.all([
    documentRepo.findByClaim(knex, claimId),
    communicationRepo.findByClaim(knex, claimId),
    approvalRepo.findByClaim(knex, claimId),
    settlementRepo.findByClaim(knex, claimId),
    assessmentRepo.findByClaim(knex, claimId),
  ]);
  const components = assessment ? await assessmentRepo.findComponents(knex, assessment.id) : [];
  return {
    claim: withStatusView(claim),
    documents,
    messages,
    approvals,
    settlement,
    assessment,
    components,
    timeline: claimStatus.timeline(claim.status),
  };
}

/** Agent acknowledges a submitted claim (-> UNDER_REVIEW). */
async function acknowledge(agent, claimId, ip) {
  return withTransaction(async (trx) => {
    const claim = await claimRepo.findById(trx, claimId);
    if (!claim) throw new NotFoundError('Claim not found.');
    if (claim.status !== claimStatus.SUBMITTED) {
      throw new ConflictError('Only submitted claims can be acknowledged.');
    }
    await claimRepo.acknowledge(trx, claimId, Number(agent.id), claimStatus.UNDER_REVIEW);
    await writeAudit(trx, agent, 'CLAIM_ACKNOWLEDGED', claim.claimNo, ip);
  });
}

async function updateNotes(agent, claimId, notes, ip) {
  return withTransaction(async (trx) => {
    const claim = await claimRepo.findById(trx, claimId);
    if (!claim) throw new NotFoundError('Claim not found.');
    await claimRepo.updateInternalNotes(trx, claimId, notes ?? null);
    await writeAudit(trx, agent, 'CLAIM_NOTE_UPDATED', claim.claimNo, ip);
  });
}

function canForward(status) {
  return (
    status === claimStatus.UNDER_REVIEW ||
    status === claimStatus.UNDER_ASSESSMENT ||
    status === claimStatus.SURVEY_SCHEDULED ||
    status === claimStatus.ON_HOLD
  );
}

/**
 * Forwards a claim for approval: builds the chain from the assessed/estimated amount
 * and moves the claim to PENDING_APPROVAL (or APPROVED if within agent authority).
 */
async function forwardForApproval(agent, claimId, ip) {
  return withTransaction(async (trx) => {
    const claim = await claimRepo.findById(trx, claimId);
    if (!claim) throw new NotFoundError('Claim not found.');
    if (!canForward(claim.status)) {
      throw new ConflictError('This claim is not ready to be forwarded for approval.');
    }
    if ((await approvalRepo.countPending(trx, claimId)) > 0) {
      throw new ConflictError('This claim is already awaiting approval.');
    }

    const assessment = await assessmentRepo.findByClaim(trx, claimId);
    const amount = assessment && assessment.netPayable != null ? assessment.netPayable : claim.estimatedLoss;

    const pending = await approvalService.createForwardChain(trx, claimId, agent, amount);
    const newStatus = pending > 0 ? claimStatus.PENDING_APPROVAL : claimStatus.APPROVED;
    await claimRepo.updateStatus(trx, claimId, newStatus);

    if (pending > 0) {
      await notifications.notifyRole(trx, MANAGER, 'ACTION', `Claim ${claim.claimNo} is awaiting your approval.`);
    }
    await writeAudit(trx, agent, 'CLAIM_FORWARDED', `${claim.claimNo} -> ${newStatus}`, ip);
    logger.info({ agent: agent.username, claimNo: claim.claimNo, newStatus }, 'Claim forwarded');
    return newStatus;
  });
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

module.exports = { list, statusCounts, worklist, bundle, acknowledge, updateNotes, forwardForApproval };
