'use strict';

const { validationResult } = require('express-validator');
const { ValidationError } = require('../utils/errors');

/**
 * Collects express-validator results and, if any failed, throws a 400
 * ValidationError carrying per-field messages. Place AFTER the validation chain:
 *   router.post('/x', [body('a').notEmpty(), validate], controller)
 */
function validate(req, res, next) {
  const result = validationResult(req);
  if (result.isEmpty()) return next();
  const fields = {};
  for (const e of result.array()) {
    // express-validator v7: e.path holds the field name
    if (!fields[e.path]) fields[e.path] = e.msg;
  }
  return next(new ValidationError('Validation failed', fields));
}

module.exports = { validate };
