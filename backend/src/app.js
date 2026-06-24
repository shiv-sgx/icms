'use strict';

const express = require('express');
const helmet = require('helmet');
const cors = require('cors');
const pinoHttp = require('pino-http');

const { config } = require('./config/env');
const logger = require('./config/logger');
const { correlationId } = require('./middleware/correlationId');
const { notFound, errorHandler } = require('./middleware/errorHandler');
const routes = require('./routes');

/**
 * Builds and configures the Express application. Kept separate from server.js so
 * it can be imported by tests (supertest) without binding a port.
 */
function createApp() {
  const app = express();

  app.disable('x-powered-by');
  app.set('trust proxy', true); // honor X-Forwarded-* from a fronting proxy

  app.use(helmet());

  if (config.cors.origins.length > 0) {
    app.use(
      cors({
        origin: config.cors.origins,
        credentials: false, // stateless JWT in Authorization header, not cookies
        allowedHeaders: ['Content-Type', 'Authorization', 'X-Correlation-Id'],
        exposedHeaders: ['X-Correlation-Id'],
      })
    );
  }

  app.use(correlationId);
  app.use(
    pinoHttp({
      logger,
      genReqId: (req) => req.correlationId,
      autoLogging: { ignore: (req) => req.url === '/api/v1/health' },
    })
  );

  app.use(express.json({ limit: '1mb' }));
  app.use(express.urlencoded({ extended: true }));

  // Versioned API surface
  app.use('/api/v1', routes);

  // Optional: serve the built Angular SPA same-origin (production cutover).
  if (config.staticDir) {
    const path = require('path');
    app.use(express.static(config.staticDir));
    // SPA fallback: any non-API GET returns index.html (client-side routing).
    app.get(/^(?!\/api\/).*/, (req, res, next) => {
      if (req.method !== 'GET') return next();
      res.sendFile(path.join(config.staticDir, 'index.html'));
    });
  }

  app.use(notFound);
  app.use(errorHandler);

  return app;
}

module.exports = { createApp };
