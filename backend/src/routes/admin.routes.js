'use strict';

const express = require('express');
const { body } = require('express-validator');
const { authJwt } = require('../middleware/authJwt');
const { roleGuard } = require('../middleware/roleGuard');
const { validate } = require('../middleware/validate');
const c = require('../controllers/admin.controller');

const router = express.Router();

router.use(authJwt, roleGuard('ADMIN'));

router.get('/dashboard', c.dashboard);

router.get('/users', c.listUsers);
router.post(
  '/users',
  [
    body('fullName').trim().notEmpty().withMessage('Full name is required'),
    body('email').trim().isEmail().withMessage('Valid email is required'),
    body('username').trim().notEmpty().withMessage('Username is required'),
    body('password').isLength({ min: 6 }).withMessage('Password must be at least 6 characters'),
    body('roleId').isInt({ gt: 0 }).withMessage('Choose a role'),
    validate,
  ],
  c.createUser
);
router.put('/users/:id', c.updateUser);
router.post('/users/:id/reset-password', c.resetPassword);

router.get('/roles', c.roles);

router.get('/config/sla', c.sla);
router.put('/config/sla/:id', c.updateSla);
router.get('/config/thresholds', c.thresholds);
router.put('/config/thresholds/:id', c.updateThreshold);
router.get('/config/templates', c.templates);
router.put('/config/templates/:id', c.updateTemplate);
router.get('/config/documents', c.documents);
router.post('/config/documents', c.addDocument);
router.delete('/config/documents/:id', c.deleteDocument);

router.get('/audit', c.audit);
router.get('/audit/export', c.exportAudit);

module.exports = router;
