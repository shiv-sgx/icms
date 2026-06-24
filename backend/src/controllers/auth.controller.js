'use strict';

const { asyncH, ok } = require('../utils/http');
const { UnauthorizedError } = require('../utils/errors');
const authService = require('../services/authService');

/** Static help content (ported from faq.jsp) — available to any authenticated user. */
const FAQ = [
  {
    q: 'How do I file a claim?',
    a: 'Go to New Claim, select the relevant policy, fill in the incident details, and submit. You can also save a draft and complete it later.',
  },
  {
    q: 'What documents do I need?',
    a: "Once a claim is created, the Documents section lists what's required for your claim type. Upload them from the claim details page.",
  },
  {
    q: 'How do I track my claim?',
    a: 'Open any claim to see its Status Timeline, from submission through assessment, approval, and settlement.',
  },
  {
    q: 'Can I withdraw a claim?',
    a: 'Yes — until it has been approved or settled. Use the Withdraw Claim button on the claim details page.',
  },
  {
    q: 'How do I contact my agent?',
    a: 'Use the Communication Center on the claim details page to message the team handling your claim.',
  },
];

const login = asyncH(async (req, res) => {
  const { username, password } = req.body;
  const result = await authService.authenticate(username, password, req.ip);
  if (!result) {
    // Single generic message — no user enumeration (mirrors LoginAction).
    throw new UnauthorizedError('Invalid username or password');
  }
  return ok(res, req, result);
});

const logout = asyncH(async (req, res) => {
  await authService.logout(req.user, req.ip);
  return ok(res, req, { ok: true });
});

const me = asyncH(async (req, res) => {
  // The token is the source of truth for identity (stateless).
  return ok(res, req, {
    id: Number(req.user.id),
    username: req.user.username,
    role: req.user.role,
    fullName: req.user.fullName,
  });
});

const faq = asyncH(async (req, res) => ok(res, req, FAQ));

module.exports = { login, logout, me, faq };
