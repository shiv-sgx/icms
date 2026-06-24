'use strict';

/**
 * Typed application errors. Controllers/services throw these; the central
 * errorHandler maps them to standardized JSON responses without leaking
 * internal details (mirrors the Struts global exception mapping + denied/error).
 */
class AppError extends Error {
  constructor(message, status = 500, fields = undefined) {
    super(message);
    this.name = this.constructor.name;
    this.status = status;
    if (fields) this.fields = fields;
    Error.captureStackTrace?.(this, this.constructor);
  }
}

class ValidationError extends AppError {
  constructor(message = 'Validation failed', fields = undefined) {
    super(message, 400, fields);
  }
}

class UnauthorizedError extends AppError {
  constructor(message = 'Authentication required') {
    super(message, 401);
  }
}

class ForbiddenError extends AppError {
  constructor(message = 'Access denied') {
    super(message, 403);
  }
}

class NotFoundError extends AppError {
  constructor(message = 'Resource not found') {
    super(message, 404);
  }
}

class ConflictError extends AppError {
  constructor(message = 'Conflict') {
    super(message, 409);
  }
}

module.exports = {
  AppError,
  ValidationError,
  UnauthorizedError,
  ForbiddenError,
  NotFoundError,
  ConflictError,
};
