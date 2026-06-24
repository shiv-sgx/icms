'use strict';

const { knex } = require('../db/knex');
const { withTransaction } = require('../db/tx');
const communicationRepo = require('../repositories/communicationRepo');

/** Claim message thread — Node port of CommunicationService. */

async function forClaim(claimId) {
  return communicationRepo.findByClaim(knex, claimId);
}

/** A user-authored message (own transaction). */
async function postMessage(actor, claimId, content) {
  return withTransaction((trx) =>
    communicationRepo.insert(trx, {
      claimId,
      senderId: Number(actor.id),
      senderName: actor.fullName,
      channel: 'MESSAGE',
      content,
    })
  );
}

/** A system-generated message inside the caller's transaction. */
async function system(trx, claimId, senderName, content) {
  return communicationRepo.insert(trx, { claimId, senderName, channel: 'MESSAGE', content });
}

module.exports = { forClaim, postMessage, system };
