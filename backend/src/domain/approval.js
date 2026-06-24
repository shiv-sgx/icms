'use strict';

/** Approval decision values — Node port of Approval.java constants. */

const PENDING = 'PENDING';
const APPROVED = 'APPROVED';
const CONDITIONAL = 'CONDITIONAL';
const REJECTED = 'REJECTED';
const RETURNED = 'RETURNED';
const ON_HOLD = 'ON_HOLD';

const DECISIONS = [APPROVED, CONDITIONAL, REJECTED, RETURNED, ON_HOLD];

function isValidDecision(dec) {
  return DECISIONS.includes(dec);
}

module.exports = { PENDING, APPROVED, CONDITIONAL, REJECTED, RETURNED, ON_HOLD, DECISIONS, isValidDecision };
