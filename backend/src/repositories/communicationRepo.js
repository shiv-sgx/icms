'use strict';

/** Persistence for `communications` — Knex port of CommunicationDao. */

const COLS = [
  'id',
  'claim_id as claimId',
  'sender_id as senderId',
  'sender_name as senderName',
  'channel',
  'content',
  'created_at as createdAt',
];

async function findByClaim(db, claimId) {
  return db('communications').select(COLS).where({ claim_id: claimId }).orderBy('created_at', 'asc');
}

/** Most recent messages across all claims (with claim number) — comms feed. */
async function findRecent(db, limit) {
  return db('communications as m')
    .join('claims as cl', 'cl.id', 'm.claim_id')
    .select([
      'm.id',
      'm.claim_id as claimId',
      'm.sender_id as senderId',
      'm.sender_name as senderName',
      'm.channel',
      'm.content',
      'm.created_at as createdAt',
      'cl.claim_no as claimNo',
    ])
    .orderBy('m.created_at', 'desc')
    .limit(limit);
}

async function insert(db, c) {
  const [id] = await db('communications').insert({
    claim_id: c.claimId,
    sender_id: c.senderId ?? null,
    sender_name: c.senderName ?? null,
    channel: c.channel || 'MESSAGE',
    content: c.content,
  });
  return id;
}

module.exports = { findByClaim, findRecent, insert };
