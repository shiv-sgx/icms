'use strict';

const { knex } = require('../db/knex');
const { withTransaction } = require('../db/tx');
const claimRepo = require('../repositories/claimRepo');
const assessmentRepo = require('../repositories/assessmentRepo');
const documentService = require('./documentService');
const notifications = require('./notificationService');
const audit = require('./auditService');
const claimStatus = require('../domain/claimStatus');
const { withStatusView } = require('./claimService');
const { Decimal, dec, toAmount } = require('../utils/money');
const { paged } = require('../utils/paging');
const { NotFoundError, ValidationError, ConflictError } = require('../utils/errors');
const logger = require('../config/logger');

const ASSESSMENT_SUBMITTED = 'SUBMITTED';

const PENDING_STATUSES = [claimStatus.SURVEY_SCHEDULED];
const ASSESSED_STATUSES = [
  claimStatus.UNDER_ASSESSMENT,
  claimStatus.PENDING_APPROVAL,
  claimStatus.APPROVED,
  claimStatus.SETTLEMENT_PROCESSING,
  claimStatus.SETTLED,
  claimStatus.CLOSED,
];

async function assignedClaims(surveyorId, page, size, offset) {
  const [total, rows] = await Promise.all([
    claimRepo.countBySurveyor(knex, surveyorId),
    claimRepo.findBySurveyor(knex, surveyorId, size, offset),
  ]);
  return paged(rows.map(withStatusView), page, size, total);
}

/** {total, pendingSurvey, assessed} for the surveyor dashboard. */
async function counts(surveyorId) {
  const [total, pendingSurvey, assessed] = await Promise.all([
    claimRepo.countBySurveyor(knex, surveyorId),
    claimRepo.countBySurveyorInStatuses(knex, surveyorId, PENDING_STATUSES),
    claimRepo.countBySurveyorInStatuses(knex, surveyorId, ASSESSED_STATUSES),
  ]);
  return { total, pendingSurvey, assessed };
}

/** Claim only if assigned to this surveyor (authorisation). */
async function getAssignedClaim(surveyorId, claimId) {
  const c = await claimRepo.findById(knex, claimId);
  if (!c || c.surveyorId == null || Number(c.surveyorId) !== Number(surveyorId)) return null;
  return c;
}

/** Assessment screen model: claim + latest assessment + components + documents. */
async function assessScreen(surveyorId, claimId) {
  const claim = await getAssignedClaim(surveyorId, claimId);
  if (!claim) return null;
  const assessment = await assessmentRepo.findByClaim(knex, claimId);
  const components = assessment ? await assessmentRepo.findComponents(knex, assessment.id) : [];
  const documents = await documentService.forClaim(claimId);
  return { claim: withStatusView(claim), assessment, components, documents };
}

/**
 * Persists a submitted assessment. Computes gross/depreciation/net payable
 * server-side (never trusts the client). Moves the claim to UNDER_ASSESSMENT and
 * notifies the handling agent. Port of SurveyorService.submitAssessment.
 */
async function submitAssessment(surveyor, claimId, input, components, ip) {
  let gross = sumRepairCosts(components);
  if (gross.lessThanOrEqualTo(0) && dec(input.grossAssessed).lessThanOrEqualTo(0)) {
    throw new ValidationError('Add at least one component (or a gross assessed amount).');
  }
  if (gross.lessThanOrEqualTo(0)) gross = dec(input.grossAssessed);

  const deductible = dec(input.policyDeductible);
  const deprPct = dec(input.depreciationPct);
  const salvage = dec(input.salvageValue);
  const deprAmt = gross.times(deprPct).dividedBy(100).toDecimalPlaces(2, Decimal.ROUND_HALF_UP);
  let net = gross.minus(deductible).minus(deprAmt).minus(salvage);
  if (net.isNegative()) net = new Decimal(0);

  const record = {
    ...input,
    grossAssessed: gross.toFixed(2),
    depreciationAmt: deprAmt.toFixed(2),
    policyDeductible: toAmount(deductible),
    depreciationPct: toAmount(deprPct),
    salvageValue: toAmount(salvage),
    netPayable: net.toFixed(2),
    surveyorId: Number(surveyor.id),
    claimId,
    status: ASSESSMENT_SUBMITTED,
  };
  const netFinal = net.toFixed(2);

  await withTransaction(async (trx) => {
    const claim = await claimRepo.findById(trx, claimId);
    if (!claim || claim.surveyorId == null || Number(claim.surveyorId) !== Number(surveyor.id)) {
      throw new NotFoundError('This claim is not assigned to you.');
    }
    if (claimStatus.isTerminal(claim.status)) throw new ConflictError('This claim is closed.');
    const existing = await assessmentRepo.findByClaim(trx, claimId);
    if (existing && existing.status === ASSESSMENT_SUBMITTED) {
      throw new ConflictError('An assessment has already been submitted for this claim.');
    }

    const assessmentId = await assessmentRepo.insert(trx, record);
    for (const c of components || []) {
      await assessmentRepo.insertComponent(trx, { ...c, assessmentId });
    }
    await claimRepo.updateStatus(trx, claimId, claimStatus.UNDER_ASSESSMENT);

    if (claim.agentId != null) {
      await notifications.notifyUser(
        trx,
        Number(claim.agentId),
        'ACTION',
        `Survey report uploaded for claim ${claim.claimNo} (net payable ₹ ${netFinal}).`
      );
    }
    await audit.record(trx, {
      userId: Number(surveyor.id),
      username: surveyor.username,
      role: surveyor.role,
      action: 'ASSESSMENT_SUBMITTED',
      entity: claim.claimNo,
      ipAddress: ip,
      result: 'SUCCESS',
    });
    logger.info({ surveyor: surveyor.username, claimNo: claim.claimNo, net: netFinal }, 'Assessment submitted');
  });

  return { netPayable: netFinal };
}

function sumRepairCosts(components) {
  let sum = new Decimal(0);
  for (const c of components || []) sum = sum.plus(dec(c.repairCost));
  return sum;
}

module.exports = {
  assignedClaims,
  counts,
  getAssignedClaim,
  assessScreen,
  submitAssessment,
};
