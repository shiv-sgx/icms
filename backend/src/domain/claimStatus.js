'use strict';

/**
 * Claim workflow status values + lifecycle — Node port of ClaimStatus.java.
 * Drives timeline rendering, transition guards, and status pill/label helpers.
 */

const S = {
  DRAFT: 'DRAFT',
  SUBMITTED: 'SUBMITTED',
  UNDER_REVIEW: 'UNDER_REVIEW',
  SURVEY_SCHEDULED: 'SURVEY_SCHEDULED',
  UNDER_ASSESSMENT: 'UNDER_ASSESSMENT',
  PENDING_APPROVAL: 'PENDING_APPROVAL',
  APPROVED: 'APPROVED',
  SETTLEMENT_PROCESSING: 'SETTLEMENT_PROCESSING',
  SETTLED: 'SETTLED',
  CLOSED: 'CLOSED',
  REJECTED: 'REJECTED',
  WITHDRAWN: 'WITHDRAWN',
  ON_HOLD: 'ON_HOLD',
};

/** Happy-path order for timeline rendering / stage comparison. */
const LIFECYCLE = [
  S.SUBMITTED,
  S.UNDER_REVIEW,
  S.SURVEY_SCHEDULED,
  S.UNDER_ASSESSMENT,
  S.PENDING_APPROVAL,
  S.APPROVED,
  S.SETTLEMENT_PROCESSING,
  S.SETTLED,
  S.CLOSED,
];

const WITHDRAWABLE = new Set([
  S.DRAFT,
  S.SUBMITTED,
  S.UNDER_REVIEW,
  S.SURVEY_SCHEDULED,
  S.UNDER_ASSESSMENT,
  S.PENDING_APPROVAL,
  S.ON_HOLD,
]);

function lifecycleIndex(status) {
  return LIFECYCLE.indexOf(status);
}

function isTerminal(status) {
  return status === S.CLOSED || status === S.REJECTED || status === S.WITHDRAWN;
}

function isWithdrawable(status) {
  return !!status && WITHDRAWABLE.has(status);
}

/** PENDING_APPROVAL -> "Pending Approval". */
function label(status) {
  if (!status) return '';
  return status
    .toLowerCase()
    .split('_')
    .filter(Boolean)
    .map((p) => p.charAt(0).toUpperCase() + p.slice(1))
    .join(' ');
}

/** Pill CSS class (matches icms.css). */
function pill(status) {
  switch (status) {
    case S.SETTLED:
    case S.APPROVED:
    case S.CLOSED:
      return 'pill-ok';
    case S.REJECTED:
    case S.WITHDRAWN:
      return 'pill-danger';
    case S.ON_HOLD:
    case S.PENDING_APPROVAL:
      return 'pill-warn';
    case S.DRAFT:
      return 'pill-muted';
    default:
      return 'pill-info';
  }
}

/**
 * Builds the status timeline view model — port of ClaimService.timeline().
 * Returns [{ label, state: 'done'|'current'|'pending' }].
 */
function timeline(status) {
  const stages = [];
  if (status === S.DRAFT) {
    stages.push({ label: 'Draft', state: 'current' });
    for (const s of LIFECYCLE) stages.push({ label: label(s), state: 'pending' });
    return stages;
  }

  let curIdx;
  if (status === S.ON_HOLD) {
    curIdx = LIFECYCLE.indexOf(S.PENDING_APPROVAL);
  } else if (status === S.REJECTED || status === S.WITHDRAWN) {
    curIdx = -1; // off-path terminal; the pill conveys the real state
  } else {
    curIdx = lifecycleIndex(status);
  }

  for (let i = 0; i < LIFECYCLE.length; i++) {
    let state;
    if (curIdx < 0) state = 'pending';
    else if (i < curIdx) state = 'done';
    else if (i === curIdx) state = 'current';
    else state = 'pending';
    stages.push({ label: label(LIFECYCLE[i]), state });
  }
  return stages;
}

module.exports = {
  ...S,
  STATUS: S,
  LIFECYCLE,
  lifecycleIndex,
  isTerminal,
  isWithdrawable,
  label,
  pill,
  timeline,
};
