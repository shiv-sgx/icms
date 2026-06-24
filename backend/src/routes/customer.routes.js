'use strict';

const express = require('express');
const { body } = require('express-validator');
const { authJwt } = require('../middleware/authJwt');
const { roleGuard } = require('../middleware/roleGuard');
const { validate } = require('../middleware/validate');
const c = require('../controllers/customer.controller');

const router = express.Router();

// All customer endpoints require an authenticated CUSTOMER (ADMIN passes too).
router.use(authJwt, roleGuard('CUSTOMER'));

router.get('/dashboard', c.dashboard);
router.get('/claims', c.listClaims);
router.get('/claims/:id', c.claimDetail);
router.get('/policies', c.policies);
router.get('/profile', c.profile);

router.post('/claims', c.createClaim);

router.post(
  '/claims/:id/messages',
  [body('content').trim().notEmpty().withMessage('Message cannot be empty'), validate],
  c.postMessage
);

router.post('/claims/:id/withdraw', c.withdraw);

module.exports = router;
