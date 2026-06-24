'use strict';

const express = require('express');
const { body } = require('express-validator');
const { authJwt } = require('../middleware/authJwt');
const { roleGuard } = require('../middleware/roleGuard');
const { validate } = require('../middleware/validate');
const c = require('../controllers/agent.controller');

const router = express.Router();

router.use(authJwt, roleGuard('AGENT'));

router.get('/dashboard', c.dashboard);
router.get('/claims', c.listClaims);
router.get('/claims/:id', c.claimDetail);
router.post('/claims/:id/acknowledge', c.acknowledge);
router.post(
  '/claims/:id/assign-surveyor',
  [body('surveyorId').isInt({ gt: 0 }).withMessage('Choose a surveyor'), validate],
  c.assignSurveyor
);
router.post('/claims/:id/forward', c.forward);
router.post('/claims/:id/notes', c.saveNote);
router.post(
  '/claims/:id/messages',
  [body('content').trim().notEmpty().withMessage('Message cannot be empty'), validate],
  c.postMessage
);
router.get('/communications', c.communications);

router.get('/claims/:id/settlement', c.settlement);
router.post(
  '/claims/:id/settlement',
  [body('amount').notEmpty().withMessage('Enter a settlement amount'), validate],
  c.processSettlement
);
router.post('/claims/:id/settlement/advance', c.advanceSettlement);

module.exports = router;
