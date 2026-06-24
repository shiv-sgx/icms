'use strict';

const { config } = require('../config/env');

/**
 * Pagination helpers. Page is 1-based; size defaults to ICMS_PAGE_SIZE (15) and is
 * clamped to a sane range to prevent unbounded result sets.
 */
const MAX_SIZE = 100;

function parsePageParams(query) {
  let page = Number.parseInt(query.page, 10);
  let size = Number.parseInt(query.size, 10);
  if (!Number.isInteger(page) || page < 1) page = 1;
  if (!Number.isInteger(size) || size < 1) size = config.pageSize;
  if (size > MAX_SIZE) size = MAX_SIZE;
  return { page, size, offset: (page - 1) * size };
}

/** Standard paginated payload: { items, page, size, total }. */
function paged(items, page, size, total) {
  return { items, page, size, total: Number(total) };
}

module.exports = { parsePageParams, paged, MAX_SIZE };
