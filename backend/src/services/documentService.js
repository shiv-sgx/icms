'use strict';

const { knex } = require('../db/knex');
const documentRepo = require('../repositories/documentRepo');

/**
 * Claim documents — Node port of DocumentService (read + requirement seeding).
 * Physical file upload (multer) is added in Phase 5.
 */

async function forClaim(claimId) {
  return documentRepo.findByClaim(knex, claimId);
}

/**
 * Seeds the required-document checklist for a new claim — within the caller's tx.
 * Required docs become PENDING; optional ones CONDITIONAL (mirrors ClaimService).
 */
async function seedRequiredDocuments(trx, claimId, type, subtype) {
  const reqs = await documentRepo.findRequirements(trx, type, subtype);
  for (const r of reqs) {
    await documentRepo.insert(trx, {
      claimId,
      docType: r.docType,
      uploadStatus: r.required ? 'PENDING' : 'CONDITIONAL',
      verificationStatus: 'PENDING',
    });
  }
  return reqs.length;
}

module.exports = { forClaim, seedRequiredDocuments };
