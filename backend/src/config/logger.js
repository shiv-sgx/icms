'use strict';

const pino = require('pino');
const { config } = require('./env');

/**
 * Structured logger. In dev, pretty-print; in prod, JSON for log shipping.
 * The per-request correlation id is attached by the HTTP logger / middleware
 * (see middleware/correlationId.js) — mirrors the Struts CorrelationIdFilter MDC.
 */
const logger = pino({
  level: config.logLevel,
  base: undefined, // omit pid/hostname noise; add back if needed for ops
  redact: {
    paths: ['req.headers.authorization', 'password', '*.password', 'token'],
    censor: '[redacted]',
  },
  transport:
    config.env === 'development'
      ? { target: 'pino-pretty', options: { colorize: true, translateTime: 'SYS:standard' } }
      : undefined,
});

module.exports = logger;
