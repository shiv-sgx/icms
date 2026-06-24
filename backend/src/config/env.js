'use strict';

/**
 * Centralised, externalised configuration (12-Factor) — the Node counterpart of
 * the legacy {@code AppConfig.java}. Resolution order for any key, highest first:
 *   1. Environment variable (DB_URL for key "db.url")
 *   2. process default below
 *
 * The dot->underscore->uppercase convention is preserved so the SAME env vars
 * drive both the Struts app and this service during migration.
 */

const os = require('os');
const path = require('path');
require('dotenv').config();

/** Read a key like "db.pool.max" from env var DB_POOL_MAX, else fallback. */
function get(key, fallback = undefined) {
  const envKey = key.toUpperCase().replace(/\./g, '_');
  const v = process.env[envKey];
  return v === undefined || v === '' ? fallback : v;
}

function getInt(key, fallback) {
  const v = get(key);
  if (v === undefined) return fallback;
  const n = Number.parseInt(String(v).trim(), 10);
  return Number.isNaN(n) ? fallback : n;
}

function require_(key) {
  const v = get(key);
  if (v === undefined) {
    const envKey = key.toUpperCase().replace(/\./g, '_');
    throw new Error(`Required configuration '${key}' is missing (set env ${envKey})`);
  }
  return v;
}

/** Expand a leading ${user.home} token the way the Struts DocumentService does. */
function expandHome(p) {
  if (!p) return p;
  return p.replace('${user.home}', os.homedir());
}

/**
 * Parse a JDBC-style MySQL URL (jdbc:mysql://host:port/db?params) into parts.
 * Falls back to discrete DB_HOST/DB_PORT/DB_NAME when no URL is provided.
 */
function parseDbConnection() {
  const url = get('db.url');
  let host = get('db.host', '127.0.0.1');
  let port = getInt('db.port', 3306);
  let database = get('db.name', 'icms');

  if (url) {
    // Strip leading "jdbc:" so the WHATWG URL parser can read it.
    const normalized = url.replace(/^jdbc:/, '');
    try {
      const u = new URL(normalized);
      if (u.hostname) host = u.hostname;
      if (u.port) port = Number.parseInt(u.port, 10);
      const pathName = (u.pathname || '').replace(/^\//, '');
      if (pathName) database = pathName;
    } catch {
      // Leave discrete defaults if the URL is malformed.
    }
  }

  return {
    host,
    port,
    database,
    user: get('db.user', 'root'),
    password: get('db.password', ''),
    charset: 'utf8mb4',
    // DECIMAL/BIGINT returned as strings to preserve precision (see money.js).
    decimalNumbers: false,
    supportBigNumbers: true,
    bigNumberStrings: true,
    // DATE/DATETIME/TIME returned as the stored strings (no JS Date timezone shift),
    // matching how the Struts app rendered LocalDate/LocalDateTime values.
    dateStrings: true,
    timezone: 'Z',
  };
}

const config = Object.freeze({
  env: get('node.env', process.env.NODE_ENV || 'development'),
  port: getInt('port', 3000),
  // Bind address. 0.0.0.0 listens on all IPv4 interfaces (reachable via LAN IP);
  // set HOST=127.0.0.1 to restrict to localhost only.
  host: get('host', '0.0.0.0'),
  logLevel: get('log.level', 'info'),

  db: {
    connection: parseDbConnection(),
    pool: {
      min: getInt('db.pool.min', 2),
      max: getInt('db.pool.max', 10),
      // tarn timeouts (ms) — mirror HikariCP intent
      acquireTimeoutMillis: getInt('db.pool.connectionTimeoutMs', 30000),
    },
  },

  pageSize: getInt('icms.page.size', 15),
  uploadDir: expandHome(get('icms.upload.dir', path.join(os.homedir(), 'icms-uploads'))),

  cors: {
    origins: get('cors.origins', '')
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean),
  },

  jwt: {
    secret: get('jwt.secret'), // validated at startup (see assertConfig)
    ttl: get('jwt.ttl', '8h'),
  },

  // Optional: absolute path to the built Angular SPA (dist/frontend/browser).
  // When set, Node serves the SPA same-origin (production cutover) with /api/* still routed.
  staticDir: expandHome(get('static.dir', '')),
});

/** Fail fast at boot if a security-critical secret is missing/insecure. */
function assertConfig() {
  if (!config.jwt.secret) {
    throw new Error("Required configuration 'jwt.secret' is missing (set env JWT_SECRET)");
  }
  if (config.env === 'production' && config.jwt.secret === 'change-me-in-every-environment') {
    throw new Error('JWT_SECRET must not be the placeholder value in production');
  }
}

module.exports = { config, get, getInt, require: require_, expandHome, assertConfig };
