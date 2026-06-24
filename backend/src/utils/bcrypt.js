'use strict';

const bcrypt = require('bcryptjs');

/**
 * BCrypt hashing/verification — the Node counterpart of the legacy PasswordService
 * (jbcrypt, cost 10). bcryptjs reads $2a/$2b/$2y hashes, so existing
 * users.password_hash values keep authenticating unchanged. New hashes use the
 * same cost so they remain interoperable.
 */
const LOG_ROUNDS = 10;

async function hash(rawPassword) {
  if (!rawPassword) {
    throw new Error('Password must not be empty');
  }
  return bcrypt.hash(rawPassword, LOG_ROUNDS);
}

/** Never throws on malformed input — returns false (mirrors PasswordService.matches). */
async function matches(rawPassword, storedHash) {
  if (!rawPassword || !storedHash) return false;
  try {
    return await bcrypt.compare(rawPassword, storedHash);
  } catch {
    return false;
  }
}

module.exports = { hash, matches, LOG_ROUNDS };
