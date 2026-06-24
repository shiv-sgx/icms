'use strict';

const { asyncH, ok } = require('../utils/http');
const { NotFoundError, ValidationError } = require('../utils/errors');
const { parsePageParams } = require('../utils/paging');
const surveyorService = require('../services/surveyorService');

const dashboard = asyncH(async (req, res) => {
  const { page, size, offset } = parsePageParams(req.query);
  const [counts, claims] = await Promise.all([
    surveyorService.counts(Number(req.user.id)),
    surveyorService.assignedClaims(Number(req.user.id), page, size, offset),
  ]);
  return ok(res, req, {
    totalAssigned: counts.total,
    pendingSurvey: counts.pendingSurvey,
    assessed: counts.assessed,
    claims,
  });
});

const assessment = asyncH(async (req, res) => {
  const screen = await surveyorService.assessScreen(Number(req.user.id), Number(req.params.id));
  if (!screen) throw new NotFoundError('Claim not found or not assigned to you');
  return ok(res, req, screen);
});

/** Parse the assessment form (mirrors AssessAction parsing). */
function parseAssessment(body) {
  const trimToNull = (v) => (v && String(v).trim() ? String(v).trim() : null);
  const input = {
    visitDate: trimToNull(body.visitDate),
    visitTime: normTime(body.visitTime),
    siteObservations: trimToNull(body.siteObservations),
    reportRefNo: trimToNull(body.reportRefNo),
    policyDeductible: numOrZero(body.policyDeductible),
    depreciationPct: numOrZero(body.depreciationPct),
    salvageValue: numOrZero(body.salvageValue),
    grossAssessed: numOrZero(body.grossAssessed),
    recommendation: trimToNull(body.recommendation),
    remarks: trimToNull(body.remarks),
  };
  if (input.visitDate && !/^\d{4}-\d{2}-\d{2}$/.test(input.visitDate)) {
    throw new ValidationError('Invalid visit date/time.', { visitDate: 'Use YYYY-MM-DD.' });
  }

  // components: array of { component, severity, repairCost, replaceFlag }
  const components = Array.isArray(body.components)
    ? body.components
        .filter((c) => c && String(c.component || '').trim())
        .map((c) => ({
          component: String(c.component).trim(),
          severity: c.severity || 'MODERATE',
          repairCost: numOrZero(c.repairCost),
          replaceFlag: c.replaceFlag === true || c.replaceFlag === 'true',
        }))
    : [];

  return { input, components };
}

function numOrZero(v) {
  if (v === undefined || v === null || String(v).trim() === '') return '0';
  const n = Number(v);
  return Number.isNaN(n) ? '0' : String(v).trim();
}

function normTime(v) {
  if (!v || !String(v).trim()) return null;
  const t = String(v).trim();
  return t.length === 5 ? `${t}:00` : t;
}

const submitAssessment = asyncH(async (req, res) => {
  const { input, components } = parseAssessment(req.body);
  const result = await surveyorService.submitAssessment(req.user, Number(req.params.id), input, components, req.ip);
  return ok(res, req, { ...result, message: 'Assessment submitted. The agent has been notified.' }, 201);
});

module.exports = { dashboard, assessment, submitAssessment };
