'use strict';

/** Admin-managed configuration — Knex port of ConfigDao (used by Phases 3 & 4). */

/* ---- approval thresholds ---- */
async function approvalThresholds(db) {
  return db('approval_thresholds')
    .select(['id', 'level', 'label', 'min_amount as minAmount', 'max_amount as maxAmount'])
    .orderBy('min_amount');
}

async function updateThreshold(db, id, min, max) {
  return db('approval_thresholds').where({ id }).update({ min_amount: min, max_amount: max ?? null });
}

/* ---- SLA config ---- */
async function slaConfigs(db) {
  return db('sla_config').select(['id', 'stage', 'hours']).orderBy('id');
}

async function updateSla(db, id, hours) {
  return db('sla_config').where({ id }).update({ hours });
}

/* ---- notification templates ---- */
async function templates(db) {
  return db('notification_templates')
    .select(['id', 'name', 'channel', 'active', 'body'])
    .orderBy('id');
}

async function updateTemplate(db, id, active, body) {
  return db('notification_templates').where({ id }).update({ active: active ? 1 : 0, body });
}

/* ---- document requirements ---- */
async function documentRequirements(db) {
  return db('document_requirements')
    .select(['id', 'claim_type as claimType', 'claim_subtype as claimSubtype', 'doc_type as docType', 'required'])
    .orderBy('claim_type')
    .orderBy('claim_subtype')
    .orderBy('id');
}

async function insertDocumentRequirement(db, d) {
  const [id] = await db('document_requirements').insert({
    claim_type: d.claimType,
    claim_subtype: d.claimSubtype ?? null,
    doc_type: d.docType,
    required: d.required ? 1 : 0,
  });
  return id;
}

async function deleteDocumentRequirement(db, id) {
  return db('document_requirements').where({ id }).del();
}

module.exports = {
  approvalThresholds,
  updateThreshold,
  slaConfigs,
  updateSla,
  templates,
  updateTemplate,
  documentRequirements,
  insertDocumentRequirement,
  deleteDocumentRequirement,
};
