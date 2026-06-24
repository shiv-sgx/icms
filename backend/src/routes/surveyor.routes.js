'use strict';

const express = require('express');
const { authJwt } = require('../middleware/authJwt');
const { roleGuard } = require('../middleware/roleGuard');
const c = require('../controllers/surveyor.controller');

const router = express.Router();

router.use(authJwt, roleGuard('SURVEYOR'));

router.get('/dashboard', c.dashboard);
router.get('/claims/:id/assessment', c.assessment);
router.post('/claims/:id/assessment', c.submitAssessment);
// POST /claims/:id/report (survey report upload) is added in Phase 5.

module.exports = router;
