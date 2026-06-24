'use strict';

const { ForbiddenError, UnauthorizedError } = require('../utils/errors');

const ADMIN = 'ADMIN';

/**
 * Authorization gate — Node counterpart of RoleInterceptor. Allows the request
 * when the principal's role matches one of the required roles, OR the principal
 * is ADMIN (ADMIN can access any namespace, by design). Must run after authJwt.
 */
function roleGuard(...required) {
  const allowed = new Set(required);
  return function guard(req, res, next) {
    if (!req.user) return next(new UnauthorizedError());
    if (req.user.role === ADMIN || allowed.has(req.user.role)) return next();
    return next(new ForbiddenError());
  };
}

module.exports = { roleGuard, ADMIN };
