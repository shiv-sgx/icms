'use strict';

const fs = require('fs/promises');
const path = require('path');
const { knex } = require('../db/knex');
const { withTransaction } = require('../db/tx');
const documentRepo = require('../repositories/documentRepo');
const { config } = require('../config/env');
const { sanitize, extOf, ALLOWED_EXT } = require('../utils/fileUpload');
const { ValidationError, AppError } = require('../utils/errors');
const logger = require('../config/logger');

/**
 * Claim documents — Node port of DocumentService: list, requirement seeding, and
 * secure upload (stored OUTSIDE the web root; client filename never trusted for the
 * stored path).
 */

async function forClaim(claimId) {
  return documentRepo.findByClaim(knex, claimId);
}

/**
 * Stores an uploaded file (multer in-memory `file`) for a claim and records it.
 * Fills an existing pending slot of the same doc type if present, else inserts.
 */
async function upload(claimId, docType, file) {
  if (!file || !file.buffer) throw new ValidationError('Please choose a file to upload.');
  if (!docType || !docType.trim()) throw new ValidationError('Please specify the document type.');

  const safeName = sanitize(file.originalname);
  if (!ALLOWED_EXT.includes(extOf(safeName))) {
    throw new ValidationError(`Unsupported file type. Allowed: ${ALLOWED_EXT.join(', ')}`);
  }

  const destDir = path.join(config.uploadDir, 'claims', String(claimId));
  const storedName = `${process.hrtime.bigint()}_${safeName}`;
  const dest = path.join(destDir, storedName);
  try {
    await fs.mkdir(destDir, { recursive: true });
    await fs.writeFile(dest, file.buffer);
  } catch (e) {
    logger.error({ err: e, claimId, docType }, 'Failed to store upload');
    throw new AppError('Could not save the uploaded file. Please try again.', 500);
  }

  const dt = docType.trim();
  await withTransaction(async (trx) => {
    const existing = await documentRepo.findByClaim(trx, claimId);
    const slot = existing.find(
      (d) => d.docType.toLowerCase() === dt.toLowerCase() && d.uploadStatus !== 'UPLOADED'
    );
    if (slot) {
      await documentRepo.markUploaded(trx, slot.id, safeName, dest);
    } else {
      await documentRepo.insert(trx, {
        claimId,
        docType: dt,
        fileName: safeName,
        filePath: dest,
        uploadStatus: 'UPLOADED',
        verificationStatus: 'UNDER_REVIEW',
      });
    }
  });
  logger.info({ docType: dt, claimId, dest }, 'Stored document');
  return { fileName: safeName };
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

module.exports = { forClaim, seedRequiredDocuments, upload };
