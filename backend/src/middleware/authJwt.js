'use strict';

const jwtUtil = require('../utils/jwt');
const { UnauthorizedError } = require('../utils/errors');

/**
 * Authentication gate — Node counterpart of AuthInterceptor. Requires a valid
 * Bearer token; attaches the decoded principal as req.user
 * ({ id, username, role, fullName }). Rejects with 401 otherwise.
 */
function authJwt(req, res, next) {
  const header = req.headers.authorization || '';
  const [scheme, token] = header.split(' ');
  if (scheme !== 'Bearer' || !token) {
    return next(new UnauthorizedError('Missing or malformed Authorization header'));
  }
  try {
    const claims = jwtUtil.verify(token);
    req.user = {
      id: claims.sub,
      username: claims.username,
      role: claims.role,
      fullName: claims.fullName,
    };
    return next();
  } catch {
    return next(new UnauthorizedError('Invalid or expired token'));
  }
}

module.exports = { authJwt };
