'use strict';

const express = require('express');
const { body } = require('express-validator');
const { validate } = require('../middleware/validate');
const { authJwt } = require('../middleware/authJwt');
const controller = require('../controllers/auth.controller');

const router = express.Router();

router.post(
  '/login',
  [
    body('username').trim().notEmpty().withMessage('Username is required'),
    body('password').notEmpty().withMessage('Password is required'),
    validate,
  ],
  controller.login
);

// Authenticated, stateless endpoints
router.post('/logout', authJwt, controller.logout);
router.get('/me', authJwt, controller.me);
router.get('/faq', authJwt, controller.faq);

module.exports = router;
