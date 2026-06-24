'use strict';

/**
 * Wraps an async route handler so thrown errors/rejections reach the central
 * error handler instead of crashing the process. Usage: router.get('/', asyncH(fn))
 */
function asyncH(fn) {
  return function wrapped(req, res, next) {
    Promise.resolve(fn(req, res, next)).catch(next);
  };
}

/** Standard success envelope: { data, correlationId }. */
function ok(res, req, data, status = 200) {
  return res.status(status).json({ data, correlationId: req.correlationId });
}

module.exports = { asyncH, ok };
