'use strict';

const crypto = require('crypto');

const HEADER = 'x-correlation-id';

/**
 * Assigns a short correlation id to every request and echoes it back — the
 * counterpart of the Struts CorrelationIdFilter. Honors an inbound id (set by a
 * trusted proxy) when present; otherwise generates one. Exposed as req.correlationId
 * and attached to the per-request child logger.
 */
function correlationId(req, res, next) {
  const inbound = req.headers[HEADER];
  const cid =
    typeof inbound === 'string' && inbound.trim()
      ? inbound.trim().slice(0, 64)
      : crypto.randomUUID().slice(0, 8);

  req.correlationId = cid;
  res.setHeader('X-Correlation-Id', cid);
  next();
}

module.exports = { correlationId, HEADER };
