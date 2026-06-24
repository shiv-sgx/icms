'use strict';

/**
 * BCrypt password hashing/verification — centralised so the work factor and the
 * library live in one place (login, admin create-user, reset-password). Thin
 * wrapper over utils/bcrypt; mirrors the Java PasswordService.
 */
const { hash, matches } = require('../utils/bcrypt');

module.exports = { hash, matches };
