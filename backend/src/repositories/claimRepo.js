'use strict';

const { knex } = require('../db/knex');

/**
 * Persistence for `claims` — Knex port of ClaimDao. The base select joins the
 * policy number and agent/surveyor display names so list/detail views avoid N+1.
 * Every method takes a query runner (`knex` or a `trx`).
 */

const COLS = [
  'c.id',
  'c.claim_no as claimNo',
  'c.policy_id as policyId',
  'c.policyholder_id as policyholderId',
  'c.claimant_name as claimantName',
  'c.claim_type as claimType',
  'c.claim_subtype as claimSubtype',
  'c.incident_date as incidentDate',
  'c.incident_time as incidentTime',
  'c.incident_location as incidentLocation',
  'c.city',
  'c.state',
  'c.pin_code as pinCode',
  'c.description',
  'c.estimated_loss as estimatedLoss',
  'c.vehicle_reg_no as vehicleRegNo',
  'c.fir_number as firNumber',
  'c.police_station as policeStation',
  'c.hospital_name as hospitalName',
  'c.workshop_name as workshopName',
  'c.third_party as thirdParty',
  'c.status',
  'c.agent_id as agentId',
  'c.surveyor_id as surveyorId',
  'c.risk_level as riskLevel',
  'c.fraud_score as fraudScore',
  'c.internal_notes as internalNotes',
  'c.sla_due_date as slaDueDate',
  'c.filed_at as filedAt',
  'c.updated_at as updatedAt',
  'p.policy_no as policyNo',
  'ag.full_name as agentName',
  'sv.full_name as surveyorName',
];

function base(db) {
  return db('claims as c')
    .join('policies as p', 'p.id', 'c.policy_id')
    .leftJoin('users as ag', 'ag.id', 'c.agent_id')
    .leftJoin('users as sv', 'sv.id', 'c.surveyor_id')
    .select(COLS);
}

async function findById(db, id) {
  return (await base(db).where('c.id', id).first()) || null;
}

async function findByPolicyholder(db, phId, limit, offset) {
  return base(db).where('c.policyholder_id', phId).orderBy('c.filed_at', 'desc').limit(limit).offset(offset);
}

async function countByPolicyholder(db, phId) {
  const row = await db('claims').where({ policyholder_id: phId }).count({ c: '*' }).first();
  return Number(row.c);
}

async function countByPolicyholderInStatuses(db, phId, statuses) {
  if (!statuses || statuses.length === 0) return 0;
  const row = await db('claims')
    .where({ policyholder_id: phId })
    .whereIn('status', statuses)
    .count({ c: '*' })
    .first();
  return Number(row.c);
}

/** Next claim number CLM-YYYY-#### (must run inside the create transaction). */
async function nextClaimNo(db, year) {
  const row = await db('claims').max({ maxId: 'id' }).first();
  const seq = Number(row.maxId || 0) + 1;
  return `CLM-${year}-${String(seq).padStart(4, '0')}`;
}

async function insert(db, c) {
  const [id] = await db('claims').insert({
    claim_no: c.claimNo,
    policy_id: c.policyId,
    policyholder_id: c.policyholderId,
    claimant_name: c.claimantName,
    claim_type: c.claimType,
    claim_subtype: c.claimSubtype ?? null,
    incident_date: c.incidentDate ?? null,
    incident_time: c.incidentTime ?? null,
    incident_location: c.incidentLocation ?? null,
    city: c.city ?? null,
    state: c.state ?? null,
    pin_code: c.pinCode ?? null,
    description: c.description ?? null,
    estimated_loss: c.estimatedLoss ?? '0',
    vehicle_reg_no: c.vehicleRegNo ?? null,
    fir_number: c.firNumber ?? null,
    police_station: c.policeStation ?? null,
    hospital_name: c.hospitalName ?? null,
    workshop_name: c.workshopName ?? null,
    third_party: c.thirdParty ?? null,
    status: c.status,
    risk_level: c.riskLevel || 'LOW',
    fraud_score: c.fraudScore ?? 0,
  });
  return id;
}

async function updateStatus(db, claimId, status) {
  return db('claims').where({ id: claimId }).update({ status });
}

/* ---- agent/manager/surveyor queries (used in later phases) ---- */

function applyFilters(q, status, type, search) {
  if (status) q.where('c.status', status);
  if (type) q.andWhere('c.claim_type', type);
  if (search && search.trim()) {
    const like = `%${search.trim()}%`;
    q.andWhere((b) => b.where('c.claim_no', 'like', like).orWhere('c.claimant_name', 'like', like));
  }
  return q;
}

async function findFiltered(db, status, type, search, limit, offset) {
  const q = base(db);
  applyFilters(q, status, type, search);
  return q.orderBy('c.filed_at', 'desc').limit(limit).offset(offset);
}

async function countFiltered(db, status, type, search) {
  const q = db('claims as c');
  applyFilters(q, status, type, search);
  const row = await q.count({ c: '*' }).first();
  return Number(row.c);
}

async function countByStatus(db) {
  const rows = await db('claims').select('status').count({ n: '*' }).groupBy('status');
  const out = {};
  for (const r of rows) out[r.status] = Number(r.n);
  return out;
}

async function findBySurveyor(db, surveyorId, limit, offset) {
  return base(db).where('c.surveyor_id', surveyorId).orderBy('c.filed_at', 'desc').limit(limit).offset(offset);
}

async function countBySurveyor(db, surveyorId) {
  const row = await db('claims').where({ surveyor_id: surveyorId }).count({ c: '*' }).first();
  return Number(row.c);
}

async function countBySurveyorInStatuses(db, surveyorId, statuses) {
  if (!statuses || statuses.length === 0) return 0;
  const row = await db('claims')
    .where({ surveyor_id: surveyorId })
    .whereIn('status', statuses)
    .count({ c: '*' })
    .first();
  return Number(row.c);
}

async function worklist(db, limit) {
  return base(db)
    .whereIn('c.status', ['SUBMITTED', 'UNDER_REVIEW', 'UNDER_ASSESSMENT', 'APPROVED'])
    .orderBy('c.filed_at', 'asc')
    .limit(limit);
}

async function acknowledge(db, claimId, agentId, status) {
  return db('claims').where({ id: claimId }).update({ agent_id: agentId, status });
}

async function assignSurveyor(db, claimId, surveyorId, agentId, status) {
  return db('claims')
    .where({ id: claimId })
    .update({ surveyor_id: surveyorId, agent_id: knex.raw('COALESCE(agent_id, ?)', [agentId]), status });
}

async function updateInternalNotes(db, claimId, notes) {
  return db('claims').where({ id: claimId }).update({ internal_notes: notes });
}

async function countAll(db) {
  const row = await db('claims').count({ c: '*' }).first();
  return Number(row.c);
}

module.exports = {
  findById,
  findByPolicyholder,
  countByPolicyholder,
  countByPolicyholderInStatuses,
  nextClaimNo,
  insert,
  updateStatus,
  findFiltered,
  countFiltered,
  countByStatus,
  findBySurveyor,
  countBySurveyor,
  countBySurveyorInStatuses,
  worklist,
  acknowledge,
  assignSurveyor,
  updateInternalNotes,
  countAll,
};
