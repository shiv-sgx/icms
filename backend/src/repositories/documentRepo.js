'use strict';

/** Persistence for `claim_documents` + `document_requirements` — port of DocumentDao. */

const DOC_COLS = [
  'id',
  'claim_id as claimId',
  'doc_type as docType',
  'file_name as fileName',
  'file_path as filePath',
  'upload_status as uploadStatus',
  'verification_status as verificationStatus',
  'uploaded_at as uploadedAt',
];

async function findByClaim(db, claimId) {
  return db('claim_documents').select(DOC_COLS).where({ claim_id: claimId }).orderBy('id');
}

async function findById(db, id) {
  return (await db('claim_documents').select(DOC_COLS).where({ id }).first()) || null;
}

async function insert(db, d) {
  const [id] = await db('claim_documents').insert({
    claim_id: d.claimId,
    doc_type: d.docType,
    file_name: d.fileName ?? null,
    file_path: d.filePath ?? null,
    upload_status: d.uploadStatus || 'PENDING',
    verification_status: d.verificationStatus || 'PENDING',
  });
  return id;
}

/** Marks an existing slot (or any doc) as uploaded. */
async function markUploaded(db, docId, fileName, filePath) {
  const { knex } = require('../db/knex');
  return db('claim_documents').where({ id: docId }).update({
    file_name: fileName,
    file_path: filePath,
    upload_status: 'UPLOADED',
    verification_status: 'UNDER_REVIEW',
    uploaded_at: knex.fn.now(),
  });
}

/** Required-doc rows for a claim type (subtype-specific OR type-wide). */
async function findRequirements(db, claimType, subtype) {
  return db('document_requirements')
    .select(['id', 'claim_type as claimType', 'claim_subtype as claimSubtype', 'doc_type as docType', 'required'])
    .where({ claim_type: claimType })
    .andWhere((b) => b.where({ claim_subtype: subtype ?? null }).orWhereNull('claim_subtype'))
    .orderBy('id');
}

module.exports = { findByClaim, findById, insert, markUploaded, findRequirements };
