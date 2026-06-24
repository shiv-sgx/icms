'use strict';

const { config, assertConfig } = require('./config/env');
const logger = require('./config/logger');
const { createApp } = require('./app');
const { verifyConnection, close } = require('./db/knex');

/**
 * Process bootstrap — the counterpart of AppContextListener: validate config,
 * verify the DB pool, start the HTTP server, and wire graceful shutdown.
 */
async function main() {
  assertConfig();
  await verifyConnection();

  const app = createApp();
  const server = app.listen(config.port, config.host, () => {
    logger.info(`ICMS API listening on ${config.host}:${config.port} (env=${config.env})`);
  });

  const shutdown = async (signal) => {
    logger.info(`${signal} received — shutting down`);
    server.close(async () => {
      try {
        await close();
        logger.info('Shutdown complete');
        process.exit(0);
      } catch (e) {
        logger.error({ err: e }, 'Error during shutdown');
        process.exit(1);
      }
    });
    // Force-exit if connections linger
    setTimeout(() => process.exit(1), 10000).unref();
  };

  ['SIGINT', 'SIGTERM'].forEach((sig) => process.on(sig, () => shutdown(sig)));
}

main().catch((err) => {
  logger.error({ err }, 'Fatal startup error');
  process.exit(1);
});
