'use strict';

const jwt = require('jsonwebtoken');
const { config } = require('../config/env');

/**
 * Stateless auth tokens. Claims mirror what the Struts SessionUser carried:
 * sub (user id), username, role, fullName. HS256, short TTL (JWT_TTL).
 */
function sign(user) {
  const payload = {
    sub: String(user.id),
    username: user.username,
    role: user.roleName || user.role,
    fullName: user.fullName,
  };
  return jwt.sign(payload, config.jwt.secret, { expiresIn: config.jwt.ttl });
}

/** Verifies + decodes; throws on invalid/expired (caller maps to 401). */
function verify(token) {
  return jwt.verify(token, config.jwt.secret, { clockTolerance: 5 });
}

module.exports = { sign, verify };
