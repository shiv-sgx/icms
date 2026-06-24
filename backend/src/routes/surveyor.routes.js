'use strict';

const express = require('express');
const { authJwt } = require('../middleware/authJwt');
const { roleGuard } = require('../middleware/roleGuard');
const { single } = require('../utils/fileUpload');
const c = require('../controllers/surveyor.controller');

const router = express.Router();

router.use(authJwt, roleGuard('SURVEYOR'));

router.get('/dashboard', c.dashboard);
router.get('/claims/:id/assessment', c.assessment);
router.post('/claims/:id/assessment', c.submitAssessment);
router.post('/claims/:id/report', single('file'), c.uploadReport);

module.exports = router;
