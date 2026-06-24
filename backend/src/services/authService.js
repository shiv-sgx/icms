'use strict';

const { knex } = require('../db/knex');
const { withTransaction } = require('../db/tx');
const userRepo = require('../repositories/userRepo');
const passwords = require('./passwordService');
const audit = require('./auditService');
const jwtUtil = require('../utils/jwt');
const logger = require('../config/logger');

/**
 * Authentication use-case — Node port of AuthService. Verifies credentials,
 * enforces ACTIVE status, records the attempt, stamps last_login, and (on success)
 * issues a JWT. Callers cannot distinguish "no such user" from "wrong password".
 */

function toPublicUser(u) {
  return {
    id: Number(u.id),
    username: u.username,
    fullName: u.fullName,
    email: u.email,
    role: u.roleName,
    branch: u.branch,
  };
}

/**
 * @returns {Promise<{token, user}|null>} null when invalid or not ACTIVE.
 */
async function authenticate(username, rawPassword, ip) {
  if (!username || !rawPassword) return null;
  const uname = String(username).trim();

  const user = await userRepo.findByUsername(knex, uname);

  const ok =
    user &&
    user.status === 'ACTIVE' &&
    (await passwords.matches(rawPassword, user.passwordHash));

  if (!ok) {
    logger.info({ username: uname, ip }, 'Failed login');
    await audit.recordSafe({
      userId: user ? Number(user.id) : null,
      username: uname,
      role: user ? user.roleName : null,
      action: 'LOGIN_FAIL',
      entity: `username:${uname}`,
      result: 'FAILED',
      ip,
    });
    return null;
  }

  await withTransaction((trx) => userRepo.updateLastLogin(trx, user.id));
  await audit.recordSafe({
    userId: Number(user.id),
    username: user.username,
    role: user.roleName,
    action: 'LOGIN',
    entity: `user:${user.username}`,
    result: 'SUCCESS',
    ip,
  });
  logger.info({ username: user.username, role: user.roleName, ip }, 'User logged in');

  const publicUser = toPublicUser(user);
  return { token: jwtUtil.sign({ ...publicUser, roleName: publicUser.role }), user: publicUser };
}

/** Logout is stateless (client drops the token); we only record the audit row. */
async function logout(actor, ip) {
  if (actor) {
    await audit.success(actor, 'LOGOUT', `user:${actor.username}`, ip);
  }
}

module.exports = { authenticate, logout, toPublicUser };
