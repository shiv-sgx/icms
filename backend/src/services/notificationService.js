'use strict';

const { knex } = require('../db/knex');
const notificationRepo = require('../repositories/notificationRepo');

/** Creates/reads in-app notifications — Node port of NotificationService. */

/** Role broadcast — within the caller's transaction. */
async function notifyRole(trx, role, type, message) {
  return notificationRepo.insert(trx, { targetRole: role, type, message });
}

/** Single-user notification — within the caller's transaction. */
async function notifyUser(trx, userId, type, message) {
  return notificationRepo.insert(trx, { userId, type, message });
}

async function recentForUser(userId, role, limit) {
  return notificationRepo.findForUser(knex, userId, role, limit);
}

module.exports = { notifyRole, notifyUser, recentForUser };
