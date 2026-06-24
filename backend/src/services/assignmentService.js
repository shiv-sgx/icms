'use strict';

const { knex } = require('../db/knex');
const { withTransaction } = require('../db/tx');
const claimRepo = require('../repositories/claimRepo');
const userRepo = require('../repositories/userRepo');
const notifications = require('./notificationService');
const audit = require('./auditService');
const claimStatus = require('../domain/claimStatus');
const { NotFoundError, ValidationError, ConflictError } = require('../utils/errors');
const logger = require('../config/logger');

/** Active surveyors for the assignment dropdown. */
async function availableSurveyors() {
  const rows = await userRepo.findActiveByRole(knex, 'SURVEYOR');
  return rows.map((u) => ({ id: Number(u.id), fullName: u.fullName, branch: u.branch }));
}

/** Assigns a surveyor, moves the claim to SURVEY_SCHEDULED, notifies the surveyor. */
async function assignSurveyor(agent, claimId, surveyorId, ip) {
  return withTransaction(async (trx) => {
    const claim = await claimRepo.findById(trx, claimId);
    if (!claim) throw new NotFoundError('Claim not found.');
    if (claimStatus.isTerminal(claim.status)) {
      throw new ConflictError('Cannot assign a surveyor to a closed claim.');
    }
    const surveyor = await userRepo.findById(trx, surveyorId);
    if (!surveyor || String(surveyor.roleName).toUpperCase() !== 'SURVEYOR') {
      throw new ValidationError('Please choose a valid surveyor.');
    }
    await claimRepo.assignSurveyor(trx, claimId, surveyorId, Number(agent.id), claimStatus.SURVEY_SCHEDULED);
    await notifications.notifyUser(
      trx,
      surveyorId,
      'ACTION',
      `You have been assigned to survey claim ${claim.claimNo}.`
    );
    await audit.record(trx, {
      userId: Number(agent.id),
      username: agent.username,
      role: agent.role,
      action: 'SURVEYOR_ASSIGNED',
      entity: `${claim.claimNo} -> ${surveyor.fullName}`,
      ipAddress: ip,
      result: 'SUCCESS',
    });
    logger.info({ agent: agent.username, surveyor: surveyor.username, claimNo: claim.claimNo }, 'Surveyor assigned');
  });
}

module.exports = { availableSurveyors, assignSurveyor };
