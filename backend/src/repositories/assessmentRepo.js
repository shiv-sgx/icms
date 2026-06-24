'use strict';

/** Persistence for `assessments` + `assessment_components` — Knex port of AssessmentDao. */

const COLS = [
  'a.id',
  'a.claim_id as claimId',
  'a.surveyor_id as surveyorId',
  'a.visit_date as visitDate',
  'a.visit_time as visitTime',
  'a.site_observations as siteObservations',
  'a.report_ref_no as reportRefNo',
  'a.gross_assessed as grossAssessed',
  'a.policy_deductible as policyDeductible',
  'a.depreciation_pct as depreciationPct',
  'a.depreciation_amt as depreciationAmt',
  'a.salvage_value as salvageValue',
  'a.net_payable as netPayable',
  'a.recommendation',
  'a.remarks',
  'a.status',
  'a.created_at as createdAt',
  'u.full_name as surveyorName',
];

const COMP_COLS = [
  'id',
  'assessment_id as assessmentId',
  'component',
  'severity',
  'repair_cost as repairCost',
  'replace_flag as replaceFlag',
];

function base(db) {
  return db('assessments as a').leftJoin('users as u', 'u.id', 'a.surveyor_id').select(COLS);
}

/** Latest assessment for a claim, or null. */
async function findByClaim(db, claimId) {
  return (await base(db).where('a.claim_id', claimId).orderBy('a.id', 'desc').first()) || null;
}

async function findComponents(db, assessmentId) {
  return db('assessment_components').select(COMP_COLS).where({ assessment_id: assessmentId }).orderBy('id');
}

async function insert(db, a) {
  const [id] = await db('assessments').insert({
    claim_id: a.claimId,
    surveyor_id: a.surveyorId ?? null,
    visit_date: a.visitDate ?? null,
    visit_time: a.visitTime ?? null,
    site_observations: a.siteObservations ?? null,
    report_ref_no: a.reportRefNo ?? null,
    gross_assessed: a.grossAssessed ?? '0',
    policy_deductible: a.policyDeductible ?? '0',
    depreciation_pct: a.depreciationPct ?? '0',
    depreciation_amt: a.depreciationAmt ?? '0',
    salvage_value: a.salvageValue ?? '0',
    net_payable: a.netPayable ?? '0',
    recommendation: a.recommendation ?? null,
    remarks: a.remarks ?? null,
    status: a.status,
  });
  return id;
}

async function insertComponent(db, c) {
  return db('assessment_components').insert({
    assessment_id: c.assessmentId,
    component: c.component,
    severity: c.severity || 'MODERATE',
    repair_cost: c.repairCost ?? '0',
    replace_flag: c.replaceFlag ? 1 : 0,
  });
}

module.exports = { findByClaim, findComponents, insert, insertComponent };
