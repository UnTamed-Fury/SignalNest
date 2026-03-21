import { Router } from 'express';
import { authMiddleware } from '../auth/auth.js';
import { getEvents, clearEvents } from './store.js';

const router = Router();

/** GET /app/events — pull buffered events */
router.get('/app/events', authMiddleware, (req, res) => {
  const limit = Math.min(parseInt(req.query.limit ?? '200'), 500);
  res.json({ events: getEvents(limit) });
});

/** DELETE /app/events — clear buffer */
router.delete('/app/events', authMiddleware, (req, res) => {
  clearEvents();
  res.json({ ok: true });
});

export { router as eventsRouter };
