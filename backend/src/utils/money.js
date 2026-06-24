'use strict';

const Decimal = require('decimal.js');

/**
 * Money helpers for DECIMAL(15,2) values. mysql2 returns DECIMAL as strings, and
 * we keep them as strings end-to-end to avoid float precision loss. Use these for
 * any arithmetic (e.g. surveyor net-payable, settlement amounts).
 */

function dec(v) {
  if (v === null || v === undefined || v === '') return new Decimal(0);
  return new Decimal(v);
}

/** Normalize to a 2dp string (DB scale) — safe for JSON and re-insertion. */
function toAmount(v) {
  return dec(v).toFixed(2);
}

function add(...vals) {
  return vals.reduce((acc, v) => acc.plus(dec(v)), new Decimal(0)).toFixed(2);
}

function sub(a, b) {
  return dec(a).minus(dec(b)).toFixed(2);
}

function mul(a, b) {
  return dec(a).times(dec(b)).toFixed(2);
}

/** Clamp to a minimum (e.g. net payable not below zero). */
function atLeast(v, min = 0) {
  const d = dec(v);
  return Decimal.max(d, new Decimal(min)).toFixed(2);
}

function isNegative(v) {
  return dec(v).isNegative();
}

module.exports = { Decimal, dec, toAmount, add, sub, mul, atLeast, isNegative };
