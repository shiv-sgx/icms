'use strict';

/**
 * Minimal RFC-4180 CSV serialisation — Node port of CsvWriter, hardened with a
 * CSV-injection guard: cells beginning with = + - @ (or tab/CR) are prefixed with
 * a single quote so spreadsheet apps don't execute them as formulas.
 */
function escape(value) {
  let v = value == null ? '' : String(value);
  if (/^[=+\-@\t\r]/.test(v)) v = `'${v}`;
  if (v.includes(',') || v.includes('"') || v.includes('\n') || v.includes('\r')) {
    return `"${v.replace(/"/g, '""')}"`;
  }
  return v;
}

function line(fields) {
  return fields.map(escape).join(',') + '\r\n';
}

/** Build a CSV string from headers + rows (array of arrays). */
function toCsv(headers, rows) {
  let out = line(headers);
  for (const row of rows) out += line(row);
  return out;
}

module.exports = { toCsv };
