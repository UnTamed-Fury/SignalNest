/**
 * SNRL REST API
 *
 * All routes require Bearer token auth (same token as /app/events).
 *
 * GET    /app/rules          — list all rules
 * POST   /app/rules          — create rule  { name, text }
 * GET    /app/rules/:id      — get one rule
 * PATCH  /app/rules/:id      — update rule  { name?, text?, enabled?, order? }
 * DELETE /app/rules/:id      — delete rule
 * POST   /app/rules/validate — validate rule text without saving { text }
 */
import { Router } from 'express';
import { authMiddleware } from '../auth/auth.js';
import { createRule, updateRule, deleteRule, getRules } from './schema.js';
import { validate } from './validator.js';
import { createLogger } from '../utils/logger.js';

const router = Router();
const log    = createLogger('snrl');

router.get('/app/rules', authMiddleware, (req, res) => {
  res.json({ rules: getRules() });
});

router.post('/app/rules', authMiddleware, (req, res) => {
  try {
    const { name, text } = req.body ?? {};
    if (!text) return res.status(400).json({ error: 'text is required' });
    const rule = createRule(name, text);
    log.info(`Rule created: "${rule.name}"`);
    res.status(201).json({ rule });
  } catch (e) {
    log.warn(`Create rule failed: ${e.message}`);
    res.status(400).json({ error: e.message });
  }
});

router.get('/app/rules/:id', authMiddleware, (req, res) => {
  const rule = getRules().find(r => r.id === req.params.id);
  if (!rule) return res.status(404).json({ error: 'Rule not found' });
  res.json({ rule });
});

router.patch('/app/rules/:id', authMiddleware, (req, res) => {
  try {
    const rule = updateRule(req.params.id, req.body ?? {});
    log.info(`Rule updated: "${rule.name}"`);
    res.json({ rule });
  } catch (e) {
    log.warn(`Update rule failed: ${e.message}`);
    res.status(400).json({ error: e.message });
  }
});

router.delete('/app/rules/:id', authMiddleware, (req, res) => {
  try {
    deleteRule(req.params.id);
    log.info(`Rule deleted: ${req.params.id}`);
    res.status(204).send();
  } catch (e) {
    res.status(404).json({ error: e.message });
  }
});

router.post('/app/rules/validate', authMiddleware, (req, res) => {
  const { text } = req.body ?? {};
  if (!text) return res.status(400).json({ error: 'text is required' });
  const result = validate(text);
  res.json(result.ok
    ? { ok: true, warnings: result.warnings ?? [] }
    : { ok: false, error: result.error });
});

export { router as snrlRoutes };
