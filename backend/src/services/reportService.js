'use strict';

const reportRepo = require('../repositories/reportRepo');

/**
 * Builds the manager Reports & Analytics tables — Node port of ReportService.
 * Each report has a stable key so the CSV-export endpoint can request exactly one.
 * Returns { key, title, headers, rows }.
 */
const DEFS = [
  { key: 'claims-volume', title: 'Claims Volume by Status', headers: ['Status', 'Claims', 'Estimated (₹)'], fn: reportRepo.claimsByStatus },
  { key: 'claims-type', title: 'Claims by Type', headers: ['Type', 'Claims', 'Estimated (₹)'], fn: reportRepo.claimsByType },
  { key: 'sla-compliance', title: 'SLA Compliance', headers: ['Bucket', 'Claims'], fn: reportRepo.slaCompliance },
  { key: 'settlement-tat', title: 'Settlement TAT', headers: ['Metric', 'Value'], fn: reportRepo.settlementTat },
  { key: 'fraud-detection', title: 'Fraud Detection (high risk)', headers: ['Claim No.', 'Type', 'Risk', 'Fraud Score', 'Status'], fn: reportRepo.fraudWatch },
  { key: 'agent-performance', title: 'Agent Performance', headers: ['Agent', 'Total', 'Settled', 'Pending Approval'], fn: reportRepo.agentPerformance },
];

async function allReports() {
  const out = [];
  for (const d of DEFS) {
    out.push({ key: d.key, title: d.title, headers: d.headers, rows: await d.fn() });
  }
  return out;
}

async function report(key) {
  const d = DEFS.find((x) => x.key === key);
  if (!d) return null;
  return { key: d.key, title: d.title, headers: d.headers, rows: await d.fn() };
}

module.exports = { allReports, report };
