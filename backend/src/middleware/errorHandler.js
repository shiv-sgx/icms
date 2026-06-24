'use strict';

const logger = require('../config/logger');
const { AppError } = require('../utils/errors');

/** 404 for any unmatched route — emitted as JSON the SPA can handle. */
function notFound(req, res, next) {
  res.status(404).json({
    error: { message: 'Not found' },
    correlationId: req.correlationId,
  });
}

/**
 * Central error handler (must be the LAST middleware). Maps typed AppErrors to
 * their status; anything else becomes a 500 with a generic message so internal
 * details never leak to clients. Full detail is logged with the correlation id.
 */
// eslint-disable-next-line no-unused-vars
function errorHandler(err, req, res, next) {
  const isApp = err instanceof AppError;
  const status = isApp ? err.status : 500;

  if (status >= 500) {
    logger.error({ err, correlationId: req.correlationId, path: req.path }, 'Unhandled error');
  } else {
    logger.warn(
      { msg: err.message, correlationId: req.correlationId, path: req.path },
      'Request error'
    );
  }

  const body = {
    error: {
      message: isApp ? err.message : 'Internal server error',
      ...(isApp && err.fields ? { fields: err.fields } : {}),
    },
    correlationId: req.correlationId,
  };
  res.status(status).json(body);
}

module.exports = { notFound, errorHandler };
