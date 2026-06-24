'use strict';

const multer = require('multer');
const path = require('path');

/**
 * Multipart upload handling — replaces Struts' commons-fileupload. Files are held
 * in memory (10MB cap) and persisted by documentService after validation, so the
 * stored path is fully controlled by the server (never the client filename).
 */
const ALLOWED_EXT = ['pdf', 'jpg', 'jpeg', 'png', 'gif', 'doc', 'docx'];
const MAX_BYTES = 10 * 1024 * 1024; // 10MB (matches struts.multipart.maxSize)

function extOf(name) {
  const dot = (name || '').lastIndexOf('.');
  return dot < 0 ? '' : name.slice(dot + 1).toLowerCase();
}

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: MAX_BYTES, files: 1 },
  fileFilter: (req, file, cb) => {
    if (!ALLOWED_EXT.includes(extOf(file.originalname))) {
      cb(new Error(`Unsupported file type. Allowed: ${ALLOWED_EXT.join(', ')}`));
      return;
    }
    cb(null, true);
  },
});

/** Strip directory parts and disallow path/control chars (port of DocumentService.sanitize). */
function sanitize(name) {
  if (!name || !name.trim()) return 'upload';
  const base = path.basename(name);
  return base.replace(/[^A-Za-z0-9._-]/g, '_');
}

/**
 * Single-file upload middleware that maps multer errors (size limit, bad type) to a
 * 400 instead of a 500.
 */
function single(field) {
  const { ValidationError } = require('./errors');
  return (req, res, next) =>
    upload.single(field)(req, res, (err) => {
      if (!err) return next();
      if (err instanceof multer.MulterError) {
        const msg =
          err.code === 'LIMIT_FILE_SIZE'
            ? `File too large (max ${MAX_BYTES / (1024 * 1024)}MB).`
            : 'File upload failed.';
        return next(new ValidationError(msg));
      }
      return next(new ValidationError(err.message || 'File upload failed.'));
    });
}

module.exports = { upload, single, sanitize, extOf, ALLOWED_EXT, MAX_BYTES };
