'use strict';

/** Persistence for `notifications` — Knex port of NotificationDao. */

const COLS = [
  'id',
  'user_id as userId',
  'target_role as targetRole',
  'type',
  'message',
  'is_read as isRead',
  'created_at as createdAt',
];

/** Notifications addressed to a user OR broadcast to their role. */
async function findForUser(db, userId, role, limit) {
  return db('notifications')
    .select(COLS)
    .where({ user_id: userId })
    .orWhere({ target_role: role })
    .orderBy('created_at', 'desc')
    .limit(limit);
}

async function insert(db, n) {
  const [id] = await db('notifications').insert({
    user_id: n.userId ?? null,
    target_role: n.targetRole ?? null,
    type: n.type || 'INFO',
    message: n.message,
    is_read: n.isRead ? 1 : 0,
  });
  return id;
}

module.exports = { findForUser, insert };
