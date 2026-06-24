'use strict';

const express = require('express');
const { body } = require('express-validator');
const { authJwt } = require('../middleware/authJwt');
const { roleGuard } = require('../middleware/roleGuard');
const { validate } = require('../middleware/validate');
const c = require('../controllers/manager.controller');

const router = express.Router();

router.use(authJwt, roleGuard('MANAGER'));

router.get('/dashboard', c.dashboard);
router.get('/approvals', c.approvals);
router.get('/claims/:id', c.claimDetail);
router.post(
  '/claims/:id/decision',
  [body('decision').trim().notEmpty().withMessage('Choose a decision'), validate],
  c.decide
);
router.post(
  '/claims/:id/settlement/override',
  [body('amount').notEmpty().withMessage('Enter an override amount'), validate],
  c.overrideSettlement
);
router.get('/reports', c.reports);
// GET /reports/:type/export (CSV) is added in Phase 5.

module.exports = router;
