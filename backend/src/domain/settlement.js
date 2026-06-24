'use strict';

/** Settlement payment states + tracker order — Node port of Settlement.java. */

const AUTHORIZED = 'AUTHORIZED';
const PAYMENT_INITIATED = 'PAYMENT_INITIATED';
const BANK_PROCESSING = 'BANK_PROCESSING';
const PAYMENT_CONFIRMED = 'PAYMENT_CONFIRMED';
const CLAIMANT_NOTIFIED = 'CLAIMANT_NOTIFIED';
const CLOSED = 'CLOSED';

const TRACKER = [
  AUTHORIZED,
  PAYMENT_INITIATED,
  BANK_PROCESSING,
  PAYMENT_CONFIRMED,
  CLAIMANT_NOTIFIED,
  CLOSED,
];

const PAYMENT_METHODS = ['NEFT', 'CHEQUE', 'DEMAND_DRAFT', 'DIRECT_TO_WORKSHOP'];

/** Next state after `status`, or null if already at the end / unknown. */
function next(status) {
  const idx = TRACKER.indexOf(status);
  if (idx < 0 || idx >= TRACKER.length - 1) return null;
  return TRACKER[idx + 1];
}

module.exports = {
  AUTHORIZED,
  PAYMENT_INITIATED,
  BANK_PROCESSING,
  PAYMENT_CONFIRMED,
  CLAIMANT_NOTIFIED,
  CLOSED,
  TRACKER,
  PAYMENT_METHODS,
  next,
};
